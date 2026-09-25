package com.fca.fasttypetrainer


import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.roundToInt

class PracticeActivity : AppCompatActivity() {

    private lateinit var textViewGreeting: TextView
    private lateinit var textViewPurpose: TextView
    private lateinit var textViewTimer: TextView
    private lateinit var textViewLevel: TextView
    private lateinit var textViewLevelTitle: TextView
    private lateinit var textViewProgress: TextView
    private lateinit var textViewTarget: TextView
    private lateinit var textViewTypedPreview: TextView
    private lateinit var textViewAccuracy: TextView
    private lateinit var editTextTyping: EditText
    private lateinit var buttonRestart: Button

    private var currentLevelIndex = 0
    private var previousInput = ""
    private var isUpdatingText = false
    private var levelFinished = false
    private var timer: CountDownTimer? = null
    private var elapsedSeconds = 0L


    //default list and count for text and level
    private val levels = listOf(
        TypingLevel(
            levelNumber = 1,
            title = "Warm-up",
            targetText = "focus on tour goals",
            timeLimitSeconds = 30
        ),
        TypingLevel(
            levelNumber = 2,
            title = "Build rhythm",
            targetText = "type fast to make better practice tomorrow future",
            timeLimitSeconds = 40
        ),
        TypingLevel(
            levelNumber = 3,
            title = "Short sentence",
            targetText = "practice makes progress so type fast to make better practice tomorrow future",
            timeLimitSeconds = 60
        ),
        TypingLevel(
            levelNumber = 4,
            title = "Speed challenge",
            targetText = "accuracy is more important than speed so practice makes progress so type fast to make better practice tomorrow future",
            timeLimitSeconds = 90
        ),
        TypingLevel(
            levelNumber = 5,
            title = "Final challenge",
            targetText = "small daily practice creates great typing speed so accuracy is more important than speed so practice makes progress so type fast to make better practice tomorrow future accuracy is more important than speed so practice makes progress so type fast to make better practice tomorrow future",
            timeLimitSeconds = 120
        )
    )

