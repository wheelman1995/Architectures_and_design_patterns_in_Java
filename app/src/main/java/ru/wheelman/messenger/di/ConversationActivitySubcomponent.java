package ru.wheelman.messenger.di;

import dagger.Subcomponent;
import ru.wheelman.messenger.ConversationActivity;

@Subcomponent(modules = ConversationActivityModule.class)
public interface ConversationActivitySubcomponent {
    void inject(ConversationActivity conversationActivity);
}
