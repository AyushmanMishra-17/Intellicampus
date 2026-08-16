package com.ayushman.intellicampus.adapters;

import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.ayushman.intellicampus.R;
import com.ayushman.intellicampus.models.Notice;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.ViewHolder> {

    public interface OnNoticeClickListener {

        void onNoticeClick(@NonNull Notice notice);

        void onEditClick(@NonNull Notice notice);

        void onDeleteClick(@NonNull Notice notice);
    }

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

    private final List<Notice> noticeList = new ArrayList<>();
    private final OnNoticeClickListener listener;
    private boolean isAdmin = false;
    public NoticeAdapter(@NonNull OnNoticeClickListener listener) {
        this.listener = listener;
    }
    public void setAdmin(boolean admin) {
        isAdmin = admin;
        notifyDataSetChanged();
    }
    public void setNotices(List<Notice> newNotices) {

        List<Notice> newList =
                newNotices == null
                        ? new ArrayList<>()
                        : new ArrayList<>(newNotices);

        DiffUtil.DiffResult diffResult =
                DiffUtil.calculateDiff(
                        new NoticeDiffCallback(noticeList, newList));

        noticeList.clear();
        noticeList.addAll(newList);

        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notice, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,
                                 int position) {

        Notice notice = noticeList.get(position);

        holder.tvTitle.setText(notice.getTitle());
        holder.tvDescription.setText(notice.getDescription());
        holder.tvCategory.setText(notice.getCategory());

        holder.tvDate.setText(
                DATE_FORMAT.format(new Date(notice.getDate()))
        );

        holder.ivPinned.setVisibility(
                notice.isPinned()
                        ? View.VISIBLE
                        : View.GONE
        );

        holder.ivAttachment.setVisibility(
                notice.getAttachmentUrl() != null &&
                        !notice.getAttachmentUrl().isEmpty()
                        ? View.VISIBLE
                        : View.GONE
        );

        holder.itemView.setOnClickListener(v ->
                listener.onNoticeClick(notice));

        if (isAdmin) {

            holder.btnOptions.setVisibility(View.VISIBLE);

            holder.btnOptions.setOnClickListener(v -> {

                PopupMenu popupMenu =
                        new PopupMenu(
                                holder.itemView.getContext(),
                                holder.btnOptions
                        );

                MenuInflater inflater =
                        popupMenu.getMenuInflater();

                inflater.inflate(
                        R.menu.menu_notice_options,
                        popupMenu.getMenu()
                );

                popupMenu.setOnMenuItemClickListener(item -> {

                    if (item.getItemId() == R.id.action_edit) {

                        listener.onEditClick(notice);
                        return true;

                    } else if (item.getItemId() == R.id.action_delete) {

                        listener.onDeleteClick(notice);
                        return true;

                    }

                    return false;

                });

                popupMenu.show();

            });

        } else {

            holder.btnOptions.setVisibility(View.GONE);

        }

    }

    @Override
    public int getItemCount() {
        return noticeList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvCategory;
        TextView tvTitle;
        TextView tvDescription;
        TextView tvDate;

        ImageView ivPinned;
        ImageView ivAttachment;

        ImageButton btnOptions;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDate = itemView.findViewById(R.id.tvDate);

            ivPinned = itemView.findViewById(R.id.ivPinned);
            ivAttachment = itemView.findViewById(R.id.ivAttachment);

            btnOptions = itemView.findViewById(R.id.btnOptions);
        }
    }

    private static class NoticeDiffCallback extends DiffUtil.Callback {

        private final List<Notice> oldList;
        private final List<Notice> newList;

        NoticeDiffCallback(@NonNull List<Notice> oldList,
                           @NonNull List<Notice> newList) {
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

            Notice oldNotice = oldList.get(oldItemPosition);
            Notice newNotice = newList.get(newItemPosition);

            return oldNotice.getTitle().equals(newNotice.getTitle())
                    && oldNotice.getDescription().equals(newNotice.getDescription())
                    && oldNotice.getCategory().equals(newNotice.getCategory())
                    && oldNotice.getDate() == newNotice.getDate()
                    && oldNotice.isPinned() == newNotice.isPinned()
                    && ((oldNotice.getAttachmentUrl() == null && newNotice.getAttachmentUrl() == null)
                    || (oldNotice.getAttachmentUrl() != null
                    && oldNotice.getAttachmentUrl().equals(newNotice.getAttachmentUrl())));
        }
    }
}