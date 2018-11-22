package ru.wheelman.messenger;

public final class LocalConstants {
    public static final String INTENT_EXTRA_CONVERSATION_TARGET = "ru.wheelman.messenger.INTENT_EXTRA_CONVERSATION_TARGET";
    public static final String INTENT_ACTION_SEND_REPLY = "ru.wheelman.messenger.SEND_REPLY";
    public static final String MAIN_SHARED_PREFERENCES_NAME = "MAIN_SHARED_PREFERENCES_NAME";
    public static final String MAIN_SHARED_PREFERENCES_USER_PHONE = "MAIN_SHARED_PREFERENCES_USER_PHONE";
    public static final String MAIN_SHARED_PREFERENCES_TOKEN = "token";
    public static final String MAIN_SHARED_PREFERENCES_OPENED_CHATS_SET = "MAIN_SHARED_PREFERENCES_OPENED_CHATS_SET";
    public static final String KEY_TEXT_REPLY = "key_text_reply";
    public static final String CHANNEL_ID_MAIN = "main_notification_channel";

    private LocalConstants() {
    }

    public static int getUniqueIdFromPhone(String phone) {
        int id = 0;
        for (int i = 0; i < phone.length(); i++) {
            id = +Character.getNumericValue(phone.charAt(i));
        }
        return id;
    }

}
