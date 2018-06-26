package it.communikein.myunimib.di.module;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import it.communikein.myunimib.ui.graduation.projection.GraduationProjectionFragment;
import it.communikein.myunimib.ui.home.HomeFragment;
import it.communikein.myunimib.ui.exam.available.AvailableExamsFragment;
import it.communikein.myunimib.ui.exam.booklet.BookletFragment;
import it.communikein.myunimib.ui.building.BuildingsFragment;
import it.communikein.myunimib.ui.exam.enrolled.EnrolledExamsFragment;
import it.communikein.myunimib.ui.timetable.TimetableFragment;

@Module
public abstract class FragmentsBuilderModule {

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

    @ContributesAndroidInjector
    abstract GraduationProjectionFragment contributeGraduationProjectionFragment();

}
