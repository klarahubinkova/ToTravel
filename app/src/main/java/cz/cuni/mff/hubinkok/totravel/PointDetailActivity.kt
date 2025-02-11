package cz.cuni.mff.hubinkok.totravel

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class PointDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_point_detail)

        val id = intent.getIntExtra("id", -1)
        val point: Point = Points.getPointById(id)
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

            val intent = Intent(applicationContext, MainActivity::class.java)
            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }

        val deleteButton = findViewById<Button>(R.id.pointDeleteButton)
        deleteButton.setOnClickListener{
            Points.deletePointById(id)

            val intent = Intent(applicationContext, MainActivity::class.java)
            Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivity(intent)
        }
    }
}