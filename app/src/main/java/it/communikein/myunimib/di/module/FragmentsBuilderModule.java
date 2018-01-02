package it.communikein.myunimib.di.module;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import it.communikein.myunimib.ui.list.availableexam.AvailableExamsFragment;
import it.communikein.myunimib.ui.list.booklet.BookletFragment;
import it.communikein.myunimib.ui.list.enrolledexam.EnrolledExamsFragment;

@Module
public abstract class FragmentsBuilderModule {

    @ContributesAndroidInjector
    abstract BookletFragment contributeBookletFragment();

    @ContributesAndroidInjector
    abstract AvailableExamsFragment contributeAvailableExamsFragment();

    @ContributesAndroidInjector
    abstract EnrolledExamsFragment contributeEnrolledExamsFragment();

}
