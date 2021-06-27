package com.example.arcorestudy.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.arcorestudy.R
import com.example.arcorestudy.databinding.CellModelBinding
import com.google.ar.sceneform.rendering.ModelRenderable
import org.jetbrains.anko.backgroundResource

class ModelAdapter(val modelSelectListener : (ModelRenderable?) -> Unit, val downloadListener : (ModelItem, Int) -> Unit) : ListAdapter<ModelItem, ModelAdapter.ModelViewHolder>(diffUtil) {

    inner class ModelViewHolder(private val binding : CellModelBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(modelItem: ModelItem){
            binding.imageModel.setImageResource(modelItem.imageResource)
            binding.btnDownload.isVisible = modelItem.readable == null
            binding.layoutBackground.backgroundResource = if(modelItem.isSelected) R.drawable.bg_round_teal else R.drawable.bg_round_white

            binding.root.setOnClickListener {
                setCheckedPosition(layoutPosition)

                if(modelItem.isSelected){
                    if(modelItem.readable == null){
                        downloadListener(modelItem, layoutPosition)
                    }else{
                        modelSelectListener(modelItem.readable)
                    }
                }else{
                    modelItem.isSelected = true
                }
            }
        }
    }

    private fun setCheckedPosition(position: Int){
        currentList.forEachIndexed { index, modelItem ->
            modelItem.isSelected = index == position
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModelViewHolder {
        return ModelViewHolder(CellModelBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ModelViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<ModelItem>(){
            override fun areItemsTheSame(oldItem: ModelItem, newItem: ModelItem): Boolean = oldItem == newItem

            override fun areContentsTheSame(oldItem: ModelItem, newItem: ModelItem): Boolean = oldItem.fileName == newItem.fileName
        }
    }
}