    private val currentLevel: TypingLevel
        get() = levels[currentLevelIndex]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_practice)

        initializeViews()
        loadUserInformation()
        setupListeners()
        showCurrentLevel()
    }

    //id`s
    private fun initializeViews() {
        textViewGreeting = findViewById(R.id.textViewGreeting)
        textViewPurpose = findViewById(R.id.textViewPurpose)
        textViewTimer = findViewById(R.id.textViewTimer)
        textViewLevel = findViewById(R.id.textViewLevel)
        textViewLevelTitle = findViewById(R.id.textViewLevelTitle)
        textViewProgress = findViewById(R.id.textViewProgress)
        textViewTarget = findViewById(R.id.textViewTarget)
        textViewTypedPreview = findViewById(R.id.textViewTypedPreview)
        textViewAccuracy = findViewById(R.id.textViewAccuracy)
        editTextTyping = findViewById(R.id.editTextTyping)
        buttonRestart = findViewById(R.id.buttonRestart)
    }

    private fun loadUserInformation() {
        val preferences = getSharedPreferences(
            "fast_type_preferences",
            MODE_PRIVATE
        )

        val name = preferences.getString("user_name", "Learner")
        val purpose = preferences.getString(
            "user_purpose",
            "Typing practice"
        )

        textViewGreeting.text = "Hello, $name!"
        textViewPurpose.text = purpose
    }

    private fun setupListeners() {
        editTextTyping.addTextChangedListener(typingWatcher)

        buttonRestart.setOnClickListener {
            restartLevel()
        }
    }

    //level update
    private fun showCurrentLevel() {
        timer?.cancel()

        previousInput = ""
        isUpdatingText = false
        levelFinished = false
        elapsedSeconds = 0L

        val level = currentLevel

        textViewLevel.text = "LEVEL ${level.levelNumber}"
        textViewLevelTitle.text = level.title
        textViewProgress.text = "Level ${level.levelNumber} of ${levels.size}"
        textViewTarget.text = level.targetText
        textViewTimer.text = formatTime(level.timeLimitSeconds)
        textViewTypedPreview.text = ""
        textViewAccuracy.text = "Accuracy: 100%"

        editTextTyping.isEnabled = true
        editTextTyping.setText("")
        editTextTyping.requestFocus()

        startCountdown(level.timeLimitSeconds)

        editTextTyping.postDelayed({
            val inputMethodManager = getSystemService(
                Context.INPUT_METHOD_SERVICE
            ) as InputMethodManager

            inputMethodManager.showSoftInput(
                editTextTyping,
                InputMethodManager.SHOW_IMPLICIT
            )
        }, 300)
    }

    //count related work
    private fun startCountdown(seconds: Long) {
        timer = object : CountDownTimer(
            seconds * 1000L,
            1000L
        ) {

            override fun onTick(millisecondsRemaining: Long) {
                val remainingSeconds = millisecondsRemaining / 1000L
                textViewTimer.text = formatTime(remainingSeconds)

                elapsedSeconds = seconds - remainingSeconds
            }

            override fun onFinish() {
                textViewTimer.text = "00:00"

                if (!levelFinished) {
                    finishLevel(timeExpired = true)
                }
            }

        }.start()
    }

    private val typingWatcher = object : TextWatcher {

        override fun beforeTextChanged(
            s: CharSequence?,
            start: Int,
            count: Int,
            after: Int
        ) {
            // No action required.
        }

        override fun onTextChanged(
            s: CharSequence?,
            start: Int,
            before: Int,
            count: Int
        ) {
            // No action required.
        }

        override fun afterTextChanged(editable: Editable?) {
            if (isUpdatingText || levelFinished) {
                return
            }

            val currentInput = editable?.toString().orEmpty()

            /*
             * Strict mode:
             * The user cannot delete a previously typed character.
             */
            if (currentInput.length < previousInput.length) {
                isUpdatingText = true

                editTextTyping.setText(previousInput)
                editTextTyping.setSelection(previousInput.length)

                isUpdatingText = false
                return
            }

            /*
             * Do not allow input beyond the target length.
             */
            if (currentInput.length > currentLevel.targetText.length) {
                isUpdatingText = true

                val limitedInput = currentInput.take(
                    currentLevel.targetText.length
                )

                editTextTyping.setText(limitedInput)
                editTextTyping.setSelection(limitedInput.length)

                isUpdatingText = false
                return
            }

            previousInput = currentInput

            updateColoredPreview(currentInput)
            updateAccuracy(currentInput)

            if (currentInput.length == currentLevel.targetText.length) {
                finishLevel(timeExpired = false)
            }
        }
    }

    private fun updateColoredPreview(userInput: String) {
        val target = currentLevel.targetText
        val coloredText = SpannableString(userInput)

        for (index in userInput.indices) {
            val typedCharacter = userInput[index]
            val correctCharacter = target[index]

            val characterColor = if (
                typedCharacter == correctCharacter
            ) {
                Color.rgb(22, 163, 74)
            } else {
                Color.rgb(220, 38, 38)
            }

            coloredText.setSpan(
                ForegroundColorSpan(characterColor),
                index,
                index + 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        textViewTypedPreview.text = coloredText
    }

    private fun updateAccuracy(userInput: String) {
        if (userInput.isEmpty()) {
            textViewAccuracy.text = "Accuracy: 100%"
            textViewAccuracy.setTextColor(
                Color.rgb(22, 163, 74)
            )
            return
        }

        val target = currentLevel.targetText
        var correctCharacters = 0

        for (index in userInput.indices) {
            if (userInput[index] == target[index]) {
                correctCharacters++
            }
        }

        val accuracy = (
                correctCharacters.toDouble() / userInput.length.toDouble()
                ) * 100.0

        val roundedAccuracy = accuracy.roundToInt()

        textViewAccuracy.text = "Accuracy: $roundedAccuracy%"

        if (roundedAccuracy >= 80) {
            textViewAccuracy.setTextColor(
                Color.rgb(22, 163, 74)
            )
        } else {
            textViewAccuracy.setTextColor(
                Color.rgb(220, 38, 38)
            )
        }
    }

    //popup after complete each level
    private fun finishLevel(timeExpired: Boolean) {
        if (levelFinished) {
            return
        }

        levelFinished = true
        timer?.cancel()
        editTextTyping.isEnabled = false

        val typedText = editTextTyping.text.toString()
        val correctText = currentLevel.targetText

        val correctCharacters = countCorrectCharacters(
            typedText,
            correctText
        )

        val totalCharacters = correctText.length

        val accuracy = if (typedText.isNotEmpty()) {
            (
                    correctCharacters.toDouble() /
                            typedText.length.toDouble()
                    ) * 100.0
        } else {
            0.0
        }

        val roundedAccuracy = accuracy.roundToInt()

        val message = if (timeExpired) {
            "Time is over. Review your result."
        } else if (typedText == correctText) {
            "Excellent! You typed everything correctly."
        } else {
            "Practice completed. Review your mistake below."
        }

        val resultMessage = buildString {
            append(message)
            append("\n\n")
            append("Correct text:\n")
            append(correctText)
            append("\n\n")
            append("Your text:\n")
            append(if (typedText.isEmpty()) "(Nothing typed)" else typedText)
            append("\n\n")
            append("Accuracy: ")
            append("$roundedAccuracy%")
            append("\n")
            append("Correct characters: ")
            append("$correctCharacters / $totalCharacters")
            append("\n")
            append("Time used: ")
            append(formatTime(elapsedSeconds))
        }

        AlertDialog.Builder(this)
            .setTitle(
                if (typedText == correctText) {
                    "🎉 Level Complete!"
                } else {
                    "Practice Result"
                }
            )
            .setMessage(resultMessage)
            .setCancelable(false)
            .setPositiveButton(
                if (currentLevelIndex < levels.lastIndex) {
                    "Next Level"
                } else {
                    "Finish"
                }
            ) { _, _ ->

                if (currentLevelIndex < levels.lastIndex) {
                    currentLevelIndex++
                    showCurrentLevel()
                } else {
                    showFinalMessage()
                }
            }
            .setNegativeButton("Try Again") { _, _ ->
                restartLevel()
            }
            .show()
    }

    private fun countCorrectCharacters(
        typedText: String,
        correctText: String
    ): Int {
        val comparisonLength = minOf(
            typedText.length,
            correctText.length
        )

        var correctCharacters = 0

        for (index in 0 until comparisonLength) {
            if (typedText[index] == correctText[index]) {
                correctCharacters++
            }
        }

        return correctCharacters
    }

    private fun restartLevel() {
        showCurrentLevel()
    }

    private fun showFinalMessage() {
        AlertDialog.Builder(this)
            .setTitle("🏆 Training Complete")
            .setMessage(
                "Congratulations!\n\n" +
                        "You completed all available levels.\n\n" +
                        "Continue practising every day to improve your speed and accuracy."
            )
            .setPositiveButton("Back to Home") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun formatTime(seconds: Long): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60

        return String.format(
            "%02d:%02d",
            minutes,
            remainingSeconds
        )
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }
}
