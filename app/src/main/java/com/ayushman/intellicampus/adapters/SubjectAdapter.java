package com.ayushman.intellicampus.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ayushman.intellicampus.R;
import com.ayushman.intellicampus.activities.SubjectDetailsActivity;
import com.ayushman.intellicampus.models.Practical;
import com.ayushman.intellicampus.models.Subject;

import java.util.List;

public class SubjectAdapter
        extends RecyclerView.Adapter<SubjectAdapter.ViewHolder> {

    private final List<Subject> subjectList;

    public SubjectAdapter(List<Subject> subjectList) {
        this.subjectList = subjectList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater.from(
                parent.getContext()
        ).inflate(
                R.layout.item_subject,
                parent,
                false
        );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        Subject subject =
                subjectList.get(position);

        // ====================================================
        // SUBJECT NAME
        // ====================================================

        holder.tvName.setText(
                safeText(
                        subject.getName(),
                        "Unnamed Subject"
                )
        );

        // ====================================================
        // SUBJECT CODE
        // ====================================================

        holder.tvCode.setText(
                safeText(
                        subject.getCode(),
                        ""
                )
        );

        // ====================================================
        // SUBJECT TYPE
        // ====================================================

        String type =
                getSubjectType(subject);

        holder.tvCredits.setText(
                type
        );

        // ====================================================
        // CLICK
        // ====================================================

        holder.itemView.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            holder.itemView.getContext(),
                            SubjectDetailsActivity.class
                    );

            intent.putExtra(
                    "subject",
                    subject
            );

            holder.itemView
                    .getContext()
                    .startActivity(intent);
        });
    }

    // ========================================================
    // DETERMINE SUBJECT TYPE
    // ========================================================

    private String getSubjectType(
            Subject subject
    ) {

        String type =
                subject.getCourseType();

        if (type != null &&
                !type.trim().isEmpty()) {

            return type
                    .trim()
                    .toUpperCase();
        }

        // ----------------------------------------------------
        // Detect using course code.
        //
        // T = Theory
        // P = Practical
        // ----------------------------------------------------

        String code =
                subject.getCourseCode();

        if (code != null) {

            code =
                    code.trim()
                            .toUpperCase();

            if (code.endsWith("P")) {
                return "PRACTICAL";
            }

            if (code.endsWith("T")) {
                return "THEORY";
            }
        }

        // ----------------------------------------------------
        // Fallback: inspect practical data.
        // ----------------------------------------------------

        List<Practical> core =
                subject.getCorePracticals();

        List<Practical> application =
                subject.getApplicationPracticals();

        if ((core != null &&
                !core.isEmpty()) ||

                (application != null &&
                        !application.isEmpty())) {

            return "PRACTICAL";
        }

        return "COURSE";
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
    // ITEM COUNT
    // ========================================================

    @Override
    public int getItemCount() {
        return subjectList.size();
    }

    // ========================================================
    // VIEW HOLDER
    // ========================================================

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvName;
        TextView tvCode;
        TextView tvCredits;

        ViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            tvName =
                    itemView.findViewById(
                            R.id.tvSubjectName
                    );

            tvCode =
                    itemView.findViewById(
                            R.id.tvSubjectCode
                    );

            tvCredits =
                    itemView.findViewById(
                            R.id.tvCredits
                    );
        }
    }
}