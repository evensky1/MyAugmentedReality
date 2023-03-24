package com.poit.myaugmentedreality

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

class AnchorActivity : AppCompatActivity(R.layout.activity_anchor) {

    private lateinit var arProcessor: ArFragment
    private lateinit var detectBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        detectBtn = findViewById(R.id.detectBtn)
        detectBtn.setOnClickListener {
            val intent = Intent(this, ObjectDetectionActivity::class.java)
            startActivity(intent)
            this.onPause()
        }

        arProcessor = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment
        arProcessor.setOnTapArPlaneListener { hitResult, _, _ ->
            val anchor = hitResult.createAnchor()

            ModelRenderable.builder()
                .setSource(this, R.raw.small_pointer)
                .setIsFilamentGltf(true)
                .build()
                .thenAccept { renderable -> addModel(anchor, renderable) }
                .exceptionally { throwable ->
                    AlertDialog.Builder(this).setMessage(throwable.message).show()
                    null
                }

        }
    }

    private fun addModel(anchor: Anchor, modelRenderable: ModelRenderable) {
        val anchorNode = AnchorNode(anchor).apply {
            setParent(arProcessor.arSceneView.scene)
        }

        val model = TransformableNode(arProcessor.transformationSystem).apply {
            setParent(anchorNode)
            renderable = modelRenderable
        }

        model.setOnTapListener { hitResult, _ ->
            hitResult.node?.setParent(null)
        }

        model.select()
    }
}