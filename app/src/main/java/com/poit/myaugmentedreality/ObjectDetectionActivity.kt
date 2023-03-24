package com.poit.myaugmentedreality

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.TextureView
import android.widget.Button
import android.widget.ImageView
import com.poit.myaugmentedreality.ml.SsdMobilenetV11Metadata1
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.ops.ResizeOp

class ObjectDetectionActivity : AppCompatActivity(R.layout.activity_object_detection) {

    private lateinit var model: SsdMobilenetV11Metadata1
    private lateinit var imageView: ImageView
    private lateinit var textureView: TextureView
    private lateinit var anchorBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        model = SsdMobilenetV11Metadata1.newInstance(this)

        val labels = FileUtil.loadLabels(this, "labels.txt")
        val resizeOp = ResizeOp(300, 300, ResizeOp.ResizeMethod.BILINEAR)
        val imageProcessor = ImageProcessor.Builder().add(resizeOp).build()
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        imageView = findViewById(R.id.imageView)
        textureView = findViewById(R.id.textureView)
        textureView.surfaceTextureListener = SurfaceListener(
            cameraManager, textureView, imageProcessor, model, imageView, labels
        )

        anchorBtn = findViewById(R.id.anchorBtn)
        anchorBtn.setOnClickListener {
            val intent = Intent(this, AnchorActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivityIfNeeded(intent, 0)
            this.onStop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        model.close()
    }
}