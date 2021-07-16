package com.example.arcorestudy

import com.google.ar.core.Config
import com.google.ar.core.Session
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.PlaneDiscoveryController

class CustomArFragment : ArFragment() {

    override fun getSessionConfiguration(session: Session?): Config {
        planeDiscoveryController.setInstructionView(null)

        val config = super.getSessionConfiguration(session)
        config.cloudAnchorMode = Config.CloudAnchorMode.ENABLED
        config.focusMode = Config.FocusMode.AUTO
        config.depthMode = Config.DepthMode.AUTOMATIC
        config.lightEstimationMode= Config.LightEstimationMode.DISABLED
        config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL

        return config
    }

}