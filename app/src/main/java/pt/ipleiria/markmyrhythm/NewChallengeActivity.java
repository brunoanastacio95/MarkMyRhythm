package pt.ipleiria.markmyrhythm;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.WeatherResponse;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Goal;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.GoalsReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import at.grabner.circleprogress.CircleProgressView;
import at.grabner.circleprogress.TextMode;

public class NewChallengeActivity extends AppCompatActivity {


    private static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1;
    private static final String LOG_TAG = "DEBUGTAG";
    private static final int REQUEST_CODE_FLPERMISSION = 20;
    private static float distance = 0;
    private static int calories = 0;
    private static TextView distanceText;
    private static CircleProgressView circleViewChanllengeRun;
    private TextView tempText;
    private float temp;
    private static float distanceAllWeek = 0;
    private float distanceGoal = 25;
    private LinkedList<Integer> conditions;
    private double latitude;
    private double longitude;
    private String locationDesc;
    private ImageView imageCondtions;
    private ImageView imageSport;
    private TextView textChallenge;
    private Button btnAcceptChallenge;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_challenge);

        distanceText = findViewById(R.id.textViewDistance);
        tempText = findViewById(R.id.textViewTemp);
        imageCondtions = findViewById(R.id.imageViewConditions);
        imageCondtions.setImageResource(R.drawable.ic_rainny_day);
        imageSport = findViewById(R.id.imageViewSport);
        textChallenge = findViewById(R.id.textViewChallenge);
        btnAcceptChallenge = findViewById(R.id.buttonShowChallenge);
        circleViewChanllengeRun = findViewById(R.id.circleViewChanllengeRun);
        circleViewChanllengeRun.setVisibility(View.INVISIBLE);
        distanceAllWeek = 0;

        checkFineLocationPermission();
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected() && GoogleSignIn.getLastSignedInAccount(this) != null) {
            conditions = new LinkedList<>();
            temp = -1000;
            latitude = -1000;
            longitude = -1000;
            DataType dataTypeDistance = DataType.TYPE_DISTANCE_DELTA;
            DataType dataTypeDistanceAggregate = DataType.AGGREGATE_DISTANCE_DELTA;
            DataType dataTypeCalories = DataType.TYPE_CALORIES_EXPENDED;
            DataType dataTypeCaloriesAggregate = DataType.AGGREGATE_CALORIES_EXPENDED;


            getWeatherOnCurrentLocation();
            getCoordinatesAndDesc();

            String firstLocation = latitude + "," + longitude;


            DistanceBetweenTwoPoints distanceBetweenTwoPoints = new DistanceBetweenTwoPoints();
            distanceBetweenTwoPoints.execute("https://maps.googleapis.com/maps/api" +
                    "/distancematrix/json?origins=" + firstLocation + "&destinations=Leiria" +
                    "&key=AIzaSyCdAUhha8frWa1Z9gTXgSh5KxqcIWd9NHc");

            allowFitnessOptions(dataTypeDistance, dataTypeDistanceAggregate);
            allowFitnessOptions(dataTypeCalories, dataTypeCaloriesAggregate);
            try {
                accessGoogleFit(dataTypeDistance, dataTypeDistanceAggregate);
                accessGoogleFit(dataTypeCalories, dataTypeCaloriesAggregate);
                accessGoogleFitForChallenge(dataTypeDistance, dataTypeDistanceAggregate);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(NewChallengeActivity.this,
                    "Error: no network connection.", Toast.LENGTH_LONG).show();
        }
    }

    private void allowFitnessOptions(DataType fieldNormal, DataType fieldAggregate) {
        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(fieldNormal, FitnessOptions.ACCESS_READ)
                .build();

        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(this,
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions);
        }
    }

    private void accessGoogleFit(DataType fieldNormal, DataType fieldAggregate) throws InterruptedException, ExecutionException, TimeoutException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int currentHour = cal.get(Calendar.HOUR_OF_DAY);
        cal.add(Calendar.HOUR, -currentHour);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_WEEK, -1);
        long startTime = cal.getTimeInMillis();


        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this)).readData(
                new DataReadRequest.Builder()
                        .aggregate(fieldNormal, fieldAggregate)
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .bucketByTime(1, TimeUnit.DAYS)
                        .build()).
                addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        printData(dataReadResponse);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    private void accessGoogleFitForChallenge(DataType fieldNormal, DataType fieldAggregate) throws InterruptedException, ExecutionException, TimeoutException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        long endTime = cal.getTimeInMillis();
        int currentHour = cal.get(Calendar.HOUR_OF_DAY);
        cal.add(Calendar.HOUR, -currentHour);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        cal.add(Calendar.DAY_OF_WEEK, -dayOfWeek +2);

        long startTime = cal.getTimeInMillis();


        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this)).readData(
                new DataReadRequest.Builder()
                        .aggregate(fieldNormal, fieldAggregate)
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .bucketByTime(1, TimeUnit.DAYS)
                        .build()).
                addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        printData(dataReadResponse);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }

    public static void printData(DataReadResponse dataReadResult) {
        // [START parse_read_data_result]
        // If the DataReadRequest object specified aggregated data, dataReadResult will be returned
        // as buckets containing DataSets, instead of just DataSets.
        if (dataReadResult.getBuckets().size() == 1) {
            Log.i(
                    LOG_TAG, "Number of returned buckets of DataSets is: " + dataReadResult.getBuckets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    dumpDataSet(dataSet);
                }
            }

        } else if (dataReadResult.getBuckets().size() > 1) {
            Log.i(LOG_TAG, "Number of returned DataSets is: " + dataReadResult.getDataSets().size());
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    dumpDataSetForChallenge(dataSet);
                }
            }
            circleViewChanllengeRun.setTextMode(TextMode.TEXT);
            float percentRunned = (distanceAllWeek / 25 ) *100;
            circleViewChanllengeRun.setValue(percentRunned);
            circleViewChanllengeRun.setText(Math.round(percentRunned)+"%" );
            circleViewChanllengeRun.setVisibility(View.VISIBLE);
        }

        // [END parse_read_data_result]
    }


    private static void dumpDataSet(DataSet dataSet) {
        Log.i(LOG_TAG, "Data returned for Data type: " + dataSet.getDataType().getName());

        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.i(LOG_TAG, "Data point:");
            Log.i(LOG_TAG, "\tType: " + dp.getDataType().getName());
            for (Field field : dp.getDataType().getFields()) {
                Log.i(LOG_TAG, "\tField: " + field.getName() + " Value: " + dp.getValue(field));
                if (field.getName().equals("distance")) {
                    int distanceValue = (int) dp.getValue(field).asFloat();
                    distance = (float) (distanceValue / 1000.0);
                }
                if (field.getName().equals("calories")) {
                    calories = (int) dp.getValue(field).asFloat();
                }
            }
        }
        distanceText.setText("Ontem percocorreu "+String.format("%.2f",distance)+ " km e perdeu "+ calories+" calorias.");
        distanceText.setGravity(Gravity.CENTER);
    }

    private static void dumpDataSetForChallenge(DataSet dataSet) {
        Log.i(LOG_TAG, "Data returned for Data type: " + dataSet.getDataType().getName());

        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.i(LOG_TAG, "Data point:");
            Log.i(LOG_TAG, "\tType: " + dp.getDataType().getName());
            for (Field field : dp.getDataType().getFields()) {
                Log.i(LOG_TAG, "\tField: " + field.getName() + " Value: " + dp.getValue(field));
                if (field.getName().equals("distance")) {
                    int distanceValue = (int) dp.getValue(field).asFloat();
                    float distanceAux = (float) (distanceValue / 1000.0);
                    distanceAllWeek +=  distanceAux ;
                }
                if (field.getName().equals("calories")) {
                    //calories = (int) dp.getValue(field).asFloat();
                }
            }
        }

    }

    private void getCoordinatesAndDesc() {
        checkFineLocationPermission();
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        String locationProvider = LocationManager.NETWORK_PROVIDER;
        locationManager.getLastKnownLocation(locationProvider);

        latitude = locationManager.getLastKnownLocation(locationProvider).getLatitude();
        longitude = locationManager.getLastKnownLocation(locationProvider).getLongitude();

        Geocoder gcd = new Geocoder(this, Locale.getDefault());

        List<Address> addresses = null;
        try {
            addresses = gcd.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        locationDesc = addresses.get(0).getAddressLine(0);
    }

    //Ver a temparuta onde estou
    private void getWeatherOnCurrentLocation() {
        checkFineLocationPermission();
        Awareness.getSnapshotClient(this).getWeather()
                .addOnSuccessListener(new OnSuccessListener<WeatherResponse>() {
                    @Override
                    public void onSuccess(WeatherResponse weatherResponse) {
                        Weather weather = weatherResponse.getWeather();
                        int conditionsCont = weather.getConditions().length;
                        temp = weather.getTemperature(Weather.CELSIUS);

                        for (int i = 0; i < conditionsCont; i++) {
                            conditions.add((weather.getConditions()[i]));
                            //6 significa que esta a chover "rainy", se tiver diferente nao chove
                            if (weather.getConditions()[i] != 6) {
                                imageCondtions.setImageResource(retrieveConditionImage(conditions.get(i)));
                                tempText.setText("Estao "+String.format("%.2f", temp) + " ºC e não está a chover, deve aproveitar para" +
                                        " ir praticar exericio fisico.");
                                tempText.setGravity(Gravity.CENTER);
                            } else {
                                imageSport.setImageResource(R.drawable.ic_workout);
                                textChallenge.setText("Aproveite faca desporto em casa");
                                btnAcceptChallenge.setVisibility(View.INVISIBLE);
                                imageCondtions.setImageResource(retrieveConditionImage(conditions.get(i)));
                                tempText.setText("Estao "+String.format("%.2f", temp) + " ºC  mas está a chover.");
                                tempText.setGravity(Gravity.CENTER);
                                return;
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void checkFineLocationPermission() {
        if (ContextCompat.checkSelfPermission(NewChallengeActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(NewChallengeActivity.this,
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

    private int retrieveConditionImage(int condition) {
        switch (condition) {
            case Weather.CONDITION_CLEAR:
                return R.drawable.ic_sunny_day;
            case Weather.CONDITION_CLOUDY:
                return R.drawable.ic_sunny_day;
            case Weather.CONDITION_FOGGY:
                return R.drawable.ic_sunny_day;
            case Weather.CONDITION_HAZY:
                return R.drawable.ic_sunny_day;
            case Weather.CONDITION_ICY:
                return R.drawable.ic_sunny_day;
            case Weather.CONDITION_RAINY:
                return R.drawable.ic_rainny_day;
            case Weather.CONDITION_SNOWY:
                return R.drawable.ic_sunny_day;
            case Weather.CONDITION_STORMY:
                return R.drawable.ic_sunny_day;
            case Weather.CONDITION_WINDY:
                return R.drawable.ic_sunny_day;
            default:
                return R.drawable.ic_sunny_day;

        }
    }

    private String retrieveConditionString(int condition) {
        switch (condition) {
            case Weather.CONDITION_CLEAR:
                return "Não esta a chover";
            case Weather.CONDITION_CLOUDY:
                return "Não esta a chover";
            case Weather.CONDITION_FOGGY:
                return "Não esta a chover";
            case Weather.CONDITION_HAZY:
                return "Não esta a chover";
            case Weather.CONDITION_ICY:
                return "Não esta a chover";
            case Weather.CONDITION_RAINY:
                return "Cuidado esta a chover";
            case Weather.CONDITION_SNOWY:
                return "Não esta a chover";
            case Weather.CONDITION_STORMY:
                return "Não esta a chover";
            case Weather.CONDITION_WINDY:
                return "Não esta a chover";
            default:
                return "Não esta a chover";
        }
    }

    public void googleMapsOnClick(View view) {
        Intent i = new Intent(NewChallengeActivity.this, MapsActivity.class);
        i.putExtra("longitude", longitude);
        i.putExtra("latitude", latitude);
        startActivity(i);
    }


    private class DistanceBetweenTwoPoints extends AsyncTask<String, Void, String> {

        @SuppressLint("LongLogTag")
        @Override
        protected String doInBackground(String... urls) {

            try {
                // establish the connection to the network resource
                URL url = new URL(urls[0]);
                HttpURLConnection httpURLConnection =
                        (HttpURLConnection) url.openConnection();
                httpURLConnection.setReadTimeout(10000);
                httpURLConnection.setConnectTimeout(15000);
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();
                int responseCode = httpURLConnection.getResponseCode();
                Log.i("Google Api Distance Matrix", "HTTP response code: " + responseCode);
                //retrieve the network resource's content
                InputStream inputStream = httpURLConnection.getInputStream();
                String contentAsString = readStream(inputStream);
                inputStream.close();
                return contentAsString;
            } catch (IOException e) {
                return "ERROR: unable to retrieve web page. URL may be invalid.";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (!result.startsWith("ERROR")) {
                JSONObject distance = new JSONObject();
                try {
                    distance = new JSONObject(result);
                    JSONArray ola = distance.getJSONArray("rows");
                    System.out.println("RESULT" + ola.getJSONObject(0).getJSONArray("elements")
                            .getJSONObject(0).getJSONObject("distance").getString("value"));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(NewChallengeActivity.this, result, Toast.LENGTH_LONG).show();
            }
        }


    }

    private String readStream(InputStream is) {
        StringBuilder sb = new StringBuilder(512);
        try {
            Reader r = new InputStreamReader(is, "UTF-8");
            int c = 0;
            while ((c = r.read()) != -1) {
                sb.append((char) c);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }
}