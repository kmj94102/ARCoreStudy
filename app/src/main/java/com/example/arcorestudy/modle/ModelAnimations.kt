package com.example.arcorestudy.modle

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.animation.AccelerateDecelerateInterpolator
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.QuaternionEvaluator
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.math.Vector3Evaluator

object ModelAnimations {

    inline fun translateModel(
        anchorNode: Node,
        targetPosition: Vector3,
        durationTime: Long = 150L,
        crossinline doWhenFinish: () -> Unit = {}
    ){
        ObjectAnimator().apply {
            setAutoCancel(false)
            target = anchorNode
            duration = durationTime
            setObjectValues(
                anchorNode.localPosition,
                targetPosition
            )
            setPropertyName("localPosition")
            setEvaluator(Vector3Evaluator())
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }.doWhenFinish { doWhenFinish() }
    }

    inline fun rotateModel(
        anchorNode: Node,
        durationTime: Long,
        crossinline doWhenFinish: () -> Unit
    ){
        ObjectAnimator().apply {
            setAutoCancel(false)
            target = anchorNode
            duration = durationTime
            setObjectValues(
                Quaternion.axisAngle(Vector3(0.0f, 0.0f, 0.0f), 0.0f),
                Quaternion.axisAngle(Vector3(2.0f, 2.0f, 2.0f), 2360f)
            )
            setPropertyName("localPosition")
            setEvaluator(QuaternionEvaluator())
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }.doWhenFinish { doWhenFinish() }
    }

}