package abhijith.carboncontacts;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;


public class MainActivity extends AppCompatActivity {

    //Cleaned code
    ListView listView;
    ArrayList<PhoneContact> phoneContacts;
    ArrayList<PhoneContact> contactDuplicates;

    ArrayList<PhoneContact> deletedContacts = new ArrayList<>();

    FloatingActionButton fab;


    ArrayList<String> allContacts = new ArrayList<>();
    ArrayList<String> onlyDuplicates = new ArrayList<>();
    ArrayList<String> whatsAppIDs = new ArrayList<>();
    ArrayAdapter<String> adapter = null;


    int k = 0, p = 0;
    boolean Allcontacts;
    Switch mySwitch;

    int flag=0;
    ProgressDialog mProgressDialog;

    int itemCount;
    SparseBooleanArray checkedItem;
    SparseBooleanArray checkedItemPositions;
    int d, d1;

    AlertDialog alertDialog;
    TextView valueTV ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();

        MobileAds.initialize(getApplicationContext(), getString(R.string.admob_myAppID));
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        phoneContacts = new ArrayList<>(); //Contains all contacts
        contactDuplicates = new ArrayList<>();

        Load loading = new Load();
        loading.execute();

        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    Allcontacts = true;
                    if(allContacts.isEmpty()){
                        fab.hide();
                    }
                    else    fab.show();

