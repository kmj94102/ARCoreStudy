package com.example.arcorestudy.adapter

import com.google.ar.sceneform.rendering.ModelRenderable

data class ModelItem(
    val imageResource : Int,				// drawable Resource id
    val name : String,						// 이름표에 쓰일 이름
    val fileName : String,					// Firebase Storage에 있는 파일 이름
    var readable: ModelRenderable?= null,	// model 객체
    var isSelected : Boolean = false		// 리사이클러뷰에서 선택 여부
)