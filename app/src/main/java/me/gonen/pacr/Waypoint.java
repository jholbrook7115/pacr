package me.gonen.pacr;


import com.google.maps.model.LatLng;

/**
 * Created by gonen on 9/5/15.
 */
public class Waypoint extends LatLng {
    double distanceToDestination;
    public Waypoint(LatLng latLng) {
        super(latLng.lat, latLng.lng);
    }

    public Waypoint(double latitude, double longitude) {
        super(latitude, longitude);
    }

    public Waypoint(double latitude, double longitude, double distanceToDestination) {
        super(latitude, longitude);
        this.distanceToDestination = distanceToDestination;
    }

    public com.google.android.gms.maps.model.LatLng toGmsLatLng(){
        return new com.google.android.gms.maps.model.LatLng(this.lat, this.lng);
    }

    public LatLng toLatLng(){
        return new LatLng(this.lat, this.lng);
    }

}
