package com.ngxqt.classmanagementmvvm.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ngxqt.classmanagementmvvm.R
import com.ngxqt.classmanagementmvvm.data.model.StudentItem
import com.ngxqt.classmanagementmvvm.databinding.FragmentStudentBinding
import com.ngxqt.classmanagementmvvm.ui.adapter.StudentAdapter
import com.ngxqt.classmanagementmvvm.ui.dialog.MyCalendar
import com.ngxqt.classmanagementmvvm.ui.dialog.MyDialog
import com.ngxqt.classmanagementmvvm.utils.DbHelper
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONArray
import org.json.JSONObject

@AndroidEntryPoint
class StudentFragment : Fragment() {
    private var _binding: FragmentStudentBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DbHelper
    private lateinit var studentAdapter: StudentAdapter
    private val studentItems: ArrayList<StudentItem> = ArrayList()
    private lateinit var className: String
    private lateinit var subjectName: String
    private lateinit var position: String
    private var cid: Long? = null
    private lateinit var calendar: MyCalendar
    private var total_Students = 0
    private var total_Present = 0
    private var total_Absences = 0
    private var total_ExcusedAbsences = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStudentBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        calendar = MyCalendar()
        dbHelper = DbHelper(requireContext())

        className = arguments?.getString("className").toString()
        subjectName = arguments?.getString("subjectName").toString()
        position = arguments?.getInt("position", -1).toString()
        cid = arguments?.getLong("cid",-1)!!

        setToolbar()
        setBottom()

