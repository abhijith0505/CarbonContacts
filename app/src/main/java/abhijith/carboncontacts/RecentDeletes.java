package abhijith.carboncontacts;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

public class RecentDeletes extends AppCompatActivity {

    ArrayList<PhoneContact> deletedContacts = new ArrayList<>();
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_deletes);

        listView = (ListView) findViewById(R.id.list);

        ArrayList<String> deleted = new ArrayList<>();

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
            deleted.add(contact.getContactName() + "(" + contact.getContactType() + "): " + contact.getContactNumber());
        }

        ArrayAdapter arrayAdapter = new ArrayAdapter(RecentDeletes.this, R.layout.row, deleted);
        listView.setAdapter(arrayAdapter);


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recent_deleted, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_delete_recent:
                Toast.makeText(RecentDeletes.this, "History cleared", Toast.LENGTH_SHORT).show();
                deleteSharedPref();
                Intent intent = new Intent(RecentDeletes.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteSharedPref() {
        SharedPreferences sharedpreferences = getSharedPreferences("deleted", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.clear();
        editor.commit();
    }

}
