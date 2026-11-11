package com.ayushman.intellicampus.ai;

/** Per-student AI provider configuration. API keys are never logged. */
public class AiProviderConfig {

    public static final String FREELLMAPI = "FreeLLMAPI";
    public static final String OPENAI = "OpenAI";
    public static final String GEMINI = "Google Gemini";
    public static final String ANTHROPIC = "Anthropic Claude";
    public static final String COHERE = "Cohere";
    public static final String GROQ = "Groq";
    public static final String OPENROUTER = "OpenRouter";
    public static final String TOGETHER = "Together AI";
    public static final String FIREWORKS = "Fireworks AI";
    public static final String MISTRAL = "Mistral AI";
    public static final String DEEPSEEK = "DeepSeek";
    public static final String XAI = "xAI";
    public static final String PERPLEXITY = "Perplexity";
    public static final String CEREBRAS = "Cerebras";
    public static final String SAMBANOVA = "SambaNova";
    public static final String CUSTOM = "Custom OpenAI-compatible";

    private final String provider;
    private final String baseUrl;
    private final String model;
    private final String apiKey;

    public AiProviderConfig(String provider, String baseUrl, String model, String apiKey) {
        this.provider = provider == null ? FREELLMAPI : provider.trim();
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim();
        this.model = model == null ? "" : model.trim();
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    public String getProvider() { return provider; }
    public String getBaseUrl() { return baseUrl; }
    public String getModel() { return model; }
    public String getApiKey() { return apiKey; }

    /** True when this provider has a distinct first-party request protocol. */
    public static boolean usesNativeProtocol(String provider) {
        return OPENAI.equals(provider) || GEMINI.equals(provider)
                || ANTHROPIC.equals(provider) || COHERE.equals(provider)
                || XAI.equals(provider);
    }

    public static String defaultBaseUrl(String provider) {
        if (OPENAI.equals(provider)) return "https://api.openai.com/v1";
        if (GEMINI.equals(provider)) return "https://generativelanguage.googleapis.com/v1beta";
        if (ANTHROPIC.equals(provider)) return "https://api.anthropic.com";
        if (COHERE.equals(provider)) return "https://api.cohere.com";
        if (GROQ.equals(provider)) return "https://api.groq.com/openai/v1";
        if (OPENROUTER.equals(provider)) return "https://openrouter.ai/api/v1";
        if (TOGETHER.equals(provider)) return "https://api.together.xyz/v1";
        if (FIREWORKS.equals(provider)) return "https://api.fireworks.ai/inference/v1";
        if (MISTRAL.equals(provider)) return "https://api.mistral.ai/v1";
        if (DEEPSEEK.equals(provider)) return "https://api.deepseek.com";
        if (XAI.equals(provider)) return "https://api.x.ai/v1";
        if (PERPLEXITY.equals(provider)) return "https://api.perplexity.ai";
        if (CEREBRAS.equals(provider)) return "https://api.cerebras.ai/v1";
        if (SAMBANOVA.equals(provider)) return "https://api.sambanova.ai/v1";
        if (CUSTOM.equals(provider)) return "";
        // Keep the existing IntelliCampus FreeLLMAPI deployment unchanged.
        return "https://intellicampus-ai-e4pw.onrender.com/v1";
    }

    /** Defaults are editable; use Fetch Models to verify what the key can access. */
    public static String defaultModel(String provider) {
        if (OPENAI.equals(provider)) return "gpt-4.1-mini";
        if (GEMINI.equals(provider)) return "gemini-3.5-flash-lite";
        if (ANTHROPIC.equals(provider)) return "claude-haiku-4-5-20251001";
        if (COHERE.equals(provider)) return "command-a-03-2025";
        if (GROQ.equals(provider)) return "llama-3.3-70b-versatile";
        if (OPENROUTER.equals(provider)) return "openai/gpt-4.1-mini";
        if (TOGETHER.equals(provider)) return "meta-llama/Llama-3.3-70B-Instruct-Turbo";
        if (FIREWORKS.equals(provider)) return "accounts/fireworks/models/llama-v3p3-70b-instruct";
        if (MISTRAL.equals(provider)) return "mistral-small-latest";
        if (DEEPSEEK.equals(provider)) return "deepseek-chat";
        if (XAI.equals(provider)) return "grok-3-mini";
        if (PERPLEXITY.equals(provider)) return "sonar";
        if (CEREBRAS.equals(provider)) return "llama-3.3-70b";
        if (SAMBANOVA.equals(provider)) return "Meta-Llama-3.3-70B-Instruct";
        if (CUSTOM.equals(provider)) return "";
        return "auto";
    }

    public String normalizedBaseUrl() {
        String value = baseUrl.trim();
        while (value.endsWith("/")) value = value.substring(0, value.length() - 1);
        return value;
    }
}
