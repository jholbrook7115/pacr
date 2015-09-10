package me.gonen.pacr;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DecimalFormat;

/**
 * Created by jeff on 9/9/15.
 */


public class MapFragment extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private GoogleMap mGoogleMap;

    private double latitude;
    private double longitude;
    private LatLng myLatLng;

    private TextView distanceLeft;
    private TextView speed;
    private float zoomLevel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayHomeAsUpEnabled(true);



        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();



        displayMap(latitude, longitude);
    }

    private void displayMap(double latitude, double longitude) {

        zoomLevel = 15;

        if(mGoogleMap == null) {

            mGoogleMap =((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

            if (mGoogleMap != null) {
                mGoogleMap.addMarker(new MarkerOptions().position(myLatLng).title("Origin").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                //Move camera to current location
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoomLevel));
                mGoogleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(CameraPosition position) {
                        zoomLevel = position.zoom;
                    }
                });
                mGoogleMap.setMyLocationEnabled(true);
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: // Back button in Action Bar.
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(1000); // Update location every second

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, MapFragment.this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        float[] distance_meters = new float[1];
        Location.distanceBetween(latitude, longitude, location.getLatitude(), location.getLongitude(), distance_meters);
        DecimalFormat dec_form = new DecimalFormat("###.##");
        distanceLeft.setText(getString(R.string.distance_textview_string) + " " + dec_form.format(distance_meters[0] * 0.000621371) );
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
