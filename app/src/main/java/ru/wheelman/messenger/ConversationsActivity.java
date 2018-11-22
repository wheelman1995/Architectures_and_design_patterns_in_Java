package ru.wheelman.messenger;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import javax.inject.Inject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ru.wheelman.messenger.common.Constants;
import ru.wheelman.messenger.databinding.ActivityConversationsBinding;
import ru.wheelman.messenger.databinding.DialogNewConversationBinding;
import ru.wheelman.messenger.di.ConversationsActivityModule;
import ru.wheelman.messenger.di.ConversationsActivitySubcomponent;
import ru.wheelman.messenger.di.UserPhoneQualifier;

public class ConversationsActivity extends AppCompatActivity {
    private static final String TAG = ConversationsActivity.class.getSimpleName();
    @Inject
    public FirebaseFirestore db;
    @Inject
    @UserPhoneQualifier
    public String userPhoneNumber;
    @Inject
    public SharedPreferences sp;
    private ConversationsActivityRecyclerViewAdapter adapter;
    private RecyclerView recyclerView;
    private ConversationsActivitySubcomponent conversationsActivitySubcomponent;
    private String userDocument;
    private ActivityConversationsBinding binding;
    private ArrayList<String> chats;

    private void initVariables() {
        conversationsActivitySubcomponent = ((App) getApplication()).getAppComponent()
                .newConversationsActivitySubcomponent(new ConversationsActivityModule(this));
        conversationsActivitySubcomponent.inject(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_conversations);
        userDocument = Constants.USERS_COLLECTION + "/" + userPhoneNumber;
        recyclerView = binding.rvConversationsActivity;
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        chats = new ArrayList<>();
        adapter = new ConversationsActivityRecyclerViewAdapter(chats);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);

        initVariables();

        addUserIntoUsers();

    }

    @Override
    protected void onStart() {
        super.onStart();
        initConversationsListener();
    }

    private void initConversationsListener() {
        db.document(userDocument).addSnapshotListener(this, (documentSnapshot, e) -> {
            ArrayList<String> chats = (ArrayList<String>) documentSnapshot.get(Constants.FIRESTORE_FIELD_CHATS);
            if (chats == null || chats.isEmpty())
                binding.tvEmpty.setVisibility(View.VISIBLE);
            else {
                binding.tvEmpty.setVisibility(View.GONE);
                ConversationsActivity.this.chats.clear();
                ConversationsActivity.this.chats.addAll(chats);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void addUserIntoUsers() {
        DocumentReference document = db.document(userDocument);
        document.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists()) {
                document.set(new HashMap<>());
            }
        });
    }

    public void showNewConversationDialog(View view) {
        DialogNewConversationBinding binding = DialogNewConversationBinding.inflate(getLayoutInflater());
        AppCompatEditText tietPhone = binding.tietPhone;
        TextInputLayout til = binding.til;
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(binding.getRoot())
                .setTitle(getString(R.string.new_conv_dialog_title))
                .setNegativeButton(R.string.new_conv_dialog_neg, null)
                .setPositiveButton(R.string.new_conv_dialog_pos, null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                    .setOnClickListener(v -> {
                        dialog.dismiss();
                    });
            dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {

                        if (tietPhone.getText() != null) {
                            String phone = tietPhone.getText().toString();

                            if (inputIsValid(phone)) {

                                searchUser(til, dialog, phone.substring(1));

                            } else {
                                til.setError(getString(R.string.new_conv_dialog_wrong_input));
//                                Toast.makeText(this, getString(R.string.new_conv_dialog_wrong_input_toast), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            til.setError(getString(R.string.new_conv_dialog_input_is_empty));
//                            Toast.makeText(this, getString(R.string.new_conv_dialog_input_is_empty), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        dialog.show();
    }

    private void searchUser(TextInputLayout til, AlertDialog dialog, String phone) {
        DocumentReference document = db.collection(Constants.USERS_COLLECTION).document(phone);
        document.get().addOnSuccessListener(documentSnapshot -> {
            if (!documentSnapshot.exists())
                til.setError(getString(R.string.user_not_found));
//                Toast.makeText(this, getString(R.string.user_not_found), Toast.LENGTH_LONG).show();
            else {
                dialog.dismiss();
                writeChatIdToOwnAndTargetDatabases(phone);
                openConversation(phone);
            }
        });
    }

    private void writeChatIdToOwnAndTargetDatabases(String phone) {

        String targetDocument = Constants.USERS_COLLECTION + "/" + phone;

        writeToChatsField(targetDocument, userPhoneNumber);
        writeToChatsField(userDocument, phone);

    }

    private void writeToChatsField(String document, String value) {

        DocumentReference doc = db.document(document);

        doc.get().addOnSuccessListener(this, documentSnapshot -> {
            ArrayList<String> chats = (ArrayList<String>) documentSnapshot.get(Constants.FIRESTORE_FIELD_CHATS);

            if (chats == null)
                chats = new ArrayList<>();

            if (!chats.contains(value))
                chats.add(value);

            HashMap<String, ArrayList<String>> hashMap = new HashMap<>();
            hashMap.put(Constants.FIRESTORE_FIELD_CHATS, chats);
            doc.set(hashMap, SetOptions.merge());
        });
    }

    private void openConversation(String phone) {
        Intent intent = new Intent(getApplicationContext(), ConversationActivity.class);
        intent.putExtra(LocalConstants.INTENT_EXTRA_CONVERSATION_TARGET, phone);
        startActivity(intent);
    }

    public void openConversation(View view) {
        CharSequence phone = ((AppCompatTextView) view).getText();
        phone = phone.subSequence(1, phone.length());
        Intent intent = new Intent(this, ConversationActivity.class);
        intent.putExtra(LocalConstants.INTENT_EXTRA_CONVERSATION_TARGET, phone);
        startActivity(intent);
    }

    private boolean inputIsValid(String text) {
        return Pattern.matches("\\+\\d{11,}", text);
    }
}
