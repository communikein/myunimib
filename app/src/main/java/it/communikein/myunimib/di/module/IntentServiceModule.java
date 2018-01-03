package it.communikein.myunimib.di.module;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import it.communikein.myunimib.data.network.BookletSyncIntentService;
import it.communikein.myunimib.data.network.BookletSyncJobService;
import it.communikein.myunimib.data.network.ExamAvailableSyncIntentService;
import it.communikein.myunimib.data.network.ExamAvailableSyncJobService;
import it.communikein.myunimib.data.network.ExamEnrolledSyncIntentService;
import it.communikein.myunimib.data.network.ExamEnrolledSyncJobService;

@Module
public abstract class IntentServiceModule {

    @ContributesAndroidInjector
    abstract BookletSyncIntentService contributeBookletSyncIntentService();

    @ContributesAndroidInjector
    abstract ExamAvailableSyncIntentService contributeAvailableExamSyncIntentService();

    @ContributesAndroidInjector
    abstract ExamEnrolledSyncIntentService contributeEnrolledExamSyncIntentService();



    @ContributesAndroidInjector
    abstract BookletSyncJobService contributeBookletSyncJobService();

    @ContributesAndroidInjector
    abstract ExamAvailableSyncJobService contributeAvailableExamSyncJobService();

    @ContributesAndroidInjector
    abstract ExamEnrolledSyncJobService contributeEnrolledExamSyncJobService();

}
