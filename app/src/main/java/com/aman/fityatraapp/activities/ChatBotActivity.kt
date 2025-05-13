package com.aman.fityatraapp.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aman.fityatraapp.R
import com.aman.fityatraapp.models.ExerciseAdd
import com.aman.fityatraapp.models.HealthData
import com.aman.fityatraapp.models.MealAdd
import com.aman.fityatraapp.models.UserData
import com.aman.fityatraapp.utils.ApiClient.apiService
import com.aman.fityatraapp.utils.ExerciseAddAdapter
import com.aman.fityatraapp.models.Item
import com.aman.fityatraapp.utils.MealAddAdapter
import com.aman.fityatraapp.utils.SQLiteUtils
import com.aman.fityatraapp.models.exerItem
import com.google.ai.client.generativeai.GenerativeModel
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader


data class Question(
    val question: String,
    val type: String,
    val options: List<String>? = null
)

class ChatBotActivity : AppCompatActivity(), MealAddAdapter.OnDeleteClickListener, ExerciseAddAdapter.OnDeleteClickListener {
    private lateinit var sqLiteUtils: SQLiteUtils
    private lateinit var chatLayout: LinearLayout
    private lateinit var inputLayout: LinearLayout
    private lateinit var inputField: EditText
    private lateinit var sendButton: Button
    private lateinit var scrollView: ScrollView
    private var currentQuestionIndex = 0
    private val questions = mutableListOf<Question>()
    private val userData = mutableMapOf<String, String>()
    private var isQuestionsAsked = false
    private var mealList = mutableListOf<MealAdd>()
    private var exerciseList = mutableListOf<ExerciseAdd>()
    private lateinit var mealAddAdapter: MealAddAdapter
    private lateinit var exerciseAddAdapter: ExerciseAddAdapter
    private lateinit var mealInputPrompt: TextView
    private lateinit var exercisePrompt: TextView
    private lateinit var weightPrompt: TextView
    private lateinit var glucosePrompt: TextView
    private lateinit var dietPlanPrompt: TextView
    private lateinit var addDataPrompt: TextView
    private lateinit var mainPrompt: TextView
    private lateinit var posturePrompt: TextView
    private lateinit var calorieStat: TextView
    private lateinit var weightStatPrompt: TextView
    private lateinit var stepStatPrompt: TextView

    private var healthData: HealthData? = null
    private var savedUserData: UserData? = null
    private var last7daysData: List<HealthData>? = null


