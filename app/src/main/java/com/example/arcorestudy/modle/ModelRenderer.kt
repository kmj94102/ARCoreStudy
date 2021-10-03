package com.example.arcorestudy.modle

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.MotionEvent
import com.google.ar.core.Anchor
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlin.random.Random

object ModelRenderer {

    inline fun renderObject(
        context: Context,
        renderingModel: RenderingModel,
        crossinline thenAccept: (Renderable) -> Unit
    ){
        ModelRenderable.builder()
            .setSource(context, Uri.parse(renderingModel.model))
            .build()
            .thenAccept { thenAccept(it) }
            .exceptionally {
                AlertDialog.Builder(context)
                    .setMessage(it.localizedMessage)
                    .show()
                return@exceptionally null
            }
    }

    fun addPokemonOnScene(
        fragment: ArFragment,
        anchor : Anchor,
        renderable : Renderable,
        renderingModel: RenderingModel
    ){
        val anchorNode = AnchorNode(anchor)
        TransformableNode(fragment.transformationSystem).apply {
            name = renderingModel.name
            localPosition = renderingModel.localPosition
            this.renderable = renderable
            translationController.isEnabled = false
            setParent(anchorNode)
            setLookDirection(renderingModel.direction)
            scaleController.minScale = renderingModel.scale
            scaleController.maxScale = renderingModel.scale + 0.05f
            fragment.arSceneView.scene.addChild(anchorNode)
            setOnTouchListener { hitTestResult, motionEvent ->
                if(motionEvent.action == MotionEvent.ACTION_UP){
                    hitTestResult.node?.let { node ->
                        node.setLookDirection(Vector3(0f, 0f, 1f))
                        ModelAnimations.translateModel(
                            anchorNode = node,
                            targetPosition = Vector3(
                                localPosition.x,
                                localPosition.y + 0.25f,
                                localPosition.z
                            ),
                            doWhenFinish = {
                                val localPosition = renderingModel.localPosition
                                ModelAnimations.translateModel(node, localPosition)
                            }
                        )
                    }
                }
                true
            }
        }
    }

    fun addPokeBallOnScene(
        fragment: ArFragment,
        anchor: Anchor,
        pokemonAnchor: Anchor,
        renderable: Renderable,
        renderingModel: RenderingModel,
        pokemon: RenderingModel,
        doAfterCatch: () -> Unit
    ) {
        val anchorNode = AnchorNode(anchor)
        TransformableNode(fragment.transformationSystem).apply {
            name = renderingModel.name
            localPosition = renderingModel.localPosition
            this.renderable = renderable
            setParent(anchorNode)
            setLookDirection(renderingModel.direction)
            scaleController.minScale = renderingModel.scale
            scaleController.maxScale = renderingModel.scale + 0.05f
            fragment.arSceneView.scene.addChild(anchorNode)
            setOnTapListener { hitTestResult, _ ->
                hitTestResult.node?.let { node ->
                    val pokemonPosition = pokemon.localPosition
                    val targetPosition = Vector3(
                        pokemonPosition.x + getRandomPosition(),
                        pokemonPosition.y + getRandomPosition(),
                        pokemonPosition.z + getRandomPosition()
                    )

                    ModelAnimations.translateModel(
                        anchorNode = node,
                        targetPosition = targetPosition,
                        durationTime = 750L
                    ) {
                        val length = Vector3.subtract(pokemonPosition, targetPosition).length()
                        if (length > 0.45) {
                            ModelAnimations.translateModel(
                                anchorNode = node,
                                targetPosition = DEFAULT_POSITION_POKE_BALL,
                                durationTime = 0
                            )
                        } else {
                            doAfterCatch()
                            pokemonAnchor.detach()
                        }
                    }

                    ModelAnimations.rotateModel(
                        anchorNode = node,
                        durationTime = 500L
                    ) {
                        node.setLookDirection(Vector3(0f, 0f, 1f))
                    }
                }
            }
            select()
        }
    }

    fun addGardenOnScene(
        fragment: ArFragment,
        anchor: Anchor,
        renderable: Renderable,
        renderingModel: RenderingModel
    ) {
        val anchorNode = AnchorNode(anchor)
        TransformableNode(fragment.transformationSystem).apply {
            name = renderingModel.name
            localPosition = renderingModel.localPosition
            this.renderable = renderable
            translationController.isEnabled = false
            setParent(anchorNode)
            setLookDirection(renderingModel.direction)
            scaleController.minScale = renderingModel.scale
            scaleController.maxScale = renderingModel.scale + 0.05f
            fragment.arSceneView.scene.addChild(anchorNode)
        }
    }

    private fun getRandomPosition() : Float {
        val position = Random.nextFloat()
        return if(position <= 0.5f){
            position
        }else{
            position - 1
        }
    }

}