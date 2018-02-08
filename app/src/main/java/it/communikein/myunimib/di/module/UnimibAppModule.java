package it.communikein.myunimib.di.module;

import android.app.Application;
import android.arch.persistence.room.Room;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import it.communikein.myunimib.AppExecutors;
import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.UniversityUtils;
import it.communikein.myunimib.data.UserHelper;
import it.communikein.myunimib.data.database.UnimibDao;
import it.communikein.myunimib.data.database.UnimibDatabase;
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
    UnimibRepository provideRepository(Application application, UnimibDao dao,
                                       UnimibNetworkDataSource networkDataSource,
                                       UniversityUtils universityHelper, AppExecutors executors,
                                       UserHelper userHelper, NotificationHelper notificationHelper) {
        return new UnimibRepository(application, dao, networkDataSource, universityHelper,
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
                .fallbackToDestructiveMigration()
                .build();
    }

    @Singleton @Provides
    UnimibDao provideDao(UnimibDatabase database) {
        return database.unimibDao();
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
    ProfilePictureVolleyRequest provideProfilePictureRequest(UnimibRepository repository,
                                                             Application application) {
        return new ProfilePictureVolleyRequest(repository, application);
    }

    @Singleton @Provides
    UserHelper provideUserHelper(Application application) {
        return new UserHelper(application);
    }

}
