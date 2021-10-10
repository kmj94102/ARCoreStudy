package com.example.arcorestudy

import android.Manifest
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.arcorestudy.databinding.ActivityMaterialBinding
import com.example.arcorestudy.material.*
import com.google.ar.core.*
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableException
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.collision.Sphere

class MaterialActivity : AppCompatActivity() {

    private val binding : ActivityMaterialBinding by lazy { ActivityMaterialBinding.inflate(layoutInflater) }
    private val arSceneView: ArSceneView get() = binding.arSceneView

    private var installRequested: Boolean = false
    private val coordinator by lazy { Coordinator(this, ::onArTap, ::onNodeSelected, ::onNodeFocused) }
    private val settings by lazy { Settings.instance(this) }
    private var drawing: Drawing?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        arSceneView.scene.addOnUpdateListener { onArUpdate() }
        arSceneView.scene.addOnPeekTouchListener { hitTestResult, motionEvent ->
            coordinator.onTouch(hitTestResult, motionEvent)

            if (shouldHandleDrawing(motionEvent, hitTestResult)){
                val x = motionEvent.x
                val y = motionEvent.y
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> drawing = Drawing.create(x, y, true, materialProperties(), arSceneView, coordinator, settings)
                    MotionEvent.ACTION_MOVE -> drawing?.extend(x, y)
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> drawing = drawing?.deleteIfEmpty().let { null }
                }
            }
        }

        with(binding){
            metallicValue.setOnSeekBarChangeListener(SimpleSeekBarChangeListener { focusedMaterialNode()?.update { metallic = it } })
            roughnessValue.setOnSeekBarChangeListener(SimpleSeekBarChangeListener { focusedMaterialNode()?.update { roughness = it } })
            reflectanceValue.setOnSeekBarChangeListener(SimpleSeekBarChangeListener { focusedMaterialNode()?.update { reflectance = it } })
        }

    }

    private fun focusedMaterialNode() = (coordinator.focusedNode as? MaterialNode)

    private fun onArUpdate(){
        val frame = arSceneView.arFrame
        val camera = frame?.camera
        val state = camera?.trackingState
        val reason = camera?.trackingFailureReason
    }

    private fun onArTap(motionEvent: MotionEvent){
        val frame = arSceneView.arFrame ?: return
        if(frame.camera.trackingState != TrackingState.TRACKING){
            coordinator.selectNode(null)
            return
        }

        frame.hitTest(motionEvent).firstOrNull {
            val trackable = it.trackable
            when {
                trackable is Plane && trackable.isPoseInPolygon(it.hitPose) -> true
                trackable is DepthPoint -> true
                trackable is Point -> true
                else -> false
            }

        }?.let {
            createNodeAndAddToScene(anchor = { it.createAnchor() } )
        } ?: coordinator.selectNode(null)
    }

    private fun createNodeAndAddToScene(anchor: () -> Anchor, focus : Boolean = true){
        Sphere(this, materialProperties(), coordinator, settings).attach(anchor(), arSceneView.scene, focus)
    }

    private fun materialProperties() =
        MaterialProperties(
            color = Color.RED,
            metallic = binding.metallicValue.progress,
            roughness = binding.roughnessValue.progress,
            reflectance = binding.reflectanceValue.progress
        )

    private fun onNodeSelected(old: Nodes? = coordinator.selectedNode, new: Nodes?){

    }

    private fun onNodeFocused(node: Nodes?){

    }


    override fun onResume() {
        super.onResume()
        initArSession()
        try {
            arSceneView.resume()
        } catch (e : CameraNotAvailableException){
            Log.e("++++++", "error !!!!!!!!!!!!!!!!!!!!!\n${e.printStackTrace()}")
        }
        if(hasCameraPermission() && installRequested.not()){
            onArResumed()
        }
    }

    private fun initArSession(){
        if (arSceneView.session != null){
            return
        }

        if(!hasCameraPermission()){
            requestCameraPermission()
            return
        }

        val sessionException: UnavailableException?
        try {
            val requestInstall = ArCoreApk.getInstance().requestInstall(this, !installRequested)
            if(requestInstall == ArCoreApk.InstallStatus.INSTALL_REQUESTED){
                installRequested = true
                return
            }
            installRequested = false
            val session = Session(this)
            session.configure(getConfig(session))
            arSceneView.setupSession(session)
            return
        } catch (e: UnavailableException){
            sessionException = e
        } catch (e: Exception){
            sessionException = UnavailableException().apply { initCause(e) }
        }

        Toast.makeText(this, sessionException.message(), Toast.LENGTH_SHORT).show()
        finish()

    }

    private fun getConfig(session: Session) : Config = Config(session).apply {
        lightEstimationMode = Config.LightEstimationMode.DISABLED
        planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
        updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        cloudAnchorMode = Config.CloudAnchorMode.ENABLED
        augmentedFaceMode = Config.AugmentedFaceMode.DISABLED
        focusMode = Config.FocusMode.AUTO
        if(session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)){
            depthMode = Config.DepthMode.AUTOMATIC
        }
    }

    private fun shouldHandleDrawing(motionEvent: MotionEvent? = null, hitTestResult: HitTestResult? = null) : Boolean {
//        if (model.selection.value != Drawing::class) return false
        if (coordinator.selectedNode?.isTransforming == true) return false
        if (arSceneView.arFrame?.camera?.trackingState != TrackingState.TRACKING) return false
        if (motionEvent?.action == MotionEvent.ACTION_DOWN && hitTestResult?.node != null) return false
        return true
    }

    private fun onArResumed(){

    }

    private fun hasCameraPermission() = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PERMISSION_GRANTED

    private fun requestCameraPermission(){
        if(hasCameraPermission().not()){
            requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION && !hasCameraPermission()) {
            redirectToApplicationSettings()
        }
    }


    override fun onPause() {
        super.onPause()
        arSceneView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        arSceneView.destroy()
    }

    private companion object {
        const val REQUEST_CAMERA_PERMISSION = 1
    }

}