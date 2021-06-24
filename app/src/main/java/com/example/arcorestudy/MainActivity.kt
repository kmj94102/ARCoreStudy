package com.example.arcorestudy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.arcorestudy.databinding.ActivityMainBinding
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {

    private val binding : ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnEx1.setOnClickListener {
            startActivity<AugmentedImageActivity>()
        }

        binding.btnEx2.setOnClickListener {
            startActivity<LoadFirebaseActivity>()
        }

    }

}