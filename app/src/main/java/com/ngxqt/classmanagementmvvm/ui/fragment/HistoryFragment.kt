package com.ngxqt.classmanagementmvvm.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ngxqt.classmanagementmvvm.R
import com.ngxqt.classmanagementmvvm.data.model.StudentItem
import com.ngxqt.classmanagementmvvm.databinding.FragmentHistoryBinding
import com.ngxqt.classmanagementmvvm.databinding.FragmentStorageBinding
import com.ngxqt.classmanagementmvvm.ui.adapter.StudentAdapter
import com.ngxqt.classmanagementmvvm.ui.adapter.StudentAttendanceAdapter


class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private lateinit var studentAdapter: StudentAdapter
    private val students: ArrayList<StudentItem> = ArrayList()
    private var keyCid: String? = null
    private val databaseReference = FirebaseDatabase.getInstance().getReference("classes")
    private var date: String? = null
    private var total_Students = 0
    private var total_Present = 0
    private var total_Absences = 0
    private var total_ExcusedAbsences = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        date = arguments?.getString("date")
        keyCid = arguments?.getString("keyCid")

        binding.toolbarStudent.titleToolbar.text = "Attendance"
        binding.toolbarStudent.subtitleToolbar.text = date

        val recyclerView = binding.studentRecycler
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)
        studentAdapter = StudentAdapter(students)
        recyclerView.adapter = studentAdapter
        loadData()
    }

    private fun loadData() {
        databaseReference.child(keyCid!!).child("attendance").child(date!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    students.clear()
                    for (childSnapshot in snapshot.children) {

                        val studentId = childSnapshot.child("studentId").getValue(String::class.java)
                        val sts = childSnapshot.child("status").getValue(String::class.java)
                        databaseReference.child(keyCid!!).child("students").addValueEventListener(object :
                            ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val studentSnapshot = snapshot.child(studentId!!)
                                val sid = studentSnapshot .child("sid").getValue(String::class.java)
                                val roll = studentSnapshot .child("roll").getValue(Int::class.java)
                                val name = studentSnapshot .child("name").getValue(String::class.java)

                                val newStudent =  StudentItem(sid, roll,name,sts)
                                students.add(newStudent)
                                studentAdapter.notifyDataSetChanged()
                                loadStatusData()


                            }
                            override fun onCancelled(error: DatabaseError) {

                            }

                        })


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

        classReference.child("attendance").child(date!!).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    val sid = childSnapshot.child("studentId").getValue(String::class.java)
                    val status = childSnapshot.child("status").getValue(String::class.java)


                    val studentItem = students.find { it.sid == sid }
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
            totalPresent.text = total_Present.toString()
            totalAbsences.text = total_Absences.toString()
            totalExcusedAbsences.text = total_ExcusedAbsences.toString()
        }
    }
}