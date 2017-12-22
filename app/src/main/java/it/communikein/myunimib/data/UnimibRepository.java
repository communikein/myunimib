package it.communikein.myunimib.data;

import android.arch.lifecycle.LiveData;
import android.arch.paging.DataSource;
import android.util.Log;

import java.util.List;

import it.communikein.myunimib.AppExecutors;
import it.communikein.myunimib.data.database.AvailableExam;
import it.communikein.myunimib.data.database.BookletEntry;
import it.communikein.myunimib.data.database.EnrolledExam;
import it.communikein.myunimib.data.database.ListAvailableExam;
import it.communikein.myunimib.data.database.ListBookletEntry;
import it.communikein.myunimib.data.database.ListEnrolledExam;
import it.communikein.myunimib.data.database.UnimibDao;
import it.communikein.myunimib.data.network.UnimibNetworkDataSource;

public class UnimibRepository {

    private static final String LOG_TAG = UnimibRepository.class.getSimpleName();

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static UnimibRepository sInstance;
    private final UnimibDao mUnimibDao;
    private final UnimibNetworkDataSource mUnimibNetworkDataSource;
    private final AppExecutors mExecutors;
    private boolean mInitialized = false;

    private UnimibRepository(UnimibDao weatherDao,
                             UnimibNetworkDataSource unimibNetworkDataSource,
                               AppExecutors executors) {
        mUnimibDao = weatherDao;
        mUnimibNetworkDataSource = unimibNetworkDataSource;
        mExecutors = executors;

        LiveData<List<BookletEntry>> bookletOnline =
                mUnimibNetworkDataSource.getOnlineBooklet();
        bookletOnline.observeForever(newOnlineData -> {
            mExecutors.diskIO().execute(() -> {
                deleteBooklet();
                Log.d(LOG_TAG, "Old data deleted");
                mUnimibDao.bulkInsertBooklet(newOnlineData);
                Log.d(LOG_TAG, "New values inserted.");
            });
        });

        LiveData<List<AvailableExam>> availableExamsOnline =
                mUnimibNetworkDataSource.getOnlineAvailableExams();
        availableExamsOnline.observeForever(newOnlineData -> {
            mExecutors.diskIO().execute(() -> {
                deleteAvailableExams();
                Log.d(LOG_TAG, "Old data deleted");
                mUnimibDao.bulkInsertAvailableExams(newOnlineData);
                Log.d(LOG_TAG, "New values inserted.");
            });
        });

        LiveData<List<EnrolledExam>> enrolledExamsOnline =
                mUnimibNetworkDataSource.getOnlineEnrolledExams();
        enrolledExamsOnline.observeForever(newOnlineData -> {
            mExecutors.diskIO().execute(() -> {
                deleteEnrolledExams();
                Log.d(LOG_TAG, "Old data deleted");
                mUnimibDao.bulkInsertEnrolledExams(newOnlineData);
                Log.d(LOG_TAG, "New values inserted.");
            });
        });
    }

    public synchronized static UnimibRepository getInstance(
            UnimibDao unimibDao, UnimibNetworkDataSource unimibNetworkDataSource,
            AppExecutors executors) {
        Log.d(LOG_TAG, "Getting the repository");
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new UnimibRepository(unimibDao, unimibNetworkDataSource,
                        executors);
                Log.d(LOG_TAG, "Made new repository");
            }
        }
        return sInstance;
    }

    /**
     * Creates periodic sync tasks and checks to see if an immediate sync is required. If an
     * immediate sync is required, this method will take care of making sure that sync occurs.
     */
    private synchronized void initializeData() {

        // Only perform initialization once per app lifetime. If initialization has already been
        // performed, we have nothing to do in this method.
        if (mInitialized) return;
        mInitialized = true;

        mUnimibNetworkDataSource.scheduleRecurringFetchBookletSync();
        mUnimibNetworkDataSource.scheduleRecurringFetchAvailableExamsSync();
        mUnimibNetworkDataSource.scheduleRecurringFetchEnrolledExamsSync();

        mExecutors.diskIO().execute(() ->{
            startFetchBookletService();
            startFetchAvailableExamsService();
            startFetchEnrolledExamsService();
        });
    }


    public DataSource.Factory<Integer, ListBookletEntry> getCurrentBooklet() {
        initializeData();

        return mUnimibDao.getBooklet();
    }

    public DataSource.Factory<Integer, ListAvailableExam> getCurrentAvailableExams() {
        initializeData();

        return mUnimibDao.getAvailableExams();
    }

    public DataSource.Factory<Integer, ListEnrolledExam> getCurrentEnrolledExams() {
        initializeData();

        return mUnimibDao.getEnrolledExams();
    }

    public LiveData<EnrolledExam> getEnrolledExam(int adsce_id) {
        initializeData();

        return mUnimibDao.getEnrolledExam(adsce_id);
    }


    /**
     * Deletes old weather data because we don't need to keep multiple days' data
     */
    private void deleteBooklet() {
        mUnimibDao.deleteBooklet();
    }

    private void deleteAvailableExams() {
        mUnimibDao.deleteAvailableExams();
    }

    private void deleteEnrolledExams() {
        mUnimibDao.deleteEnrolledExams();
    }


    /**
     * Network related operation
     */

    private void startFetchBookletService() {
        mUnimibNetworkDataSource.startFetchBookletService();
    }

    private void startFetchAvailableExamsService() {
        mUnimibNetworkDataSource.startFetchAvailableExamsService();
    }

    private void startFetchEnrolledExamsService() {
        mUnimibNetworkDataSource.startFetchEnrolledExamsService();
    }

}
