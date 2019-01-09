package pt.ipleiria.markmyrhythm.Activitty;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import pt.ipleiria.markmyrhythm.Model.Route;
import pt.ipleiria.markmyrhythm.Model.Singleton;
import pt.ipleiria.markmyrhythm.R;
import pt.ipleiria.markmyrhythm.Util.MapsHelper;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Double longitude;
    private Double latitude;
    private static final int REQUEST_CODE_FLPERMISSION = 20;
    private LinkedList<Route> routes;
    private Route nearestRoute = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Bundle extras = getIntent().getExtras();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        latitude = (Double) extras.get("latitude");
        longitude = (Double) extras.get("longitude");
        String firstLocation = latitude + "," + longitude;

        routes = Singleton.getInstance().getRoutes();

        Double lastDistance = 9999999.0;
        for (int i = 0;i < routes.size();i++){
            String latString = routes.get(i).getStart().split(",")[0];
            Double lat = Double.parseDouble(latString);

            String longString = routes.get(i).getStart().split(",")[1];
            Double lon = Double.parseDouble(longString);
            Double auxDistance = MapsHelper.distance(latitude, lat, longitude, lon);
            if(lastDistance > auxDistance ){
                lastDistance = auxDistance;
                nearestRoute = routes.get(i);
            }
        }

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera

        String latString = nearestRoute.getStart().split(",")[0];
        Double lat = Double.parseDouble(latString);

        String longString = nearestRoute.getStart().split(",")[1];
        Double lon = Double.parseDouble(longString);

        LatLng myLocation = new LatLng(lat, lon);
        checkFineLocationPermission();
        mMap.setMyLocationEnabled(true);
        //mMap.addMarker(new MarkerOptions().position(myLocation).title("Marker in My Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 13));
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);


        FetchUrl distanceBetweenTwoPoints = new FetchUrl();
         distanceBetweenTwoPoints.execute("https://maps.googleapis.com/maps/api/directions/json?" +
              "origin="+nearestRoute.getStart()+"&destination="+nearestRoute.getEnd()+
                 "&waypoints="+nearestRoute.getWayPoints()+
                 "&avoid=highways&mode=walking" +
                 "&key=" +
                "AIzaSyDwkI3PFysOsI8y6616OPFQLxQnfmdaTXM");
    }

    private void checkFineLocationPermission() {
        if (ContextCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MapsActivity.this,
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

    class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);

                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }

        private String downloadUrl(String strUrl) throws IOException {
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(strUrl);

                // Creating an http connection to communicate with url
                urlConnection = (HttpURLConnection) url.openConnection();

                // Connecting to url
                urlConnection.connect();

                // Reading data from url
                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuffer sb = new StringBuffer();

                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                data = sb.toString();
                Log.d("downloadUrl", data.toString());
                br.close();

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            } finally {
                iStream.close();
                urlConnection.disconnect();
            }
            return data;
        }
    }

    class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask", "Executing routes");
                Log.d("ParserTask", routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.BLUE);

                Log.d("onPostExecute", "onPostExecute lineoptions decoded");
            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                mMap.addPolyline(lineOptions);
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
            builder.setMessage("Quer fazer o " + nearestRoute.getName()+  " ?" + "\n " + "Distância: " + nearestRoute.getTotalDistance() + "metros.")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    })
                    .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setTitle("Let's go running");
            AlertDialog d = builder.create();
            d.show();

        }


    }

    class DataParser {

        /**
         * Receives a JSONObject and returns a list of lists containing latitude and longitude
         */
        public List<List<HashMap<String, String>>> parse(JSONObject jObject) {

            int totalDistanceMeters =  0;
            List<List<HashMap<String, String>>> routes = new ArrayList<>();
            JSONArray jRoutes;
            JSONArray jLegs;
            JSONArray jSteps;
            JSONObject jDistance;

            try {

                jRoutes = jObject.getJSONArray("routes");

                /** Traversing all routes */
                for (int i = 0; i < jRoutes.length(); i++) {
                    jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                    List path = new ArrayList<>();

                    /** Traversing all legs */
                    for (int j = 0; j < jLegs.length(); j++) {

                        jDistance = ((JSONObject) jLegs.get(j)).getJSONObject("distance");
                        totalDistanceMeters += jDistance.getInt("value");

                        jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");
                        /** Traversing all steps */
                        for (int k = 0; k < jSteps.length(); k++) {
                            String polyline = "";
                            polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                            List<LatLng> list = decodePoly(polyline);

                            /** Traversing all points */
                            for (int l = 0; l < list.size(); l++) {
                                HashMap<String, String> hm = new HashMap<>();
                                hm.put("lat", Double.toString((list.get(l)).latitude));
                                hm.put("lng", Double.toString((list.get(l)).longitude));
                                path.add(hm);
                            }
                        }
                        routes.add(path);
                    }
                    System.out.println("Distance: " + totalDistanceMeters);
                    nearestRoute.setTotalDistance(totalDistanceMeters);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
            }


            return routes;
        }


        /**
         * Method to decode polyline points
         * Courtesy : https://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
         */
        private List<LatLng> decodePoly(String encoded) {

            List<LatLng> poly = new ArrayList<>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }

            return poly;
        }
    }

}

class DistanceBetweenTwoPoints extends AsyncTask<String, Void, String> {

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

            //Toast.makeText(MapsActivity.this, result, Toast.LENGTH_LONG).show();
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