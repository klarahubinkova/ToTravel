package cz.cuni.mff.hubinkok.totravel

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class AddByNameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_by_name)

        val nameEditText = findViewById<EditText>(R.id.nameEdit)

        val submitButton = findViewById<Button>(R.id.nameSubmitButton)
        submitButton.setOnClickListener {
            val name = nameEditText.text.toString()

            addPointByName(name)
        }
    }
}