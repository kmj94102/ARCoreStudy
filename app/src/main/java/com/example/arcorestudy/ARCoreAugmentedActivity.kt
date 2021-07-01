package com.example.arcorestudy

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.arcorestudy.databinding.ActivityArcoreAugmentedBinding
import com.google.ar.core.*
import com.google.ar.core.exceptions.*
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Scene
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import org.jetbrains.anko.toast
import java.io.File
import java.io.IOException


class ARCoreAugmentedActivity : AppCompatActivity(), Scene.OnUpdateListener{

    private val binding : ActivityArcoreAugmentedBinding by lazy { ActivityArcoreAugmentedBinding.inflate(layoutInflater) }

    private lateinit var session : Session
    private val fileName = "out.glb"
    private var renderableFile : File? = null
    private var shouldConfigureSession : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initSceneView()
        getAugmentImage()
        initDownload()
        checkCameraPermission()

    }

    private fun initSceneView(){
        binding.arView.scene.addOnUpdateListener(this)
    }

    private fun initDownload(){
        try {
            val storageRef = Firebase.storage.reference
            val splitFileName = fileName.split(".")
            val file = File.createTempFile(splitFileName[0], splitFileName[1])

            storageRef.child(fileName).getFile(file).addOnSuccessListener {
                Log.e("+++++", "다운로드 완료")
                renderableFile = file   // buildModel(file)
            }
        }catch (e: IOException){
            e.printStackTrace()
            toast("파일 다운로드중 오류가 발생하였습니다.")
        }catch (e: java.lang.Exception){
            e.printStackTrace()
            toast("파일 다운로드중 오류가 발생하였습니다.")
        }
    }

    private fun configSession(){
        val config = Config(session)
        if(!buildDatabase(config)){
            Toast.makeText(this, "Error database", Toast.LENGTH_SHORT).show()
        }
        config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
        session.configure(config)
    }

    private fun buildDatabase(config : Config) : Boolean{
        val bitmap = getAugmentImage() ?: return false
        val augmentedImageDatabase = AugmentedImageDatabase(session)

        augmentedImageDatabase.addImage("qr", bitmap)
        config.augmentedImageDatabase = augmentedImageDatabase

        return true
    }

    private fun getAugmentImage() : Bitmap? {
        try {
            val inputStream = assets.open("qr.png")
            val bitmap = BitmapFactory.decodeStream(inputStream)
            binding.img.setImageBitmap(bitmap)
            return bitmap
        }catch (e : Exception){
            e.printStackTrace()
        }
        return null
    }

    private fun checkCameraPermission(){
        TedPermission.with(this)
            .setPermissionListener(object: PermissionListener {
                override fun onPermissionGranted() {
                    createSession()
                }
                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    finish()
                }
            })
            .setDeniedMessage(R.string.permission_denied_message)
            .setPermissions(Manifest.permission.CAMERA)
            .check()
    }

    private fun createSession(){
        try {
            // Create a new ARCore session
            session = Session(this)

            // Create a session confing
            val config = Config(session)

            // 깊이 활성화 또는 증강 얼굴 지원 켜기와 같은 기능별 작업을 여기서 수행하십시오.
            config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE

            // Configure the session
            configSession()

            binding.arView.setupSession(session)
            session.resume()
            binding.arView.resume()

        }catch (e : UnavailableArcoreNotInstalledException){
            e.printStackTrace()
        }catch (e : UnavailableApkTooOldException){
            e.printStackTrace()
        }catch (e : UnavailableSdkTooOldException){
            e.printStackTrace()
        }catch (e : UnavailableDeviceNotCompatibleException){
            e.printStackTrace()
        }catch (e : CameraNotAvailableException){
            e.printStackTrace()
        }catch (e : Exception){
            e.printStackTrace()
        }

    }


    override fun onPause() {
        super.onPause()

        if(this::session.isInitialized){
            session.pause()
            binding.arView.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        session.close()
    }

    override fun onUpdate(frameTime: FrameTime?) {
        val frame = binding.arView.arFrame ?: return

        frame.getUpdatedTrackables(AugmentedImage::class.java).forEach { plane ->
            if(plane.trackingState == TrackingState.TRACKING){
                if(plane.name == "qr"){
                    Log.e("+++++", "qr")
                    if(shouldConfigureSession.not()){
                        shouldConfigureSession = true
                        val node = MyARNode(this, renderableFile)
                        node.setImage(plane)
                        binding.arView.scene.addChild(node)
                    }
                }
            }
        }
    }

}