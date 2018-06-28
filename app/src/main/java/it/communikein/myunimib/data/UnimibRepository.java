package it.communikein.myunimib.data;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import it.communikein.myunimib.AppExecutors;
import it.communikein.myunimib.R;
import it.communikein.myunimib.data.database.AvailableExamsDao;
import it.communikein.myunimib.data.database.BookletDao;
import it.communikein.myunimib.data.database.EnrolledExamsDao;
import it.communikein.myunimib.data.database.FacultiesDao;
import it.communikein.myunimib.data.database.LessonsDao;
import it.communikein.myunimib.data.database.UserDao;
import it.communikein.myunimib.data.model.AvailableExam;
import it.communikein.myunimib.data.model.BookletEntry;
import it.communikein.myunimib.data.model.Building;
import it.communikein.myunimib.data.model.EnrolledExam;
import it.communikein.myunimib.data.model.Exam;
import it.communikein.myunimib.data.model.ExamID;
import it.communikein.myunimib.data.model.Faculty;
import it.communikein.myunimib.data.model.Lesson;
import it.communikein.myunimib.data.model.User;
import it.communikein.myunimib.data.model.UserAuthentication;
import it.communikein.myunimib.data.network.UnimibNetworkDataSource;
import it.communikein.myunimib.data.network.loaders.CertificateLoader;
import it.communikein.myunimib.data.network.loaders.EnrollLoader;
import it.communikein.myunimib.data.network.loaders.LoginLoader;
import it.communikein.myunimib.data.network.loaders.UnEnrollLoader;
import it.communikein.myunimib.data.network.loaders.UserDataLoader;
import it.communikein.myunimib.ui.graduation.projection.GraduationProjectionFragment;
import it.communikein.myunimib.ui.timetable.AddLessonActivity;
import it.communikein.myunimib.ui.timetable.DayFragment;
import it.communikein.myunimib.utilities.NotificationHelper;
import it.communikein.myunimib.data.UserHelper.AccountRemoveErrorListener;
import it.communikein.myunimib.data.UserHelper.AccountRemovedListener;

@Singleton
public class UnimibRepository {

    private static final String LOG_TAG = UnimibRepository.class.getSimpleName();

    private final Context mContext;

    private final UserDao mUserDao;
    private final BookletDao mBookletDao;
    private final AvailableExamsDao mAvailableExamsDao;
    private final EnrolledExamsDao mEnrolledExamsDao;
    private final LessonsDao mLessonsDao;
    private final FacultiesDao mFacultiesDao;

    private final SharedPreferences mSharedPreferences;
    private final UnimibNetworkDataSource mUnimibNetworkDataSource;
    private final UniversityUtils mUniversityHelper;
    private final UserHelper mUserHelper;
    private final AppExecutors mExecutors;
    private final NotificationHelper mNotificationHelper;

    private final String ACCOUNT_TYPE;

    private boolean mInitialized = false;

    private final MutableLiveData<Integer> mModifiedBookletEntriesCount;
    private final MutableLiveData<Integer> mModifiedAvailableExamsCount;
    private final MutableLiveData<Integer> mModifiedEnrolledExamsCount;

    private static final int NOTIFICATION_BOOKLET_CHANGES_ID = 23;
    private static final int NOTIFICATION_AVAILABLE_EXAMS_CHANGES_ID = 24;
    private static final int NOTIFICATION_ENROLLED_EXAMS_CHANGES_ID = 25;


