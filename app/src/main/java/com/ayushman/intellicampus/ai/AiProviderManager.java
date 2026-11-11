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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Provider-aware AI Tutor client.
 *
 * Uses first-party protocols for OpenAI Responses, Gemini generateContent,
 * Anthropic Messages and Cohere Chat. Providers whose documented public API
 * is OpenAI-compatible use their official endpoint and authentication.
 */
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
        validateCredentialsAndUrl(config);
        if (config.getModel().trim().isEmpty()) {
            throw new IllegalArgumentException("Enter a model identifier or use Fetch Models.");
        }
    }

    private void validateCredentialsAndUrl(AiProviderConfig config) throws Exception {
        if (config == null) throw new IllegalArgumentException("Choose an AI provider.");
        if (config.getApiKey().trim().isEmpty()) throw new IllegalArgumentException("Enter your API key.");
        String base = config.normalizedBaseUrl();
        if (base.isEmpty()) throw new IllegalArgumentException("The provider base URL is missing.");
        URL parsed = new URL(base);
        if (!"https".equalsIgnoreCase(parsed.getProtocol())) {
            throw new IllegalArgumentException("For security, API endpoints must use HTTPS.");
        }
        if (parsed.getHost() == null || parsed.getHost().trim().isEmpty()) {
            throw new IllegalArgumentException("The provider base URL is invalid.");
        }
    }

    public String createChatCompletion(AiProviderConfig config, JSONArray messages,
                                       double temperature) throws Exception {
        validate(config);
        if (AiProviderConfig.OPENAI.equals(config.getProvider())
                || AiProviderConfig.XAI.equals(config.getProvider())) {
            return executeResponsesProtocol(config, messages, temperature);
        }
        if (AiProviderConfig.GEMINI.equals(config.getProvider())) {
            return executeGemini(config, messages, temperature);
        }
        if (AiProviderConfig.ANTHROPIC.equals(config.getProvider())) {
            return executeAnthropic(config, messages, temperature);
        }
        if (AiProviderConfig.COHERE.equals(config.getProvider())) {
            return executeCohere(config, messages, temperature);
        }
        return executeOfficialChatCompletions(config, messages, temperature);
    }

    public String testConnection(AiProviderConfig config) throws Exception {
        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "user")
                .put("content", "Reply with the single word OK."));
        String answer = createChatCompletion(config, messages, 0);
        return answer.isEmpty() ? "Connection successful."
                : "Connection successful. Model replied: " + answer;
    }

    /** Friendly model metadata returned by provider catalogue APIs. */
    public static class AiModelInfo {
        private final String id;
        private final String displayName;
        private final boolean recommended;

        public AiModelInfo(String id, String displayName, boolean recommended) {
            this.id = id == null ? "" : id;
            this.displayName = displayName == null || displayName.trim().isEmpty()
                    ? this.id : displayName.trim();
            this.recommended = recommended;
        }

        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public boolean isRecommended() { return recommended; }

        @Override public String toString() {
            return displayName + "  (" + id + ")" + (recommended ? "  • Recommended" : "");
        }
    }

    /**
     * Fetch provider catalogue entries. Gemini results are restricted to models
     * advertising generateContent support; catalogue presence is not treated as
     * proof of account entitlement, so the selected model is still connection-tested.
     */
    public List<AiModelInfo> fetchModels(AiProviderConfig config) throws Exception {
        validateCredentialsAndUrl(config);
        String provider = config.getProvider();
        String endpoint;
        if (AiProviderConfig.GEMINI.equals(provider)) {
            endpoint = config.normalizedBaseUrl() + "/models?pageSize=100";
        } else if (AiProviderConfig.ANTHROPIC.equals(provider)) {
            endpoint = config.normalizedBaseUrl() + "/v1/models?limit=100";
        } else if (AiProviderConfig.COHERE.equals(provider)) {
            endpoint = config.normalizedBaseUrl() + "/v1/models";
        } else {
            endpoint = config.normalizedBaseUrl() + "/models";
        }

        JSONObject response = request(config, endpoint, null, "GET", authMode(provider));
        JSONArray items = response.optJSONArray("data");
        if (items == null) items = response.optJSONArray("models");
        if (items == null) items = response.optJSONArray("items");
        if (items == null) {
            throw new Exception("This provider did not return a supported model catalogue. You can enter a model ID manually.");
        }

        java.util.LinkedHashMap<String, AiModelInfo> unique = new java.util.LinkedHashMap<>();
        String preferred = AiProviderConfig.defaultModel(provider);
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.optJSONObject(i);
            if (item == null) continue;

            if (AiProviderConfig.GEMINI.equals(provider)) {
                JSONArray methods = item.optJSONArray("supportedGenerationMethods");
                boolean supportsGenerate = false;
                if (methods != null) {
                    for (int j = 0; j < methods.length(); j++) {
                        if ("generateContent".equalsIgnoreCase(methods.optString(j))) {
                            supportsGenerate = true;
                            break;
                        }
                    }
                }
                if (!supportsGenerate) continue;
            }

            String id = firstNonEmpty(item.optString("id", ""),
                    item.optString("name", ""), item.optString("model", ""));
            if (id.startsWith("models/")) id = id.substring("models/".length());
            if (id.isEmpty()) continue;

            String displayName = firstNonEmpty(item.optString("displayName", ""),
                    item.optString("name", ""), item.optString("title", ""), id);
            if (displayName.startsWith("models/")) displayName = id;
            unique.put(id, new AiModelInfo(id, displayName, id.equals(preferred)));
        }

        List<AiModelInfo> result = new ArrayList<>(unique.values());
        Collections.sort(result, (a, b) -> {
            if (a.isRecommended() != b.isRecommended()) return a.isRecommended() ? -1 : 1;
            int nameOrder = a.getDisplayName().compareToIgnoreCase(b.getDisplayName());
            return nameOrder != 0 ? nameOrder : a.getId().compareToIgnoreCase(b.getId());
        });
        if (result.isEmpty()) {
            throw new Exception("No models supporting this provider's chat operation were returned. Check API access or enter a model ID manually.");
        }
        return result;
    }

    private String executeResponsesProtocol(AiProviderConfig config, JSONArray messages,
                                            double temperature) throws Exception {
        JSONArray input = new JSONArray();
        for (int i = 0; i < messages.length(); i++) {
            JSONObject message = messages.getJSONObject(i);
            String role = message.optString("role", "user");
            if ("system".equals(role)) role = "developer";
            input.put(new JSONObject()
                    .put("role", role)
                    .put("content", message.optString("content", "")));
        }
        JSONObject body = new JSONObject();
        body.put("model", config.getModel());
        body.put("input", input);
        // Responses API models may reject temperature (notably reasoning models),
        // so leave it at the provider default for broad model compatibility.
        JSONObject response = request(config, config.normalizedBaseUrl() + "/responses",
                body, "POST", "bearer");

        String outputText = response.optString("output_text", "");
        if (!outputText.trim().isEmpty()) return outputText.trim();
        JSONArray output = response.optJSONArray("output");
        StringBuilder answer = new StringBuilder();
        if (output != null) {
            for (int i = 0; i < output.length(); i++) {
                JSONObject block = output.optJSONObject(i);
                JSONArray content = block == null ? null : block.optJSONArray("content");
                if (content == null) continue;
                for (int j = 0; j < content.length(); j++) {
                    JSONObject part = content.optJSONObject(j);
                    if (part == null) continue;
                    String text = part.optString("text", "");
                    if (!text.isEmpty()) {
                        if (answer.length() > 0) answer.append("\n");
                        answer.append(text);
                    }
                }
            }
        }
        if (answer.length() == 0) throw new Exception(config.getProvider() + " returned no text output.");
        return answer.toString().trim();
    }

    private String executeGemini(AiProviderConfig config, JSONArray messages,
                                 double temperature) throws Exception {
        JSONArray contents = new JSONArray();
        String systemInstruction = "";
        for (int i = 0; i < messages.length(); i++) {
            JSONObject item = messages.getJSONObject(i);
            String role = item.optString("role", "user");
            String content = item.optString("content", "");
            if ("system".equals(role) || "developer".equals(role)) {
                systemInstruction = systemInstruction.isEmpty()
                        ? content : systemInstruction + "\n\n" + content;
                continue;
            }
            JSONObject part = new JSONObject().put("text", content);
            contents.put(new JSONObject()
                    .put("role", "assistant".equals(role) ? "model" : "user")
                    .put("parts", new JSONArray().put(part)));
        }
        JSONObject body = new JSONObject().put("contents", contents);
        body.put("generationConfig", new JSONObject().put("temperature", temperature));
        if (!systemInstruction.isEmpty()) {
            body.put("systemInstruction", new JSONObject().put("parts",
                    new JSONArray().put(new JSONObject().put("text", systemInstruction))));
        }
        String endpoint = config.normalizedBaseUrl() + "/models/" +
                URLEncoder.encode(config.getModel(), "UTF-8") + ":generateContent";
        JSONObject response = request(config, endpoint, body, "POST", "gemini");
        JSONArray candidates = response.optJSONArray("candidates");
        if (candidates == null || candidates.length() == 0) {
            throw new Exception("Gemini returned no candidates. Check model access and safety settings.");
        }
        JSONObject content = candidates.getJSONObject(0).optJSONObject("content");
        JSONArray parts = content == null ? null : content.optJSONArray("parts");
        StringBuilder answer = new StringBuilder();
        if (parts != null) {
            for (int i = 0; i < parts.length(); i++) {
                String text = parts.getJSONObject(i).optString("text", "");
                if (!text.isEmpty()) {
                    if (answer.length() > 0) answer.append("\n");
                    answer.append(text);
                }
            }
        }
        if (answer.length() == 0) throw new Exception("Gemini returned an empty response.");
        return answer.toString().trim();
    }

    private String executeAnthropic(AiProviderConfig config, JSONArray messages,
                                    double temperature) throws Exception {
        JSONArray conversation = new JSONArray();
        String system = "";
        for (int i = 0; i < messages.length(); i++) {
            JSONObject item = messages.getJSONObject(i);
            String role = item.optString("role", "user");
            if ("system".equals(role) || "developer".equals(role)) {
                system = system.isEmpty() ? item.optString("content", "")
                        : system + "\n\n" + item.optString("content", "");
            } else {
                conversation.put(new JSONObject()
                        .put("role", "assistant".equals(role) ? "assistant" : "user")
                        .put("content", item.optString("content", "")));
            }
        }
        JSONObject body = new JSONObject()
                .put("model", config.getModel())
                .put("max_tokens", 4096)
                .put("temperature", temperature)
                .put("messages", conversation);
        if (!system.isEmpty()) body.put("system", system);
        JSONObject response = request(config, config.normalizedBaseUrl() + "/v1/messages",
                body, "POST", "anthropic");
        JSONArray content = response.optJSONArray("content");
        StringBuilder answer = new StringBuilder();
        if (content != null) {
            for (int i = 0; i < content.length(); i++) {
                JSONObject block = content.optJSONObject(i);
                if (block != null && "text".equals(block.optString("type"))) {
                    if (answer.length() > 0) answer.append("\n");
                    answer.append(block.optString("text", ""));
                }
            }
        }
        if (answer.length() == 0) throw new Exception("Anthropic returned an empty response.");
        return answer.toString().trim();
    }

    private String executeCohere(AiProviderConfig config, JSONArray messages,
                                 double temperature) throws Exception {
        JSONArray conversation = new JSONArray();
        for (int i = 0; i < messages.length(); i++) {
            JSONObject item = messages.getJSONObject(i);
            String role = item.optString("role", "user");
            conversation.put(new JSONObject()
                    .put("role", "system".equals(role) || "developer".equals(role) ? "system"
                            : ("assistant".equals(role) ? "assistant" : "user"))
                    .put("content", item.optString("content", "")));
        }
        JSONObject body = new JSONObject()
                .put("model", config.getModel())
                .put("messages", conversation)
                .put("temperature", temperature);
        JSONObject response = request(config, config.normalizedBaseUrl() + "/v2/chat",
                body, "POST", "bearer");
        JSONObject message = response.optJSONObject("message");
        JSONArray content = message == null ? null : message.optJSONArray("content");
        StringBuilder answer = new StringBuilder();
        if (content != null) {
            for (int i = 0; i < content.length(); i++) {
                JSONObject block = content.optJSONObject(i);
                if (block != null) {
                    String text = block.optString("text", "");
                    if (!text.isEmpty()) {
                        if (answer.length() > 0) answer.append("\n");
                        answer.append(text);
                    }
                }
            }
        }
        if (answer.length() == 0) throw new Exception("Cohere returned an empty response.");
        return answer.toString().trim();
    }

    private String executeOfficialChatCompletions(AiProviderConfig config,
                                                   JSONArray messages,
                                                   double temperature) throws Exception {
        JSONObject body = new JSONObject()
                .put("model", config.getModel())
                .put("messages", messages)
                .put("temperature", temperature);
        JSONObject response = request(config,
                config.normalizedBaseUrl() + "/chat/completions",
                body, "POST", "bearer");
        JSONArray choices = response.optJSONArray("choices");
        if (choices == null || choices.length() == 0) {
            throw new Exception("The provider returned no response choices.");
        }
        JSONObject message = choices.getJSONObject(0).optJSONObject("message");
        if (message == null) throw new Exception("The provider returned an unsupported response format.");
        Object content = message.opt("content");
        String answer = content == null || content == JSONObject.NULL
                ? "" : String.valueOf(content).trim();
        if (answer.isEmpty()) throw new Exception("The model returned an empty response.");
        return answer;
    }

    private String authMode(String provider) {
        if (AiProviderConfig.GEMINI.equals(provider)) return "gemini";
        if (AiProviderConfig.ANTHROPIC.equals(provider)) return "anthropic";
        return "bearer";
    }

    private JSONObject request(AiProviderConfig config, String endpoint,
                               JSONObject body, String method,
                               String authMode) throws Exception {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL(endpoint).openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(90000);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            if ("gemini".equals(authMode)) {
                connection.setRequestProperty("x-goog-api-key", config.getApiKey());
            } else if ("anthropic".equals(authMode)) {
                connection.setRequestProperty("x-api-key", config.getApiKey());
                connection.setRequestProperty("anthropic-version", "2023-06-01");
            } else {
                connection.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
            }
            if (AiProviderConfig.OPENROUTER.equals(config.getProvider())) {
                connection.setRequestProperty("HTTP-Referer",
                        "https://github.com/AyushmanMishra-17/Intellicampus");
                connection.setRequestProperty("X-Title", "IntelliCampus");
            }
            if (body != null) {
                connection.setDoOutput(true);
                byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
                try (OutputStream output = connection.getOutputStream()) {
                    output.write(payload);
                }
            }
            int status = connection.getResponseCode();
            InputStream stream = status >= 200 && status < 300
                    ? connection.getInputStream() : connection.getErrorStream();
            String responseText = readStream(stream);
            if (status < 200 || status >= 300) {
                throw new Exception(describeHttpError(status, responseText));
            }
            return new JSONObject(responseText);
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) return value.trim();
        }
        return "";
    }

    private String describeHttpError(int status, String response) {
        String detail = response == null ? "" : response.trim();
        if (detail.length() > 700) detail = detail.substring(0, 700) + "…";
        if (status == 400)
            return "The provider rejected the request (HTTP 400). Check the model, request options and API version."
                    + (detail.isEmpty() ? "" : "\n\nProvider details: " + detail);
        if (status == 401)
            return "Invalid API key (HTTP 401). Check that the key belongs to the selected provider.";
        if (status == 403)
            return "Access denied (HTTP 403). The key may lack permission, billing, project or regional access."
                    + (detail.isEmpty() ? "" : "\n\nProvider details: " + detail);
        if (status == 404)
            return "Model or endpoint not found (HTTP 404). Refresh available models and select a compatible model. "
                    + "A model listed by the provider may still be unavailable to your account."
                    + (detail.isEmpty() ? "" : "\n\nProvider details: " + detail);
        if (status == 429)
            return "Rate limit or quota reached (HTTP 429). Check your provider's usage limits or billing."
                    + (detail.isEmpty() ? "" : "\n\nProvider details: " + detail);
        return "Provider request failed (HTTP " + status + ")."
                + (detail.isEmpty() ? "" : "\n\nProvider details: " + detail);
    }

    private String readStream(InputStream stream) throws Exception {
        if (stream == null) return "";
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) result.append(line);
        }
        return result.toString();
    }
}
