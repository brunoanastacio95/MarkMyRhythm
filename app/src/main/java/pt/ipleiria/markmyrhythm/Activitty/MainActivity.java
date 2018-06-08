package pt.ipleiria.markmyrhythm.Activitty;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.util.IOUtils;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.LinkedList;

import pt.ipleiria.markmyrhythm.Model.Route;
import pt.ipleiria.markmyrhythm.Util.CropSquareTransformation;
import pt.ipleiria.markmyrhythm.Model.Singleton;
import pt.ipleiria.markmyrhythm.R;

public class MainActivity extends AppCompatActivity {


    private static final int RC_SIGN_IN = 2;
    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton signInButton;
    // Set the dimensions of the sign-in button.
    private GoogleSignInAccount acct;
    private TextView nameAcct;
    private TextView emailAcct;
    private ImageView imgAcct;
    private static final int REQUEST_CODE_FLPERMISSION = 20;
    private LinkedList<Route> routes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        nameAcct = findViewById(R.id.nameAcct);
        nameAcct.setText("");
        emailAcct = findViewById(R.id.emailAcct);
        emailAcct.setText("");
        imgAcct = findViewById(R.id.imgAcct);
        routes = new LinkedList<>();

        signInButton.setOnClickListener(new SignInButton.OnClickListener() {
            public void onClick(View v) {
                signIn();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        acct = GoogleSignIn.getLastSignedInAccount(this);

        //Singleton
        Singleton.getInstance().setGoogleSignClient(mGoogleSignInClient);
        Singleton.getInstance().setGoogleAccount(acct);
        checkFineLocationPermission();


        if (acct != null) {
            setGooglePlusButtonText(signInButton,"Sign out");
            String personName = acct.getDisplayName();
            String email = acct.getEmail();
            Uri personPhoto = acct.getPhotoUrl();
            Picasso.get().load(personPhoto).transform(new CropSquareTransformation()).into(imgAcct);
            nameAcct.setText(personName);
            emailAcct.setText(email);
        }
        addRoutes();

    }

    private void addRoutes() {
        //routes.add(new Route("39.7380986,-8.8257577","fim","a,b,c"));
       // routes.add(new Route("39.2380986,-8.8257577","fim1","a,b,c"));



        String text = readFile("short_3.txt");
        routes.add(createRoute(text, 1));
        text = readFile("short_4.txt");
        routes.add(createRoute(text, 1));

        text = readFile("short_1.txt");
        routes.add(createRoute(text, 2));
        text = readFile("short_2.txt");
        routes.add(createRoute(text, 2));

        text = readFile("short_5.txt");
        routes.add(createRoute(text, 3));

        Singleton.getInstance().setRoutes(routes);
    }

    private Route createRoute(String text, int size){
        String[]lines = text.split(";");
        String partial = "";
        String wayPoints = "";
        String start = "";
        String end = "";

        String[]auxStart = lines[0].split(",");
        start = auxStart[1]+","+auxStart[0];

        String[]auxEnd = lines[lines.length-1].split(",");
        end = auxEnd[1]+","+auxEnd[0];

        for(int i = 1; i < lines.length-1; i++){
            String[]values = lines[i].split(",");
            partial = values[1]+ "," +values[0];
            if(i != 1){
                wayPoints += "|" + partial;
            }else{
                wayPoints += partial;
            }
        }

        System.out.println("WAYPOINTS: ");
        System.out.println(wayPoints);
        Route r = new Route(start, end, wayPoints, size );
        return r;
    }

    private String readFile(String fileName)  {
        StringBuilder strBuilder = new StringBuilder();

        InputStream fIn = null;
        InputStreamReader isr = null;
        BufferedReader input = null;
        try {
            fIn = this.getResources().getAssets().open(fileName);
            isr = new InputStreamReader(fIn);
            input = new BufferedReader(isr);
            String line = "";
            while ((line = input.readLine()) != null) {
                strBuilder.append(line);
            }
        } catch (Exception e) {
            e.getMessage();
        } finally {
            try {
                if (isr != null)
                    isr.close();
                if (fIn != null)
                    fIn.close();
                if (input != null)
                    input.close();
            } catch (Exception e2) {
                e2.getMessage();
            }
        }

        return strBuilder.toString();
    }

    private void signIn() {
        if(acct == null) {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }else{
            imgAcct.setImageBitmap(null);
            mGoogleSignInClient.signOut();
            acct = null;
            Singleton.getInstance().setGoogleSignClient(mGoogleSignInClient);
            Singleton.getInstance().setGoogleAccount(acct);
            setGooglePlusButtonText(signInButton,"Sign in");
            nameAcct.setText("");
            emailAcct.setText("");
        }
    }
    private void checkFineLocationPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_FLPERMISSION
            );
        }
        try {
            int locationMode = Settings.Secure.getInt(getContentResolver(),
                    Settings.Secure.LOCATION_MODE);
            if (locationMode != Settings.Secure.LOCATION_MODE_HIGH_ACCURACY) {
                Toast.makeText(this,
                        "Error: high accuracy location mode must be enabled in the device.",
                        Toast.LENGTH_LONG).show();
                return;
            }
        } catch (Settings.SettingNotFoundException e) {
            Toast.makeText(this, "Error: could not access location mode.",
                    Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RC_SIGN_IN) {

            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            setGooglePlusButtonText(signInButton,"Sign out");
            acct = GoogleSignIn.getLastSignedInAccount(this);

            Singleton.getInstance().setGoogleSignClient(mGoogleSignInClient);
            Singleton.getInstance().setGoogleAccount(acct);

            String personName = acct.getDisplayName();
            String email = acct.getEmail();
            Uri personPhoto = Uri.parse(String.valueOf(acct.getPhotoUrl()));
            Picasso.get().load(personPhoto).transform(new CropSquareTransformation()).into(imgAcct);
            nameAcct.setText(personName);
            emailAcct.setText(email);
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }

    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            // updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.

        }
    }


    protected void setGooglePlusButtonText(SignInButton signInButton, String buttonText) {
        // Find the TextView that is inside of the SignInButton and set its text
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(buttonText);
                return;
            }
        }
    }

    public void newChallengeOnClick(View view) {
       if (GoogleSignIn.getLastSignedInAccount(this) != null ) {
           Intent i = new Intent(MainActivity.this, NewChallengeActivity.class);
           startActivity(i);
       }else{
           Snackbar.make(view, "Tem de estar logado", Snackbar.LENGTH_SHORT).show();
       }
    }
}
