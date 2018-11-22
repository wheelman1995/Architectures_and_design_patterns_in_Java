package ru.wheelman.messenger.di;

import androidx.lifecycle.ViewModelProviders;
import dagger.Module;
import dagger.Provides;
import ru.wheelman.messenger.ConversationActivity;
import ru.wheelman.messenger.ConversationActivityViewModel;

@Module
public class ConversationActivityModule {
    private ConversationActivity activity;

    public ConversationActivityModule(ConversationActivity conversationActivity) {
        this.activity = conversationActivity;
    }

    @Provides
    public ConversationActivityViewModel conversationActivityViewModel() {
        return ViewModelProviders.of(activity).get(ConversationActivityViewModel.class);
    }
}
