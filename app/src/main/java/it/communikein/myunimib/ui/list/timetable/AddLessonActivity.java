package it.communikein.myunimib.ui.list.timetable;

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
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_lesson);

        mViewModel = ViewModelProviders
                .of(this, viewModelFactory)
                .get(AddLessonViewModel.class);
        mViewModel.setDayOfWeek(parseIntent());

        initUI();
    }

    private void initUI(){
        initProgressDialog();

        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Window w = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        mBinding.courseNameText.requestFocus();

        initCoursesAutoComplete();
        initBuildingsAutoComplete();
        initStartTime();
        initEndTime();

        initFab();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
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
                return true;
        }
        return super.onOptionsItemSelected(item);
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
        mBinding.lessonStartTime.setOnClickListener(v -> {
            Calendar currentTime = Calendar.getInstance();
            int hour = currentTime.get(Calendar.HOUR_OF_DAY);
            int minute = currentTime.get(Calendar.MINUTE);
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(this, (timePicker, selectedHour, selectedMinute) -> {
                String hourDisplay = String.valueOf(selectedHour);
                if (selectedHour < 10)
                    hourDisplay = "0" + selectedHour;

                String minuteDisplay = String.valueOf(selectedMinute);
                if (selectedMinute < 10)
                    minuteDisplay = "0" + selectedMinute;

                mBinding.lessonStartTime.setText(hourDisplay + "." + minuteDisplay);
            }, hour, minute, true);
            mTimePicker.setTitle(getString(R.string.prompt_start_time));
            mTimePicker.show();
        });
    }

    private void initEndTime() {
        mBinding.lessonEndTime.setOnClickListener(v -> {
            Calendar currentTime = Calendar.getInstance();
            int hour = currentTime.get(Calendar.HOUR_OF_DAY);
            int minute = currentTime.get(Calendar.MINUTE);
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(this, (timePicker, selectedHour, selectedMinute) -> {
                String hourDisplay = String.valueOf(selectedHour);
                if (selectedHour < 10)
                    hourDisplay = "0" + selectedHour;

                String minuteDisplay = String.valueOf(selectedMinute);
                if (selectedMinute < 10)
                    minuteDisplay = "0" + selectedMinute;

                mBinding.lessonEndTime.setText(hourDisplay + "." + minuteDisplay);
            }, hour, minute, true);
            mTimePicker.setTitle(getString(R.string.prompt_end_time));
            mTimePicker.show();
        });
    }

    private void initFab() {
        mBinding.fab.setOnClickListener(v -> {
            String name = mBinding.courseNameText.getText().toString();
            String building = mBinding.lessonBuildingText.getText().toString();
            String room = mBinding.lessonClassText.getText().toString();

            String start_str = mBinding.lessonStartTime.getText().toString().replace(':', '.');
            long start = DateHelper.getTime(start_str);
            String end_str = mBinding.lessonEndTime.getText().toString().replace(':', '.');
            long end = DateHelper.getTime(end_str);

            Lesson lesson = new Lesson(name, building, room, mViewModel.getDay(), start, end);
            mViewModel.addLesson(lesson, () -> {
                progressDialog.dismiss();
                finish();
            });
            progressDialog.show();
        });
    }

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.saving_lesson));
        progressDialog.setCancelable(false);
    }

    private DAY_OF_WEEK parseIntent() {
        Intent intent = getIntent();

        if (intent != null)
            return (DAY_OF_WEEK) intent.getSerializableExtra(DAY);

        return null;
    }

}
