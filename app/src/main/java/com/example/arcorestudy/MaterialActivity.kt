package com.example.arcorestudy

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.arcorestudy.databinding.ActivityMaterialBinding
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.ux.ArFragment

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


            }
        }
    }
}