package ru.wheelman.messenger;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Collections;
import java.util.HashMap;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import ru.wheelman.messenger.common.Constants;

public class AuthenticationActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private static final String TAG = AuthenticationActivity.class.getSimpleName();
    @Inject
    SharedPreferences sp;

    @Inject
    FirebaseFirestore db;
    private String phoneNumber;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((App) getApplication()).getAppComponent().inject(this);

        startActivityForResult(
                // Get an instance of AuthUI based on the default app
                AuthUI.getInstance().createSignInIntentBuilder()
                        .setAvailableProviders(Collections.singletonList(new AuthUI.IdpConfig.PhoneBuilder()
                                .setDefaultCountryIso("ru")
                                .build()))
                        .build(),
                RC_SIGN_IN);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            // Successfully signed in
            if (resultCode == RESULT_OK) {
                phoneNumber = response.getPhoneNumber().substring(1);

                savePhone();

                saveTokenToFirestore();

                Log.d(TAG, phoneNumber);
                Intent intent = new Intent(getApplicationContext(), ConversationsActivity.class);
//                intent.putExtra(MainActivity.INTENT_EXTRA_PHONE_NUMBER, phoneNumber);
                startActivity(intent);
                finish();
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    finish();
                    return;
//                    showSnackbar(R.string.sign_in_cancelled);
                }

                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
//                    showSnackbar(R.string.no_internet_connection);
                    finish();
                    return;
                }

//                showSnackbar(R.string.unknown_error);
                Log.e(TAG, "Sign-in error: ", response.getError());
                finish();
            }
        }
    }

    private void savePhone() {
        sp.edit().putString(LocalConstants.MAIN_SHARED_PREFERENCES_USER_PHONE, phoneNumber).commit();
    }

    private void saveTokenToFirestore() {
        String token = sp.getString(LocalConstants.MAIN_SHARED_PREFERENCES_TOKEN, null);

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(Constants.FIRESTORE_FIELD_TOKEN, token);

        db.document(String.format("%s/%s", Constants.USERS_COLLECTION, phoneNumber)).set(hashMap, SetOptions.merge());
    }
}
