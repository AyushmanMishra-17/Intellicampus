package com.ayushman.intellicampus.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.ayushman.intellicampus.R;
import com.ayushman.intellicampus.models.Timetable;

import java.util.ArrayList;
import java.util.List;

public class TimetableAdapter extends RecyclerView.Adapter<TimetableAdapter.ViewHolder> {

    public interface OnTimeTableClickListener {

        void onEditClick(@NonNull Timetable timetable);

        void onDeleteClick(@NonNull Timetable timetable);
    }

    private final List<Timetable> timetableList = new ArrayList<>();
    private final OnTimeTableClickListener listener;
    private boolean isAdmin = false;

    public TimetableAdapter(@NonNull OnTimeTableClickListener listener) {
        this.listener = listener;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
        notifyDataSetChanged();
    }

    public void setTimetable(List<Timetable> newTimetable) {

        List<Timetable> newList =
                newTimetable == null
                        ? new ArrayList<>()
                        : new ArrayList<>(newTimetable);

        DiffUtil.DiffResult diffResult =
                DiffUtil.calculateDiff(
                        new TimeTableDiffCallback(timetableList, newList));

        timetableList.clear();
        timetableList.addAll(newList);

        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_timetable, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Timetable timetable = timetableList.get(position);

        holder.tvSubject.setText(timetable.getSubject());
        holder.tvFaculty.setText(timetable.getFaculty());
        holder.tvRoom.setText(timetable.getRoom());

        holder.tvTime.setText(
                timetable.getStartTime() + " - " + timetable.getEndTime()
        );

        if (isAdmin) {
            holder.itemView.setOnClickListener(v ->
                    listener.onEditClick(timetable));

            holder.itemView.setOnLongClickListener(v -> {
                listener.onDeleteClick(timetable);
                return true;
            });
        } else {
            holder.itemView.setOnClickListener(null);
            holder.itemView.setOnLongClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return timetableList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView tvTime;
        final TextView tvSubject;
        final TextView tvFaculty;
        final TextView tvRoom;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTime = itemView.findViewById(R.id.tvTime);
            tvSubject = itemView.findViewById(R.id.tvSubject);
            tvFaculty = itemView.findViewById(R.id.tvFaculty);
            tvRoom = itemView.findViewById(R.id.tvRoom);
        }
    }

    private static class TimeTableDiffCallback extends DiffUtil.Callback {

        private final List<Timetable> oldList;
        private final List<Timetable> newList;

        TimeTableDiffCallback(@NonNull List<Timetable> oldList,
                              @NonNull List<Timetable> newList) {
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
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {

            String oldId = oldList.get(oldItemPosition).getId();
            String newId = newList.get(newItemPosition).getId();

            if (oldId == null || newId == null) {
                return false;
            }

            return oldId.equals(newId);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {

            Timetable oldItem = oldList.get(oldItemPosition);
            Timetable newItem = newList.get(newItemPosition);

            return oldItem.getSubject().equals(newItem.getSubject())
                    && oldItem.getFaculty().equals(newItem.getFaculty())
                    && oldItem.getRoom().equals(newItem.getRoom())
                    && oldItem.getDay().equals(newItem.getDay())
                    && oldItem.getStartTime().equals(newItem.getStartTime())
                    && oldItem.getEndTime().equals(newItem.getEndTime());
        }
    }
}