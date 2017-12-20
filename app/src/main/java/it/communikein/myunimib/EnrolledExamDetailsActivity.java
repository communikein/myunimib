package it.communikein.myunimib;

import android.app.ActionBar;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import it.communikein.myunimib.data.ExamContract;
import it.communikein.myunimib.data.type.EnrolledExam;
import it.communikein.myunimib.data.type.ExamID;
import it.communikein.myunimib.databinding.ActivityEnrolledExamDetailsBinding;
import it.communikein.myunimib.sync.S3Helper;
import it.communikein.myunimib.utilities.UniversityUtils;
import it.communikein.myunimib.utilities.Utils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

@SuppressWarnings("unchecked")
public class EnrolledExamDetailsActivity extends FragmentActivity
        implements OnMapReadyCallback, LoaderManager.LoaderCallbacks {

    final public static int LOADER_CERTIFICATE_ID = 3000;
    final public static int LOADER_DETAILS_ID = 3001;

    public static final String[] DETAILS = {
            ExamContract.EnrolledExamEntry.COLUMN_ADSCE_ID,
            ExamContract.EnrolledExamEntry.COLUMN_APP_ID,
            ExamContract.EnrolledExamEntry.COLUMN_ATT_DID_ESA_ID,
            ExamContract.EnrolledExamEntry.COLUMN_CDS_ESA_ID,

            ExamContract.EnrolledExamEntry.COLUMN_COURSE_NAME,
            ExamContract.EnrolledExamEntry.COLUMN_DATE,
            ExamContract.EnrolledExamEntry.COLUMN_DESCRIPTION,
            ExamContract.EnrolledExamEntry.COLUMN_CODE,
            ExamContract.EnrolledExamEntry.COLUMN_ROOM,
            ExamContract.EnrolledExamEntry.COLUMN_BUILDING,
            ExamContract.EnrolledExamEntry.COLUMN_TEACHERS,
            ExamContract.EnrolledExamEntry.COLUMN_RESERVED
    };

    public static final int INDEX_ADSCE_ID = 0;
    public static final int INDEX_APP_ID = 1;
    public static final int INDEX_ATT_DID_ESA_ID = 2;
    public static final int INDEX_CDS_ESA_ID = 3;
    public static final int INDEX_COURSE_NAME = 4;
    public static final int INDEX_DATE = 5;
    public static final int INDEX_DESCRIPTION = 6;
    public static final int INDEX_CODE = 7;
    public static final int INDEX_ROOM = 8;
    public static final int INDEX_BUILDING = 9;
    public static final int INDEX_TEACHERS = 10;
    public static final int INDEX_RESERVED = 11;

    ActivityEnrolledExamDetailsBinding mBinding;

    private Uri mUri;
    private EnrolledExam exam;

    //* Might be null if Google Play services APK is not available. */
    private GoogleMap mMap = null;
    private ProgressDialog progress;

    Thread.UncaughtExceptionHandler handler = (thread, throwable) -> {
        throwable.printStackTrace();
        Utils.saveBugReport((Exception) throwable, "ENROLLED DETAILS");
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(handler);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_enrolled_exam_details);

        initUI();
        loadExamData();
    }

    private void initUI() {
        // Try to obtain the map from the SupportMapFragment.
        SupportMapFragment mMap = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.exam_map));
        mMap.getMapAsync(this);

        progress = new ProgressDialog(this);
        progress.setCancelable(false);

        ActionBar actionBar = getActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        initFab();
    }

    private void updateUI() {
        if (exam != null) {
            mBinding.examCertificateFab.setVisibility(View.VISIBLE);

            mBinding.examLocationTextview.setText(exam.printLocation());
            mBinding.examDateTextview.setText(exam.printDateTime(this));
            mBinding.examDescriptionTextview.setText(exam.getDescription());
            mBinding.examTeachersTextview.setText(exam.printTeachers());

            updateMap();

            updateToolbar();
        }
    }

    private void updateMap() {
        if (mMap != null) {
            LatLng coords = new LatLng(0, 0);

            String building = getString(R.string.error_exam_missing_room);
            int zoom = 0;
            try {
                if (exam != null) {
                    building = exam.getBuilding().substring(0, exam.getBuilding().indexOf("-")).trim();

                    coords = UniversityUtils.getLatLongBuilding(building.toLowerCase());
                    zoom = 16;
                }
            } catch (Exception ex) {
                building = getString(R.string.error_exam_missing_room);
                coords = new LatLng(0, 0);
                zoom = 0;
            }

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(coords)
                    .title(building.toUpperCase());

            CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(markerOptions.getPosition(), zoom);

            mMap.addMarker(markerOptions);
            mMap.moveCamera(cu);
        }
    }

    private void updateToolbar() {
        /* Get a reference to the MainActivity ActionBar */
        ActionBar actionBar = getActionBar();
        /* If there is an ActionBar, set it's title */
        if (actionBar != null && exam != null)
            actionBar.setTitle(exam.getName());
    }

    private void initFab(){
        mBinding.examCertificateFab.setOnClickListener(v -> showCertificate());

        mBinding.examCertificateFab.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.colorAccent)));
        mBinding.examCertificateFab.setVisibility(View.INVISIBLE);
    }

    private void loadExamData() {
        mUri = getIntent().getData();
        if (mUri == null) throw new NullPointerException("URI for EnrolledExamDetailActivity cannot be null");

        /* This connects our Activity into the loader lifecycle. */
        getLoaderManager().initLoader(LOADER_DETAILS_ID, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home)
            NavUtils.navigateUpFromSameTask(this);

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;

        updateMap();
    }



    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_DETAILS_ID:
                toggleLoading(true);

                return new CursorLoader(this,
                        mUri,
                        DETAILS,
                        null,
                        null,
                        null);

            case LOADER_CERTIFICATE_ID:
                toggleLoading(true);

                return new S3Helper.CertificateLoader(this, exam);

            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader loader, Object result) {
        toggleLoading(false);

        switch (loader.getId()) {
            case LOADER_DETAILS_ID:
                Cursor data = (Cursor) result;
                /*
                 * Before we bind the data to the UI that will display that data, we need to check the
                 * cursor to make sure we have the results that we are expecting. In order to do that, we
                 * check to make sure the cursor is not null and then we call moveToFirst on the cursor.
                 * Although it may not seem obvious at first, moveToFirst will return true if it contains
                 * a valid first row of data.
                 *
                 * If we have valid data, we want to continue on to bind that data to the UI. If we don't
                 * have any data to bind, we just return from this method.
                 */
                if (data == null || !data.moveToFirst()) {
                    /* No data to display, simply return and do nothing */
                    return;
                }

                /* ***************
                 * Exam id       *
                 *****************/
                /* Read exam id fields from the cursor */
                int adsce_id = data.getInt(INDEX_ADSCE_ID);
                int app_id = data.getInt(INDEX_APP_ID);
                int att_did_esa_id = data.getInt(INDEX_ATT_DID_ESA_ID);
                int cds_esa_id = data.getInt(INDEX_CDS_ESA_ID);

                /* Create a new Exam ID object */
                ExamID examID = new ExamID(app_id, cds_esa_id, att_did_esa_id, adsce_id);

                /* **************
                 * Exam details *
                 ****************/
                /* Read exam details fields from the cursor */
                String name = data.getString(INDEX_COURSE_NAME);
                String description = data.getString(INDEX_DESCRIPTION);
                String code = data.getString(INDEX_CODE);
                String room = data.getString(INDEX_ROOM);
                String building = data.getString(INDEX_BUILDING);
                String reserved = data.getString(INDEX_RESERVED);

                /* **************
                 * Exam date    *
                 ****************/
                /* Read exam details fields from the cursor */
                long date_long = data.getLong(INDEX_DATE);

                Date date = new Date(date_long);

                /* ***************
                 * Exam teachers *
                 *****************/
                /* Read exam details fields from the cursor */
                String teachers_str = data.getString(INDEX_TEACHERS);

                /* Parse the teachers from the JSON string */
                ArrayList<String> teachers = new ArrayList<>();
                try {
                    JSONArray teachers_json = new JSONArray(teachers_str);
                    for(int i=0; i<teachers_json.length(); i++)
                        teachers.add(teachers_json.getString(i));

                } catch (JSONException e) {
                    teachers = new ArrayList<>();
                }

                /* Create a new Exam Enrolled object */
                exam = new EnrolledExam(examID, name, date, description, code, building,
                        room, reserved, teachers);

                updateUI();

                break;

            case LOADER_CERTIFICATE_ID:
                handleDownloadCertificate();
                break;

            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {}



    private void showCertificate(){
        if (exam != null){
            if (!EnrolledExam.getCertificatePath(exam).exists())
                getLoaderManager()
                        .initLoader(LOADER_CERTIFICATE_ID, null, this)
                        .forceLoad();
            else
                openCertificate(exam);
        }
        else
            Snackbar.make(mBinding.container, R.string.error_enrolled_exam_missing,
                    Snackbar.LENGTH_LONG).show();
    }

    private void openCertificate(EnrolledExam exam) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        File certificate_file = EnrolledExam.getCertificatePath(exam);
        Uri certificate_uri = FileProvider.getUriForFile(this,
                getString(R.string.file_provider_authority), certificate_file);

        intent.setDataAndType(certificate_uri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    private void handleDownloadCertificate() {
        Snackbar.make(mBinding.container, R.string.enrolled_exam_certificate_downloaded, Snackbar.LENGTH_LONG)
                .setAction(R.string.open, view -> openCertificate(exam)).show();
    }

    private void toggleLoading(final boolean show) {
        if (show) progress.show();
        else if(progress != null && progress.isShowing()) progress.dismiss();
    }
}
