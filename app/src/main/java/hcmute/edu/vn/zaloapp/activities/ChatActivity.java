package hcmute.edu.vn.zaloapp.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import hcmute.edu.vn.zaloapp.R;
import hcmute.edu.vn.zaloapp.adapters.ChatAdapter;
import hcmute.edu.vn.zaloapp.databinding.ActivityChatBinding;
import hcmute.edu.vn.zaloapp.models.ChatMessage;
import hcmute.edu.vn.zaloapp.models.User;
import hcmute.edu.vn.zaloapp.network.ApiClient;
import hcmute.edu.vn.zaloapp.network.ApiService;
import hcmute.edu.vn.zaloapp.utilities.Constants;
import hcmute.edu.vn.zaloapp.utilities.PreferenceManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private  User receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private  String conversationId;
    private boolean isVisible;
    private  String encodedImage;
    private  boolean isReceiverAvailable = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        isVisible = false;
        encodedImage = "";
        cameraPermission();
        setListener();
        loadReceiverDetail();
        init();
        listenMessages();
    }

    private void cameraPermission(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, 101);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101){
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            binding.previewImage.setImageBitmap(photo);
            encodedImage = encodeImage(photo);
            binding.previewImgLayout.setVisibility(View.VISIBLE);
            binding.pickImageFrom.setVisibility(View.GONE);
        }
    }

    private void init(){
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                getBitmapFromEncodedString(receiverUser.image),
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }


    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String messageBody){
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMSGHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call,@NonNull Response<String> response) {
                if (response.isSuccessful()){
                    try {
                        if (response.body() != null){
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if (responseJson.getInt("failure") == 1){
                                JSONObject error = (JSONObject) results.get(0);
                                showToast(error.getString("error"));
                                return;
                            }
                        }
                    }
                    catch (JSONException e){
                        e.printStackTrace();
                    }
                    showToast("Notification sent successfull");
                }
                else {
                    showToast("Error: "+ response.code() );
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call,@NonNull Throwable t) {
                showToast(t.getMessage());
            }
        });
    }
    private void listenAvailabilityOfReceiver(){
        database.collection(Constants.KEY_COLLECTION_USERS).document(
                receiverUser.id
        ).addSnapshotListener(ChatActivity.this,(value, error) -> {
            if (error!=null){
                return;
            }
            if (value!=null){
                if (value.getLong(Constants.KEY_AVAILABILITY) != null){
                    int availability = Objects.requireNonNull(
                            value.getLong(Constants.KEY_AVAILABILITY)
                    ).intValue();
                    isReceiverAvailable = availability == 1;
                }
                receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN);
                if (receiverUser.image == null){
                    receiverUser.image = value.getString(Constants.KEY_IMAGE);
                    chatAdapter.setReceiverProfileImage(getBitmapFromEncodedString(receiverUser.image));
                    chatAdapter.notifyItemRangeChanged(0,chatMessages.size());
                }
            }
            if (isReceiverAvailable) {

                binding.activeStatus.setText("Active now");
                binding.iconStatus.setVisibility(View.VISIBLE);
            }
            else {
                binding.activeStatus.setText("Not active");
                binding.iconStatus.setVisibility(View.GONE);
            }

        });
    }

    private void sendMessage(){
        HashMap<String,Object> message = new HashMap<>();
        if (binding.inputMessage.getText().toString().equals("") && encodedImage.equals("")){
            Toast.makeText(this, "Enter your message", Toast.LENGTH_SHORT).show();
        }
        else {
            message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            if (encodedImage.equals("")){
                message.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());
            }
            else {
                message.put(Constants.KEY_MESSAGE, encodedImage);
                binding.inputMessage.setText("");
                encodedImage = "";
                binding.previewImgLayout.setVisibility(View.GONE);
            }

            message.put(Constants.KEY_TIMESTAMP, new Date());
            database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
            if (conversationId != null){
                updateConversation(binding.inputMessage.getText().toString());
            }else {
                HashMap<String, Object> conversation = new HashMap<>();
                conversation.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                conversation.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
                conversation.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
                conversation.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
                conversation.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
                conversation.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
                conversation.put(Constants.KEY_LAST_MESSAGE, binding.inputMessage.getText().toString());
                conversation.put(Constants.KEY_TIMESTAMP, new Date());
                addConversation(conversation);
            }
            if (!isReceiverAvailable){
                try {
                    JSONArray tokens = new JSONArray();
                    tokens.put(receiverUser.token);

                    JSONObject data = new JSONObject();
                    data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                    data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                    data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                    data.put(Constants.KEY_MESSAGE, binding.inputMessage.getText().toString());

                    JSONObject body = new JSONObject();
                    body.put(Constants.REMOTE_MSG_DATA,data);
                    body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                    sendNotification(body.toString());
                }catch (Exception e){
                    showToast(e.getMessage());
                }
            }
            binding.inputMessage.setText(null);
        }
    }

    private  void listenMessages(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);

    }
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null){
            return;
        }
        if (value != null){
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()){
                if (documentChange.getType() ==     DocumentChange.Type.ADDED){
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderID = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverID = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    if (chatMessage.message.length() > 200){
                        chatMessage.image = chatMessage.message;
                        chatMessage.message = "";
                    }
                    else {
                        chatMessage.image = "";
                    }
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages,(obj1,obj2)-> obj1.dateObject.compareTo(obj2.dateObject));
            if (count == 0){
                chatAdapter.notifyDataSetChanged();
            }
            else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(),chatMessages.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() -1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
        if (conversationId == null){
            checkForConversation();
        }
    };
    private Bitmap getBitmapFromEncodedString(String encodedImage){
        if (encodedImage != null){
            byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        }else {
            return  null;
        }

    }
    private void loadReceiverDetail(){
        receiverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textName.setText(receiverUser.name);

    }
    private void setListener(){
        binding.imageBack.setOnClickListener(v-> {
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
        });
        binding.layoutSend.setOnClickListener(v -> sendMessage());

        binding.layoutGallery.setOnClickListener(v -> {
            if (!isVisible){
                binding.pickImageFrom.setVisibility(View.VISIBLE);
                isVisible = true;
            }
            else {
                binding.pickImageFrom.setVisibility(View.GONE   );
                isVisible = false;
            }

        });

        binding.GalleryBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });

        binding.CameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent open_camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(open_camera, 101);
            }
        });
        binding.imageInfo.setOnClickListener(v->{
            binding.Option.setVisibility(View.VISIBLE);
        });
        binding.Option.setOnClickListener(v->{
            deleteConversation();
        });
    }

    private String getReadableDateTime(Date date){
        return  new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date);
    }

    private void addConversation(HashMap<String, Object> conversation){
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .add(conversation)
                .addOnSuccessListener(documentReference ->  conversationId = documentReference.getId());
    }

    private void updateConversation(String  message){
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_CONVERSATION).document(conversationId);
        documentReference.update(
                Constants.KEY_LAST_MESSAGE, message,
                Constants.KEY_TIMESTAMP, new Date()
        );
    }

    private void checkForConversation(){
        if (chatMessages.size() != 0){
            checkForConversationRemotely(preferenceManager.getString(Constants.KEY_USER_ID),
                    receiverUser.id);
            checkForConversationRemotely(
                    receiverUser.id,
                    preferenceManager.getString(Constants.KEY_USER_ID)
            );
        }
    }

    private  void checkForConversationRemotely(String senderId, String receiverId){
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(Constants.KEY_SENDER_ID ,senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener((conversationOnCompleteListener));

    }

    private  final OnCompleteListener<QuerySnapshot> conversationOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size()>0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId = documentSnapshot.getId();
        }
    };

    private  String encodeImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight()*previewWidth/bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] bytes =  byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT);
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK){
                    if (result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.previewImgLayout.setVisibility(View.VISIBLE);
                            binding.pickImageFrom.setVisibility(View.GONE);
                            binding.previewImage.setImageBitmap(bitmap);
                            encodedImage   = encodeImage(bitmap);
                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private void deleteConversation(){
        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverUser.id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        String ConversationId = documentSnapshot.getId();
                        database.collection(Constants.KEY_COLLECTION_CONVERSATION)
                                .document(ConversationId)
                                .delete();
                    }
                });
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,receiverUser.id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            String chatId = queryDocumentSnapshot.getId();
                            database.collection(Constants.KEY_COLLECTION_CHAT)
                                    .document(chatId)
                                    .delete();
                            onBackPressed();
                        }

                    }
                });
        startActivity(new Intent(getApplicationContext(),MainActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }
}