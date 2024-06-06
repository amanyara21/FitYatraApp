package com.aman.fityatraapp

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.os.Bundle
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.aman.fityatraapp.utils.Display
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import java.util.concurrent.ExecutionException
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt


class CameraActivity : AppCompatActivity() {
    private var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>? = null
    private var previewView: PreviewView? = null

    private var options: PoseDetectorOptions = PoseDetectorOptions.Builder()
        .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
        .build()
    private var poseDetector = PoseDetection.getClient(options)
    private var canvas: Canvas? = null
    private var mPaint = Paint()
    private var display: Display? = null
    private var bitmap4Save: Bitmap? = null
    private var bitmapArrayList = ArrayList<Bitmap?>()
    private var bitmap4DisplayArrayList = ArrayList<Bitmap?>()
    private var poseArrayList = ArrayList<Pose>()
    private var isRunning = false
    private var exerciseCount = 0

    private lateinit var exercise: String
    private lateinit var countExerciseText: TextView
    private val THROTTLE_TIME_MILLIS = 250L
    private var lastPushupCheckTime = 0L
    var exerciseState = -1
    private var prevHeight = 0.0
    private var prevAngle = 0.0

    @OptIn(ExperimentalGetImage::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        supportActionBar?.hide()
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        previewView = findViewById(R.id.previewView)
        display = findViewById(R.id.displayOverlay)
        countExerciseText = findViewById(R.id.textCount)
        exercise = intent.getStringExtra("Exercise").toString()
        countExerciseText.text = exerciseCount.toString()
        mPaint.color = Color.RED
        mPaint.style = Paint.Style.FILL_AND_STROKE
        mPaint.strokeWidth = 10f
        cameraProviderFuture!!.addListener({
            try {
                val cameraProvider = cameraProviderFuture!!.get()
                bindPreview(cameraProvider)
            } catch (e: ExecutionException) {
                // No errors
            } catch (e: InterruptedException) {
                // No errors
            }
        }, ContextCompat.getMainExecutor(this))

    }

    var RunMlkit = Runnable {
        poseDetector.process(InputImage.fromBitmap(bitmapArrayList[0]!!, 0))
            .addOnSuccessListener { pose ->
                poseArrayList.add(
                    pose
                )
            }.addOnFailureListener { }
    }


