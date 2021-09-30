package com.example.arcorestudy.modle

import android.animation.Animator
import android.animation.ObjectAnimator
import com.google.ar.sceneform.math.Vector3

inline fun ObjectAnimator.doWhenFinish(
    crossinline block: () -> Unit
){
    addListener(object : Animator.AnimatorListener{
        override fun onAnimationStart(animation: Animator?) = block()
        override fun onAnimationEnd(animation: Animator?) = Unit
        override fun onAnimationCancel(animation: Animator?) = Unit
        override fun onAnimationRepeat(animation: Animator?) = Unit
    })
}


val DEFAULT_POSITION_POKEMON = Vector3(0f, -0.25f, -3f)

val DEFAULT_POSITION_GARDEN = Vector3(0f, -1f, -3f)

val DEFAULT_POSITION_DETAILS_POKEMON = Vector3(0f, -0.88f, -2f)

val DEFAULT_POSITION_POKE_BALL = Vector3(0f, -0.5f, 0.5f)