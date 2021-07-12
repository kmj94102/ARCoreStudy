package com.example.arcorestudy

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.arcorestudy.adapter.ModelAdapter
import com.example.arcorestudy.adapter.ModelItem
import com.example.arcorestudy.databinding.ActivityArcoreBinding
import com.google.ar.core.*
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.jetbrains.anko.toast
import java.io.File
import java.io.IOException

class ARCoreActivity : AppCompatActivity() {

    private val binding : ActivityArcoreBinding by lazy { ActivityArcoreBinding.inflate(layoutInflater) }
    private lateinit var arFragment : ArFragment

    private lateinit var adapter: ModelAdapter
    private var cardName : String? = null
    private var renderable : ModelRenderable? = null
    private var shouldConfigureSession : Boolean = false
    private var renderableFileList = mutableListOf<File?>(null, null, null, null, null)
    private var position : Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initModelRecyclerView()
        bindModelRecyclerView()
        settingArFragment()

        binding.radioAugmentedImage.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                settingAugmentedImage()
            }
        }

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
        settingDownloadStartState()

        try {
            val storageRef = Firebase.storage.reference
            val splitFileName = modelItem.fileName.split(".") // ex : [ "out", "glb" ]
            val file = File.createTempFile(splitFileName[0], splitFileName[1])

            storageRef.child(modelItem.fileName).getFile(file).addOnSuccessListener {
                buildModel(file, position)
                this.position = position
                renderableFileList[position] = file
            }

        }catch (e: IOException){
            e.printStackTrace()
            settingDownloadErrorState()
        }catch (e: Exception){
            e.printStackTrace()
            settingDownloadErrorState()
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
                cardName = adapter.currentList[position].name
                Log.e("++++++", "${renderable?.animationDataCount}")
            }

        settingDownloadEndState()
    }

    private fun settingDownloadStartState(){
        binding.progressBar.isVisible = true
        toast("서버에서 이미지를 받아오고 있습니다. 잠시만 기다려주세요..")
    }

    private fun settingDownloadEndState(){
        binding.progressBar.isVisible = false
        toast("다운로드가 완료되었습니다.")
    }

    private fun settingDownloadErrorState(){
        binding.progressBar.isVisible = false
        toast("파일 다운로드중 오류가 발생하였습니다.")
    }

    private fun bindModelRecyclerView(){
        val modelList = mutableListOf<ModelItem>()
        modelList.add(ModelItem(imageResource = R.drawable.cat, name = "고양이", fileName = "cat.glb"))
        modelList.add(ModelItem(imageResource = R.drawable.ivysaur, name = "이상해풀", fileName = "Ivysaur.glb"))
        modelList.add(ModelItem(imageResource = R.drawable.out, name = "고우스트", fileName = "out.glb"))
        modelList.add(ModelItem(imageResource = R.drawable.spider, name = "스파이더맨", fileName = "spider.glb"))
        modelList.add(ModelItem(imageResource = R.drawable.clock, name = "시계", fileName = "Bee.glb"))

        adapter.submitList(modelList)
    }

    private fun settingArFragment() = with(binding){
        arFragment = supportFragmentManager.findFragmentById(R.id.sceneFormFragment) as ArFragment
        arFragment.setOnTapArPlaneListener { hitResult, plane, _ ->

            when (radioGroup.checkedRadioButtonId) {
                // 즉시 배치
                radioInstantPlacement.id -> {
                    settingInstantPlacement(hitResult)
                }
                // 이름표
                radioNameCard.id -> {
                    settingNameCard(hitResult)
                }
            }
        }
    }

    private fun settingInstantPlacement(hitResult: HitResult) {
        val anchorNode = AnchorNode(hitResult.createAnchor())
        anchorNode.renderable = renderable
        anchorNode.setOnTouchListener { _, _ ->
            anchorNode.setParent(null)
            return@setOnTouchListener true
        }
        arFragment.arSceneView.scene.addChild(anchorNode)
    }

    private fun settingNameCard(hitResult: HitResult){
        val anchorNode =AnchorNode(hitResult.createAnchor())
        anchorNode.setParent(arFragment.arSceneView.scene)
        val transFromAbleNode = TransformableNode(arFragment.transformationSystem)
        transFromAbleNode.setParent(anchorNode)
        transFromAbleNode.renderable = renderable
        transFromAbleNode.select()

        addName(anchorNode, transFromAbleNode, cardName)
    }

    private fun addName(anchorNode: AnchorNode, node: TransformableNode, name: String?) {
        ViewRenderable.builder().setView(this, R.layout.name_card)
            .build()
            .thenAccept { viewRenderable ->
                val nameView = TransformableNode(arFragment.transformationSystem)
                nameView.localPosition = Vector3(0f, node.localPosition.y + 0.5f, 0f)
                nameView.setParent(anchorNode)
                nameView.renderable = viewRenderable
                nameView.select()

                val txtName = viewRenderable.view as TextView
                txtName.text = name
                txtName.setOnClickListener {
                    anchorNode.setParent(null)
                }
            }
    }

    private fun settingAugmentedImage() {
        val frame = arFragment.arSceneView.arFrame ?: return

        val config = arFragment.arSceneView.session?.config
        config?.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        config?.instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP
        arFragment.arSceneView.session?.configure(config)

        frame.getUpdatedTrackables(AugmentedImage::class.java).forEach { plane ->
            if(plane.trackingState == TrackingState.TRACKING){
                if(plane.name == "qr"){
                    Log.e("+++++", "qr!")
                    if(shouldConfigureSession.not()){
                        shouldConfigureSession = true
                        val node = MyARNode(this, renderableFileList[position])
                        node.setImage(plane)
                        arFragment.arSceneView.scene.addChild(node)
                    }
                }
            }
        }
    }
}