package com.example.arcorestudy

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.arcorestudy.databinding.ActivityArcoreAugmentedBinding
import com.google.ar.core.*
import com.google.ar.core.exceptions.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import java.io.File


class ARCoreAugmentedActivity : AppCompatActivity(){

    private val binding : ActivityArcoreAugmentedBinding by lazy { ActivityArcoreAugmentedBinding.inflate(layoutInflater) }
    private lateinit var session : Session
    private var shouldConfigureSession : Boolean = false
    private val storage : StorageReference = Firebase.storage.reference
    private val fileName = "out.glb"
    private var renderableFile : File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        checkCameraPermission()

        binding.btnTest.setOnClickListener {
            test()

        }

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
            session.configure(config)

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

    fun test(){
        val sceneViewerIntent = Intent(Intent.ACTION_VIEW)
        val intentUri: Uri = Uri.parse("https://arvr.google.com/scene-viewer/1.0").buildUpon()
            .appendQueryParameter(
                "file",
                "https://raw.githubusercontent.com/KhronosGroup/glTF-Sample-Models/master/2.0/Avocado/glTF/Avocado.gltf"
            )
            .appendQueryParameter("mode", "ar_only")
            .build()
        sceneViewerIntent.data = intentUri
        sceneViewerIntent.setPackage("com.google.ar.core")
        startActivity(sceneViewerIntent)
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

}