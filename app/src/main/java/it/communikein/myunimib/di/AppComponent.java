package it.communikein.myunimib.di;

import android.app.Application;
import android.app.IntentService;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.support.AndroidSupportInjectionModule;
import it.communikein.myunimib.UnimibApp;
import it.communikein.myunimib.di.module.ActivityModule;
import it.communikein.myunimib.di.module.IntentServiceModule;
import it.communikein.myunimib.di.module.UnimibAppModule;

@Singleton
@Component(modules = {
        AndroidSupportInjectionModule.class,
        UnimibAppModule.class,
        ActivityModule.class,
        IntentServiceModule.class})
public interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder application(Application application);

        AppComponent build();
    }

    void inject(UnimibApp app);

}
