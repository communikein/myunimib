package it.communikein.myunimib.data;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import it.communikein.myunimib.AppExecutors;
import it.communikein.myunimib.data.model.AvailableExam;
import it.communikein.myunimib.data.model.BookletEntry;
import it.communikein.myunimib.data.model.Building;
import it.communikein.myunimib.data.model.EnrolledExam;
import it.communikein.myunimib.data.model.Exam;
import it.communikein.myunimib.data.model.ExamID;
import it.communikein.myunimib.data.database.UnimibDao;
import it.communikein.myunimib.data.model.Lesson;
import it.communikein.myunimib.data.model.User;
import it.communikein.myunimib.data.network.UnimibNetworkDataSource;
import it.communikein.myunimib.data.network.loaders.CertificateLoader;
import it.communikein.myunimib.data.network.loaders.EnrollLoader;
import it.communikein.myunimib.data.network.loaders.LoginLoader;
import it.communikein.myunimib.data.network.loaders.UserDataLoader;
import it.communikein.myunimib.ui.list.timetable.AddLessonActivity;
import it.communikein.myunimib.utilities.NotificationHelper;
import it.communikein.myunimib.data.UserHelper.AccountRemoveErrorListener;
import it.communikein.myunimib.data.UserHelper.AccountRemovedListener;

@Singleton
public class UnimibRepository {

    private static final String LOG_TAG = UnimibRepository.class.getSimpleName();

    private final Context mContext;
    private final UnimibDao mUnimibDao;
    private final UnimibNetworkDataSource mUnimibNetworkDataSource;
    private final UniversityUtils mUniversityHelper;
    private final UserHelper mUserHelper;
    private final AppExecutors mExecutors;
    private final NotificationHelper mNotificationHelper;

    private boolean mInitialized = false;

    private final MutableLiveData<Integer> mModifiedBookletEntriesCount;
    private final MutableLiveData<Integer> mModifiedAvailableExamsCount;
    private final MutableLiveData<Integer> mModifiedEnrolledExamsCount;

    private static final int NOTIFICATION_BOOKLET_CHANGES_ID = 23;
    private static final int NOTIFICATION_AVAILABLE_EXAMS_CHANGES_ID = 24;
    private static final int NOTIFICATION_ENROLLED_EXAMS_CHANGES_ID = 25;


