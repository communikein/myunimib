package it.communikein.myunimib.di.module;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import it.communikein.myunimib.ui.login.LoginActivity;

@Module
public abstract class LoginActivityModule {

    @ContributesAndroidInjector
    abstract LoginActivity contributeLoginActivity();

}
