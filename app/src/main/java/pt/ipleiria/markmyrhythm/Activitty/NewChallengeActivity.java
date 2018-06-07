package pt.ipleiria.markmyrhythm.Activitty;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.TimeFence;
import com.google.android.gms.awareness.snapshot.WeatherResponse;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Goal;
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
import java.sql.SQLOutput;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


import pt.ipleiria.markmyrhythm.Util.CircleAdapter;
import pt.ipleiria.markmyrhythm.R;


public class NewChallengeActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "sdas" ;
    static float t = 0;
    private static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1;
    private static final String FENCE_RECEIVER_ACTION = "FENCE_RECEIVER_ACTION";

    private static final String LOG_TAG = "DEBUGTAG";
    private static final int REQUEST_CODE_FLPERMISSION = 20;
    private static float distance = 0;
    private static int calories = 0;
    private static float distanceAllweek = -10;
    private static float distannceDay = -10;
    private static float stepAllweek = -10;
    private static float stepDay = -10;
    private static TextView distanceText;
    private TextView tempText;
    private float temp;
    private LinkedList<Integer> conditions;
    private double latitude;
    private double longitude;
    private String locationDesc;
    private ImageView imageCondtions;
    private ImageView imageSport;
    private TextView textChallenge;
    private Button btnAcceptChallenge;
    private ArrayList<pt.ipleiria.markmyrhythm.Model.Goal> goals;
    private static int contHour;
    private static int hourMaxActivity;
    private static float maxActivity;

    private PendingIntent myPendingIntent;
    private FenceReceiver fenceReceiver;


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

        goals = new ArrayList<>();
        conditions = new LinkedList<>();
        temp = -1000;
        latitude = -1000;
        longitude = -1000;
        contHour = 0;
        maxActivity = 0;

        Intent i = new Intent(FENCE_RECEIVER_ACTION);
        myPendingIntent = PendingIntent.getBroadcast(this, 0, i, 0);
        fenceReceiver = new FenceReceiver();
        registerReceiver(fenceReceiver, new IntentFilter(FENCE_RECEIVER_ACTION));


        checkFineLocationPermission();
        if (ContextCompat.checkSelfPermission(NewChallengeActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected() && GoogleSignIn.getLastSignedInAccount(this) != null) {
            DataType dataTypeDistance = DataType.TYPE_DISTANCE_DELTA;
            DataType dataTypeDistanceAggregate = DataType.AGGREGATE_DISTANCE_DELTA;

            getWeatherOnCurrentLocation();
            getCoordinatesAndDesc();

            allowFitnessOptions(dataTypeDistance);
            accessGoogleFit(dataTypeDistance, dataTypeDistanceAggregate);
            accessGoogleFitForChallenge();
            getHourActivityLastWeek(dataTypeDistance, dataTypeDistanceAggregate);
            addFenceTime();
        } else {
            Toast.makeText(NewChallengeActivity.this,
                    "Error: no network connection.", Toast.LENGTH_LONG).show();
        }

      /*  Intent i = new Intent(FENCE_RECEIVER_ACTION);
        myPendingIntent = PendingIntent.getBroadcast(this, 0, i, 0);
        AlarmReceiver a = new AlarmReceiver();
        registerReceiver(a, new IntentFilter(FENCE_RECEIVER_ACTION));
        a.scheduleAlarm(this,1);
*/

    }

    private void allowFitnessOptions(DataType fieldNormal) {
        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(fieldNormal, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_CALORIES_EXPENDED)
                .build();

        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(this,
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions);
        }
    }

    private void addFenceTime() {
        long nowMillis = System.currentTimeMillis();
        long oneMinuteMilis = 60L * 1000L;
        long thirtySecondsMillis = 30L * 1000L;
        AwarenessFence timeFence = TimeFence.inInterval(
                nowMillis + thirtySecondsMillis,
                nowMillis + oneMinuteMilis); // one minute starting in thirty seconds
        addFence("timeFence", timeFence);
    }



    private void addFence(final String fenceKey, final AwarenessFence fence) {
        Awareness.getFenceClient(this).updateFences(new FenceUpdateRequest.Builder()
                .addFence(fenceKey, fence, myPendingIntent)
                .build())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                })
        ;
    }

    private void accessGoogleFit(DataType fieldNormal, DataType fieldAggregate) {
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
                        .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
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
                Toast.makeText(NewChallengeActivity.this,
                        "Error: ",
                        Toast.LENGTH_LONG).show();
            }
        });

    }

    private void accessGoogleFitForChallenge() {
        distanceAllweek = -10;
        stepAllweek = -10;
        goals.clear();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        long endTime = cal.getTimeInMillis();
        int currentHour = cal.get(Calendar.HOUR_OF_DAY);
        cal.add(Calendar.HOUR, -currentHour);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        cal.add(Calendar.DAY_OF_WEEK, -dayOfWeek + 2);
        long startTime = cal.getTimeInMillis();

        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this)).readData(
                new DataReadRequest.Builder()
                        .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                        .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .bucketByTime(1, TimeUnit.DAYS)
                        .build()).
                addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        printDataChallenge(dataReadResponse);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NewChallengeActivity.this,
                        "Error: ",
                        Toast.LENGTH_LONG).show();
            }
        });

    }

    private void accessGoogleFitGoals() {
        Fitness.getGoalsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readCurrentGoals(
                        new GoalsReadRequest.Builder()
                                .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                                .addDataType(DataType.TYPE_DISTANCE_DELTA)
                                .build()).addOnSuccessListener(new OnSuccessListener<List<Goal>>() {
            @Override
            public void onSuccess(List<Goal> goalsResult) {

                for (int i = 0; i < goalsResult.size(); i++) {
                    if (goalsResult.get(i).getRecurrence().getUnit() != 3) {
                        int recurrence = goalsResult.get(i).getRecurrence().getUnit();
                        float value = (float) goalsResult.get(i).getMetricObjective().getValue();
                        String type = goalsResult.get(i).getMetricObjective().getDataTypeName();
                        float current = 0;

                        if (goalsResult.get(i).getRecurrence().getUnit() == 1) {
                            if (goalsResult.get(i).getMetricObjective().getDataTypeName().matches("com.google.distance.delta")) {
                                current = distannceDay;
                            } else {
                                current = stepDay;
                            }

                        } else {
                            if (goalsResult.get(i).getMetricObjective().getDataTypeName().matches("com.google.distance.delta")) {
                                current = distanceAllweek;
                            } else {
                                current = stepAllweek;
                            }
                        }
                        pt.ipleiria.markmyrhythm.Model.Goal g = new pt.ipleiria.markmyrhythm.Model.Goal(value, recurrence, type, current);
                        goals.add(g);
                    }
                }
                createCircleGoals();
                checkIfGoalsCompleted();
            }
        });

    }

    private void getHourActivityLastWeek(DataType fieldNormal, DataType fieldAggregate) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int currentHour = cal.get(Calendar.HOUR_OF_DAY);
        cal.add(Calendar.HOUR, -currentHour);
        cal.add(Calendar.DAY_OF_MONTH, -6);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        long startTime = cal.getTimeInMillis();

        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this)).readData(
                new DataReadRequest.Builder()
                        .aggregate(fieldNormal, fieldAggregate)
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .bucketByTime(1, TimeUnit.HOURS)
                        .build()).
                addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        printData(dataReadResponse);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(NewChallengeActivity.this,
                        "Error: ",
                        Toast.LENGTH_LONG).show();
            }
        });


    }

    private Boolean checkIfGoalsCompleted() {

        for (int i = 0; i < goals.size(); i++) {
            if (goals.get(i).getRecurence() == 1) {
                float objective = goals.get(i).getValue();
                float current = goals.get(i).getCurrent();
                float percentComplete = (current / objective) * 100;
                if (percentComplete < 100) {
                    return false;
                }
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Hoje ja completou os desafios todos")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                })
                .setTitle("MarkMyRhythm");
        AlertDialog d = builder.create();
        d.show();
        return true;
    }

    private void createCircleGoals() {
        ListView l = (ListView) findViewById(R.id.listviewCircles);
        CircleAdapter adapter;
        adapter = new CircleAdapter(NewChallengeActivity.this, 0, goals);
        l.setAdapter(adapter);
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
                    dumpDataSetForHourActivity(dataSet);
                }
                contHour++;
            }
            System.out.println("PUTAS MAX" + maxActivity + "HORAS" + hourMaxActivity);
        }

        // [END parse_read_data_result]
    }

    public void printDataChallenge(DataReadResponse dataReadResult) {
        for (Bucket bucket : dataReadResult.getBuckets()) {
            List<DataSet> dataSets = bucket.getDataSets();
            for (DataSet dataSet : dataSets) {
                dumpDataSetForChallenge(dataSet);
            }
        }
        accessGoogleFitGoals();
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
        distanceText.setText("Ontem percocorreu " + String.format("%.2f", distance) + " km e perdeu " + calories + " calorias.");
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
                    float distanceValue = dp.getValue(field).asFloat();
                    if (distanceAllweek == -10) {
                        distanceAllweek = distanceValue;
                    } else {
                        distanceAllweek += distanceValue;
                    }
                    distannceDay = distanceValue;
                }
                if (field.getName().equals("steps")) {
                    int value = dp.getValue(field).asInt();

                    if (stepAllweek == -10) {
                        stepAllweek = value;
                    } else {
                        stepAllweek += value;
                    }
                    stepDay = value;
                }
            }
        }

    }

    private static void dumpDataSetForHourActivity(DataSet dataSet) {

        Log.i(LOG_TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        for (DataPoint dp : dataSet.getDataPoints()) {

            Log.i(LOG_TAG, "Data point:");
            Log.i(LOG_TAG, "\tType: " + dp.getDataType().getName());
            for (Field field : dp.getDataType().getFields()) {
                Log.i(LOG_TAG, "\tField: " + field.getName() + " Value: " + dp.getValue(field));
                if (dp.getValue(field).asFloat() > maxActivity) {
                    maxActivity = dp.getValue(field).asFloat();
                    hourMaxActivity = contHour;
                }
                System.out.println("PUTAS" + dp.getValue(field) + "HORAS" + contHour);
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
                                tempText.setText("Estao " + String.format("%.2f", temp) + " ºC e não está a chover, deve aproveitar para" +
                                        " ir praticar exericio fisico.");
                                tempText.setGravity(Gravity.CENTER);
                            } else {
                                imageSport.setImageResource(R.drawable.ic_workout);
                                textChallenge.setText("Aproveite faca desporto em casa");
                                btnAcceptChallenge.setVisibility(View.INVISIBLE);
                                imageCondtions.setImageResource(retrieveConditionImage(conditions.get(i)));
                                tempText.setText("Estao " + String.format("%.2f", temp) + " ºC  mas está a chover.");
                                tempText.setGravity(Gravity.CENTER);
                                return;
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(NewChallengeActivity.this,
                                "Error: ",
                                Toast.LENGTH_LONG).show();
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

    public void googleMapsOnClick(View view) {
        Intent i = new Intent(NewChallengeActivity.this, MapsActivity.class);
        i.putExtra("longitude", longitude);
        i.putExtra("latitude", latitude);
        startActivity(i);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(fenceReceiver);
    }

    private class FenceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (!intent.getAction().equals(FENCE_RECEIVER_ACTION)) {
                return;
            }
            FenceState fenceState = FenceState.extract(intent);
            String fenceInfo = null;
            switch (fenceState.getFenceKey()) {
                case "timeFence":
                    switch (fenceState.getCurrentState()) {
                        case FenceState.TRUE:
                            fenceInfo = "TRUE | Within timeslot.";
                            System.out.println("entrei");
                           // createNotificationChannel();
                           // createNotification();
                            break;
                        case FenceState.FALSE:
                            fenceInfo = "FALSE | Out of timeslot.";
                            System.out.println("entrei false");
                            break;
                        case FenceState.UNKNOWN:
                            fenceInfo = "Error: unknown state.";
                            break;
                    }
                    break;
                default:
                    fenceInfo = "Error: unknown fence: " + fenceState.getFenceKey();
                    break;
            }

            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            String text = "\n\n[Fences @ " + timestamp + "]\n"
                    + fenceState.getFenceKey() + ": " + fenceInfo;

            Toast.makeText(NewChallengeActivity.this,
                    "DISPAREII", Toast.LENGTH_LONG).show();
        }


    }

    public  class AlarmReceiver extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (!intent.getAction().equals(FENCE_RECEIVER_ACTION)) {
                return;
            }
            System.out.println("FIRRRRREEEEEE");
            //Wake up every 6 hours
            //createNotificationChannel();
            //createNotification();
            scheduleAlarm(context, 6);
            Toast.makeText(context, "ALARM FIRED !!!", Toast.LENGTH_SHORT).show();
        }

        /**
         * Schedule Alarm in the specified time after the current time
         * @param context Application Context
         * @param hours Hours after the current time is going to ring the Alarm
         */
        public void scheduleAlarm(Context context, int hours)
        {
            System.out.println("FOOOOOOK");
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            System.out.println("1");
            System.out.println(calendar.getTimeInMillis());
            calendar.add(Calendar.MILLISECOND, 5000); // Hour-DAY
            System.out.println("2");
            System.out.println(calendar.getTimeInMillis());
            // Actual time plus y + hour in milliseconds
            // long millis = System.currentTimeMillis() + (hours * 1000) ; //* 60 * 60

            Intent intentAlarm = new Intent(context, AlarmReceiver.class);
            // Get the Alarm Service.
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if(alarmManager != null){
                System.out.println("LEEEEELLLLL");
                // Set the alarm for a particular time.
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 30*1000,PendingIntent.getBroadcast(context, 1, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT) );
                // alarmManager.set(AlarmManager.RTC_WAKEUP, millis, PendingIntent.getBroadcast(context, 1, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT));
            }
            Log.i("Alarm Scheduled", "Alarm Scheduled");
        }


    }
    private void createNotification(){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(NewChallengeActivity.this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_run)
                .setContentTitle("My notification")
                .setContentText("Much longer text that cannot fit one line...")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Much longer text that cannot fit one line..."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent resultIntent = new Intent(NewChallengeActivity.this, NewChallengeActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(NewChallengeActivity.this);
        stackBuilder.addParentStack(NewChallengeActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0, PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Random r = new Random();
        int a = r.nextInt((100-10)+1)+10;
        mNotificationManager.notify(a, mBuilder.build());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //CharSequence name = getString(R.string.channel_name);
            //String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "MarkMyRhythmChannel", importance);
            channel.setDescription("MarkMyRhythmChannel");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
