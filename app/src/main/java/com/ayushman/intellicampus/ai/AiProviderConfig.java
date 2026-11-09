package com.ayushman.intellicampus.ai;

/** Per-student AI provider configuration. API keys are never logged. */
public class AiProviderConfig {

    public static final String FREELLMAPI = "FreeLLMAPI";
    public static final String OPENAI = "OpenAI";
    public static final String GROQ = "Groq";
    public static final String OPENROUTER = "OpenRouter";
    public static final String TOGETHER = "Together AI";
    public static final String FIREWORKS = "Fireworks AI";
    public static final String CUSTOM = "Custom OpenAI-compatible";

    private String provider;
    private String baseUrl;
    private String model;
    private String apiKey;

    public AiProviderConfig(String provider, String baseUrl,
                            String model, String apiKey) {
        this.provider = provider == null ? FREELLMAPI : provider.trim();
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim();
        this.model = model == null ? "" : model.trim();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    public String getProvider() { return provider; }
    public String getBaseUrl() { return baseUrl; }
    public String getModel() { return model; }
    public String getApiKey() { return apiKey; }

    public static String defaultBaseUrl(String provider) {
        if (OPENAI.equals(provider)) return "https://api.openai.com/v1";
        if (GROQ.equals(provider)) return "https://api.groq.com/openai/v1";
        if (OPENROUTER.equals(provider)) return "https://openrouter.ai/api/v1";
        if (TOGETHER.equals(provider)) return "https://api.together.xyz/v1";
        if (FIREWORKS.equals(provider)) return "https://api.fireworks.ai/inference/v1";
        if (CUSTOM.equals(provider)) return "";
        // Keep the currently verified IntelliCampus FreeLLMAPI deployment.
        return "https://intellicampus-ai-e4pw.onrender.com/v1";
    }

    public static String defaultModel(String provider) {
        if (OPENAI.equals(provider)) return "gpt-4.1-mini";
        if (GROQ.equals(provider)) return "llama-3.3-70b-versatile";
        if (OPENROUTER.equals(provider)) return "openai/gpt-4.1-mini";
        if (TOGETHER.equals(provider)) return "meta-llama/Llama-3.3-70B-Instruct-Turbo";
        if (FIREWORKS.equals(provider)) return "accounts/fireworks/models/llama-v3p3-70b-instruct";
        if (CUSTOM.equals(provider)) return "";
        return "auto";
    }

    public String normalizedBaseUrl() {
        String value = baseUrl.trim();
        while (value.endsWith("/")) value = value.substring(0, value.length() - 1);
        return value;
    }
}
