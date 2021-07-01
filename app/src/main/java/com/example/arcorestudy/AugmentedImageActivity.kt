package com.example.arcorestudy

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.arcorestudy.databinding.ActivityAugmentedImageBinding
import com.google.ar.core.*
import com.google.ar.core.exceptions.*
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Scene
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import org.jetbrains.anko.toast
import java.io.File
import java.io.IOException
import java.lang.Exception

class AugmentedImageActivity : AppCompatActivity(), Scene.OnUpdateListener {

    private val binding : ActivityAugmentedImageBinding by lazy { ActivityAugmentedImageBinding.inflate(layoutInflater) }
    private var session : Session? = null
    private var shouldConfigureSession : Boolean = false
    private val fileName = "out.glb"
    private var renderableFile : File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Request Permission
        Dexter.withActivity(this)
            .withPermission(android.Manifest.permission.CAMERA)
            .withListener(object : PermissionListener{
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    setupSession()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    Toast.makeText(this@AugmentedImageActivity, "Permission need to display camera", Toast.LENGTH_SHORT).show()
                }

                override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {

                }

            }).check()

        initDownload()
        initSceneView()

    }

    private fun initDownload(){
        try {
            val storageRef = Firebase.storage.reference
            val splitFileName = fileName.split(".")
            val file = File.createTempFile(splitFileName[0], splitFileName[1])

            storageRef.child(fileName).getFile(file).addOnSuccessListener {
                renderableFile = file   // buildModel(file)
            }
        }catch (e: IOException){
            e.printStackTrace()
            toast("파일 다운로드중 오류가 발생하였습니다.")
        }catch (e: Exception){
            e.printStackTrace()
            toast("파일 다운로드중 오류가 발생하였습니다.")
        }
    }

//    private fun buildModel(file: File){
//        val renderableSource =
//            RenderableSource
//                .builder()
//                .setSource(this, Uri.parse(file.path), RenderableSource.SourceType.GLB)
//                .setRecenterMode(RenderableSource.RecenterMode.ROOT)
//                .build()
//
//        ModelRenderable
//            .builder()
//            .setSource(this, renderableSource)
//            .setRegistryId(file.path)
//            .build()
//            .thenAccept{ modelRenderable ->
//                toast("다운로드 완료")
//                animationCrab = modelRenderable
//            }
//            .exceptionally {
//                toast("${it.message}")
//                return@exceptionally null
//            }
//    }

    private fun initSceneView(){
        binding.arView.scene.addOnUpdateListener(this)
    }

    private fun setupSession(){
        if(session == null){
            try {
                session = Session(this)
            }catch (e : UnavailableArcoreNotInstalledException){
                e.printStackTrace()
            }catch (e : UnavailableApkTooOldException){
                e.printStackTrace()
            }catch (e : UnavailableSdkTooOldException){
                e.printStackTrace()
            }catch (e : UnavailableDeviceNotCompatibleException){
                e.printStackTrace()
            }

            shouldConfigureSession = true
        }

        if (shouldConfigureSession){
            configSession()
            shouldConfigureSession = false
            binding.arView.setupSession(session)
        }

        try {
            session?.resume()
            binding.arView.resume()
        }catch (e : CameraNotAvailableException){
            e.printStackTrace()
            session = null
            return
        }
    }

    private fun configSession(){
        val config = Config(session)
        if(!buildDatabase(config)){
            Toast.makeText(this, "Error database", Toast.LENGTH_SHORT).show()
        }
        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        session?.configure(config)
    }

    private fun buildDatabase(config : Config) : Boolean{
        val bitmap = loadImage() ?: return false
        val augmentedImageDatabase = AugmentedImageDatabase(session)

        augmentedImageDatabase.addImage("qr", bitmap)
        config.augmentedImageDatabase = augmentedImageDatabase

        return true
    }

    private fun loadImage() : Bitmap? {
        try {
            val inputStream = assets.open("qr.png")
            return BitmapFactory.decodeStream(inputStream)
        }catch (e : IOException){
            e.printStackTrace()
        }
        return null
    }

    override fun onResume() {
        super.onResume()

        // Request Permission
        Dexter.withActivity(this)
            .withPermission(android.Manifest.permission.CAMERA)
            .withListener(object : PermissionListener{
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    setupSession()
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    Toast.makeText(this@AugmentedImageActivity, "Permission need to display camera", Toast.LENGTH_SHORT).show()
                }

                override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest?, token: PermissionToken?) {

                }

            }).check()

    }

    override fun onPause() {
        super.onPause()

        if (session != null){
            binding.arView.pause()
            session?.pause()
        }
    }

    override fun onUpdate(frameTime : FrameTime?) {
        val frame = binding.arView.arFrame
        val updateAugmentedImg = frame?.getUpdatedTrackables(AugmentedImage::class.java)
        updateAugmentedImg?.let {
            for(image in it){
                if(image.trackingState == TrackingState.TRACKING){
                    if(image.name == "qr"){
                        val node = MyARNode(this, renderableFile)
                        node.setImage(image)
                        binding.arView.scene.addChild(node)
                    }
                }
            }
        }
    }

}