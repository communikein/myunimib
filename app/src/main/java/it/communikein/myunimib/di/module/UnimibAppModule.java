package it.communikein.myunimib.di.module;

import android.app.Application;
import android.arch.persistence.room.Room;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import it.communikein.myunimib.AppExecutors;
import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.database.UnimibDao;
import it.communikein.myunimib.data.database.UnimibDatabase;
import it.communikein.myunimib.data.network.UnimibNetworkDataSource;
import it.communikein.myunimib.utilities.NotificationHelper;

@Module
public class UnimibAppModule {

    @Singleton
    @Provides
    UnimibRepository provideRepository(UnimibDao dao, UnimibNetworkDataSource networkDataSource,
                                       AppExecutors executors, NotificationHelper notificationHelper) {
        return new UnimibRepository(dao, networkDataSource, executors, notificationHelper);
    }

    @Singleton
    @Provides
    UnimibNetworkDataSource provideNetworkDataSource(Application app, AppExecutors executors) {
        return new UnimibNetworkDataSource(app.getApplicationContext(), executors);
    }

    @Singleton @Provides
    UnimibDatabase provideDatabase(Application app) {
        return Room.databaseBuilder(app, UnimibDatabase.class, UnimibDatabase.NAME).build();
    }

    @Singleton @Provides
    UnimibDao provideDao(UnimibDatabase database) {
        return database.unimibDao();
    }

    @Singleton @Provides
    NotificationHelper provideNotificationHelper(Application app) {
        return new NotificationHelper(app.getApplicationContext());
    }

}
