package com.ayushman.intellicampus.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.ayushman.intellicampus.models.Assignment;
import com.ayushman.intellicampus.models.Notice;
import com.ayushman.intellicampus.models.Timetable;
import com.ayushman.intellicampus.R;
import com.ayushman.intellicampus.databinding.FragmentHomeBinding;
import com.ayushman.intellicampus.databinding.LayoutHomeHeaderBinding;
import com.ayushman.intellicampus.databinding.LayoutHomeOverviewBinding;
import com.ayushman.intellicampus.databinding.LayoutHomeQuickActionsBinding;
import com.ayushman.intellicampus.repositories.AssignmentRepository;
import com.ayushman.intellicampus.repositories.NoticeRepository;
import com.ayushman.intellicampus.repositories.TimetableRepository;
import com.ayushman.intellicampus.viewmodels.AssignmentViewModel;
import com.ayushman.intellicampus.viewmodels.NoticeViewModel;
import com.ayushman.intellicampus.viewmodels.TimetableViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private LayoutHomeHeaderBinding headerBinding;
    private LayoutHomeOverviewBinding overviewBinding;
    private LayoutHomeQuickActionsBinding quickActionsBinding;

    private AssignmentViewModel assignmentViewModel;
    private NoticeViewModel noticeViewModel;
    private TimetableViewModel timetableViewModel;
    private final AssignmentRepository assignmentRepository =
            new AssignmentRepository();

    private final NoticeRepository noticeRepository =
            new NoticeRepository();

    private final TimetableRepository timetableRepository =
            new TimetableRepository();
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);

        headerBinding = binding.headerLayout;
        overviewBinding = binding.overviewLayout;
        quickActionsBinding = binding.quickActionsLayout;

        assignmentViewModel =
                new ViewModelProvider(this)
                        .get(AssignmentViewModel.class);

        noticeViewModel =
                new ViewModelProvider(this)
                        .get(NoticeViewModel.class);

        timetableViewModel =
                new ViewModelProvider(this)
                        .get(TimetableViewModel.class);

        initListeners();
        loadData();

        return binding.getRoot();
    }

    private void initListeners() {

        BottomNavigationView bottomNav =
                requireActivity().findViewById(R.id.bottomNavigation);

        quickActionsBinding.cardAssignment.setOnClickListener(v ->
                bottomNav.setSelectedItemId(R.id.nav_academics));

        quickActionsBinding.cardSchedule.setOnClickListener(v ->
                bottomNav.setSelectedItemId(R.id.nav_schedule));

        quickActionsBinding.cardNotice.setOnClickListener(v ->
                bottomNav.setSelectedItemId(R.id.nav_academics));

        quickActionsBinding.cardAiTutor.setOnClickListener(v ->
                bottomNav.setSelectedItemId(R.id.nav_ai));

        binding.noticeLayout.tvViewAllNotices.setOnClickListener(v ->
                bottomNav.setSelectedItemId(R.id.nav_academics));

        binding.assignmentLayout.tvViewAllAssignments.setOnClickListener(v ->
                bottomNav.setSelectedItemId(R.id.nav_academics));
    }
    private void loadData() {

        setGreeting();
        setUserName();
        setSubtitle();

        setupDashboard();

    }

    private void setGreeting() {

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        if (hour < 12) {
            headerBinding.tvGreeting.setText("🌅 Good Morning");
        } else if (hour < 17) {
            headerBinding.tvGreeting.setText("☀️ Good Afternoon");
        } else {
            headerBinding.tvGreeting.setText("🌙 Good Evening");
        }
    }

    private void setUserName() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Fragment view is no longer available
        if (!isAdded() || headerBinding == null) {
            return;
        }

        if (user == null) {
            headerBinding.tvUserName.setText("Student");
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {

                    // Firestore callback may execute after onDestroyView()
                    if (!isAdded() || headerBinding == null) {
                        return;
                    }

                    if (document.exists()) {

                        String name = document.getString("name");

                        if (name != null && !name.trim().isEmpty()) {
                            headerBinding.tvUserName.setText(name);
                        } else {
                            headerBinding.tvUserName.setText("Student");
                        }

                    } else {

                        headerBinding.tvUserName.setText("Student");
                    }

                })
                .addOnFailureListener(e -> {

                    // Fragment view may already be destroyed
                    if (!isAdded() || headerBinding == null) {
                        return;
                    }

                    headerBinding.tvUserName.setText("Student");
                });
    }

    private void setSubtitle() {

        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        if (hour < 12) {

            headerBinding.tvSubtitle.setText(
                    "Let's make today productive!");

        } else if (hour < 17) {

            headerBinding.tvSubtitle.setText(
                    "Stay focused and keep learning.");

        } else {

            headerBinding.tvSubtitle.setText(
                    "Review today's progress and relax.");

        }
    }

    private void setupDashboard() {

        assignmentViewModel.loadAssignments();
        noticeViewModel.loadNotices();
        timetableViewModel.loadTimetable();

        observeAssignments();
        observeNotices();
        observeTimetable();

    }
    private void observeAssignments() {

        assignmentViewModel.getAssignments()
                .observe(getViewLifecycleOwner(), assignments -> {

                    if (assignments == null) {
                        return;
                    }

                    // Overview Card
                    overviewBinding.tvAssignmentCount.setText(
                            String.valueOf(assignments.size()));

                    // Upcoming Assignment
                    Assignment upcoming =
                            assignmentRepository.getUpcomingAssignment(assignments);

                    if (upcoming != null) {

                        binding.assignmentLayout.tvAssignmentSubject
                                .setText(upcoming.getSubject());

                        binding.assignmentLayout.tvAssignmentTitle
                                .setText(upcoming.getTitle());

                        binding.assignmentLayout.tvAssignmentDue
                                .setText("Due Soon");

                    } else {

                        binding.assignmentLayout.tvAssignmentSubject
                                .setText("No Pending Assignments");

                        binding.assignmentLayout.tvAssignmentTitle
                                .setText("You're all caught up!");

                        binding.assignmentLayout.tvAssignmentDue
                                .setText("");

                    }

                });

    }

    private void observeNotices() {

        noticeViewModel.getNoticeList()
                .observe(getViewLifecycleOwner(), notices -> {

                    if (notices == null) {
                        return;
                    }

                    // Overview Card
                    overviewBinding.tvNoticeCount.setText(
                            String.valueOf(notices.size()));

                    // Latest Notice
                    Notice latest =
                            noticeRepository.getLatestNotice(notices);

                    if (latest != null) {

                        binding.noticeLayout.tvLatestNoticeCategory
                                .setText(latest.getCategory());

                        binding.noticeLayout.tvLatestNoticeTitle
                                .setText(latest.getTitle());

                        binding.noticeLayout.tvLatestNoticeDescription
                                .setText(latest.getDescription());

                    } else {

                        binding.noticeLayout.tvLatestNoticeCategory
                                .setText("");

                        binding.noticeLayout.tvLatestNoticeTitle
                                .setText("No Notices");

                        binding.noticeLayout.tvLatestNoticeDescription
                                .setText("You're up to date.");

                    }

                });

    }
    private void observeTimetable() {

        timetableViewModel.getTimetableList()
                .observe(getViewLifecycleOwner(), timetable -> {

                    if (timetable == null) {
                        return;
                    }

                    String today = new SimpleDateFormat(
                            "EEEE",
                            Locale.getDefault())
                            .format(new Date());

                    String currentTime = new SimpleDateFormat(
                            "HH:mm",
                            Locale.getDefault())
                            .format(new Date());

                    int todayClasses =
                            timetableRepository.getTodayClassCount(
                                    timetable,
                                    today);

                    overviewBinding.tvClassesCount.setText(
                            String.valueOf(todayClasses));

                    Timetable nextClass =
                            timetableRepository.getNextClass(
                                    timetable,
                                    today,
                                    currentTime);

                    if (nextClass != null) {

                        binding.scheduleLayout.tvNextSubject
                                .setText(nextClass.getSubject());

                        binding.scheduleLayout.tvNextFaculty
                                .setText(nextClass.getFaculty());

                        binding.scheduleLayout.tvNextTime
                                .setText(
                                        nextClass.getStartTime()
                                                + " - "
                                                + nextClass.getEndTime());

                        binding.scheduleLayout.tvNextRoom
                                .setText(nextClass.getRoom());

                    } else {

                        binding.scheduleLayout.tvNextSubject
                                .setText("No More Classes");

                        binding.scheduleLayout.tvNextFaculty
                                .setText("");

                        binding.scheduleLayout.tvNextTime
                                .setText("");

                        binding.scheduleLayout.tvNextRoom
                                .setText("");

                    }

                });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
        headerBinding = null;
        overviewBinding = null;
        quickActionsBinding = null;
    }

}