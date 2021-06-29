package com.example.arcorestudy

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.arcorestudy.databinding.ActivityLoadFirebaseBinding
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.jetbrains.anko.toast
import java.io.File
import java.io.IOException

class LoadFirebaseActivity : AppCompatActivity() {

    private val binding : ActivityLoadFirebaseBinding by lazy { ActivityLoadFirebaseBinding.inflate(layoutInflater) }
    private lateinit var renderable : ModelRenderable
    private lateinit var arFragment: ArFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)

        val modelRef = Firebase.storage.reference.child("out.glb")

        arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment

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

        arFragment.setOnTapArPlaneListener{ hitResult, _, _ ->
            val anchorNode =AnchorNode(hitResult.createAnchor())
            anchorNode.setParent(arFragment.arSceneView.scene)
//            anchorNode.renderable = renderable
            val transFromAbleNode = TransformableNode(arFragment.transformationSystem)
            transFromAbleNode.setParent(anchorNode)
            transFromAbleNode.renderable = renderable
            transFromAbleNode.select()

//            arFragment.arSceneView.scene.addChild(anchorNode)

            addName(anchorNode, transFromAbleNode, "고우스트")
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

    private fun addName(anchorNode: AnchorNode, node: TransformableNode, name: String) {
        ViewRenderable.builder().setView(this, R.layout.name_card)
            .build()
            .thenAccept { viewRenderable ->
                val nameView = TransformableNode(arFragment.transformationSystem)
                nameView.localPosition = Vector3(0f, node.localPosition.y + 0.5f, 0f)
                nameView.setParent(anchorNode)
                nameView.renderable = viewRenderable
                nameView.select()

                val txtName = viewRenderable.view as TextView
                txtName.text = name
                txtName.setOnClickListener {
                    anchorNode.setParent(null)

                }
            }
    }

}