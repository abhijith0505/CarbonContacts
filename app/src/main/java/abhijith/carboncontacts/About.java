package abhijith.carboncontacts;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_about);
    }
}
