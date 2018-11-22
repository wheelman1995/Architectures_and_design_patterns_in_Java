package ru.wheelman.messenger;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import androidx.annotation.MainThread;
import androidx.lifecycle.LiveData;
import ru.wheelman.messenger.common.Constants;

public class MyLiveData extends LiveData<ArrayList<Message>> {
    @Inject
    FirebaseFirestore db;
    private ArrayList<Message> messages;
    private String collectionToObserve;
    private ListenerRegistration registration;

    @Inject
    @MainThread
    public MyLiveData() {
        messages = new ArrayList<>();
    }

    public void setObservationTarget(String collection) {
        this.collectionToObserve = collection;
    }

    @Override
    protected void onActive() {
        requestDataUpdates();
    }

    @Override
    protected void onInactive() {
        if (registration != null)
            registration.remove();
    }

    private void requestDataUpdates() {
        CollectionReference collection = db.collection(collectionToObserve);

        registration = collection.addSnapshotListener((queryDocumentSnapshots, e) -> {
            messages.clear();

            List<DocumentChange> documentChanges = queryDocumentSnapshots.getDocumentChanges();

            ArrayList<DocumentChange> list = new ArrayList<>(documentChanges);

            Collections.sort(list, (o1, o2) -> (Long.valueOf((String) o1.getDocument().get(Constants.FIRESTORE_MESSAGE_FIELD_TIME))) <
                    (Long.valueOf((String) o2.getDocument().get(Constants.FIRESTORE_MESSAGE_FIELD_TIME))) ? -1 : 1);

            for (int i = 0; i < list.size(); i++) {
                DocumentChange documentChange = list.get(i);
                String type = (String) documentChange.getDocument().get(Constants.FIRESTORE_MESSAGE_FIELD_TYPE);
                String text = (String) documentChange.getDocument().get(Constants.FIRESTORE_MESSAGE_FIELD_TEXT);
                long time = Long.valueOf((String) documentChange.getDocument().get(Constants.FIRESTORE_MESSAGE_FIELD_TIME));

                Message message = new Message(text, type, time);

                messages.add(message);
            }
            setValue(messages);
        });
    }
}
