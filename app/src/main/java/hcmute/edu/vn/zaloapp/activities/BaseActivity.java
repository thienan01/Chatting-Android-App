package hcmute.edu.vn.zaloapp.activities;

import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import hcmute.edu.vn.zaloapp.utilities.Constants;
import hcmute.edu.vn.zaloapp.utilities.PreferenceManager;

//Other activity extend from this class to recognize active status
public class BaseActivity extends AppCompatActivity {
    DocumentReference documentReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());

        FirebaseFirestore database = FirebaseFirestore.getInstance();

        documentReference = database.collection(Constants.KEY_COLLECTION_USERS)
                .document(preferenceManager.getString(Constants.KEY_USER_ID));
    }

    @Override
    protected void onPause() {
        super.onPause();
        documentReference.update(Constants.KEY_AVAILABILITY,0); //value equal 0: Not active
    }

    @Override
    protected void onResume() {
        super.onResume();
        documentReference.update(Constants.KEY_AVAILABILITY,1);//value equal 1:  Active
    }
}
