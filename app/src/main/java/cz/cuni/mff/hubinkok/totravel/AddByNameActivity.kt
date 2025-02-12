package cz.cuni.mff.hubinkok.totravel

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class AddByNameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_by_name)

        val nameEditText = findViewById<EditText>(R.id.nameEdit)

        val submitButton = findViewById<Button>(R.id.nameSubmitButton)
        submitButton.setOnClickListener {
            val name = nameEditText.text.toString()

            lifecycleScope.launch {
                try {
                    addPointByName(name)

                    val intent = Intent(applicationContext, MainActivity::class.java)
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                }
                catch (_: NoSuchElementException) {
                    findViewById<View>(android.R.id.content).post {
                        Snackbar
                            .make(findViewById(android.R.id.content), "Place not found", Snackbar.LENGTH_SHORT)
                            .setAnchorView(R.id.nameEdit)
                            .show()
                    }
                }
            }
        }
    }
}