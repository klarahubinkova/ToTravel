package cz.cuni.mff.hubinkok.totravel.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import cz.cuni.mff.hubinkok.totravel.MainActivity
import cz.cuni.mff.hubinkok.totravel.R
import cz.cuni.mff.hubinkok.totravel.ToTravelApplication
import cz.cuni.mff.hubinkok.totravel.data.Point
import cz.cuni.mff.hubinkok.totravel.data.PointTag
import cz.cuni.mff.hubinkok.totravel.viewmodels.PointsViewModel

class PointDetailActivity : AppCompatActivity() {
    private lateinit var viewModel: PointsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_point_detail)

        viewModel = ViewModelProvider(application as ToTravelApplication)[PointsViewModel::class.java]

        val id = intent.getIntExtra("id", -1)
        val point: Point = viewModel.getPointById(id)
            ?: Point(id, 0.0, 0.0, "")

        val nameEdit = findViewById<EditText>(R.id.pointNameEdit)
        nameEdit.setText(point.name)

        val tags = PointTag.entries.toTypedArray()
        val tagAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tags)

        val tagDropdownMenu: AutoCompleteTextView = findViewById(R.id.tagAutoComplete)
        tagDropdownMenu.setText(point.tag.name)
        tagDropdownMenu.setAdapter(tagAdapter)
        tagDropdownMenu.setOnItemClickListener { _, _, position, _ ->
            point.tag = tags[position]
        }

        val noteEdit = findViewById<EditText>(R.id.noteEdit)
        noteEdit.setText(point.note)

        val submitButton = findViewById<Button>(R.id.pointSubmitButton)
        submitButton.setOnClickListener{
            point.name = nameEdit.text.toString()
            point.note = noteEdit.text.toString()
            viewModel.update()

            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        val deleteButton = findViewById<Button>(R.id.pointDeleteButton)
        deleteButton.setOnClickListener{
            viewModel.deletePointById(id)

            if (viewModel.errorMessage.value == null) {
                val intent = Intent(applicationContext, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                startActivity(intent)
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