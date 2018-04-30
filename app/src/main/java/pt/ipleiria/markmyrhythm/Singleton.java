package pt.ipleiria.markmyrhythm;

public class Singleton {
    private static final Singleton ourInstance = new Singleton();

    public static Singleton getInstance() {
        return ourInstance;
    }
    private GoogleClient googleClient;
    private Singleton() {
        this.googleClient = new GoogleClient();
    }

    public GoogleClient getGoogleClient() {
        return googleClient;
    }
    public void setGoogleClient (GoogleClient googleClient){
        this.googleClient = googleClient;
    }
}
