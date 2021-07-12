package com.example.arcorestudy

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.text.InputFilter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import androidx.fragment.app.DialogFragment


/** A DialogFragment for the Resolve Dialog Box.  */
class ResolveDialogFragment : DialogFragment() {
    interface OkListener {
        fun onOkPressed(dialogValue: String)
    }

    private var okListener: OkListener? = null
    private var shortCodeField: EditText? = null

    /** Sets a listener that is invoked when the OK button on this dialog is pressed.  */
    fun setOkListener(okListener: OkListener?) {
        this.okListener = okListener
    }

    /**
     * Creates a simple layout for the dialog. This contains a single user-editable text field whose
     * input type is retricted to numbers only, for simplicity.
     */
    private val dialogLayout: LinearLayout
        private get() {
            val context: Context? = context
            val layout = LinearLayout(context)
            shortCodeField = EditText(context)
            shortCodeField!!.inputType = InputType.TYPE_CLASS_NUMBER
            shortCodeField!!.layoutParams =
                LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            shortCodeField!!.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(8))
            layout.addView(shortCodeField)
            layout.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            return layout
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder
            .setView(dialogLayout)
            .setTitle("Resolve Anchor")
            .setPositiveButton(
                "OK"
            ) { _, _ ->
                val shortCodeText = shortCodeField!!.text
                if (okListener != null && shortCodeText != null && shortCodeText.isNotEmpty()) {
                    // Invoke the callback with the current checked item.
                    okListener!!.onOkPressed(shortCodeText.toString())
                }
            }
            .setNegativeButton("Cancel") { dialog, which -> }
        return builder.create()
    }
}