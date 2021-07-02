package com.example.arcorestudy

import android.view.GestureDetector
import android.view.MotionEvent
import androidx.fragment.app.Fragment
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.ux.FootprintSelectionVisualizer
import com.google.ar.sceneform.ux.PlaneDiscoveryController
import com.google.ar.sceneform.ux.TransformationSystem

class BaseArFragment : Fragment(), Scene.OnPeekTouchListener, Scene.OnUpdateListener {

    private lateinit var arSceneView : ArSceneView
    private lateinit var transformationSystem : TransformationSystem
    private lateinit var gestureDetector : GestureDetector

    init {
        transformationSystem = makeTransformationSystem()
    }

    protected fun makeTransformationSystem() : TransformationSystem{
        val selectionVisualizer = FootprintSelectionVisualizer()

        val transformationSystem = TransformationSystem(resources.displayMetrics, selectionVisualizer)
        setupSelectionRenderable(selectionVisualizer)
        return transformationSystem
    }

    protected fun setupSelectionRenderable(selectionVisualizer : FootprintSelectionVisualizer){

    }

    override fun onPeekTouch(hitTestResult: HitTestResult?, motionEvent: MotionEvent?) {
        transformationSystem.onTouch(hitTestResult, motionEvent)

        if(hitTestResult?.node == null){
            gestureDetector.onTouchEvent(motionEvent)
        }
    }

    override fun onUpdate(frameTime: FrameTime?) {
        val frame = arSceneView.arFrame
        frame ?: return

        frame.getUpdatedTrackables(Plane::class.java).forEach { plane ->
            if(plane.trackingState == TrackingState.TRACKING){
            }
        }
    }
}