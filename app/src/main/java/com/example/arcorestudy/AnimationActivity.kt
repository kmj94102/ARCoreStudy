package com.example.arcorestudy

import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.arcorestudy.databinding.ActivityAnimationBinding
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import org.jetbrains.anko.toast
import java.io.File

class AnimationActivity : AppCompatActivity() {

    private val binding : ActivityAnimationBinding by lazy { ActivityAnimationBinding.inflate(layoutInflater) }

    private lateinit var arFragment : ArFragment
    private var anchorNode : AnchorNode? = null
    private var animator : ModelAnimator? = null
    private var nextAnimation : Int = 0
    private var animationCrab : ModelRenderable? = null

    private lateinit var transformableNode: TransformableNode
    private var reference: StorageReference = Firebase.storage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)

        arFragment = supportFragmentManager.findFragmentById(R.id.sceneFormFragment) as ArFragment
        arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            animationCrab ?: return@setOnTapArPlaneListener

            // create the Anchor
            val anchor = hitResult.createAnchor()
            if(anchorNode == null){
                anchorNode = AnchorNode(anchor)
                anchorNode!!.setParent(arFragment.arSceneView.scene)

                transformableNode = TransformableNode(arFragment.transformationSystem)
                // Scale model
                transformableNode.scaleController.minScale = 0.09f
                transformableNode.scaleController.maxScale = 0.1f
                transformableNode.setParent(anchorNode)
                transformableNode.renderable = animationCrab

            }
        }

        // add frame update to control state of button
        arFragment.arSceneView.scene.addOnUpdateListener {
            if(anchorNode == null){
                if(binding.btnAnim.isEnabled){
                    binding.btnAnim.backgroundTintList = ColorStateList.valueOf(Color.GRAY)
                    binding.btnAnim.isEnabled = false
                }
            }else{
                if(binding.btnAnim.isEnabled.not()){
                    binding.btnAnim.backgroundTintList = ContextCompat.getColorStateList(this, R.color.teal_200)
                    binding.btnAnim.isEnabled = true
                }
            }
        }

        binding.btnAnim.isEnabled = false
        binding.btnAnim.setOnClickListener {
            if(animator == null || animator?.isRunning == false){
                val data = animationCrab?.getAnimationData(nextAnimation)
                nextAnimation = (nextAnimation+1) % (animationCrab?.animationDataCount ?: 1)
                animator = ModelAnimator(data, animationCrab)
                animator?.start()
            }
        }

        binding.btnDownload.setOnClickListener {
            downloadFile()
        }

    }

    private fun downloadFile(){
        val reference: StorageReference = Firebase.storage.reference
        val file = File.createTempFile("clock", "glb")

        reference.child("clock.glb").getFile(file).addOnSuccessListener { task ->
            buildModel(file)
        }
    }

    private fun buildModel(file: File){
        val renderableSource =
            RenderableSource
                .builder()
                .setSource(this, Uri.parse(file.path), RenderableSource.SourceType.GLB)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build()

        ModelRenderable
            .builder()
            .setSource(this, renderableSource)
            .setRegistryId(file.path)
            .build()
            .thenAccept{ modelRenderable ->
                toast("다운로드 완료")
                animationCrab = modelRenderable
            }
            .exceptionally {
                toast("${it.message}")
                return@exceptionally null
            }
    }
}