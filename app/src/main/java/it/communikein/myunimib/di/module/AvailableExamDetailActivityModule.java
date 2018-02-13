package it.communikein.myunimib.di.module;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import it.communikein.myunimib.ui.exam.available.AvailableExamDetailActivity;

@Module
public abstract class AvailableExamDetailActivityModule {

    @ContributesAndroidInjector
    abstract AvailableExamDetailActivity contributeAvailableExamDetailActivity();

}
