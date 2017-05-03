package com.example.zeroliam.mapsandpolygons;

import android.app.Dialog;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.R.id.list;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //Declare elements here
    private GoogleMap mMap;
    private Button goToBtn;
    private Button polygonBtn;
    private EditText et;

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
    }

    /**
     * Method name: googleServicesAvailable
     * Modifier:    public
     * Purpose:     Tries to connect to Google Services in order to make the map work
     * Parameters:  none
     * Returns:     boolean
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

        goToLocationZoom(50.9816511,11.3173627,10, "Weimar");
        if (mMap != null) {

            mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    MapsActivity.this.setMarker("local", latLng.latitude, latLng.longitude);
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
     * Method name: goToLocation
     * Modifier:    private
     * Purpose:     Makes the map go to a specified location, adds marker
     * Parameters:  double, double, String
     * Returns:     void
     */
    private void goToLocation(double lat, double lng, String placeName)
    {
        LatLng newLocation = new LatLng(lat,lng);
        CameraUpdate updateCam = CameraUpdateFactory.newLatLng(newLocation);
        mMap.addMarker(new MarkerOptions().position(newLocation).title("Marker in " + placeName));
        mMap.moveCamera(updateCam);
    }

    /**
     * Method name: goToLocation
     * Modifier:    private
     * Purpose:     Makes the map go to a specified location, puts a marker, zooms in
     * Parameters:  double, double, float
     * Returns:     void
     */
    private void goToLocationZoom(double lat, double lng, float zoom, String placeName)
    {
        LatLng newLocation = new LatLng(lat,lng);
        CameraUpdate updateCam = CameraUpdateFactory.newLatLngZoom(newLocation, zoom);
        mMap.addMarker(new MarkerOptions().position(newLocation).title("Marker in " + placeName));
        mMap.moveCamera(updateCam);
    }


    /**
     * Method name: geoLocate
     * Modifier:    public
     * Purpose:     Search and mark a place from the EditText
     * Parameters:  View
     * Returns:     void
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

    Circle circle;
//    Marker marker1;
//    Marker marker2;
//    Polyline line;

    ArrayList<Marker> markers = new ArrayList<Marker>();
    static final int POLYGON_POINTS = 6;
    Polygon shape;

    private void setMarker(String locality, double lat, double lng) {

        if (markers.size() == POLYGON_POINTS) {
            removeEverything();
        }
        MarkerOptions options = new MarkerOptions()
                .title(locality)
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher))
                .position(new LatLng(lat, lng));

        markers.add(mMap.addMarker(options));

        if (markers.size() == POLYGON_POINTS) {
            drawPolygon();
        }
//        if (marker1 == null) {
//            marker1 = mMap.addMarker(options);
//        } else if (marker2 == null) {
//            marker1 = mMap.addMarker(options);
//            drawLine();
//        } else {
//            removeEverything();
//            marker1 = mMap.addMarker(options);
//        }
//        circle = drawCircle(new LatLng(lat, lng));
//    private Circle drawCircle(LatLng latLng) {
//
//        CircleOptions options=new CircleOptions()
//                        .center(latLng)
//                        .radius(1000)
//                        .fillColor(0x33FF0000)
//                        .strokeColor(Color.BLUE)
//                        .strokeWidth(3);
//        return mMap.addCircle(options);
//    }


    }

    private void drawPolygon() {
        PolygonOptions options = new PolygonOptions()
                .fillColor(0x330000FF)
                .strokeWidth(3)
                .strokeColor(Color.BLUE);

        for (int i = 0; i < POLYGON_POINTS; i++) {
            options.add(markers.get(i).getPosition());
        }
        shape = mMap.addPolygon(options);
    }
//
//    private void drawLine() {
//
//        PolylineOptions options = new PolylineOptions()
//                        .add(marker1.getPosition())
//                        .add(marker2.getPosition())
//                        .color(Color.BLUE)
//                        .width(3);
//
//        line=mMap.addPolyline(options);
//    }

    private void removeEverything() {
        for (Marker marker : markers) {
            marker.remove();

        }
        markers.clear();
        shape.remove();
        shape = null;
    }
}

