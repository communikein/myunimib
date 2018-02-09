package it.communikein.myunimib.di.module;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import it.communikein.myunimib.ui.detail.HomeFragment;
import it.communikein.myunimib.ui.list.availableexam.AvailableExamsFragment;
import it.communikein.myunimib.ui.list.booklet.BookletFragment;
import it.communikein.myunimib.ui.list.building.BuildingsFragment;
import it.communikein.myunimib.ui.list.enrolledexam.EnrolledExamsFragment;
import it.communikein.myunimib.ui.list.timetable.TimetableFragment;

@Module
public abstract class FragmentsBuilderModule {

    @ContributesAndroidInjector
    abstract HomeFragment contributeHomeFragment();

    @ContributesAndroidInjector
    abstract BookletFragment contributeBookletFragment();

    @ContributesAndroidInjector
    abstract AvailableExamsFragment contributeAvailableExamsFragment();

    @ContributesAndroidInjector
    abstract EnrolledExamsFragment contributeEnrolledExamsFragment();

    @ContributesAndroidInjector
    abstract BuildingsFragment contributeBuildingsFragment();

    @ContributesAndroidInjector
    abstract TimetableFragment contributeTimetableFragment();

}
