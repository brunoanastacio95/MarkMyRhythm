package pt.ipleiria.markmyrhythm;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class NewChallengeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_challenge);

        if(Singleton.getInstance().getGoogleAccount() != null) {
            System.out.println("OLA" +
                    Singleton.getInstance().getGoogleAccount().getDisplayName());
        }
    }
}
