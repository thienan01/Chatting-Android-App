package hcmute.edu.vn.zaloapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hcmute.edu.vn.zaloapp.R;
import hcmute.edu.vn.zaloapp.databinding.ItemContainerReceivedMessageBinding;
import hcmute.edu.vn.zaloapp.databinding.ItemContainerSentMessageBinding;
import hcmute.edu.vn.zaloapp.databinding.ItemContainerUserBinding;
import hcmute.edu.vn.zaloapp.models.ChatMessage;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final List<ChatMessage> chatMessages; // list of message
    private  Bitmap receiverProfileImage; //profile image of receiver
    private final  String senderID; //store id of sender

    public  static  final  int VIEW_TYPE_SENT = 1;
    public  static  final  int VIEW_TYPE_RECEIVED = 2;

    public void setReceiverProfileImage(Bitmap bitmap){
        receiverProfileImage = bitmap;
    }

    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap receiverProfileImage, String senderID) {
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderID = senderID;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        if (viewType == VIEW_TYPE_SENT){
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }else {
            return  new ReceivedMessageViewHolder(
                    ItemContainerReceivedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT){
            ((SentMessageViewHolder)holder).setData(chatMessages.get(position));
        }else {
            ((ReceivedMessageViewHolder)holder).setData(chatMessages.get(position),receiverProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).senderID.equals(senderID)){
            return  VIEW_TYPE_SENT;
        }else{
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder{

        private final ItemContainerSentMessageBinding binding;

        SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding){
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setData(ChatMessage chatMessage){//set data for view of item

            if (chatMessage.message != "" && chatMessage.image == ""){
                binding.textMessage.setText(chatMessage.message);
                binding.imageMessage.setVisibility(View.GONE);
            }
            else if (chatMessage.image !="" && chatMessage.message == ""){
                binding.imageMessage.setImageBitmap(getImageMessage(chatMessage.image));
                binding.imageMessage.setVisibility(View.VISIBLE);
                binding.textMessage.setVisibility(View.GONE);
            }
            else {
                binding.imageMessage.setImageResource(R.drawable.logo);
                binding.textMessage.setText("");
            }
            binding.textDateTime.setText(chatMessage.dateTime);
        }
    }
    static class ReceivedMessageViewHolder extends  RecyclerView.ViewHolder{
        private final ItemContainerReceivedMessageBinding binding;

        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding){
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }
        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage){//set data for view of item

            if (chatMessage.message != "" && chatMessage.image == ""){
                binding.textMessage.setText(chatMessage.message);
                binding.imageMessage.setVisibility(View.GONE);
            }
            else if (chatMessage.image !="" && chatMessage.message == ""){
                binding.imageMessage.setImageBitmap(getImageMessage(chatMessage.image));
                binding.imageMessage.setVisibility(View.VISIBLE);
            }
            else {
                binding.imageMessage.setImageResource(R.drawable.logo);
                binding.textMessage.setText("");
            }

            binding.textDateTime.setText(chatMessage.dateTime);

            if (receiverProfileImage != null){
                binding.imageProfile.setImageBitmap(receiverProfileImage);
            }

        }
    }
    private static Bitmap getImageMessage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
}