    private val generativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = "AIzaSyAq70kaOeY0Dv8341W66B9ilvVW0wgOqVQ"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_bot)
        sqLiteUtils = SQLiteUtils(this)
        supportActionBar?.hide()

        lifecycleScope.launch {
            val response = apiService.startServer()
        }

        chatLayout = findViewById(R.id.chatLayout)
        inputLayout = findViewById(R.id.inputLayout)
        inputField = findViewById(R.id.inputField)
        sendButton = findViewById(R.id.sendButton)
        scrollView = findViewById(R.id.scrollView)


        loadQuestions()
        greetUserAndCheckData()

        sendButton.setOnClickListener {
            handleSendButtonClick()
        }


        mealInputPrompt = findViewById(R.id.mealInputPrompt)
        exercisePrompt = findViewById(R.id.exercisePrompt)
        weightPrompt = findViewById(R.id.weightPrompt)
        glucosePrompt = findViewById(R.id.glucosePrompt)
        dietPlanPrompt = findViewById(R.id.dietPlanPrompt)
        addDataPrompt = findViewById(R.id.addDataPrompt)
        mainPrompt = findViewById(R.id.mainPrompt)
        posturePrompt = findViewById(R.id.posturePrompt)
        weightStatPrompt = findViewById(R.id.weightStatPrompt)
        stepStatPrompt = findViewById(R.id.stepStatPrompt)
        calorieStat = findViewById(R.id.calorieStat)


        mealInputPrompt.setOnClickListener {
            handleUserInput("Show Meal Input")
            addChatMessage("Show Meal Input", true)
        }
        exercisePrompt.setOnClickListener {
            handleUserInput("go to Exercise")
            addChatMessage("Navigate to Exercise Fragment", true)
        }
        weightPrompt.setOnClickListener {
            handleUserInput("Open Weight Editor")
            addChatMessage("Open Weight Editor", true)
        }
        glucosePrompt.setOnClickListener {
            handleUserInput("Open Blood sugar Editor")
            addChatMessage("Open Blood sugar Editor", true)
        }
        dietPlanPrompt.setOnClickListener {
            handleUserInput("See Diet Plan")
            addChatMessage("Navigate to Diet Plan Fragment", true)
        }
        addDataPrompt.setOnClickListener {
            handleUserInput("Handle Add Data")
            addChatMessage("Handle Add Data", true)
        }
        mainPrompt.setOnClickListener {
            handleUserInput("Navigate to Main Activity")
            addChatMessage("Navigate to Main Activity", true)
        }
        posturePrompt.setOnClickListener {
            handleUserInput("Navigate to Posture Detection")
            addChatMessage("Navigate to Posture Activity", true)
        }
        weightStatPrompt.setOnClickListener {
            handleUserInput("Show weight Graph")
            addChatMessage("Show weight Graph", true)
        }
        calorieStat.setOnClickListener {
            handleUserInput("Show Calorie Graph")
            addChatMessage("Show Calorie Graph", true)
        }
        stepStatPrompt.setOnClickListener {
            handleUserInput("Show Step Counts")
            addChatMessage("Show Step Counts", true)
        }


    }

    private fun loadQuestions() {
        val inputStream = assets.open("questions.json")
        val reader = InputStreamReader(inputStream)
        val gson = Gson()
        val questionsArray = gson.fromJson(reader, Array<Question>::class.java)
        questions.addAll(questionsArray)
    }

    private fun greetUserAndCheckData() {
        addChatMessage("Hello")
        checkUserData()
    }


    private fun checkUserData() {

        val isDataAvailable = sqLiteUtils.isUserDataAvailable()
        if (isDataAvailable) {
            isQuestionsAsked = true
            setupChatBot()
            findViewById<HorizontalScrollView>(R.id.horizontalScrollView).visibility = View.VISIBLE
        } else {
            askNextQuestion()
        }
    }

    private fun setupChatBot() {
        lifecycleScope.launch {
            delay(500)
            showInputField(InputType.TYPE_CLASS_TEXT)
            addChatMessage("Welcome To the App! How can I assist you with your health?")
        }
    }


    private fun askNextQuestion() {
        if (currentQuestionIndex < questions.size) {
            val question = questions[currentQuestionIndex]
            addChatMessage(question.question)
            when (question.type) {
                "number" -> showInputField(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
                "height" -> askHeightPreference()
                "dropdown" -> showOptionButtons(question.options!!)
                "text" -> showInputField(InputType.TYPE_CLASS_TEXT)
            }
        } else {
            saveUserData()
            checkUserData()
        }
    }

    private fun handleSendButtonClick() {
        val input = inputField.text.toString()
        if (isQuestionsAsked) {
            handleUserInput(input)
            addChatMessage(input, true)
            inputField.text.clear()
            scrollToBottom()
            return
        }
        if (input.isNotEmpty()) {
            userData[questions[currentQuestionIndex].question] = input
            addChatMessage(input, true)
            inputField.text.clear()
            currentQuestionIndex++

            if (!isQuestionsAsked) {
                askNextQuestion()
            } else {

            }

        } else {
            addChatMessage("Please provide a valid answer.")
        }
    }

    private fun showInputField(inputType: Int) {
        inputField.inputType = inputType
        inputLayout.visibility = View.VISIBLE
    }

    private fun showOptionButtons(options: List<String>) {
        hideKeyboard()

        inputLayout.visibility = View.GONE

        val optionLayout = GridLayout(this).apply {
            rowCount = (options.size + 1) / 2
            columnCount = 2
            setPadding(16, 16, 16, 16)
            layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)
            }
        }

        options.forEach { option ->
            val optionButton = createOptionButton(option) {
                userData[questions[currentQuestionIndex].question] = option
                addChatMessage(option, true)
                currentQuestionIndex++
                askNextQuestion()
                chatLayout.removeView(optionLayout)
            }

            optionButton.background = ContextCompat.getDrawable(this, R.drawable.option_background)

            optionButton.setTextColor(ContextCompat.getColor(this, R.color.white))

            val layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(8, 8, 8, 8)
            }
            optionButton.layoutParams = layoutParams
            optionButton.gravity = Gravity.CENTER

            optionLayout.addView(optionButton)
        }

        chatLayout.addView(optionLayout)
        showInputField(InputType.TYPE_CLASS_TEXT)

    }


    private fun createOptionButton(text: String, onClickListener: View.OnClickListener): Button {
        return Button(this).apply {
            this.text = text
            setOnClickListener(onClickListener)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
        }
    }

    private fun addChatMessage(message: String, isUserMessage: Boolean = false) {
        val messageView = TextView(this).apply {
            text = message
            textSize = 16f

            gravity = Gravity.CENTER_VERTICAL

            setBackgroundResource(if (isUserMessage) R.drawable.user_message_background else R.drawable.bot_message_background)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                gravity = if (isUserMessage) Gravity.END else Gravity.START

            }
            (layoutParams as LinearLayout.LayoutParams).setMargins(20, 20, 20, 20)
        }
        chatLayout.addView(messageView)
        scrollToBottom()
    }

    private fun scrollToBottom() {
//        scrollView.post {
//            scrollView.scrollTo(0, scrollView.scrollY)
//        }
        scrollView.post {
            scrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun saveUserData() {
        val name = userData[questions[0].question]!!
        val heightInCm = convertHeightToCm(userData[questions[2].question]!!)
        val weightInKg = userData[questions[3].question]!!.toFloat()
        val gender = if (userData[questions[4].question] == "Male") "m" else "f"
        val mealPreference = if (userData[questions[5].question] == "Veg") "veg" else "non-veg"
        val activityLevel = mapActivityLevel(userData[questions[6].question]!!)
        val goal = mapFitnessGoal(userData[questions[7].question]!!)
        val sleepSchedule = userData[questions[8].question]!!
        val medicalProblems = userData[questions[9].question]!!
        val HbA1c = userData[questions[10].question]!!.toFloat()

        val userData = UserData(
            name = name,
            Height = heightInCm,
            Weight = weightInKg,
            Preference = mealPreference,
            Age = userData[questions[1].question]!!.toInt(),
            Activity = activityLevel,
            Sex = gender,
            Goal = goal,
            sleepSchedule = sleepSchedule,
            medicalProblems = medicalProblems,
            HbA1c = HbA1c
        )


        makeApiCall(userData)
        addChatMessage("Diet Plan Generated Successfully")
        sqLiteUtils.saveUserData(userData)
        sqLiteUtils.addOrUpdateHealthData(emptyList(), emptyList(), 0,0, 0, weight = weightInKg, glucoseLevel = HbA1c, onSuccess = {}, onFailure = {})
        updateHealthData()

        navigateToMainActivity()

    }

    private fun makeApiCall(userData: UserData) {
        lifecycleScope.launch {
            val response = apiService.generateDietPlan(userData)
            if (response.isSuccessful) {
                sqLiteUtils.saveDietPlan(response.body()!!)
            }
        }
    }

    private fun convertHeightToCm(height: String): Int {
        val parts = height.split(" ")
        Log.d("parts", parts.toString())
        if (parts.size == 2) {
            return parts[0].toInt()
        } else if (parts.size == 4) {
            val feet = parts[0].toInt()
            val inches = parts[2].toInt()
            return (feet * 30.48 + inches * 2.54).toInt()
        } else {
            throw IllegalArgumentException("Invalid height format")
        }
    }


    private fun mapActivityLevel(activity: String): Double {
        return when (activity) {
            "Basal Metabolic Rate (BMR)" -> 1.0
            "Sedentary: little or no exercise" -> 1.2
            "Light: exercise 1-3 times/week" -> 1.375
            "Moderate: exercise 4-5 times/week" -> 1.465
            "Active: daily exercise or intense exercise 3-4 times/week" -> 1.55
            "Very Active: intense exercise 6-7 times/week" -> 1.725
            "Extra Active: very intense exercise daily, or physical job" -> 1.9
            else -> 1.0
        }
    }

    private fun mapFitnessGoal(goal: String): String {
        return when (goal) {
            "Maintain weight" -> "m"
            "Mild weight loss of 0.5 lb (0.25 kg) per week" -> "l"
            "Weight loss of 1 lb (0.5 kg) per week" -> "l1"
            "Extreme weight loss of 2 lb (1 kg) per week" -> "l2"
            "Mild weight gain of 0.5 lb (0.25 kg) per week" -> "g"
            "Weight gain of 1 lb (0.5 kg) per week" -> "g1"
            "Extreme weight gain of 2 lb (1 kg) per week" -> "g2"
            else -> "m"
        }
    }

    private fun askHeightPreference() {
        hideKeyboard()

        inputLayout.visibility = View.GONE

        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val feetButton = createButton("Feet/Inches") {
            chatLayout.removeView(buttonLayout)
            addHeightInputFields()
        }

        val cmButton = createButton("Centimeters") {
            chatLayout.removeView(buttonLayout)
            addCmInputField()
        }

        feetButton.background = ContextCompat.getDrawable(this, R.drawable.option_background)
        cmButton.background = ContextCompat.getDrawable(this, R.drawable.option_background)

        val buttonLayoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            weight = 1f
            gravity = Gravity.CENTER
            marginStart = resources.getDimensionPixelSize(R.dimen.button_margin)
            marginEnd = resources.getDimensionPixelSize(R.dimen.button_margin)
        }
        feetButton.layoutParams = buttonLayoutParams
        cmButton.layoutParams = buttonLayoutParams

        buttonLayout.addView(feetButton)
        buttonLayout.addView(cmButton)
        chatLayout.addView(buttonLayout)
    }


    private fun addHeightInputFields() {
        val inputFeetField = NumberPicker(this).apply {
            minValue = 0
            maxValue = 8
        }
        val inputInchesField = NumberPicker(this).apply {
            minValue = 0
            maxValue = 11
        }
        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        val sendButton = createButton("Send") {
            val feet = inputFeetField.value
            val inches = inputInchesField.value

            if (feet != 0 || inches != 0) {
                val height = "$feet feet $inches inches"
                userData[questions[currentQuestionIndex].question] = height
                chatLayout.removeView(buttonLayout)
                addChatMessage(height, true)
                currentQuestionIndex++
                askNextQuestion()
            } else {
                addChatMessage("Please provide a valid answer.")
            }
        }
        buttonLayout.addView(inputFeetField)
        buttonLayout.addView(inputInchesField)
        buttonLayout.addView(sendButton)
        chatLayout.addView(buttonLayout)
    }

    private fun addCmInputField() {
        val inputCmField = NumberPicker(this).apply {
            minValue = 0
            maxValue = 300
        }
        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        val sendButton = createButton("Send") {
            val cm = inputCmField.value

            if (cm != 0) {
                val height = "$cm cm"
                userData[questions[currentQuestionIndex].question] = height
                chatLayout.removeView(buttonLayout)
                addChatMessage(height, true)
                currentQuestionIndex++
                askNextQuestion()
            } else {
                addChatMessage("Please provide a valid answer.")
            }
        }
        buttonLayout.addView(inputCmField)
        buttonLayout.addView(sendButton)
        chatLayout.addView(buttonLayout)

    }

    private fun createButton(text: String, onClickListener: View.OnClickListener): Button {
        return Button(this).apply {
            this.text = text
            setOnClickListener(onClickListener)
        }
    }


    private fun calculateBMI() {
        val weight = userData["What is your weight?"]?.toDoubleOrNull()
        val heightStr = userData["What is your height?"]
        val heightInCm = when {
            heightStr?.contains("cm") == true -> heightStr.split(" ")[0].toDoubleOrNull()
            else -> {
                val (feet, inches) = heightStr!!.split(" ").filter { it.matches(Regex("\\d+")) }
                    .map { it.toDouble() }
                (feet * 30.48) + (inches * 2.54)
            }
        }

        if (weight != null && heightInCm != null) {
            val heightInMeters = heightInCm / 100
            val bmi = weight / (heightInMeters * heightInMeters)
            addChatMessage("Your BMI is %.2f".format(bmi))
        } else {
            addChatMessage("Error calculating BMI. Please ensure weight and height are entered correctly.")
        }
    }


    fun Activity.hideKeyboard() {
        val view: View? = this.currentFocus
        view?.let { v ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }


    private fun handleUserInput(userInput: String) {
        lifecycleScope.launch {
            delay(500)
            when {
                userInput.contains("breakfast", ignoreCase = true) -> showMealInput()
                userInput.contains("lunch", ignoreCase = true) -> showMealInput()
                userInput.contains("dinner", ignoreCase = true) -> showMealInput()
                (userInput.contains("meal", ignoreCase = true) && (userInput.contains("add", ignoreCase = true) || userInput.contains("input", ignoreCase = true))) -> showMealInput()
                (userInput.contains(
                    "exercise",
                    ignoreCase = true
                ) || userInput.contains(
                    "exercises",
                    ignoreCase = true
                )) && userInput.contains(
                    "add",
                    ignoreCase = true
                ) || userInput.contains("exercise data", ignoreCase = true) -> showExerciseInput()

                userInput.contains(
                    "do exercise",
                    ignoreCase = true
                ) || userInput.contains(
                    "go to exercise",
                    ignoreCase = true
                )  -> navigateToExerciseFragment()

                userInput.contains("weight", ignoreCase = true) && (userInput.contains(
                    "add",
                    ignoreCase = true
                ) || userInput.contains("data", ignoreCase = true) ||
                        userInput.contains("open", ignoreCase = true)) -> openWeightEditor()

                userInput.contains("blood sugar", ignoreCase = true) -> openGlucoseEditor()
                userInput.contains("diet plan", ignoreCase = true) -> navigateToDietPlanFragment()
                userInput.contains("add data", ignoreCase = true) -> handleAddData()
                userInput.contains("show calorie", ignoreCase = true) -> navigateToCalorie()
                userInput.contains("show step", ignoreCase = true) -> navigateToSteps()
                userInput.contains("show weight", ignoreCase = true) -> navigateToWeight()
                userInput.contains("main", ignoreCase = true) -> navigateToMainActivity()
                userInput.contains("posture", ignoreCase = true) -> navigateToPostureActivity()
                else -> {
                    handleFallback(userInput)
                }

            }
            scrollToBottom()
        }
    }

    private fun navigateToCalorie() {
        val intent = Intent(this, CalorieStatisticsActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToSteps() {
        val intent = Intent(this, StepCountStatisticsActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToWeight() {
        val intent = Intent(this, WeightStatisticsActivity::class.java)
        startActivity(intent)
    }


    private fun handleAddData() {
        addChatMessage(
            "Which Data You want to Add like\n Exercise Data\n" +
                    "Meal Data\n" +
                    " Weight\n" +
                    "Blood Sugar Level"
        )
    }


    private fun showMealInput() {
        mealList.add(MealAdd())

        val mealInputLayout = layoutInflater.inflate(R.layout.meal_input_layout, null)
        val mealRecyclerView: RecyclerView = mealInputLayout.findViewById(R.id.meal_recycler_view)
        val addMealButton: Button = mealInputLayout.findViewById(R.id.iv_add_meal)
        val saveMealButton: Button = mealInputLayout.findViewById(R.id.saveBtn)

        mealRecyclerView.layoutManager = LinearLayoutManager(this)
        mealAddAdapter = MealAddAdapter(mealList, this)
        mealRecyclerView.adapter = mealAddAdapter

        addMealButton.setOnClickListener {
            mealList.add(MealAdd())
            mealAddAdapter.notifyItemInserted(mealList.size - 1)
        }


        saveMealButton.setOnClickListener {
            calculateMealCalories()
            findViewById<LinearLayout>(R.id.chatLayout).removeView(mealInputLayout)
            addChatMessage("Successfully added Meal")
        }

        findViewById<LinearLayout>(R.id.chatLayout).addView(mealInputLayout)
    }

    private fun calculateMealCalories()  {
        val mealData = mealList.map { Item(it.dishName, it.quantity) }

        lifecycleScope.launch {
            val responseMealDeferred = async { apiService.calculateCalories(mealData) }
            val responseMeal = responseMealDeferred.await()

            if (responseMeal.isSuccessful) {
                val totalCalories = responseMeal.body()?.total_calories?.toInt() ?: 0
                Toast.makeText(this@ChatBotActivity, "Meal added successfully", Toast.LENGTH_SHORT).show()

                sqLiteUtils.addOrUpdateHealthData(
                    null,
                    mealList,
                    0,
                    totalCalories,
                    0,
                    null,
                    null,
                    onSuccess = {
                        mealList.clear()
                        mealList.add(MealAdd())
                    },
                    onFailure = { error ->
                        Log.e("MealActivity", "Error adding meal to database", error)
                    }
                )
                updateHealthData()
            } else {
            }
        }
    }

    private fun updateHealthData() {
        sqLiteUtils.getTodayHealthData(onSuccess = {data->
            healthData=data
        }, onFailure = {})
    }

    private fun showExerciseInput() {
        exerciseList.add(ExerciseAdd())

        val exerciseInputLayout = layoutInflater.inflate(R.layout.exercise_input_layout, null)
        val exerciseRecyclerView: RecyclerView =
            exerciseInputLayout.findViewById(R.id.exercise_recycler_view)
        val addExerciseButton: Button = exerciseInputLayout.findViewById(R.id.iv_add_exercise)
        val saveExerciseButton: Button = exerciseInputLayout.findViewById(R.id.saveBtn)

        exerciseRecyclerView.layoutManager = LinearLayoutManager(this)
        exerciseAddAdapter = ExerciseAddAdapter(exerciseList, this)
        exerciseRecyclerView.adapter = exerciseAddAdapter

        addExerciseButton.setOnClickListener {
            exerciseList.add(ExerciseAdd())
            exerciseAddAdapter.notifyItemInserted(exerciseList.size - 1)
        }

        saveExerciseButton.setOnClickListener {
            saveExerciseData()
            findViewById<LinearLayout>(R.id.chatLayout).removeView(exerciseInputLayout)
            addChatMessage("Successfully added Exercise")
        }

        findViewById<LinearLayout>(R.id.chatLayout).addView(exerciseInputLayout)
    }

    private fun saveExerciseData() {
        lifecycleScope.launch {
            var totalCalories = 0
            val exercisesData = exerciseList.map {  exerItem(it.exerciseName, it.duration) }

            exercisesData.forEach { exercise ->
                try {
                    val response = apiService.calculateCaloriesBurn(exercise)
                    if (response.isSuccessful) {
                        val caloriesForExercise = response.body()?.calories_burnt?.toInt() ?: 0
                        totalCalories += caloriesForExercise
                    } else {
                        Log.e("Error", "Failed to calculate calories for ${exercise.exercise_name}")
                    }
                } catch (e: Exception) {
                    Log.e("Error", "Exception occurred: ${e.message}")
                }
            }

            sqLiteUtils.addOrUpdateHealthData(
                exerciseList,
                null,
                null,
                null,
                totalCalories,
                null,
                null,
                onSuccess = {
                    showToast("Exercises added successfully")
                    exerciseList.clear()
                    exerciseList.add(ExerciseAdd())
                    exerciseAddAdapter.notifyDataSetChanged()
                },
                onFailure = { e ->
                    Log.e("Error", "Failed to add exercise to database", e)
                    showToast("Failed to add exercises")
                }
            )
            updateHealthData()
        }
    }


    private fun navigateToDietPlanFragment() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("SHOW_DIET_PLAN_FRAGMENT", true)
        startActivity(intent)
    }

    private fun navigateToPostureActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("POSTURE_FRAGMENT", true)
        startActivity(intent)
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }


    private fun navigateToExerciseFragment() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("EXERCISE_FRAGMENT", true)
        startActivity(intent)
    }

    private fun openWeightEditor() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_edit_weight, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.editTextWeight)

        with(builder) {
            setTitle("Add Weight")
            setView(dialogLayout)
            setPositiveButton("Save") { _, _ ->
                val weight = editText.text.toString().toFloat()

                if (weight != 0.0f) {
                    sqLiteUtils.addOrUpdateHealthData(emptyList(),
                        emptyList(),
                        0,
                        0,
                        0,
                        weight,
                        null,
                        onSuccess = {
                            showToast("Weight Level Successully")
                        },
                        onFailure = {
                            showToast("Error in adding Weight")
                        })
                    updateHealthData()

                } else {
                    showToast("Weight cannot be empty")
                }
            }
            setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            show()
        }
    }

    private fun openGlucoseEditor() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_edit_glucose, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.editTextGlucose)
        editText.hint = "2.4"

        with(builder) {
            setTitle("Add Glucose (HbA1c) Level (in %)")
            setView(dialogLayout)
            setPositiveButton("Save") { _, _ ->
                val glucose = editText.text.toString().toFloatOrNull()
                if (glucose != null && glucose != 0.0f) {
                    sqLiteUtils.getUserData(
                        onSuccess = { userData ->
                            if (userData != null) {
                                userData.HbA1c = glucose
                                sqLiteUtils.addOrUpdateHealthData(
                                    emptyList(),
                                    emptyList(),
                                    0,
                                    0,
                                    0,
                                    null,
                                    glucose,
                                    onSuccess = {
                                        showToast("Glucose Level Added Successfully")
                                        makeApiCall(userData)
                                    },
                                    onFailure = {
                                        showToast("Error adding glucose level")
                                    }
                                )
                            } else {
                                showToast("User data not found")
                            }
                        },
                        onFailure = {
                            showToast("Error retrieving user data")
                        }
                    )
                    updateHealthData()
                } else {
                    showToast("Glucose level cannot be empty or zero")
                }
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            show()
        }
    }


    private suspend fun handleFallback(userInput: String) {

        withContext(Dispatchers.IO) {
            val healthDataDeferred = if (healthData == null) {
                async {
                    sqLiteUtils.getTodayHealthData(onSuccess = { data ->
                        healthData = data
                    }, onFailure = {})
                }
            } else null

            val userDataDeferred = if (savedUserData == null) {
                async {
                    sqLiteUtils.getUserData(onSuccess = { data ->
                        savedUserData = data
                    }, onFailure = {})
                }
            } else null

            val today7daysDataDeferred = if (last7daysData == null) {
                async {
                    sqLiteUtils.getLast7DaysData(onSuccess = { data ->
                        last7daysData = data
                    }, onFailure = {})
                }
            } else null

            healthDataDeferred?.await()
            userDataDeferred?.await()
            today7daysDataDeferred?.await()
        }


        Log.d("trimeuserdata",
            last7daysData!!.toString()
        )
        val modifiedUserInput = buildString {
            append("Imagine yourself as an AI chatbot for Fit Yatra :A Health and Fitness app and provide accurate and well-formatted answers to user queries.\n" +
                    "\n" +
                    "User Question: ${userInput}\n" +
                    "\n" +
                    "Respond to the user's query in Human readable form and without any note. If the question pertains to health, incorporate relevant data. For general inquiries, respond appropriately without using specific data.\n")
            if (healthData != null) {
                append(" User Today's Health data: ${healthData.toString()}. \n")
            }
            if (savedUserData != null) {
                append(" User data: ${savedUserData.toString()}.\n")
            }
            if (last7daysData != null) {
                append(" User's Last ${last7daysData!!.size} days Health data in ascending order (i.e. latest date comes last) of date: ${last7daysData!!}.")
            }
        }

        val response = withContext(Dispatchers.Default) {
            generativeModel.generateContent(modifiedUserInput)
        }
        val modifiedResponseText = response.text.toString().replace("*", "")

        withContext(Dispatchers.Main) {
            addChatMessage(modifiedResponseText)
            scrollToBottom()
        }
    }


    private fun showToast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }

    override fun onDeleteClick(position: Int, type: String) {
        if (type=="meal"){
            mealList.removeAt(position)
            mealAddAdapter.notifyItemRemoved(position)
        }else{
            exerciseList.removeAt(position)
            exerciseAddAdapter.notifyItemRemoved(position)
        }
    }

}

