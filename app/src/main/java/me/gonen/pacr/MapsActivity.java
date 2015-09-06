package me.gonen.pacr;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsRoute;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location currentLocation;
    private LatLng lastWaypoint;
    private float zoomLevel = 10;
    private Marker myMarker;
    private ArrayList<LatLng> route = new ArrayList<>();
    private boolean routeDrawn = false;
    private boolean mapInitialized = false;
    private final LatLng defaultCoordinates = new LatLng(39.9814367, -75.15507);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        setUpMapIfNeeded();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 3, getLocationListener());

        currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        mMap.setMyLocationEnabled(true);

    }

    private void initializeMapElements() {
        double lat = currentLocation.getLatitude();
        double lon = currentLocation.getLongitude();
        LatLng myLatLng = new LatLng(lat, lon);

        //Set initial values for running value variables
        lastWaypoint = myLatLng;
        zoomLevel = 10;

        //Add marker
        myMarker = mMap.addMarker(new MarkerOptions().position(myLatLng).title("Origin"));

        //Move camera to current location
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, zoomLevel));
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                zoomLevel = position.zoom;
            }
        });

        /*for (float x = 0.0001f; x < 0.0100f; x += 0.0005f) {
            float y = x * x + 5 * x + 5;
            route.add(new LatLng(myLatLng.latitude + x / 10, myLatLng.longitude + y / 10));
        }
        DirectionsService ds = new DirectionsService(new GeoApiContext().setApiKey(getString(R.string.google_maps_key)));

        try {
            DirectionsRoute[] dsRoute = ds.testGetDirections();
            for(DirectionsRoute direction : dsRoute){
                addRouteWaypoint(new LatLng(direction.waypointOrder[0], direction.waypointOrder[1]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/

//        DirectionsService dService = new DirectionsService(new GeoApiContext().setApiKey(getString(R.string.google_maps_key)));

//        Log.d(getString(R.string.app_name),dHelper.getDirection(myLatLng, defaultCoordinates));

        mapInitialized = true;

        /***********************************************************/
        //Test directions API call

        // Getting URL to the Google Directions API
        String url = getDirectionsUrl(myLatLng, defaultCoordinates);

        DownloadTask downloadTask = new DownloadTask();

        // Start downloading json data from Google Directions API
        downloadTask.execute(url);
        /***********************************************************/

    }

    public void addRouteWaypoint(LatLng newWaypoint) {
        mMap.addPolyline(new PolylineOptions().
                add(lastWaypoint, newWaypoint)
                .width(10).color(Color.argb(75, 0, 0, 255)));
        lastWaypoint = newWaypoint;
    }

    private LocationListener getLocationListener() {
        if (locationListener == null)
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (currentLocation == null)
                        currentLocation = new Location(LocationManager.PASSIVE_PROVIDER);
                    currentLocation.set(location);
                    if (!mapInitialized) initializeMapElements();

                    double lat = 0;
                    double lon = 0;
                    if (location != null) {
                        lat = location.getLatitude();
                        lon = location.getLongitude();
                    }

                    LatLng myLatLng = new LatLng(lat, lon);

                    //Add marker
                    myMarker.setPosition(myLatLng);

                    if (!routeDrawn) drawRoute();

                    //Move camera to current location
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(myLatLng, zoomLevel);
                    mMap.animateCamera(cameraUpdate);

                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            };
        return locationListener;
    }

    private void drawRoute() {
        Iterator<LatLng> it = route.iterator();
        while (it.hasNext()) {
            //Add path line to map
            LatLng waypoint = it.next();
            addRouteWaypoint(waypoint);
        }
        routeDrawn = true;
    }


    /////////////////////////////////////

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String mode = "mode=walking";

        // Sensor enabled
        String sensor = "sensor=true";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }

    /**
     * A method to download json data from url
     */
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

            br.close();

        } catch (Exception e) {
            //Exception while downloading url
            Log.d("G_ERR","Error downloading data");
            throw e;
        } finally {
            if (iStream != null)
                iStream.close();
            if (urlConnection != null)
                urlConnection.disconnect();
        }
        return data;
    }


    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
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
                lineOptions.color(Color.argb(75,0,0,255));

            }

            // Drawing polyline in the Google Map for the i-th route
            mMap.addPolyline(lineOptions);
        }
    }
}

