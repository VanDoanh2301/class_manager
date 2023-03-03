package com.ngxqt.classmanagementmvvm.ui.dialog

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.text.format.DateFormat
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment
import java.util.Calendar

class MyCalendar: DialogFragment() {
    private val calendar = Calendar.getInstance()
    private var year = Calendar.YEAR
    private var month = Calendar.MONTH
    private var day = Calendar.DAY_OF_MONTH

    var onCalendarOkClick: ((year: Int,month:Int,day: Int) -> Unit)? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return DatePickerDialog(requireActivity(), { view: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
            onCalendarOkClick?.invoke(year, month, dayOfMonth)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
    }

    fun setDate(year: Int,month: Int,day: Int){
        calendar.set(Calendar.YEAR,year)
        calendar.set(Calendar.MONTH,month)
        calendar.set(Calendar.DAY_OF_MONTH,day )
    }

    fun getDate(): String{
        return DateFormat.format("dd.MM.yyyy",calendar).toString()
    }
}