package com.example.arcorestudy

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.example.arcorestudy.databinding.ActivitySceneformBinding
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.jetbrains.anko.toast
import java.io.File
import java.io.IOException
import java.lang.Exception

class SceneFormActivity : AppCompatActivity() {

    private val binding : ActivitySceneformBinding by lazy { ActivitySceneformBinding.inflate(layoutInflater) }
    private val storageList = listOf("cat.glb", "Ivysaur.glb", "out.glb", "spider.glb")
    private val renderableList = mutableListOf<ModelRenderable?>(null, null, null, null)
    private lateinit var arFragment: ArFragment
    private var selected = 0
//    private var catRenderable : ModelRenderable?= null
//    private var outRenderable : ModelRenderable?= null
//    private var ivysaurRenderable : ModelRenderable?= null
//    private var spiderRenderable : ModelRenderable?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)

        initDownload()

        settingARFragment()

    }

    private fun initDownload(){
        downloadStartMessage()

        try {
            val storageRef = Firebase.storage.reference
            storageList.forEachIndexed { index, fileName ->
                val splitFileName = fileName.split(".")
                val file = File.createTempFile(splitFileName[0], splitFileName[1])

                storageRef.child(fileName).getFile(file).addOnSuccessListener {
                    buildModel(file, index)
                }
            }

            downloadEndMessage()
        }catch (e: IOException){
            e.printStackTrace()
            toast("파일 다운로드중 오류가 발생하였습니다.")
        }catch (e: Exception){
            e.printStackTrace()
            toast("파일 다운로드중 오류가 발생하였습니다.")
        }
    }

    private fun buildModel(file : File, index : Int){
        val renderableSource =
            RenderableSource
                .builder()
                .setSource(this, Uri.parse(file.path), RenderableSource.SourceType.GLB)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build()

        ModelRenderable
            .builder()
            .setSource(this, renderableSource)
            .setRegistryId(file.path)
            .build()
            .thenAccept { modelRenderable ->
                renderableList[index] = modelRenderable
            }
    }

    private fun downloadStartMessage(){
        binding.progressBar.isVisible = true
        toast("서버에서 이미지를 받아오고 있습니다. 잠시만 기다려주세요..")
    }

    private fun downloadEndMessage(){
        binding.progressBar.isVisible = false
        toast("다운로드가 완료되었습니다.")
    }

    private fun settingARFragment(){
        arFragment = supportFragmentManager.findFragmentById(R.id.sceneFromUxFragment) as ArFragment
        arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
//            if(selected == 1){
//                val anchor = hitResult.createAnchor()
//                val anchorNode = AnchorNode(anchor)
//                anchorNode.setParent(arFragment.arSceneView.scene)
//
//                createModel(anchorNode)

//            }
            val anchorNode = AnchorNode(hitResult.createAnchor())
            anchorNode.renderable = renderableList[selected]
            arFragment.arSceneView.scene.addChild(anchorNode)
        }
    }

    fun itemOnClick(view : View){
        selected = view.tag.toString().toInt()
    }

    private fun createModel(anchorNode: AnchorNode){

//        if(selected == 1){
//            val cat = TransformableNode(arFragment.transformationSystem)
//            cat.setParent(anchorNode)
//            cat.renderable = renderableList[0]
//            cat.select()
//        }
    }

}