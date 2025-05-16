package com.aman.fityatraapp.viewModel

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.util.Log
import androidx.camera.core.ImageProxy
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions

import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

class CameraViewModel : ViewModel() {

    private val _exerciseCount = MutableLiveData<Int>()
    val exerciseCount: LiveData<Int> = _exerciseCount


    private var poseDetector: PoseDetector = PoseDetection.getClient(
        PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
    )

    private var mPaint: Paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 10f
    }

    private var lastCheckTime = 0L
    private var exerciseState = -1
    private var prevHeight = 0.0
    private var prevAngle = 0.0
    private var exerciseType = ""
    private val throttleTimeMillis = 250L

    private val _exerciseCounter = MutableLiveData(0)
    val exerciseCounter: LiveData<Int> = _exerciseCounter

    private val _bitmapToDraw = MutableLiveData<Bitmap>()
    val bitmapToDraw: LiveData<Bitmap> = _bitmapToDraw

    fun setExerciseType(type: String) {
        exerciseType = type
    }

    fun processImageProxy(imageProxy: ImageProxy) {
        val byteBuffer = imageProxy.planes[0].buffer
        byteBuffer.rewind()
        val bitmap = Bitmap.createBitmap(
            imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888
        ).apply {
            copyPixelsFromBuffer(byteBuffer)
        }

        val rotatedBitmap = rotateAndMirrorBitmap(bitmap)
        detectPose(rotatedBitmap)

        imageProxy.close()
    }
    private fun rotateAndMirrorBitmap(bitmap: Bitmap): Bitmap {
        val matrix = Matrix().apply {
            postRotate(270f)
            postScale(-1f, 1f)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }


    private fun detectPose(bitmap: Bitmap) {
        poseDetector.process(InputImage.fromBitmap(bitmap, 0))
            .addOnSuccessListener { pose ->
                drawLandmarksOnBitmap(bitmap, pose)
                _bitmapToDraw.postValue(bitmap)
                processPose(pose, exerciseType)
            }
            .addOnFailureListener { }
    }

    private fun drawLandmarksOnBitmap(bitmap: Bitmap, pose: Pose) {
        val canvas = Canvas(bitmap)
        for (landmark in pose.allPoseLandmarks) {
            canvas.drawCircle(landmark.position.x, landmark.position.y, 5f, mPaint)
        }
    }
    private fun processPose(pose: Pose, exercise: String) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastCheckTime >= throttleTimeMillis) {
            lastCheckTime = currentTime
            when (exercise) {
                "Pushups" -> countPushups(pose)
                "Pullups" -> countPullups(pose)
                "Squats" -> countSquats(pose)
                "Situps" -> countSitups(pose)
            }
        }
    }

    private fun countPushups(pose: Pose) {
        try {
            val lWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
            val lElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
            val lShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
            val lEar = pose.getPoseLandmark(PoseLandmark.LEFT_EAR)
            val rWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
            val rElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
            val rShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
            val rEar = pose.getPoseLandmark(PoseLandmark.RIGHT_EAR)

            val leftAngle = calculateAngle(lWrist, lElbow, lShoulder)
            val rightAngle = calculateAngle(rWrist, rElbow, rShoulder)

            val leftHeight = calculateDistance(lWrist, lEar)
            val rightHeight = calculateDistance(rWrist, rEar)

            val avgHeight = (leftHeight + rightHeight) / 2
            Log.d("Angle", lWrist?.toString() ?: "Hello")

            if (leftAngle > 170 && rightAngle > 170 && avgHeight > prevHeight) {
                mPaint.color = Color.GREEN
                if (exerciseState == 2) incrementCount()
                exerciseState = 0
            } else if (avgHeight < prevHeight && exerciseState == 0) {
                exerciseState = 1
            } else if (avgHeight > prevHeight && exerciseState == 1) {
                exerciseState = 2
            }

            prevHeight = avgHeight
        } catch (_: Exception) {}
    }



    private fun countPullups(pose: Pose) {
        try {
            val lWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
            val rWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
            val nose = pose.getPoseLandmark(PoseLandmark.NOSE)

            val leftDist = calculateDistance(lWrist, nose)
            val rightDist = calculateDistance(rWrist, nose)

            val avg = (leftDist + rightDist) / 2

            if (avg < prevHeight) {
                mPaint.color = Color.GREEN
                if (exerciseState == 2) incrementCount()
                exerciseState = 0
            } else if (avg > prevHeight && exerciseState == 0) {
                exerciseState = 1
            } else if (avg < prevHeight && exerciseState == 1) {
                exerciseState = 2
            }

            prevHeight = avg
        } catch (_: Exception) {}
    }

    private fun countSquats(pose: Pose) {
        try {
            val lHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
            val rHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
            val lAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
            val rAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)
            val lKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
            val rKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)

            val lAngle = calculateAngle(lAnkle, lKnee, lHip)
            val rAngle = calculateAngle(rAnkle, rKnee, rHip)

            val avgHeight = (calculateDistance(lHip, lAnkle) + calculateDistance(rHip, rAnkle)) / 2

            if (lAngle > 170 && rAngle > 170 && avgHeight > prevHeight) {
                mPaint.color = Color.GREEN
                if (exerciseState == 2) incrementCount()
                exerciseState = 0
            } else if (avgHeight < prevHeight && exerciseState == 0) {
                exerciseState = 1
            } else if (avgHeight > prevHeight && exerciseState == 1) {
                exerciseState = 2
            }

            prevHeight = avgHeight
        } catch (_: Exception) {}
    }

    private fun countSitups(pose: Pose) {
        try {
            val lHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
            val rHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
            val lKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
            val rKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
            val lShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
            val rShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)

            val lAngle = calculateAngle(lHip, lKnee, lShoulder)
            val rAngle = calculateAngle(rHip, rKnee, rShoulder)

            val avgAngle = (lAngle + rAngle) / 2

            if (lAngle > 90 && rAngle > 90 && avgAngle > prevAngle) {
                mPaint.color = Color.GREEN
                if (exerciseState == 2) incrementCount()
                exerciseState = 0
            } else if (avgAngle < prevAngle && exerciseState == 0) {
                exerciseState = 1
            } else if (avgAngle > prevAngle && exerciseState == 1) {
                exerciseState = 2
            }

            prevAngle = avgAngle
        } catch (_: Exception) {}
    }

    private fun incrementCount() {
        _exerciseCount.postValue((_exerciseCount.value ?: 0) + 1)
    }

    private fun calculateAngle(p1: PoseLandmark?, p2: PoseLandmark?, p3: PoseLandmark?): Double {
        if (p1 == null || p2 == null || p3 == null) return 0.0
        val radians = atan2(p1.position.y - p2.position.y, p1.position.x - p2.position.x) -
                atan2(p3.position.y - p2.position.y, p3.position.x - p2.position.x)
        var angle = Math.toDegrees(radians.toDouble())
        if (angle < 0) angle += 360.0
        return angle
    }

    private fun calculateDistance(p1: PoseLandmark?, p2: PoseLandmark?): Double {
        if (p1 == null || p2 == null) return 0.0
        return sqrt(
            (p2.position.x - p1.position.x).toDouble().pow(2) +
                    (p2.position.y - p1.position.y).toDouble().pow(2)
        )
    }
}
