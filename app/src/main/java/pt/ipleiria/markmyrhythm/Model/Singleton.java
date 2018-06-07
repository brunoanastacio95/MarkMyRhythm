package pt.ipleiria.markmyrhythm.Model;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.util.LinkedList;

public class Singleton {
    private static final Singleton ourInstance = new Singleton();

    public static Singleton getInstance() {
        return ourInstance;
    }

    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build();
    private GoogleSignInClient googleSignClient;
    private GoogleSignInAccount googleAccount;
    private LinkedList<Route> routes;
    private int lastActivityHour = -1;

    public LinkedList<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(LinkedList<Route> routes) {
        this.routes = routes;
    }

    public GoogleSignInAccount getGoogleAccount() {
        return googleAccount;
    }
    public void setGoogleAccount (GoogleSignInAccount googleAccount){
        this.googleAccount = googleAccount;
    }

    public GoogleSignInClient getGoogleSignClient() {
        return googleSignClient;
    }
    public void setGoogleSignClient (GoogleSignInClient googleClient){
        this.googleSignClient = googleClient;
    }

    public int getLastActivityHour() {
        return lastActivityHour;
    }

    public void setLastActivityHour(int lastActivityHour) {
        this.lastActivityHour = lastActivityHour;
    }
}
