package cz.cuni.mff.hubinkok.totravel

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class AddPointActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_point)

        val addByNameButton = findViewById<Button>(R.id.addByNameButton)
        addByNameButton.setOnClickListener{
            val intent = Intent(this, AddByNameActivity::class.java)
            startActivity(intent)
        }

        val addByCoordinatesButton = findViewById<Button>(R.id.addByCoordinatesButton)
        addByCoordinatesButton.setOnClickListener{
            val intent = Intent(this, AddByCoordinatesActivity::class.java)
            startActivity(intent)
        }
    }
}