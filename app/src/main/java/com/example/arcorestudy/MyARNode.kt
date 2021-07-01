package com.example.arcorestudy

import android.content.Context
import android.net.Uri
import com.google.ar.core.AugmentedImage
import com.google.ar.core.Pose
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import java.io.File
import java.util.concurrent.CompletableFuture

class MyARNode(context: Context?, file: File?) : AnchorNode() {
    var image: AugmentedImage? = null
        private set

    fun setImage(image: AugmentedImage) {
        this.image = image
        if (!modelRenderableCompletableFuture!!.isDone) {
            CompletableFuture.allOf(modelRenderableCompletableFuture)
                .thenAccept { setImage(image) }
                .exceptionally { null }
        }
        anchor = image.createAnchor(image.centerPose)
        val node = Node()
        val pose = Pose.makeTranslation(0.0f, 0.0f, 0.25f)
        node.setParent(this)
        node.localPosition = Vector3(pose.tx(), pose.ty(), pose.tz())
        node.localRotation = Quaternion(pose.qx(), pose.qy(), pose.qz(), pose.qw())
        node.renderable = modelRenderableCompletableFuture!!.getNow(null)
    }

    companion object {
        private var modelRenderableCompletableFuture: CompletableFuture<ModelRenderable?>? = null
    }

    init {
        if (modelRenderableCompletableFuture == null) {
            val renderableSource =
                RenderableSource
                    .builder()
                    .setSource(context, Uri.parse(file?.path), RenderableSource.SourceType.GLB)
                    .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                    .build()

            modelRenderableCompletableFuture = ModelRenderable.builder()
                .setRegistryId("my_model")
                .setSource(context, renderableSource)
                .build()
        }
    }
}