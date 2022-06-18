package hcmute.edu.vn.zaloapp.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.zaloapp.R;
import hcmute.edu.vn.zaloapp.adapters.UsersAdapter;
import hcmute.edu.vn.zaloapp.databinding.FragmentContactBinding;
import hcmute.edu.vn.zaloapp.databinding.FragmentMessageBinding;
import hcmute.edu.vn.zaloapp.listeners.UserListener;
import hcmute.edu.vn.zaloapp.models.User;
import hcmute.edu.vn.zaloapp.utilities.Constants;
import hcmute.edu.vn.zaloapp.utilities.PreferenceManager;

public class ContactFragment extends Fragment implements UserListener {
    private FragmentContactBinding binding; //get view through binding
    private PreferenceManager preferenceManager; //get data stored in preferenceManager
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentContactBinding.inflate(getLayoutInflater());// init view
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        preferenceManager = new PreferenceManager(getActivity().getApplicationContext());
        getUsers();
        setListener();
    }

    private void setListener(){// listen button event
        binding.imageBack.setOnClickListener(v-> {
            startActivity(new Intent(getActivity().getApplicationContext(),MainActivity.class));
        });
    }
    private void getUsers(){//get all user and show on Contact view
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();// get instance of database
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID); //get id of user
                    if (task.isSuccessful() && task.getResult() != null){
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if (currentUserId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            User user   = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.phoneNum = queryDocumentSnapshot.getString(Constants.KEY_PHONE_NUMBER);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        if (users.size() > 0){
                            UsersAdapter usersAdapter = new UsersAdapter(users, this);
                            binding.usersRecyclerView.setAdapter(usersAdapter);
                            binding.usersRecyclerView.setVisibility(View.VISIBLE);
                        }
                        else{
                            showErrorMessage();
                        }
                    }
                    else {
                        showErrorMessage();
                    }
                });
    }

    private void showErrorMessage(){ // show message when get error
        binding.textErrorMessage.setText(String.format("%s","No user available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }
    private void loading(boolean isLoading){ //enable and disable progressbar while access database
        if (isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else
            binding.progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onUserClicked(User user) { //Call back function to get data of user when user click on item of recycle view
        Intent intent = new Intent(getActivity().getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        getActivity().finish();
    }
}