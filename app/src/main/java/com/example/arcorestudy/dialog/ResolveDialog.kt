package com.example.arcorestudy.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.example.arcorestudy.databinding.DialogResolveBinding

class ResolveDialog(context: Context, val okClickListener : (String) -> Unit) : Dialog(context) {

    private val binding : DialogResolveBinding by lazy {
        DialogResolveBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setCancelable(false)

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnOk.setOnClickListener {
            okClickListener(binding.editCode.text.toString())
        }

    }

}