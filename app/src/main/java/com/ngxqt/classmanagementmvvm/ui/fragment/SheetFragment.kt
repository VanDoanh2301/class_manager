package com.ngxqt.classmanagementmvvm.ui.fragment

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import com.ngxqt.classmanagementmvvm.R
import com.ngxqt.classmanagementmvvm.databinding.FragmentClassBinding
import com.ngxqt.classmanagementmvvm.databinding.FragmentSheetBinding
import com.ngxqt.classmanagementmvvm.utils.DbHelper
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class SheetFragment : Fragment() {
    private var _binding: FragmentSheetBinding? = null
    private val binding get() = _binding!!
    private val day_of_month = 31

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSheetBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showTable()
    }

    private fun setToolbar(month: String) {
        val className = arguments?.getString("className").toString()
        val subjectName = arguments?.getString("subjectName").toString()
        binding.toolbarSheet.apply {
            titleToolbar.setText(className)
            subtitleToolbar.setText("Attendance Table | "+month)
            back.setOnClickListener { requireActivity().onBackPressed() }
            //save.setOnClickListener { createPdf() }
            save.isInvisible = true
        }
    }

    private fun showTable() {
        val dbHelper = DbHelper(requireContext())

        val idArray = arguments?.getLongArray("idArray")
        val rollArray = arguments?.getIntArray("rollArray")
        val nameArray = arguments?.getStringArray("nameArray")
        val month = arguments?.getString("month")

        setToolbar(month!!)

        val rowSize: Int = idArray!!.size + 2
        val rows = arrayOfNulls<TableRow>(rowSize)
        val roll_tvs = arrayOfNulls<TextView>(rowSize)
        val name_tvs = arrayOfNulls<TextView>(rowSize)
        val total_tvs = arrayOfNulls<TextView>(rowSize)
        val status_tvs = Array(rowSize) { arrayOfNulls<TextView>(day_of_month + 1) }

        Log.i(
            "LOG_SIZE", "rowSize: " + rowSize.toString() +
                    ", rows: " + rows.size +
                    ", roll_tvs: " + roll_tvs.size +
                    ", name_tvs: " + name_tvs.size +
                    ", status_tvs: " + status_tvs.size +
                    ", day_in_month: " + day_of_month
        )
        for (i in 0 until rowSize) {
            roll_tvs[i] = TextView(requireContext())
            name_tvs[i] = TextView(requireContext())
            total_tvs[i] = TextView(requireContext())
            for (j in 1..day_of_month) {
                status_tvs[i][j] = TextView(requireContext())
            }
        }

        /**Cài đặt Header*/
        roll_tvs[0]!!.setText("ID")
        roll_tvs[0]!!.setTypeface(roll_tvs[0]!!.typeface, Typeface.BOLD)
        name_tvs[0]!!.setText("Name")
        name_tvs[0]!!.setTypeface(name_tvs[0]!!.typeface, Typeface.BOLD)
        total_tvs[0]!!.setText("Total\nAbsence")
        total_tvs[0]!!.setTypeface(total_tvs[0]!!.typeface, Typeface.BOLD)
        for (i in 1..day_of_month) {
            status_tvs[0][i]!!.setText(i.toString())
            status_tvs[0][i]!!.setTypeface(status_tvs[0][i]!!.typeface, Typeface.BOLD)
        }

        /** Đặt trạng thái cho sinh viên*/
        for (i in 1 until rowSize-1) {
            roll_tvs[i]!!.setText(rollArray!![i - 1].toString())
            name_tvs[i]!!.setText(nameArray!![i - 1])

            var totalAbsence = 0.0
            for (j in 1..day_of_month) {
                var day = j.toString()
                if (day.length == 1) {
                    day = "0" + day
                }
                val date = day + "." + month
                val status = dbHelper.getStatus(idArray[i - 1], date)
                status_tvs[i][j]!!.setText(status)
                if (status=="A") {
                    totalAbsence++
                    status_tvs[i][j]!!.setBackgroundColor(Color.parseColor("#33FF0000"))
                } else if (status=="EA") {
                    totalAbsence = totalAbsence + 0.5
                    status_tvs[i][j]!!.setBackgroundColor(Color.parseColor("#39FFBF00"))
                }
            }
            if(totalAbsence>3){
                total_tvs[i]!!.apply {
                    setText(totalAbsence.toString())
                    setTypeface(total_tvs[0]!!.typeface, Typeface.BOLD)
                    setBackgroundColor(Color.parseColor("#33FF0000"))
                }
            } else if(totalAbsence==3.0){
                total_tvs[i]!!.apply {
                    setText(totalAbsence.toString())
                    setBackgroundColor(Color.parseColor("#39FFBF00"))
                }
            } else{
                total_tvs[i]!!.setText(totalAbsence.toString())
            }
        }

        /** Đặt trạng thái cho tính tổng*/
        roll_tvs[rowSize-1]!!.apply {
            setText("Total Student")
            setTypeface(roll_tvs[rowSize-1]!!.typeface, Typeface.BOLD)
        }
        name_tvs[rowSize-1]!!.apply {
            setText(nameArray?.size.toString())
            setTypeface(name_tvs[rowSize-1]!!.typeface, Typeface.BOLD)
        }
        total_tvs[rowSize-1]!!.apply {
            setText("Total Present\nEach Day")
            setTypeface(total_tvs[rowSize-1]!!.typeface, Typeface.BOLD)
        }
        for (j in 1..day_of_month) {
            var day = j.toString()
            if (day.length == 1) {
                day = "0" + day
            }
            val date = day + "." + month

            var totalPresent = 0
            for(i in 1 until rowSize-1){
                val status = dbHelper.getStatus(idArray[i - 1], date)
                if (status == "P"){
                    totalPresent++
                }
            }

            if (totalPresent!=0){
                status_tvs[rowSize-1][j]!!.apply {
                    setText(totalPresent.toString())
                    setTypeface(status_tvs[rowSize-1][j]!!.typeface, Typeface.BOLD)
                }
            }

        }

        /**Cài đặt màn hiển thị*/
        for (i in 0 until rowSize) {
            rows[i] = TableRow(requireContext())

            if (i%2==0){
                rows[i]!!.setBackgroundColor(Color.parseColor("#EEEEEE"))
            }else{
                rows[i]!!.setBackgroundColor(Color.parseColor("#E4E4E4"))
            }

            roll_tvs[i]!!.setPadding(16, 16, 16, 16)
            name_tvs[i]!!.setPadding(16, 16, 16, 16)
            total_tvs[i]!!.setPadding(16, 16, 16, 16)


            rows[i]!!.apply {
                addView(roll_tvs[i])
                addView(name_tvs[i])
                addView(total_tvs[i])
            }

            for (j in 1..day_of_month step 1) {
                status_tvs[i][j]!!.setPadding(16, 16, 16, 16)

                rows[i]!!.addView(status_tvs[i][j])
            }

            binding.tableLayout.addView(rows[i])
        }
        binding.tableLayout.showDividers = TableLayout.SHOW_DIVIDER_MIDDLE
    }

    private fun getDayInMonth(month: String): Int {
        val monthIndex = Integer.valueOf(month.substring(0, 1))
        val year = Integer.valueOf(month.substring(4))

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, monthIndex)
        calendar.set(Calendar.YEAR, year)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }
}