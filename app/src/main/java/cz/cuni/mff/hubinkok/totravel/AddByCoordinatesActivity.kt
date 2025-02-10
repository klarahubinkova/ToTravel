package cz.cuni.mff.hubinkok.totravel

import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class AddByCoordinatesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_by_coordinates)

        val nameEditText = findViewById<EditText>(R.id.coordinatesNameEdit)

        val latitudeEditText = findViewById<EditText>(R.id.latitudeText)
        latitudeEditText.filters = arrayOf<InputFilter>(MinMaxFilter(0.0, 90.0))
        val longitudeEditText = findViewById<EditText>(R.id.longitudeText)
        longitudeEditText.filters = arrayOf<InputFilter>(MinMaxFilter(0.0, 180.0))

        val latitudeDirections = LatitudeDirection.entries.toTypedArray()
        val longitudeDirections = LongitudeDirection.entries.toTypedArray()
        var selectedLatitudeDirection: LatitudeDirection = LatitudeDirection.N
        var selectedLongitudeDirection: LongitudeDirection = LongitudeDirection.E

        val latitudeAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, latitudeDirections)
        val longitudeAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, longitudeDirections)

        val latitudeDropdownMenu: AutoCompleteTextView = findViewById(R.id.latitudeAutoComplete)
        latitudeDropdownMenu.setAdapter(latitudeAdapter)
        latitudeDropdownMenu.setOnItemClickListener { _, _, position, _ ->
            selectedLatitudeDirection = latitudeDirections[position]
        }

        val longitudeDropdownMenu: AutoCompleteTextView = findViewById(R.id.longitudeAutoComplete)
        longitudeDropdownMenu.setAdapter(longitudeAdapter)
        longitudeDropdownMenu.setOnItemClickListener { _, _, position, _ ->
            selectedLongitudeDirection = longitudeDirections[position]
        }

        val submitButton = findViewById<Button>(R.id.coordinatesSubmitButton)
        submitButton.setOnClickListener {
            val name: String = nameEditText.text.toString()
            val latitude: Double = latitudeEditText.text.toString().toDoubleOrNull() ?: 0.0
            val longitude: Double = longitudeEditText.text.toString().toDoubleOrNull() ?: 0.0

            addPointByCoordinates(name, latitude, longitude, selectedLatitudeDirection, selectedLongitudeDirection)
        }
    }

    inner class MinMaxFilter() : InputFilter {
        private var min: Double = 0.0
        private var max: Double = 0.0

        constructor(minValue: Double, maxValue: Double) : this() {
            min = minValue
            max = maxValue
        }

        override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dStart: Int, dEnd: Int): CharSequence? {
            try {
                val input = (dest.toString() + source.toString()).toDouble()
                if (isInRange(min, max, input)) {
                    return null
                }
            } catch (_: NumberFormatException) { }
            return ""
        }

        private fun isInRange(a: Double, b: Double, c: Double): Boolean {
            return if (b > a) c in a..b else c in b..a
        }
    }
}