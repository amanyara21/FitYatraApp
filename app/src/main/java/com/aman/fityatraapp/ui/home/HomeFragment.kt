package com.aman.fityatraapp.ui.home

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.aman.fityatraapp.ExerciseAddActivity
import com.aman.fityatraapp.MealActivity
import com.aman.fityatraapp.R
import com.aman.fityatraapp.databinding.FragmentHomeBinding
import com.aman.fityatraapp.utils.FirebaseUtils
import com.google.firebase.auth.FirebaseAuth


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var firebaseUtils: FirebaseUtils
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        firebaseUtils = FirebaseUtils()
        (activity as AppCompatActivity).supportActionBar?.hide()
        observeViewModel()

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addMealCard.setOnClickListener {
            val intent = Intent(activity, MealActivity::class.java)
            startActivity(intent)
        }

        binding.addExercisesCard.setOnClickListener {
            val intent = Intent(activity, ExerciseAddActivity::class.java)
            startActivity(intent)
        }

        binding.addWeightCard.setOnClickListener {
            openWeightEditor()
        }

        binding.addGlucoseCard.setOnClickListener {
            openGlucoseEditor()
        }

        binding.weightEditIcon.setOnClickListener {
            openWeightEditor()
        }

        binding.glucoseEditIcon.setOnClickListener {
            openGlucoseEditor()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun observeViewModel() {
        binding.nameTextView.text = FirebaseAuth.getInstance().currentUser?.displayName
    }
    private fun openWeightEditor() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_edit_weight, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.editTextWeight)

        with(builder) {
            setTitle("Edit Weight")
            setView(dialogLayout)
            setPositiveButton("Save") { _, _ ->
                val weight = editText.text.toString().toFloat()

                if (weight!=0.0f) {
                    firebaseUtils.addOrUpdateHealthData(emptyList(), emptyList(), 0, 0, 0,weight, 0.0f, onSuccess = {}, onFailure = {})
                } else {
                    Toast.makeText(requireContext(), "Weight cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            show()
        }
    }

    private fun openGlucoseEditor() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_edit_glucose, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.editTextGlucose)

        with(builder) {
            setTitle("Edit Glucose Level")
            setView(dialogLayout)
            setPositiveButton("Save") { _, _ ->
                val glucose = editText.text.toString().toFloat()
                if (glucose!=0.0f) {
                    firebaseUtils.addOrUpdateHealthData(emptyList(), emptyList(), 0, 0, 0,0.0f, glucose , onSuccess = {
                                Toast.makeText(requireContext(), "Added Glucose Level successfully", Toast.LENGTH_SHORT).show()
                    }, onFailure = {
                        Toast.makeText(requireContext(), "Error in adding Glucose Level", Toast.LENGTH_SHORT).show()
                    })
                } else {
                    Toast.makeText(requireContext(), "Glucose level cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            show()
        }
    }

}

