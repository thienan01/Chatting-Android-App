package hcmute.edu.vn.zaloapp.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import hcmute.edu.vn.zaloapp.R;
import hcmute.edu.vn.zaloapp.databinding.FragmentProfileBinding;
import hcmute.edu.vn.zaloapp.utilities.Constants;
import hcmute.edu.vn.zaloapp.utilities.PreferenceManager;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding; //get View through binding
    private PreferenceManager preferenceManager; //get data stored in preferenceManager
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(getLayoutInflater());
        return binding.getRoot();//init view
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preferenceManager = new PreferenceManager(getActivity().getApplicationContext());
        loadUserDetails();
        setListener();
    }
    private void loadUserDetails(){
        binding.txtName.setText(preferenceManager.getString(Constants.KEY_NAME));
        binding.phoneNumber.setText(preferenceManager.getString(Constants.KEY_PHONE_NUMBER));
        binding.txtAddress.setText(preferenceManager.getString(Constants.KEY_ADDRESS));
        binding.txtEmail.setText(preferenceManager.getString(Constants.KEY_EMAIL));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE),Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);// decode image was encoded by base64
        binding.imageProfile.setImageBitmap(bitmap);
    }
    private void setListener(){ //listen button event
        binding.txtLogout.setOnClickListener(v-> signOut());
        binding.imageBack.setOnClickListener(v-> {
            startActivity(new Intent(getActivity().getApplicationContext(),MainActivity.class));//start new activity
        });
        binding.btnSetting.setOnClickListener(v->{
            startActivity(new Intent(getActivity().getApplicationContext(),EditProfileActivity.class));
        });
    }

    private void showToast(String message){ // show message
        Toast.makeText(getActivity().getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

    private void signOut(){ //Log out account
        showToast("Signing out...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS)
                        .document(preferenceManager.getString(Constants.KEY_USER_ID));
        HashMap<String,Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());//delete token when user log out
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getActivity().getApplicationContext(), SignInActivity.class));
                    getActivity().finish();
                })
                .addOnFailureListener(e -> showToast("Unable to logout"));
    }
}