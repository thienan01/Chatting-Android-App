package hcmute.edu.vn.zaloapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.PrivilegedAction;
import java.util.HashMap;

import hcmute.edu.vn.zaloapp.R;
import hcmute.edu.vn.zaloapp.databinding.ActivitySignInBinding;
import hcmute.edu.vn.zaloapp.utilities.Constants;
import hcmute.edu.vn.zaloapp.utilities.PreferenceManager;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding; //get view through binding
    private PreferenceManager preferenceManager; //get data stored in preferenceManager
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGN_IN)){
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);//start new intent
            finish();
        }
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }
    private void setListeners(){//listen button event
        binding.txtCreateAccount.setOnClickListener(v->
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));

        binding.btnSignIn.setOnClickListener(v ->{
            if (isValidSignInDetail()){
                signIn();
            }
        });
    }

    private void signIn(){//Check Phone number and password to sign in
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_PHONE_NUMBER, binding.inputPhoneNum.getText().toString())//select by condition
                .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null
                    && task.getResult().getDocuments().size()>0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGN_IN,true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                        preferenceManager.putString(Constants.KEY_PHONE_NUMBER, documentSnapshot.getString(Constants.KEY_PHONE_NUMBER));
                        preferenceManager.putString(Constants.KEY_EMAIL, documentSnapshot.getString(Constants.KEY_EMAIL));
                        preferenceManager.putString(Constants.KEY_ADDRESS, documentSnapshot.getString(Constants.KEY_ADDRESS));
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                    else{
                        loading(false);
                        showToast("Wrong Phone Number Or Password");
                    }
                }
                );
    }

    private void loading(Boolean isLoading){ // Enable and disable progress bar
        if(isLoading){
            binding.btnSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else{
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.btnSignIn.setVisibility(View.VISIBLE);
        }
    }

    private void showToast(String message){ // show message
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

    private  Boolean isValidSignInDetail(){ // check input
        if (binding.inputPhoneNum.getText().toString().trim().isEmpty()){
            showToast("Enter Email");
            return false;

        }else if(binding.inputPassword.getText().toString().trim().isEmpty()){
            showToast("Enter Password");
            return false;
        }else {
            return true;
        }
    }







//    private void addDataToFirestore(){
//        FirebaseFirestore database = FirebaseFirestore.getInstance();
//        HashMap<String, Object> data = new HashMap<>();
//        data.put("phone_num","0383865402");
//        data.put("first_name","hoa");
//        database.collection("user")
//            .add(data)
//                .addOnSuccessListener(documentReference -> {
//                    Toast.makeText(getApplicationContext(), "Data Inserted", Toast.LENGTH_SHORT).show();
//                }).addOnFailureListener(exception ->{
//                    Toast.makeText(getApplicationContext(), exception.getMessage(), Toast.LENGTH_SHORT).show();
//        });

//    }
}