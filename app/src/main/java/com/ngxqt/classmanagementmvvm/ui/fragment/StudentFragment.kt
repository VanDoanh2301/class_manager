package com.ngxqt.classmanagementmvvm.ui.fragment

import android.os.Bundle
import android.os.CountDownTimer
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.ngxqt.classmanagementmvvm.R
import com.ngxqt.classmanagementmvvm.data.dto.StudentDto
import com.ngxqt.classmanagementmvvm.data.model.Attendance
import com.ngxqt.classmanagementmvvm.data.model.StudentItem
import com.ngxqt.classmanagementmvvm.databinding.FragmentStudentBinding
import com.ngxqt.classmanagementmvvm.ui.adapter.StudentAdapter
import com.ngxqt.classmanagementmvvm.ui.adapter.StudentAttendanceAdapter
import com.ngxqt.classmanagementmvvm.ui.dialog.MyCalendar
import com.ngxqt.classmanagementmvvm.ui.dialog.MyDialog
import com.ngxqt.classmanagementmvvm.utils.ClassPreference
import com.ngxqt.classmanagementmvvm.utils.DbHelper
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class StudentFragment : Fragment() {
    private var _binding: FragmentStudentBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DbHelper
    private lateinit var studentAdapter: StudentAdapter
    private lateinit var studentAttendance: StudentAttendanceAdapter
    private val studentItems: ArrayList<StudentItem> = ArrayList()
    private val students: ArrayList<StudentItem> = ArrayList()
    private lateinit var className: String
    private lateinit var subjectName: String
    private lateinit var position: String
    private var cid: Long? = null
    private var keyCid: String? = null
    private lateinit var calendar: MyCalendar
    private val databaseReference = FirebaseDatabase.getInstance().getReference("classes")
    val studentIds = mutableListOf<String>()
    private var total_Students = 0
    private var total_Present = 0
    private var total_Absences = 0
    private var total_ExcusedAbsences = 0
    private var role:String? =null
    private var uid:String? =null

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
        cid = arguments?.getLong("cid", -1)!!
        keyCid = arguments?.getString("keyCid")
        uid = arguments?.getString("uid")
        role = arguments?.getString("role")
        Log.d("role", role+"")

        if (role.equals("USER")) {
            binding.btnAttendance.visibility = View.GONE
        } else {
            binding.btnAttendance.visibility =View.VISIBLE
        }
//        setToolbar()
//        setBottom()

        /**Cài đặt RecyclerView và Adapter để hiển thị item*/
        val recyclerView = binding.studentRecycler
        val recy = binding.studentAttendance
        val layoutManager = LinearLayoutManager(requireContext())
        val layoutAd = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager
        recy.layoutManager = layoutAd
        recyclerView.setHasFixedSize(true)
        recy.setHasFixedSize(true)
        studentAttendance = StudentAttendanceAdapter(studentItems)
        studentAdapter = StudentAdapter(students)
        recyclerView.adapter = studentAttendance
        recy.adapter = studentAdapter



//        studentAdapter.onItemClick = {
//            changStatus(it)
//        }
        studentAdapter.onContactClick = {
            gotoContactActivity(it)
        }
        binding.btnAttendance.setOnClickListener(View.OnClickListener {
            gotoAttendance();
        })

        databaseReference.child(keyCid!!).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                    val status = snapshot.child("status").getValue(String::class.java)
                    if (status.equals("true")) {

                        recyclerView.visibility = View.GONE
                        recy.visibility = View.VISIBLE
                        if (role.equals("USER")) {
                            binding.toolbarStudent.attendance.visibility = View.VISIBLE
                        } else {
                            binding.toolbarStudent.attendance.visibility = View.GONE
                        }
                    } else {
                        recyclerView.visibility = View.VISIBLE
                        recy.visibility = View.GONE
                        binding.toolbarStudent.attendance.visibility = View.GONE
                    }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
        loadData()
        //readDataJson()

        binding.toolbarStudent.attendance.setOnClickListener {
            val attendanceRef = databaseReference.child(keyCid!!).child("attendance").
            child(getCurrentDate())
            attendanceRef.child(uid!!).child("status").setValue("P").addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        studentAttendance.notifyDataSetChanged()
                        Toast.makeText(requireContext(), "Attendance success", Toast.LENGTH_SHORT).show()
                    } else {
                    }
                }
            }
    }

    private fun gotoAttendance() {
        val attendanceRef = databaseReference.child(keyCid!!).child("attendance").
        child(getCurrentDate())
        val  studentRef = databaseReference.child(keyCid!!).child("students")
        for (studentId in studentIds) {
            val newAttendance = Attendance(studentId, "A")
            for (i in studentItems) {
                i.status = "A"
                studentRef.child(studentId).child("status").setValue(i.status)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    studentAttendance.notifyDataSetChanged()
                } else {
                }
            }
            }
            attendanceRef.child(studentId).setValue(newAttendance)

        }

        databaseReference.child(keyCid!!).child("status").setValue("true").addOnCompleteListener {

        }

        val TWO_MINUTES = 2 * 60 * 1000 // 2 phút expressed in milliseconds

        val timer = object : CountDownTimer(TWO_MINUTES.toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Mỗi giây được gọi trong suốt 2 phút (1000 milliseconds = 1 giây)
                binding.cdAttendance.visibility = View.GONE
                binding.lnSheet.visibility = View.VISIBLE
                binding.toolbarStudent.apply {
                    val minutes = (millisUntilFinished / 1000) / 60
                    val seconds = (millisUntilFinished / 1000) % 60
                    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
                    subtitleToolbar.text = formattedTime

                }
            }

            override fun onFinish() {
                binding.toolbarStudent.save.visibility = View.VISIBLE
                databaseReference.child(keyCid!!).child("status").setValue("false").addOnCompleteListener {

                }
            }
        }

        timer.start()



    }

    private fun getCurrentDate(): String {
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        return dateFormat.format(currentDate)
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
        findNavController().navigate(R.id.action_studentFragment_to_contactFragment, bundle)
    }


    private fun loadData() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("classes")
        databaseReference.child(keyCid!!).child("students")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    studentItems.clear()
                    for (childSnapshot in snapshot.children) {
                        val sid = childSnapshot.child("sid").getValue(String::class.java)
                        val roll = childSnapshot.child("roll").getValue(Int::class.java)
                        val name = childSnapshot.child("name").getValue(String::class.java)
                        val status = childSnapshot.child("status").getValue(String::class.java)

                        if (sid != null) {
                            studentIds.add(sid)
                        }
                        // Create StudentItem and add it to the list
                        val studentItem = StudentItem(sid, roll, name, status)
                        studentItems.add(studentItem)
                    }

                    studentAdapter.notifyDataSetChanged() // Notify adapter after updating the list
