package it.communikein.myunimib.di.module;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import it.communikein.myunimib.ui.MainActivity;

@Module
public abstract class MainActivityModule {

    @ContributesAndroidInjector()
    abstract MainActivity contributeMainActivity();

}
