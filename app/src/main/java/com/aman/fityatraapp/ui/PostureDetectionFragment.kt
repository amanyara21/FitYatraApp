package com.aman.fityatraapp.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aman.fityatraapp.R
import com.aman.fityatraapp.models.Point
import com.aman.fityatraapp.utils.ActivityAdapter
import com.aman.fityatraapp.utils.FirebaseUtils
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import kotlin.math.abs
import kotlin.math.atan2


class PostureDetectionFragment : Fragment() {

    companion object {
        private const val REQUEST_GALLERY = 123
    }

    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var title: TextView
    private lateinit var uploadButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ActivityAdapter
    private var firebaseUtils = FirebaseUtils()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_posture_detection, container, false)
        (activity as AppCompatActivity).supportActionBar?.hide()

        uploadButton = view.findViewById(R.id.uploadButton)
        imageView = view.findViewById(R.id.imageView)
        textView = view.findViewById(R.id.resultTextView)
        title = view.findViewById(R.id.headerTitle)
        recyclerView = view.findViewById(R.id.exerciseRecyclerView)

        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        title.text = getString(R.string.check_posture_problem)

        uploadButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_GALLERY)
        }

        return view
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GALLERY && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.data
            imageUri?.let {
                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, it)
                imageView.setImageURI(imageUri)
                runPose(bitmap)
            }
        }
    }

    private fun runPose(bitmap: Bitmap) {
        val poseDetector = PoseDetection.getClient(
            PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.SINGLE_IMAGE_MODE)
                .build()
        )

        val image = InputImage.fromBitmap(bitmap, 0)
        poseDetector.process(image)
            .addOnSuccessListener { pose ->
                processPose(pose)
            }
            .addOnFailureListener { _ ->
                Toast.makeText(
                    requireContext(),
                    "Pose detection failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun calculateAngle(a: Point, b: Point, c: Point): Double {
        var ang = Math.toDegrees(
            atan2(
                c.y - b.y.toDouble(),
                c.x - b.x.toDouble()
            ) - atan2(a.y - b.y.toDouble(), a.x - b.x.toDouble())
        )
        ang = if (ang < 0) ang + 360 else ang
        return abs(ang)
    }

    private fun detectPosture(
        hip: Point,
        knee: Point,
        ankle: Point,
        shoulder: Point,
        ear: Point,
        eye: Point
    ): String {
        val lowerBackAngle = calculateAngle(hip, knee, ankle)
        val upperBackAngle = calculateAngle(shoulder, hip, knee)
        val neckAngle = calculateAngle(shoulder, ear, eye)

        return when {
            neckAngle > 180 -> "Forward head"
            lowerBackAngle > 200 -> "Lordosis"
            upperBackAngle < 160 -> "Kyphosis"
            upperBackAngle > 200 -> "Flatback"
            else -> "Healthy Posture"
        }
    }

    private fun processPose(pose: Pose) {
        val rightEar =
            pose.getPoseLandmark(PoseLandmark.RIGHT_EAR)?.position?.let { Point(it.x, it.y) }
        val rightEye =
            pose.getPoseLandmark(PoseLandmark.RIGHT_EYE)?.position?.let { Point(it.x, it.y) }
        val rightShoulder =
            pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)?.position?.let { Point(it.x, it.y) }
        val rightHip =
            pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)?.position?.let { Point(it.x, it.y) }
        val rightKnee =
            pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)?.position?.let { Point(it.x, it.y) }
        val rightAnkle =
            pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)?.position?.let { Point(it.x, it.y) }

        val leftEar =
            pose.getPoseLandmark(PoseLandmark.LEFT_EAR)?.position?.let { Point(it.x, it.y) }
        val leftEye =
            pose.getPoseLandmark(PoseLandmark.LEFT_EYE)?.position?.let { Point(it.x, it.y) }
        val leftShoulder =
            pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)?.position?.let { Point(it.x, it.y) }
        val leftHip =
            pose.getPoseLandmark(PoseLandmark.LEFT_HIP)?.position?.let { Point(it.x, it.y) }
        val leftKnee =
            pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)?.position?.let { Point(it.x, it.y) }
        val leftAnkle =
            pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)?.position?.let { Point(it.x, it.y) }
        var posture: String? = ""
        if (rightEar != null && rightEye != null && rightShoulder != null && rightHip != null && rightKnee != null && rightAnkle != null) {
            posture =
                detectPosture(rightHip, rightKnee, rightAnkle, rightShoulder, rightEar, rightEye)
            textView.text = posture
        }

        if (leftEar != null && leftEye != null && leftShoulder != null && leftHip != null && leftKnee != null && leftAnkle != null) {
            posture = detectPosture(leftHip, leftKnee, leftAnkle, leftShoulder, leftEar, leftEye)
            println("Left side posture: $posture")
            textView.text = posture
        }

        if (posture != null) {
            getActivities(posture)
        }
    }

    private fun getActivities(postureProblem: String) {
        Log.d("posture", postureProblem)
        firebaseUtils.getActivitiesByCure(postureProblem) { activities ->
            activities.let {
                adapter = ActivityAdapter(it)
                recyclerView.adapter = adapter
            }
        }
    }
}
