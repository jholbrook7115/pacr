package me.gonen.pacr;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by gonen on 9/5/15.
 */
public class DirectionsHelper {

//    Calculate bearing between two locations
    public float getBearing(LatLng begin, LatLng end) {
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);

        if(begin.latitude < end.latitude && begin.longitude < end.longitude)
            return (float)(Math.toDegrees(Math.atan(lng / lat)));
        else if(begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (float)((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if(begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return  (float)(Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if(begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (float)((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);

        return -1;
    }

//    Calculate distance between two locations
    public float getDistanceInMeters(LatLng my_latlong,LatLng frnd_latlong){
        Location l1=new Location("One");
        l1.setLatitude(my_latlong.latitude);
        l1.setLongitude(my_latlong.longitude);

        Location l2=new Location("Two");
        l2.setLatitude(frnd_latlong.latitude);
        l2.setLongitude(frnd_latlong.longitude);

        float distance=l1.distanceTo(l2);
//        String dist=distance+" M";

        /*if(distance>1000.0f)
        {
            distance=distance/1000.0f;
            dist=distance+" KM";
        }*/
        return distance;
    }

//    Get direction between two locations
    public String getDirection(LatLng my_latlong,LatLng frnd_latlong) {
        double my_lat=my_latlong.latitude;
        double my_long=my_latlong.longitude;

        double frnd_lat=frnd_latlong.latitude;
        double frnd_long=frnd_latlong.longitude;

        double radians=getAtan2((frnd_long-my_long),(frnd_lat-my_lat));
        double compassReading = radians * (180 / Math.PI);

        String[] coordNames = {"North", "North-East", "East", "South-East", "South", "South-West", "West", "North-West", "North"};
        int coordIndex = (int) Math.round(compassReading / 45);

        if (coordIndex < 0) {
            coordIndex = coordIndex + 8;
        };

        return coordNames[coordIndex]; // returns the coordinate value
    }

    public double getAtan2(double longi,double lat) {
        return Math.atan2(longi, lat);
    }

}
