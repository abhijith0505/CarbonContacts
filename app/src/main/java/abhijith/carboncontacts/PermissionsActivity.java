package abhijith.carboncontacts;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/*
    Activity to set the new runtime permissions for Android M and above.
    For versions below M, old permission system using Android Manifest
 */

public class PermissionsActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_CONTACTS = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        if (ContextCompat.checkSelfPermission(PermissionsActivity.this,
                Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(PermissionsActivity.this,
                        Manifest.permission.WRITE_CONTACTS)
                        == PackageManager.PERMISSION_GRANTED) {

            //If permission given, start the Main Carbon Contacts (CC) activity
            Intent intent = new Intent(PermissionsActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

        Button readPerm = (Button) findViewById(R.id.readPerm);
        Button writePerm = (Button) findViewById(R.id.writePerm);
        Button grantedPerm = (Button) findViewById(R.id.granted);

        readPerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(PermissionsActivity.this,
                        Manifest.permission.READ_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(PermissionsActivity.this,
                            Manifest.permission.READ_CONTACTS)) {
                        //Toast.makeText(PermissionsActivity.this,"Read contacts",Toast.LENGTH_LONG).show();
                        //TODO: Show an expanation to the user *asynchronously* -- UI to be done
                        ActivityCompat.requestPermissions(PermissionsActivity.this,
                                new String[]{Manifest.permission.READ_CONTACTS},
                                MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                    } else {
                        // No explanation needed, we can request the permission.
                        ActivityCompat.requestPermissions(PermissionsActivity.this,
                                new String[]{Manifest.permission.READ_CONTACTS},
                                MY_PERMISSIONS_REQUEST_READ_CONTACTS);
                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                }
            }
        });

        //Below permission not needed as One single READ permission appears to do the job
        //TODO: remove unnecessary permissions

        writePerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(PermissionsActivity.this,
                        Manifest.permission.WRITE_CONTACTS)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (ActivityCompat.shouldShowRequestPermissionRationale(PermissionsActivity.this,
                            Manifest.permission.READ_CONTACTS)) {

                        // Show an expanation to the user *asynchronously* -- don't block
                        // this thread waiting for the user's response! After the user
                        // sees the explanation, try again to request the permission.
                        ActivityCompat.requestPermissions(PermissionsActivity.this,
                                new String[]{Manifest.permission.WRITE_CONTACTS},
                                MY_PERMISSIONS_REQUEST_WRITE_CONTACTS);

                    } else {

                        // No explanation needed, we can request the permission.

                        ActivityCompat.requestPermissions(PermissionsActivity.this,
                                new String[]{Manifest.permission.WRITE_CONTACTS},
                                MY_PERMISSIONS_REQUEST_WRITE_CONTACTS);

                        // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    }
                }
            }
        });


        //Proceed button that goes to mainactivity only if the desired permissions are given
        grantedPerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(PermissionsActivity.this,
                        Manifest.permission.READ_CONTACTS)
                        == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(PermissionsActivity.this,
                                Manifest.permission.WRITE_CONTACTS)
                                == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(PermissionsActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish(); // call this to finish the current activity
                }
            }
        });
    }

    // Callback with the request from calling requestPermissions(...)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Read Contacts permission granted", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "Read Contacts permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        //Below callback not needed is one permission does the job
        //TODO: remove unnecessary callbacks

        else if(requestCode == MY_PERMISSIONS_REQUEST_WRITE_CONTACTS) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Write Contacts permission granted", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "Write Contacts permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if(requestCode == MY_PERMISSIONS_REQUEST_READ_PHONE_STATE){
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Read Phone State permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Read Phone state permission denied", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
