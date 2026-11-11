package com.ayushman.intellicampus.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.res.ColorStateList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.ayushman.intellicampus.ai.AiProviderConfig;
import com.ayushman.intellicampus.ai.AiProviderManager;
import com.ayushman.intellicampus.R;
import com.ayushman.intellicampus.constants.FirestoreConstants;
import com.ayushman.intellicampus.constants.UserConstants;
import com.ayushman.intellicampus.firebase.FirestoreManager;
import com.ayushman.intellicampus.utils.PdfExportManager;
import com.ayushman.intellicampus.models.AcademicBatch;
import com.ayushman.intellicampus.models.Practical;
import com.ayushman.intellicampus.models.Subject;
import com.ayushman.intellicampus.models.Unit;
import com.ayushman.intellicampus.repositories.AcademicBatchRepository;
import com.ayushman.intellicampus.repositories.StudyRepository;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AITutorFragment extends Fragment {

    private static final String TAG = "AITutor";
    private final AcademicBatchRepository batchRepository =
            new AcademicBatchRepository();

    private final StudyRepository studyRepository =
            new StudyRepository();

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private LinearLayout chatContainer;
    private ScrollView chatScroll;
    private EditText etMessage;
    private TextView tvContext;
    private View sendButton;

    private AiProviderManager aiProviderManager;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private String uid;
    private String chatId;
    private String studentBatch = "";
    private String programme = "BCA";
    private int semester = 0;
    private String academicYear = "";
    private boolean chatPersisted = false;
    private String syllabusContext =
            "No syllabus has been loaded yet.";

    private final List<Map<String, String>> messages =
            new ArrayList<>();

    public AITutorFragment() {
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {

        View view = inflater.inflate(
                R.layout.fragment_ai_tutor,
                container,
                false
        );

        auth = FirebaseAuth.getInstance();

        firestore =
                FirestoreManager
                        .getInstance()
                        .getFirestore();

        uid = auth.getCurrentUser() == null
                ? null
                : auth.getCurrentUser().getUid();

        aiProviderManager = new AiProviderManager(requireContext(), uid);

        chatContainer =
                view.findViewById(R.id.chatContainer);

        chatScroll =
                view.findViewById(R.id.chatScroll);

        etMessage =
                view.findViewById(R.id.etTutorMessage);

        tvContext =
                view.findViewById(R.id.tvTutorContext);

        sendButton =
                view.findViewById(R.id.btnSendTutor);

        view.findViewById(R.id.btnTutorHistory)
                .setOnClickListener(v ->
                        showHistory());

        view.findViewById(R.id.btnTutorNewChat)
                .setOnClickListener(v ->
                        startNewChat(true));

        view.findViewById(R.id.btnTutorSettings)
                .setOnClickListener(v ->
                        showSettings());

        sendButton.setOnClickListener(v ->
                sendCurrentMessage());

        view.findViewById(R.id.btnExplain)
                .setOnClickListener(v ->
                        promptQuickAction(
                                "Explain the most important topic from my current syllabus in simple terms."
                        ));

        view.findViewById(R.id.btnSummarize)
                .setOnClickListener(v ->
                        promptQuickAction(
                                "Summarize my current syllabus into a concise exam revision sheet."
                        ));

        view.findViewById(R.id.btnQuiz)
                .setOnClickListener(v ->
                        promptQuickAction(
                                "Quiz me with 5 questions from my current syllabus. Ask one question at a time and wait for my answer."
                        ));

        view.findViewById(R.id.btnStudyPlan)
                .setOnClickListener(v ->
                        promptQuickAction(
                                "Create a practical 7-day study plan based only on my current syllabus."
                        ));

        startNewChat(false);
        loadStudentContext();

        return view;
    }

    private void showSettings() {
        if (!isAdded()) return;

        View settingsView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_ai_provider_settings, null, false);

        Spinner providerSpinner = settingsView.findViewById(R.id.spinnerAiProvider);
        EditText modelInput = settingsView.findViewById(R.id.etAiModel);
        EditText keyInput = settingsView.findViewById(R.id.etAiApiKey);
        EditText baseUrlInput = settingsView.findViewById(R.id.etAiBaseUrl);
        MaterialButton fetchModelsButton = settingsView.findViewById(R.id.btnFetchAiModels);
        TextView selectedModelLabel = settingsView.findViewById(R.id.tvAiSelectedModel);

        String[] providerNames = getResources().getStringArray(R.array.ai_provider_names);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                requireContext(), R.layout.item_ai_provider_spinner, providerNames) {
            private View bindProviderRow(int position, View convertView, android.view.ViewGroup parent,
                                         boolean dropdown) {
                View row = LayoutInflater.from(requireContext()).inflate(
                        dropdown ? R.layout.item_ai_provider_dropdown
                                : R.layout.item_ai_provider_spinner, parent, false);
                String providerName = getItem(position);
                if (dropdown) {
                    TextView title = row.findViewById(R.id.tvDropdownTitle);
                    TextView subtitle = row.findViewById(R.id.tvDropdownSubtitle);
                    title.setText(providerName);
                    subtitle.setText(providerDescription(providerName));
                } else {
                    ((TextView) row).setText(providerName);
                }
                return row;
            }

            @Override public View getView(int position, View convertView, android.view.ViewGroup parent) {
                return bindProviderRow(position, convertView, parent, false);
            }

            @Override public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                return bindProviderRow(position, convertView, parent, true);
            }
        };
        providerSpinner.setAdapter(adapter);
        providerSpinner.setDropDownWidth(android.view.ViewGroup.LayoutParams.MATCH_PARENT);
        providerSpinner.setDropDownVerticalOffset(6);

        AiProviderConfig current;
        try {
            current = aiProviderManager.loadConfig();
        } catch (Exception e) {
            current = new AiProviderConfig(
                    AiProviderConfig.FREELLMAPI,
                    AiProviderConfig.defaultBaseUrl(AiProviderConfig.FREELLMAPI),
                    AiProviderConfig.defaultModel(AiProviderConfig.FREELLMAPI), "");
        }

        final AiProviderConfig savedConfig = current;
        int selectedIndex = adapter.getPosition(current.getProvider());
        providerSpinner.setSelection(Math.max(0, selectedIndex));
        modelInput.setText(current.getModel());
        updateSelectedModelLabel(selectedModelLabel, current.getModel(), false);
        keyInput.setText(current.getApiKey());
        baseUrlInput.setText(current.getBaseUrl());
        updateBaseUrlEditing(providerSpinner, baseUrlInput);

        providerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View item, int position, long id) {
                String provider = String.valueOf(parent.getItemAtPosition(position));
                if (provider.equals(savedConfig.getProvider())) {
                    modelInput.setText(savedConfig.getModel());
                    updateSelectedModelLabel(selectedModelLabel, savedConfig.getModel(), false);
                    keyInput.setText(savedConfig.getApiKey());
                    baseUrlInput.setText(savedConfig.getBaseUrl());
                } else {
                    modelInput.setText(AiProviderConfig.defaultModel(provider));
                    updateSelectedModelLabel(selectedModelLabel,
                            AiProviderConfig.defaultModel(provider), false);
                    // Do not accidentally send one provider's key to another provider.
                    keyInput.setText("");
                    baseUrlInput.setText(AiProviderConfig.defaultBaseUrl(provider));
                }
                updateBaseUrlEditing(providerSpinner, baseUrlInput);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        modelInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSelectedModelLabel(selectedModelLabel, s.toString(), false);
            }
            @Override public void afterTextChanged(Editable editable) { }
        });

        fetchModelsButton.setOnClickListener(v -> {
            AiProviderConfig config = readConfigFromSettings(
                    providerSpinner, modelInput, keyInput, baseUrlInput);
            if (config.getApiKey().trim().isEmpty()) {
                Toast.makeText(requireContext(), "Enter the provider API key first.",
                        Toast.LENGTH_LONG).show();
                return;
            }
            fetchModelsButton.setEnabled(false);
            fetchModelsButton.setText("Fetching models…");
            executor.execute(() -> {
                List<AiProviderManager.AiModelInfo> models;
                String error = null;
                try {
                    models = aiProviderManager.fetchModels(config);
                } catch (Exception e) {
                    models = new ArrayList<>();
                    error = e.getMessage() == null
                            ? "Unable to fetch models. You can enter a model ID manually." : e.getMessage();
                }
                final List<AiProviderManager.AiModelInfo> availableModels = models;
                final String fetchError = error;
                if (isAdded()) requireActivity().runOnUiThread(() -> {
                    if (!fetchModelsButton.isAttachedToWindow()) return;
                    fetchModelsButton.setEnabled(true);
                    fetchModelsButton.setText("Fetch available models");
                    if (fetchError != null) {
                        new AlertDialog.Builder(requireContext())
                                .setTitle("Model discovery unavailable")
                                .setMessage(fetchError)
                                .setPositiveButton("OK", null)
                                .show();
                        return;
                    }
                    ArrayAdapter<AiProviderManager.AiModelInfo> modelAdapter =
                            new ArrayAdapter<AiProviderManager.AiModelInfo>(
                                    requireContext(), R.layout.item_ai_model_dropdown, availableModels) {
                                private View createModelRow(int position, android.view.ViewGroup parent) {
                                    View row = LayoutInflater.from(requireContext()).inflate(
                                            R.layout.item_ai_model_dropdown, parent, false);
                                    AiProviderManager.AiModelInfo model = getItem(position);
                                    ((TextView) row.findViewById(R.id.tvModelDisplayName))
                                            .setText(model.getDisplayName());
                                    ((TextView) row.findViewById(R.id.tvModelId)).setText(model.getId());
                                    TextView recommended = row.findViewById(R.id.tvModelRecommended);
                                    recommended.setVisibility(model.isRecommended() ? View.VISIBLE : View.GONE);
                                    return row;
                                }

                                @Override public View getView(int position, View convertView, android.view.ViewGroup parent) {
                                    return createModelRow(position, parent);
                                }

                                @Override public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                                    return createModelRow(position, parent);
                                }
                            };
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Choose an AI model")
                            .setAdapter(modelAdapter, (modelDialog, which) -> {
                                AiProviderManager.AiModelInfo selected = availableModels.get(which);
                                modelInput.setText(selected.getId());
                                updateSelectedModelLabel(selectedModelLabel,
                                        selected.getDisplayName() + " (" + selected.getId() + ")", false);
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });
            });
        });

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("AI Provider Settings")
                .setView(settingsView)
                .setPositiveButton("Save", null)
                .setNeutralButton("Test connection", null)
                .setNegativeButton("Cancel", null)
                .create();

        settingsView.findViewById(R.id.btnRemoveAiConfig).setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Remove AI configuration?")
                    .setMessage("This removes the saved API key and provider settings for this student on this device.")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Remove", (confirm, which) -> {
                        aiProviderManager.clearConfig();
                        Toast.makeText(requireContext(), "AI provider configuration removed", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .show();
        });

        dialog.setOnShowListener(ignored -> {
            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawable(
                        new android.graphics.drawable.ColorDrawable(
                                requireContext().getColor(R.color.ai_surface)));
            }
            TextView dialogTitle = dialog.findViewById(androidx.appcompat.R.id.alertTitle);
            if (dialogTitle != null) {
                dialogTitle.setTextColor(requireContext().getColor(R.color.ai_text_primary));
            }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    requireContext().getColor(R.color.ai_accent));
            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(
                    requireContext().getColor(R.color.ai_accent));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                    requireContext().getColor(R.color.ai_text_secondary));

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                AiProviderConfig config = readConfigFromSettings(
                        providerSpinner, modelInput, keyInput, baseUrlInput);
                try {
                    aiProviderManager.saveConfig(config);
                    Toast.makeText(requireContext(), "AI provider settings saved securely",
                            Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } catch (Exception e) {
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
                AiProviderConfig config = readConfigFromSettings(
                        providerSpinner, modelInput, keyInput, baseUrlInput);
                try {
                    aiProviderManager.validate(config);
                } catch (Exception e) {
                    Toast.makeText(requireContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }

                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(false);
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setText("Testing…");
                executor.execute(() -> {
                    String result;
                    boolean success;
                    try {
                        result = aiProviderManager.testConnection(config);
                        success = true;
                    } catch (Exception e) {
                        result = e.getMessage() == null ? "Connection failed." : e.getMessage();
                        if (result.contains("HTTP 404")) {
                            result += "\n\nNext step: tap “Fetch available models”, choose a listed model, and test again. "
                                    + "The app will not switch models without your approval.";
                        } else if (result.contains("HTTP 403")) {
                            result += "\n\nNext step: check that this API key has access to the selected model and project.";
                        }
                        success = false;
                    }
                    final String message = result;
                    final boolean connected = success;
                    if (isAdded()) requireActivity().runOnUiThread(() -> {
                        if (!dialog.isShowing()) return;
                        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(true);
                        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setText("Test connection");
                        if (connected) {
                            updateSelectedModelLabel(selectedModelLabel, config.getModel(), true);
                        }
                        new AlertDialog.Builder(requireContext())
                                .setTitle(connected ? "Connection successful" : "Connection failed")
                                .setMessage(message)
                                .setPositiveButton("OK", null)
                                .show();
                    });
                });
            });
        });
        dialog.show();
    }

    private void updateBaseUrlEditing(Spinner providerSpinner, EditText baseUrlInput) {
        String provider = String.valueOf(providerSpinner.getSelectedItem());
        boolean custom = AiProviderConfig.CUSTOM.equals(provider);
        baseUrlInput.setEnabled(custom);
        baseUrlInput.setAlpha(custom ? 1f : 0.72f);
    }

    private String providerDescription(String provider) {
        if (provider == null) return "AI service provider";
        switch (provider) {
            case AiProviderConfig.GEMINI: return "Google · Gemini models";
            case AiProviderConfig.OPENAI: return "OpenAI · GPT models";
            case AiProviderConfig.ANTHROPIC: return "Anthropic · Claude models";
            case AiProviderConfig.COHERE: return "Cohere · Command models";
            case AiProviderConfig.GROQ: return "Groq · Fast inference";
            case AiProviderConfig.OPENROUTER: return "OpenRouter · Multiple AI models";
            case AiProviderConfig.TOGETHER: return "Together AI · Hosted open models";
            case AiProviderConfig.FIREWORKS: return "Fireworks AI · Model inference";
            case AiProviderConfig.MISTRAL: return "Mistral AI · Mistral models";
            case AiProviderConfig.DEEPSEEK: return "DeepSeek · Reasoning and chat";
            case AiProviderConfig.XAI: return "xAI · Grok models";
            case AiProviderConfig.PERPLEXITY: return "Perplexity · Search-focused AI";
            case AiProviderConfig.CEREBRAS: return "Cerebras · Fast inference";
            case AiProviderConfig.SAMBANOVA: return "SambaNova · AI inference";
            case AiProviderConfig.FREELLMAPI: return "IntelliCampus hosted AI service";
            default: return "Custom OpenAI-compatible endpoint";
        }
    }

    private void updateSelectedModelLabel(TextView label, String model, boolean validated) {
        if (label == null) return;
        String value = model == null || model.trim().isEmpty()
                ? "No model selected" : model.trim();
        label.setText((validated ? "Connection verified: " : "Selected model: ") + value);
    }

    private AiProviderConfig readConfigFromSettings(
            Spinner providerSpinner, EditText modelInput,
            EditText keyInput, EditText baseUrlInput) {
        String provider = String.valueOf(providerSpinner.getSelectedItem());
        String baseUrl = AiProviderConfig.CUSTOM.equals(provider)
                ? baseUrlInput.getText().toString().trim()
                : AiProviderConfig.defaultBaseUrl(provider);
        return new AiProviderConfig(
                provider,
                baseUrl,
                modelInput.getText().toString().trim(),
                keyInput.getText().toString().trim());
    }

    private boolean hasConfiguredApiKey() {
        try {
            return !aiProviderManager.loadConfig().getApiKey().trim().isEmpty();
        } catch (Exception e) {
            Log.e(TAG, "Unable to read encrypted AI provider settings", e);
            return false;
        }
    }

    private void startNewChat(
            boolean saveCurrent
    ) {

        if (saveCurrent &&
                !messages.isEmpty()) {

            saveChat();
        }

        chatId =
                UUID.randomUUID()
                        .toString();

        chatPersisted = false;

        messages.clear();

        chatContainer.removeAllViews();

        addMessage(
                "assistant",
                "Hi! I'm your Intellicampus AI Tutor. "
                        + "I know your current syllabus and can help you "
                        + "understand concepts, revise units, and prepare for exams."
        );
    }

    private void promptQuickAction(
            String prompt
    ) {

        etMessage.setText(prompt);

        sendCurrentMessage();
    }

    private void sendCurrentMessage() {

        String text =
                etMessage
                        .getText()
                        .toString()
                        .trim();

        if (TextUtils.isEmpty(text)) {
            return;
        }

        if (!hasConfiguredApiKey()) {
            showSettings();
            Toast.makeText(requireContext(),
                    "Configure an AI provider and API key first.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        etMessage.setText("");

        addMessage(
                "user",
                text
        );

        setSending(true);

        executor.execute(() -> {

            try {

                String answer =
                        callAiProvider();

                if (isAdded()) {

                    requireActivity()
                            .runOnUiThread(() -> {

                                setSending(false);

                                addMessage(
                                        "assistant",
                                        answer
                                );

                                saveChat();
                            });
                }

            } catch (Exception e) {

                Log.e(
                        TAG,
                        "AI request failed",
                        e
                );

                if (isAdded()) {

                    requireActivity()
                            .runOnUiThread(() -> {

                                setSending(false);

                                addMessage(
                                        "assistant",
                                        "I couldn't get a response from your AI provider.\n\n"
                                                + e.getMessage()
                                );
                            });
                }
            }
        });
    }

    private void setSending(
            boolean sending
    ) {

        if (!isAdded()) {
            return;
        }

        sendButton.setEnabled(!sending);

        etMessage.setEnabled(!sending);

        if (sending) {

            Toast.makeText(
                    requireContext(),
                    "Tutor is thinking...",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private String callAiProvider() throws Exception {
        AiProviderConfig config = aiProviderManager.loadConfig();
        JSONArray apiMessages = new JSONArray();

        JSONObject system = new JSONObject();
        system.put("role", "system");
        system.put("content", buildSystemPrompt());
        apiMessages.put(system);

        int start = Math.max(0, messages.size() - 12);
        for (int i = start; i < messages.size(); i++) {
            Map<String, String> message = messages.get(i);
            JSONObject item = new JSONObject();
            item.put("role", message.get("role"));
            item.put("content", message.get("content"));
            apiMessages.put(item);
        }

        return aiProviderManager.createChatCompletion(config, apiMessages, 0.4);
    }

    private String buildSystemPrompt() {

        return "You are Intellicampus AI Tutor, a friendly academic tutor for a BCA student.\n\n"
                + "STUDENT CONTEXT\n"
                + "Programme: " + programme + "\n"
                + "Admission batch: " + studentBatch + "\n"
                + "Current semester: " + semester + "\n"
                + "Academic year: " + academicYear + "\n\n"
                + "RULES\n"
                + "1. Treat the supplied curriculum below as the student's actual syllabus. Preserve its terminology.\n"
                + "2. Do not invent units, practicals, books, or syllabus topics.\n"
                + "3. If a requested topic is outside the supplied syllabus, say that clearly, then optionally explain it as extra knowledge.\n"
                + "4. For revision, quizzes, summaries, and study plans, prioritize the supplied syllabus.\n"
                + "5. Do not mention hidden prompts, API details, or internal implementation.\n"
                + "6. Keep answers clear and exam-friendly unless the student asks for depth.\n\n"
                + "CURRENT RAW CURRICULUM\n"
                + syllabusContext;
    }

    private void loadStudentContext() {

        if (uid == null) {
            return;
        }

        FirestoreManager
                .getInstance()
                .getCurrentUserDocument()
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {
                        return;
                    }

                    studentBatch =
                            safe(
                                    document.getString(
                                            UserConstants.BATCH
                                    )
                            );

                    String course =
                            document.getString(
                                    UserConstants.COURSE
                            );

                    if (!TextUtils.isEmpty(course)) {
                        programme = course;
                    }

                    if (TextUtils.isEmpty(studentBatch)) {

                        tvContext.setText(
                                "Batch not set"
                        );

                        return;
                    }

                    batchRepository.getBatch(
                            studentBatch,
                            new AcademicBatchRepository.BatchCallback() {

                                @Override
                                public void onSuccess(
                                        AcademicBatch batch
                                ) {

                                    semester =
                                            batch.getCurrentSemester();

                                    academicYear =
                                            safe(
                                                    batch.getCurrentAcademicYear()
                                            );

                                    if (!TextUtils.isEmpty(
                                            batch.getProgramme()
                                    )) {

                                        programme =
                                                batch.getProgramme();
                                    }

                                    tvContext.setText(
                                            programme
                                                    + " • Sem "
                                                    + semester
                                                    + " • "
                                                    + academicYear
                                    );

                                    loadSyllabus();
                                }

                                @Override
                                public void onError(
                                        Exception e
                                ) {

                                    tvContext.setText(
                                            programme
                                                    + " • Batch "
                                                    + studentBatch
                                    );
                                }
                            }
                    );

                })
                .addOnFailureListener(
                        e ->
                                tvContext.setText(
                                        "Unable to load academic context"
                                )
                );
    }

    private void loadSyllabus() {

        if (semester <= 0) {
            return;
        }

        studyRepository.getSubjects(
                programme,
                semester,
                studentBatch,
                new StudyRepository.SubjectsCallback() {

                    @Override
                    public void onSuccess(
                            List<Subject> subjects
                    ) {

                        syllabusContext =
                                buildSyllabusContext(
                                        subjects
                                );

                        if (subjects.isEmpty()) {

                            tvContext.setText(
                                    programme
                                            + " • Sem "
                                            + semester
                                            + " • No syllabus found"
                            );
                        }
                    }

                    @Override
                    public void onError(
                            Exception e
                    ) {

                        Log.e(
                                TAG,
                                "Syllabus load failed",
                                e
                        );

                        syllabusContext =
                                "No syllabus could be loaded from Firestore for this semester.";
                    }
                }
        );
    }

    private String buildSyllabusContext(
            List<Subject> subjects
    ) {

        StringBuilder out =
                new StringBuilder();

        for (Subject subject : subjects) {

            out.append("\n\nSUBJECT: ")
                    .append(
                            safe(
                                    subject.getCourseCode()
                            )
                    )
                    .append(" - ")
                    .append(
                            safe(
                                    subject.getCourseName()
                            )
                    )
                    .append("\n");

            if (!TextUtils.isEmpty(
                    subject.getCourseType()
            )) {

                out.append("Type: ")
                        .append(
                                subject.getCourseType()
                        )
                        .append("\n");
            }

            if (!TextUtils.isEmpty(
                    subject.getLearningObjectives()
            )) {

                out.append(
                                "Learning objectives:\n"
                        )
                        .append(
                                subject.getLearningObjectives()
                        )
                        .append("\n");
            }

            if (!TextUtils.isEmpty(
                    subject.getPrerequisites()
            )) {

                out.append(
                                "Prerequisites:\n"
                        )
                        .append(
                                subject.getPrerequisites()
                        )
                        .append("\n");
            }

            if (subject.getUnits() != null) {

                for (Unit unit :
                        subject.getUnits()) {

                    out.append("Unit ")
                            .append(
                                    safe(
                                            unit.getUnit()
                                    )
                            )
                            .append(
                                    " (Hours: "
                            )
                            .append(
                                    unit.getHours()
                            )
                            .append("):\n")
                            .append(
                                    safe(
                                            unit.getContent()
                                    )
                            )
                            .append("\n");
                }
            }

            appendPracticals(
                    out,
                    "Core practicals",
                    subject.getCorePracticals()
            );

            appendPracticals(
                    out,
                    "Application practicals",
                    subject.getApplicationPracticals()
            );

            if (!TextUtils.isEmpty(
                    subject.getTextbooks()
            )) {

                out.append(
                                "Textbooks:\n"
                        )
                        .append(
                                subject.getTextbooks()
                        )
                        .append("\n");
            }

            if (!TextUtils.isEmpty(
                    subject.getReferenceBooks()
            )) {

                out.append(
                                "Reference books:\n"
                        )
                        .append(
                                subject.getReferenceBooks()
                        )
                        .append("\n");
            }
        }

        String result =
                out.toString().trim();

        if (result.length() > 50000) {

            result =
                    result.substring(
                            0,
                            50000
                    )
                            + "\n[Remaining curriculum omitted only because of context size.]";
        }

        return result.isEmpty()
                ? "No curriculum subjects were found."
                : result;
    }

    private void appendPracticals(
            StringBuilder out,
            String title,
            List<Practical> practicals
    ) {

        if (practicals == null ||
                practicals.isEmpty()) {

            return;
        }

        out.append(title)
                .append(":\n");

        for (Practical practical :
                practicals) {

            out.append(
                            practical.getNumber()
                    )
                    .append(". ")
                    .append(
                            safe(
                                    practical.getDescription()
                            )
                    )
                    .append("\n");
        }
    }

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value.trim();
    }

    private void addMessage(
            String role,
            String content
    ) {

        Map<String, String> message =
                new HashMap<>();

        message.put(
                "role",
                role
        );

        message.put(
                "content",
                content
        );

        messages.add(message);

        boolean isUser =
                "user".equals(role);

        TextView roleView =
                new TextView(
                        requireContext()
                );

        roleView.setText(
                isUser
                        ? "You"
                        : "Intellicampus AI"
        );

        roleView.setTextSize(11);

        roleView.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        roleView.setTextColor(
                requireContext().getColor(
                        isUser
                                ? R.color.ai_accent
                                : R.color.ai_text_secondary
                )
        );

        roleView.setGravity(
                isUser
                        ? android.view.Gravity.END
                        : android.view.Gravity.START
        );

        TextView textView =
                new TextView(
                        requireContext()
                );

        textView.setText(content);

        textView.setTextSize(15);

        textView.setTextColor(
                requireContext().getColor(
                        R.color.ai_text_primary
                )
        );

        textView.setLineSpacing(
                0f,
                1.08f
        );

        textView.setPadding(
                18,
                14,
                18,
                14
        );

        textView.setBackgroundResource(
                isUser
                        ? R.drawable.bg_user_message
                        : R.drawable.bg_ai_message
        );

        LinearLayout wrapper =
                new LinearLayout(
                        requireContext()
                );

        wrapper.setOrientation(
                LinearLayout.VERTICAL
        );

        wrapper.setPadding(
                4,
                6,
                4,
                6
        );

        wrapper.setGravity(
                isUser
                        ? android.view.Gravity.END
                        : android.view.Gravity.START
        );

        LinearLayout.LayoutParams bubbleParams =
                new LinearLayout.LayoutParams(
                        isUser
                                ? (int) (
                                getResources()
                                        .getDisplayMetrics()
                                .widthPixels
                                * 0.78f
                        )
                                : (int) (
                                getResources()
                                        .getDisplayMetrics()
                                .widthPixels
                                * 0.90f
                        ),
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        bubbleParams.gravity =
                isUser
                        ? android.view.Gravity.END
                        : android.view.Gravity.START;

        wrapper.addView(
                roleView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                )
        );

        wrapper.addView(
                textView,
                bubbleParams
        );

        // Offer PDF export for substantive AI responses.
        if (!isUser && content != null && content.trim().length() >= 100) {
            MaterialButton exportButton = new MaterialButton(
                    requireContext(),
                    null,
                    com.google.android.material.R.attr.materialButtonOutlinedStyle
            );
            exportButton.setText("Download PDF");
            exportButton.setAllCaps(false);
            exportButton.setTextSize(12f);
            exportButton.setTypeface(
                    exportButton.getTypeface(),
                    android.graphics.Typeface.BOLD
            );
            exportButton.setMinHeight(dp(38));
            exportButton.setInsetTop(0);
            exportButton.setInsetBottom(0);
            exportButton.setCornerRadius(dp(12));
            exportButton.setStrokeWidth(dp(1));
            exportButton.setStrokeColor(ColorStateList.valueOf(
                    androidx.core.content.ContextCompat.getColor(
                            requireContext(), R.color.ai_border
                    )
            ));
            exportButton.setBackgroundTintList(ColorStateList.valueOf(
                    androidx.core.content.ContextCompat.getColor(
                            requireContext(), R.color.ai_surface_2
                    )
            ));
            exportButton.setTextColor(
                    androidx.core.content.ContextCompat.getColor(
                            requireContext(), R.color.ai_text_primary
                    )
            );
            exportButton.setIconResource(android.R.drawable.ic_menu_save);
            exportButton.setIconTint(ColorStateList.valueOf(
                    androidx.core.content.ContextCompat.getColor(
                            requireContext(), R.color.ai_accent
                    )
            ));
            exportButton.setIconSize(dp(16));
            exportButton.setIconPadding(dp(8));
            exportButton.setPadding(dp(14), 0, dp(14), 0);
            exportButton.setContentDescription("Download this AI response as a PDF");
            exportButton.setOnClickListener(v ->
                    PdfExportManager.exportToDownloads(
                            requireContext(),
                            "AI Tutor Study Notes",
                            content
                    )
            );

            LinearLayout.LayoutParams exportParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            dp(40)
                    );
            exportParams.topMargin = dp(8);
            exportParams.gravity = android.view.Gravity.START;
            wrapper.addView(exportButton, exportParams);
        }

        chatContainer.addView(
                wrapper
        );

        chatScroll.post(
                () ->
                        chatScroll.fullScroll(
                                View.FOCUS_DOWN
                        )
        );
    }

    private void saveChat() {

        if (uid == null ||
                messages.size() < 2) {

            return;
        }

        String title =
                "AI Tutor Chat";

        for (Map<String, String> m :
                messages) {

            if ("user".equals(
                    m.get("role")
            )) {

                title =
                        m.get("content");

                if (title.length() > 48) {

                    title =
                            title.substring(
                                            0,
                                            48
                                    )
                                    .trim()
                                    + "…";
                }

                break;
            }
        }

        Map<String, Object> data =
                new HashMap<>();

        data.put(
                "title",
                title
        );

        data.put(
                "subject",
                "General"
        );

        data.put(
                "batch",
                studentBatch
        );

        data.put(
                "semester",
                semester
        );

        data.put(
                "academicYear",
                academicYear
        );

        data.put(
                "messages",
                new ArrayList<>(messages)
        );

        data.put(
                "updatedAt",
                FieldValue.serverTimestamp()
        );

        if (!chatPersisted) {

            data.put(
                    "createdAt",
                    FieldValue.serverTimestamp()
            );
        }

        final String savedChatId =
                chatId;

        firestore
                .collection(
                        FirestoreConstants.USERS
                )
                .document(uid)
                .collection("ai_chats")
                .document(savedChatId)
                .set(
                        data,
                        com.google.firebase.firestore.SetOptions.merge()
                )
                .addOnSuccessListener(
                        unused -> {

                            if (savedChatId.equals(
                                    chatId
                            )) {

                                chatPersisted = true;
                            }
                        }
                )
                .addOnFailureListener(
                        e ->
                                Log.e(
                                        TAG,
                                        "Could not save chat history",
                                        e
                                )
                );
    }

    private void showHistory() {

        if (uid == null) {
            return;
        }

        firestore
                .collection(
                        FirestoreConstants.USERS
                )
                .document(uid)
                .collection("ai_chats")
                .get()
                .addOnSuccessListener(
                        snapshot -> {

                            List<DocumentSnapshot> docs =
                                    new ArrayList<>(
                                            snapshot.getDocuments()
                                    );

                            Collections.sort(
                                    docs,
                                    (a, b) -> {

                                        Timestamp at =
                                                a.getTimestamp(
                                                        "updatedAt"
                                                );

                                        Timestamp bt =
                                                b.getTimestamp(
                                                        "updatedAt"
                                                );

                                        if (at == null &&
                                                bt == null) {

                                            return 0;
                                        }

                                        if (at == null) {
                                            return 1;
                                        }

                                        if (bt == null) {
                                            return -1;
                                        }

                                        return bt.compareTo(at);
                                    }
                            );

                            if (docs.isEmpty()) {

                                new AlertDialog.Builder(
                                        requireContext()
                                )
                                        .setTitle(
                                                "Chat History"
                                        )
                                        .setMessage(
                                                "No saved conversations yet."
                                        )
                                        .setPositiveButton(
                                                "New Chat",
                                                (d, w) ->
                                                        startNewChat(
                                                                true
                                                        )
                                        )
                                        .setNegativeButton(
                                                "Close",
                                                null
                                        )
                                        .show();

                                return;
                            }

                            String[] labels =
                                    new String[
                                            docs.size()
                                            ];

                            for (
                                    int i = 0;
                                    i < docs.size();
                                    i++
                            ) {

                                String title =
                                        docs.get(i)
                                                .getString(
                                                        "title"
                                                );

                                labels[i] =
                                        title == null ||
                                                title.trim()
                                                        .isEmpty()
                                                ? "AI Tutor Chat"
                                                : title;
                            }

                            new AlertDialog.Builder(
                                    requireContext()
                            )
                                    .setTitle(
                                            "Chat History"
                                    )
                                    .setItems(
                                            labels,
                                            (d, which) ->
                                                    showChatActions(
                                                            docs.get(which)
                                                    )
                                    )
                                    .setNeutralButton(
                                            "New Chat",
                                            (d, w) ->
                                                    startNewChat(
                                                            true
                                                    )
                                    )
                                    .setNegativeButton(
                                            "Close",
                                            null
                                    )
                                    .show();

                        }
                )
                .addOnFailureListener(
                        e ->
                                Toast.makeText(
                                        requireContext(),
                                        "Unable to load chat history: "
                                                + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show()
                );
    }

    private void showChatActions(
            DocumentSnapshot document
    ) {

        if (!isAdded()) {
            return;
        }

        String title =
                document.getString(
                        "title"
                );

        if (TextUtils.isEmpty(title)) {
            title = "AI Tutor Chat";
        }

        final String chatTitle =
                title;

        new AlertDialog.Builder(
                requireContext()
        )
                .setTitle(chatTitle)
                .setItems(
                        new String[]{
                                "Open conversation",
                                "Delete conversation"
                        },
                        (dialog, which) -> {

                            if (which == 0) {

                                loadChat(document);

                            } else {

                                new AlertDialog.Builder(
                                        requireContext()
                                )
                                        .setTitle(
                                                "Delete conversation?"
                                        )
                                        .setMessage(
                                                "This conversation will be permanently removed from your chat history."
                                        )
                                        .setNegativeButton(
                                                "Cancel",
                                                null
                                        )
                                        .setPositiveButton(
                                                "Delete",
                                                (d, w) ->
                                                        deleteChat(
                                                                document.getId()
                                                        )
                                        )
                                        .show();
                            }
                        }
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .show();
    }

    private void deleteChat(
            String id
    ) {

        if (uid == null ||
                TextUtils.isEmpty(id)) {

            return;
        }

        firestore
                .collection(
                        FirestoreConstants.USERS
                )
                .document(uid)
                .collection("ai_chats")
                .document(id)
                .delete()
                .addOnSuccessListener(
                        unused -> {

                            if (chatId.equals(id)) {
                                startNewChat(false);
                            }

                            Toast.makeText(
                                    requireContext(),
                                    "Conversation deleted",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                )
                .addOnFailureListener(
                        e ->
                                Toast.makeText(
                                        requireContext(),
                                        "Could not delete conversation: "
                                                + e.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show()
                );
    }

    @SuppressWarnings("unchecked")
    private void loadChat(
            DocumentSnapshot document
    ) {

        chatId =
                document.getId();

        chatPersisted = true;

        messages.clear();

        chatContainer.removeAllViews();

        List<Map<String, Object>> stored =
                (List<Map<String, Object>>)
                        document.get("messages");

        if (stored != null) {

            for (Map<String, Object> raw :
                    stored) {

                String role =
                        raw.get("role") == null
                                ? "assistant"
                                : String.valueOf(
                                raw.get("role")
                        );

                String content =
                        raw.get("content") == null
                                ? ""
                                : String.valueOf(
                                raw.get("content")
                        );

                if (!content.isEmpty()) {

                    addMessage(
                            role,
                            content
                    );
                }
            }
        }

        if (messages.isEmpty()) {

            addMessage(
                    "assistant",
                    "This conversation has no messages."
            );
        }
    }

    private int dp(float value) {
        return Math.round(
                value * getResources().getDisplayMetrics().density
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {

        executor.shutdownNow();

        super.onDestroy();
    }
}