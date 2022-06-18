package hcmute.edu.vn.zaloapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;

import com.google.firebase.firestore.FirebaseFirestore;

import hcmute.edu.vn.zaloapp.databinding.ActivityEditProfileBinding;
import hcmute.edu.vn.zaloapp.utilities.Constants;
import hcmute.edu.vn.zaloapp.utilities.PreferenceManager;

public class EditProfileActivity extends AppCompatActivity {
    ActivityEditProfileBinding binding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager; //get data stored in preferenceManager
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        loadUserDetails();
        setListener();
    }
    private void loadUserDetails(){
        binding.txtName.setText(preferenceManager.getString(Constants.KEY_NAME));
        binding.phoneNumber.setText(preferenceManager.getString(Constants.KEY_PHONE_NUMBER));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE),Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);// decode image was encoded by base64
        binding.imageProfile.setImageBitmap(bitmap);
    }
    private void setListener(){ //listen button event
        binding.imageBack.setOnClickListener(v-> {
            startActivity(new Intent(getApplicationContext(),MainActivity.class));//start new activity
        });
        binding.btnSave.setOnClickListener(v->{
            updateUserInfo();
            onBackPressed();
        });
    }
    private  void updateUserInfo(){
        database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID)).update(
                Constants.KEY_NAME,binding.txtName.getText().toString()
        );
        database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID)).update(
                Constants.KEY_EMAIL,binding.email.getText().toString()
        );
        database.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID)).update(
                Constants.KEY_ADDRESS,binding.address.getText().toString()
        );
        preferenceManager.putString(Constants.KEY_NAME,binding.txtName.getText().toString() );
        preferenceManager.putString(Constants.KEY_EMAIL,binding.email.getText().toString() );
        preferenceManager.putString(Constants.KEY_ADDRESS,binding.address.getText().toString() );
    }
}