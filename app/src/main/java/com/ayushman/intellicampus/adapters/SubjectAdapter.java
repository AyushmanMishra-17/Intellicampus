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

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder> {

    private final Context context;
    private final List<Subject> subjectList;

    public SubjectAdapter(Context context, List<Subject> subjectList) {
        this.context = context;
        this.subjectList = subjectList;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_subject, parent, false);

        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        Subject subject = subjectList.get(position);

        holder.tvSubjectCode.setText(subject.getCode());
        holder.tvSubjectName.setText(subject.getName());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SubjectDetailsActivity.class);

            /*
             * Do NOT pass the complete Subject object through Intent.
             * The Subject can contain a large syllabus, which can exceed
             * Android's Binder transaction limit and crash the app.
             *
             * Instead, pass only the information required to retrieve
             * the subject from Firestore.
             */
            intent.putExtra(SubjectDetailsActivity.EXTRA_SUBJECT_ID, subject.getId());
            intent.putExtra(SubjectDetailsActivity.EXTRA_COURSE, subject.getCourse());
            intent.putExtra(SubjectDetailsActivity.EXTRA_SEMESTER, subject.getSemester());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return subjectList == null ? 0 : subjectList.size();
    }

    static class SubjectViewHolder extends RecyclerView.ViewHolder {

        TextView tvSubjectCode;
        TextView tvSubjectName;

        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);

            tvSubjectCode = itemView.findViewById(R.id.tvSubjectCode);
            tvSubjectName = itemView.findViewById(R.id.tvSubjectName);
        }
    }
}