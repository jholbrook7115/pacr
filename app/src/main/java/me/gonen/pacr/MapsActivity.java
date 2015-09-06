package me.gonen.pacr;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.PendingResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location currentLocation;
    private LatLng lastWaypoint;
    private DirectionsHelper directionsHelper;
    private float zoomLevel;
    private int pathLineWidth;
    private Marker myMarker;
    private ArrayList<Waypoint> waypoints = new ArrayList<>();
    private ArrayList<LatLng> route = new ArrayList<>();
    private Polyline polyline;
    private boolean routeDrawn = false;
    private boolean mapInitialized = false;
    private final LatLng defaultCoordinates = new LatLng(39.9814367, -75.15507);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        directionsHelper = new DirectionsHelper();
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
        zoomLevel = 15;
        pathLineWidth = 15;

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


        mapInitialized = true;

        /***********************************************************/
        //Test directions API call

        DirectionsService dService = new DirectionsService(mMap);
        dService.getDirections(myLatLng, defaultCoordinates, new PendingResult.Callback<List<LatLng>>() {
            @Override
            public void onResult(List<LatLng> result) {
                route.addAll(result);
                analyzeRoute(route);
                drawRoute();
            }

            @Override
            public void onFailure(Throwable e) {

            }
        });
        /***********************************************************/

    }

    /*public void addRouteWaypoint(LatLng newWaypoint) {
        mMap.addPolyline(new PolylineOptions().
                add(lastWaypoint, newWaypoint)
                .width(pathLineWidth).color(Color.argb(75, 0, 255, 0)));
        lastWaypoint = newWaypoint;
    }*/

    private void drawRoute() {
        polyline = mMap.addPolyline(new PolylineOptions()
                .addAll(route)
                .width(pathLineWidth).color(Color.argb(75, 0, 0, 255)));
        routeDrawn = true;
    }

    //Populates the waypoints ArrayList with the route waypoints, each with its relative distance from the destination
    private void analyzeRoute(ArrayList<LatLng> route){
        if(route == null) route = this.route;
        double runningDistance = 0;

        ArrayList<LatLng> reverseRoute = new ArrayList<>();
        reverseRoute.addAll(route);
        Collections.reverse(reverseRoute);

        Iterator<LatLng> it = reverseRoute.iterator();
        LatLng previousWaypoint = it.next();

        while(it.hasNext()){
            LatLng waypoint = it.next();
            double distance = directionsHelper.getDistanceInMeters(waypoint, previousWaypoint);
            runningDistance+=distance;
            previousWaypoint = waypoint;
            waypoints.add(new Waypoint(waypoint.latitude, waypoint.longitude, runningDistance));
        }
        Collections.reverse(waypoints);

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

//                    if (!routeDrawn) drawRoute();

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


}

