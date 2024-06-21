package com.aman.fityatraapp.utils

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.aman.fityatraapp.models.DayPlan
import com.aman.fityatraapp.models.DietPlan
import com.aman.fityatraapp.models.ExerciseAdd
import com.aman.fityatraapp.models.Goal
import com.aman.fityatraapp.models.HealthData
import com.aman.fityatraapp.models.Meal
import com.aman.fityatraapp.models.MealAdd
import com.aman.fityatraapp.models.UserData
import java.util.Calendar


class SQLiteUtils(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "userhealthdata.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_HEALTH = "health"
        private const val COLUMN_ID = "id"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_STEP_COUNT = "step_count"
        private const val COLUMN_CALORIE_INTAKE = "calorie_intake"
        private const val COLUMN_CALORIE_BURN = "calorie_burn"
        private const val COLUMN_WEIGHT = "weight"
        private const val COLUMN_GLUCOSE_LEVEL = "glucose_level"
        private const val COLUMN_EXERCISES = "exercises"
        private const val COLUMN_MEALS = "meals"

        private const val TABLE_USER_DATA = "user_data"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_AGE = "age"
        private const val COLUMN_HEIGHT = "height"
        private const val COLUMN_GENDER = "gender"
        private const val COLUMN_MEAL_PREFERENCES = "meal_preferences"
        private const val COLUMN_EXERCISE_FREQUENCY = "exercise_frequency"
        private const val COLUMN_FITNESS_GOALS = "fitness_goals"
        private const val COLUMN_SLEEP_SCHEDULE = "sleep_schedule"
        private const val COLUMN_MEDICAL_PROBLEMS = "medical_problems"

        private const val TABLE_GOALS = "goals"
        private const val COLUMN_GOAL_ID = "goal_id"
        private const val COLUMN_GOAL_TYPE = "goal_type"
        private const val COLUMN_GOAL_VALUE = "goal_value"

        const val TABLE_DIET_PLAN = "DietPlan"
        const val TABLE_MEAL = "Meal"

        const val COLUMN_DIET_ID = "dietId"
        const val COLUMN_DIET_DAY = "day"
        const val COLUMN_DIET_MEAL_TIME = "mealTime"
        const val COLUMN_DIET_MEAL_ID = "mealId"

        const val COLUMN_MEAL_ID = "mealId"
        const val COLUMN_MEAL_CARBOHYDRATE = "carbohydrateByDifference"
        const val COLUMN_MEAL_CATEGORY = "category"
        const val COLUMN_MEAL_ENERGY = "energy"
        const val COLUMN_MEAL_FOOD_NAME = "foodName"
        const val COLUMN_MEAL_MEAL = "meal"
        const val COLUMN_MEAL_PROTEIN = "protein"
        const val COLUMN_MEAL_SUGARS = "sugarsTotalIncludingNLEA"
        const val COLUMN_MEAL_LIPID_FAT = "totalLipidFat"


    }

    override fun onCreate(db: SQLiteDatabase) {
        val createHealthTable = """
            CREATE TABLE $TABLE_HEALTH (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_DATE INTEGER,
                $COLUMN_STEP_COUNT INTEGER,
                $COLUMN_CALORIE_INTAKE INTEGER,
                $COLUMN_CALORIE_BURN INTEGER,
                $COLUMN_WEIGHT REAL,
                $COLUMN_GLUCOSE_LEVEL REAL,
                $COLUMN_EXERCISES TEXT,
                $COLUMN_MEALS TEXT
            )
        """
        db.execSQL(createHealthTable)

        val createUserDataTable = """
            CREATE TABLE $TABLE_USER_DATA (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT,
                $COLUMN_AGE INTEGER,
                $COLUMN_HEIGHT INTEGER,
                $COLUMN_WEIGHT REAL,
                $COLUMN_GENDER TEXT,
                $COLUMN_MEAL_PREFERENCES TEXT,
                $COLUMN_EXERCISE_FREQUENCY TEXT,
                $COLUMN_FITNESS_GOALS TEXT,
                $COLUMN_SLEEP_SCHEDULE TEXT,
                $COLUMN_MEDICAL_PROBLEMS TEXT
            )
        """
        db.execSQL(createUserDataTable)

        val createGoalsTable = """
            CREATE TABLE $TABLE_GOALS (
                $COLUMN_GOAL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_GOAL_TYPE TEXT,
                $COLUMN_GOAL_VALUE INTEGER
            )
        """
        db.execSQL(createGoalsTable)

        val createDietPlanTable = """
    CREATE TABLE $TABLE_DIET_PLAN (
        $COLUMN_DIET_ID INTEGER PRIMARY KEY AUTOINCREMENT,
        $COLUMN_DIET_DAY TEXT,
        $COLUMN_DIET_MEAL_TIME TEXT,
        $COLUMN_DIET_MEAL_ID INTEGER,
        FOREIGN KEY ($COLUMN_DIET_MEAL_ID) REFERENCES $TABLE_MEAL($COLUMN_MEAL_ID)
    )
"""
        db.execSQL(createDietPlanTable)

        val createMealTable = """
    CREATE TABLE $TABLE_MEAL (
        $COLUMN_MEAL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
        $COLUMN_MEAL_CARBOHYDRATE REAL,
        $COLUMN_MEAL_CATEGORY TEXT,
        $COLUMN_MEAL_ENERGY REAL,
        $COLUMN_MEAL_FOOD_NAME TEXT,
        $COLUMN_MEAL_MEAL TEXT,
        $COLUMN_MEAL_PROTEIN REAL,
        $COLUMN_MEAL_SUGARS REAL,
        $COLUMN_MEAL_LIPID_FAT REAL
    )
"""
        db.execSQL(createMealTable)


        insertDefaultGoals(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_HEALTH")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER_DATA")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_GOALS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_DIET_PLAN")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_MEAL")

        onCreate(db)
    }

    fun addOrUpdateHealthData(
        exercises: List<ExerciseAdd>?,
        meals: List<MealAdd>?,
        stepCount: Int?,
        calorieIntake: Int?,
        calorieBurn: Int?,
        weight: Float?,
        glucoseLevel: Float?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        Log.d("addOrUpdateHealthData", "Starting function")
        val db = writableDatabase
        val today = getStartOfTodayInMillis()

        db.beginTransaction()
        var cursor: Cursor? = null
        try {
            cursor = db.query(
                TABLE_HEALTH,
                null,
                "$COLUMN_DATE = ?",
                arrayOf(today.toString()),
                null,
                null,
                null
            )

            if (cursor.moveToFirst()) {
                Log.d("addOrUpdateHealthData", "Updating existing data for today")
                // Data exists for today, update it
                val existingStepCount = getColumnValue(cursor, COLUMN_STEP_COUNT) { getInt(it) } ?: 0
                val existingCalorieIntake = getColumnValue(cursor, COLUMN_CALORIE_INTAKE) { getInt(it) } ?: 0
                val existingCalorieBurn = getColumnValue(cursor, COLUMN_CALORIE_BURN) { getInt(it) } ?: 0
                val existingWeight = getColumnValue(cursor, COLUMN_WEIGHT) { getFloat(it) } ?: 0f
                val existingGlucoseLevel = getColumnValue(cursor, COLUMN_GLUCOSE_LEVEL) { getFloat(it) } ?: 0f

                val existingExercises = getColumnValue(cursor, COLUMN_EXERCISES) { getString(it) }
                    ?.split(";")
                    ?.mapNotNull {
                        val parts = it.split(":")
                        if (parts.size == 2) {
                            val name = parts[0]
                            val duration = parts[1].toIntOrNull()
                            if (duration != null) ExerciseAdd(name, duration) else null
                        } else {
                            null
                        }
                    }
                    ?.toMutableList() ?: mutableListOf()

                val existingMeals = getColumnValue(cursor, COLUMN_MEALS) { getString(it) }
                    ?.split(";")
                    ?.mapNotNull {
                        val parts = it.split(":")
                        if (parts.size == 2) {
                            val name = parts[0]
                            val quantity = parts[1].toIntOrNull()
                            if (quantity != null) MealAdd(name, quantity) else null
                        } else {
                            null
                        }
                    }
                    ?.toMutableList() ?: mutableListOf()

                // Update exercises
                exercises?.forEach { exercise ->
                    val existingExercise = existingExercises.find { it.exerciseName == exercise.exerciseName }
                    if (existingExercise != null) {
                        existingExercise.duration += exercise.duration
                    } else {
                        existingExercises.add(exercise)
                    }
                }

                // Update meals
                meals?.forEach { meal ->
                    val existingMeal = existingMeals.find { it.dishName == meal.dishName }
                    if (existingMeal != null) {
                        existingMeal.quantity += meal.quantity
                    } else {
                        existingMeals.add(meal)
                    }
                }

                // Prepare content values for update
                val contentValues = ContentValues().apply {
                    put(COLUMN_DATE, today)
                    put(COLUMN_STEP_COUNT, existingStepCount + (stepCount ?: 0))
                    put(COLUMN_CALORIE_INTAKE, existingCalorieIntake + (calorieIntake ?: 0))
                    put(COLUMN_CALORIE_BURN, existingCalorieBurn + (calorieBurn ?: 0))
                    put(COLUMN_WEIGHT, weight ?: existingWeight)
                    put(COLUMN_GLUCOSE_LEVEL, glucoseLevel ?: existingGlucoseLevel)
                    put(
                        COLUMN_EXERCISES,
                        existingExercises.joinToString(";") { "${it.exerciseName}:${it.duration}" })
                    put(
                        COLUMN_MEALS,
                        existingMeals.joinToString(";") { "${it.dishName}:${it.quantity}" })
                }

                val rowsAffected = db.update(
                    TABLE_HEALTH,
                    contentValues,
                    "$COLUMN_DATE = ?",
                    arrayOf(today.toString())
                )
                if (rowsAffected > 0) {
                    db.setTransactionSuccessful()
                    onSuccess()
                } else {
                    onFailure(Exception("Error updating data"))
                }
            } else {
                Log.d("addOrUpdateHealthData", "Inserting new data for today")
                // Data does not exist for today, insert new
                val contentValues = ContentValues().apply {
                    put(COLUMN_DATE, today)
                    put(COLUMN_STEP_COUNT, stepCount ?: 0)
                    put(COLUMN_CALORIE_INTAKE, calorieIntake ?: 0)
                    put(COLUMN_CALORIE_BURN, calorieBurn ?: 0)
                    put(COLUMN_WEIGHT, weight ?: 0f)
                    put(COLUMN_GLUCOSE_LEVEL, glucoseLevel ?: 0f)
                    put(
                        COLUMN_EXERCISES,
                        exercises?.joinToString(";") { "${it.exerciseName}:${it.duration}" } ?: "")
                    put(
                        COLUMN_MEALS,
                        meals?.joinToString(";") { "${it.dishName}:${it.quantity}" } ?: "")
                }

                val rowId = db.insertWithOnConflict(
                    TABLE_HEALTH,
                    null,
                    contentValues,
                    SQLiteDatabase.CONFLICT_REPLACE
                )
                if (rowId != -1L) {
                    db.setTransactionSuccessful()
                    onSuccess()
                } else {
                    onFailure(Exception("Error inserting data"))
                }
            }
        } catch (e: Exception) {
            Log.e("addOrUpdateHealthData", "Exception: ${e.message}", e)
            onFailure(e)
        } finally {
            cursor?.close()
            db.endTransaction()
            Log.d("addOrUpdateHealthData", "Transaction ended")
        }
    }



    fun getStartOfTodayInMillis(): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
    private fun getStartOfDateInMillis(calendar: Calendar): Long {
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }


    private inline fun <T> getColumnValue(
        cursor: Cursor,
        columnName: String,
        extractor: Cursor.(Int) -> T
    ): T? {
        val columnIndex = cursor.getColumnIndex(columnName)
        return if (columnIndex != -1) {
            cursor.extractor(columnIndex)
        } else {
            null
        }
    }


    fun getTodayHealthData(
        onSuccess: (HealthData?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = readableDatabase
        val today = getStartOfTodayInMillis()

        val cursor = db.query(
            TABLE_HEALTH,
            null,
            "$COLUMN_DATE = ?",
            arrayOf(today.toString()),
            null,
            null,
            null
        )

        try {
            if (cursor.moveToFirst()) {
                val meals = getColumnValue(cursor, COLUMN_MEALS) { getString(it) }
                    ?.split(";")
                    ?.mapNotNull {
                        val parts = it.split(":")
                        if (parts.size == 2) {
                            MealAdd(parts[0], parts[1].toIntOrNull() ?: 0)
                        } else {
                            null
                        }
                    }
                    ?.toMutableList() ?: mutableListOf()

                val exercises = getColumnValue(cursor, COLUMN_EXERCISES) { getString(it) }
                    ?.split(";")
                    ?.mapNotNull {
                        val parts = it.split(":")
                        if (parts.size == 2) {
                            ExerciseAdd(parts[0], parts[1].toIntOrNull() ?: 0)
                        } else {
                            null
                        }
                    }
                    ?.toMutableList() ?: mutableListOf()

                val stepCount = getColumnValue(cursor, COLUMN_STEP_COUNT) { getInt(it) }
                val calorieIntake = getColumnValue(cursor, COLUMN_CALORIE_INTAKE) { getInt(it) }
                val calorieBurn = getColumnValue(cursor, COLUMN_CALORIE_BURN) { getInt(it) }
                val weight = getColumnValue(cursor, COLUMN_WEIGHT) { getFloat(it) }
                val glucoseLevel = getColumnValue(cursor, COLUMN_GLUCOSE_LEVEL) { getFloat(it) }
                val date = getColumnValue(cursor, COLUMN_DATE) { getLong(it) }

                val healthData = HealthData(
                    exercises = exercises,
                    meals = meals,
                    stepCount = stepCount ?: 0,
                    calorieIntake = calorieIntake ?: 0,
                    calorieBurn = calorieBurn ?: 0,
                    weight = weight ?: 0.0f,
                    glucoseLevel = glucoseLevel ?: 0.0f,
                    date = date ?: 0L
                )


                Log.d("getTodayHealthData", "Successfully retrieved data: $healthData")
                onSuccess(healthData)
            } else {
                Log.d("getTodayHealthData", "No data found for today")
                onSuccess(null)
            }
        } catch (e: Exception) {
            Log.e("getTodayHealthData", "Exception: ${e.message}", e)
            onFailure(e)
        } finally {
            cursor.close()
        }
    }



    fun getLast7DaysData(
        onSuccess: (List<HealthData>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = readableDatabase
        val healthDataList = mutableListOf<HealthData>()

        try {
            val currentDate = Calendar.getInstance()

            for (i in 0 downTo -6) {
                val date = currentDate.clone() as Calendar
                date.add(Calendar.DATE, i)
                val dayTimestamp = getStartOfDateInMillis(date)

                Log.d("dateOf", date.toString())

                val cursor = db.query(
                    TABLE_HEALTH,
                    null,
                    "$COLUMN_DATE = ?",
                    arrayOf(dayTimestamp.toString()),
                    null,
                    null,
                    null
                )

                try {
                    if (cursor.moveToFirst()) {
                        val meals = getColumnValue(cursor, COLUMN_MEALS) { getString(it) }
                            ?.split(";")
                            ?.mapNotNull {
                                val parts = it.split(":")
                                if (parts.size == 2) {
                                    MealAdd(parts[0], parts[1].toIntOrNull() ?: 0)
                                } else {
                                    null
                                }
                            }
                            ?.toMutableList() ?: mutableListOf()

                        val exercises = getColumnValue(cursor, COLUMN_EXERCISES) { getString(it) }
                            ?.split(";")
                            ?.mapNotNull {
                                val parts = it.split(":")
                                if (parts.size == 2) {
                                    ExerciseAdd(parts[0], parts[1].toIntOrNull() ?: 0)
                                } else {
                                    null
                                }
                            }
                            ?.toMutableList() ?: mutableListOf()

                        val stepCount = getColumnValue(cursor, COLUMN_STEP_COUNT) { getInt(it) }
                        val calorieIntake = getColumnValue(cursor, COLUMN_CALORIE_INTAKE) { getInt(it) }
                        val calorieBurn = getColumnValue(cursor, COLUMN_CALORIE_BURN) { getInt(it) }
                        val weight = getColumnValue(cursor, COLUMN_WEIGHT) { getFloat(it) }
                        val glucoseLevel = getColumnValue(cursor, COLUMN_GLUCOSE_LEVEL) { getFloat(it) }
                        val dateValue = getColumnValue(cursor, COLUMN_DATE) { getLong(it) }

                        val healthData = HealthData(
                            exercises = exercises,
                            meals = meals,
                            stepCount = stepCount ?: 0,
                            calorieIntake = calorieIntake ?: 0,
                            calorieBurn = calorieBurn ?: 0,
                            weight = weight ?: 0.0f,
                            glucoseLevel = glucoseLevel ?: 0.0f,
                            date = dateValue ?: 0L
                        )

                        healthDataList.add(healthData)
                    }
                } finally {
                    cursor.close()
                }
            }

            // Reverse the list to get data in reverse chronological order
            healthDataList.reverse()

            onSuccess(healthDataList)
        } catch (e: Exception) {
            Log.e("getLast7DaysData", "Error fetching last 7 days data", e)
            onFailure(e)
        }
    }





    fun saveDietPlan(dietPlan: List<DietPlan>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            // Delete existing diet plan entries
            db.delete(TABLE_DIET_PLAN, null, null)
            db.delete(TABLE_MEAL, null, null)

            for (dayPlan in dietPlan) {
                saveMeal(db, "Day 1", "Breakfast", dayPlan.day1.Breakfast)
                saveMeal(db, "Day 1", "Lunch", dayPlan.day1.Lunch)
                saveMeal(db, "Day 1", "Dinner", dayPlan.day1.Dinner)

                saveMeal(db, "Day 2", "Breakfast", dayPlan.day2.Breakfast)
                saveMeal(db, "Day 2", "Lunch", dayPlan.day2.Lunch)
                saveMeal(db, "Day 2", "Dinner", dayPlan.day2.Dinner)

                saveMeal(db, "Day 3", "Breakfast", dayPlan.day3.Breakfast)
                saveMeal(db, "Day 3", "Lunch", dayPlan.day3.Lunch)
                saveMeal(db, "Day 3", "Dinner", dayPlan.day3.Dinner)

                saveMeal(db, "Day 4", "Breakfast", dayPlan.day4.Breakfast)
                saveMeal(db, "Day 4", "Lunch", dayPlan.day4.Lunch)
                saveMeal(db, "Day 4", "Dinner", dayPlan.day4.Dinner)

                saveMeal(db, "Day 5", "Breakfast", dayPlan.day5.Breakfast)
                saveMeal(db, "Day 5", "Lunch", dayPlan.day5.Lunch)
                saveMeal(db, "Day 5", "Dinner", dayPlan.day5.Dinner)

                saveMeal(db, "Day 6", "Breakfast", dayPlan.day6.Breakfast)
                saveMeal(db, "Day 6", "Lunch", dayPlan.day6.Lunch)
                saveMeal(db, "Day 6", "Dinner", dayPlan.day6.Dinner)

                saveMeal(db, "Day 7", "Breakfast", dayPlan.day7.Breakfast)
                saveMeal(db, "Day 7", "Lunch", dayPlan.day7.Lunch)
                saveMeal(db, "Day 7", "Dinner", dayPlan.day7.Dinner)
            }

            db.setTransactionSuccessful()
            Log.d("SQLiteUtils", "Diet plan saved successfully")
        } catch (e: Exception) {
            Log.e("SQLiteUtils", "Error saving diet plan: ${e.message}")
            throw e
        } finally {
            db.endTransaction()
        }
    }

    private fun saveMeal(db: SQLiteDatabase, day: String, mealTime: String, meal: Meal) {
        val contentValues = ContentValues().apply {
            put(COLUMN_MEAL_CARBOHYDRATE, meal.carbohydrateByDifference)
            put(COLUMN_MEAL_CATEGORY, meal.category)
            put(COLUMN_MEAL_ENERGY, meal.energy)
            put(COLUMN_MEAL_FOOD_NAME, meal.foodName)
            put(COLUMN_MEAL_MEAL, meal.meal)
            put(COLUMN_MEAL_PROTEIN, meal.protein)
            put(COLUMN_MEAL_SUGARS, meal.sugarsTotalIncludingNLEA)
            put(COLUMN_MEAL_LIPID_FAT, meal.totalLipidFat)
        }
        val mealId = db.insertWithOnConflict(
            TABLE_MEAL,
            null,
            contentValues,
            SQLiteDatabase.CONFLICT_REPLACE
        ).toInt()

        val dietPlanValues = ContentValues().apply {
            put(COLUMN_DIET_DAY, day)
            put(COLUMN_DIET_MEAL_TIME, mealTime)
            put(COLUMN_DIET_MEAL_ID, mealId)
        }
        db.insertWithOnConflict(
            TABLE_DIET_PLAN,
            null,
            dietPlanValues,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun getSavedDietPlan(
        onSuccess: (List<DietPlan>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = readableDatabase
        val dietPlanList = mutableListOf<DietPlan>()

        try {
            val days = listOf("Day 1", "Day 2", "Day 3", "Day 4", "Day 5", "Day 6", "Day 7")
            val dietPlan = DietPlan()

            for (day in days) {
                val breakfast = getMeal(db, day, "Breakfast")
                val lunch = getMeal(db, day, "Lunch")
                val dinner = getMeal(db, day, "Dinner")

                when (day) {
                    "Day 1" -> dietPlan.day1 = DayPlan(breakfast, lunch, dinner)
                    "Day 2" -> dietPlan.day2 = DayPlan(breakfast, lunch, dinner)
                    "Day 3" -> dietPlan.day3 = DayPlan(breakfast, lunch, dinner)
                    "Day 4" -> dietPlan.day4 = DayPlan(breakfast, lunch, dinner)
                    "Day 5" -> dietPlan.day5 = DayPlan(breakfast, lunch, dinner)
                    "Day 6" -> dietPlan.day6 = DayPlan(breakfast, lunch, dinner)
                    "Day 7" -> dietPlan.day7 = DayPlan(breakfast, lunch, dinner)
                }
            }

            dietPlanList.add(dietPlan)
            onSuccess(dietPlanList)
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    private fun getMeal(db: SQLiteDatabase, day: String, mealTime: String): Meal {
        val cursor = db.query(
            TABLE_DIET_PLAN,
            arrayOf(COLUMN_DIET_MEAL_ID),
            "$COLUMN_DIET_DAY = ? AND $COLUMN_DIET_MEAL_TIME = ?",
            arrayOf(day, mealTime),
            null,
            null,
            null
        )

        return if (cursor.moveToFirst()) {
            val mealId = getColumnValue(cursor, COLUMN_DIET_MEAL_ID) { getInt(it) } ?: 0
            cursor.close()

            val mealCursor = db.query(
                TABLE_MEAL,
                null,
                "$COLUMN_MEAL_ID = ?",
                arrayOf(mealId.toString()),
                null,
                null,
                null
            )

            if (mealCursor.moveToFirst()) {
                val meal = Meal(
                    carbohydrateByDifference = getColumnValue(
                        mealCursor,
                        COLUMN_MEAL_CARBOHYDRATE
                    ) { getDouble(it) } ?: 0.0,
                    category = getColumnValue(mealCursor, COLUMN_MEAL_CATEGORY) { getString(it) }
                        ?: "",
                    energy = getColumnValue(mealCursor, COLUMN_MEAL_ENERGY) { getDouble(it) }
                        ?: 0.0,
                    foodName = getColumnValue(mealCursor, COLUMN_MEAL_FOOD_NAME) { getString(it) }
                        ?: "",
                    meal = getColumnValue(mealCursor, COLUMN_MEAL_MEAL) { getString(it) } ?: "",
                    protein = getColumnValue(mealCursor, COLUMN_MEAL_PROTEIN) { getDouble(it) }
                        ?: 0.0,
                    sugarsTotalIncludingNLEA = getColumnValue(
                        mealCursor,
                        COLUMN_MEAL_SUGARS
                    ) { getDouble(it) } ?: 0.0,
                    totalLipidFat = getColumnValue(
                        mealCursor,
                        COLUMN_MEAL_LIPID_FAT
                    ) { getDouble(it) } ?: 0.0
                )
                mealCursor.close()
                meal
            } else {
                mealCursor.close()
                Meal()
            }
        } else {
            cursor.close()
            Meal()
        }
    }


    fun saveUserData(userData: UserData) {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(COLUMN_NAME, userData.name)
            put(COLUMN_AGE, userData.Age)
            put(COLUMN_HEIGHT, userData.Height)
            put(COLUMN_WEIGHT, userData.Weight)
            put(COLUMN_GENDER, userData.Sex)
            put(COLUMN_MEAL_PREFERENCES, userData.Preference)
            put(COLUMN_EXERCISE_FREQUENCY, userData.Activity)
            put(COLUMN_FITNESS_GOALS, userData.Goal)
            put(COLUMN_SLEEP_SCHEDULE, userData.sleepSchedule)
            put(COLUMN_MEDICAL_PROBLEMS, userData.medicalProblems)
        }
        val rowId = db.insertWithOnConflict(
            TABLE_USER_DATA,
            null,
            contentValues,
            SQLiteDatabase.CONFLICT_REPLACE
        )
        if (rowId != -1L) {
            Log.d("SQLiteUtils", "User data saved successfully")
        } else {
            Log.e("SQLiteUtils", "Error saving user data")
        }
    }

    fun getUserData(onSuccess: (UserData?) -> Unit, onFailure: (Exception) -> Unit) {
        val db = readableDatabase
        val cursor = db.query(TABLE_USER_DATA, null, null, null, null, null, null)

        try {
            if (cursor.moveToFirst()) {
                val userData = UserData(
                    name = getColumnValue(cursor, COLUMN_NAME) { getString(it) } ?: "",
                    Age = getColumnValue(cursor, COLUMN_AGE) { getInt(it) } ?: 0,
                    Height = getColumnValue(cursor, COLUMN_HEIGHT) { getInt(it) } ?: 0,
                    Weight = getColumnValue(cursor, COLUMN_WEIGHT) { getFloat(it) } ?: 0.0f,
                    Sex = getColumnValue(cursor, COLUMN_GENDER) { getString(it) } ?: "",
                    Preference = getColumnValue(cursor, COLUMN_MEAL_PREFERENCES) { getString(it) }
                        ?: "",
                    Activity = getColumnValue(cursor, COLUMN_EXERCISE_FREQUENCY) { getDouble(it) }
                        ?: 0.0,
                    Goal = getColumnValue(cursor, COLUMN_FITNESS_GOALS) { getString(it) } ?: "",
                    sleepSchedule = getColumnValue(cursor, COLUMN_SLEEP_SCHEDULE) { getString(it) }
                        ?: "",
                    medicalProblems = getColumnValue(
                        cursor,
                        COLUMN_MEDICAL_PROBLEMS
                    ) { getString(it) } ?: ""
                )
                onSuccess(userData)
            } else {
                onSuccess(null)
            }
        } catch (e: Exception) {
            onFailure(e)
        } finally {
            cursor.close()
        }
    }

    fun isUserDataAvailable(): Boolean {
        val db = readableDatabase
        var cursor: Cursor? = null
        return try {
            cursor = db.rawQuery("SELECT 1 FROM $TABLE_USER_DATA LIMIT 1", null)
            cursor?.moveToFirst() == true
        } catch (e: Exception) {
            Log.e("SQLiteUtils", "Error checking user data availability: ${e.message}")
            false
        } finally {
            cursor?.close()
        }
    }


    private fun insertDefaultGoals(db: SQLiteDatabase) {
        val goals = listOf(
            Goal("step_count", 6000),
            Goal("calorie_intake", 3000),
            Goal("calorie_burn", 3000)
        )

        for (goal in goals) {
            val initialValues = ContentValues().apply {
                put(COLUMN_GOAL_TYPE, goal.goalType)
                put(COLUMN_GOAL_VALUE, goal.goalValue)
            }
            db.insert(TABLE_GOALS, null, initialValues)
        }
    }


    fun updateGoal(goal: Goal, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val contentValues = ContentValues().apply {
                put(COLUMN_GOAL_VALUE, goal.goalValue)
            }
            val rowsAffected = db.update(
                TABLE_GOALS,
                contentValues,
                "$COLUMN_GOAL_TYPE = ?",
                arrayOf(goal.goalType)
            )
            if (rowsAffected > 0) {
                db.setTransactionSuccessful()
                onSuccess()
            } else {
                onFailure(Exception("Error updating goal"))
            }
        } catch (e: Exception) {
            onFailure(e)
        } finally {
            db.endTransaction()
        }
    }

    fun getGoals(
        onSuccess: (List<Goal>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = readableDatabase
        val goalsList = mutableListOf<Goal>()

        try {
            val cursor = db.query(
                TABLE_GOALS, null, null, null, null, null, null
            )

            while (cursor.moveToNext()) {
                val goalType = getColumnValue(cursor, COLUMN_GOAL_TYPE) { getString(it) } ?: ""
                val goalValue = getColumnValue(cursor, COLUMN_GOAL_VALUE) { getInt(it) } ?: 0

                val goal = Goal(goalType, goalValue)
                goalsList.add(goal)
            }

            cursor.close()
            onSuccess(goalsList)
        } catch (e: Exception) {
            Log.e("SQLiteUtils", "Error fetching goals: ${e.message}")
            onFailure(e)
        }
    }
}
