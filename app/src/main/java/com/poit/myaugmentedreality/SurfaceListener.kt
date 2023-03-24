package com.poit.myaugmentedreality

import android.annotation.SuppressLint
import android.graphics.*
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.HandlerThread
import android.view.TextureView
import android.widget.ImageView
import com.poit.myaugmentedreality.ml.SsdMobilenetV11Metadata1
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage

class SurfaceListener(
    private val cameraManager: CameraManager,
    private val textureView: TextureView,
    private val imageProcessor: ImageProcessor,
    private val model: SsdMobilenetV11Metadata1,
    private val imageView: ImageView,
    private val labels: List<String>
) : TextureView.SurfaceTextureListener {

    private val colors = listOf(
        Color.BLUE,
        Color.GREEN,
        Color.RED,
        Color.CYAN,
        Color.GRAY,
        Color.BLACK,
        Color.DKGRAY,
        Color.MAGENTA,
        Color.YELLOW,
        Color.RED
    )

    private val paint: Paint = Paint()
    private val handler: Handler
    init {
        val handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)
    }

    @SuppressLint("MissingPermission")
    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        cameraManager.openCamera(
            cameraManager.cameraIdList[0],
            CameraStateCallBack(textureView, handler),
            handler
        )
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        return false
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        val bitmap = textureView.bitmap!!

        val image = TensorImage.fromBitmap(bitmap)
        imageProcessor.process(image)

        val outputs = model.process(image)
        val locations = outputs.locationsAsTensorBuffer.floatArray
        val classes = outputs.classesAsTensorBuffer.floatArray
        val scores = outputs.scoresAsTensorBuffer.floatArray
        val numberOfDetections = outputs.numberOfDetectionsAsTensorBuffer

        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        val h = mutableBitmap.height
        val w = mutableBitmap.width

        paint.textSize = h / 15f
        paint.strokeWidth = h / 85f

        var x: Int
        scores.forEachIndexed { index, fl ->
            x = index
            x *= 4

            if (fl > 0.5) {
                paint.color = colors[index]
                paint.style = Paint.Style.STROKE
                canvas.drawRect(
                    RectF(
                        locations[x + 1] * w,
                        locations[x] * h,
                        locations[x + 3] * w,
                        locations[x + 2] * h
                    ), paint
                )
                paint.style = Paint.Style.FILL
                canvas.drawText(
                    labels[classes[index].toInt()] + " " + fl.toString(),
                    locations[x + 1] * w,
                    locations[x] * h,
                    paint
                )
            }
        }

        imageView.setImageBitmap(mutableBitmap)
    }
}