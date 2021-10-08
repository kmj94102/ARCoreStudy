package com.example.arcorestudy.material

import android.content.Context
import android.util.Log
import android.view.MotionEvent
import com.google.ar.core.*
import com.google.ar.sceneform.*
import com.google.ar.sceneform.Camera
import com.google.ar.sceneform.collision.RayHit
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Material
import com.google.ar.sceneform.rendering.MaterialFactory.makeOpaqueWithColor
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.ux.BaseTransformableNode
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.ux.TransformationSystem
import java.util.concurrent.atomic.AtomicLong
import kotlin.reflect.KClass

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

    init {
        this.name = "$name #${newId()}"
        scaleController.apply {
            minScale = 0.25f
            maxScale = 5f
        }
        @Suppress("LeakingThis")
        if (this is FacingCamera) rotationController.isEnabled = false
    }

    var onNodeUpdate: ((Nodes) -> Any)? = null

    internal fun anchor(): Anchor? = (parent as? AnchorNode)?.anchor

    override fun getTransformationSystem(): Coordinator = super.getTransformationSystem() as Coordinator

    override fun setRenderable(renderable: Renderable?) {
        super.setRenderable(renderable?.apply { })
    }

    override fun onUpdate(frameTime: FrameTime) {
        onNodeUpdate?.invoke(this)
        if(this is FacingCamera){
            facingCamera()
        }
    }

    private fun facingCamera(){
        if (isTransforming) return
        val camera = scene?.camera ?: return
        val direction = Vector3.subtract(camera.worldPosition, worldPosition)
        worldRotation = Quaternion.lookRotation(direction, Vector3.up())
    }

    open fun attach(anchor: Anchor, scene: Scene, focus: Boolean = false){
        setParent(AnchorNode(anchor).apply { setParent(scene) })
        if (focus){
            transformationSystem.focusNode(this)
        }
    }

    open fun detach(){
        if(this == transformationSystem.selectedNode){
            transformationSystem.selectNode(selectionContinuation())
        }
        (parent as? AnchorNode)?.anchor?.detach()
        setParent(null)
    }

    open fun selectionContinuation() : BaseTransformableNode?= null

    override fun onTap(hitTestResult: HitTestResult?, motionEvent: MotionEvent?) {
        super.onTap(hitTestResult, motionEvent)
        if(isTransforming) return
        transformationSystem.focusNode(this)
    }

}



sealed class MaterialNode(
    name: String,
    val properties: MaterialProperties,
    coordinator: Coordinator,
    settings: Settings
) : Nodes(name, coordinator, settings) {

    init {
        update()
    }

    fun update(block: (MaterialProperties.() -> Unit) = {}){
        properties.update(renderable?.material, block)
    }

}

class Sphere(
    context : Context,
    properties: MaterialProperties,
    coordinator: Coordinator,
    settings: Settings
) : MaterialNode("Sphere", properties, coordinator, settings) {

    companion object {
        private const val RADIUS = 0.05F
        private val CENTER = Vector3(0F, RADIUS, 0F)
    }

    init {
        val color = properties.color.toArColor()
        makeOpaqueWithColor(context.applicationContext, color)
            .thenAccept { renderable = ShapeFactory.makeSphere(RADIUS, CENTER, it) }
    }

}

typealias CollisionPlane = com.google.ar.sceneform.collision.Plane

class Drawing(
    val isFromTouch: Boolean,
    private val plane: CollisionPlane?,
    properties: MaterialProperties,
    coordinator: Coordinator,
    settings: Settings
) : MaterialNode("Drawing", properties, coordinator, settings) {

    companion object {

        private const val RADIUS = 0.005F
        private const val PLANE_ANCHORING_DISTANCE = 2F
        private const val DEFAULT_DRAWING_DISTANCE = 0.5F

        private fun hit(frame: Frame, x: Float, y: Float): HitResult? {
            return frame.hitTest(x, y).firstOrNull {
                (it.trackable as? Plane)?.isPoseInPolygon(it.hitPose) == true && it.distance <= PLANE_ANCHORING_DISTANCE
            }
        }

        private fun pose(camera: Camera, x: Float, y: Float): Pose {
            val ray = camera.screenPointToRay(x, y)
            val point = ray.getPoint(DEFAULT_DRAWING_DISTANCE)
            return Pose.makeTranslation(point.x, point.y, point.z)
        }

        private fun plane(hitResult: HitResult?): CollisionPlane? {
            return (hitResult?.trackable as? Plane)?.let {
                val pose = it.centerPose
                val normal = Quaternion.rotateVector(pose.rotation(), Vector3.up())
                CollisionPlane(pose.translation(), normal)
            }
        }

        fun create(
            x: Float,
            y: Float,
            fromTouch: Boolean,
            properties: MaterialProperties,
            ar: ArSceneView,
            coordinator: Coordinator,
            settings: Settings
        ): Drawing? {
            val context = ar.context
            val session = ar.session ?: return null
            val scene = ar.scene ?: return null
            val frame = ar.arFrame ?: return null
            if (frame.camera.trackingState != TrackingState.TRACKING) return null

            val hit = hit(frame, x, y)
            val pose = hit?.hitPose ?: pose(scene.camera, x, y)
            val plane = plane(hit)
            val anchor = hit?.createAnchor() ?: session.createAnchor(pose)

            return Drawing(fromTouch, plane, properties, coordinator, settings).apply {
                makeOpaqueWithColor(context.applicationContext, properties.color.toArColor()).thenAccept { material = it }
                attach(anchor, scene)
                extend(x, y)
            }
        }
    }

    private val line = LineSimplifier()
    private var material: Material? = null
        set(value) {
            field = value?.apply { properties.update(this) }
            render()
        }

    private fun append(pointInWorld: Vector3) {
        val pointInLocal = (parent as AnchorNode).worldToLocalPoint(pointInWorld)
        line.append(pointInLocal)
        render()
    }

    private fun render() {
        val definition = ExtrudedCylinder.makeExtrudedCylinder(RADIUS, line.points, material ?: return) ?: return
        if (renderable == null) {
            ModelRenderable.builder().setSource(definition).build().thenAccept { renderable = it }
        } else {
            renderable?.updateFromDefinition(definition)
        }
    }

    fun extend(x: Float, y: Float) {
        val ray = scene?.camera?.screenPointToRay(x, y) ?: return
        if (plane != null) {
            val rayHit = RayHit()
            if (plane.rayIntersection(ray, rayHit)) {
                append(rayHit.point)
            }
        } else {
            append(ray.getPoint(DEFAULT_DRAWING_DISTANCE))
        }
    }

    fun deleteIfEmpty() = if (line.points.size < 2) detach() else Unit

}
