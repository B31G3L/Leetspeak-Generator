package com.beigel.leetSpeak_Generator

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        private const val SIMPLE = 0
        private const val EXTENDED = 1
        private const val CUSTOM = 2
    }

    private val plaintextAlphabet = charArrayOf('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z')

    private val simpleLeetAlphabet = arrayOf("4", "8", "C", "D", "3", "F", "6", "#", "1", "J", "K", "L", "M", "N", "0", "P", "Q", "R", "5", "7", "U", "V", "W", "X", "Y", "2")

    private val extendedLeetAlphabet = arrayOf("4", "8", "(", "|)", "3", "|=", "6", "#", "!", "_|", "|<", "1", "/\\/\\", "|\\|", "0", "9", "0_", "2", "5", "7", "|_|", "\\/", "\\/\\/", "><", "`/", "Z")

    private var activeMode = SIMPLE

    private lateinit var inputPlainText: EditText
    private lateinit var outputLeetText: TextView
    private lateinit var buttonSimple: Button
    private lateinit var buttonExtended: Button
    private lateinit var buttonCustom: Button
    private lateinit var buttonCopy: Button
    private lateinit var buttonEdit: Button
    private lateinit var buttonSave: Button
    private lateinit var tableTitle: TextView
    private lateinit var leetTable: TableLayout

    private lateinit var storage: SharedPreferences
    private var editableFields: Array<Array<EditText?>> = Array(13) { Array(2) { null } }
    private var displayFields: Array<Array<TextView?>> = Array(13) { Array(2) { null } }
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize SharedPreferences
        storage = getSharedPreferences("LeetSpeakCustom", MODE_PRIVATE)

        // Initialize UI elements
        inputPlainText = findViewById(R.id.inputPlainText)
        outputLeetText = findViewById(R.id.outputLeetText)
        buttonSimple = findViewById(R.id.buttonSimple)
        buttonExtended = findViewById(R.id.buttonExtended)
        buttonCustom = findViewById(R.id.buttonCustom)
        buttonCopy = findViewById(R.id.buttonCopy)
        buttonEdit = findViewById(R.id.buttonEdit)
        buttonSave = findViewById(R.id.buttonSave)
        tableTitle = findViewById(R.id.tableTitle)
        leetTable = findViewById(R.id.leetTable)

        // Set up text change listener
        inputPlainText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                updateOutput()
            }
        })

        // Set up button click listeners
        buttonSimple.setOnClickListener {
            setActiveMode(SIMPLE)
        }

        buttonExtended.setOnClickListener {
            setActiveMode(EXTENDED)
        }

        buttonCustom.setOnClickListener {
            checkCustomTranslations()
            setActiveMode(CUSTOM)
        }

        buttonCopy.setOnClickListener {
            copyToClipboard()
        }

        buttonEdit.setOnClickListener {
            switchToEditMode()
        }

        buttonSave.setOnClickListener {
            saveCustomTranslations()
        }

        // Check if custom translations exist, create defaults if needed
        checkCustomTranslations()

        // Set initial mode and update UI
        setActiveMode(SIMPLE)
    }

    private fun setActiveMode(mode: Int) {
        activeMode = mode

        // Reset button backgrounds
        buttonSimple.isSelected = false
        buttonExtended.isSelected = false
        buttonCustom.isSelected = false

        // Set active button
        when (mode) {
            SIMPLE -> {
                buttonSimple.isSelected = true
                tableTitle.setText(R.string.simple_table_title)
                buttonEdit.visibility = View.GONE
                buttonSave.visibility = View.GONE
            }
            EXTENDED -> {
                buttonExtended.isSelected = true
                tableTitle.setText(R.string.extended_table_title)
                buttonEdit.visibility = View.GONE
                buttonSave.visibility = View.GONE
            }
            CUSTOM -> {
                buttonCustom.isSelected = true
                tableTitle.setText(R.string.custom_table_title)
                buttonEdit.visibility = View.VISIBLE
                buttonSave.visibility = if (isEditMode) View.VISIBLE else View.GONE
            }
        }

        // Update placeholder
        val placeholder = getString(R.string.plaintext_placeholder)
        inputPlainText.hint = placeholder

        // Update output and table
        updateOutput()
        updateTable()
    }

    private fun updateOutput() {
        val input = inputPlainText.text.toString()
        val output = translate(input)
        outputLeetText.text = output
    }

    private fun translate(input: String): String {
        val output = StringBuilder()

        for (c in input.toCharArray()) {
            val outputChar = getTranslatedChar(c)
            output.append(outputChar)
        }

        return output.toString()
    }

    private fun getTranslatedChar(inputChar: Char): String {
        // Convert to uppercase for lookup
        val upperChar = inputChar.uppercaseChar()

        // Find the index in the alphabet
        val index = plaintextAlphabet.indexOf(upperChar)

        // If not found in alphabet, return original character
        if (index == -1) {
            return inputChar.toString()
        }

        // Translate based on active mode
        return when (activeMode) {
            SIMPLE -> simpleLeetAlphabet[index]
            EXTENDED -> extendedLeetAlphabet[index]
            CUSTOM -> storage.getString(upperChar.toString(), upperChar.toString()) ?: upperChar.toString()
            else -> inputChar.toString()
        }
    }

    private fun updateTable() {
        // Clear the table
        leetTable.removeAllViews()

        // Add rows to the table
        for (i in 0 until 13) {
            val row = TableRow(this)

            // Left side (letters 0-12)
            val leftPlain = TextView(this).apply {
                text = plaintextAlphabet[i].toString()
                setPadding(10, 10, 10, 10)
            }
            row.addView(leftPlain)

            // Left side leet character
            if (isEditMode && activeMode == CUSTOM) {
                val leftLeet = EditText(this).apply {
                    setText(getTranslatedChar(plaintextAlphabet[i]))
                }
                editableFields[i][0] = leftLeet
                row.addView(leftLeet)
            } else {
                val leftLeet = TextView(this).apply {
                    text = getTranslatedChar(plaintextAlphabet[i])
                    setPadding(10, 10, 20, 10)
                }
                if (!isEditMode) {
                    displayFields[i][0] = leftLeet
                }
                row.addView(leftLeet)
            }

            // Right side (letters 13-25)
            val rightPlain = TextView(this).apply {
                text = plaintextAlphabet[i + 13].toString()
                setPadding(20, 10, 10, 10)
            }
            row.addView(rightPlain)

            // Right side leet character
            if (isEditMode && activeMode == CUSTOM) {
                val rightLeet = EditText(this).apply {
                    setText(getTranslatedChar(plaintextAlphabet[i + 13]))
                }
                editableFields[i][1] = rightLeet
                row.addView(rightLeet)
            } else {
                val rightLeet = TextView(this).apply {
                    text = getTranslatedChar(plaintextAlphabet[i + 13])
                    setPadding(10, 10, 10, 10)
                }
                if (!isEditMode) {
                    displayFields[i][1] = rightLeet
                }
                row.addView(rightLeet)
            }

            leetTable.addView(row)
        }
    }

    private fun switchToEditMode() {
        if (activeMode != CUSTOM) return

        isEditMode = true
        buttonEdit.visibility = View.GONE
        buttonSave.visibility = View.VISIBLE
        updateTable()
    }

    private fun saveCustomTranslations() {
        if (!isEditMode) return

        val editor = storage.edit()

        // Save left column (letters 0-12)
        for (i in 0 until 13) {
            val plainChar = plaintextAlphabet[i].toString()
            val leetChar = editableFields[i][0]?.text.toString()
            editor.putString(plainChar, leetChar)
        }

        // Save right column (letters 13-25)
        for (i in 0 until 13) {
            val plainChar = plaintextAlphabet[i + 13].toString()
            val leetChar = editableFields[i][1]?.text.toString()
            editor.putString(plainChar, leetChar)
        }

        editor.apply()

        isEditMode = false
        buttonEdit.visibility = View.VISIBLE
        buttonSave.visibility = View.GONE

        Toast.makeText(this, R.string.save_success, Toast.LENGTH_SHORT).show()

        updateTable()
        updateOutput()
    }

    private fun checkCustomTranslations() {
        var needsDefault = false

        // Check if we have entries for all letters
        for (c in plaintextAlphabet) {
            val key = c.toString()
            if (!storage.contains(key)) {
                needsDefault = true
                break
            }
        }

        // If any letter is missing, create defaults
        if (needsDefault) {
            val editor = storage.edit()
            for (c in plaintextAlphabet) {
                val key = c.toString()
                editor.putString(key, key) // Default to same character
            }
            editor.apply()
        }
    }

    private fun copyToClipboard() {
        val text = outputLeetText.text.toString()
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Leetspeak Text", text)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this, R.string.copy_success, Toast.LENGTH_SHORT).show()
    }
}