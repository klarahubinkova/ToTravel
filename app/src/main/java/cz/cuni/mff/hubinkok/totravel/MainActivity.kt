package cz.cuni.mff.hubinkok.totravel

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), OnMapReadyCallback, OnInfoWindowClickListener {
    private var map: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        lifecycleScope.launch {
            withContext(Dispatchers.IO) { Points.loadPoints(this@MainActivity) }
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val addButton = findViewById<Button>(R.id.addButton)
        addButton.setOnClickListener{
            val intent = Intent(this, AddPointActivity::class.java)
            startActivity(intent)
        }

        val deleteButton = findViewById<Button>(R.id.deleteButton)
        deleteButton.setOnClickListener{
            AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete all the points? This action cannot be undone.")
                .setPositiveButton("Delete") { _, _ ->
                    Points.deletePoints(this)
                    showPointsOnMap()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.setOnInfoWindowClickListener(this)
        map = googleMap

        showPointsOnMap()
    }

    override fun onInfoWindowClick(marker: Marker) {
        val intent = Intent(this, PointDetailActivity::class.java).apply {
            putExtra("id", marker.title?.toInt())
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()

        Points.savePoints(this)

        showPointsOnMap()
    }

    override fun onDestroy() {
        super.onDestroy()
        Points.savePoints(this)
    }

    private fun showPointsOnMap() {
        map?.clear()

        for (p in Points.list) {
            map?.addMarker(
                MarkerOptions()
                    .position(LatLng(p.latitude, p.longitude))
                    .title(p.id.toString())
                    .snippet(p.name)
            )
        }

        if (Points.list.isNotEmpty()) {
            val bounds = LatLngBounds.builder()
            Points.list.forEach { bounds.include(LatLng(it.latitude, it.longitude)) }
            map?.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))
        }
    }
}