package com.aman.fityatraapp.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.aman.fityatraapp.R
import com.aman.fityatraapp.utils.Display
import com.aman.fityatraapp.viewModel.CameraViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import dagger.hilt.android.AndroidEntryPoint


class CameraActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var display: Display
    private lateinit var countExerciseText: TextView
    private lateinit var cameraProvider: ProcessCameraProvider

    private lateinit var viewModel: CameraViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        supportActionBar?.hide()

        viewModel= ViewModelProvider(this)[CameraViewModel::class.java]

        previewView = findViewById(R.id.previewView)
        display = findViewById(R.id.displayOverlay)
        countExerciseText = findViewById(R.id.textCount)

        val exercise = intent.getStringExtra("Exercise") ?: ""
        viewModel.setExerciseType(exercise)

        viewModel.exerciseCount.observe(this){
            countExerciseText.text=it.toString()
        }

        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            cameraProvider = future.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(this))

        viewModel.bitmapToDraw.observe(this) { bitmap ->
            display.getBitmap(bitmap)
        }

        viewModel.exerciseCounter.observe(this) {
            countExerciseText.text = it.toString()
        }
    }

    private fun bindCameraUseCases() {
        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }

        val imageAnalysis = ImageAnalysis.Builder()
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
            viewModel.processImageProxy(imageProxy)
        }

        val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

        cameraProvider.bindToLifecycle(
            this, cameraSelector, preview, imageAnalysis
        )
    }
}