    @Inject
    public UnimibRepository(Context application, UnimibDao unimibDao,
                            UnimibNetworkDataSource unimibNetworkDataSource,
                            UniversityUtils universityHelper, UserHelper userHelper,
                            AppExecutors executors, NotificationHelper notificationHelper) {
        mContext = application;
        mUnimibDao = unimibDao;
        mUnimibNetworkDataSource = unimibNetworkDataSource;
        mUniversityHelper = universityHelper;
        mUserHelper = userHelper;
        mExecutors = executors;
        mNotificationHelper = notificationHelper;

        mModifiedBookletEntriesCount = new MutableLiveData<>();
        mModifiedAvailableExamsCount = new MutableLiveData<>();
        mModifiedEnrolledExamsCount = new MutableLiveData<>();

        LiveData<List<BookletEntry>> bookletOnline =
                mUnimibNetworkDataSource.getOnlineBooklet();
        bookletOnline.observeForever(newOnlineData -> mExecutors.diskIO().execute(() -> {
            if (newOnlineData != null)
                Log.d(LOG_TAG, "Repository observer notified. Found " + newOnlineData.size() + " booklet entries.");
            else
                Log.d(LOG_TAG, "Repository observer notified. Found NULL booklet entries.");

            handleBookletReceived((ArrayList<BookletEntry>) newOnlineData);
        }));

        LiveData<List<AvailableExam>> availableExamsOnline =
                mUnimibNetworkDataSource.getOnlineAvailableExams();
        availableExamsOnline.observeForever(newOnlineData -> mExecutors.diskIO().execute(() -> {
            if (newOnlineData != null)
                Log.d(LOG_TAG, "Repository observer notified. Found " + newOnlineData.size() + " available exams.");
            else
                Log.d(LOG_TAG, "Repository observer notified. Found NULL available exams.");

            handleAvailableExamsReceived((ArrayList<AvailableExam>) newOnlineData);
        }));

        LiveData<List<EnrolledExam>> enrolledExamsOnline =
                mUnimibNetworkDataSource.getOnlineEnrolledExams();
        enrolledExamsOnline.observeForever(newOnlineData -> mExecutors.diskIO().execute(() -> {
            if (newOnlineData != null)
                Log.d(LOG_TAG, "Repository observer notified. Found " + newOnlineData.size() + " enrolled exams.");
            else
                Log.d(LOG_TAG, "Repository observer notified. Found NULL enrolled exams.");

            handleEnrolledExamsReceived((ArrayList<EnrolledExam>) newOnlineData);
        }));

        mModifiedBookletEntriesCount.observeForever(count -> {
            Log.d(LOG_TAG, "Booklet has been modified. " + count + ".");

            if (count != null && count > 0) {
                Log.d(LOG_TAG, "There are " + count + " booklet changes.");

                mNotificationHelper.notify(NOTIFICATION_BOOKLET_CHANGES_ID,
                        mNotificationHelper.getNotificationBookletChanges());

                Log.d(LOG_TAG, "Clearing booklet changes.");
                mModifiedBookletEntriesCount.postValue(0);
            }
        });

        mModifiedAvailableExamsCount.observeForever(count -> {
            Log.d(LOG_TAG, "Available exams have been modified. " + count + ".");

            if (count != null && count > 0) {
                Log.d(LOG_TAG, "There are " + count + " available exams changes.");

                mNotificationHelper.notify(NOTIFICATION_AVAILABLE_EXAMS_CHANGES_ID,
                        mNotificationHelper.getNotificationAvailableChanges());

                Log.d(LOG_TAG, "Clearing available exams changes.");
                mModifiedAvailableExamsCount.postValue(0);
            }
        });

        mModifiedEnrolledExamsCount.observeForever(count -> {
            Log.d(LOG_TAG, "Enrolled exams have been modified. " + count + ".");

            if (count != null && count > 0) {
                Log.d(LOG_TAG, "There are " + count + " enrolled exams changes.");

                mNotificationHelper.notify(NOTIFICATION_ENROLLED_EXAMS_CHANGES_ID,
                        mNotificationHelper.getNotificationEnrolledChanges());

                Log.d(LOG_TAG, "Clearing enrolled exams changes.");
                mModifiedEnrolledExamsCount.postValue(0);
            }
        });
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

    private void handleBookletReceived(ArrayList<BookletEntry> newOnlineData) {
        int changes = 0;
        if (isBookletEmpty() && newOnlineData != null) {
            mUnimibDao.bulkInsertBooklet(newOnlineData);
            changes = newOnlineData.size();

            Log.d(LOG_TAG, "New booklet entry inserted.");
        }
        else if (newOnlineData != null) {
            ArrayList<BookletEntry> oldExams = getCurrentBooklet();

            if (oldExams != null) {
                for (BookletEntry newEntry : newOnlineData) {
                    BookletEntry oldEntry = getBookletEntry(newEntry.getAdsceId());

                    /* If there is a new booklet entry */
                    if (oldEntry == null) {
                        mUnimibDao.addBookletEntry(newEntry);

                        Log.d(LOG_TAG, "New booklet entry inserted.");
                        changes++;
                    }
                    /* If the exam already exists locally */
                    else {
                        /* Remove is from the list of those that will be deleted from the DB */
                        oldExams.remove(oldEntry);

                        /* If the booklet entry's data has been modified */
                        if (!newEntry.isIdentic(oldEntry)) {
                            mUnimibDao.updateBookletEntry(newEntry);

                            Log.d(LOG_TAG, "Booklet entry updated.");
                            changes++;
                        }
                    }
                }

                if (oldExams.size() > 0) {
                    changes += oldExams.size();
                    deleteBookletEntries(oldExams);
                }
            }
        }

        if (changes > 0) {
            Log.d(LOG_TAG, "Notifying booklet changes.");
            mModifiedBookletEntriesCount.postValue(changes);
        }
    }

    private void handleAvailableExamsReceived(ArrayList<AvailableExam> newOnlineData) {
        int changes = 0;
        if (isAvailableExamsEmpty() && newOnlineData != null) {
            mUnimibDao.bulkInsertAvailableExams(newOnlineData);
            changes = newOnlineData.size();

            Log.d(LOG_TAG, "New available exams inserted.");
        }
        else if (newOnlineData != null) {
            ArrayList<AvailableExam> oldExams = getCurrentAvailableExams();

            if (oldExams != null) {
                for (AvailableExam newEntry : newOnlineData) {
                    AvailableExam oldEntry = getAvailableExam(newEntry);

                    /* If there is a new available exam */
                    if (oldEntry == null) {
                        mUnimibDao.addAvailableExam(newEntry);

                        Log.d(LOG_TAG, "New available exam inserted.");
                        changes++;
                    }
                    /* If the exam already exists locally */
                    else {
                        /* Remove is from the list of those that will be deleted from the DB */
                        oldExams.remove(oldEntry);

                        /* If the available exam's data has been modified */
                        if (!newEntry.isIdentic(oldEntry)) {
                            mUnimibDao.updateAvailableExam(newEntry);

                            Log.d(LOG_TAG, "Available exam updated.");
                            changes++;
                        }
                    }
                }

                if (oldExams.size() > 0) {
                    changes += oldExams.size();
                    deleteAvailableExams(oldExams);
                }
            }
        }

        if (changes > 0) {
            Log.d(LOG_TAG, "Notifying available exams changes.");
            mModifiedAvailableExamsCount.postValue(changes);
        }
    }

    private void handleEnrolledExamsReceived(ArrayList<EnrolledExam> newOnlineData) {
        int changes = 0;
        if (isEnrolledExamsEmpty() && newOnlineData != null) {
            mUnimibDao.bulkInsertEnrolledExams(newOnlineData);
            changes = newOnlineData.size();

            Log.d(LOG_TAG, "New enrolled exams inserted.");
        }
        else if (newOnlineData != null) {
            ArrayList<EnrolledExam> oldExams = getCurrentEnrolledExams();

            if (oldExams != null) {
                for (EnrolledExam newEntry : newOnlineData) {
                    EnrolledExam oldEntry = getEnrolledExam(newEntry);

                    /* If there is a new enrolled exam */
                    if (oldEntry == null) {
                        mUnimibDao.addEnrolledExam(newEntry);

                        Log.d(LOG_TAG, "New enrolled exam inserted.");
                        changes++;
                    }
                    /* If the exam already exists locally */
                    else {
                        /* Remove is from the list of those that will be deleted from the DB */
                        oldExams.remove(oldEntry);

                        /* If the enrolled exam's data has been modified */
                        if (!newEntry.isIdentic(oldEntry)) {
                            mUnimibDao.updateEnrolledExam(newEntry);

                            Log.d(LOG_TAG, "Enrolled exam updated.");
                            changes++;
                        }
                    }
                }

                if (oldExams.size() > 0) {
                    changes += oldExams.size();
                    deleteEnrolledExams(oldExams);
                }
            }
        }

        if (changes > 0) {
            Log.d(LOG_TAG, "Notifying enrolled exam changes.");
            mModifiedEnrolledExamsCount.postValue(changes);
        }
    }




    /***************
     * BOOKLET *****
     ***************/

    public LiveData<List<BookletEntry>> getObservableCurrentBooklet() {
        initializeData();

        return mUnimibDao.getObservableBooklet();
    }

    public ArrayList<BookletEntry> getCurrentBooklet() {
        initializeData();

        return (ArrayList<BookletEntry>) mUnimibDao.getBooklet();
    }

    public LiveData<Boolean> getBookletLoading() {
        return mUnimibNetworkDataSource.getBookletLoading();
    }

    public BookletEntry getBookletEntry(int adsceId) {
        initializeData();

        return mUnimibDao.getBookletEntry(adsceId);
    }

    public boolean isBookletEmpty() {
        return mUnimibDao.getBookletSize() == 0;
    }

    private void deleteBooklet() {
        mUnimibDao.deleteBooklet();
    }

    public void deleteBookletEntry(int adsce_id) {
        mUnimibDao.deleteBookletEntry(adsce_id);
    }

    public void deleteBookletEntries(ArrayList<BookletEntry> exams) {
        for (BookletEntry exam : exams)
            deleteBookletEntry(exam.getAdsceId());
    }


    public void startFetchBookletService() {
        mUnimibNetworkDataSource.startFetchBookletService();
    }

    public void fetchBooklet() {
        mUnimibNetworkDataSource.fetchBooklet(getUser(),
                sessionID -> mExecutors.diskIO().execute(() -> updateUserSessionId(sessionID)));
    }



    /***********************
     * AVAILABLE EXAMS *****
     ***********************/

    public LiveData<List<AvailableExam>> getObservableCurrentAvailableExams() {
        initializeData();

        return mUnimibDao.getObservableAvailableExams();
    }

    public ArrayList<AvailableExam> getCurrentAvailableExams() {
        initializeData();

        return (ArrayList<AvailableExam>) mUnimibDao.getAvailableExams();
    }

    public LiveData<Boolean> getAvailableExamsLoading() {
        return mUnimibNetworkDataSource.getAvailableExamsLoading();
    }

    public LiveData<AvailableExam> getObservableAvailableExam(ExamID examID) {
        initializeData();

        return mUnimibDao.getObservableAvailableExam(examID.getAdsceId(), examID.getAppId(),
                examID.getAttDidEsaId(), examID.getCdsEsaId());
    }

    public AvailableExam getAvailableExam(ExamID examID) {
        initializeData();

        return mUnimibDao.getAvailableExam(examID.getAdsceId(), examID.getAppId(),
                examID.getAttDidEsaId(), examID.getCdsEsaId());
    }

    private boolean isAvailableExamsEmpty() {
        return mUnimibDao.getAvailableExamsSize() == 0;
    }

    private void deleteAvailableExams() {
        mUnimibDao.deleteAvailableExams();
    }

    public void deleteAvailableExam(ExamID examId) {
        mUnimibDao.deleteAvailableExam(examId.getAdsceId(), examId.getAppId(),
                examId.getAttDidEsaId(), examId.getCdsEsaId());
    }

    public void deleteAvailableExams(ArrayList<AvailableExam> exams) {
        for (AvailableExam exam : exams)
            deleteAvailableExam(exam);
    }


    public void startFetchAvailableExamsService() {
        mUnimibNetworkDataSource.startFetchAvailableExamsService();
    }

    public void fetchAvailableExams() {
        mUnimibNetworkDataSource.fetchAvailableExams(getUser(),
                sessionID -> mExecutors.diskIO().execute(() -> updateUserSessionId(sessionID)));
    }

    public EnrollLoader enrollExam(Exam exam, Activity activity,
                                   EnrollLoader.EnrollUpdatesListener enrollUpdatesListener) {
        return mUnimibNetworkDataSource.enrollExam(exam, activity,
                (/*Enrollment complete*/) -> {
                    mExecutors.diskIO().execute(() -> deleteAvailableExam(exam));
                    startFetchEnrolledExamsService();
                    startFetchAvailableExamsService();
                },
                enrollUpdatesListener);
    }




    /**********************
     * ENROLLED EXAMS *****
     **********************/

    public LiveData<List<EnrolledExam>> getObservableCurrentEnrolledExams() {
        initializeData();

        return mUnimibDao.getObservableEnrolledExams();
    }

    public ArrayList<EnrolledExam> getCurrentEnrolledExams() {
        initializeData();

        return (ArrayList<EnrolledExam>) mUnimibDao.getEnrolledExams();
    }

    public LiveData<Boolean> getEnrolledExamsLoading() {
        return mUnimibNetworkDataSource.getEnrolledExamsLoading();
    }

    public LiveData<EnrolledExam> getObservableEnrolledExam(ExamID examID) {
        initializeData();

        return mUnimibDao.getObservableEnrolledExam(examID.getAdsceId(), examID.getAppId(),
                examID.getAttDidEsaId(), examID.getCdsEsaId());
    }

    public EnrolledExam getEnrolledExam(ExamID examID) {
        initializeData();

        return mUnimibDao.getEnrolledExam(examID.getAdsceId(), examID.getAppId(),
                examID.getAttDidEsaId(), examID.getCdsEsaId());
    }

    private boolean isEnrolledExamsEmpty() {
        return mUnimibDao.getEnrolledExamsSize() == 0;
    }

    private void deleteEnrolledExams() {
        mUnimibDao.deleteEnrolledExams();
    }

    public void deleteEnrolledExam(ExamID examId) {
        mUnimibDao.deleteAvailableExam(examId.getAdsceId(), examId.getAppId(),
                examId.getAttDidEsaId(), examId.getCdsEsaId());
    }

    public void deleteEnrolledExams(ArrayList<EnrolledExam> exams) {
        for (EnrolledExam exam : exams)
            deleteEnrolledExam(exam);
    }


    public void startFetchEnrolledExamsService() {
        mUnimibNetworkDataSource.startFetchEnrolledExamsService();
    }

    public void fetchEnrolledExams() {
        mUnimibNetworkDataSource.fetchEnrolledExams(getUser(),
                sessionID -> mExecutors.diskIO().execute(() -> updateUserSessionId(sessionID)));
    }

    public CertificateLoader loadCertificate(EnrolledExam exam, Activity activity) {
        return mUnimibNetworkDataSource.loadCertificate(exam, activity);
    }



    /*****************
     * LESSONS *******
     *****************/

    public void addLesson(Lesson lesson, AddLessonActivity.AddLessonListener listener) {
        mExecutors.diskIO().execute(() -> {
            mUnimibDao.addLesson(lesson);
            listener.onLessonAddComplete();
        });
    }

    public LiveData<List<Lesson>> getObservableTimetable() {
        return mUnimibDao.getObservableTimetable();
    }

    public LiveData<List<Lesson>> getObservableTimetable(String dayOfWeek) {
        return mUnimibDao.getObservableTimetableOfDay(dayOfWeek);
    }

    public List<Lesson> getTimetable() {
        return mUnimibDao.getTimetable();
    }

    public List<Lesson> getTimetable(String dayOfWeek) {
        return mUnimibDao.getTimetableOfDay(dayOfWeek);
    }

    public LiveData<Lesson> getObservableLesson(int id) {
        return mUnimibDao.getObservableLesson(id);
    }

    public Lesson getLesson(int id) {
        return mUnimibDao.getLesson(id);
    }

    public boolean isTimetableEmpty() {
        return mUnimibDao.getTimetableSize() == 0;
    }

    public void deleteTimetable() {
        mUnimibDao.deleteTimetable();
    }

    public void deleteLesson(int id) {
        mUnimibDao.deleteLesson(id);
    }

    public void deleteLessons(List<Lesson> lessons) {
        for (Lesson lesson : lessons)
            mUnimibDao.deleteLesson(lesson.getId());
    }

    public int updateLesson(Lesson lesson) {
        return mUnimibDao.updateLesson(lesson);
    }

    public LiveData<List<String>> getCoursesNames(String like) {
        return mUnimibDao.getCoursesNames(like + '%');
    }



    /*****************
     * BUILDINGS *****
     *****************/

    public List<Building> getCurrentBuildings() {
        return mUniversityHelper.getBuildings();
    }

    public Building getBuilding(String name) {
        return mUniversityHelper.getBuilding(name);
    }



    /*************
     * USERS *****
     *************/

    public void saveUser(User user) {
        mUserHelper.saveUser(user);
    }

    public void updateUserSessionId(String sessionID) {
        mUserHelper.updateSessionId(sessionID);
    }

    public void updateChosenFaculty(int chosenFaculty) {
        mUserHelper.updateChosenFaculty(chosenFaculty);
    }

    public User getUser() {
        return mUserHelper.getUser();
    }

    public LiveData<User> getObservableUser() {
        return mUserHelper.getObservableUser();
    }

    public void deleteUser(Activity activity, AccountRemovedListener accountRemovedListener,
                           AccountRemoveErrorListener accountRemoveErrorListener) {
        mUserHelper.deleteUser(activity, accountRemovedListener, accountRemoveErrorListener);
    }


    public LoginLoader loginUser(User userToLogin, Activity activity) {
        return mUnimibNetworkDataSource.loginUser(userToLogin, activity);
    }

    public UserDataLoader updateUserData(Activity activity) {
        return mUnimibNetworkDataSource.downloadUserData(activity);
    }




    /***************
     * VARIOUS *****
     ***************/

    public void clearData() {
        mExecutors.diskIO().execute(() -> {
            deleteBooklet();
            deleteAvailableExams();
            deleteEnrolledExams();
        });
    }

    public Context getContext() {
        return mContext;
    }
}
