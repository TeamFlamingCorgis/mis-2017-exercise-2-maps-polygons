package com.example.zeroliam.mapsandpolygons;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //Declare elements here
    private GoogleMap mMap;
    public static final String MAPS_AND_POLYGONS_PREFS = "MapsAndPolygonsPrefs";
    public static final String markerTitle = "markerTitleKey";
    public static final String markerLocation = "markerLocationKey";
    SharedPreferences sharedprefs;
    private Button goToBtn;
    private Button polygonBtn;
    private EditText et;
    private ArrayList<Marker> polyMarkers;
    MarkerOptions tricentroid, regcentroid;
    Marker triCenter, regCenter;
    private Polygon shape;
    private int polyPoints;
    private boolean isDrawing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if(googleServicesAvailable()){
            Toast.makeText(this, "Hi there!", Toast.LENGTH_SHORT).show();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Call elements from the activity xml
        //The onClick method call is defined in the xml view, cool isn't it? :D
        goToBtn = (Button) findViewById(R.id.goBtn);
        polygonBtn = (Button) findViewById(R.id.createPolyBtn);
        et=(EditText) findViewById(R.id.inputTxt);
        sharedprefs = MapsActivity.this.getSharedPreferences(MAPS_AND_POLYGONS_PREFS, Context.MODE_PRIVATE);

    }

    /**
     * *****************************************************
     * Method name: googleServicesAvailable
     * Modifier:    public
     * Purpose:     Tries to connect to Google Services in order to make the map work
     * Parameters:  none
     * Returns:     boolean
     * *****************************************************
     */
    public boolean googleServicesAvailable()
    {
        GoogleApiAvailability api=GoogleApiAvailability.getInstance();
        int isAvailable=api.isGooglePlayServicesAvailable(this);
        if(isAvailable== ConnectionResult.SUCCESS){
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();

        }
        else {
            Toast.makeText(this, "Can't connect to Google Services", Toast.LENGTH_SHORT).show();
        }
        return false;
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
        polyMarkers = new ArrayList<Marker>();
        final Geocoder gc = new Geocoder(this);

        //Start in Weimar
        goToLocationZoom(50.9816511,11.3173627,10, null);

        //Setup the interaction
        if (mMap != null) {

            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    try{
                        List<Address> list= gc.getFromLocation(latLng.latitude, latLng.longitude, 1);
                        Address address = list.get(0);
                        String locality = address.getLocality();
                        MapsActivity.this.setMarker(latLng.latitude, latLng.longitude, locality);
                    }catch(IOException ioe){

                    }
                }
            });
            mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {

                }

                @Override
                public void onMarkerDrag(Marker marker) {

                }

                @Override
                public void onMarkerDragEnd(Marker marker) {

                    Geocoder gc = new Geocoder(MapsActivity.this);
                    LatLng ll = marker.getPosition();
                    List<Address> list = null;
                    try {
                        list = gc.getFromLocation(ll.latitude, ll.longitude, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address add = list.get(0);
                    marker.setTitle(add.getLocality());
                    marker.showInfoWindow();
                }
            });
            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    View v = getLayoutInflater().inflate(R.layout.info_window, null);

                    TextView tvLocality = (TextView) v.findViewById(R.id.tv_locality);
                    TextView tvLat = (TextView) v.findViewById(R.id.tv_lat);
                    TextView tvLng = (TextView) v.findViewById(R.id.tv_lng);

                    LatLng ll = marker.getPosition();
                    tvLocality.setText(marker.getTitle());
                    tvLat.setText("Latitude: " + ll.latitude);
                    tvLng.setText("Longitude: " + ll.longitude);

                    return v;
                }
            });
        }
    }

    /**
     * *******************************************************
     * **************  MAP METHODS GO HERE   *****************
     * *******************************************************
     */

    /**
     * *****************************************************
     * Method name: goToLocationZoom
     * Modifier:    private
     * Purpose:     Makes the map go to a specified location, puts a marker, zooms in
     * Parameters:  double, double, float
     * Returns:     void
     * *****************************************************
     */
    private void goToLocationZoom(double lat, double lng, float zoom, String placeName)
    {
        LatLng newLocation = new LatLng(lat,lng);
        CameraUpdate updateCam = CameraUpdateFactory.newLatLngZoom(newLocation, zoom);
        if(placeName != null){
            MapsActivity.this.setMarker(lat, lng, placeName);
        }
        mMap.moveCamera(updateCam);
    }

    /**
     * *****************************************************
     * Method name: geoLocate
     * Modifier:    public
     * Purpose:     Search and mark a place from the EditText
     * Parameters:  View
     * Returns:     void
     * *****************************************************
     */
    public void geoLocate(View view) throws IOException {
        String location= et.getText().toString();
        Geocoder gc = new Geocoder(this);
        List<Address> list= gc.getFromLocationName(location,1);
        Address address=list.get(0);
        String locality=address.getLocality();

        //Where are we now?
        Toast.makeText(this, locality, Toast.LENGTH_LONG).show();

        //Go to location, zoom, and mark it
        double lat=address.getLatitude();
        double lng=address.getLongitude();
        goToLocationZoom(lat,lng,10, locality);
    }

    /**
     * *****************************************************
     * Method name: setMarker
     * Modifier:    private
     * Purpose:     Sets and adds marker
     * Parameters:  String, double, double, ArrayList<Marker>
     * Returns:     void
     * *****************************************************
     */
    private void setMarker(double lat, double lng, String locality){
        //Setup the marker options
        MarkerOptions options = new MarkerOptions()
        .draggable(true)
        .title("Marker in " + locality)
        .position(new LatLng(lat, lng));

        //Adds marker
        polyMarkers.add(mMap.addMarker(options));
        //Writes it on the SharedPreferences
//        writeToPrefs(polyMarkers);
        for(int i = 0; i < polyMarkers.size(); i++){
            writeToPrefs(polyMarkers.get(i));
        }
        //Updates the number of points based on the number of markers
        setPolygonPoints(polyMarkers.size());
    }

    public void writeToPrefs(Marker markers){
        //Makes an editor to add ingo into the SharedPreferences
        SharedPreferences.Editor editor = sharedprefs.edit();

        //Writes each marker info into the sharedpreferences
        editor.putString(markerTitle, markers.getTitle());
        editor.putString(markerLocation, markers.getPosition().toString());
        //Commits the changes
        editor.apply();
        editor.commit();

    }

    /**
     * *******************************************************
     * ************  POLYGON METHODS GO HERE   ***************
     * *******************************************************
     */

    /**
     * *****************************************************
     * Method name: setPolygonPoints
     * Modifier:    public
     * Purpose:     Sets the amount of polygon points available
     * Parameters:  int
     * Returns:     void
     * *****************************************************
     */
    public void setPolygonPoints(int numPoints){
        polyPoints = numPoints;
    }

    /**
     * *****************************************************
     * Method name: getPolygonPoints
     * Modifier:    public
     * Purpose:     Gets the amount of polygon points available
     * Parameters:  none
     * Returns:     int
     * *****************************************************
     */
    public int getPolygonPoints(){
        return polyPoints;
    }

    /**
     * *****************************************************
     * Method name: createPolygon
     * Modifier:    public
     * Purpose:     Creates the shape only if there is no shape already. If it is, it deletes it.
     * Parameters:  View
     * Returns:     void
     * *****************************************************
     */
    public void createPolygon (View view) throws Exception {
        isDrawing = !isDrawing;

        if (isDrawing) {
            polygonBtn.setText("End Polygon");
            drawPolygon(getPolygonPoints(), polyMarkers);

            //Sets the marker for the center of the polygon
            if(polyMarkers.size() == 3){
                triCentroid(polyMarkers);
            }else if(polyMarkers.size() > 3){
                triCentroid(polyMarkers);
            }else{
                Toast.makeText(this, "THAT'S NOT A POLYGON!!", Toast.LENGTH_LONG).show();
            }

        } else {
            polygonBtn.setText("Start Polygon");
            removeEverything();
        }
    }

    /**
     * *****************************************************
     * Method name: drawPolygon
     * Modifier:    private
     * Purpose:     Draws the polygon and a stroke on it based on the markers position.
     * Parameters:  int, ArrayList<Marker>
     * Returns:     void
     * *****************************************************
     */
    private void drawPolygon(int polyPoints, ArrayList<Marker> markers) {
        PolygonOptions options = new PolygonOptions()
                .fillColor(0x330000FF)
                .strokeWidth(3)
                .strokeColor(Color.BLUE);

        for (int i = 0; i < polyPoints; i++) {
            options.add(markers.get(i).getPosition());
        }
        shape = mMap.addPolygon(options);
    }

    /**
     * *****************************************************
     * Method name: removeEverything
     * Modifier:    private
     * Purpose:     Clears everything from the map
     * Parameters:  none
     * Returns:     void
     * *****************************************************
     */
    private void removeEverything() {
        for (Marker marker : polyMarkers) {
            marker.remove();

        }
        polyMarkers.clear();
        shape.remove();
        shape = null;
        mMap.clear();
    }

    /**
     * Method name: calculateAreaPolygon
     * Modifier:    private
     * Purpose:     Calculates the area of the polygon. It also works for triangles!
     * Parameters:  ArrayList<Marker>
     * Returns:     double
     * *****************************************************
     * ******************* NOTES!!!! ***********************
     * MADE BUT NEVER USED! WHY? WELL:
     * (1)  Returns a vector and not the actual area in mts.
     * (2)  This works on a planar polygon, but the Earth is
     *      round (wow, I know) and that changes everything.
     * (3)  I'm keeping it because I did my research and my
     *      time is valuable (I got to love this math!)
     * *****************************************************
     */
    private double calculateAreaPolygon(ArrayList<Marker> markers) {
        //Variables for the area and the sum of the coordinates
        double totalArea = 0, coordSum = 0;
        //Now we need to take each vertex (given by the markers coordinates) and then
        //plug it into the formula for Area of a Polygon:
        // Area = abs( (vertex coordinates sum) / 2 ), in which
        //Vertex Coord Sum = [( x(n) * y(n + 1) ) - ( y(n) * x(n + 1) ) + ... +  x(n) * y(0) ) - ( y(n) * x(0) )]
        // ^^^ and the zero (0) at the end is because we need to close the polygon, so we go back to the 1st vertex.
        //Calling the markers position was too long and confusing so I split it into these vars
        //xPosNow = x(n); xPosNext = x(n + 1) and yPosNow = y(n); yPosNext = y(n + 1)
        double xPosNow, xPosNext, yPosNow, yPosNext;

        //Source? MATH! (http://www.mathopenref.com/coordpolygonarea.html)
        for(int i = 0; i < markers.size(); i++){
            //sum each vertex
            if(i < markers.size() - 1){
                xPosNow = markers.get(i).getPosition().latitude;
                yPosNext = markers.get(i + 1).getPosition().longitude;
                yPosNow = markers.get(i).getPosition().longitude;
                xPosNext = markers.get(i + 1).getPosition().latitude;
                coordSum += (xPosNow * yPosNext) - (yPosNow * xPosNext);
            }else{
                //now add the sum for the last vertex, a.k.a. "close the polygon yooo!"

                xPosNow = markers.get(i).getPosition().latitude;
                yPosNext = markers.get(0).getPosition().longitude;
                yPosNow = markers.get(i).getPosition().longitude;
                xPosNext = markers.get(0).getPosition().latitude;
                coordSum += (xPosNow * yPosNext) - (yPosNow * xPosNext);
            }
        }

        //Ok now give me the total area
        totalArea = Math.abs(coordSum / 2);

        return totalArea;
    }

    /**
     * *****************************************************
     * Method name: getArea
     * Modifier:    private
     * Purpose:     Calculates the area of the polygon using SphericalUtil (in square mts)
     * Parameters:  ArrayList<Marker>
     * Returns:     double
     * *****************************************************
     */
    private double getArea(ArrayList<Marker> markers){
        double totalAreaOut = 0;
        //Get all the latitudes and longitudes from the markers
        ArrayList<LatLng> markersPositions = new ArrayList<>();

        for(int i = 0; i < markers.size(); i++){
            markersPositions.add(new LatLng(markers.get(i).getPosition().latitude, markers.get(i).getPosition().longitude));
        }

        //Calculate the area with the Google Maps library:
        totalAreaOut = SphericalUtil.computeArea(markersPositions);

        return totalAreaOut;
    }

    /**
     * *****************************************************
     * Method name: triCentroid
     * Modifier:    private
     * Purpose:     Calculates the centroid of any triangle
     * Parameters:  ArrayList<Marker>
     * Returns:     void
     * *****************************************************
     */
    private void triCentroid(ArrayList<Marker> markers){
        //This one takes less time, we only have three vertices in a triangle
        //We just need the vertices sums and the centers
        double sumX = 0, sumY = 0, centX = 0, centY = 0;
        //Remember to display the area in kms, so format the number properly please
        double areaInKm = getArea(markers) / 1000;
        DecimalFormat kms = new DecimalFormat("#.000");
        //The string to display on the marker
        String printArea = kms.format(areaInKm);

        //Source? Yes, our old friend MATH! (http://www.mathopenref.com/coordcentroid.html)
        for(int i = 0; i < markers.size(); i++){
            sumX += markers.get(i).getPosition().latitude;
            sumY += markers.get(i).getPosition().longitude;
        }

        //Then, divide it by 3 since it's a triangle
        centX = sumX / markers.size();
        centY = sumY / markers.size();

        //Setup the marker options
        tricentroid = new MarkerOptions()
                .draggable(false)
                .title("Area: " + printArea + " Sq Km")
                .position(new LatLng(centX, centY));
        triCenter = mMap.addMarker(tricentroid);

        //Writes it on the SharedPreferences
        writeToPrefs(triCenter);
    }

    /**
     * *****************************************************
     * Method name: regCentroid
     * Modifier:    private
     * Purpose:     Calculates the centroid of any polygon
     * Parameters:  ArrayList<Marker>
     * Returns:     void
     * *****************************************************
     */
    private void regCentroid(ArrayList<Marker> markers){
        //Yes, we are looping again among the vertices so we need those position vars once more
        double xPosNow, xPosNext, yPosNow, yPosNext, vertXSum = 0, vertYSum = 0, centX = 0, centY = 0;
        double ourPolyArea = getArea(markers);
        //Display area in km, and format the number
        double areaInKm = getArea(markers) / 1000;
        DecimalFormat kms = new DecimalFormat("#.000");
        //The string to display on the marker
        String printArea = kms.format(areaInKm);

        //Now, given the formula to find C(x) and C(y):
        //C(x) = { [x(n) + x(n + 1)] * [ (x(n) * y(n + 1)) - (x(n + 1) * y(n)) ] } / Area * 6
        //C(y) = { [y(n) + y(n + 1)] * [ (x(n) * y(n + 1)) - (x(n + 1) * y(n)) ] } / Area * 6
        //we loop, get the sums, and remember that the last vertex is zero to close our polygon

        //Source? You guessed again, MATH! (https://en.wikipedia.org/wiki/Centroid#Centroid_of_a_polygon)
        for(int i = 0; i < markers.size(); i++){
            //sum each vertex
            if(i < markers.size() - 1){
                xPosNow = markers.get(i).getPosition().latitude;
                yPosNext = markers.get(i + 1).getPosition().longitude;
                yPosNow = markers.get(i).getPosition().longitude;
                xPosNext = markers.get(i + 1).getPosition().latitude;

                //Bring me that beautiful (long) formula here and execute it
                //For C(x) vertices needed, vertXSum
                vertXSum += (xPosNow + xPosNext) * ( (xPosNow * yPosNext) - (xPosNext * yPosNow) );
                //For C(y) vertices needed, vertXSum
                vertYSum += (yPosNow + yPosNext) * ( (xPosNow * yPosNext) - (xPosNext * yPosNow) );
            }else{
                //Close this polygon
                xPosNow = markers.get(i).getPosition().latitude;
                yPosNext = markers.get(0).getPosition().longitude;
                yPosNow = markers.get(i).getPosition().longitude;
                xPosNext = markers.get(0).getPosition().latitude;

                //For C(x) vertices needed, vertXSum
                vertXSum += (xPosNow + xPosNext) * ( (xPosNow * yPosNext) - (xPosNext * yPosNow) );
                //For C(y) vertices needed, vertXSum
                vertYSum += (yPosNow + yPosNext) * ( (xPosNow * yPosNext) - (xPosNext * yPosNow) );
            }
        }

        //Now, calculate the Centroid for X and Y
        centX = vertXSum / (ourPolyArea * 6);
        centY = vertYSum / (ourPolyArea * 6);

        //Setup the marker options
        regcentroid = new MarkerOptions()
                .draggable(false)
                .title("Area: " + printArea + " Sq Km")
                .position(new LatLng(centX, centY));
        regCenter = mMap.addMarker(regcentroid);
        writeToPrefs(regCenter);
    }


}

