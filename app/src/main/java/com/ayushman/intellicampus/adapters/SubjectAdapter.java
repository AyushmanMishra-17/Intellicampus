package com.ayushman.intellicampus.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ayushman.intellicampus.R;
import com.ayushman.intellicampus.activities.SubjectDetailsActivity;
import com.ayushman.intellicampus.models.Subject;

import java.util.List;
import java.util.Locale;

public class SubjectAdapter
        extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    private final Context context;
    private final List<Subject> subjectList;

    public SubjectAdapter(Context context, List<Subject> subjectList) {
        this.context = context;
        this.subjectList = subjectList;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subject, parent, false);

        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull SubjectViewHolder holder, int position) {

        Subject subject = subjectList.get(position);

        String code = subject.getCourseCode();
        String name = subject.getCourseName();
        String type = subject.getCourseType();
        String id = subject.getId();

        holder.tvSubjectCode.setText(
                isBlank(code) ? (isBlank(id) ? "Subject code unavailable" : id) : code
        );

        holder.tvSubjectName.setText(
                isBlank(name) ? "Untitled subject" : name
        );

        holder.tvCourseType.setText(
                isBlank(type) ? "COURSE" : type.trim().toUpperCase(Locale.ROOT)
        );

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SubjectDetailsActivity.class);
            intent.putExtra(SubjectDetailsActivity.EXTRA_SUBJECT_ID, subject.getId());
            intent.putExtra(SubjectDetailsActivity.EXTRA_COURSE, subject.getCourse());
            intent.putExtra(SubjectDetailsActivity.EXTRA_SEMESTER, subject.getSemester());
            context.startActivity(intent);
        });
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    @Override
    public int getItemCount() {
        return subjectList == null ? 0 : subjectList.size();
    }

    static class SubjectViewHolder extends RecyclerView.ViewHolder {

        final TextView tvSubjectCode;
        final TextView tvSubjectName;
        final TextView tvCourseType;

        SubjectViewHolder(@NonNull View itemView) {
            super(itemView);

            tvSubjectCode = itemView.findViewById(R.id.tvSubjectCode);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName);
            tvCourseType = itemView.findViewById(R.id.tvCredits);
        }
    }
}
