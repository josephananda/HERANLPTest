package com.jadeappstudio.heranlptest

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnCompleteListener
import kotlinx.android.synthetic.main.activity_location.*
import java.io.IOException
import java.util.*


class LocationActivity : AppCompatActivity() {

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this@LocationActivity)

        btnGetLocation.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this@LocationActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                showLocation()
            } else {
                ActivityCompat.requestPermissions(
                    this@LocationActivity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            }
        }
    }

    fun showLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.lastLocation.addOnCompleteListener(OnCompleteListener<Location> {
            var location: Location = it.result
            if (location != null) {
                var geocoder: Geocoder = Geocoder(this@LocationActivity, Locale.getDefault())
                try {
                    var addressList: List<Address> =
                        geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    tvLatitude.text = "Latitude: " + addressList.get(0).latitude
                    tvLongitude.text = "Longitude: " + addressList.get(0).longitude
                    tvAddressLine.text = "Alamat: " + addressList.get(0).getAddressLine(0)
                    tvLocality.text = "Kecamatan: " + addressList.get(0).locality
                    tvCountry.text = "Negara: " + addressList.get(0).countryName
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                Toast.makeText(this@LocationActivity, "Location null error", Toast.LENGTH_SHORT)
                    .show();
            }
        })
    }
}