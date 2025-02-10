package cz.cuni.mff.hubinkok.totravel

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity

class AddByCoordinatesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_by_coordinates)

        val latitude = arrayOf("N", "S")
        val longitude = arrayOf("E", "W")

        val latitudeAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, latitude)
        val longitudeAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, longitude)

        val latitudeDropdownMenu: AutoCompleteTextView = findViewById(R.id.latitudeAutoComplete)
        latitudeDropdownMenu.setAdapter(latitudeAdapter)
        latitudeDropdownMenu.setOnItemClickListener { _, _, position, _ ->
            val selectedLatitude = latitude[position]
        }

        val longitudeDropdownMenu: AutoCompleteTextView = findViewById(R.id.longitudeAutoComplete)
        longitudeDropdownMenu.setAdapter(longitudeAdapter)
        longitudeDropdownMenu.setOnItemClickListener { _, _, position, _ ->
            val selectedLongitude = longitude[position]
        }
    }
}