package com.example.arcorestudy

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.arcorestudy.adapter.ModelAdapter
import com.example.arcorestudy.adapter.ModelItem
import com.example.arcorestudy.databinding.ActivityArcoreBinding
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.jetbrains.anko.toast
import java.io.File
import java.io.IOException

class ARCoreActivity : AppCompatActivity() {

    private val binding : ActivityArcoreBinding by lazy { ActivityArcoreBinding.inflate(layoutInflater) }
    private lateinit var arFragment : ArFragment

    private lateinit var adapter: ModelAdapter
    private var renderable : ModelRenderable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initModelRecyclerView()
        bindModelRecyclerView()
        settingArFragment()
    }

    private fun initModelRecyclerView(){
        adapter = ModelAdapter(
            modelSelectListener = {
                renderable = it
            },
            downloadListener = { modelItem, position ->
                modelDownload(modelItem, position)
            }
        )

        binding.modelRecyclerView.adapter = adapter
    }

    private fun modelDownload(modelItem: ModelItem, position: Int) {
        downloadStartMessage()

        try {
            val storageRef = Firebase.storage.reference
            val splitFileName = modelItem.fileName.split(".") // ex : [ "out", "glb" ]
            val file = File.createTempFile(splitFileName[0], splitFileName[1])

            storageRef.child(modelItem.fileName).getFile(file).addOnSuccessListener {
                buildModel(file, position)
            }

        }catch (e: IOException){
            e.printStackTrace()
            binding.progressBar.isVisible = false
            toast("파일 다운로드중 오류가 발생하였습니다.")
        }catch (e: Exception){
            e.printStackTrace()
            binding.progressBar.isVisible = false
            toast("파일 다운로드중 오류가 발생하였습니다.")
        }
    }

    private fun buildModel(file: File, position: Int){
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
                renderable = modelRenderable
                adapter.currentList[position].readable = modelRenderable
                adapter.notifyDataSetChanged()
            }

        downloadEndMessage()
    }

    private fun downloadStartMessage(){
        binding.progressBar.isVisible = true
        toast("서버에서 이미지를 받아오고 있습니다. 잠시만 기다려주세요..")
    }

    private fun downloadEndMessage(){
        binding.progressBar.isVisible = false
        toast("다운로드가 완료되었습니다.")
    }

    private fun bindModelRecyclerView(){
        val modelList = mutableListOf<ModelItem>()
        modelList.add(ModelItem(imageResource = R.drawable.cat, name = "고양이", fileName = "cat.glb"))
        modelList.add(ModelItem(imageResource = R.drawable.ivysaur, name = "이상해풀", fileName = "Ivysaur.glb"))
        modelList.add(ModelItem(imageResource = R.drawable.out, name = "고우스트", fileName = "out.glb"))
        modelList.add(ModelItem(imageResource = R.drawable.spider, name = "스파이더맨", fileName = "spider.glb"))
        modelList.add(ModelItem(imageResource = R.drawable.clock, name = "시계", fileName = "clock.glb"))

        adapter.submitList(modelList)
    }

    private fun settingArFragment(){
        arFragment = supportFragmentManager.findFragmentById(R.id.sceneFormFragment) as ArFragment
        arFragment.setOnTapArPlaneListener { hitResult, _, _ ->
            val anchorNode = AnchorNode(hitResult.createAnchor())
            anchorNode.renderable = renderable
            arFragment.arSceneView.scene.addChild(anchorNode)
        }
    }
}