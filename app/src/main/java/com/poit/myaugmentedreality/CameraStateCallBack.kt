package com.poit.myaugmentedreality

import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.os.Handler
import android.view.Surface
import android.view.TextureView

class CameraStateCallBack(
    private val textureView: TextureView,
    private val handler: Handler
) : CameraDevice.StateCallback() {

    override fun onOpened(camera: CameraDevice) {
        val surface = Surface(textureView.surfaceTexture)

        val captureRequest = camera
            .createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                addTarget(surface)
            }.build()

        camera.createCaptureSession(
            listOf(surface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    session.setRepeatingRequest(captureRequest, null, null)
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {}
            },
            handler
        )
    }

    override fun onDisconnected(camera: CameraDevice) {}
    override fun onError(camera: CameraDevice, error: Int) {}
}