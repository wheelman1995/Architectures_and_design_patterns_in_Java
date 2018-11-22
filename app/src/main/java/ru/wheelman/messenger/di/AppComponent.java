package ru.wheelman.messenger.di;

import android.content.Context;

import com.google.firebase.firestore.FirebaseFirestore;

import dagger.Component;
import ru.wheelman.messenger.AuthenticationActivity;
import ru.wheelman.messenger.MainActivity;
import ru.wheelman.messenger.MyFirebaseMessagingService;
import ru.wheelman.messenger.ReplyReceiver;

@Component(modules = {FirebaseModule.class, AppModule.class})
@AppScope
public interface AppComponent {


    //    Dependent components require the parent component to explicitly list out what dependencies can be injected downstream, while subcomponents do not
    FirebaseFirestore getFirebaseFirestore();

    Context getContext();

    // factory method to instantiate the subcomponent defined here (passing in the module instance)
    ConversationsActivitySubcomponent newConversationsActivitySubcomponent(ConversationsActivityModule conversationsActivityModule);

    ConversationActivitySubcomponent newConversationActivitySubcomponent(ConversationActivityModule conversationActivityModule);

    ConversationActivityViewModelSubcomponent newConversationActivityViewModelSubcomponent(ConversationActivityViewModelModule conversationActivityViewModelModule);

    //    the activities, services, or fragments that are allowed to request the dependencies declared by the modules (by means of the @Inject annotation)
// should be declared in this class with individual inject() methods:
    void inject(MainActivity mainActivity);

    void inject(AuthenticationActivity authenticationActivity);

    //    void inject(ConversationsActivity conversationsActivity);
    void inject(MyFirebaseMessagingService myFirebaseMessagingService);

    //    void inject(ConversationActivityViewModel conversationActivityViewModel);
    void inject(ReplyReceiver replyReceiver);

    // void inject(MyFragment fragment);
    // void inject(MyService service);

}
