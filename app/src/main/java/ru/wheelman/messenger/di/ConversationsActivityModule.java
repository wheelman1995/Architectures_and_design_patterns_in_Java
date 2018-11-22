package ru.wheelman.messenger.di;

import dagger.Module;
import ru.wheelman.messenger.ConversationsActivity;

@Module
public class ConversationsActivityModule {

    private final ConversationsActivity conversationsActivity;

    public ConversationsActivityModule(ConversationsActivity conversationsActivity) {
        this.conversationsActivity = conversationsActivity;
    }
}
