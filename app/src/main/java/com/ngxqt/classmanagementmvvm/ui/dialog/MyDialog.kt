package com.ngxqt.classmanagementmvvm.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.fragment.app.DialogFragment
import com.ngxqt.classmanagementmvvm.R
import com.ngxqt.classmanagementmvvm.data.model.ClassItem
import com.ngxqt.classmanagementmvvm.data.model.StudentItem
import com.ngxqt.classmanagementmvvm.databinding.DialogBinding


class MyDialog constructor(var roll: Int?, var name: String?) : DialogFragment() {

    constructor() : this(null,null)

    companion object {
        const val CLASS_AND_DIALOG = "addClass"
        const val CLASS_UPDATE_DIALOG = "updateClass"
        const val STUDENT_AND_DIALOG = "addStudent"
        const val STUDENT_UPDATE_DIALOG = "updateStudent"
        const val CONFIRM_DIALOG = "confirm"
    }

    private var _bindingDialog: DialogBinding? = null
    private val bindingDialog get() = _bindingDialog!!

    var onItemClassClick: ((ClassItem) -> Unit)? = null
    var onItemStudentClick: ((StudentItem) -> Unit)? = null
    var confirmClick: ((Boolean) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _bindingDialog = DialogBinding.inflate(LayoutInflater.from(activity))
        var dialog: Dialog? = null
        if (tag.equals(CLASS_AND_DIALOG)) dialog = getAddClassDialog()
        if (tag.equals(STUDENT_AND_DIALOG)) dialog = getAddStudentDialog()
        if (tag.equals(CLASS_UPDATE_DIALOG)) dialog = getUpdateClassDialog()
        if (tag.equals(STUDENT_UPDATE_DIALOG)) dialog = getUpdateStudentDialog()
        if (tag.equals(CONFIRM_DIALOG)) dialog = confirmDialog()
        return dialog!!
    }


    private fun getUpdateStudentDialog(): Dialog? {
        val builder = AlertDialog.Builder(requireActivity())
        val view: View = LayoutInflater.from(activity).inflate(R.layout.dialog, null)
        builder.setView(view)

        val title: TextView = view.findViewById(R.id.titleDialog)
        val roll_edt: EditText = view.findViewById(R.id.edt01)
        val name_edt: EditText = view.findViewById(R.id.edt02)
        val cancel: Button = view.findViewById(R.id.cancel_btn)
        val add: Button = view.findViewById(R.id.add_btn)

        title.setText("Update Student")
        roll_edt.setText(roll.toString()+"")
        roll_edt.setHint("ID")
        roll_edt.inputType = InputType.TYPE_CLASS_NUMBER
        //roll_edt.isEnabled = false
        name_edt.setHint("Name")
        name_edt.setText(name.toString()+"")
        cancel.setOnClickListener { dismiss() }
        add.setText("Update")
        add.setOnClickListener {
            var roll = roll_edt.text.toString()
            val name = name_edt.text.toString()
            if(checkNull(roll,name)){
                Toast.makeText(activity,"Please Fill Out Completely",Toast.LENGTH_SHORT).show()
            }else{
                onItemStudentClick?.invoke(StudentItem(null, Integer.parseInt(roll), name, null))
                dismiss()
            }
        }

        return builder.create()
    }

    private fun getUpdateClassDialog(): Dialog? {
        val builder = AlertDialog.Builder(requireActivity())
        val view: View = LayoutInflater.from(activity).inflate(R.layout.dialog, null)
        builder.setView(view)

        val title: TextView = view.findViewById(R.id.titleDialog)
        val class_edt: EditText = view.findViewById(R.id.edt01)
        val subject_edt: EditText = view.findViewById(R.id.edt02)
        val cancel: Button = view.findViewById(R.id.cancel_btn)
        val add: Button = view.findViewById(R.id.add_btn)

        title.setText("Update Class")
        class_edt.setHint("Class Name")
        subject_edt.setHint("Subject Name")
        cancel.setOnClickListener { dismiss() }
        add.setText("Update")
        add.setOnClickListener {
            val className = class_edt.text.toString().trim()
            val subjectName = subject_edt.text.toString().trim()
            if(checkNull(className,subjectName)){
                Toast.makeText(activity,"Please Fill Out Completely",Toast.LENGTH_SHORT).show()
            }else{
                class_edt.setText("")
                subject_edt.setText("")
                onItemClassClick?.invoke(ClassItem(null,className, subjectName))
                dismiss()
            }

        }

        return builder.create()
    }

