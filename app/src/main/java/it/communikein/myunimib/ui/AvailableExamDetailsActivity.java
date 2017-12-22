package it.communikein.myunimib.ui;

import android.app.ProgressDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import it.communikein.myunimib.R;
import it.communikein.myunimib.data.database.AvailableExam;

public class AvailableExamDetailsActivity extends AppCompatActivity {

    private AvailableExam exam;
    private boolean isBooking = false;

    private TextView examRoom_text;
    private ProgressDialog progress;

    private FloatingActionButton fab;

    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_available_exam_details);
    }
}
