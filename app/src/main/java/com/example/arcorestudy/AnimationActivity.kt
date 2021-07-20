package com.example.arcorestudy

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.arcorestudy.databinding.ActivityAnimationBinding
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.SkeletonNode
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode


class AnimationActivity : AppCompatActivity() {

    private val binding : ActivityAnimationBinding by lazy{
        ActivityAnimationBinding.inflate(layoutInflater)
    }
    lateinit var arFragment: ArFragment
    private lateinit var uri: Uri
    private var renderable: ModelRenderable? = null
    private var animator: ModelAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        arFragment = supportFragmentManager.findFragmentById(R.id.sceneFormFragment) as ArFragment
        uri = Uri.parse("model_fight.sfb")

        arFragment.setOnTapArPlaneListener { hitResult, plane, _ ->
            if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING) {
                return@setOnTapArPlaneListener
            }
            val anchor = hitResult.createAnchor()
            placeObject(arFragment, anchor, uri)
        }

        binding.animateKickButton.setOnClickListener { animateModel("Character|Kick") }
        binding.animateIdleButton.setOnClickListener { animateModel("Character|Idle") }
        binding.animateBoxingButton.setOnClickListener { animateModel("Character|Boxing") }
    }

    private fun animateModel(name: String) {
        animator?.let { it ->
            if (it.isRunning) {
                it.end()
            }
        }
        renderable?.let { modelRenderable ->
            val data = modelRenderable.getAnimationData(name)
            animator = ModelAnimator(data, modelRenderable)
            animator?.start()
        }
    }

    private fun placeObject(fragment: ArFragment, anchor: Anchor, uri: Uri) {
        ModelRenderable.builder()
            .setSource(fragment.context, uri)
            .build()
            .thenAccept {
                renderable = it
                addToScene(fragment, anchor, it)
            }
            .exceptionally {
                val builder = AlertDialog.Builder(this)
                builder.setMessage(it.message).setTitle("Error")
                val dialog = builder.create()
                dialog.show()
                return@exceptionally null
            }
    }

    private fun addToScene(fragment: ArFragment, anchor: Anchor, renderable: Renderable) {
        val anchorNode = AnchorNode(anchor)

        val skeletonNode = SkeletonNode()
        skeletonNode.renderable = renderable

        val node = TransformableNode(fragment.transformationSystem)
        node.addChild(skeletonNode)
        node.setParent(anchorNode)

        fragment.arSceneView.scene.addChild(anchorNode)
    }
}