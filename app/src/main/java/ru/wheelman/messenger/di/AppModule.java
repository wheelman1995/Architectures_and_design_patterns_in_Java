package ru.wheelman.messenger.di;

import android.content.Context;
import android.content.SharedPreferences;

import dagger.Module;
import dagger.Provides;
import ru.wheelman.messenger.LocalConstants;
import ru.wheelman.messenger.common.Constants;

@Module
public class AppModule {

    private final Context context;

    public AppModule(Context context) {
        this.context = context;
    }


    @Provides
    @AppScope
    public Context context() {
        return context;
    }

    @Provides
    @AppScope
    public SharedPreferences sharedPreferences(Context context) {
        return context.getSharedPreferences(LocalConstants.MAIN_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    @Provides
    @UserPhoneQualifier
    @AppScope
    public String userPhone(SharedPreferences sp) {
        return sp.getString(LocalConstants.MAIN_SHARED_PREFERENCES_USER_PHONE, null);
    }

    @Provides
    @UserDocumentQualifier
    @AppScope
    public String userDocument(@UserPhoneQualifier String userPhone) {
        return String.format("%s/%s", Constants.USERS_COLLECTION, userPhone);
    }
}
