package it.communikein.myunimib.ui.detail;

import android.app.Activity;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import it.communikein.myunimib.R;
import it.communikein.myunimib.data.model.EnrolledExam;
import it.communikein.myunimib.data.model.ExamID;
import it.communikein.myunimib.data.network.UnimibNetworkDataSource;
import it.communikein.myunimib.databinding.ActivityEnrolledExamDetailsBinding;
import it.communikein.myunimib.ui.FragmentAppCompatActivity;
import it.communikein.myunimib.viewmodel.EnrolledExamDetailViewModel;
import it.communikein.myunimib.viewmodel.factory.EnrolledExamViewModelFactory;

@SuppressWarnings("unchecked")
public class EnrolledExamDetailActivity extends FragmentAppCompatActivity
         implements OnMapReadyCallback, LoaderManager.LoaderCallbacks, HasActivityInjector {

    private final static int LOADER_CERTIFICATE_ID = 3000;

    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;

    /* */
    private ActivityEnrolledExamDetailsBinding mBinding;

    /* */
    @Inject
    EnrolledExamViewModelFactory viewModelFactory;

    /* */
    private EnrolledExamDetailViewModel mViewModel;


    /* Might be null if Google Play services APK is not available. */
    private GoogleMap mMap = null;
    private SupportMapFragment mMapFragment = null;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_enrolled_exam_details);

        ExamID examID = loadData();

        mViewModel = ViewModelProviders
                .of(this, viewModelFactory)
                .get(EnrolledExamDetailViewModel.class);
        mViewModel.setExamId(examID);

        mBinding.setLifecycleOwner(this);
        mBinding.setExam(mViewModel.getExam());

        initUI();
    }

    private ExamID loadData() {
        if (getIntent() != null) {
            int adsceId = getIntent().getIntExtra(UnimibNetworkDataSource.ADSCE_ID, -1);
            int appId = getIntent().getIntExtra(UnimibNetworkDataSource.APP_ID, -1);
            int attDidEsaId = getIntent().getIntExtra(UnimibNetworkDataSource.ATT_DID_ESA_ID, -1);
            int cdsEsaId = getIntent().getIntExtra(UnimibNetworkDataSource.CDS_ESA_ID, -1);

            return new ExamID(cdsEsaId, attDidEsaId, appId, adsceId);
        }

        return null;
    }

    private void initUI() {
        progress = new ProgressDialog(this);
        progress.setCancelable(false);

        initMap();
        initToolbar();
        initFab();

        mViewModel.getExam().observe(this, this::updateUI);
    }

    private void initMap() {
        // Try to obtain the map from the SupportMapFragment.
        mMapFragment = ((SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map));
        if (mMapFragment.getView() != null)
            mMapFragment.getView().setVisibility(View.INVISIBLE);
        mMapFragment.getMapAsync(this);
    }

    private void initToolbar() {
        setSupportActionBar(mBinding.toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initFab(){
        mBinding.examCertificateFab.setBackgroundTintList(ColorStateList.valueOf(
                ContextCompat.getColor(this, R.color.colorAccent)));
    }


    private void updateUI(EnrolledExam exam) {
        if (exam != null) {
            updateMap(exam);
            updateToolbar(exam);
            updateFab(exam);
        }
    }

    private void updateMap(EnrolledExam exam) {
        if (mMapFragment.getView() != null) {

            if (exam == null ||
                    (TextUtils.isEmpty(exam.getBuilding()) && TextUtils.isEmpty(exam.getRoom()))) {
                mMapFragment.getView().setVisibility(View.INVISIBLE);
            }
            else if (mMap != null) {
                mMapFragment.getView().setVisibility(View.VISIBLE);

                LatLng coords;
                String building;
                int zoom;
                try {
                    building = exam.getBuilding().substring(0, exam.getBuilding().indexOf("-")).trim();

                    coords = mViewModel.getBuilding(building.toLowerCase()).getCoordinates();
                    zoom = 14;
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
    }

    private void updateToolbar(EnrolledExam exam) {
        if (exam != null)
            mBinding.toolbarLayout.setTitle(exam.getName());
    }

    private void updateFab(EnrolledExam exam) {
        mBinding.examCertificateFab.setOnClickListener(v -> showCertificate(exam));
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

        updateMap(mViewModel.getExam().getValue());
    }



    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_CERTIFICATE_ID:
                toggleLoading(true);

                return mViewModel.loadCertificate(this);

            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader loader, Object result) {
        toggleLoading(false);

        switch (loader.getId()) {
            case LOADER_CERTIFICATE_ID:
                handleDownloadCertificate(mViewModel.getExam().getValue());
                break;

            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {}



    private void showCertificate(EnrolledExam exam){
        if (exam != null){
            if (!exam.getCertificatePath().exists())
                getSupportLoaderManager()
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
        File certificate_file = exam.getCertificatePath();
        Uri certificate_uri = FileProvider.getUriForFile(this,
                getString(R.string.file_provider_authority), certificate_file);

        intent.setDataAndType(certificate_uri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    private void handleDownloadCertificate(EnrolledExam exam) {
        Snackbar.make(mBinding.container, R.string.enrolled_exam_certificate_downloaded, Snackbar.LENGTH_LONG)
                .setAction(R.string.open, view -> openCertificate(exam)).show();
    }

    private void toggleLoading(final boolean show) {
        if (show) progress.show();
        else if(progress != null && progress.isShowing()) progress.dismiss();
    }



    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidInjector;
    }
}
