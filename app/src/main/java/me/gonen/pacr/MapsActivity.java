package me.gonen.pacr;

import android.content.Context;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location currentLocation;

    private LatLng lastWaypoint;
    private float zoomLevel = 10;
    private Marker myMarker;
    private ArrayList<LatLng> route;
    private boolean routeDrawn = false;

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        setUpMapIfNeeded();

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

//        locationListener = getLocationListener();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 3, getLocationListener());
        currentLocation = new Location(LocationManager.NETWORK_PROVIDER);
        //currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
/*

        if (currentLocation != null) {
            initializeMapElements();
        } else {
            Toast.makeText(getApplicationContext(), "Location Service is off!", Toast.LENGTH_LONG).show();

        }
*/
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
        myMarker = mMap.addMarker(new MarkerOptions().position(myLatLng).title("Marker"));
        //Add path line to map
        addRouteWaypoint(new LatLng(myLatLng.latitude + 1, myLatLng.longitude + 1));

        //Move camera to current location
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, zoomLevel));
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                zoomLevel = position.zoom;
            }
        });

    }

    public void addRouteWaypoint(LatLng newWaypoint) {
        if (lastWaypoint == null) lastWaypoint = newWaypoint;
        mMap.addPolyline(new PolylineOptions()
                .add(lastWaypoint, newWaypoint)
                .width(10).color(Color.argb(75, 0, 0, 255)));
        lastWaypoint = newWaypoint;
    }

    private LocationListener getLocationListener() {
        if (locationListener == null)
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                    if (currentLocation == null) currentLocation = new Location(location);
                    else currentLocation.set(location);

                    double lat = 0;
                    double lon = 0;
                    if (location != null) {
                        lat = location.getLatitude();
                        lon = location.getLongitude();
                    }

                    LatLng myLatLng = new LatLng(lat, lon);

                    //Add marker
                    MarkerOptions defaultMarkerOption = new MarkerOptions().position(myLatLng);
                    if (myMarker == null) myMarker = mMap.addMarker(defaultMarkerOption);
                    else myMarker.setPosition(myLatLng);


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
        locationManager.requestSingleUpdate(locationManager.getBestProvider(new Criteria(), true), locationListener, Looper.myLooper());
        return locationListener;
    }

    private void drawRoute() {
        //Add path line to map
        addRouteWaypoint(new LatLng(currentLocation.getLatitude() + 1, currentLocation.getLongitude()));

        routeDrawn = true;
    }


    @Override
    public void onConnected(Bundle bundle) {
        currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        initializeMapElements();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