                    adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.row, allContacts);
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    listView.invalidateViews();
                    valueTV.setVisibility(View.INVISIBLE);

                } else {
                    Allcontacts = false;

                    if (onlyDuplicates.isEmpty()) {
                        fab.hide();
                        valueTV.setVisibility(View.VISIBLE);
                        adapter = new ArrayAdapter<>(MainActivity.this, R.layout.row, onlyDuplicates);
                        listView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        listView.invalidateViews();
                    } else {
                        fab.show();
                        valueTV.setVisibility(View.INVISIBLE);
                        adapter = new ArrayAdapter<>(MainActivity.this, R.layout.row, onlyDuplicates);
                        listView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        listView.invalidateViews();
                    }
                }
            }
        });

        fab.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDupes();
            }
        });
    }

    private void getWhatsAppContactIDs() {
        ContentResolver cr = MainActivity.this.getContentResolver();

        //RowContacts for filter Account Types
        Cursor contactCursor = cr.query(
                ContactsContract.RawContacts.CONTENT_URI,
                new String[]{ContactsContract.RawContacts._ID,
                        ContactsContract.RawContacts.CONTACT_ID},
                ContactsContract.RawContacts.ACCOUNT_TYPE + "= ?",
                new String[]{"com.whatsapp"},
                null);

        //ArrayList for Store Whatsapp Contact
        //ArrayList<String> myWhatsappContacts = new ArrayList<>();

        if (contactCursor != null) {
            if (contactCursor.getCount() > 0) {
                if (contactCursor.moveToFirst()) {
                    do {
                        //whatsappContactId for get Number,Name,Id ect... from  ContactsContract.CommonDataKinds.Phone
                        String whatsappContactId = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID));

                        if (whatsappContactId != null) {
                            //Get Data from ContactsContract.CommonDataKinds.Phone of Specific CONTACT_ID
                            Cursor whatsAppContactCursor = cr.query(
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    new String[]{ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                    new String[]{whatsappContactId}, null);

                            if (whatsAppContactCursor != null) {
                                whatsAppContactCursor.moveToFirst();
                                String id = whatsAppContactCursor.getString(whatsAppContactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                                String name = whatsAppContactCursor.getString(whatsAppContactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                                String number = whatsAppContactCursor.getString(whatsAppContactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                                String phoneNumberID;
                                Log.i("whatsapp", " WhatsApp contact id  :  " + id);
                                Log.i("whatsapp", " WhatsApp contact name :  " + name);
                                Log.i("whatsapp", " WhatsApp contact number :  " + number);
                                //if (Integer.parseInt(whatsAppContactCursor.getString(whatsAppContactCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
                                {
                                    //the below cursor will give you details for multiple contacts
                                    Cursor pCursor = MainActivity.this.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ? and " +ContactsContract.RawContacts.ACCOUNT_TYPE + "= ?" ,
                                            new String[]{id, "com.whatsapp"}, null);
                                    // continue till this cursor reaches to all phone numbers which are associated with a contact in the contact list
                                    while (pCursor.moveToNext())
                                    {
                                        //String isStarred = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED));
                                        //String phoneNo 	= pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                        //you will get all phone numbers according to it's type as below switch case.
                                        //Logs.e will print the phone number along with the name in DDMS. you can use these details where ever you want.
                                        phoneNumberID = pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
                                        whatsAppIDs.add(phoneNumberID);
                                        Log.i("whatsapp", " WhatsApp contact numberID :  " + phoneNumberID);
                                    }
                                    pCursor.close();
                                }
                                whatsAppContactCursor.close();
                                //Add Number to ArrayList
                                //myWhatsappContacts.add(number);

                            }
                        }
                    } while (contactCursor.moveToNext());
                    contactCursor.close();
                }
            }
        }
    }


    //Cleaned
    //View initialiser
    private void initializeViews() {
        listView = (ListView) findViewById(R.id.list);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        mySwitch = (Switch) findViewById(R.id.switch1);
        mySwitch.setChecked(false);
    }

    //Cleaned
    //Retrieves all contacts
    public void readPhoneContacts(Context context)
    {
        Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        Integer contactsCount = cursor.getCount(); //get how many contacts you have in your contacts list
        if (contactsCount > 0)
        {
            while(cursor.moveToNext())
            {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
                {
                    //the below cursor will give you details for multiple contacts
                    Cursor pCursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                            new String[]{id}, null);
                    // continue till this cursor reaches to all phone numbers which are associated with a contact in the contact list
                    while (pCursor.moveToNext())
                    {
                        int phoneType = pCursor.getInt(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                        //String isStarred = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED));
                        String phoneNo 	= pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        //you will get all phone numbers according to it's type as below switch case.
                        //Logs.e will print the phone number along with the name in DDMS. you can use these details where ever you want.
                        String phoneNumberID = pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
                        String type = null;

                        switch (phoneType)
                        {
                            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                                type = "M";
                                break;
                            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                                type = "H";
                                break;
                            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                                type = "W";
                                break;
                            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE:
                                type = "WM";
                                break;
                            case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
                                type = "O";
                                break;
                            default:
                                break;
                        }
                        if(!whatsAppIDs.contains(phoneNumberID))
                            phoneContacts.add(new PhoneContact(phoneNo, contactName, type, id, phoneNumberID));
                        Log.i("Contact details:",phoneNo + ": " + contactName + ": " + type + ": " + id + ": " + phoneNumberID);
                    }
                    pCursor.close();
                }
            }
            cursor.close();
        }
    }

    private void refreshLists() {
        phoneContacts.clear();
        contactDuplicates.clear();
        allContacts.clear();
        onlyDuplicates.clear();
        readPhoneContacts(MainActivity.this);
        if (phoneContacts.size() > 1)
            contactDuplicates = findDuplicates(phoneContacts); //Contains all duplicated entries

        if(contactDuplicates.size() == 0){
            fab.hide();
        } else {
            fab.show();

        }
        //populates arraylists for simple listview adapter

        for (PhoneContact contact : phoneContacts) {
            allContacts.add(contact.getContactName() + "(" + contact.getContactType() + "): " + contact.getContactNumber());
        }
        for (PhoneContact contact : contactDuplicates) {
            onlyDuplicates.add(contact.getContactName() + "(" + contact.getContactType() + "): " + contact.getContactNumber());
        }
    }

    //Cleaned
    //Returns all occurences of the duplicate entries

    public ArrayList<PhoneContact> findDuplicates(ArrayList<PhoneContact> listContainingDuplicates) {
        ArrayList<PhoneContact> duplicatesOrganised = new ArrayList();
        ArrayList<PhoneContact> setToReturn = new ArrayList();

        Collections.sort(listContainingDuplicates);
        int i, size = listContainingDuplicates.size();

        //Orders all the duplicates together along with the unique(non-duplicate)
        for (i = 0; i < size; i++) {
            if(i+1 == size){
                duplicatesOrganised.add(listContainingDuplicates.get(i));
                Log.i("DuplicateOrdered: ",listContainingDuplicates.get(i).getContactNumber()+" "+listContainingDuplicates.get(i).getContactName());
            }else if (listContainingDuplicates.get(i).getContactNumber().equals(listContainingDuplicates.get(i+1).getContactNumber())) {
                duplicatesOrganised.add(listContainingDuplicates.get(i));
                Log.i("DuplicateOrdered: ",listContainingDuplicates.get(i).getContactNumber()+" "+listContainingDuplicates.get(i).getContactName());
            }else{
                duplicatesOrganised.add(listContainingDuplicates.get(i));
                Log.i("DuplicateOrdered: ",listContainingDuplicates.get(i).getContactNumber()+" "+listContainingDuplicates.get(i).getContactName());
            }
        }

        int start = 0;
        if(!duplicatesOrganised.isEmpty() &&
                duplicatesOrganised.size() > 1 &&
                !duplicatesOrganised.get(0).getContactNumber().equals(duplicatesOrganised.get(1).getContactNumber()) ){
            start = 1;
        }

        //setToReturn contains only ordered duplicates
        for (i = start; i < size; i++) {
            if(i+1 == size && duplicatesOrganised.get(i).getContactNumber().equals(duplicatesOrganised.get(i-1).getContactNumber())){
                setToReturn.add(duplicatesOrganised.get(i));
                Log.i("Duplicate: ", duplicatesOrganised.get(i).getContactNumber() + " " + duplicatesOrganised.get(i).getContactName());
            } else if(i+1 == size && !duplicatesOrganised.get(i).getContactNumber().equals(duplicatesOrganised.get(i-1).getContactNumber())){
                continue;
            } else if (duplicatesOrganised.get(i).getContactNumber().equals(duplicatesOrganised.get(i+1).getContactNumber())) {
                setToReturn.add(duplicatesOrganised.get(i));
                Log.i("Duplicate: ", duplicatesOrganised.get(i).getContactNumber() + " " + duplicatesOrganised.get(i).getContactName());
            } else if(!duplicatesOrganised.get(i).getContactNumber().equals(duplicatesOrganised.get(i+1).getContactNumber())){
                if (duplicatesOrganised.get(i).getContactNumber().equals(duplicatesOrganised.get(i-1).getContactNumber())) {
                    setToReturn.add(duplicatesOrganised.get(i));
                    Log.i("Duplicate: ", duplicatesOrganised.get(i).getContactNumber() + " " + duplicatesOrganised.get(i).getContactName());
                }
            }
        }
        return setToReturn;
    }

    //________________________________________________________________________________________________________________________________



    public void deleteDupes() {

        mProgressDialog.setMessage("Deleting...");
        mProgressDialog.setIndeterminate(false);
        mProgressDialog.setMax(100);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        checkedItem = listView.getCheckedItemPositions();
        checkedItemPositions = listView.getCheckedItemPositions();
        itemCount = listView.getCount();
        checkedItem = listView.getCheckedItemPositions();
        LongOperation lp = new LongOperation();
        if (checkedItem.size() == 0 && k == 1) {
            Toast.makeText(MainActivity.this, "Select Contacts To Delete!", Toast.LENGTH_SHORT).show();
        } else {
            lp.execute("");
            mProgressDialog.show();
        }
        if(flag==1)
            mProgressDialog.dismiss();
    }


    public void emptyRemover(String s) {
        ContentResolver contentResolver = this.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()&&!cursor.isClosed()) {
            if (cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts._ID)).equalsIgnoreCase(s)) {
                if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) == 0) {
                    String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                    Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                    contentResolver.delete(uri, null, null);
                    cursor.close();
                    return;
                }
            }
        }
        cursor.close();
    }


    public void updateContact(PhoneContact contact, String contactId, String phoneNumberID) {
        ContentResolver contentResolver = this.getContentResolver();
        Cursor cur = contentResolver.query(ContactsContract.RawContacts.CONTENT_URI,
                new String[]{ContactsContract.RawContacts._ID},
                ContactsContract.RawContacts.CONTACT_ID + "=?",
                new String[] {contact.getContactID()}, null);

        int rowId=0;

        if(cur.moveToFirst()){
            rowId = cur.getInt(cur.getColumnIndex(ContactsContract.RawContacts._ID));
        }
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        String selectPhone = ContactsContract.RawContacts.Data.RAW_CONTACT_ID + " = ? AND " +
                ContactsContract.RawContacts.Data.MIMETYPE + " = ? AND " +
                ContactsContract.CommonDataKinds.Phone._ID + " = ?";

        String[] phoneArgs = new String[] { Integer.toString(rowId),
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                contact.getContactNumberID()};

        ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                .withSelection(selectPhone, phoneArgs).build());

        //Deletes entire contact
        /*ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        String[] phoneArgs = new String[]{contactId};
        ops.add(ContentProviderOperation.newDelete(ContactsContract.RawContacts.CONTENT_URI)
                .withSelection(ContactsContract.RawContacts._ID + "=?", phoneArgs)
                .build());*/
        Log.d("Deleted: ", contact.getContactName() + "(" + contact.getContactType() + "): " + contact.getContactNumber());
        deletedContacts.add(contact);

        try {
            getApplicationContext().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
        cur.close();
    }

    class Load extends AsyncTask<String, String, String> {
        ProgressDialog progDailog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progDailog = new ProgressDialog(MainActivity.this);
            progDailog.setMessage("Finding duplicates...");
            progDailog.setIndeterminate(false);
            progDailog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progDailog.setCancelable(false);
            progDailog.show();
        }
        @Override
        protected String doInBackground(String... aurl) {
            //loads WhatsApp contact number IDs
            getWhatsAppContactIDs();
            //Reads all the contacts and stores them in phoneContacts Arraylist
            readPhoneContacts(MainActivity.this);

            if (phoneContacts.size() > 1)
                contactDuplicates = findDuplicates(phoneContacts); //Contains all duplicated entries


            //populates arraylists for simple listview adapter

            for (PhoneContact contact : phoneContacts) {
                allContacts.add(contact.getContactName() + "(" + contact.getContactType() + "): " + contact.getContactNumber());
            }
            for (PhoneContact contact : contactDuplicates) {
                onlyDuplicates.add(contact.getContactName() + "(" + contact.getContactType() + "): " + contact.getContactNumber());
            }
            return null;
        }
        @Override
        protected void onPostExecute(String unused) {
            super.onPostExecute(unused);
            progDailog.dismiss();




            if(contactDuplicates.size() == 0){
                fab.hide();
            } else {
                fab.show();
            }

            k = 0;
            p = 0;
            Allcontacts = false;
            mProgressDialog = new ProgressDialog(MainActivity.this);
            alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            adapter = new ArrayAdapter<>(MainActivity.this, R.layout.row, onlyDuplicates);
            listView.setAdapter(adapter);
            listView.setItemsCanFocus(false);
            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            adapter.notifyDataSetChanged();
            listView.invalidateViews();
            RelativeLayout layout;
            valueTV =  new TextView(MainActivity.this);
            if (contactDuplicates.isEmpty() && Allcontacts == false) {
                layout = (RelativeLayout) findViewById(R.id.rr);
                valueTV.setText("No Duplicates!");
                valueTV.setId('5');
                valueTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 60);

                valueTV.setLayoutParams(new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.FILL_PARENT,
                        RelativeLayout.LayoutParams.FILL_PARENT));
                valueTV.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                layout.addView(valueTV);
                fab.hide();
            } else if (!contactDuplicates.isEmpty() && Allcontacts == false) {
                k = 1;
                fab.show();
                alertDialog.setTitle("Duplicates Found!");
                alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        }
    }


    private class LongOperation extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {

            d = d1 = 0;
            if (Allcontacts == true) {
                for (int i = 0, t = 1; i <itemCount; t++, i++) {
                    if (checkedItemPositions.get(i)) {
                        String id = phoneContacts.get(i).getContactID();
                        updateContact(phoneContacts.get(i),id, phoneContacts.get(i).getContactNumberID());
                        emptyRemover(id);
                        publishProgress((int) (t * 100 / itemCount));
                    }
                    d1 = 1;
                }
            } else {
                for (int i = 0, t = 1; i <itemCount; t++, i++) {
                    if (checkedItemPositions.get(i)) {
                        String id = contactDuplicates.get(i).getContactID();
                        updateContact(contactDuplicates.get(i), id, contactDuplicates.get(i).getContactNumberID());
                        emptyRemover(id);
                        publishProgress((int) (t * 100 / itemCount));
                    }
                    d = 1;
                }
            }


            return null;
        }




        @Override
        protected void onPostExecute(String result) {
            flag=1;
            if (Allcontacts == false) {
                for (int i = listView.getCount() - 1; i >= 0; i--) {
                    if (checkedItemPositions.get(i)) {
                        if (checkedItemPositions.get(i)) {
                            adapter.remove(onlyDuplicates.get(i));
                        }
                    }
                }
                checkedItemPositions.clear();

                if (k == 0) {

                    Toast.makeText(MainActivity.this, "No Duplicates", Toast.LENGTH_SHORT).show();
                }


                if (d == 1)
                    Toast.makeText(MainActivity.this, "Duplicates Deleted!", Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
                listView.invalidateViews();
            } else {
                for (int i = listView.getCount() - 1; i >= 0; i--) {
                    if (checkedItemPositions.get(i)) {
                        if (checkedItem.get(i)) {
                            adapter.remove(allContacts.get(i));
                        }
                    }
                }


                if (checkedItem.size() == 0) {
                    Toast.makeText(MainActivity.this, "Select Contacts To Delete!", Toast.LENGTH_SHORT).show();
                }
                checkedItemPositions.clear();

                if (d1 == 1)
                    Toast.makeText(MainActivity.this, "Contact(s) Deleted!", Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
                listView.invalidateViews();
            }
            refreshLists();
            mProgressDialog.dismiss();
            storeDeletedContacts();
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            mProgressDialog.setProgress(values[0]);
        }
    }



    private long mBackPressed;
    @Override
    public void onBackPressed() {

        if (mBackPressed + 2000 > System.currentTimeMillis()) {
            adapter.notifyDataSetChanged();
            listView.invalidateViews();
            super.onBackPressed();
            return;
        } else {
            Toast.makeText(getBaseContext(), "Tap once more to exit", Toast.LENGTH_SHORT).show();
        }

        mBackPressed = System.currentTimeMillis();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_about:
                startActivity(new Intent(MainActivity.this, About.class));
                return true;
            case R.id.action_recent:
                Intent intent = new Intent(MainActivity.this, RecentDeletes.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void storeDeletedContacts() {
        SharedPreferences sharedpreferences = getSharedPreferences("deleted", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        int size = sharedpreferences.getInt("size", 0);
        int i = size;
        for(int j = 0; j<deletedContacts.size(); ++j){
            Gson gson = new Gson();
            String json = gson.toJson(deletedContacts.get(j));
            editor.putString("deleted_" + i, json);
            editor.commit();
            i++;
        }
        deletedContacts.clear();
        editor.putInt("size",i);
        editor.commit();

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if ( mProgressDialog!=null && mProgressDialog.isShowing() ){
            mProgressDialog.cancel();
            mProgressDialog.dismiss();
        }
    }


}
