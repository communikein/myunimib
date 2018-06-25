package it.communikein.myunimib.di.module;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import it.communikein.myunimib.data.network.BookletSyncJobService;
import it.communikein.myunimib.data.network.ExamAvailableSyncJobService;
import it.communikein.myunimib.data.network.ExamEnrolledSyncJobService;
import it.communikein.myunimib.ui.widget.WidgetUpdateService;

@Module
public abstract class IntentServiceModule {

    @ContributesAndroidInjector
    abstract BookletSyncJobService contributeBookletSyncJobService();

    @ContributesAndroidInjector
    abstract ExamAvailableSyncJobService contributeAvailableExamSyncJobService();

    @ContributesAndroidInjector
    abstract ExamEnrolledSyncJobService contributeEnrolledExamSyncJobService();

    @ContributesAndroidInjector
    abstract WidgetUpdateService contributeWidgetUpdateService();

}
