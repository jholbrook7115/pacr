package me.gonen.pacr;

/**
 * Created by gonen on 9/5/15.
 */
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.TravelMode;
import com.google.maps.model.Unit;

import android.util.Log;

public class DirectionsService {
    DirectionsApi directionsApi;

    private GeoApiContext context;

    public DirectionsService(GeoApiContext context) {
        this.context = context
                .setQueryRateLimit(3)
                .setConnectTimeout(1, TimeUnit.SECONDS)
                .setReadTimeout(1, TimeUnit.SECONDS)
                .setWriteTimeout(1, TimeUnit.SECONDS);
    }

    public void testGetDirections() throws Exception {
        DirectionsRoute[] routes = DirectionsApi.getDirections(context, "Sydney, AU",
                "Melbourne, AU").await();
    }

    public void testBuilder() throws Exception {
        DirectionsRoute[] routes = DirectionsApi.newRequest(context)
                .mode(TravelMode.BICYCLING)
                .avoid(DirectionsApi.RouteRestriction.HIGHWAYS, DirectionsApi.RouteRestriction.TOLLS, DirectionsApi.RouteRestriction.FERRIES)
                .units(Unit.METRIC)
                .region("au")
                .origin("Sydney")
                .destination("Melbourne").await();
    }

    public void testTravelModeRoundTrip() throws Exception {
        DirectionsRoute[] routes = DirectionsApi.newRequest(context)
                .mode(TravelMode.BICYCLING)
                .origin("Town Hall, Sydney")
                .destination("Parramatta, NSW").await();
    }

    public void testResponseTimesArePopulatedCorrectly() throws Exception {
        DateTime now = new DateTime();
        DirectionsRoute[] routes = DirectionsApi.newRequest(context)
                .mode(TravelMode.TRANSIT)
                .origin("Town Hall, Sydney")
                .destination("Parramatta, NSW")
                .departureTime(now)
                .await();
}

    /**
     * A simple query from Toronto to Montreal.
     * {@url http://maps.googleapis.com/maps/api/directions/json?origin=Toronto&destination=Montreal}
     */
    public void testTorontoToMontreal() throws Exception {
        DirectionsRoute[] routes = DirectionsApi.newRequest(context)
                .origin("Toronto")
                .destination("Montreal").await();
    }

    /**
     * Going from Toronto to Montreal by bicycle, avoiding highways.
     * {@url http://maps.googleapis.com/maps/api/directions/json?origin=Toronto&destination=Montreal&avoid=highways&mode=bicycling}
     */
    public void testTorontoToMontrealByBicycleAvoidingHighways() throws Exception {
        DirectionsRoute[] routes = DirectionsApi.newRequest(context)
                .origin("Toronto")
                .destination("Montreal")
                .avoid(DirectionsApi.RouteRestriction.HIGHWAYS)
                .mode(TravelMode.BICYCLING)
                .await();
    }
}
