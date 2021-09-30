package com.example.arcorestudy.modle

import com.google.ar.sceneform.math.Vector3

data class RenderingModel(
    val name: String,
    val model : String,
    val direction: Vector3 = Vector3(0f, 0f, 1f),
    val scale: Float = 1f,
    val localPosition: Vector3 = Vector3(0.5f, 0.5f, 0.5f)
)
