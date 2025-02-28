package cz.cuni.mff.hubinkok.totravel

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import cz.cuni.mff.hubinkok.totravel.data.Point
import cz.cuni.mff.hubinkok.totravel.data.PointTag
import cz.cuni.mff.hubinkok.totravel.ui.AddPointActivity
import cz.cuni.mff.hubinkok.totravel.ui.PointDetailActivity
import cz.cuni.mff.hubinkok.totravel.viewmodels.PointsViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), OnMapReadyCallback, OnInfoWindowClickListener {
    private var map: GoogleMap? = null
    private lateinit var viewModel: PointsViewModel

    private lateinit var addButton: Button
    private lateinit var deleteButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(application as ToTravelApplication)[PointsViewModel::class.java]

        lifecycleScope.launch {
            viewModel.loadPoints(this@MainActivity)
        }

        addButton = findViewById(R.id.addButton)
        addButton.setOnClickListener{
            val intent = Intent(this, AddPointActivity::class.java)
            startActivity(intent)
        }

        deleteButton = findViewById(R.id.deleteButton)
        deleteButton.setOnClickListener{
            AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete all the points? This action cannot be undone.")
                .setPositiveButton("Delete") { _, _ ->
                    lifecycleScope.launch {
                        viewModel.deletePoints(this@MainActivity)
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        hideButtons()
        loadMapIfConnected()

        viewModel.pointsList.observe(this) { showPointsOnMap(it) }
    }

    private fun hideButtons() {
        addButton.visibility = View.GONE
        deleteButton.visibility = View.GONE
    }

    private fun showButtons() {
        addButton.visibility = View.VISIBLE
        deleteButton.visibility = View.VISIBLE
    }

    private fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    private fun loadMapIfConnected() {
        if (isNetworkConnected(this)) {
            loadMap()
        } else {
            AlertDialog.Builder(this)
                .setTitle("No Internet Connection")
                .setMessage("The app cannot be used without an internet connection. Please connect to the internet.")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                    if (isNetworkConnected(this)) {
                        loadMap()
                    } else {
                        finish()
                    }
                }
                .show()
        }
    }

    private fun loadMap() {
        showButtons()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.setOnInfoWindowClickListener(this)
        map = googleMap
    }

    override fun onInfoWindowClick(marker: Marker) {
        val intent = Intent(this, PointDetailActivity::class.java).apply {
            putExtra("id", marker.title?.toInt())
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch {
            viewModel.savePoints(this@MainActivity)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        lifecycleScope.launch {
            viewModel.savePoints(this@MainActivity)
        }
    }

    private fun showPointsOnMap(points: List<Point>) {
        map?.clear()

        for (p in points) {
            map?.addMarker(
                MarkerOptions()
                    .position(LatLng(p.latitude, p.longitude))
                    .title(p.id.toString())
                    .snippet(p.name)
                    .icon(getMarkerColor(p.tag))
            )
        }

        if (points.isNotEmpty()) {
            val bounds = LatLngBounds.builder()
            points.forEach { bounds.include(LatLng(it.latitude, it.longitude)) }
            map?.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 100))
        }
    }

    private fun getMarkerColor(tag: PointTag): BitmapDescriptor {
        return when (tag) {
            PointTag.VISIT -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
            PointTag.PLANNED -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)
            PointTag.VISITED -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
            PointTag.FAVOURITE -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)
            PointTag.CUSTOM -> BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
        }
    }
}