package com.example.arcorestudy

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.arcorestudy.databinding.ActivityLoadFirebaseBinding
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.jetbrains.anko.toast
import java.io.File
import java.io.IOException

class LoadFirebaseActivity : AppCompatActivity() {

    private val binding : ActivityLoadFirebaseBinding by lazy { ActivityLoadFirebaseBinding.inflate(layoutInflater) }
    private lateinit var renderable : ModelRenderable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)

        val modelRef = Firebase.storage.reference.child("out.glb")

        val arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment

        binding.downloadBtn.setOnClickListener {
            try {
                val file = File.createTempFile("out", "glb")

                modelRef.getFile(file).addOnSuccessListener { task ->
                    buildModel(file)
                }
            }catch (e : IOException){
                e.printStackTrace()
            }
        }

        arFragment.setOnTapArPlaneListener{ hitResult, plance, motionEvent ->
            val anchorNode =AnchorNode(hitResult.createAnchor())
            anchorNode.renderable = renderable
            arFragment.arSceneView.scene.addChild(anchorNode)
        }

    }

    private fun buildModel(file : File){
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
                toast("Model built")
                renderable = modelRenderable
            }
    }

}