package com.example.arcorestudy

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.arcorestudy.databinding.ActivityMaterialBinding
import com.example.arcorestudy.material.Sphere
import com.example.arcorestudy.material.toArColor
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.rendering.MaterialFactory.makeOpaqueWithColor
import com.google.ar.sceneform.rendering.ShapeFactory

class MaterialActivity : AppCompatActivity() {

    private val binding : ActivityMaterialBinding by lazy { ActivityMaterialBinding.inflate(layoutInflater) }
    private lateinit var arFragment : ArFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initArFragment()

    }

    private fun initArFragment(){
        arFragment = supportFragmentManager.findFragmentById(R.id.sceneFromUxFragment) as ArFragment

        with(arFragment){
            planeDiscoveryController.hide()
            planeDiscoveryController.setInstructionView(null)
            arSceneView.planeRenderer.isVisible = false
            arSceneView.scene.addOnUpdateListener {
                onUpdate(it)

                // AR 프레임의 상태가 추적인지 확인합니다.
                val arFrame = arSceneView.arFrame ?: return@addOnUpdateListener
                if (arFrame.camera?.trackingState != TrackingState.TRACKING){
                    return@addOnUpdateListener
                }

                // 기본 렌더링 모델로 전역 앵커 초기화
                arSceneView.session?.let { session ->
                    initializeModels(this, session)
                }



            }
        }
    }

    private fun initializeModels(arFragment: ArFragment, session: Session) {
        if (session.allAnchors.isEmpty()) {
            val pose = Pose(floatArrayOf(0f, 0f, -1f), floatArrayOf(0f, 0f, 0f, 1f))
            session.createAnchor(pose).apply {
                makeOpaqueWithColor(applicationContext, Color.RED.toArColor())
                    .thenAccept { material ->
                        ShapeFactory.makeSphere(0.05f, Vector3(0f, 0.05f, 0F), material)
                    }
            }
        }
    }

}