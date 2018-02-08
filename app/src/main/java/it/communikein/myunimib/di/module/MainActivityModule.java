package it.communikein.myunimib.di.module;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import it.communikein.myunimib.ui.MainActivity;
import it.communikein.myunimib.ui.detail.AvailableExamDetailActivity;
import it.communikein.myunimib.ui.detail.EnrolledExamDetailActivity;

@Module
public abstract class MainActivityModule {

    @ContributesAndroidInjector(modules = FragmentsBuilderModule.class)
    abstract MainActivity contributeMainActivity();

}