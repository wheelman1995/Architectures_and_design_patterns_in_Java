package ru.wheelman.messenger.di;

import dagger.Subcomponent;
import ru.wheelman.messenger.ConversationActivityViewModel;

@Subcomponent(modules = ConversationActivityViewModelModule.class)
public interface ConversationActivityViewModelSubcomponent {
    void inject(ConversationActivityViewModel conversationActivityViewModel);
}
