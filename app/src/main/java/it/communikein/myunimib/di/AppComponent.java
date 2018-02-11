package it.communikein.myunimib.di;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Component;
import dagger.android.support.AndroidSupportInjectionModule;
import it.communikein.myunimib.UnimibApp;
import it.communikein.myunimib.di.module.AddLessonActivityModule;
import it.communikein.myunimib.di.module.AvailableExamDetailActivityModule;
import it.communikein.myunimib.di.module.EnrolledExamDetailActivityModule;
import it.communikein.myunimib.di.module.LoginActivityModule;
import it.communikein.myunimib.di.module.MainActivityModule;
import it.communikein.myunimib.di.module.IntentServiceModule;
import it.communikein.myunimib.di.module.UnimibAppModule;

@Singleton
@Component(modules = {
        AndroidSupportInjectionModule.class,
        UnimibAppModule.class,
        LoginActivityModule.class,
        MainActivityModule.class,
        AvailableExamDetailActivityModule.class,
        EnrolledExamDetailActivityModule.class,
        AddLessonActivityModule.class,
        IntentServiceModule.class})
public interface AppComponent {

    void inject(UnimibApp app);

    Application getApplication();

}
