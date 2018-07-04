package it.communikein.myunimib.ui;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import dagger.android.AndroidInjection;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import it.communikein.myunimib.R;
import it.communikein.myunimib.data.model.EnrolledExam;
import it.communikein.myunimib.data.model.User;
import it.communikein.myunimib.databinding.ActivityMainBinding;
import it.communikein.myunimib.ui.graduation.projection.GraduationProjectionFragment;
import it.communikein.myunimib.ui.home.HomeFragment;
import it.communikein.myunimib.ui.exam.available.AvailableExamsFragment;
import it.communikein.myunimib.ui.exam.booklet.BookletFragment;
import it.communikein.myunimib.ui.building.BuildingsFragment;
import it.communikein.myunimib.ui.exam.enrolled.EnrolledExamsFragment;
import it.communikein.myunimib.ui.settings.SettingsActivity;
import it.communikein.myunimib.ui.timetable.TimetableFragment;
import it.communikein.myunimib.viewmodel.MainActivityViewModel;
import it.communikein.myunimib.viewmodel.factory.MainActivityViewModelFactory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity implements
        HasSupportFragmentInjector, NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private ActivityMainBinding mBinding;

    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;

    /* */
    @Inject
    MainActivityViewModelFactory viewModelFactory;

    /* */
    private MainActivityViewModel mViewModel;

    private final List<Fragment> fragments = new ArrayList<>();

    public static final String INTENT_PARAM_SHOW_FRAGMENT = "show-fragment";
    private static final String SAVE_FRAGMENT_SELECTED = "save-fragment-selected";
    private String FRAGMENT_SELECTED_TAG;

    private static final int INDEX_FRAGMENT_HOME = 0;
    private static final int INDEX_FRAGMENT_BOOKLET = 1;
    private static final int INDEX_FRAGMENT_EXAMS_AVAILABLE = 2;
    private static final int INDEX_FRAGMENT_EXAMS_ENROLLED = 3;
    private static final int INDEX_FRAGMENT_BUILDINGS = 4;
    private static final int INDEX_FRAGMENT_TIMETABLE = 5;
    private static final int INDEX_FRAGMENT_GRADUATION_PROJECTION = 6;

    private static final long DRAWER_CLOSE_DELAY_MS = 250;
    private ActionBarDrawerToggle mDrawerToggle;
    private final Handler mDrawerActionHandler = new Handler();
    private int drawerItemSelectedId = R.id.navigation_home;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        mBinding.startupProgressbar.show();
        mBinding.tabContainer.setVisibility(View.INVISIBLE);

        mViewModel = ViewModelProviders
                .of(this, viewModelFactory)
                .get(MainActivityViewModel.class);

        parseIntent();
        restoreInstanceState(savedInstanceState);
        initUI(savedInstanceState);

        mBinding.startupProgressbar.hide();
        mBinding.tabContainer.setVisibility(View.VISIBLE);
    }

    public MainActivityViewModel getViewModel() {
        return mViewModel;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(SAVE_FRAGMENT_SELECTED, FRAGMENT_SELECTED_TAG);

        super.onSaveInstanceState(outState);
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        if (FRAGMENT_SELECTED_TAG == null && savedInstanceState != null)
            FRAGMENT_SELECTED_TAG = savedInstanceState.getString(SAVE_FRAGMENT_SELECTED);
    }

    private void initUI(Bundle savedInstanceState){
        mBinding.startupProgressbar.show();

        buildFragmentsList();
        initBottomNavigation();
        initProgressDialog();

        setSupportActionBar(mBinding.toolbar);
        initDrawerNavigation(savedInstanceState);
    }

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.label_logging_out));
        progressDialog.setCancelable(false);
    }

    private void initBottomNavigation() {
        mBinding.bottomNavigation.setOnNavigationItemSelectedListener(item ->
                navigate(item.getItemId()));
        int navId = getNavIdFromFragmentTag(FRAGMENT_SELECTED_TAG);
        mBinding.bottomNavigation.setSelectedItemId(navId);
    }

    private void initDrawerNavigation(Bundle savedInstanceState) {
        initDrawerHeader();
        updateDrawerHeader();

        mDrawerToggle = new ActionBarDrawerToggle(this, mBinding.drawerLayout,
                mBinding.toolbar, R.string.open, R.string.close);
        mBinding.drawerLayout.addDrawerListener(mDrawerToggle);
        mBinding.drawerNavigation.setNavigationItemSelectedListener(this);

        if (savedInstanceState != null) {
            /* Get the last selected drawer menu item ID */
            String DRAWER_ITEM_SELECTED = "drawer-item-selected";
            drawerItemSelectedId = savedInstanceState.getInt(DRAWER_ITEM_SELECTED);

            /* Remove this entry to avoid problems further */
            savedInstanceState.remove(DRAWER_ITEM_SELECTED);
            if (savedInstanceState.size() == 0)
                savedInstanceState = null;

            mBinding.drawerNavigation.getMenu()
                    .findItem(drawerItemSelectedId)
                    .setChecked(true);
        }

        /*
         * If the savedInstanceState is not null, it means we don't need to display the fragment,
         * since it has already been saved and will be restored by the system
         */
        if (savedInstanceState == null) {
            navigate(R.id.navigation_home);

            mBinding.drawerNavigation.getMenu()
                    .findItem(R.id.navigation_home)
                    .setChecked(true);
        }
    }

    private void initDrawerHeader() {
        updateDrawerHeader();
    }

    private void updateDrawerHeader() {
        View header = mBinding.drawerNavigation.getHeaderView(0);
        NetworkImageView userImageView = header.findViewById(R.id.circleView);
        TextView userNameTextView = header.findViewById(R.id.user_name_textview);
        TextView userEmailTextView = header.findViewById(R.id.user_email_textview);

        userImageView.setVisibility(View.VISIBLE);
        userNameTextView.setVisibility(View.VISIBLE);
        userEmailTextView.setVisibility(View.VISIBLE);

        mViewModel.getUser().observe(this, (user) -> {
            if (user != null) {
                userNameTextView.setText(user.getRealName());
                if (user.getUsername().contains("@"))
                    userEmailTextView.setText(user.getUsername());
                else
                    userEmailTextView.setText(user.getUniversityMail());
            }
        });

        mViewModel.loadProfilePictureVolley(userImageView);
    }

    private int getNavIdFromFragmentTag(String tag) {
        int id = R.id.navigation_home;
        if (tag == null) return id;

        if (tag.equals(HomeFragment.TAG))
            id = R.id.navigation_home;
        else if (tag.equals(BookletFragment.TAG))
            id = R.id.navigation_booklet;
        else if (tag.equals(AvailableExamsFragment.TAG))
            id = R.id.navigation_exams_available;
        else if (tag.equals(EnrolledExamsFragment.TAG))
            id = R.id.navigation_exams_enrolled;

        return id;
    }

    private void parseIntent() {
        Intent intent = getIntent();

        if (intent != null) {
            String fragment_tag = intent.getStringExtra(INTENT_PARAM_SHOW_FRAGMENT);

            if (fragment_tag != null)
                FRAGMENT_SELECTED_TAG = fragment_tag;
        }
    }

    private void buildFragmentsList() {
        fragments.add(INDEX_FRAGMENT_HOME, new HomeFragment());
        fragments.add(INDEX_FRAGMENT_BOOKLET, new BookletFragment());
        fragments.add(INDEX_FRAGMENT_EXAMS_AVAILABLE, new AvailableExamsFragment());
        fragments.add(INDEX_FRAGMENT_EXAMS_ENROLLED, new EnrolledExamsFragment());
        fragments.add(INDEX_FRAGMENT_BUILDINGS, new BuildingsFragment());
        fragments.add(INDEX_FRAGMENT_TIMETABLE, new TimetableFragment());
        fragments.add(INDEX_FRAGMENT_GRADUATION_PROJECTION, new GraduationProjectionFragment());
    }


    public void hideTabsLayout() {
        mBinding.tabs.setVisibility(View.GONE);
    }

    public TabLayout getTabLayout() {
        return mBinding.tabs;
    }

    public void showTabsLayout(ArrayList<String> tabs) {
        mBinding.tabs.setVisibility(View.VISIBLE);
        mBinding.tabs.removeAllTabs();

        for (String title : tabs)
            mBinding.tabs.addTab(mBinding.tabs.newTab().setText(title));
    }

    public TabLayout getTabsLayout() {
        return mBinding.tabs;
    }

    public void hideBottomNavigation() {
        mBinding.bottomNavigation.setVisibility(View.GONE);
    }

    public void showBottomNavigation() {
        mBinding.bottomNavigation.setVisibility(View.VISIBLE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mBinding.drawerLayout.isDrawerOpen(GravityCompat.START))
            mBinding.drawerLayout.closeDrawer(GravityCompat.START);

        if (getSupportFragmentManager().findFragmentByTag(HomeFragment.TAG) == null)
            navigate(R.id.navigation_home);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem menuItem) {
        /* Update highlighted item in the bottom_navigation menu */
        drawerItemSelectedId = menuItem.getItemId();
        uncheckNavigationDrawersItems();
        menuItem.setChecked(true);

        /*
         * Allow some time after closing the drawer before performing real bottom_navigation
         * so the user can see what is happening.
         */
        mBinding.drawerLayout.closeDrawer(GravityCompat.START);
        mDrawerActionHandler.postDelayed(() -> navigate(menuItem.getItemId()),
                DRAWER_CLOSE_DELAY_MS);

        return true;
    }

    private void uncheckNavigationDrawersItems() {
        int size = mBinding.drawerNavigation.getMenu().size();

        for (int i=0; i<size; i++)
            mBinding.drawerNavigation.getMenu().getItem(i).setChecked(false);
    }

    private boolean navigate(int tab_id) {
        int index;

        switch (tab_id) {
            case R.id.navigation_home:
                index = INDEX_FRAGMENT_HOME;
                FRAGMENT_SELECTED_TAG = HomeFragment.TAG;
                break;
            case R.id.navigation_booklet:
                index = INDEX_FRAGMENT_BOOKLET;
                FRAGMENT_SELECTED_TAG = BookletFragment.TAG;
                break;
            case R.id.navigation_exams_available:
                index = INDEX_FRAGMENT_EXAMS_AVAILABLE;
                FRAGMENT_SELECTED_TAG = AvailableExamsFragment.TAG;
                break;
            case R.id.navigation_exams_enrolled:
                index = INDEX_FRAGMENT_EXAMS_ENROLLED;
                FRAGMENT_SELECTED_TAG = EnrolledExamsFragment.TAG;
                break;
            case R.id.navigation_buildings:
                index = INDEX_FRAGMENT_BUILDINGS;
                FRAGMENT_SELECTED_TAG = BuildingsFragment.TAG;
                break;
            case R.id.navigation_timetable:
                index = INDEX_FRAGMENT_TIMETABLE;
                FRAGMENT_SELECTED_TAG = TimetableFragment.TAG;
                break;
            case R.id.navigation_graduation_score:
                index = INDEX_FRAGMENT_GRADUATION_PROJECTION;
                FRAGMENT_SELECTED_TAG = GraduationProjectionFragment.TAG;
                break;
            case R.id.navigation_logout:
                return tryLogout();
            case R.id.navigation_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            default:
                return false;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.tab_container, fragments.get(index), FRAGMENT_SELECTED_TAG)
                .addToBackStack(FRAGMENT_SELECTED_TAG)
                .commit();
        return true;
    }



    private boolean tryLogout() {
        showProgress(true);

        mViewModel.logout(this,
                (removed) -> {
                    showProgress(false);

                    if (removed) {
                        showLogoutCompletedDialog();
                    }
                    else {
                        String error = getString(R.string.error_logout_user_not_removed);
                        showLogoutErrorDialog(error);
                    }
                },
                (error) -> {
                    showProgress(false);

                    showLogoutErrorDialog(error);
                });
        return true;
    }

    private void showLogoutCompletedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_logout));
        builder.setMessage(getString(R.string.dialog_logout_message_ok));
        String positiveText = getString(android.R.string.ok);
        builder.setPositiveButton(positiveText,
                (dialog, which) -> MainActivity.this.finish());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showLogoutErrorDialog(String error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.dialog_logout));

        if (error == null) error = getString(R.string.error_logout_failed);
        builder.setMessage(error);

        String positiveText = getString(android.R.string.ok);
        builder.setPositiveButton(positiveText, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showProgress(final boolean show) {
        if (show) progressDialog.show();
        else if(progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
    }

    public PendingIntent buildPendingIntent(int navigation_tab_id) {
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra(INTENT_PARAM_SHOW_FRAGMENT, navigation_tab_id);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Adds the back stack
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        // Gets a PendingIntent containing the entire back stack
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    @Override
    public DispatchingAndroidInjector<Fragment> supportFragmentInjector() {
        return dispatchingAndroidInjector;
    }
}