//                binding.totalStudents.text = studentItems.size.toString() // Update the total students count
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle the error
                }
            })

        databaseReference.child(keyCid!!).child("attendance").child(getCurrentDate())
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                   students.clear()
                    for (childSnapshot in snapshot.children) {

                        val studentId = childSnapshot.child("studentId").getValue(String::class.java)
                        val sts = childSnapshot.child("status").getValue(String::class.java)
                        databaseReference.child(keyCid!!).child("students").addValueEventListener(object : ValueEventListener{
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val studentSnapshot = snapshot.child(studentId!!)
                                val sid = studentSnapshot .child("sid").getValue(String::class.java)
                                val roll = studentSnapshot .child("roll").getValue(Int::class.java)
                                val name = studentSnapshot .child("name").getValue(String::class.java)

                                val newStudent =  StudentItem(sid, roll,name,sts)
                                students.add(newStudent)
                                studentAttendance.notifyDataSetChanged()

                            }
                            override fun onCancelled(error: DatabaseError) {

                            }

                        })
                        studentAttendance.notifyDataSetChanged()

                    }


                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle the error
                }
            })

        loadStatusData()
    }

    private fun loadStatusData() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("classes")
        val classReference = databaseReference.child(keyCid!!)

        classReference.child("attendance").child(getCurrentDate()).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    val sid = childSnapshot.child("studentId").getValue(String::class.java)
                    val status = childSnapshot.child("status").getValue(String::class.java)


                    val studentItem = studentItems.find { it.sid == sid }
                    studentItem?.status = status
                }

                setAttendanceBottom()
                studentAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    /**Xử lý Status*/
//    private fun changStatus(position: Int) {
//        var status = studentItems[position].status
//        if (status.equals("P")) {
//            status = "A"
//        } else if (status.equals("A")) {
//            status = "EA"
//        } else if (status.equals("EA")){
//            status = "P"
//        } else {
//            status = "P"
//        }
//
//        val databaseReference = FirebaseDatabase.getInstance().getReference("classes")
//        val classReference = databaseReference.child(keyCid!!)
//        val studentReference = classReference.child("students").child(studentItems[position].sid.toString())
//
//        // Update the status in Firebase
//        studentReference.child("status").setValue(status)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    studentAdapter.notifyItemChanged(position)
//                    setAttendanceBottom()
//                } else {
//                    // Handle error
//                }
//            }
//    }


