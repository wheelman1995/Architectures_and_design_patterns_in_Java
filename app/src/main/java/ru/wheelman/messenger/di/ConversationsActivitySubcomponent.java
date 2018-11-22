package ru.wheelman.messenger.di;

import dagger.Subcomponent;
import ru.wheelman.messenger.ConversationsActivity;

@Subcomponent(modules = {ConversationsActivityModule.class})
@ConversationsActivityScope
public interface ConversationsActivitySubcomponent {

    void inject(ConversationsActivity conversationsActivity);
}
