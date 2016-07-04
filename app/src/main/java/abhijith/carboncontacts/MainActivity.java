package abhijith.carboncontacts;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    ListView listView;
    ArrayList<String> listItems = new ArrayList<String>();
    ArrayList<String> PhonelistItems = new ArrayList<String>();
    ArrayList<String> dupesRemoved = listItems;
    ArrayList<String> allContacts = new ArrayList<String>();
    ArrayList<String> newList = new ArrayList<String>();
    ArrayAdapter<String> adapter = null;
    ImageButton button;
    int k = 0, p = 0;
    boolean Allcontacts;
    Switch mySwitch;
    RelativeLayout layout;
    TextView valueTV;
    int flag=0;
    ProgressDialog mProgressDialog;
    private long mBackPressed;
    int itemCount;

    ArrayList<PhoneContact> phoneContacts;
    ArrayList<PhoneContact> contactDuplicates;
    //PhoneContact[] phoneContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android.support.v7.app.ActionBar bar = MainActivity.this.getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#556e2d")));
        setContentView(R.layout.activity_main);

        phoneContacts = new ArrayList<>();
        contactDuplicates = new ArrayList<>();

        initializeViews();

        readPhoneContacts(MainActivity.this);

        k = 0;
        p = 0;
        Allcontacts = false;
        mProgressDialog = new ProgressDialog(this);



       /* String order = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC";
        Cursor curLog = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, order);


        String listString = null;
        if (curLog != null) {

            while (curLog.moveToNext()) {
                String str = curLog.getString(curLog.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                String id = curLog.getString(curLog.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                String name = curLog.getString(curLog.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));

                listItems.add(str.trim().replaceAll(" ", "") + "                    :" + name + "           " + id);

            }
        }

        curLog.close();*/

        contactDuplicates = findDuplicates(phoneContacts);

        for (PhoneContact contact : phoneContacts) {
            allContacts.add(contact.getContactNumber() + "(" + contact.getContactType() + "): " + contact.getContactName());
        }
        for (PhoneContact contact : contactDuplicates) {
            newList.add(contact.getContactNumber() + "(" + contact.getContactType() + "): " + contact.getContactName());

        }
        
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.row, newList);
        listView.setAdapter(adapter);
        listView.setItemsCanFocus(false);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        adapter.notifyDataSetChanged();
        listView.invalidateViews();
        if (contactDuplicates.isEmpty() && Allcontacts == false) {
            //listView.setVisibility(View.INVISIBLE);
            layout = (RelativeLayout) findViewById(R.id.rr);
            // valueTV = new TextView(this);
            valueTV.setText("No Duplicates!");
            valueTV.setId('5');
            valueTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 60);

            valueTV.setLayoutParams(new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.FILL_PARENT,
                    RelativeLayout.LayoutParams.FILL_PARENT));
            valueTV.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            layout.addView(valueTV);
        } else if (!contactDuplicates.isEmpty() && Allcontacts == false) {
            k = 1;
            alertDialog.setTitle("Duplicates Found!");
            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.show();
        }


        mySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (isChecked) {
                    Allcontacts = true;
                    adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.row, allContacts);
                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    listView.invalidateViews();
                    valueTV.setVisibility(View.INVISIBLE);

                } else {
                    Allcontacts = false;
                    if (dupesRemoved.isEmpty()) {
                        valueTV.setVisibility(View.VISIBLE);
                        adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.row, newList);
                        listView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        listView.invalidateViews();
                    } else {
                        valueTV.setVisibility(View.INVISIBLE);
                        adapter = new ArrayAdapter<String>(MainActivity.this, R.layout.row, newList);
                        listView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        listView.invalidateViews();
                    }
                }
            }
        });

    }

    private void initializeViews() {
        listView = (ListView) findViewById(R.id.list);
        button = (ImageButton) findViewById(R.id.button);
        mySwitch = (Switch) findViewById(R.id.switch1);
        mySwitch.setChecked(false);
        valueTV = new TextView(this);
    }


    public void readPhoneContacts(Context context) //This Context parameter is nothing but your Activity class's Context
    {
        Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        Integer contactsCount = cursor.getCount(); // get how many contacts you have in your contacts list



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
                        int phoneType 		= pCursor.getInt(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                        //String isStarred 		= pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED));
                        String phoneNo 	= pCursor.getString(pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        //you will get all phone numbers according to it's type as below switch case.
                        //Logs.e will print the phone number along with the name in DDMS. you can use these details where ever you want.
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

                        phoneContacts.add(new PhoneContact(phoneNo, contactName, type));
                        Log.i("Contact details:",phoneNo + ": " + contactName + ": " + type);

                    }
                    pCursor.close();
                }
            }
            cursor.close();
        }
    }

    SparseBooleanArray checkedItem;
    SparseBooleanArray checkedItemPositions;
    int d, d1;

    public void deleteDupes(View view) {

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


    public void emptyRemover(String s, String name) {
        ContentResolver contentResolver = this.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()&&!cursor.isClosed()) {
            if (cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)).equalsIgnoreCase(name)){
                if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))==0) {
                    String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                    Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                    contentResolver.delete(uri, null, null);
                    cursor.close();
                    return;
                }
            }
        }
    }


    public void updateContact(String contactId, String type) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
         String selectPhone = ContactsContract.Data.CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + "='" +
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "'" + " AND " + ContactsContract.CommonDataKinds.Phone.TYPE + "=?";
        String[] phoneArgs = new String[]{contactId, type};
        //ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI).withSelection(selectPhone, phoneArgs).build());
        ops.add(ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                .withSelection(ContactsContract.Contacts._ID + "=? and " + ContactsContract.RawContacts.Data.MIMETYPE + "=?", new String[]{String.valueOf(contactId), ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE})
                .build());
        try {
            getApplicationContext().getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<PhoneContact> findDuplicates(ArrayList<PhoneContact> listContainingDuplicates) {
        ArrayList<PhoneContact> duplicatesOrganised = new ArrayList();
        ArrayList<PhoneContact> setToReturn = new ArrayList();

        Collections.sort(listContainingDuplicates);
        int i;
        int size = listContainingDuplicates.size();


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
        if(!duplicatesOrganised.get(0).getContactNumber().equals(duplicatesOrganised.get(1).getContactNumber())){
            start = 1;
        }

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


    private String[] getContactInfo(String number) {
        String[] contactInfo = new String[3];

        ContentResolver context = getContentResolver();
        Uri lookupUri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number));

        String[] mPhoneNumberProjection = {ContactsContract.Contacts._ID, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.PhoneLookup.TYPE};

        Cursor cur = context.query(lookupUri, mPhoneNumberProjection, null, null, null);

        if (cur.moveToFirst()) {
            contactInfo[0] = cur.getString(0);
            contactInfo[1] = cur.getString(2);
            contactInfo[2] = cur.getString(1);
            if (contactInfo[2] == null || contactInfo[2].isEmpty()) {
                contactInfo[2] = "";
            }
            cur.close();
            return contactInfo;
        }


        return contactInfo;


    }

    private class LongOperation extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... params) {

            d = d1 = 0;
            if (Allcontacts == true) {
                for (int i = 0, t = 1; i <itemCount; t++, i++) {
                    if (checkedItemPositions.get(i)) {
                        String[] info = new String[3];
                        String s = listItems.get(i).substring(0, 14);
                        String id1 = listItems.get(i);
                        String id = id1.substring(id1.length() - 10, id1.length()).trim().replaceAll(" ", "");
                        info = getContactInfo(s);
                        updateContact(id, info[1]);
                        emptyRemover(id,info[2]);
                        publishProgress((int) (t * 100 / itemCount));
                    }

                    d1 = 1;
                }
            } else {
                for (int i = 0, t = 1; i <itemCount; t++, i++) {
                    if (checkedItemPositions.get(i)) {
                        String[] info = new String[3];
                        String s = dupesRemoved.get(i).substring(0, 14);
                        String id1 = dupesRemoved.get(i);
                        String id = id1.substring(id1.length() - 10, id1.length()).trim().replaceAll(" ", "");
                        info = getContactInfo(s);
                        updateContact(id, info[1]);
                        emptyRemover(id,info[2]);
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
                            adapter.remove(newList.get(i));
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
            mProgressDialog.dismiss();
        }

        @Override
        protected void onPreExecute() {

    }

        @Override
        protected void onProgressUpdate(Integer... values) {
            mProgressDialog.setProgress(values[0]);
        }
    }


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
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart() {

        adapter.notifyDataSetChanged();
        listView.invalidateViews();
        super.onStart();
    }

    @Override
    protected void onRestart() {
        adapter.notifyDataSetChanged();
        listView.invalidateViews();
        super.onRestart();

    }

    @Override
    protected void onPause() {

        adapter.notifyDataSetChanged();

        listView.invalidateViews();
        super.onPause();
    }


    @Override
    protected void onPostResume() {
        adapter.notifyDataSetChanged();
        listView.invalidateViews();
        super.onPostResume();


    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        if ( mProgressDialog!=null && mProgressDialog.isShowing() ){
            mProgressDialog.cancel();
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onResume() {

        adapter.notifyDataSetChanged();
        listView.invalidateViews();
        super.onResume();
    }
}
