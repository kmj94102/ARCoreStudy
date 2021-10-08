package com.example.arcorestudy

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.arcorestudy.databinding.ActivityPokemonGoBinding
import com.example.arcorestudy.modle.DEFAULT_POSITION_POKEMON
import com.example.arcorestudy.modle.DEFAULT_POSITION_POKE_BALL
import com.example.arcorestudy.modle.ModelRenderer
import com.example.arcorestudy.modle.RenderingModel
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.ux.ArFragment

class PokemonGoActivity : AppCompatActivity() {

    private val binding : ActivityPokemonGoBinding by lazy { ActivityPokemonGoBinding.inflate(layoutInflater) }
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

    private fun initializeModels(arFragment: ArFragment, session: Session){
        // 확인 필요 : !viewModel.isCaught
        if (session.allAnchors.isEmpty()){
            val pose = Pose(floatArrayOf(0f, 0f, -1f), floatArrayOf(0f, 0f, 0f, 1f))
            session.createAnchor(pose).apply {
                val pokemon = RenderingModel(
                    name = "eevee",
                    model = "eevee.sfb",
                    localPosition = DEFAULT_POSITION_POKEMON
                )
                ModelRenderer.renderObject(this@PokemonGoActivity, pokemon) { renderable ->
                    ModelRenderer.addPokemonOnScene(arFragment, this, renderable, pokemon)
                }

                val pokeBall = RenderingModel(
                    name = "pokeball",
                    model = "pokeball.sfb",
                    scale = 0.1f,
                    localPosition = DEFAULT_POSITION_POKE_BALL
                )
                ModelRenderer.renderObject(this@PokemonGoActivity, pokeBall) { renderable ->
                    ModelRenderer.addPokeBallOnScene(arFragment, this, this, renderable, pokeBall, pokemon){
                        // 확인 필요
                        // viewModel.insertPokemonModel(pokemon)
                        AlertDialog.Builder(this@PokemonGoActivity).apply {
                            title = "GET!!!"
                            setMessage("eevee catch!!")
                            setPositiveButton("확인") { _, _ ->

                            }
                            create()
                        }.show()
                    }
                }
            }
        }
    }

}