    @Inject
    public UnimibRepository(Context application, UserDao userDao,
                            BookletDao bookletDao, AvailableExamsDao availableExamsDao,
                            EnrolledExamsDao enrolledExamsDao, LessonsDao lessonsDao,
                            FacultiesDao facultiesDao, SharedPreferences preferences,
                            UnimibNetworkDataSource unimibNetworkDataSource,
                            UniversityUtils universityHelper, UserHelper userHelper,
                            AppExecutors executors, NotificationHelper notificationHelper) {
        mContext = application;

        mUserDao = userDao;
        mBookletDao = bookletDao;
        mAvailableExamsDao = availableExamsDao;
        mEnrolledExamsDao = enrolledExamsDao;
        mLessonsDao = lessonsDao;
        mFacultiesDao = facultiesDao;

        mSharedPreferences = preferences;
        mUnimibNetworkDataSource = unimibNetworkDataSource;
        mUniversityHelper = universityHelper;
        mUserHelper = userHelper;
        mExecutors = executors;
        mNotificationHelper = notificationHelper;

        ACCOUNT_TYPE = application.getString(R.string.account_type);

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
            fetchBooklet();
            fetchAvailableExams();
            fetchEnrolledExams();
        });
    }

    private void handleBookletReceived(ArrayList<BookletEntry> newOnlineData) {
        int changes = 0;
        if (isBookletEmpty() && newOnlineData != null) {
            mBookletDao.add(newOnlineData);
            changes = newOnlineData.size();

            Log.d(LOG_TAG, "New booklet entry inserted.");
        }
        else if (newOnlineData != null) {
            ArrayList<BookletEntry> oldExams = getRealBooklet();

            if (oldExams != null) {
                for (BookletEntry newEntry : newOnlineData) {
                    BookletEntry oldEntry = getBookletEntry(newEntry.getAdsceId());

                    /* If there is a new booklet entry */
                    if (oldEntry == null) {
                        mBookletDao.add(newEntry);

                        Log.d(LOG_TAG, "New booklet entry inserted.");
                        changes++;
                    }
                    /* If the exam already exists locally */
                    else {
                        /* Remove is from the list of those that will be deleted from the DB */
                        oldExams.remove(oldEntry);

                        /* If the booklet entry's data has been modified */
                        if (!newEntry.isIdentic(oldEntry)) {
                            /* Update the DB entry */
                            mBookletDao.update(newEntry);

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
            mAvailableExamsDao.add(newOnlineData);
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
                        mAvailableExamsDao.add(newEntry);

                        Log.d(LOG_TAG, "New available exam inserted.");
                        changes++;
                    }
                    /* If the exam already exists locally */
                    else {
                        /* Remove is from the list of those that will be deleted from the DB */
                        oldExams.remove(oldEntry);

                        /* If the available exam's data has been modified */
                        if (!newEntry.isIdentic(oldEntry)) {
                            mAvailableExamsDao.update(newEntry);

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
            mEnrolledExamsDao.add(newOnlineData);
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
                        mEnrolledExamsDao.add(newEntry);

                        Log.d(LOG_TAG, "New enrolled exam inserted.");
                        changes++;
                    }
                    /* If the exam already exists locally */
                    else {
                        /* Remove is from the list of those that will be deleted from the DB */
                        oldExams.remove(oldEntry);

                        /* If the enrolled exam's data has been modified */
                        if (!newEntry.isIdentic(oldEntry)) {
                            mEnrolledExamsDao.update(newEntry);

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

    public void addBookletEntry(BookletEntry entry, GraduationProjectionFragment.AddProjectionListener listener) {
        mExecutors.diskIO().execute(() -> {
            mBookletDao.add(entry);
            listener.onProjectionAddComplete();
        });
    }

    public LiveData<List<BookletEntry>> getObservableCurrentBooklet() {
        initializeData();

        return mBookletDao.getObservableBooklet();
    }

    public LiveData<List<BookletEntry>> getObservableFakeExams() {
        return mBookletDao.getObservableFakeBooklet();
    }

    private ArrayList<BookletEntry> getRealBooklet() {
        initializeData();

        return (ArrayList<BookletEntry>) mBookletDao.getRealBooklet();
    }

    public LiveData<Boolean> getBookletLoading() {
        return mUnimibNetworkDataSource.getBookletLoading();
    }

    private BookletEntry getBookletEntry(int adsceId) {
        initializeData();

        return mBookletDao.getBookletEntry(adsceId);
    }

    private boolean isBookletEmpty() {
        return mBookletDao.getBookletSize() == 0;
    }

    private void deleteBooklet() {
        mBookletDao.delete();
    }

    public void deleteBookletEntry(BookletEntry entry, GraduationProjectionFragment.DeleteProjectionListener listener) {
        mExecutors.diskIO().execute(() -> {
            mBookletDao.delete(entry.getId());
            listener.onProjectionDeleteComplete();
        });
    }

    public void deleteBookletEntry(BookletEntry entry) {
        mBookletDao.delete(entry.getId());
    }

    private void deleteBookletEntries(ArrayList<BookletEntry> exams) {
        mBookletDao.delete(exams);
    }

    public void fetchBooklet() {
        mUnimibNetworkDataSource.fetchBooklet(getUserAuth(),
                sessionID -> mExecutors.diskIO().execute(() -> updateUserSessionId(sessionID)));
    }



    /***********************
     * AVAILABLE EXAMS *****
     ***********************/

    public LiveData<List<AvailableExam>> getObservableCurrentAvailableExams() {
        initializeData();

        return mAvailableExamsDao.getObservableAvailableExams();
    }

    private ArrayList<AvailableExam> getCurrentAvailableExams() {
        initializeData();

        return (ArrayList<AvailableExam>) mAvailableExamsDao.getAvailableExams();
    }

    public LiveData<Boolean> getAvailableExamsLoading() {
        return mUnimibNetworkDataSource.getAvailableExamsLoading();
    }

    public LiveData<AvailableExam> getObservableAvailableExam(ExamID examID) {
        initializeData();

        return mAvailableExamsDao.getObservableAvailableExam(examID.getAdsceId(), examID.getAppId(),
                examID.getAttDidEsaId(), examID.getCdsEsaId());
    }

    private AvailableExam getAvailableExam(ExamID examID) {
        initializeData();

        return mAvailableExamsDao.getAvailableExam(examID.getAdsceId(), examID.getAppId(),
                examID.getAttDidEsaId(), examID.getCdsEsaId());
    }

    private boolean isAvailableExamsEmpty() {
        return mAvailableExamsDao.getAvailableExamsSize() == 0;
    }

    private void deleteAvailableExams() {
        mAvailableExamsDao.delete();
    }

    private void deleteAvailableExam(ExamID examId) {
        mAvailableExamsDao.delete(examId.getAdsceId(), examId.getAppId(),
                examId.getAttDidEsaId(), examId.getCdsEsaId());
    }

    private void deleteAvailableExams(ArrayList<AvailableExam> exams) {
        mAvailableExamsDao.delete(exams);
    }



    public void fetchAvailableExams() {
        mUnimibNetworkDataSource.fetchAvailableExams(getUserAuth(),
                sessionID -> mExecutors.diskIO().execute(() -> updateUserSessionId(sessionID)));
    }

    public EnrollLoader enrollExam(Exam exam, Activity activity,
                                   EnrollLoader.EnrollUpdatesListener enrollUpdatesListener) {
        return mUnimibNetworkDataSource.enrollExam(exam, activity,
                (/*Enrollment complete*/) -> {
                    mExecutors.diskIO().execute(() -> deleteAvailableExam(exam));
                    fetchEnrolledExams();
                    fetchAvailableExams();
                },
                enrollUpdatesListener);
    }




    /**********************
     * ENROLLED EXAMS *****
     **********************/

    public LiveData<List<EnrolledExam>> getObservableCurrentEnrolledExams() {
        initializeData();

        return mEnrolledExamsDao.getObservableEnrolledExams();
    }

    private ArrayList<EnrolledExam> getCurrentEnrolledExams() {
        initializeData();

        return (ArrayList<EnrolledExam>) mEnrolledExamsDao.getEnrolledExams();
    }

    public LiveData<Boolean> getEnrolledExamsLoading() {
        return mUnimibNetworkDataSource.getEnrolledExamsLoading();
    }

    public LiveData<EnrolledExam> getObservableEnrolledExam(ExamID examID) {
        initializeData();

        return mEnrolledExamsDao.getObservableEnrolledExam(examID.getAdsceId(), examID.getAppId(),
                examID.getAttDidEsaId(), examID.getCdsEsaId());
    }

    private EnrolledExam getEnrolledExam(ExamID examID) {
        initializeData();

        return mEnrolledExamsDao.getEnrolledExam(examID.getAdsceId(), examID.getAppId(),
                examID.getAttDidEsaId(), examID.getCdsEsaId());
    }

    private boolean isEnrolledExamsEmpty() {
        return mEnrolledExamsDao.getEnrolledExamsSize() == 0;
    }

    private void deleteEnrolledExams() {
        mEnrolledExamsDao.delete();
    }

    private void deleteEnrolledExam(ExamID examId) {
        mEnrolledExamsDao.delete(examId.getAdsceId(), examId.getAppId(),
                examId.getAttDidEsaId(), examId.getCdsEsaId());
    }

    private void deleteEnrolledExams(ArrayList<EnrolledExam> exams) {
        mEnrolledExamsDao.delete(exams);
    }



    public void fetchEnrolledExams() {
        mUnimibNetworkDataSource.fetchEnrolledExams(getUserAuth(),
                sessionID -> mExecutors.diskIO().execute(() -> updateUserSessionId(sessionID)));
    }

    public CertificateLoader loadCertificate(EnrolledExam exam, Activity activity) {
        return mUnimibNetworkDataSource.loadCertificate(exam, activity);
    }

    public UnEnrollLoader unEnrollExam(Exam exam, Activity activity,
                                   UnEnrollLoader.UnEnrollUpdatesListener unEnrollUpdatesListener) {
        return mUnimibNetworkDataSource.unEnrollExam(exam, activity,
                (/*Un-enrollment complete*/) -> {
                    mExecutors.diskIO().execute(() -> deleteEnrolledExam(exam));
                    fetchEnrolledExams();
                    fetchAvailableExams();
                },
                unEnrollUpdatesListener);
    }



    /*****************
     * LESSONS *******
     *****************/

    public void addLesson(Lesson lesson, AddLessonActivity.AddLessonListener listener) {
        mExecutors.diskIO().execute(() -> {
            mLessonsDao.add(lesson);
            listener.onLessonAddComplete();
        });
    }

    public LiveData<List<Lesson>> getObservableTimetable() {
        return mLessonsDao.getObservableTimetable();
    }

    public LiveData<List<Lesson>> getObservableTimetable(String dayOfWeek) {
        return mLessonsDao.getObservableTimetableOfDay(dayOfWeek);
    }

    public List<Lesson> getTimetable() {
        return mLessonsDao.getTimetable();
    }

    public List<Lesson> getTimetable(String dayOfWeek) {
        return mLessonsDao.getTimetableOfDay(dayOfWeek);
    }

    public LiveData<Lesson> getObservableLesson(int id) {
        return mLessonsDao.getObservableLesson(id);
    }

    public Lesson getLesson(int id) {
        return mLessonsDao.getLesson(id);
    }

    public boolean isTimetableEmpty() {
        return mLessonsDao.getTimetableSize() == 0;
    }

    public void deleteTimetable() {
        mLessonsDao.delete();
    }

    public void deleteLesson(int id, DayFragment.DeleteLessonListener listener) {
        mExecutors.diskIO().execute(() -> {
            mLessonsDao.delete(id);
            listener.onDeleteLessonComplete();
        });
    }

    public void deleteLessons(ArrayList<Lesson> lessons) {
        mLessonsDao.delete(lessons);
    }

    public int updateLesson(Lesson lesson) {
        return mLessonsDao.update(lesson);
    }

    public LiveData<List<String>> getCoursesNames(String like) {
        return mLessonsDao.getCoursesNames(like + '%');
    }



    /*****************
     * FACULTIES *****
     *****************/

    public void addFaculty(Faculty lesson, AddLessonActivity.AddLessonListener listener) {
        mExecutors.diskIO().execute(() -> {
            mFacultiesDao.add(lesson);
            listener.onLessonAddComplete();
        });
    }

    public LiveData<List<Faculty>> getObservableFaculties() {
        return mFacultiesDao.getObservableAllFaculties();
    }

    public LiveData<List<Faculty>> getObservableFaculties(String userId) {
        return mFacultiesDao.getObservableFacultiesForUser(userId);
    }

    public List<Faculty> getFaculties() {
        return mFacultiesDao.getAllFaculties();
    }

    public List<Faculty> getFaculties(String userId) {
        return mFacultiesDao.getFacultiesForUser(userId);
    }

    public LiveData<Faculty> getObservableFaculty(int code) {
        return mFacultiesDao.getObservableFaculty(code);
    }

    public Faculty getFaculty(int code) {
        return mFacultiesDao.getFaculty(code);
    }

    public boolean hasUserFaculties(String username) {
        return mFacultiesDao.getNumberOfFacultiesForUser(username) == 0;
    }

    public void deleteFaculties() {
        mFacultiesDao.delete();
    }

    public void deleteFaculty(int id, DayFragment.DeleteLessonListener listener) {
        mExecutors.diskIO().execute(() -> {
            mFacultiesDao.delete(id);
            listener.onDeleteLessonComplete();
        });
    }

    public void deleteFaculties(ArrayList<Faculty> faculties) {
        mFacultiesDao.delete(faculties);
    }

    public int updatefaculty(Faculty faculty) {
        return mFacultiesDao.update(faculty);
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

    public interface UserOperationsListener {
        void onUserLoaded(User user);
    }

    public void saveUser(User user) {
        mExecutors.diskIO().execute(() -> {
            mUserDao.add(user);
            mUserHelper.saveUser(user);
        });
    }

    public void updateUserSessionId(String sessionID) {
        mUserHelper.updateSessionId(sessionID);
    }

    public void updateChosenFaculty(Faculty chosenFaculty) {
        mUserHelper.updateChosenFaculty(chosenFaculty);
    }

    public User getUserAuth() {
        return mUserHelper.getUser();
    }

    public User getUser(final UserOperationsListener listener) {
        User user = mUserHelper.getUser();

        mExecutors.diskIO().execute(() -> {
            User userDb = mUserDao.getUser(user.getUsername());
            userDb.setUsername(user.getUsername());
            userDb.setPassword(user.getPassword());
            userDb.setSessionId(user.getSessionId());
            userDb.setSelectedFaculty(user.getSelectedFaculty());

            if (listener != null) listener.onUserLoaded(userDb);
        });

        return user;
    }

    public void deleteUser(Activity activity, AccountRemovedListener accountRemovedListener,
                           AccountRemoveErrorListener accountRemoveErrorListener) {
        mExecutors.diskIO().execute(mUserDao::delete);
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

    public String getAccountType() {
        return ACCOUNT_TYPE;
    }
}
