package it.communikein.myunimib.di.module;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import it.communikein.myunimib.AppExecutors;
import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.UniversityUtils;
import it.communikein.myunimib.data.UserHelper;
import it.communikein.myunimib.data.database.AvailableExamsDao;
import it.communikein.myunimib.data.database.BookletDao;
import it.communikein.myunimib.data.database.EnrolledExamsDao;
import it.communikein.myunimib.data.database.FacultiesDao;
import it.communikein.myunimib.data.database.LessonsDao;
import it.communikein.myunimib.data.database.UnimibDatabase;
import it.communikein.myunimib.data.database.UserDao;
import it.communikein.myunimib.data.network.ProfilePictureVolleyRequest;
import it.communikein.myunimib.data.network.UnimibNetworkDataSource;
import it.communikein.myunimib.utilities.NotificationHelper;

@Module
public class UnimibAppModule {

    private final Application application;

    public UnimibAppModule(Application application) {
        this.application = application;
    }

    @Provides
    Application provideApplication() {
        return application;
    }



    @Singleton @Provides
    UnimibRepository provideRepository(Application application, UserDao userDao,
                                       BookletDao bookletDao, AvailableExamsDao availableExamsDao,
                                       EnrolledExamsDao enrolledExamsDao, LessonsDao lessonsDao,
                                       FacultiesDao facultiesDao, SharedPreferences preferences,
                                       UnimibNetworkDataSource networkDataSource,
                                       UniversityUtils universityHelper, AppExecutors executors,
                                       UserHelper userHelper, NotificationHelper notificationHelper) {
        return new UnimibRepository(application, userDao,
                bookletDao, availableExamsDao, enrolledExamsDao, lessonsDao, facultiesDao,
                preferences, networkDataSource, universityHelper,
                userHelper, executors, notificationHelper);
    }

    @Singleton @Provides
    UnimibNetworkDataSource provideNetworkDataSource(Application application,
                                                     AppExecutors executors) {
        return new UnimibNetworkDataSource(application.getApplicationContext(), executors);
    }

    @Singleton @Provides
    UnimibDatabase provideDatabase(Application application) {
        return Room.databaseBuilder(application, UnimibDatabase.class, UnimibDatabase.NAME)
                .addMigrations(UnimibDatabase.MIGRATION_29_30)
                .build();
    }

    @Singleton @Provides
    UserDao provideUserDao(UnimibDatabase database) {
        return database.userDao();
    }

    @Singleton @Provides
    BookletDao provideBookletDao(UnimibDatabase database) {
        return database.bookletDao();
    }

    @Singleton @Provides
    AvailableExamsDao provideAvailableExamsDao(UnimibDatabase database) {
        return database.availableExamsDao();
    }

    @Singleton @Provides
    EnrolledExamsDao provideEnrolledExamsDao(UnimibDatabase database) {
        return database.enrolledExamsDao();
    }

    @Singleton @Provides
    LessonsDao provideLessonsDao(UnimibDatabase database) {
        return database.lessonsDao();
    }

    @Singleton @Provides
    FacultiesDao provideFacultiesDao(UnimibDatabase database) {
        return database.facultyDao();
    }

    @Singleton @Provides
    NotificationHelper provideNotificationHelper(Application application) {
        return new NotificationHelper(application.getApplicationContext());
    }

    @Singleton @Provides
    UniversityUtils provideUniversityHelper() {
        return new UniversityUtils();
    }

    @Singleton @Provides
    UserHelper provideUserHelper(Application application) {
        return new UserHelper(application);
    }

    @Singleton @Provides
    SharedPreferences provideSharedPreferences(Application application) {
        return application.getSharedPreferences("myunimib", Context.MODE_PRIVATE);
    }


    @Singleton @Provides
    ProfilePictureVolleyRequest provideProfilePictureRequest(UnimibRepository repository,
                                                             Application application) {
        return new ProfilePictureVolleyRequest(repository, application);
    }

}
