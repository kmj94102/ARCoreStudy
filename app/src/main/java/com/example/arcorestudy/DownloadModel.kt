package com.example.arcorestudy

import android.content.Context
import android.net.Uri
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import org.jetbrains.anko.toast
import java.io.File

class DownloadModel(val context: Context) {

    private var reference: StorageReference = Firebase.storage.reference
    private lateinit var renderable : ModelRenderable

    private fun downloadFile(){
        val file = File.createTempFile("out", "glb")

        reference.getFile(file).addOnSuccessListener { task ->
            buildModel(file)
        }
    }

    private fun buildModel(file : File){
        val renderableSource =
            RenderableSource
                .builder()
                .setSource(context, Uri.parse(file.path), RenderableSource.SourceType.GLB)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build()

        ModelRenderable
            .builder()
            .setSource(context, renderableSource)
            .setRegistryId(file.path)
            .build()
            .thenAccept{ modelRenderable ->
                renderable = modelRenderable
            }
    }
}