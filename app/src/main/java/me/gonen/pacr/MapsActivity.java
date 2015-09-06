package me.gonen.pacr;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.PendingResult;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location currentLocation;
    private LatLng myLatLng;
    private DirectionsHelper directionsHelper;
    private float zoomLevel;
    private int pathLineWidth;
    private Marker myMarker;
    private Stack<Waypoint> waypoints = new Stack<>();
    private ArrayList<LatLng> route = new ArrayList<>();
    private Polyline polyline;
    private boolean routeDrawn = false;
    private boolean mapInitialized = false;
    private boolean followLocation = true;
    private final LatLng defaultCoordinates = new LatLng(39.9814367, -75.15507);
    private EditText myEditText;
    private float accuracy = -1;
    private float currentSpeed = -1;
    private DateTime arrivalTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        arrivalTime = new DateTime().withTime(13, 0, 0, 0);
        directionsHelper = new DirectionsHelper();
        setUpMapIfNeeded();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 3, getLocationListener());
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 3, getLocationListener());

        currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        myEditText = (EditText) findViewById(R.id.location_edittext); //make final to refer in onTouch

        myEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Toast.makeText(getApplicationContext(), "Searching for " + myEditText.getText().toString(), Toast.LENGTH_LONG).show();
                    EditText myTimeEdit = (EditText) findViewById(R.id.timepicker);
                    String today = Integer.toString(DateTime.now().getYear()) + "-" + Integer.toString(DateTime.now().getMonthOfYear()) + "-" + Integer.toString(DateTime.now().getDayOfMonth());
                    arrivalTime = DateTime.parse(today + "T" + myTimeEdit.getText().toString());
                    Toast.makeText(getApplicationContext(), "Arrival time set for " + arrivalTime.toString(), Toast.LENGTH_LONG).show();
                    getDirections(myEditText.getText().toString());
                }
                return false;
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

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

    private void setUpMap() {

        mMap.setMyLocationEnabled(true);

    }

    private void initializeMapElements() {
        double lat = currentLocation.getLatitude();
        double lon = currentLocation.getLongitude();
        myLatLng = new LatLng(lat, lon);

        //Set initial values for map variables
        zoomLevel = 15;
        pathLineWidth = 15;

        //Add marker
        myMarker = mMap.addMarker(new MarkerOptions().position(myLatLng).title("Origin").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        //Move camera to current location
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, zoomLevel));
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                zoomLevel = position.zoom;
            }
        });


        mapInitialized = true;

    }

    private void getDirections(String dest) {
        DirectionsService dService = new DirectionsService(mMap);
        dService.getDirections(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), dest, new PendingResult.Callback<List<LatLng>>() {
            @Override
            public void onResult(List<LatLng> result) {
                if (result == null || result.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Could not find route", Toast.LENGTH_LONG).show();
                    return;
                }

                mMap.clear();
                route.clear();
                waypoints.clear();

                route.addAll(result);
                analyzeRoute(route);
                drawRoute();
            }

            @Override
            public void onFailure(Throwable e) {

            }
        });
    }

    private void drawRoute() {
        polyline = mMap.addPolyline(new PolylineOptions()
                .addAll(route)
                .width(pathLineWidth).color(Color.argb(75, 0, 0, 255)));
        routeDrawn = true;
    }

    //Populates the waypoints ArrayList with the route waypoints, each with its relative distance from the destination
    private void analyzeRoute(ArrayList<LatLng> route) {
        if (route == null) route = this.route;
        double runningDistance = 0;
        if (waypoints == null) waypoints = new Stack<>();

        ArrayList<LatLng> reverseRoute = new ArrayList<>();
        reverseRoute.addAll(route);
        Collections.reverse(reverseRoute);

        LatLng previousWaypoint = new LatLng(0, 0);
        Iterator<LatLng> it = reverseRoute.iterator();
        if (it.hasNext())
            previousWaypoint = it.next();

        mMap.addMarker(new MarkerOptions().position(previousWaypoint).title("Destination").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        while (it.hasNext()) {
            LatLng waypoint = it.next();
            double distance = directionsHelper.getDistanceInMeters(waypoint, previousWaypoint);
            runningDistance += distance;
            previousWaypoint = waypoint;
            waypoints.push(new Waypoint(waypoint.latitude, waypoint.longitude, runningDistance));
        }
    }


    private LocationListener getLocationListener() {
        if (locationListener == null)
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (location == null) return;

                    if (currentLocation == null)
                        currentLocation = new Location(LocationManager.GPS_PROVIDER);

                    currentLocation.set(location);
                    accuracy = location.getAccuracy();

                    if (!mapInitialized) initializeMapElements();

                    double lat = 0;
                    double lon = 0;
                    lat = location.getLatitude();
                    lon = location.getLongitude();

                    myLatLng = new LatLng(lat, lon);

                    //Add marker
                    myMarker.setPosition(myLatLng);

                    //Move camera to current location
                    if (followLocation)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, zoomLevel));

                    TextView userText = (TextView) findViewById(R.id.eta_textview);
                    TextView reqSpeedText = (TextView) findViewById(R.id.speed_textview);

                    userText.setText("Distance");

                    if (waypoints == null || waypoints.empty()) return;

                    int ok = Color.argb(200, 200, 200, 50);
                    int bad = Color.argb(200, 200, 50, 50);
                    int good = Color.argb(200, 50, 255, 50);

                    currentSpeed = location.getSpeed();

                    int relevantColor = ok;
                    if((int)currentSpeed > (int)getRequiredSpeed()) relevantColor = good;
                    if((int)currentSpeed < (int)getRequiredSpeed()) relevantColor = bad;

                    reqSpeedText.setBackgroundColor(relevantColor);

                    if (directionsHelper.getDistanceInMeters(myLatLng, waypoints.peek().toGmsLatLng()) <= accuracy) {
                        Waypoint waypoint = waypoints.pop();
                        String rSpd = Double.toString(getRequiredSpeedKMH());
                        rSpd = rSpd.substring(0, rSpd.indexOf(".") + 3);

                        String cSpd = Float.toString(currentSpeed);
//                        cSpd = cSpd.substring(0, cSpd.indexOf(".") + 3);
                        if(cSpd.equals("0.0") && currentSpeed>0) cSpd = "~0.01";
                        reqSpeedText.setText(cSpd + "/" + rSpd + " km/h");

                        Toast.makeText(getApplicationContext(), "Got to waypoint " + waypoint.toString(), Toast.LENGTH_SHORT).show();
                    }

                    String str = Double.toString(getRemainingDistance() / 1000);
                    userText.setText(str.substring(0, str.indexOf(".") + 3).concat(" km"));


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


    private double getRemainingDistance() {
        return directionsHelper.getDistanceInMeters(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), waypoints.peek().toGmsLatLng()) + waypoints.peek().distanceToDestination;
    }

    private long getRemainingTime() {
        long remainingSeconds = (arrivalTime.getMillis() - DateTime.now().getMillis()) / 1000;
        return remainingSeconds;
    }

    private double getRequiredSpeed() {
        return getRemainingDistance() / getRemainingTime();
    }

    private double getRequiredSpeedKMH() {
        return getRemainingDistance() / getRemainingTime() * 3.6;
    }

    private double getRequiredSpeedMPH() {
        return getRequiredSpeedKMH() * 1.6;
    }

}