    private fun getAddClassDialog(): Dialog? {
        /*val builder = AlertDialog.Builder(requireActivity())
        //bindingDialog = DialogBinding.inflate(LayoutInflater.from(activity))
        builder.setView(bindingDialog.root)

        val dialog: AlertDialog =  builder.create()
        dialog.show()

        bindingDialog.cancelBtn.setOnClickListener { dismiss() }
        bindingDialog.addBtn.setOnClickListener {
            val className = bindingDialog.edt01.text.toString().trim()
            val subjectName = bindingDialog.edt02.text.toString().trim()
            onItemClick?.invoke(ClassItem(className, subjectName))
            //listener?.onClick(className, subjectName)
            dismiss()
        }

        return builder.create()*/

        val builder = AlertDialog.Builder(requireActivity())
        val view: View = LayoutInflater.from(activity).inflate(R.layout.dialog, null)
        builder.setView(view)

        val title: TextView = view.findViewById(R.id.titleDialog)
        val class_edt: EditText = view.findViewById(R.id.edt01)
        val subject_edt: EditText = view.findViewById(R.id.edt02)
        val cancel: Button = view.findViewById(R.id.cancel_btn)
        val add: Button = view.findViewById(R.id.add_btn)

        title.setText("Add New Class")
        class_edt.setHint("Class Name")
        subject_edt.setHint("Subject Name")
        cancel.setOnClickListener { dismiss() }
        add.setOnClickListener {
            val className = class_edt.text.toString().trim()
            val subjectName = subject_edt.text.toString().trim()
            if(checkNull(className,subjectName)){
                Toast.makeText(activity,"Please Fill Out Completely",Toast.LENGTH_SHORT).show()
            }else{
                class_edt.setText("")
                subject_edt.setText("")
                onItemClassClick?.invoke(ClassItem(null,className, subjectName))
                dismiss()
            }
        }

        return builder.create()
    }

    private fun getAddStudentDialog(): Dialog? {
        val builder = AlertDialog.Builder(requireActivity())
        val view: View = LayoutInflater.from(activity).inflate(R.layout.dialog, null)
        builder.setView(view)

        val title: TextView = view.findViewById(R.id.titleDialog)
        val roll_edt: EditText = view.findViewById(R.id.edt01)
        val name_edt: EditText = view.findViewById(R.id.edt02)
        val cancel: Button = view.findViewById(R.id.cancel_btn)
        val add: Button = view.findViewById(R.id.add_btn)

        title.setText("Add New Student")
        roll_edt.setHint("ID")
        roll_edt.inputType = InputType.TYPE_CLASS_NUMBER
        name_edt.setHint("Name")
        cancel.setOnClickListener { dismiss() }
        add.setOnClickListener {
            val roll = roll_edt.text.toString()
            val name = name_edt.text.toString()
            if(checkNull(roll,name)){
                Toast.makeText(activity,"Please Fill Out Completely",Toast.LENGTH_SHORT).show()
            }else{
                //roll_edt.setText((Integer.parseInt(roll)+1).toString())
                roll_edt.setText("")
                name_edt.setText("")
                onItemStudentClick?.invoke(StudentItem(null,Integer.parseInt(roll), name, null))
            }
        }

        return builder.create()
    }


    private fun confirmDialog(): Dialog? {
        val builder = AlertDialog.Builder(requireActivity())
        val view: View = LayoutInflater.from(activity).inflate(R.layout.dialog, null)
        builder.setView(view)

        val title: TextView = view.findViewById(R.id.titleDialog)
        val roll_edt: EditText = view.findViewById(R.id.edt01)
        val name_edt: EditText = view.findViewById(R.id.edt02)
        val cancel: Button = view.findViewById(R.id.cancel_btn)
        val add: Button = view.findViewById(R.id.add_btn)

        title.setText("Delete Confirmation")
        roll_edt.isGone = true
        name_edt.isGone = true
        cancel.setOnClickListener { dismiss() }
        add.setText("CONFIRM")
        add.setOnClickListener {
            confirmClick?.invoke(true)
            dismiss()
        }

        return builder.create()
    }

    private fun checkNull(roll: String,name: String): Boolean {
        if((roll!="")&&(name!="")){
            return false
        }
        else return true
    }
}