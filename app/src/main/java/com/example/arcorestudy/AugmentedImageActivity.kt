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
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import java.io.IOException

class AugmentedImageActivity : AppCompatActivity(), Scene.OnUpdateListener {

    private val binding : ActivityAugmentedImageBinding by lazy { ActivityAugmentedImageBinding.inflate(layoutInflater) }
    private var session : Session? = null
    private var shouldConfigureSession : Boolean = false

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

        initSceneView()

    }

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

        augmentedImageDatabase.addImage("lion", bitmap)
        config.augmentedImageDatabase = augmentedImageDatabase

        return true
    }

    private fun loadImage() : Bitmap? {
        try {
            val inputStream = assets.open("lion_qr.jpeg")
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
                    if(image.name == "lion"){
//                        val node = MyARNode(this, R.raw.lion)
//                        node.setImage(image)
//                        binding.arView.scene.addChild(node)
                    }
                }
            }
        }
    }

}