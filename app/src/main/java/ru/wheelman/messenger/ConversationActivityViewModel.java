package ru.wheelman.messenger;

import android.app.Application;

import javax.inject.Inject;

import androidx.lifecycle.AndroidViewModel;
import ru.wheelman.messenger.di.ConversationActivityViewModelModule;

public class ConversationActivityViewModel extends AndroidViewModel {

    @Inject
    MyLiveData messages;


    public ConversationActivityViewModel(Application application) {
        super(application);

        ((App) application).getAppComponent().newConversationActivityViewModelSubcomponent(new ConversationActivityViewModelModule())
                .inject(this);
    }

    public MyLiveData getMessagesLiveData() {
        return messages;
    }

}
