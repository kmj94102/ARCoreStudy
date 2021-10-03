package com.example.arcorestudy.material

import android.content.Context
import com.google.ar.core.DepthPoint
import com.google.ar.core.Plane
import com.google.ar.core.Point
import com.google.ar.core.Pose
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.ux.TransformableNode
import java.util.concurrent.atomic.AtomicLong
import kotlin.reflect.KClass
import com.google.ar.sceneform.rendering.MaterialFactory.makeOpaqueWithColor
import com.google.ar.sceneform.rendering.ShapeFactory

sealed class Nodes(
    name: String,
    coordinator : Coordinator,
    private val settings: Settings
) : TransformableNode(coordinator){

    interface FacingCamera

    companion object {

        private const val PLANE_ANCHORING_DISTANCE = 2F
        private const val DEFAULT_POSE_DISTANCE = 2F

        private val IDS: MutableMap<KClass<*>, AtomicLong> = mutableMapOf()

        fun Any.newId() : Long = IDS.getOrElse(this::class, { AtomicLong().also { IDS[this::class] = it } }).incrementAndGet()

        fun defaultPose(ar : ArSceneView) : Pose {
            val centerX = ar.width / 2F
            val centerY = ar.height / 2F
            val hits = ar.arFrame?.hitTest(centerX, centerY)
            val planeHitPose = hits?.firstOrNull {
                when (val trackable = it.trackable) {
                    is Plane -> trackable.isPoseInPolygon(it.hitPose) && it.distance <= PLANE_ANCHORING_DISTANCE
                    is DepthPoint, is Point -> it.distance <= DEFAULT_POSE_DISTANCE
                    else -> false
                }
            }?.hitPose
            if (planeHitPose != null) return planeHitPose
            val ray = ar.scene.camera.screenPointToRay(centerX, centerY)
            val point = ray.getPoint(DEFAULT_POSE_DISTANCE)
            return Pose.makeTranslation(point.x, point.y, point.z)
        }

    }

}

sealed class MaterialNode(
    name: String,
    val properties: MaterialProperties,
    coordinator: Coordinator,
    settings: Settings
) : Nodes(name, coordinator, settings){
    init {
        update()
    }

    fun update(block: (MaterialProperties.() -> Unit) = {}){
        properties.update(renderable?.material, block)
    }
}

class Sphere(
    context: Context,
    properties: MaterialProperties,
    coordinator: Coordinator,
    settings: Settings
) : MaterialNode("Sphere", properties, coordinator, settings) {
    companion object {
        private const val RADIUS = 0.05f
        private val CENTER = Vector3(0f, RADIUS, 0F)
    }

    init {
        val color = properties.color.toArColor()
        makeOpaqueWithColor(context.applicationContext, color)
            .thenAccept { renderable = ShapeFactory.makeSphere(RADIUS, CENTER, it) }
    }
}