package com.ayushman.intellicampus.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import com.ayushman.intellicampus.ai.AiProviderConfig;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/** Encrypts each student's AI provider configuration with an Android Keystore AES key. */
public final class SecureAiConfigStore {

    private static final String PREFS = "secure_ai_provider_config";
    private static final String CIPHER_TEXT = "encrypted_config";
    private static final String KEY_ALIAS = "intellicampus_ai_config_key_v1";
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final int GCM_TAG_BITS = 128;

    private SecureAiConfigStore() { }

    public static synchronized void save(Context context, String accountId, AiProviderConfig config) throws Exception {
        JSONObject json = new JSONObject();
        json.put("provider", config.getProvider());
        json.put("baseUrl", config.getBaseUrl());
        json.put("model", config.getModel());
        json.put("apiKey", config.getApiKey());

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey());
        byte[] encrypted = cipher.doFinal(json.toString().getBytes(StandardCharsets.UTF_8));
        byte[] iv = cipher.getIV();
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        context.getSharedPreferences(preferencesName(accountId), Context.MODE_PRIVATE)
                .edit()
                .putString(CIPHER_TEXT, Base64.encodeToString(combined, Base64.NO_WRAP))
                .apply();
    }

    public static synchronized AiProviderConfig load(Context context, String accountId) throws Exception {
        SharedPreferences preferences = context.getSharedPreferences(preferencesName(accountId), Context.MODE_PRIVATE);
        String encoded = preferences.getString(CIPHER_TEXT, null);
        if (encoded == null || encoded.trim().isEmpty()) {
            return new AiProviderConfig(
                    AiProviderConfig.FREELLMAPI,
                    AiProviderConfig.defaultBaseUrl(AiProviderConfig.FREELLMAPI),
                    AiProviderConfig.defaultModel(AiProviderConfig.FREELLMAPI),
                    ""
            );
        }

        byte[] combined = Base64.decode(encoded, Base64.NO_WRAP);
        if (combined.length <= 12) throw new IllegalStateException("Stored AI settings are invalid. Please configure them again.");
        byte[] iv = new byte[12];
        byte[] encrypted = new byte[combined.length - iv.length];
        System.arraycopy(combined, 0, iv, 0, iv.length);
        System.arraycopy(combined, iv.length, encrypted, 0, encrypted.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), new GCMParameterSpec(GCM_TAG_BITS, iv));
        JSONObject json = new JSONObject(new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8));
        return new AiProviderConfig(
                json.optString("provider", AiProviderConfig.FREELLMAPI),
                json.optString("baseUrl", AiProviderConfig.defaultBaseUrl(AiProviderConfig.FREELLMAPI)),
                json.optString("model", AiProviderConfig.defaultModel(AiProviderConfig.FREELLMAPI)),
                json.optString("apiKey", "")
        );
    }

    public static synchronized void clear(Context context, String accountId) {
        // Keep the device encryption key: other signed-in student profiles may
        // still have their own encrypted configuration on this same device.
        context.getSharedPreferences(preferencesName(accountId), Context.MODE_PRIVATE)
                .edit().clear().apply();
    }

    private static String preferencesName(String accountId) {
        String safeId = accountId == null || accountId.trim().isEmpty() ? "signed_out" : accountId.trim();
        return PREFS + "_" + safeId.replaceAll("[^A-Za-z0-9_-]", "_");
    }

    private static SecretKey getOrCreateKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);
        if (keyStore.containsAlias(KEY_ALIAS)) {
            return ((KeyStore.SecretKeyEntry) keyStore.getEntry(KEY_ALIAS, null)).getSecretKey();
        }

        KeyGenerator generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);
        KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT
        )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build();
        generator.init(spec);
        return generator.generateKey();
    }
}
