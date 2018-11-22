package ru.wheelman.messenger;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((App) getApplication()).getAppComponent().inject(this);

        checkAuthentication();
        finish();
    }

    private void checkAuthentication() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            // already signed in
            Intent intent = new Intent(getApplicationContext(), ConversationsActivity.class);
            startActivity(intent);
        } else {
            // not signed in
            startActivity(new Intent(getApplicationContext(), AuthenticationActivity.class));
        }
    }


}
