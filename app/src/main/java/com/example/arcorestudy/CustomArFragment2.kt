package com.example.arcorestudy

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.ar.core.AugmentedImageDatabase
import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment
import java.io.IOException

class CustomArFragment2 : ArFragment() {

    override fun getSessionConfiguration(session: Session?): Config {
        planeDiscoveryController.hide()

        val config = super.getSessionConfiguration(session)
        config.cloudAnchorMode = Config.CloudAnchorMode.ENABLED
        config.focusMode = Config.FocusMode.AUTO
        config.depthMode = Config.DepthMode.AUTOMATIC
        config.lightEstimationMode= Config.LightEstimationMode.AMBIENT_INTENSITY
        config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL

        if(!buildDatabase(config, session)){
            Log.e("++++++", "Error database")
        }
        config.updateMode = Config.UpdateMode.BLOCKING

        return config
    }

    private fun buildDatabase(config: Config, session: Session?) : Boolean{
        val bitmap = loadImage() ?: return false
        val augmentedImageDatabase = AugmentedImageDatabase(session)

        augmentedImageDatabase.addImage("qr", bitmap)
        config.augmentedImageDatabase = augmentedImageDatabase

        return true
    }

    private fun loadImage() : Bitmap? {
        try {
            val inputStream = context?.assets?.open("qr.png")
            return BitmapFactory.decodeStream(inputStream)
        }catch (e : IOException){
            e.printStackTrace()
        }
        return null
    }

}