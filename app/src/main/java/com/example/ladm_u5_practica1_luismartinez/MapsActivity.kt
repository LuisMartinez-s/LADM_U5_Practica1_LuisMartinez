package com.example.ladm_u5_practica1_luismartinez

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    var baseRemota = FirebaseFirestore.getInstance()
    var marcadores=ArrayList<Data>()
    var lat=0.0
    var long=0.0
    var edificio=""
    lateinit var ubicacionCliente: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        var extra = intent.extras
        edificio=extra!!.get("data").toString()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),1)

        }
        ubicacionCliente= LocationServices.getFusedLocationProviderClient(this)

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
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        baseRemota.collection("tecnologico")
            .whereEqualTo("nombre",edificio)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    setTitle("Error: " + firebaseFirestoreException.message)
                    return@addSnapshotListener
                }//if
                var resultado = ""
                marcadores.clear()
                for (document in querySnapshot!!) {
                    var data = Data()

                    data.nombre = document.getString("nombre").toString()
                    data.posicion1 = document.getGeoPoint("posicion1")!!
                    data.posicion2 = document.getGeoPoint("posicion2")!!
                    resultado += data.toString() + "\n" + "\n"
                    marcadores.add(data)
                    lat=data.posicion1.latitude
                    long=data.posicion1.longitude
                }
                Toast.makeText(this, resultado, Toast.LENGTH_LONG).show()
                mMap = googleMap
                setTitle(edificio)

                // Add a marker in Sydney and move the camera
                val sydney = LatLng(lat,long)
                mMap.addMarker(MarkerOptions().position(sydney).title(edificio))
                mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
                mMap.uiSettings.isZoomControlsEnabled=true
                mMap.uiSettings.isMyLocationButtonEnabled=true
                mMap.isMyLocationEnabled=true
                ubicacionCliente.lastLocation.addOnSuccessListener {

                    if(it!=null){
                        val posMark=LatLng(lat,long)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(posMark,18f))
                    }
                }//addonsucc
            }//add snap
    }//onMap
}