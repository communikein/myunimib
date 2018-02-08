package it.communikein.myunimib;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import dagger.android.HasServiceInjector;
import it.communikein.myunimib.di.AppComponent;
import it.communikein.myunimib.di.DaggerAppComponent;
import it.communikein.myunimib.di.module.UnimibAppModule;

public class UnimibApp extends Application implements HasActivityInjector, HasServiceInjector {

    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidActivityInjector;

    @Inject
    DispatchingAndroidInjector<Service> dispatchingAndroidServiceInjector;

    protected AppComponent appComponent;

    public static UnimibApp get(Context context) {
        return (UnimibApp) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        appComponent = DaggerAppComponent
                .builder()
                .unimibAppModule(new UnimibAppModule(this))
                .build();

        appComponent.inject(this);
    }

    public AppComponent getComponent() {
        return appComponent;
    }

    @Override
    public AndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidActivityInjector;
    }

    @Override
    public AndroidInjector<Service> serviceInjector() {
        return dispatchingAndroidServiceInjector;
    }
}
