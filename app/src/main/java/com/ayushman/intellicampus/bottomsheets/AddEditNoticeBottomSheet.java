package com.ayushman.intellicampus.bottomsheets;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.ayushman.intellicampus.R;
import com.ayushman.intellicampus.databinding.BottomSheetAddEditNoticeBinding;
import com.ayushman.intellicampus.interfaces.FirestoreCallback;
import com.ayushman.intellicampus.models.Notice;
import com.ayushman.intellicampus.viewmodels.NoticeViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;

public class AddEditNoticeBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "AddEditNoticeBottomSheet";
    private static final String ARG_NOTICE = "notice";

    private BottomSheetAddEditNoticeBinding binding;
    private NoticeViewModel viewModel;

    private Notice notice;
    private boolean isEditMode = false;

    private final String[] categories = {
            "Academic",
            "Administrative",
            "Examination",
            "Placement",
            "Club",
            "Event",
            "General"
    };

    public AddEditNoticeBottomSheet() {
    }

    public static AddEditNoticeBottomSheet newInstance() {

        return new AddEditNoticeBottomSheet();

    }

    public static AddEditNoticeBottomSheet newInstance(Notice notice) {

        AddEditNoticeBottomSheet sheet =
                new AddEditNoticeBottomSheet();

        Bundle bundle = new Bundle();

        bundle.putSerializable(ARG_NOTICE, notice);

        sheet.setArguments(bundle);

        return sheet;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding =
                BottomSheetAddEditNoticeBinding.inflate(
                        inflater,
                        container,
                        false
                );

        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        viewModel =
                new ViewModelProvider(this)
                        .get(NoticeViewModel.class);

        if (getArguments() != null &&
                getArguments().containsKey(ARG_NOTICE)) {

            notice =
                    (Notice) getArguments()
                            .getSerializable(ARG_NOTICE);

            isEditMode = true;

        }

        setupCategoryDropdown();

        if (isEditMode) {

            binding.tvSheetTitle.setText("Edit Notice");
            binding.btnSave.setText("Update Notice");

            populateFields();

        }

        binding.btnSave.setOnClickListener(v -> {

            if (validateInputs()) {

                saveNotice();

            }

        });

    }

    private void setupCategoryDropdown() {

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_list_item_1,
                        categories
                );

        binding.acCategory.setAdapter(adapter);

    }

    private void populateFields() {

        if (notice == null)
            return;

        binding.etTitle.setText(notice.getTitle());

        binding.etDescription.setText(
                notice.getDescription());

        binding.acCategory.setText(
                notice.getCategory(),
                false
        );

        binding.etAttachment.setText(
                notice.getAttachmentUrl());

        binding.switchPin.setChecked(
                notice.isPinned());

    }

    private boolean validateInputs() {

        if (TextUtils.isEmpty(
                binding.etTitle.getText())) {

            binding.etTitle.setError("Required");

            return false;

        }

        if (TextUtils.isEmpty(
                binding.etDescription.getText())) {

            binding.etDescription.setError("Required");

            return false;

        }

        if (TextUtils.isEmpty(
                binding.acCategory.getText())) {

            binding.acCategory.setError("Required");

            return false;

        }

        return true;

    }
    private void saveNotice() {

        if (notice == null) {
            notice = new Notice();
            notice.setDate(System.currentTimeMillis());
        }

        notice.setTitle(
                binding.etTitle.getText().toString().trim());

        notice.setDescription(
                binding.etDescription.getText().toString().trim());

        notice.setCategory(
                binding.acCategory.getText().toString().trim());

        notice.setPinned(
                binding.switchPin.isChecked());

        notice.setAttachmentUrl(
                binding.etAttachment.getText()
                        .toString()
                        .trim());

        setLoading(true);

        if (isEditMode) {

            viewModel.updateNotice(
                    notice,
                    new FirestoreCallback<Void>() {

                        @Override
                        public void onSuccess(Void result) {

                            setLoading(false);

                            Snackbar.make(
                                    binding.getRoot(),
                                    "Notice updated successfully",
                                    Snackbar.LENGTH_SHORT
                            ).show();

                            dismiss();

                        }

                        @Override
                        public void onFailure(Exception e) {

                            setLoading(false);

                            Snackbar.make(
                                    binding.getRoot(),
                                    e.getMessage(),
                                    Snackbar.LENGTH_LONG
                            ).show();

                        }

                    });

        } else {

            viewModel.addNotice(
                    notice,
                    new FirestoreCallback<Void>() {

                        @Override
                        public void onSuccess(Void result) {

                            setLoading(false);

                            Snackbar.make(
                                    binding.getRoot(),
                                    "Notice added successfully",
                                    Snackbar.LENGTH_SHORT
                            ).show();

                            dismiss();

                        }

                        @Override
                        public void onFailure(Exception e) {

                            setLoading(false);

                            Snackbar.make(
                                    binding.getRoot(),
                                    e.getMessage(),
                                    Snackbar.LENGTH_LONG
                            ).show();

                        }

                    });

        }

    }

    private void setLoading(boolean loading) {

        binding.progressBar.setVisibility(
                loading ? View.VISIBLE : View.GONE);

        binding.btnSave.setEnabled(!loading);

        binding.etTitle.setEnabled(!loading);
        binding.etDescription.setEnabled(!loading);
        binding.acCategory.setEnabled(!loading);
        binding.etAttachment.setEnabled(!loading);
        binding.switchPin.setEnabled(!loading);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}