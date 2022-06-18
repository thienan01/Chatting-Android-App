package hcmute.edu.vn.zaloapp.utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private final SharedPreferences sharedPreferences; //store info as key-value

    public PreferenceManager(Context context) {// constructor
        sharedPreferences = context.getSharedPreferences(Constants.KEY_PREFERENCE_NAME,
                Context.MODE_PRIVATE) ;
    }


    public void putBoolean(String key, Boolean value){// store boolean type
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key,value);
        editor.apply();
    }

    public Boolean getBoolean(String key){//get data boolean type
        return sharedPreferences.getBoolean(key,false);
    }

    public void putString(String key, String value){//store data string type
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key,value);
        editor.apply();
    }

    public  String getString(String key){//get data string type
        return  sharedPreferences.getString(key,null);
    }

    public void clear(){ //clear reference
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
