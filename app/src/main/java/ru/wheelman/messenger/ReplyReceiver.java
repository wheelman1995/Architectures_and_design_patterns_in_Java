package ru.wheelman.messenger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import javax.inject.Inject;

import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

public class ReplyReceiver extends BroadcastReceiver {

    @Inject
    MessageSender messageSender;

    public ReplyReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ReplyReceiver", "onReceive");
        ((App) context.getApplicationContext()).getAppComponent().inject(this);

        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        CharSequence text = remoteInput.getCharSequence(LocalConstants.KEY_TEXT_REPLY);

        messageSender.send(intent.getStringExtra(LocalConstants.INTENT_EXTRA_CONVERSATION_TARGET), text.toString());

        String phone = intent.getStringExtra(LocalConstants.INTENT_EXTRA_CONVERSATION_TARGET);
        Log.d("ReplyReceiver", phone);

        NotificationManagerCompat.from(context).cancel(phone, LocalConstants.getUniqueIdFromPhone(phone));
    }

}
