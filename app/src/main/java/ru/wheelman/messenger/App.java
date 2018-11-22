package ru.wheelman.messenger;

import android.app.Application;

import ru.wheelman.messenger.di.AppComponent;
import ru.wheelman.messenger.di.AppModule;
import ru.wheelman.messenger.di.DaggerAppComponent;

public class App extends Application {

    private static final String TAG = App.class.getSimpleName();

    private AppComponent appComponent;

    public AppComponent getAppComponent() {
        return appComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }
}
