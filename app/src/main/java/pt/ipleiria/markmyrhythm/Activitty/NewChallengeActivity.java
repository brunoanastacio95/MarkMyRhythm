package pt.ipleiria.markmyrhythm.Activitty;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
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
import android.support.design.widget.Snackbar;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLOutput;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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


import pt.ipleiria.markmyrhythm.Model.AlarmReceiver;
import pt.ipleiria.markmyrhythm.Model.NotificationAlarm;
import pt.ipleiria.markmyrhythm.Model.Route;
import pt.ipleiria.markmyrhythm.Model.Singleton;
import pt.ipleiria.markmyrhythm.Util.CircleAdapter;
import pt.ipleiria.markmyrhythm.R;


public class NewChallengeActivity extends AppCompatActivity {

    static float t = 0;
    private static final int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1;
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
    private ImageView imagePlus;
    private ImageView imageEqual;
    private ImageView imageProgress;
    private TextView textChallenge;
    private Button btnAcceptChallenge;
    private ArrayList<pt.ipleiria.markmyrhythm.Model.Goal> goals;
    private static int contHour;
    private static int hourMaxActivity;
    private static float maxActivity;
    private static View view;
    private static String dayName;
    private boolean isRainning;
    private LinkedList<Route> routes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_challenge);

        AlarmReceiver.getInstance().scheduleAlarm(this, 24);

        distanceText = findViewById(R.id.textViewDistance);
        tempText = findViewById(R.id.textViewTemp);
        imageCondtions = findViewById(R.id.imageViewConditions);
        imageCondtions.setImageResource(R.drawable.ic_rainny_day);
        imageSport = findViewById(R.id.imageViewSport);
        imagePlus = findViewById(R.id.imageViewPlus);
        imageEqual = findViewById(R.id.imageViewEqual);
        imageProgress = findViewById(R.id.imageViewProgress);
        textChallenge = findViewById(R.id.textViewChallenge);
        btnAcceptChallenge = findViewById(R.id.buttonShowChallenge);
        view = findViewById(android.R.id.content);


        goals = new ArrayList<>();
        conditions = new LinkedList<>();
        temp = -1000;
        latitude = -1000;
        longitude = -1000;
        contHour = 0;
        maxActivity = 0;
        isRainning = false;
        routes = Singleton.getInstance().getRoutes();

        checkFineLocationPermission();
        if (ContextCompat.checkSelfPermission(NewChallengeActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            DataType dataTypeDistance = DataType.TYPE_DISTANCE_DELTA;
            DataType dataTypeDistanceAggregate = DataType.AGGREGATE_DISTANCE_DELTA;

            getWeatherOnCurrentLocation();
            getCoordinatesAndDesc();

            allowFitnessOptions(dataTypeDistance);
            accessGoogleFit(dataTypeDistance, dataTypeDistanceAggregate);
            accessGoogleFitForChallenge();
        } else {
            imageCondtions.setVisibility(View.INVISIBLE);
            imageSport.setVisibility(View.INVISIBLE);
            btnAcceptChallenge.setVisibility(View.INVISIBLE);
            textChallenge.setVisibility(View.INVISIBLE);
            imageProgress.setVisibility(View.INVISIBLE);
            imagePlus.setVisibility(View.INVISIBLE);
            imageEqual.setVisibility(View.INVISIBLE);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Não há ligação à internet neste momento")
                    .setPositiveButton("Tenta outra vez", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = getIntent();
                            finish();
                            startActivity(intent);
                        }
                    })
                    .setTitle("Sem ligação à internet");
            AlertDialog d = builder.create();
            d.show();
        }

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

    private void accessGoogleFit(DataType fieldNormal, DataType fieldAggregate) {
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        cal.setTime(new Date());
        int currentHour = cal.get(Calendar.HOUR_OF_DAY);
        cal.add(Calendar.HOUR, -currentHour);
        cal.add(Calendar.DAY_OF_WEEK, -6);
        Locale pt = new Locale("pt","pt");
        dayName = new SimpleDateFormat("EEEE", pt).format(date.getTime());

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
                Snackbar.make(view, "Error: Cant Access google fit history ", Snackbar.LENGTH_SHORT).show();
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
        if (dayOfWeek == 1){
            cal.add(Calendar.DAY_OF_WEEK,-6);
        }else {
            cal.add(Calendar.DAY_OF_WEEK, -dayOfWeek + 2);
        }

        long startTime = cal.getTimeInMillis();

        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this)).readData(
                new DataReadRequest.Builder()
                        .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
                        .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
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
                Snackbar.make(view, "Error: Cant Access google fit history ", Snackbar.LENGTH_SHORT).show();
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
                addRoutes();
                checkIfGoalsCompleted();

            }
        });

    }

    public static void getHourActivityLastWeek_2(final Context context) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int currentHour = cal.get(Calendar.HOUR_OF_DAY);
        cal.add(Calendar.HOUR, -currentHour);
        cal.add(Calendar.DAY_OF_MONTH, -6);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        long startTime = cal.getTimeInMillis();

        DataType fieldNormal = DataType.TYPE_DISTANCE_DELTA;
        DataType fieldAggregate = DataType.AGGREGATE_DISTANCE_DELTA;

        Fitness.getHistoryClient(context, GoogleSignIn.getLastSignedInAccount(context)).readData(
                new DataReadRequest.Builder()
                        .aggregate(fieldNormal, fieldAggregate)
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .bucketByTime(1, TimeUnit.HOURS)
                        .build()).
                addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        printData(dataReadResponse);
                        // validar se o tempo é válido
                        NotificationAlarm.getInstance().scheduleAlarm(context);
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(view, "Error: Cant Access google fit history ", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void checkIfGoalsCompleted() {
        float percentCompleteDay = 100;
        float percentCompleteWeek = 100;
        float percentStepDay = 100;
        Calendar cal = Calendar.getInstance();
        int day =  cal.get(Calendar.DAY_OF_WEEK);
        float accuracy = 0;
        LinkedList<Route> finalRoutes = new LinkedList<>();
        float objectiveWeek = 0;

        for (int i = 0; i < goals.size(); i++) {
            if (goals.get(i).getDataType().equals("com.google.distance.delta")) {
                float objective = goals.get(i).getValue();

                float current = goals.get(i).getCurrent();
                if (goals.get(i).getRecurence() == 1) {
                    percentCompleteDay = (current / objective) * 100;
                }else {
                    objectiveWeek = objective/1000;
                    percentCompleteWeek = (current / objective) * 100;
                }
            }else {
                float objective = goals.get(i).getValue();
                float current = goals.get(i).getCurrent();
                if (goals.get(i).getRecurence() == 1){
                    percentStepDay = current/objective;
                }
            }
        }
        if (percentCompleteDay > 100){
            percentCompleteDay = 100;
        }
        if (percentCompleteWeek > 100){
            percentCompleteWeek = 100;
        }
        percentCompleteDay = percentCompleteDay/100;
        percentCompleteWeek = percentCompleteWeek/100;
        //System.out.println("DAY"+day);
        int daysToFinishGoal = 7-(day-1);
        float weekPercentageToFinish = 1-percentCompleteWeek;
        float X = (weekPercentageToFinish/daysToFinishGoal);
        accuracy = (X - ((X * percentCompleteDay)));
        // System.out.println("DAYS: " + daysToFinishGoal);
        //  System.out.println("X: " + X);
        //  System.out.println("ACCURACY: " + accuracy); //
        //  System.out.println("OBJECTIVE: " + objectiveWeek); //
        float adviseDistance = objectiveWeek*accuracy;
        // System.out.println("AUX: " + adviseDistance); //

        if (adviseDistance <= 2){
            for(int j = 0; j < routes.size() ;j++){
                if (routes.get(j).getSize() == 1){
                    finalRoutes.add(routes.get(j));
                }
            }
        }
        if (adviseDistance > 2 && adviseDistance < 3.5){
            for(int j = 0; j < routes.size() ;j++){
                if (routes.get(j).getSize() == 2){
                    finalRoutes.add(routes.get(j));
                }
            }
        }
        if (adviseDistance >= 3.5){
            for(int j = 0; j < routes.size() ;j++){
                if (routes.get(j).getSize() == 3){
                    finalRoutes.add(routes.get(j));
                }
            }
        }

        if (finalRoutes != null) {
            Singleton.getInstance().setRoutes(finalRoutes);
        }

        if (percentCompleteDay == 1 && percentStepDay >= 1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Hoje já completou todos os seus desafios")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    })
                    .setTitle("Let's go running");
            AlertDialog d = builder.create();
            d.show();
        }

    }

    private void addRoutes() {
        routes = new LinkedList<>();
        String text = readFile("route_3.txt");
        routes.add(createRoute(text, 1,"trilho IPLEIRIA"));
        text = readFile("route_4.txt");
        routes.add(createRoute(text, 1,"trilho do liz"));

        text = readFile("route_1.txt");
        routes.add(createRoute(text, 2,"trilho do liz médio"));
        text = readFile("route_2.txt");
        routes.add(createRoute(text, 2,"trilho da volta"));

        text = readFile("route_5.txt");
        routes.add(createRoute(text, 3,"trilho dos marrazes"));

        text = readFile("route_6.txt");
        routes.add(createRoute(text, 3,"trilho dos hospital"));

        Singleton.getInstance().setRoutes(routes);
    }

    private Route createRoute(String text, int size,String name){
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

        Route r = new Route(start, end, wayPoints, size,name );
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

    private void createCircleGoals() {
        ListView l = (ListView) findViewById(R.id.listviewCircles);
        CircleAdapter adapter;
        adapter = new CircleAdapter(NewChallengeActivity.this, 0, goals);
        l.setAdapter(adapter);
    }

    public static void printData(DataReadResponse dataReadResult) {

        if (dataReadResult.getBuckets().size() == 1) {
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    dumpDataSet(dataSet);
                }
            }
        } else if (dataReadResult.getBuckets().size() > 1) {
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    dumpDataSetForHourActivity(dataSet);
                }
                contHour++;
            }

            Singleton.getInstance().setLastActivityHour(hourMaxActivity);
        }

        // [END parse_read_data_result]
    }

    public void printDataChallenge(DataReadResponse dataReadResult) {
        for (Bucket bucket : dataReadResult.getBuckets()) {
            List<DataSet> dataSets = bucket.getDataSets();
            for (DataSet dataSet : dataSets) {
                dumpDataSetForChallenge(dataSet);
                if (distannceDay == -10){
                    distannceDay = 0;
                }
                if (distanceAllweek == -10){
                    distanceAllweek = 0;
                }
            }
        }
        accessGoogleFitGoals();
    }

    private static void dumpDataSet(DataSet dataSet) {

        for (DataPoint dp : dataSet.getDataPoints()) {
            for (Field field : dp.getDataType().getFields()) {
                if (field.getName().equals("distance")) {
                    int distanceValue = (int) dp.getValue(field).asFloat();
                    distance = (float) (distanceValue / 1000.0);
                }
                if (field.getName().equals("calories")) {
                    calories = (int) dp.getValue(field).asFloat();
                }
            }
        }

        if (dayName.equals( "domingo") || dayName.equals( "sábado")){
            distanceText.setText("No ultimo "+ dayName+ " percorreu " + String.format("%.2f", distance) + " km e perdeu " + calories + " calorias.");
        }else {
            distanceText.setText("Na ultima " + dayName + " percorreu " + String.format("%.2f", distance) + " km e perdeu " + calories + " calorias.");
        }
        distanceText.setGravity(Gravity.CENTER);
    }

    private static void dumpDataSetForChallenge(DataSet dataSet) {

        for (DataPoint dp : dataSet.getDataPoints()) {
            for (Field field : dp.getDataType().getFields()) {

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
        for (DataPoint dp : dataSet.getDataPoints()) {

            for (Field field : dp.getDataType().getFields()) {
                if (dp.getValue(field).asFloat() > maxActivity) {
                    maxActivity = dp.getValue(field).asFloat();
                    hourMaxActivity = contHour;
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
                                tempText.setText("Estão " + String.format("%.0f", temp) + "ºC e não está a chover, deve aproveitar para" +
                                        " ir praticar exercício físico.");
                                tempText.setGravity(Gravity.CENTER);
                            } else {
                                isRainning = true;
                                imageSport.setImageResource(R.drawable.ic_workout);
                                textChallenge.setText("Aproveite faça desporto em casa.");
                           //     btnAcceptChallenge.setVisibility(View.INVISIBLE);
                                imageCondtions.setImageResource(retrieveConditionImage(conditions.get(i)));
                                tempText.setText("Estão " + String.format("%.0f", temp) + " ºC  mas está a chover.");
                                tempText.setGravity(Gravity.CENTER);
                               // return;
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(view, "Error: Cant Get Weather", Snackbar.LENGTH_SHORT).show();
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
                Snackbar.make(view, "Error: high accuracy location mode must be enabled in the device.", Snackbar.LENGTH_SHORT).show();
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
                return R.drawable.ic_rainny_day;
            case Weather.CONDITION_RAINY:
                return R.drawable.ic_rainny_day;
            case Weather.CONDITION_SNOWY:
                return R.drawable.ic_rainny_day;
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

}
