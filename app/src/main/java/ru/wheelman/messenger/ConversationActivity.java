package ru.wheelman.messenger;

import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import ru.wheelman.messenger.common.Constants;
import ru.wheelman.messenger.databinding.ActivityConversationBinding;
import ru.wheelman.messenger.di.ConversationActivityModule;
import ru.wheelman.messenger.di.UserPhoneQualifier;

public class ConversationActivity extends AppCompatActivity {

    private static final String TAG = ConversationActivity.class.getSimpleName();
    @Inject
    @UserPhoneQualifier
    String userPhoneNumber;
    @Inject
    MessageSender messageSender;
    @Inject
    FirebaseFirestore db;
    @Inject
    ConversationActivityViewModel viewModel;
    private String myCollectionPath;
    private String targetCollectionPath;
    private boolean bound;
    private String conversationTarget;
    private AppCompatEditText etMessage;
    private ActivityConversationBinding binding;
    private MyLiveData messages;
    private Observer<ArrayList<ru.wheelman.messenger.Message>> observer;

    private void initVariables() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_conversation);
        etMessage = binding.etMessage;
        conversationTarget = getIntent().getStringExtra(LocalConstants.INTENT_EXTRA_CONVERSATION_TARGET);
        myCollectionPath = String.format("%s/%s/%s", Constants.USERS_COLLECTION, userPhoneNumber, conversationTarget);
        targetCollectionPath = String.format("%s/%s/%s", Constants.USERS_COLLECTION, conversationTarget, userPhoneNumber);
        messages = viewModel.getMessagesLiveData();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((App) getApplication()).getAppComponent().newConversationActivitySubcomponent(new ConversationActivityModule(this))
                .inject(this);

        initVariables();

        initListeners();

        subscribeForData();

    }

    private void initListeners() {

        observer = messages -> {
            for (int i = 0; i < messages.size(); i++) {
                ru.wheelman.messenger.Message message = messages.get(i);
                String type = message.getType();
                String text = message.getText();

                AppCompatTextView textView = createTextView();
                LinearLayoutCompat.LayoutParams params = createLayoutParams();

                switch (type) {
                    case Constants.FIRESTORE_MESSAGE_FIELD_TYPE_RECEIVED:
                        params.gravity = Gravity.END;
                        textView.setGravity(Gravity.END);
                        textView.setBackgroundColor(getResources().getColor(R.color.colorBlue));
                        break;
                    case Constants.FIRESTORE_MESSAGE_FIELD_TYPE_SENT:
                        params.gravity = Gravity.START;
                        textView.setGravity(Gravity.START);
                        textView.setBackgroundColor(getResources().getColor(R.color.colorViolet));
                        break;
                }
                textView.setLayoutParams(params);
                textView.setText(text);
                binding.llMessages.addView(textView);
            }
            binding.svActivityConv.fullScroll(View.FOCUS_DOWN);
            etMessage.requestFocus();
        };

//        binding.svActivityConv.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
//            binding.svActivityConv.fullScroll(View.FOCUS_DOWN);
//            etMessage.requestFocus();
//        });
        binding.svActivityConv.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etMessage.getWindowToken(), 0);
                    break;
            }
            v.performClick();
            return false;
        });
    }

    private void subscribeForData() {
        messages.setObservationTarget(myCollectionPath);
        messages.observe(this, observer);
    }

    private LinearLayoutCompat.LayoutParams createLayoutParams() {

        LinearLayoutCompat.LayoutParams params = new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMarginStart(dpTOpx(10));
        params.setMarginEnd(dpTOpx(10));
        params.topMargin = dpTOpx(5);
        params.bottomMargin = dpTOpx(5);
        return params;
    }

    private int dpTOpx(int dp) {
        //px = dp * (dpi / 160)
        int dpi = getResources().getDisplayMetrics().densityDpi;
        return dp * (dpi / 160);
    }

    private AppCompatTextView createTextView() {
        AppCompatTextView textView = new AppCompatTextView(this);
        textView.setFocusable(true);
        textView.setClickable(true);
        textView.setTextIsSelectable(true);

        int widthPixels = getResources().getDisplayMetrics().widthPixels;
        int threeFourth = widthPixels - widthPixels / 4;
        textView.setMaxWidth(threeFourth);
        return textView;
    }

    public void sendMessage(View view) {
        if (!etMessage.getText().toString().isEmpty()) {
            String text = etMessage.getText().toString();
            etMessage.setText("");

            messageSender.send(conversationTarget, text);
        }
    }

    @Override
    protected void onStop() {
        messageSender.unbindService();
        super.onStop();
    }
}