        /**Cài đặt RecyclerView và Adapter để hiển thị item*/
        val recyclerView = binding.studentRecycler
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)
        studentAdapter = StudentAdapter(studentItems)
        recyclerView.adapter = studentAdapter

        studentAdapter.onItemClick = {
            changStatus(it)
        }
        studentAdapter.onContactClick = {
            gotoContactActivity(it)
        }
        loadData()
        //readDataJson()
    }

    private fun setBottom() {
        binding.apply {
            totalPresent.setOnClickListener {
                for (i in 0 until studentItems.size){
                    if (studentItems.get(i).status != "P"){
                        studentItems.get(i).status = "P"
                        studentAdapter.notifyItemChanged(i)
                    }
                }
                setAttendanceBottom()
            }
            totalAbsences.setOnClickListener {
                for (i in 0 until studentItems.size){
                    if (studentItems.get(i).status != "A"){
                        studentItems.get(i).status = "A"
                        studentAdapter.notifyItemChanged(i)
                    }
                }
                setAttendanceBottom()
            }
            totalExcusedAbsences.setOnClickListener {
                for (i in 0 until studentItems.size){
                    if (studentItems.get(i).status != "EA"){
                        studentItems.get(i).status = "EA"
                        studentAdapter.notifyItemChanged(i)
                    }
                }
                setAttendanceBottom()
            }
        }

    }

    private fun gotoContactActivity(position: Int) {
        val bundle = bundleOf(
            "studentName" to studentItems.get(position).name,
            "studentId" to studentItems.get(position).roll,
            "studentPhone" to "0347846669"
        )
        findNavController().navigate(R.id.action_studentFragment_to_contactFragment,bundle)
    }

    private fun readDataJson() {
        val defaultIdList = mutableListOf<Int>()
        val defaultNameList = mutableListOf<String>()

        /**Đọc list_student.json*/
        val jsonData = requireContext().resources.openRawResource(
            requireContext().resources.getIdentifier(
                "list_student",
                "raw",requireContext().packageName
            )
        ).bufferedReader().use { it.readText() }
        val outputJsonArray = JSONObject(jsonData).getJSONArray("data") as JSONArray
        /**Gán Data vào List*/
        for (i in 0 until outputJsonArray.length()){
            val defaultId =  Integer.parseInt(outputJsonArray.getJSONObject(i).getString("studentId"))
            val defaultName = outputJsonArray.getJSONObject(i).getString("studentName")
            defaultIdList.add(defaultId)
            defaultNameList.add(defaultName)
        }

        addDefaultStudent(defaultIdList,defaultNameList)
    }

    private fun addDefaultStudent(defaultId: MutableList<Int>, defaultName: MutableList<String>) {
        val cursor = dbHelper.getStudentTabale(cid!!)
        if (cursor.count == 0){
            for (i in 0..defaultId.size-1){
                val roll = defaultId[i]
                val name = defaultName[i]
                Log.i("LOG_DEFAUL", i.toString()+" "+roll.toString()+" "+i.toString()+" "+name)
                val sid = dbHelper.addStudent(cid!!,roll,name)
                dbHelper.addStatus(sid,cid!!,calendar.getDate(),"P")
                val studentItem = StudentItem(sid,roll,name,"P")
                studentItems.add(studentItem)
                studentAdapter.notifyDataSetChanged()
            }
        }
        cursor.close()
    }

    private fun loadData() {
        val cursor = dbHelper.getStudentTabale(cid!!)
        Log.i("TAG","LOAD_DATA")
        studentItems.clear()
        total_Students = 0
        while (cursor.moveToNext()){
            val sid = cursor.getLong(cursor.getColumnIndex(DbHelper.S_ID))
            val roll = cursor.getInt(cursor.getColumnIndex(DbHelper.STUDENT_ROLL_KEY))
            val name = cursor.getString(cursor.getColumnIndex(DbHelper.STUDENT_NAME_KEY))
            val status = dbHelper.getStatus(sid,calendar.getDate())
            studentItems.add(StudentItem(sid,roll,name,status))
            total_Students++
        }
        studentAdapter.notifyDataSetChanged()
        cursor.close()
        binding.totalStudents.setText(total_Students.toString())
        loadStatusData()
    }

    /**Xử lý Status*/
    private fun changStatus(position: Int) {
        var status = studentItems.get(position).status
        if (status.equals("P")) {
            status = "A"
        } else if (status.equals("A")) {
            status = "EA"
        } else if (status.equals("EA")){
            status = "P"
        } else {
            status = "P"
        }
        studentItems.get(position).status = status
        studentAdapter.notifyItemChanged(position)
        setAttendanceBottom()
    }

    private fun saveStatus() {
        for (studentItem: StudentItem in studentItems){
            val status = studentItem.status

            Log.i("LOG_VALUE","${studentItem.sid!!} + $cid + ${calendar.getDate()} + $status")
            val value = dbHelper.addStatus(studentItem.sid!!,cid!!,calendar.getDate(),status)
            //Log.i("LOG_VALUE",value.toString())
            if (value==-1L){ dbHelper.updateStatus(studentItem.sid!!,calendar.getDate(),status) }
            Log.i("TAG","SAVE_STATUS")
        }
    }

    private fun loadStatusData(){
        for (studentItem: StudentItem in studentItems){
            val status = dbHelper.getStatus(studentItem.sid!!,calendar.getDate())
            studentItem.status = status
        }
        setAttendanceBottom()
        studentAdapter.notifyDataSetChanged()
    }

    private fun setAttendanceBottom(){
        total_Present = 0
        total_Absences = 0
        total_ExcusedAbsences = 0
        for (studentItem: StudentItem in studentItems){
            val status = studentItem.status
            if(status == "P"){
                total_Present++
            } else if (status == "A"){
                total_Absences++
            } else if (status == "EA"){
                total_ExcusedAbsences++
            }
        }
        binding.apply {
            totalPresent.setText(total_Present.toString())
            totalAbsences.setText(total_Absences.toString())
            totalExcusedAbsences.setText(total_ExcusedAbsences.toString())
        }
    }

    private fun setToolbar() {
        binding.toolbarStudent.apply {
            titleToolbar.setText(className)
            subtitleToolbar.setText(subjectName+" | "+calendar.getDate())
            back.setOnClickListener { requireActivity().onBackPressed() }
            save.setOnClickListener {
                Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
                saveStatus()
            }
            toolbar.inflateMenu(R.menu.student_menu)
            toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.add_student -> showAddStudentDialog()
                    R.id.show_Calendar -> showCalendar()
                    R.id.show_attendance_sheet -> openSheetList()
                }
                true
            }
        }
    }

    /**Chọn Menu*/
    private fun showCalendar() {
        calendar.show(parentFragmentManager,"")
        calendar.onCalendarOkClick = {year, month, day ->
            onCalendarOkClicked(year, month, day)
        }
    }

    private fun onCalendarOkClicked(year: Int, month: Int, day: Int) {
        calendar.setDate(year, month, day)
        binding.toolbarStudent.subtitleToolbar.setText(subjectName+" | "+calendar.getDate())
        loadStatusData()
    }

    private fun showAddStudentDialog() {
        val dialog = MyDialog()
        dialog.show(parentFragmentManager, MyDialog.STUDENT_AND_DIALOG)
        dialog.onItemStudentClick = {
            addStudent(it.roll!!, it.name)
        }
    }

    private fun addStudent(roll: Int, name: String) {
        val sid = dbHelper.addStudent(cid!!,roll,name)
        dbHelper.addStatus(sid,cid!!,calendar.getDate(),"P")
        val studentItem = StudentItem(sid,roll,name,"P")
        studentItems.add(studentItem)
        studentAdapter.notifyDataSetChanged()
        Toast.makeText(requireContext(),"Add Success", Toast.LENGTH_SHORT).show()
        loadData()
    }

    private fun openSheetList() {
        val idArray  = LongArray(studentItems.size)
        val rollArray = IntArray(studentItems.size)
        val nameArray = Array<String?>(studentItems.size){null}
        for (i in idArray.indices){
            idArray[i] = studentItems.get(i).sid!!
        }
        for (i in rollArray.indices){
            rollArray[i] = studentItems.get(i).roll!!
        }
        for (i in nameArray.indices){
            nameArray[i] = studentItems.get(i).name
        }
        val bundle = bundleOf(
            "cid" to cid,
            "idArray" to idArray,
            "rollArray" to rollArray,
            "nameArray" to nameArray,
            "className" to className,
            "subjectName" to subjectName
        )
        findNavController().navigate(R.id.action_studentFragment_to_sheetListFragment,bundle)
    }

    /**Bấm giữ item để Delete hoặc Update Student*/
    override fun onContextItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            0 -> showUpdateStudentDialog(item.groupId)
            1 -> showComfirmDialog(item.groupId)
        }
        return super.onContextItemSelected(item)
    }

    private fun showUpdateStudentDialog(position: Int) {
        val dialog = MyDialog(studentItems.get(position).roll,studentItems.get(position).name)
        dialog.show(parentFragmentManager, MyDialog.STUDENT_UPDATE_DIALOG)
        dialog.onItemStudentClick = {
            updateStudent(position,it.roll!!,it.name)
        }
    }

    private fun updateStudent(position: Int, roll: Int,name: String) {
        dbHelper.updateStudent(studentItems.get(position).sid!!,roll, name)
        studentItems.get(position).roll = roll
        studentItems.get(position).name = name
        studentAdapter.notifyItemChanged(position)
        Toast.makeText(requireContext(),"Update Success", Toast.LENGTH_SHORT).show()
    }

    private fun showComfirmDialog(position: Int) {
        val dialog = MyDialog()
        dialog.show(parentFragmentManager, MyDialog.CONFIRM_DIALOG)
        dialog.confirmClick = {
            if (it){
                deleteStudent(position)
            }
        }
    }

    private fun deleteStudent(position: Int) {
        dbHelper.deleteStudent(studentItems.get(position).sid!!)
        studentItems.removeAt(position)
        studentAdapter.notifyItemRemoved(position)
        loadData()
    }
}