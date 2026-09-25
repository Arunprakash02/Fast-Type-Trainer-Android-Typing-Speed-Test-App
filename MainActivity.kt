package com.fca.fasttypetrainer

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fca.fasttypetrainer.R

class MainActivity : AppCompatActivity() {

    private lateinit var editTextName: EditText
    private lateinit var spinnerPurpose: Spinner
    private lateinit var buttonStart: Button
//drop down vlaues default
    private val purposeList = listOf(
        "Select your purpose",
        "Interview preparation",
        "Coding practice",
        "Exam preparation",
        "Office work",
        "General typing improvement"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextName = findViewById(R.id.editTextName)
        spinnerPurpose = findViewById(R.id.spinnerPurpose)
        buttonStart = findViewById(R.id.buttonStart)

        setupPurposeSpinner()
        loadSavedUser()

        buttonStart.setOnClickListener {
            startPractice()
        }
    }
//drop down 
    private fun setupPurposeSpinner() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            purposeList
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinnerPurpose.adapter = adapter
    }

    //save
    private fun loadSavedUser() {
        val preferences = getSharedPreferences("fast_type_preferences", MODE_PRIVATE)

        val savedName = preferences.getString("user_name", "")
        val savedPurpose = preferences.getString("user_purpose", "")

        editTextName.setText(savedName)

        val savedPosition = purposeList.indexOf(savedPurpose)

        if (savedPosition >= 0) {
            spinnerPurpose.setSelection(savedPosition)
        }
    }

    //validation
    private fun startPractice() {
        val name = editTextName.text.toString().trim()
        val purpose = spinnerPurpose.selectedItem.toString()

        if (name.isEmpty()) {
            editTextName.error = "Please enter your name"
            editTextName.requestFocus()
            return
        }

        if (purpose == purposeList[0]) {
            Toast.makeText(
                this,
                "Please select your purpose",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val preferences = getSharedPreferences(
            "fast_type_preferences",
            MODE_PRIVATE
        )

        preferences.edit()
            .putString("user_name", name)
            .putString("user_purpose", purpose)
            .apply()

        val intent = Intent(this, PracticeActivity::class.java)
        startActivity(intent)
    }
}
