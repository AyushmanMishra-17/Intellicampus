package com.ayushman.intellicampus.activities;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.ayushman.intellicampus.databinding.ActivitySubjectDetailsBinding;
import com.ayushman.intellicampus.models.Practical;
import com.ayushman.intellicampus.models.Subject;
import com.ayushman.intellicampus.models.Unit;

import java.util.List;

public class SubjectDetailsActivity extends AppCompatActivity {

    private ActivitySubjectDetailsBinding binding;

    // ========================================================
    // INTELLICAMPUS THEME
    // ========================================================

    private static final int BG_CARD =
            Color.rgb(255, 255, 255);

    private static final int TEXT_PRIMARY =
            Color.rgb(15, 23, 42);

    private static final int TEXT_SECONDARY =
            Color.rgb(71, 85, 105);

    private static final int ACCENT =
            Color.rgb(37, 99, 235);

    // ========================================================
    // ON CREATE
    // ========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySubjectDetailsBinding.inflate(
                getLayoutInflater()
        );

        setContentView(binding.getRoot());

        Subject subject =
                (Subject) getIntent()
                        .getSerializableExtra("subject");

        if (subject == null) {
            finish();
            return;
        }

        displaySubject(subject);
    }

    // ========================================================
    // DISPLAY SUBJECT
    // ========================================================

    private void displaySubject(Subject subject) {

        // ----------------------------------------------------
        // HEADER
        // ----------------------------------------------------

        binding.tvSubjectName.setText(
                safeText(
                        subject.getCourseName(),
                        "Subject"
                )
        );

        binding.tvSubjectCode.setText(
                safeText(
                        subject.getCourseCode(),
                        ""
                )
        );

        String type = subject.getCourseType();

        if (type == null ||
                type.trim().isEmpty()) {

            type = isPractical(subject)
                    ? "PRACTICAL"
                    : "THEORY";
        }

        type = type.toUpperCase();

        binding.tvCredits.setText(
                "BCA • Semester " +
                        subject.getSemester() +
                        " • " +
                        type
        );

        // ----------------------------------------------------
        // OBJECTIVES
        // ----------------------------------------------------

        binding.tvObjectives.setText(
                formatText(
                        subject.getLearningObjectives()
                )
        );

        // ----------------------------------------------------
        // PREREQUISITES
        // ----------------------------------------------------

        binding.tvPrerequisites.setText(
                formatText(
                        subject.getPrerequisites()
                )
        );

        // ----------------------------------------------------
        // CONTENT
        // ----------------------------------------------------

        if (isPractical(subject)) {

            binding.tvContentSectionTitle.setText(
                    "PRACTICALS"
            );

            displayPracticalSections(subject);

        } else {

            binding.tvContentSectionTitle.setText(
                    "COURSE UNITS"
            );

            displayUnits(
                    subject.getUnits()
            );
        }

        // ----------------------------------------------------
        // BOOKS
        // ----------------------------------------------------

        binding.tvTextbooks.setText(
                formatText(
                        subject.getTextbooks()
                )
        );

        binding.tvReferenceBooks.setText(
                formatText(
                        subject.getReferenceBooks()
                )
        );
    }

    // ========================================================
    // DETERMINE PRACTICAL
    // ========================================================

    private boolean isPractical(Subject subject) {

        String type =
                subject.getCourseType();

        if (type != null &&
                type.trim()
                        .equalsIgnoreCase("PRACTICAL")) {

            return true;
        }

        String code =
                subject.getCourseCode();

        if (code != null &&
                code.trim()
                        .toUpperCase()
                        .endsWith("P")) {

            return true;
        }

        List<Practical> core =
                subject.getCorePracticals();

        List<Practical> application =
                subject.getApplicationPracticals();

        return (core != null &&
                !core.isEmpty()) ||

                (application != null &&
                        !application.isEmpty());
    }

    // ========================================================
    // PRACTICAL SECTIONS
    // ========================================================

    private void displayPracticalSections(
            Subject subject
    ) {

        binding.layoutUnits.removeAllViews();

        List<Practical> core =
                subject.getCorePracticals();

        List<Practical> application =
                subject.getApplicationPracticals();

        boolean hasCore =
                core != null &&
                        !core.isEmpty();

        boolean hasApplication =
                application != null &&
                        !application.isEmpty();

        if (!hasCore && !hasApplication) {

            binding.layoutUnits.addView(
                    createBodyText(
                            "Practical information is not available."
                    )
            );

            return;
        }

        if (hasCore) {

            addPracticalSection(
                    "CORE PRACTICALS",
                    core
            );
        }

        if (hasApplication) {

            addPracticalSection(
                    "APPLICATION PRACTICALS",
                    application
            );
        }
    }

    // ========================================================
    // PRACTICAL SECTION
    // ========================================================

    private void addPracticalSection(
            String title,
            List<Practical> practicals
    ) {

        TextView sectionTitle =
                new TextView(this);

        sectionTitle.setText(title);
        sectionTitle.setTextSize(15);
        sectionTitle.setTextColor(ACCENT);
        sectionTitle.setTypeface(
                null,
                Typeface.BOLD
        );

        LinearLayout.LayoutParams titleParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        titleParams.topMargin = dp(4);
        titleParams.bottomMargin = dp(10);

        binding.layoutUnits.addView(
                sectionTitle,
                titleParams
        );

        for (int i = 0;
             i < practicals.size();
             i++) {

            Practical practical =
                    practicals.get(i);

            if (practical == null) {
                continue;
            }

            binding.layoutUnits.addView(
                    createPracticalCard(
                            practical,
                            i + 1
                    )
            );
        }
    }

    // ========================================================
    // PRACTICAL CARD
    // ========================================================

    private LinearLayout createPracticalCard(
            Practical practical,
            int fallbackNumber
    ) {

        LinearLayout card =
                new LinearLayout(this);

        card.setOrientation(
                LinearLayout.HORIZONTAL
        );

        card.setGravity(
                Gravity.TOP
        );

        card.setPadding(
                dp(14),
                dp(14),
                dp(14),
                dp(14)
        );

        GradientDrawable background =
                new GradientDrawable();

        background.setColor(BG_CARD);

        background.setCornerRadius(
                dp(14)
        );

        background.setStroke(
                dp(1),
                Color.rgb(226, 232, 240)
        );

        card.setBackground(background);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.bottomMargin =
                dp(10);

        card.setLayoutParams(cardParams);

        // ----------------------------------------------------
        // NUMBER BADGE
        // ----------------------------------------------------

        TextView number =
                new TextView(this);

        int practicalNumber =
                practical.getNumber();

        if (practicalNumber <= 0) {
            practicalNumber =
                    fallbackNumber;
        }

        number.setText(
                String.valueOf(
                        practicalNumber
                )
        );

        number.setTextSize(13);
        number.setTextColor(Color.WHITE);
        number.setGravity(Gravity.CENTER);

        number.setTypeface(
                null,
                Typeface.BOLD
        );

        GradientDrawable badgeBackground =
                new GradientDrawable();

        badgeBackground.setColor(ACCENT);
        badgeBackground.setShape(
                GradientDrawable.OVAL
        );

        number.setBackground(
                badgeBackground
        );

        LinearLayout.LayoutParams numberParams =
                new LinearLayout.LayoutParams(
                        dp(30),
                        dp(30)
                );

        numberParams.rightMargin =
                dp(12);

        card.addView(
                number,
                numberParams
        );

        // ----------------------------------------------------
        // DESCRIPTION
        // ----------------------------------------------------

        TextView description =
                createBodyText(
                        practical.getDescription()
                );

        description.setTextColor(
                TEXT_PRIMARY
        );

        description.setTextSize(14);

        LinearLayout.LayoutParams
                descriptionParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1
                );

        card.addView(
                description,
                descriptionParams
        );

        return card;
    }

    // ========================================================
    // THEORY UNITS
    // ========================================================

    private void displayUnits(
            List<Unit> units
    ) {

        binding.layoutUnits.removeAllViews();

        if (units == null ||
                units.isEmpty()) {

            binding.layoutUnits.addView(
                    createBodyText(
                            "Unit information is not available."
                    )
            );

            return;
        }

        for (Unit unit : units) {

            if (unit == null) {
                continue;
            }

            LinearLayout card =
                    new LinearLayout(this);

            card.setOrientation(
                    LinearLayout.VERTICAL
            );

            card.setPadding(
                    dp(16),
                    dp(16),
                    dp(16),
                    dp(16)
            );

            GradientDrawable background =
                    new GradientDrawable();

            background.setColor(BG_CARD);

            background.setCornerRadius(
                    dp(14)
            );

            background.setStroke(
                    dp(1),
                    Color.rgb(226, 232, 240)
            );

            card.setBackground(
                    background
            );

            LinearLayout.LayoutParams cardParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            cardParams.bottomMargin =
                    dp(12);

            card.setLayoutParams(cardParams);

            // ------------------------------------------------
            // UNIT HEADER
            // ------------------------------------------------

            LinearLayout header =
                    new LinearLayout(this);

            header.setOrientation(
                    LinearLayout.HORIZONTAL
            );

            header.setGravity(
                    Gravity.CENTER_VERTICAL
            );

            TextView title =
                    new TextView(this);

            title.setText(
                    "UNIT " +
                            unit.getUnit()
            );

            title.setTextSize(17);

            title.setTextColor(
                    TEXT_PRIMARY
            );

            title.setTypeface(
                    null,
                    Typeface.BOLD
            );

            LinearLayout.LayoutParams titleParams =
                    new LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1
                    );

            header.addView(
                    title,
                    titleParams
            );

            if (unit.getHours() > 0) {

                TextView hours =
                        new TextView(this);

                hours.setText(
                        unit.getHours() +
                                " hrs"
                );

                hours.setTextSize(13);

                hours.setTextColor(
                        ACCENT
                );

                hours.setTypeface(
                        null,
                        Typeface.BOLD
                );

                header.addView(hours);
            }

            card.addView(header);

            // ------------------------------------------------
            // UNIT CONTENT
            // ------------------------------------------------

            TextView content =
                    createBodyText(
                            formatText(
                                    unit.getContent()
                            )
                    );

            LinearLayout.LayoutParams
                    contentParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            contentParams.topMargin =
                    dp(12);

            card.addView(
                    content,
                    contentParams
            );

            binding.layoutUnits.addView(
                    card
            );
        }
    }

    // ========================================================
    // BODY TEXT
    // ========================================================

    private TextView createBodyText(
            String text
    ) {

        TextView view =
                new TextView(this);

        view.setText(
                safeText(
                        text,
                        "Not available."
                )
        );

        view.setTextSize(15);

        view.setTextColor(
                TEXT_SECONDARY
        );

        view.setLineSpacing(
                dp(2),
                1.0f
        );

        return view;
    }

    // ========================================================
    // TEXT FORMATTING
    // ========================================================

    private String formatText(
            String text
    ) {

        if (text == null ||
                text.trim().isEmpty()) {

            return "Not available.";
        }

        String formatted =
                text.trim();

        formatted =
                formatted.replace(
                        "",
                        "•"
                );

        formatted =
                formatted.replaceAll(
                        "\\n{3,}",
                        "\n\n"
                );

        return formatted;
    }

    // ========================================================
    // SAFE TEXT
    // ========================================================

    private String safeText(
            String text,
            String fallback
    ) {

        if (text == null ||
                text.trim().isEmpty()) {

            return fallback;
        }

        return text.trim();
    }

    // ========================================================
    // DP
    // ========================================================

    private int dp(int value) {

        return (int) (
                value *
                        getResources()
                                .getDisplayMetrics()
                                .density
        );
    }

    // ========================================================
    // DESTROY
    // ========================================================

    @Override
    protected void onDestroy() {

        super.onDestroy();

        binding = null;
    }
}