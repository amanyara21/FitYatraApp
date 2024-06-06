package com.aman.fityatraapp.ui


import com.aman.fityatraapp.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson

class DietPlannerFragment : Fragment() {

    private lateinit var tableLayout: TableLayout
    private lateinit var databaseReference: DatabaseReference
    private lateinit var uid: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_diet_planner, container, false)

        tableLayout = rootView.findViewById(R.id.tableLayout)
        databaseReference = FirebaseDatabase.getInstance().reference
        uid = FirebaseAuth.getInstance().currentUser!!.uid

        // Fetch diet plan data from Firebase
        fetchDietPlanFromFirebase()

        return rootView
    }

    private fun fetchDietPlanFromFirebase() {
        databaseReference.child("$uid/diettable").addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val jsonString = dataSnapshot.value as? String
                jsonString?.let {
//                    displayDietPlan(it)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }

//    private fun displayDietPlan(jsonString: String) {
//        val dietPlanResponse = Gson().fromJson(jsonString, DietPlanResponse::class.java)
//
//        dietPlanResponse.DayPlan.forEach { (day, meals) ->
//            val tableRow = TableRow(requireContext())
//
//            val dayTextView = TextView(requireContext())
//            dayTextView.text = day
//            tableRow.addView(dayTextView)
//
//            val breakfastTextView = TextView(requireContext())
//            breakfastTextView.text = meals.breakfast.foodName
//            tableRow.addView(breakfastTextView)
//
//            val lunchTextView = TextView(requireContext())
//            lunchTextView.text = meals.lunch.foodName
//            tableRow.addView(lunchTextView)
//
//            val dinnerTextView = TextView(requireContext())
//            dinnerTextView.text = meals.dinner.foodName
//            tableRow.addView(dinnerTextView)
//
//            tableLayout.addView(tableRow)
//        }
//    }
}
