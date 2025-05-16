package com.aman.fityatraapp.ui

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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aman.fityatraapp.R
import com.aman.fityatraapp.data.local.model.HealthData
import com.aman.fityatraapp.data.local.model.UserData
import com.aman.fityatraapp.data.api.ExerciseAdd
import com.aman.fityatraapp.models.MealAdd
import com.aman.fityatraapp.utils.ApiClient.apiService
import com.aman.fityatraapp.utils.ExerciseAddAdapter
import com.aman.fityatraapp.utils.MealAddAdapter
import com.aman.fityatraapp.viewModel.ChatBotViewModel
import com.aman.fityatraapp.viewModel.DashboardViewModel
import com.aman.fityatraapp.viewModel.ExerciseAddViewModel
import com.aman.fityatraapp.viewModel.MealViewModel
import com.google.ai.client.generativeai.GenerativeModel
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader


data class Question(
    val question: String,
    val type: String,
    val options: List<String>? = null
)

@AndroidEntryPoint
class ChatBotActivity : AppCompatActivity(), MealAddAdapter.OnDeleteClickListener, ExerciseAddAdapter.OnDeleteClickListener {
    private lateinit var  chatBotViewModel : ChatBotViewModel
    private lateinit var mealViewModel : MealViewModel
    private lateinit var exerciseAddViewModel : ExerciseAddViewModel
    private lateinit var dashboardViewModel: DashboardViewModel

    private lateinit var chatLayout: LinearLayout
    private lateinit var inputLayout: LinearLayout
    private lateinit var inputField: EditText
    private lateinit var sendButton: Button
    private lateinit var scrollView: ScrollView
    private var currentQuestionIndex = 0
    private val questions = mutableListOf<Question>()
    private lateinit var userData: MutableMap<String, String>
    private lateinit var mealList: MutableList<MealAdd>
    private lateinit var exerciseList : MutableList<ExerciseAdd>
    private var isQuestionsAsked = false

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
        modelName = "gemini-2.0-flash",
        apiKey = "AIzaSyAq70kaOeY0Dv8341W66B9ilvVW0wgOqVQ"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_bot)

        chatBotViewModel= ViewModelProvider(this)[ChatBotViewModel::class.java]
        exerciseAddViewModel= ViewModelProvider(this)[ExerciseAddViewModel::class.java]
        mealViewModel= ViewModelProvider(this)[MealViewModel::class.java]
        dashboardViewModel= ViewModelProvider(this)[DashboardViewModel::class.java]

        supportActionBar?.hide()

        chatBotViewModel.fetchUserData()
        chatBotViewModel.fetchLast7DaysData()
        chatBotViewModel.fetchTodayHealthData()

        chatBotViewModel.last7daysData.observe(this){
            last7daysData=it
        }
        chatBotViewModel.todayHealthData.observe(this){
            healthData=it
        }
        chatBotViewModel.savedUserData.observe(this){
            savedUserData=it
        }

        lifecycleScope.launch {
            val response = apiService.startServer()
        }


        userData = chatBotViewModel.userData
        mealList = mealViewModel.mealList.value!!
        exerciseList = exerciseAddViewModel.exerciseList.value!!

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
        chatBotViewModel.isUserDataAvailable.observe(this) { available ->
            if (available) {
                isQuestionsAsked = true
                setupChatBot()
                findViewById<HorizontalScrollView>(R.id.horizontalScrollView).visibility = View.VISIBLE
            } else {
                askNextQuestion()
            }
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
            chatBotViewModel.addUserData(questions)
            isQuestionsAsked = true
            setupChatBot()
            findViewById<HorizontalScrollView>(R.id.horizontalScrollView).visibility = View.VISIBLE
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
        scrollView.post {
            scrollView.fullScroll(View.FOCUS_DOWN)
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
        feetButton.setTextColor(ContextCompat.getColor(this, R.color.white))
        cmButton.background = ContextCompat.getDrawable(this, R.drawable.option_background)
        cmButton.setTextColor(ContextCompat.getColor(this, R.color.white))


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

    private fun Activity.hideKeyboard() {
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
                userInput.contains("show calorie", ignoreCase = true) -> navigateToStatisticsActivity("calorie")
                userInput.contains("show step", ignoreCase = true) -> navigateToStatisticsActivity("steps")
                userInput.contains("show weight", ignoreCase = true) -> navigateToStatisticsActivity("weight")
                userInput.contains("main", ignoreCase = true) -> navigateToMainActivity()
                userInput.contains("posture", ignoreCase = true) -> navigateToPostureActivity()
                else -> {
                    handleFallback(userInput)
                }

            }
            scrollToBottom()
        }
    }

    private fun navigateToStatisticsActivity(type: String) {
        val intent=Intent(this, StatisticsActivity::class.java)
        intent.putExtra("type", type)
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
        mealAddAdapter = MealAddAdapter(mutableListOf(), this)
        mealRecyclerView.adapter = mealAddAdapter

        addMealButton.setOnClickListener {
            mealViewModel.addMealItem()
        }

        mealViewModel.mealList.observe(this) {
            mealAddAdapter.updateData(it)
        }

        saveMealButton.setOnClickListener {
            mealViewModel.calculateCalories()
        }

        mealViewModel.successEvent.observe(this) {
            mealViewModel.mealList.value = mutableListOf(MealAdd())
            findViewById<LinearLayout>(R.id.chatLayout).removeView(mealInputLayout)
            addChatMessage("Meal Added Successfully")
        }

        findViewById<LinearLayout>(R.id.chatLayout).addView(mealInputLayout)
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
            exerciseAddViewModel.addExercise()
        }
        exerciseAddViewModel.exerciseList.observe(this) {
            exerciseAddAdapter.updateData(it)
        }
        saveExerciseButton.setOnClickListener {
            exerciseAddViewModel.calculateCalories()
        }
        exerciseAddViewModel.successEvent.observe(this) {
            exerciseAddViewModel.exerciseList.value = mutableListOf(ExerciseAdd())
            findViewById<LinearLayout>(R.id.chatLayout).removeView(exerciseInputLayout)
            addChatMessage("Exercise Added Successfully")
        }

        findViewById<LinearLayout>(R.id.chatLayout).addView(exerciseInputLayout)
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
                    dashboardViewModel.updateWeight(weight)
                } else {
                    showToast("Weight cannot be empty")
                }
            }
            setNegativeButton("Cancel") { dialog, _ ->
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
                    dashboardViewModel.updateGlucose(glucose)
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
            mealViewModel.removeMealItem(position)
        }else{
            exerciseAddViewModel.removeExercise(position)
        }
    }
}