    @ExperimentalGetImage
    fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview = Preview.Builder()
            .build()
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            .build()
        preview.setSurfaceProvider(previewView!!.getSurfaceProvider())
        val imageAnalysis =
            ImageAnalysis.Builder() //                         enable the following line if RGBA output is needed.
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888) //                        .setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
        imageAnalysis.setAnalyzer(
            ActivityCompat.getMainExecutor(this)
        ) { imageProxy ->
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees

            val byteBuffer = imageProxy.image!!.planes[0].buffer
            byteBuffer.rewind()
            val bitmap = Bitmap.createBitmap(
                imageProxy.width,
                imageProxy.height,
                Bitmap.Config.ARGB_8888
            )
            bitmap.copyPixelsFromBuffer(byteBuffer)
            val matrix = Matrix()
            matrix.postRotate(270f)
            matrix.postScale(-1f, 1f)
            val rotatedBitmap = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                imageProxy.width,
                imageProxy.height,
                matrix,
                false
            )
            bitmapArrayList.add(rotatedBitmap)

            if (poseArrayList.size >= 1) {
                canvas = Canvas(bitmapArrayList[0]!!)
                for (poseLandmark in poseArrayList[0].allPoseLandmarks) {
                    canvas!!.drawCircle(
                        poseLandmark.position.x,
                        poseLandmark.position.y,
                        5f,
                        mPaint
                    )
                }
                calculateExercise(poseArrayList[0])

                bitmap4DisplayArrayList.clear()
                bitmap4DisplayArrayList.add(bitmapArrayList[0])
                bitmap4Save = bitmapArrayList[bitmapArrayList.size - 1]
                bitmapArrayList.clear()
                bitmapArrayList.add(bitmap4Save)
                poseArrayList.clear()
                isRunning = false
            }
            if (poseArrayList.size == 0 && bitmapArrayList.size >= 1 && !isRunning) {
                RunMlkit.run()
                isRunning = true
            }
            if (bitmap4DisplayArrayList.size >= 1) {
                display?.getBitmap(bitmap4DisplayArrayList[0])
            }
            imageProxy.close()
        }
        val camera = cameraProvider.bindToLifecycle(
            (this as LifecycleOwner),
            cameraSelector,
            imageAnalysis,
            preview
        )
    }


    private fun calculateExercise(pose: Pose) {
        try {
            val currentTime = System.currentTimeMillis()
            val timeSinceLastCheck = currentTime - lastPushupCheckTime
            if (timeSinceLastCheck >= THROTTLE_TIME_MILLIS) {
                lastPushupCheckTime = currentTime
                when (exercise) {
                    "Pushups" -> countPushups(pose)
                    "Pullups" -> countPullups(pose)
                    "Squats" -> countSquats(pose)
                    "Situps" -> countSitups(pose)
                    else -> {
                        // Handle unsupported exercise
                    }
                }
            }
        } catch (e: Exception) {

        }
    }


    private fun countPushups(pose: Pose) {
        try {
            val landmarks = pose.allPoseLandmarks
            // Left Side
            val leftShoulder = landmarks[PoseLandmark.LEFT_SHOULDER]
            val leftWrist = landmarks[PoseLandmark.LEFT_WRIST]
            val leftElbow = landmarks[PoseLandmark.LEFT_ELBOW]
            val leftEar = landmarks[PoseLandmark.LEFT_EAR]
            // Right Side
            val rightShoulder = landmarks[PoseLandmark.RIGHT_SHOULDER]
            val rightWrist = landmarks[PoseLandmark.RIGHT_WRIST]
            val rightElbow = landmarks[PoseLandmark.RIGHT_ELBOW]
            val rightEar = landmarks[PoseLandmark.RIGHT_EAR]

            val leftAngle =
                calculateAngle(leftWrist.position, leftElbow.position, leftShoulder.position)
            val rightAngle =
                calculateAngle(rightWrist.position, rightElbow.position, rightShoulder.position)

            val leftHeight = calculateDistance(leftWrist.position, leftEar.position)
            val rightHeight = calculateDistance(rightWrist.position, rightEar.position)

            if (leftAngle > 170 && rightAngle > 170 && (leftHeight > prevHeight && rightHeight > prevHeight)) {
                if (exerciseState == 2) {
                    exerciseCount++
                }
                exerciseState = 0
                mPaint.color = Color.GREEN
            } else if (leftHeight < prevHeight && rightHeight < prevHeight && exerciseState == 0) {
                exerciseState = 1
            } else if (leftHeight > prevHeight && rightHeight > prevHeight && exerciseState == 1) {
                exerciseState = 2
            }
            prevHeight = (leftHeight + rightHeight) / 2
            updateExerciseCount()
        } catch (e: Exception) {

        }
    }


    private fun countPullups(pose: Pose) {
        try {
            val landmarks = pose.allPoseLandmarks
            // Left Side
            val leftWrist = landmarks[PoseLandmark.LEFT_WRIST]
            // Right Side
            val rightWrist = landmarks[PoseLandmark.RIGHT_WRIST]
            // Head
            val neck = landmarks[PoseLandmark.NOSE]


            val leftDistance = calculateDistance(leftWrist.position, neck.position)
            val rightDistance = calculateDistance(rightWrist.position, neck.position)

            if (leftDistance < prevHeight && rightDistance < prevHeight) {
                if (exerciseState == 2) {
                    exerciseCount++
                }
                exerciseState = 0
                mPaint.color = Color.GREEN
            } else if (leftDistance > prevHeight && rightDistance > prevHeight && exerciseState == 0) {
                exerciseState = 1
            } else if (leftDistance < prevHeight && rightDistance < prevHeight && exerciseState == 1) {
                exerciseState = 2
            }
            prevHeight = (leftDistance + rightDistance) / 2
            updateExerciseCount()
        } catch (e: Exception) {
//            No errors
        }
    }


    private fun countSquats(pose: Pose) {
        try {
            val landmarks = pose.allPoseLandmarks
            // Hips
            val leftHip = landmarks[PoseLandmark.LEFT_HIP]
            val rightHip = landmarks[PoseLandmark.RIGHT_HIP]
            val leftKnee = landmarks[PoseLandmark.LEFT_KNEE]
            val rightKnee = landmarks[PoseLandmark.RIGHT_KNEE]
            // Ankles
            val leftAnkle = landmarks[PoseLandmark.LEFT_ANKLE]
            val rightAnkle = landmarks[PoseLandmark.RIGHT_ANKLE]

            val leftAngle =
                calculateAngle(leftAnkle.position, leftKnee.position, leftHip.position)
            val rightAngle =
                calculateAngle(rightAnkle.position, rightKnee.position, rightHip.position)

            val hipHeight =
                (calculateDistance(leftHip.position, leftAnkle.position) + calculateDistance(
                    rightHip.position,
                    rightAnkle.position
                )) / 2

            if (leftAngle > 170 && rightAngle > 170 && hipHeight > prevHeight) {
                if (exerciseState == 2) {
                    exerciseCount++
                }
                exerciseState = 0
                mPaint.color = Color.GREEN
            } else if (hipHeight < prevHeight && exerciseState == 0) {
                exerciseState = 1
            } else if (hipHeight > prevHeight && exerciseState == 1) {
                exerciseState = 2
            }
            prevHeight = hipHeight
            updateExerciseCount()
        } catch (e: Exception) {
//            No errors
        }
    }


    private fun countSitups(pose: Pose) {
        try {
            val landmarks = pose.allPoseLandmarks
            // Hips
            val leftHip = landmarks[PoseLandmark.LEFT_HIP]
            val rightHip = landmarks[PoseLandmark.RIGHT_HIP]
            // Knees
            val leftKnee = landmarks[PoseLandmark.LEFT_KNEE]
            val rightKnee = landmarks[PoseLandmark.RIGHT_KNEE]
            // Shoulders
            val leftShoulder = landmarks[PoseLandmark.LEFT_SHOULDER]
            val rightShoulder = landmarks[PoseLandmark.RIGHT_SHOULDER]

            val leftAngle =
                calculateAngle(leftHip.position, leftKnee.position, leftShoulder.position)
            val rightAngle =
                calculateAngle(rightHip.position, rightKnee.position, rightShoulder.position)

            if (leftAngle > 90 && rightAngle > 90 && leftAngle > prevAngle && rightAngle > prevAngle) {
                if (exerciseState == 2) {
                    exerciseCount++
                }
                exerciseState = 0
                mPaint.color = Color.GREEN
            } else if (leftAngle < prevAngle && rightAngle < prevAngle && exerciseState == 0) {
                exerciseState = 1
            } else if (leftAngle > prevAngle && rightAngle > prevAngle && exerciseState == 1) {
                exerciseState = 2
            }
            prevAngle = (leftAngle + rightAngle) / 2
            updateExerciseCount()
        } catch (e: Exception) {
//            No errors
        }
    }


    private fun updateExerciseCount() {
        countExerciseText.text = exerciseCount.toString()
    }


    private fun calculateAngle(firstPoint: PointF, midPoint: PointF, lastPoint: PointF): Double {
        val radian =
            atan2(lastPoint.y - midPoint.y.toDouble(), lastPoint.x - midPoint.x.toDouble()) -
                    atan2(
                        firstPoint.y - midPoint.y.toDouble(),
                        firstPoint.x - midPoint.x.toDouble()
                    )
        var angle = Math.toDegrees(radian)
        if (angle < 0) {
            angle += 360.0
        }
        return angle
    }

    private fun calculateDistance(firstPoint: PointF, secondPoint: PointF): Double {
        return sqrt(
            (secondPoint.x - firstPoint.x).toDouble()
                .pow(2.0) + (secondPoint.y - firstPoint.y).toDouble()
                .pow(2.0)
        )
    }

}