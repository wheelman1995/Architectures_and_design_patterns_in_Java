package ru.wheelman.messenger;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import javax.inject.Inject;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.Person;
import androidx.core.app.RemoteInput;
import androidx.core.app.TaskStackBuilder;
import ru.wheelman.messenger.common.Constants;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    @Inject
    SharedPreferences sp;
    private String text;
    private String senderPhone;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate ");
        ((App) getApplication()).getAppComponent().inject(this);
        createNotificationChannel();
    }

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
        saveTokenToSharedPrefs(token);

    }

    private void saveTokenToSharedPrefs(String token) {
        sp.edit().putString(LocalConstants.MAIN_SHARED_PREFERENCES_TOKEN, token).commit();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        text = remoteMessage.getData().get(Constants.DATA_KEY_TEXT);
        senderPhone = remoteMessage.getData().get(Constants.DATA_KEY_SOURCE_PHONE);

        createNotification();

    }

    private void createNotification() {

        PendingIntent contentPendingIntent = createContentPendingIntent();

        NotificationCompat.Action replyAction = createReplyAction();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, LocalConstants.CHANNEL_ID_MAIN)
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setAutoCancel(true)
                .setContentIntent(contentPendingIntent)
                .addAction(replyAction)
                .setStyle(new NotificationCompat.MessagingStyle(new Person.Builder().setName("Me").build())
                        .addMessage(text, System.currentTimeMillis(), new Person.Builder().setName("+" + senderPhone).build()));

        NotificationManagerCompat.from(this).notify(senderPhone, LocalConstants.getUniqueIdFromPhone(senderPhone), builder.build());
    }

    private NotificationCompat.Action createReplyAction() {

        RemoteInput remoteInput = createRemoteInput();

        PendingIntent replyPendingIntent = createReplyPendingIntent();

        return new NotificationCompat.Action.Builder(R.drawable.ic_iconfinder_ic_send_48px_352094,
                getString(R.string.reply_label), replyPendingIntent)
                .addRemoteInput(remoteInput)
                .setAllowGeneratedReplies(true)
                .build();
    }

    private PendingIntent createReplyPendingIntent() {
        Intent messageReplyIntent = new Intent(getApplicationContext(), ReplyReceiver.class);
        messageReplyIntent.putExtra(LocalConstants.INTENT_EXTRA_CONVERSATION_TARGET, senderPhone);

        return PendingIntent.getBroadcast(getApplicationContext(),
                LocalConstants.getUniqueIdFromPhone(senderPhone),
                messageReplyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private RemoteInput createRemoteInput() {
        String replyLabel = getResources().getString(R.string.reply_label);
        return new RemoteInput.Builder(LocalConstants.KEY_TEXT_REPLY)
                .setLabel(replyLabel)
                .build();
    }

    private PendingIntent createContentPendingIntent() {
        Intent contentIntent = new Intent(this, ConversationActivity.class);
        contentIntent.putExtra(LocalConstants.INTENT_EXTRA_CONVERSATION_TARGET, senderPhone);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(contentIntent);
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence channelName = getString(R.string.main_notification_channel_name);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(LocalConstants.CHANNEL_ID_MAIN, channelName, importance);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
