package abhijith.carboncontacts;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;

public class RecentDeletes extends Activity {

    ArrayList<PhoneContact> deletedContacts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_deletes);
       // getActionBar().setDisplayHomeAsUpEnabled(true);


        SharedPreferences sharedpreferences = getSharedPreferences("deleted", Context.MODE_PRIVATE);
        int size = sharedpreferences.getInt("size", 0);
        for(int i=0; i<size ; i++){
            Gson gson = new Gson();
            String json = sharedpreferences.getString("deleted_" + i, "");
            PhoneContact obj = gson.fromJson(json, PhoneContact.class);
            deletedContacts.add(obj);
        }

        for(PhoneContact contact: deletedContacts){
            Log.d("delLOG: ", contact.getContactName() + ": " + contact.getContactNumber());
        }

    }


}
