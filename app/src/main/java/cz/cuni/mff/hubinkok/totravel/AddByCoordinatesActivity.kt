package cz.cuni.mff.hubinkok.totravel

import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class AddByCoordinatesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_by_coordinates)

        val latitudeEditText = findViewById<EditText>(R.id.latitudeText)
        latitudeEditText.filters = arrayOf<InputFilter>(MinMaxFilter(0, 90))
        val longitudeEditText = findViewById<EditText>(R.id.longitudeText)
        longitudeEditText.filters = arrayOf<InputFilter>(MinMaxFilter(0, 180))

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

    inner class MinMaxFilter() : InputFilter {
        private var intMin: Int = 0
        private var intMax: Int = 0

        constructor(minValue: Int, maxValue: Int) : this() {
            this.intMin = minValue
            this.intMax = maxValue
        }

        override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dStart: Int, dEnd: Int): CharSequence? {
            try {
                val input = Integer.parseInt(dest.toString() + source.toString())
                if (isInRange(intMin, intMax, input)) {
                    return null
                }
            } catch (_: NumberFormatException) { }
            return ""
        }

        // Check if input c is in between min a and max b and
        // returns corresponding boolean
        private fun isInRange(a: Int, b: Int, c: Int): Boolean {
            return if (b > a) c in a..b else c in b..a
        }
    }
}