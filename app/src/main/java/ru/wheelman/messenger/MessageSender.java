package ru.wheelman.messenger;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import javax.inject.Inject;

import ru.wheelman.messenger.common.Constants;
import ru.wheelman.messenger.di.AppScope;
import ru.wheelman.messenger.di.UserPhoneQualifier;

import static android.content.Context.BIND_AUTO_CREATE;

@AppScope
public class MessageSender {

    private static final String TAG = MessageSender.class.getSimpleName();
    @Inject
    Context appContext;
    @Inject
    @UserPhoneQualifier
    String sender;
    @Inject
    FirebaseFirestore db;
    private boolean bound;
    private ServiceConnection serviceConnection;
    private Messenger messenger;
    private String senderCollectionPath;
    private String receiverCollectionPath;
    private String receiver;
    private String text;

    @Inject
    public MessageSender() {
        initListeners();
    }

    private void initListeners() {
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                bound = true;
                Log.d(TAG, "onServiceConnected");
                messenger = new Messenger(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "onServiceDisconnected");
                bound = false;
                messenger = null;
            }
        };
    }

    public void send(String receiver, String text) {

        this.receiver = receiver;
        this.text = text;

        senderCollectionPath = String.format("%s/%s/%s", Constants.USERS_COLLECTION, sender, receiver);
        receiverCollectionPath = String.format("%s/%s/%s", Constants.USERS_COLLECTION, receiver, sender);

        writeToOwnAndTargetDatabases();

        sendPush();

    }

    private void sendPush() {
        db.collection(Constants.USERS_COLLECTION).document(receiver).get().addOnSuccessListener(documentSnapshot -> {
            String targetToken = (String) documentSnapshot.get(Constants.FIRESTORE_FIELD_TOKEN);

            Bundle bundle = createBundleForPush(targetToken);

            if (!bound) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("ru.wheelman.messenger.service", "ru.wheelman.messenger.service.MainService"));

                intent.putExtra(Constants.INTENT_EXTRA_BUNDLE_KEY, bundle);

                appContext.bindService(intent, serviceConnection, BIND_AUTO_CREATE);

            } else {
                try {
                    android.os.Message message = new Message();

                    message.setData(bundle);

                    messenger.send(message);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Bundle createBundleForPush(String targetToken) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DATA_KEY_TOKEN, targetToken);
        bundle.putString(Constants.DATA_KEY_SOURCE_PHONE, sender);
        bundle.putString(Constants.DATA_KEY_TEXT, text);
        return bundle;
    }

    private void writeToOwnAndTargetDatabases() {
        CollectionReference collection = db.collection(senderCollectionPath);

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(Constants.FIRESTORE_MESSAGE_FIELD_TEXT, text);
        hashMap.put(Constants.FIRESTORE_MESSAGE_FIELD_TYPE, Constants.FIRESTORE_MESSAGE_FIELD_TYPE_SENT);
        hashMap.put(Constants.FIRESTORE_MESSAGE_FIELD_TIME, Long.toString(System.currentTimeMillis()));

        collection.add(hashMap);

        collection = db.collection(receiverCollectionPath);

        hashMap.put(Constants.FIRESTORE_MESSAGE_FIELD_TYPE, Constants.FIRESTORE_MESSAGE_FIELD_TYPE_RECEIVED);

        collection.add(hashMap);
    }

    public void unbindService() {
        if (bound) {
            appContext.unbindService(serviceConnection);
            bound = false;
        }
    }
}
