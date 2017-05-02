package com.example.zeroliam.mapsandpolygons;

import android.app.Dialog;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if(googleServicesAvailable()){
            Toast.makeText(this, "Perfect", Toast.LENGTH_SHORT).show();
        }
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public boolean googleServicesAvailable()
    {
        GoogleApiAvailability api=GoogleApiAvailability.getInstance();
        int isAvailable=api.isGooglePlayServicesAvailable(this);
        if(isAvailable== ConnectionResult.SUCCESS){
            return true;
        }
        else if(api.isUserResolvableError(isAvailable)){
            Dialog dialog= api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        }
        else {
            Toast.makeText(this, "can't connect to google service", Toast.LENGTH_SHORT).show();
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

        Button goToBtn = (Button) findViewById(R.id.goBtn);
        goToBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                try{
                    geoLocate(v);
                }catch(IOException ioe){
                    Log.e("LOL", "lol");
                }

            }
        });
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

    public void geoLocate(View view) throws IOException {
        EditText et=(EditText) findViewById(R.id.inputTxt);
        String location= et.getText().toString();
        Geocoder gc = new Geocoder(this);
        List<Address> list= gc.getFromLocationName(location,1);
        Address address=list.get(0);
        String locality=address.getLocality();

        Toast.makeText(this, locality, Toast.LENGTH_LONG).show();

        double lat=address.getLatitude();
        double lng=address.getLongitude();
        goToLocationZoom(lat,lng,10, locality);


    }
}