//    private fun saveStatus() {
//        for (studentItem: StudentItem in studentItems){
//            val status = studentItem.status
//
//            Log.i("LOG_VALUE","${studentItem.sid!!} + $cid + ${calendar.getDate()} + $status")
//            val value = dbHelper.addStatus(studentItem.sid!!,cid!!,calendar.getDate(),status)
//            //Log.i("LOG_VALUE",value.toString())
//            if (value==-1L){ dbHelper.updateStatus(studentItem.sid!!,calendar.getDate(),status) }
//            Log.i("TAG","SAVE_STATUS")
//        }
//    }


    private fun setAttendanceBottom(){
        total_Present = 0
        total_Absences = 0
        total_ExcusedAbsences = 0
        for (studentItem: StudentItem in students){
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

//    private fun setToolbar() {
//        binding.toolbarStudent.apply {
//            titleToolbar.setText(className)
//            subtitleToolbar.setText(subjectName+" | "+calendar.getDate())
//            back.setOnClickListener { requireActivity().onBackPressed() }
//            save.setOnClickListener {
//                Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
//                saveStatus()
//            }
//            toolbar.inflateMenu(R.menu.student_menu)
//            toolbar.setOnMenuItemClickListener {
//                when (it.itemId) {
//                    R.id.add_student -> showAddStudentDialog()
//                    R.id.show_Calendar -> showCalendar()
//                    R.id.show_attendance_sheet -> openSheetList()
//                }
//                true
//            }
//        }
//    }

    /**Chọn Menu*/
    private fun showCalendar() {
        calendar.show(parentFragmentManager, "")
        calendar.onCalendarOkClick = { year, month, day ->
            onCalendarOkClicked(year, month, day)
        }
    }

    private fun onCalendarOkClicked(year: Int, month: Int, day: Int) {
        calendar.setDate(year, month, day)
        binding.toolbarStudent.subtitleToolbar.setText(subjectName + " | " + calendar.getDate())
        loadStatusData()
    }

//    private fun showAddStudentDialog() {
//        val dialog = MyDialog()
//        dialog.show(parentFragmentManager, MyDialog.STUDENT_AND_DIALOG)
//        dialog.onItemStudentClick = {
//            it.name?.let { it1 -> addStudent(it.roll!!, it1) }
//        }
//    }

//    private fun addStudent(roll: Int, name: String) {
//        val sid = dbHelper.addStudent(cid!!,roll,name)
//        dbHelper.addStatus(sid,cid!!,calendar.getDate(),"P")
//        val studentItem = StudentItem(sid,roll,name,"P")
//        studentItems.add(studentItem)
//        studentAdapter.notifyDataSetChanged()
//        Toast.makeText(requireContext(),"Add Success", Toast.LENGTH_SHORT).show()
//        loadData()
//    }
//
//    private fun openSheetList() {
//        val idArray  = LongArray(studentItems.size)
//        val rollArray = IntArray(studentItems.size)
//        val nameArray = Array<String?>(studentItems.size){null}
//        for (i in idArray.indices){
//            idArray[i] = studentItems.get(i).sid!!
//        }
//        for (i in rollArray.indices){
//            rollArray[i] = studentItems.get(i).roll!!
//        }
//        for (i in nameArray.indices){
//            nameArray[i] = studentItems.get(i).name
//        }
//        val bundle = bundleOf(
//            "cid" to cid,
//            "idArray" to idArray,
//            "rollArray" to rollArray,
//            "nameArray" to nameArray,
//            "className" to className,
//            "subjectName" to subjectName
//        )
//        findNavController().navigate(R.id.action_studentFragment_to_sheetListFragment,bundle)
//    }

    /**Bấm giữ item để Delete hoặc Update Student*/
//    override fun onContextItemSelected(item: MenuItem): Boolean {
//        when(item.itemId){
//            0 -> showUpdateStudentDialog(item.groupId)
//            1 -> showComfirmDialog(item.groupId)
//        }
//        return super.onContextItemSelected(item)
//    }

//    private fun showUpdateStudentDialog(position: Int) {
//        val dialog = MyDialog(studentItems.get(position).roll,studentItems.get(position).name)
//        dialog.show(parentFragmentManager, MyDialog.STUDENT_UPDATE_DIALOG)
//        dialog.onItemStudentClick = {
//            it.name?.let { it1 -> updateStudent(position,it.roll!!, it1) }
//        }
//    }

//    private fun updateStudent(position: Int, roll: Int,name: String) {
//        dbHelper.updateStudent(studentItems.get(position).sid!!,roll, name)
//        studentItems.get(position).roll = roll
//        studentItems.get(position).name = name
//        studentAdapter.notifyItemChanged(position)
//        Toast.makeText(requireContext(),"Update Success", Toast.LENGTH_SHORT).show()
//    }
//
//    private fun showComfirmDialog(position: Int) {
//        val dialog = MyDialog()
//        dialog.show(parentFragmentManager, MyDialog.CONFIRM_DIALOG)
//        dialog.confirmClick = {
//            if (it){
//                deleteStudent(position)
//            }
//        }
//    }
//
//    private fun deleteStudent(position: Int) {
//        dbHelper.deleteStudent(studentItems.get(position).sid!!)
//        studentItems.removeAt(position)
//        studentAdapter.notifyItemRemoved(position)
//        loadData()
//    }
}