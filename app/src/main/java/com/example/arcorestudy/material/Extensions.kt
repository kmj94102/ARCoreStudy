package com.example.arcorestudy.material

import androidx.annotation.ColorInt
import com.google.ar.sceneform.rendering.Color

fun @receiver:ColorInt Int.toArColor(): Color = Color(this)