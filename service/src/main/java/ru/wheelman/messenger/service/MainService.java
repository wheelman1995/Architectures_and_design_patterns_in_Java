package ru.wheelman.messenger.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.Nullable;
import ru.wheelman.messenger.common.Constants;

public class MainService extends Service {

    private static final String TAG = MainService.class.getSimpleName();
    private static String text;
    private static String targetToken;
    private static String sourcePhone;
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    private Messenger mMessenger;

    private static void sendPush() {
        Log.d(TAG, "sendPush");
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                com.google.firebase.messaging.Message message = com.google.firebase.messaging.Message.builder()
                        .setToken(targetToken)
                        .putData(Constants.DATA_KEY_SOURCE_PHONE, sourcePhone)
                        .putData(Constants.DATA_KEY_TEXT, text)
                        .build();
                try {
                    FirebaseMessaging.getInstance().send(message);
                } catch (FirebaseMessagingException e) {
                    e.printStackTrace();
                }
            }
        };
        executorService.submit(runnable);
    }

    private static void extractBundle(Bundle bundle) {
        text = bundle.getString(Constants.DATA_KEY_TEXT);
        sourcePhone = bundle.getString(Constants.DATA_KEY_SOURCE_PHONE);
        targetToken = bundle.getString(Constants.DATA_KEY_TOKEN);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate service");

        if (FirebaseApp.getApps().isEmpty()) {
            initFirebaseApp();
        }
    }

    private void initFirebaseApp() {
        try (InputStream serviceAccount = getResources().openRawResource(R.raw.messenger_32d45_06b8804e8564)) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        extractBundle(intent.getBundleExtra(Constants.INTENT_EXTRA_BUNDLE_KEY));

        mMessenger = new Messenger(new IncomingHandler(this));

        sendPush();

        return mMessenger.getBinder();
    }

    @Override
    public void onDestroy() {
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    /**
     * Handler of incoming messages from clients.
     */
    static class IncomingHandler extends Handler {
        private Context applicationContext;

        IncomingHandler(Context context) {
            applicationContext = context.getApplicationContext();
        }

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage");

            extractBundle(msg.getData());

            sendPush();
        }
    }
}
