package com.example.arcorestudy

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.arcorestudy.StorageManager.CloudAnchorIdListener
import com.example.arcorestudy.databinding.ActivityCloudAnchorBinding
import com.example.arcorestudy.dialog.ResolveDialog
import com.google.ar.core.Anchor
import com.google.ar.core.Anchor.CloudAnchorState
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import org.jetbrains.anko.alert
import java.io.File

@SuppressLint("SetTextI18n")
class CloudAnchorActivity : AppCompatActivity() {

    private lateinit var fragment : CustomArFragment
    private var cloudAnchor: Anchor? = null
    private var appAnchorState = AppAnchorState.NONE
    private lateinit var storageManager : StorageManager

    private val binding : ActivityCloudAnchorBinding by lazy {
        ActivityCloudAnchorBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        fragment = supportFragmentManager.findFragmentById(R.id.sceneFormFragment) as CustomArFragment
        fragment.arSceneView.scene.addOnUpdateListener(this::onUpdateFrame)

        binding.clearBtn.setOnClickListener {
            setCloudAnchor(null)
        }

        binding.resolveButton.setOnClickListener {
            if(cloudAnchor != null){
                binding.textView.text = "Please clear Anchor"
                return@setOnClickListener
            }

            ResolveDialog(this){
                onResolveOkPressed(it)
            }.show()
        }

        fragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            if(plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING){
                return@setOnTapArPlaneListener
            }

            val newAnchor = fragment.arSceneView.session?.hostCloudAnchor(hitResult.createAnchor())
            setCloudAnchor(newAnchor)

            appAnchorState = AppAnchorState.HOSTING
            binding.textView.text = "Now hosting anchor..."

            placeObject(fragment, cloudAnchor, "out.glb")
        }

        storageManager = StorageManager(this)
    }

    private fun onResolveOkPressed(dialogValue: String) {
        val shortCode = dialogValue.toInt()
        storageManager.getCloudAnchorID(shortCode, object : CloudAnchorIdListener {
            override fun onCloudAnchorIdAvailable(cloudAnchorId: String?) {
                val resolvedAnchor = fragment.arSceneView.session!!.resolveCloudAnchor(cloudAnchorId)
                setCloudAnchor(resolvedAnchor)
                placeObject(fragment, cloudAnchor, "out.glb")
                binding.textView.text = "Now Resolving Anchor.."
                appAnchorState = AppAnchorState.RESOLVING
            }
        })
    }

    private fun setCloudAnchor(newAnchor: Anchor?){
        if(cloudAnchor != null){
            cloudAnchor?.detach()
        }

        cloudAnchor = newAnchor
        appAnchorState = AppAnchorState.NONE
    }

    private fun placeObject(arFragment: ArFragment, anchor : Anchor?, fileName : String){
        val splitFileName = fileName.split(".")
        val file = File.createTempFile(splitFileName[0], splitFileName[1])

        assets.open(fileName).use { input->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        val renderableSource =
            RenderableSource
                .builder()
                .setSource(this, Uri.parse(file.path), RenderableSource.SourceType.GLB)
                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                .build()

        ModelRenderable
            .builder()
            .setSource(this, renderableSource)
            .build()
            .thenAccept { renderable->
                addNodeToScene(arFragment, anchor, renderable)
            }
            .exceptionally { throwable ->
                alert {
                    title = "Error"
                    message = throwable.message.toString()
                }.show()
                return@exceptionally null
            }
    }

    private fun addNodeToScene(arFragment: ArFragment, anchor: Anchor?, renderable: ModelRenderable) {
        val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(arFragment.transformationSystem)
        node.renderable = renderable
        node.setParent(anchorNode)
        arFragment.arSceneView.scene.addChild(anchorNode)
        node.select()
    }

    private fun onUpdateFrame(frameTime: FrameTime){
        checkUpdatedAnchor()
    }

    @Synchronized private fun checkUpdatedAnchor(){
        if (appAnchorState !== AppAnchorState.HOSTING && appAnchorState !== AppAnchorState.RESOLVING) {
            return
        }
        val cloudState = cloudAnchor!!.cloudAnchorState
        if (appAnchorState === AppAnchorState.HOSTING) {
            if (cloudState.isError) {
                binding.textView.text = "Error hosting anchor.. $cloudState"
                appAnchorState = AppAnchorState.NONE
            } else if (cloudState == CloudAnchorState.SUCCESS) {
                storageManager.nextShortCode(object : StorageManager.ShortCodeListener{
                    override fun onShortCodeAvailable(shortCode: Int?) {
                        if (shortCode == null) {
                            binding.textView.text = "Could not get shortCode"
                            return
                        }
                        storageManager.storeUsingShortCode(shortCode, cloudAnchor!!.cloudAnchorId)
                        binding.textView.text = "Anchor hosted! Cloud Short Code: $shortCode"
                    }

                })
                binding.textView.text = "Anchor hosted with id ${cloudAnchor?.cloudAnchorId}"
                appAnchorState = AppAnchorState.HOSTED
            }
        }else if (appAnchorState === AppAnchorState.RESOLVING) {
            if (cloudState.isError) {
                binding.textView.text = "Error resolving anchor : $cloudState"

                appAnchorState = AppAnchorState.NONE
            } else if (cloudState == CloudAnchorState.SUCCESS) {
                binding.textView.text = "Anchor resolved successfully"
                appAnchorState = AppAnchorState.RESOLVED
            }
        }
    }

    private enum class AppAnchorState{
        NONE,
        HOSTING,
        HOSTED,
        RESOLVING,
        RESOLVED
    }

}