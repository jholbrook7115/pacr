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
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsActivity extends FragmentActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private LocationManager locationManager;
//    private LocationListener locationListener;
    private Location currentLocation;

    private LatLng lastWaypoint;

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
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, this/*locationListener*/);

        //currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if(currentLocation != null) {
            initializeMapElements();
        }
        else {
            Toast.makeText(getApplicationContext(), "Location Service is off!", Toast.LENGTH_LONG).show();

        }
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

    private void initializeMapElements(){
        double lat = currentLocation.getLatitude();
        double lon = currentLocation.getLongitude();
        LatLng myLatLng = new LatLng(lat, lon);
        lastWaypoint = myLatLng;
        //Add marker
        mMap.addMarker(new MarkerOptions().position(myLatLng).title("Marker"));

        //Add path line to map
        addRouteWaypoint(new LatLng(myLatLng.latitude+1, myLatLng.longitude+1));

        //Move camera to current location
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 10));

    }
    public void addRouteWaypoint(LatLng newWaypoint) {
        mMap.addPolyline(new PolylineOptions().
                add(lastWaypoint, newWaypoint)
                .width(10).color(Color.BLUE));
        lastWaypoint = newWaypoint;
    }

    /*private LocationListener getLocationListener() {
        if (locationListener == null)
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    currentLocation.set(location);
                    double lat = 0;
                    double lon = 0;
                    if (location != null) {
                        lat = location.getLatitude();
                        lon = location.getLongitude();
                    }

                    LatLng myLatLng = new LatLng(lat, lon);

                    //Add marker
                    mMap.addMarker(new MarkerOptions().position(myLatLng).title("Marker"));

                    //Add path line to map
                    addPolyline();

                    //Move camera to current location
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(myLatLng, 10);
                    mMap.animateCamera(cameraUpdate);
                    locationManager.removeUpdates(this);
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
    }*/

    @Override
    public void onLocationChanged(Location location) {
        currentLocation.set(location);
        double lat = 0;
        double lon = 0;
        if (location != null) {
            lat = location.getLatitude();
            lon = location.getLongitude();
        }

        LatLng myLatLng = new LatLng(lat, lon);

        //Add marker
        mMap.addMarker(new MarkerOptions().position(myLatLng).title("Marker"));


        //Move camera to current location
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(myLatLng, 10);
        mMap.animateCamera(cameraUpdate);
//        locationManager.removeUpdates(this);
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

    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(getApplicationContext(), "Location Services Connected!", Toast.LENGTH_LONG).show();
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
