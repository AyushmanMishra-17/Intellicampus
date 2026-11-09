package com.ayushman.intellicampus.ai;

import android.content.Context;

import com.ayushman.intellicampus.security.SecureAiConfigStore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** Handles configuration and OpenAI-compatible chat-completion requests. */
public class AiProviderManager {

    private final Context appContext;
    private final String accountId;

    public AiProviderManager(Context context, String accountId) {
        appContext = context.getApplicationContext();
        this.accountId = accountId == null ? "signed_out" : accountId;
    }

    public AiProviderConfig loadConfig() throws Exception {
        return SecureAiConfigStore.load(appContext, accountId);
    }

    public void saveConfig(AiProviderConfig config) throws Exception {
        validate(config);
        SecureAiConfigStore.save(appContext, accountId, config);
    }

    public void clearConfig() {
        SecureAiConfigStore.clear(appContext, accountId);
    }

    public void validate(AiProviderConfig config) throws Exception {
        if (config == null) throw new IllegalArgumentException("Choose an AI provider.");
        if (config.getApiKey().trim().isEmpty()) throw new IllegalArgumentException("Enter your API key.");
        if (config.getModel().trim().isEmpty()) throw new IllegalArgumentException("Enter a model identifier.");
        String base = config.normalizedBaseUrl();
        if (base.isEmpty()) throw new IllegalArgumentException("Enter the API base URL.");
        URL parsed = new URL(base);
        if (!"https".equalsIgnoreCase(parsed.getProtocol())) {
            throw new IllegalArgumentException("For security, API endpoints must use HTTPS.");
        }
        if (parsed.getHost() == null || parsed.getHost().trim().isEmpty()) {
            throw new IllegalArgumentException("Enter a valid API base URL.");
        }
    }

    public String createChatCompletion(AiProviderConfig config, JSONArray messages,
                                       double temperature) throws Exception {
        validate(config);
        JSONObject body = new JSONObject();
        body.put("model", config.getModel());
        body.put("messages", messages);
        body.put("temperature", temperature);
        return execute(config, body);
    }

    public String testConnection(AiProviderConfig config) throws Exception {
        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", "Reply with the single word OK.");
        messages.put(message);
        JSONObject body = new JSONObject();
        body.put("model", config.getModel());
        body.put("messages", messages);
        body.put("temperature", 0);
        body.put("max_tokens", 8);
        String answer = execute(config, body);
        return answer.isEmpty() ? "Connection successful." : "Connection successful. Model replied: " + answer;
    }

    private String execute(AiProviderConfig config, JSONObject body) throws Exception {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(config.normalizedBaseUrl() + "/chat/completions");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(90000);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + config.getApiKey());

            if (AiProviderConfig.OPENROUTER.equals(config.getProvider())) {
                connection.setRequestProperty("HTTP-Referer", "https://github.com/AyushmanMishra-17/Intellicampus");
                connection.setRequestProperty("X-Title", "IntelliCampus");
            }

            byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
            try (OutputStream output = connection.getOutputStream()) {
                output.write(payload);
            }

            int status = connection.getResponseCode();
            InputStream stream = status >= 200 && status < 300
                    ? connection.getInputStream() : connection.getErrorStream();
            String response = readStream(stream);
            if (status < 200 || status >= 300) {
                throw new Exception(describeHttpError(status, response));
            }

            JSONObject json = new JSONObject(response);
            JSONArray choices = json.optJSONArray("choices");
            if (choices == null || choices.length() == 0) {
                throw new Exception("The provider returned no response choices.");
            }
            JSONObject message = choices.getJSONObject(0).optJSONObject("message");
            if (message == null) throw new Exception("The provider returned an unsupported response format.");
            Object content = message.opt("content");
            String answer = content == null || content == JSONObject.NULL ? "" : String.valueOf(content).trim();
            if (answer.isEmpty()) throw new Exception("The model returned an empty response.");
            return answer;
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private String describeHttpError(int status, String response) {
        String detail = response == null ? "" : response.trim();
        if (detail.length() > 700) detail = detail.substring(0, 700) + "…";
        if (status == 401 || status == 403) return "Authentication failed (HTTP " + status + "). Check the API key and provider permissions.";
        if (status == 404) return "Endpoint or model not found (HTTP 404). Check the base URL and model identifier.";
        if (status == 429) return "Rate limit or quota reached (HTTP 429). Check your provider's usage limits.";
        return "AI provider returned HTTP " + status + (detail.isEmpty() ? "." : ": " + detail);
    }

    private String readStream(InputStream stream) throws Exception {
        if (stream == null) return "";
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) result.append(line);
        }
        return result.toString();
    }
}
