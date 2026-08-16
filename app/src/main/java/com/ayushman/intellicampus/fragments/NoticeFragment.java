package com.ayushman.intellicampus.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.ayushman.intellicampus.constants.RoleConstants;
import com.ayushman.intellicampus.constants.UserConstants;
import com.ayushman.intellicampus.firebase.FirestoreManager;
import com.ayushman.intellicampus.adapters.NoticeAdapter;
import com.ayushman.intellicampus.bottomsheets.AddEditNoticeBottomSheet;
import com.ayushman.intellicampus.databinding.FragmentNoticeBinding;
import com.ayushman.intellicampus.interfaces.FirestoreCallback;
import com.ayushman.intellicampus.models.Notice;
import com.ayushman.intellicampus.viewmodels.NoticeViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class NoticeFragment extends Fragment {

    private FragmentNoticeBinding binding;
    private NoticeAdapter adapter;
    private NoticeViewModel viewModel;

    private final List<Notice> noticeList = new ArrayList<>();

    public NoticeFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentNoticeBinding.inflate(inflater, container, false);

        setupRecyclerView();

        setupViewModel();

        observeNotices();
        checkUserRole();
        binding.fabAddNotice.setOnClickListener(v ->

                AddEditNoticeBottomSheet
                        .newInstance()
                        .show(
                                getParentFragmentManager(),
                                AddEditNoticeBottomSheet.TAG
                        )
        );

        return binding.getRoot();
    }

    private void setupRecyclerView() {

        adapter = new NoticeAdapter(
                new NoticeAdapter.OnNoticeClickListener() {

                    @Override
                    public void onNoticeClick(@NonNull Notice notice) {

                        AddEditNoticeBottomSheet
                                .newInstance(notice)
                                .show(
                                        getParentFragmentManager(),
                                        AddEditNoticeBottomSheet.TAG
                                );

                    }

                    @Override
                    public void onEditClick(@NonNull Notice notice) {

                        AddEditNoticeBottomSheet
                                .newInstance(notice)
                                .show(
                                        getParentFragmentManager(),
                                        AddEditNoticeBottomSheet.TAG
                                );

                    }

                    @Override
                    public void onDeleteClick(@NonNull Notice notice) {

                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Delete Notice")
                                .setMessage("Are you sure you want to delete this notice?")
                                .setNegativeButton("Cancel", null)
                                .setPositiveButton("Delete", (dialog, which) ->

                                        viewModel.deleteNotice(
                                                notice.getId(),
                                                new FirestoreCallback<Void>() {

                                                    @Override
                                                    public void onSuccess(Void result) {

                                                        Snackbar.make(
                                                                binding.getRoot(),
                                                                "Notice deleted",
                                                                Snackbar.LENGTH_SHORT
                                                        ).show();

                                                    }

                                                    @Override
                                                    public void onFailure(Exception e) {

                                                        Snackbar.make(
                                                                binding.getRoot(),
                                                                e.getMessage(),
                                                                Snackbar.LENGTH_LONG
                                                        ).show();

                                                    }

                                                })

                                )
                                .show();

                    }

                });

        binding.rvNotices.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        binding.rvNotices.setAdapter(adapter);

    }

    private void setupViewModel() {

        viewModel =
                new ViewModelProvider(this)
                        .get(NoticeViewModel.class);

        viewModel.loadNotices();

    }

    private void observeNotices() {

        viewModel.getNoticeList().observe(
                getViewLifecycleOwner(),

                notices -> {

                    noticeList.clear();

                    if (notices != null) {
                        noticeList.addAll(notices);
                    }

                    adapter.setNotices(noticeList);

                    binding.layoutEmpty.setVisibility(
                            noticeList.isEmpty()
                                    ? View.VISIBLE
                                    : View.GONE
                    );

                });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void checkUserRole() {

        FirestoreManager.getInstance()
                .getCurrentUserDocument()
                .get()
                .addOnSuccessListener(document -> {

                    String role = document.getString(UserConstants.ROLE);

                    boolean isAdmin =
                            RoleConstants.ADMIN.equalsIgnoreCase(role);

                    adapter.setAdmin(isAdmin);

                    binding.fabAddNotice.setVisibility(
                            isAdmin
                                    ? View.VISIBLE
                                    : View.GONE
                    );

                });

    }
}