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

import com.ayushman.intellicampus.databinding.BottomsheetAddEditTimetableBinding;
import com.ayushman.intellicampus.models.Timetable;
import com.ayushman.intellicampus.viewmodels.TimetableViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Locale;

public class AddEditTimetableBottomSheet extends BottomSheetDialogFragment {

    private BottomsheetAddEditTimetableBinding binding;

    private TimetableViewModel viewModel;

    private Timetable timetable;

    private boolean isEditMode = false;

    private final String[] DAYS = {
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday",
            "Saturday",
            "Sunday"
    };

    public AddEditTimetableBottomSheet() {
        // Required empty constructor
    }

    public static AddEditTimetableBottomSheet newInstance() {
        return new AddEditTimetableBottomSheet();
    }

    public static AddEditTimetableBottomSheet newInstance(
            @NonNull Timetable timetable) {

        AddEditTimetableBottomSheet sheet =
                new AddEditTimetableBottomSheet();

        sheet.timetable = timetable;
        sheet.isEditMode = true;

        return sheet;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        binding = BottomsheetAddEditTimetableBinding.inflate(
                inflater,
                container,
                false
        );

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity())
                .get(TimetableViewModel.class);

        setupDayDropdown();

        setupTimePickers();

        if (isEditMode) {

            binding.tvTitle.setText("Edit Timetable");

            populateFields();

        } else {

            binding.tvTitle.setText("Add Timetable");

        }

        binding.btnCancel.setOnClickListener(v -> dismiss());

        binding.btnSave.setOnClickListener(v -> {

            if (validateInputs()) {

                saveTimetable();

            }

        });

    }

    private void setupDayDropdown() {

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_list_item_1,
                        DAYS
                );

        binding.actvDay.setAdapter(adapter);

    }

    private void setupTimePickers() {

        binding.etStartTime.setOnClickListener(v ->
                showTimePicker(binding.etStartTime));

        binding.etEndTime.setOnClickListener(v ->
                showTimePicker(binding.etEndTime));

    }
    private void showTimePicker(com.google.android.material.textfield.TextInputEditText editText) {

        MaterialTimePicker picker =
                new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_24H)
                        .setHour(9)
                        .setMinute(0)
                        .setTitleText("Select Time")
                        .build();

        picker.addOnPositiveButtonClickListener(v -> {

            String time = String.format(
                    Locale.getDefault(),
                    "%02d:%02d",
                    picker.getHour(),
                    picker.getMinute()
            );

            editText.setText(time);

        });

        picker.show(getParentFragmentManager(), "TIME_PICKER");

    }
    private boolean validateInputs() {

        if (TextUtils.isEmpty(binding.etSubject.getText())) {
            binding.etSubject.setError("Subject is required");
            binding.etSubject.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(binding.etFaculty.getText())) {
            binding.etFaculty.setError("Faculty is required");
            binding.etFaculty.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(binding.etRoom.getText())) {
            binding.etRoom.setError("Room is required");
            binding.etRoom.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(binding.actvDay.getText())) {
            binding.actvDay.setError("Select a day");
            binding.actvDay.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(binding.etStartTime.getText())) {
            binding.etStartTime.setError("Select start time");
            binding.etStartTime.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(binding.etEndTime.getText())) {
            binding.etEndTime.setError("Select end time");
            binding.etEndTime.requestFocus();
            return false;
        }

        return true;
    }private void saveTimetable() {

        if (timetable == null) {
            timetable = new Timetable();
        }

        timetable.setSubject(binding.etSubject.getText().toString().trim());
        timetable.setFaculty(binding.etFaculty.getText().toString().trim());
        timetable.setRoom(binding.etRoom.getText().toString().trim());
        timetable.setDay(binding.actvDay.getText().toString().trim());
        timetable.setStartTime(binding.etStartTime.getText().toString().trim());
        timetable.setEndTime(binding.etEndTime.getText().toString().trim());

        binding.btnSave.setEnabled(false);

        if (isEditMode) {

            viewModel.updateTimetable(timetable)
                    .addOnSuccessListener(unused -> {

                        Snackbar.make(
                                binding.getRoot(),
                                "Timetable updated successfully",
                                Snackbar.LENGTH_SHORT
                        ).show();

                        dismiss();

                    })
                    .addOnFailureListener(e -> {

                        binding.btnSave.setEnabled(true);

                        Snackbar.make(
                                binding.getRoot(),
                                e.getMessage(),
                                Snackbar.LENGTH_LONG
                        ).show();

                    });

        } else {

            viewModel.addTimetable(timetable)
                    .addOnSuccessListener(unused -> {

                        Snackbar.make(
                                binding.getRoot(),
                                "Timetable added successfully",
                                Snackbar.LENGTH_SHORT
                        ).show();

                        dismiss();

                    })
                    .addOnFailureListener(e -> {

                        binding.btnSave.setEnabled(true);

                        Snackbar.make(
                                binding.getRoot(),
                                e.getMessage(),
                                Snackbar.LENGTH_LONG
                        ).show();

                    });

        }

    }
    private void populateFields() {

        if (timetable == null) {
            return;
        }

        binding.etSubject.setText(timetable.getSubject());
        binding.etFaculty.setText(timetable.getFaculty());
        binding.etRoom.setText(timetable.getRoom());
        binding.actvDay.setText(timetable.getDay(), false);
        binding.etStartTime.setText(timetable.getStartTime());
        binding.etEndTime.setText(timetable.getEndTime());

    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }}