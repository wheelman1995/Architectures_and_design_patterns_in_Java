package ru.wheelman.messenger.di;

import com.google.firebase.firestore.FirebaseFirestore;

import dagger.Module;
import dagger.Provides;

@Module
public class FirebaseModule {

    @Provides
    @AppScope
    public FirebaseFirestore firebaseFirestore() {
        return FirebaseFirestore.getInstance();
    }
}
