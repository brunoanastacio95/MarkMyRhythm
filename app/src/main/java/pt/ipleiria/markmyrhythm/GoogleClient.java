package pt.ipleiria.markmyrhythm;

import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.SignInButton;

import java.io.Serializable;

public class GoogleClient implements Serializable{

    private static final String LOG_TAG = "DEBUGTAG" ;
    private static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1;
    private static final int RC_SIGN_IN = 2;
    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton signInButton;
    // Set the dimensions of the sign-in button.
    private GoogleSignInAccount acct;
    private TextView nameAcct;
    private ImageView imgAcct;


}
