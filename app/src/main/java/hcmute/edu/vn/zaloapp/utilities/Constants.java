package hcmute.edu.vn.zaloapp.utilities;

import java.util.HashMap;

public class Constants {
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_NAME = "name";
    public static final String KEY_PHONE_NUMBER = "phoneNumber";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_PREFERENCE_NAME = "chatAppPreference";
    public static final String KEY_IS_SIGN_IN = "isSignIn";
    public static final String KEY_USER_ID = "userID";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_USER = "user";
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "senderID";
    public static final String KEY_RECEIVER_ID = "receiverID";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "timestamp";
    public static final String KEY_COLLECTION_CONVERSATION = "conversations";
    public static final String  KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LAST_MESSAGE = "lastMessage";
    public static final String KEY_AVAILABILITY = "availability";

    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    public static HashMap<String,String> remoteMsgHeaders = null;
    public static HashMap<String ,String> getRemoteMSGHeaders(){
         if (remoteMsgHeaders == null){
             remoteMsgHeaders = new HashMap<>();
             remoteMsgHeaders.put(
                     REMOTE_MSG_AUTHORIZATION,
                     "key=AAAAkcRPaA8:APA91bEw60hMQ9Bx8ZAhmYD2x1W1nel-9U5ZM0oXYOAWPnZ5vurIebQGM3qqQxDEhp5n6YqiolgV7GOO-mpxfElmXUIfaJtNgDrnftMEYl4SYWuyMe99ZnMwje1uuuwm33d3NC0n5aK1"
             );
             remoteMsgHeaders.put(
                     REMOTE_MSG_CONTENT_TYPE,
                     "application/json"
             );
         }
         return remoteMsgHeaders;
    }
}
