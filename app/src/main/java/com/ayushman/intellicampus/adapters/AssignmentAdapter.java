package com.ayushman.intellicampus.adapters;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.ayushman.intellicampus.R;
import com.ayushman.intellicampus.models.Assignment;
import com.google.android.material.checkbox.MaterialCheckBox;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.ViewHolder> {

    public interface OnAssignmentClickListener {
        void onAssignmentClick(@NonNull Assignment assignment);

        void onAssignmentLongClick(@NonNull Assignment assignment);

        void onStatusChanged(@NonNull Assignment assignment, boolean isCompleted);
    }

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

    private final List<Assignment> assignmentList = new ArrayList<>();
    private final OnAssignmentClickListener listener;

    public AssignmentAdapter(@NonNull OnAssignmentClickListener listener) {
        this.listener = listener;
    }

    public void setAssignments(List<Assignment> newAssignments) {

        List<Assignment> newList =
                newAssignments == null
                        ? new ArrayList<>()
                        : new ArrayList<>(newAssignments);

        DiffUtil.DiffResult diffResult =
                DiffUtil.calculateDiff(
                        new AssignmentDiffCallback(assignmentList, newList));

        assignmentList.clear();
        assignmentList.addAll(newList);

        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_assignment, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Assignment assignment = assignmentList.get(position);

        holder.tvTitle.setText(assignment.getTitle());
        holder.tvSubject.setText(assignment.getSubject());

        holder.tvDueDate.setText(
                "Due • " +
                        DATE_FORMAT.format(
                                new Date(assignment.getDueDateTime()))
        );

        holder.cbCompleted.setOnCheckedChangeListener(null);

        boolean completed =
                assignment.getStatus() == Assignment.STATUS_COMPLETED;

        holder.cbCompleted.setChecked(completed);

        // Dim completed assignments
        float alpha = completed ? 0.55f : 1f;

        holder.tvTitle.setAlpha(alpha);
        holder.tvSubject.setAlpha(alpha);
        holder.tvDueDate.setAlpha(alpha);

        // Strike-through title
        if (completed) {
            holder.tvTitle.setPaintFlags(
                    holder.tvTitle.getPaintFlags()
                            | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvTitle.setPaintFlags(
                    holder.tvTitle.getPaintFlags()
                            & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        holder.itemView.setOnClickListener(v ->
                listener.onAssignmentClick(assignment));

        holder.itemView.setOnLongClickListener(v -> {
            listener.onAssignmentLongClick(assignment);
            return true;
        });

        holder.cbCompleted.setOnCheckedChangeListener(
                (buttonView, isChecked) ->
                        listener.onStatusChanged(assignment, isChecked));
    }

    @Override
    public int getItemCount() {
        return assignmentList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView tvTitle;
        final TextView tvSubject;
        final TextView tvDueDate;
        final MaterialCheckBox cbCompleted;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            cbCompleted = itemView.findViewById(R.id.cbCompleted);
        }
    }

    private static class AssignmentDiffCallback extends DiffUtil.Callback {

        private final List<Assignment> oldList;
        private final List<Assignment> newList;

        AssignmentDiffCallback(@NonNull List<Assignment> oldList,
                               @NonNull List<Assignment> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition,
                                       int newItemPosition) {

            String oldId = oldList.get(oldItemPosition).getId();
            String newId = newList.get(newItemPosition).getId();

            if (oldId == null || newId == null) {
                return false;
            }

            return oldId.equals(newId);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition,
                                          int newItemPosition) {

            return oldList.get(oldItemPosition)
                    .equals(newList.get(newItemPosition));
        }
    }
}