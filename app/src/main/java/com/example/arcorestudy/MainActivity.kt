package com.example.arcorestudy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.arcorestudy.databinding.ActivityMainBinding
import com.example.arcorestudy.map.MapAnchorActivity
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity() {

    private val binding : ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnMain.setOnClickListener {
            startActivity<ARCoreActivity>()
        }

        binding.btnAugment.setOnClickListener {
            startActivity<ARCoreAugmentedActivity>()
        }

        binding.btnEx1.setOnClickListener {
            startActivity<AugmentedImageActivity>()
        }

        binding.btnEx2.setOnClickListener {
            startActivity<LoadFirebaseActivity>()
        }

        binding.btnEx3.setOnClickListener {
            startActivity<SceneFormActivity>()
        }

        binding.btnEx4.setOnClickListener {
            startActivity<AnimationActivity>()
        }

        binding.btnEx5.setOnClickListener {
            startActivity<CloudAnchorActivity>()
        }

        binding.btnEx6.setOnClickListener {
            startActivity<MapAnchorActivity>()
        }

        binding.btnEx7.setOnClickListener {
            startActivity<PokemonGoActivity>()
        }

        binding.btnEx8.setOnClickListener {
            startActivity<MaterialActivity>()
        }

    }

}