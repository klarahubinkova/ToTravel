package cz.cuni.mff.hubinkok.totravel.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import cz.cuni.mff.hubinkok.totravel.MainActivity
import cz.cuni.mff.hubinkok.totravel.R
import cz.cuni.mff.hubinkok.totravel.ToTravelApplication
import cz.cuni.mff.hubinkok.totravel.viewmodels.PointsViewModel
import kotlinx.coroutines.launch

class AddByNameActivity : AppCompatActivity() {
    private lateinit var viewModel: PointsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_by_name)

        viewModel = ViewModelProvider(application as ToTravelApplication)[PointsViewModel::class.java]

        val nameEditText = findViewById<EditText>(R.id.nameEdit)

        val submitButton = findViewById<Button>(R.id.nameSubmitButton)
        submitButton.setOnClickListener {
            lifecycleScope.launch {
                val name = nameEditText.text.toString()
                viewModel.addPointByName(name)

                if (viewModel.errorMessage.value == null) {
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                }
            }
        }

        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                Snackbar.make(findViewById(android.R.id.content), it, Snackbar.LENGTH_SHORT)
                    .setAnchorView(R.id.nameEdit)
                    .show()
                viewModel.clearError()
            }
        }
    }
}