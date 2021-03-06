package it.communikein.myunimib.ui.timetable;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Calendar;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import it.communikein.myunimib.R;
import it.communikein.myunimib.data.model.Lesson;
import it.communikein.myunimib.databinding.ActivityAddLessonBinding;
import it.communikein.myunimib.utilities.DAY_OF_WEEK;
import it.communikein.myunimib.utilities.DateHelper;
import it.communikein.myunimib.viewmodel.AddLessonViewModel;
import it.communikein.myunimib.viewmodel.factory.AddLessonViewModelFactory;

public class AddLessonActivity extends AppCompatActivity {

    public static final String DAY = "DAY";
    public static final String LESSON_ID = "LESSON_ID";

    public interface AddLessonListener {
        void onLessonAddComplete();
    }

    private ActivityAddLessonBinding mBinding;

    @Inject
    AddLessonViewModelFactory viewModelFactory;

    private AddLessonViewModel mViewModel;

    private ArrayAdapter<String> adapterCourses;
    private ArrayAdapter<String> adapterBuildings;
    private ProgressDialog progressDialog;

    private int lessonId;
    private DAY_OF_WEEK dayOfWeek;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_lesson);

        mViewModel = ViewModelProviders
                .of(this, viewModelFactory)
                .get(AddLessonViewModel.class);

        parseIntent();
        mViewModel.setDayOfWeek(dayOfWeek);

        initUI();
    }

    private void initUI(){
        initProgressDialog(getString(R.string.saving_lesson));

        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mBinding.courseNameText.requestFocus();

        initCoursesAutoComplete();
        initBuildingsAutoComplete();
        initStartTime();
        initEndTime();

        if (lessonId != -1) {
            initProgressDialog(getString(R.string.label_loading_lesson));
            progressDialog.show();

            mViewModel.getLesson(lessonId).observe(this, lesson -> {
                progressDialog.dismiss();
                initProgressDialog(getString(R.string.saving_lesson));

                updateUI(lesson);
            });
        }
        else {
            mBinding.fab.setOnClickListener(v -> saveLesson());
        }
    }

    private void updateUI(Lesson lesson) {
        if (lesson == null) return;

        mBinding.courseNameText.setText(lesson.getCourseName());
        mBinding.lessonBuildingText.setText(lesson.getBuilding());
        mBinding.lessonClassText.setText(lesson.getClassroom());
        mBinding.lessonStartTime.setText(lesson.printTimeStart());
        mBinding.lessonEndTime.setText(lesson.printTimeEnd());

        mBinding.fab.setOnClickListener(v -> saveLesson());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.attention_title))
                        .setMessage(R.string.confirm_discard)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            Intent upIntent = NavUtils.getParentActivityIntent(this);
                            if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                                // This activity is NOT part of this app's task, so create a new task
                                // when navigating up, with a synthesized back stack.
                                TaskStackBuilder.create(this)
                                        // Add all of this activity's parents to the back stack
                                        .addNextIntentWithParentStack(upIntent)
                                        // Navigate up to the closest parent
                                        .startActivities();
                            } else {
                                // This activity is part of this app's task, so simply
                                // navigate up to the logical parent activity.
                                NavUtils.navigateUpTo(this, upIntent);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.attention_title))
                .setMessage(R.string.confirm_discard)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    setResult(RESULT_CANCELED);
                    finish();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void initCoursesAutoComplete() {
        adapterCourses = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                new ArrayList<String>());
        mBinding.courseNameText.setAdapter(adapterCourses);
        mBinding.courseNameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mViewModel.getCoursesNames(s.toString()).observe(AddLessonActivity.this, list -> {
                    if (list != null) {
                        // update the adapter
                        adapterCourses.clear();
                        adapterCourses.addAll(list);
                        adapterCourses.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        mBinding.nameShowIcon.setOnClickListener(v -> {
            String name = mBinding.courseNameText.getText().toString();
            mViewModel.getCoursesNames(name).observe(AddLessonActivity.this, list -> {
                if (list != null) {
                    // update the adapter
                    adapterCourses.clear();
                    adapterCourses.addAll(list);
                    adapterCourses.notifyDataSetChanged();
                    mBinding.courseNameText.showDropDown();
                }
            });
        });
    }

    private void initBuildingsAutoComplete() {
        adapterBuildings = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                new ArrayList<String>());
        mBinding.lessonBuildingText.setAdapter(adapterBuildings);
        mBinding.lessonBuildingText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ArrayList<String> result = mViewModel.getBuildings(s.toString());

                // update the adapter
                adapterBuildings.clear();
                adapterBuildings.addAll(result);
                adapterBuildings.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void initStartTime() {
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        mBinding.lessonStartTime.setText(formatTime(hour, 0));

        mBinding.lessonStartTime.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                initTime(mBinding.lessonStartTime);
        });
        mBinding.lessonStartTime.setOnClickListener(v -> initTime(mBinding.lessonStartTime));
    }

    private void initEndTime() {
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        mBinding.lessonEndTime.setText(formatTime(hour, 0));

        mBinding.lessonEndTime.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                initTime(mBinding.lessonEndTime);
        });
        mBinding.lessonEndTime.setOnClickListener(v -> initTime(mBinding.lessonEndTime));
    }

    private void initTime(final EditText timeEditText) {
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);

        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(this,
                (timePicker, selectedHour, selectedMinute) ->
                        timeEditText.setText(formatTime(selectedHour, selectedMinute)),
                hour, minute, true);
        mTimePicker.setTitle(getString(R.string.prompt_start_time));
        mTimePicker.show();
    }

    private String formatTime(int hours, int minutes) {
        String hourDisplay = String.valueOf(hours);
        if (hours < 10)
            hourDisplay = "0" + hours;

        String minuteDisplay = String.valueOf(minutes);
        if (minutes < 10)
            minuteDisplay = "0" + minutes;

        return getString(R.string.time_format, hourDisplay, minuteDisplay);
    }

    private void saveLesson() {
        Lesson lesson = validateData();
        if (lesson != null) {
            mViewModel.addLesson(lesson, () -> {
                progressDialog.dismiss();
                Intent intent = getIntent();
                intent.putExtra(DAY, dayOfWeek.ordinal());
                setResult(RESULT_OK, intent);
                finish();
            });
            progressDialog.show();
        }
    }

    private Lesson validateData() {
        mBinding.courseNameWrapper.setError(null);
        mBinding.lessonBuildingWrapper.setError(null);
        mBinding.lessonRoomWrapper.setError(null);

        mBinding.lessonStartTime.setError(null);
        mBinding.lessonEndTime.setError(null);

        View error_view = null;

        String name = mBinding.courseNameText.getText().toString();
        if (name.isEmpty()) {
            mBinding.courseNameWrapper.setError(getString(R.string.error_missing_course_name));
            error_view = mBinding.courseNameWrapper;
        }
        String building = null;
        if (error_view == null) {
            building = mBinding.lessonBuildingText.getText().toString();
            if (building.isEmpty()) {
                mBinding.lessonBuildingText.setError(getString(R.string.error_missing_building));
                error_view = mBinding.lessonBuildingWrapper;
            }
        }
        String room = null;
        if (error_view == null) {
            room = mBinding.lessonClassText.getText().toString();
            if (room.isEmpty()) {
                mBinding.lessonClassText.setError(getString(R.string.error_missing_room));
                error_view = mBinding.lessonRoomWrapper;
            }
        }

        long start = -1;
        if (error_view == null) {
            String start_str = mBinding.lessonStartTime.getText().toString();
            if (start_str.isEmpty()) {
                mBinding.lessonStartTime.setError(getString(R.string.error_missing_start_time));
                error_view = mBinding.lessonStartTime;
            } else
                start = DateHelper.getTime(start_str);
        }
        long end = -1;
        if (error_view == null) {
            String end_str = mBinding.lessonEndTime.getText().toString();
            if (end_str.isEmpty()) {
                mBinding.lessonEndTime.setError(getString(R.string.error_missing_end_time));
                error_view = mBinding.lessonEndTime;
            } else
                end = DateHelper.getTime(end_str);
        }
        if (error_view == null) {
            if (end <= start) {
                mBinding.lessonEndTime.setError(getString(R.string.error_time_end_before_start));
                error_view = mBinding.lessonEndTime;
            }
        }

        if (error_view != null) {
            error_view.requestFocus();
            return null;
        }
        else
            return new Lesson(name, building, room, mViewModel.getDay(), start, end);
    }

    private void initProgressDialog(String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
    }

    private void parseIntent() {
        Intent intent = getIntent();

        if (intent != null) {
            dayOfWeek = (DAY_OF_WEEK) intent.getSerializableExtra(DAY);
            lessonId = intent.getIntExtra(LESSON_ID, -1);
        }
    }

}
