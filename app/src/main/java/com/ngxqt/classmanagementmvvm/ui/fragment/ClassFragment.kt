package com.ngxqt.classmanagementmvvm.ui.fragment

import android.content.ContentValues.TAG
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.ngxqt.classmanagementmvvm.R
import com.ngxqt.classmanagementmvvm.data.dto.StudentDto
import com.ngxqt.classmanagementmvvm.data.model.ClassItem
import com.ngxqt.classmanagementmvvm.data.model.StudentItem
import com.ngxqt.classmanagementmvvm.databinding.FragmentClassBinding
import com.ngxqt.classmanagementmvvm.databinding.ToolbarBinding
import com.ngxqt.classmanagementmvvm.ui.adapter.ClassAdapter
import com.ngxqt.classmanagementmvvm.ui.dialog.MyCalendar
import com.ngxqt.classmanagementmvvm.ui.dialog.MyDialog
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalTime

@AndroidEntryPoint
class ClassFragment : Fragment() {
    private var _binding: FragmentClassBinding? = null
    private val binding get() = _binding!!
    private lateinit var toolbarBinding: ToolbarBinding
    private lateinit var calendar: MyCalendar
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private lateinit var classAdapter: ClassAdapter
    private val classItems: ArrayList<ClassItem> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentClassBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        calendar = MyCalendar()
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("classes")
        binding.fabMain.setOnClickListener { showDialog() }
        setToolbar()
        loadData()

        val recyclerView = binding.recyclerView
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)
        classAdapter = ClassAdapter(classItems)
        recyclerView.adapter = classAdapter

        classAdapter.onItemClick = {
            gotoStudentFragment(it)
        }
        classAdapter.onMapClick = {
            gotoMapFragment(it)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finishAffinity()
        }
    }

    private fun loadData() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                classItems.clear()
                for (postSnapshot in dataSnapshot.children) {
                    val classItem = postSnapshot.getValue(ClassItem::class.java)
                    if (classItem == null) {
                        addDefaultClass("Lập Trình Ứng Dụng Di Động", "ET4710")
                    } else {
                        classItem?.let { classItems.add(it) }
                    }
                }
                classAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("Firebase", "loadPost:onCancelled", databaseError.toException())
            }
        })
    }

    private fun setToolbar() {
        toolbarBinding = binding.toolbarMain
        toolbarBinding.apply {
            titleToolbar.text = "Class Management App"
            subtitleToolbar.text = "Class List"
            back.isInvisible = true
            save.isInvisible = true
        }
    }

    private fun gotoStudentFragment(position: Int) {
        val idClass = databaseReference.push().key
        val bundle = bundleOf(
            "className" to classItems[position].className,
            "subjectName" to classItems[position].subjectName,
            "position" to position,
            "cid" to classItems[position].cid,
            "keyCid" to classItems[position].cid
        )
        findNavController().navigate(R.id.action_classFragment_to_studentFragment, bundle)
    }

    private fun gotoMapFragment(position: Int) {
        val bundle = bundleOf(
            "className" to classItems[position].className,
            "subjectName" to classItems[position].subjectName,
            "position" to position,
            "cid" to classItems[position].cid
        )
        findNavController().navigate(R.id.action_classFragment_to_mapFragment, bundle)
    }

    private fun showDialog() {
        val dialog = MyDialog()
        dialog.show(parentFragmentManager, MyDialog.CLASS_AND_DIALOG)
        dialog.onItemClassClick = {
            addClass(it.className, it.subjectName)
        }
    }

    private fun addClass(className: String, subjectName: String) {
        val cid = subjectName
        val classItem = ClassItem(cid, className, subjectName)
        databaseReference.child(cid!!).setValue(classItem)
        Toast.makeText(requireContext(), "Add Success", Toast.LENGTH_SHORT).show()
    }

    private fun addDefaultClass(className: String, subjectName: String) {
        if (classItems.isEmpty()) {
            val cid = databaseReference.push().key
            val classItem = ClassItem(cid, className, subjectName, false)
            databaseReference.child(cid!!).setValue(classItem)
            readDataJson(cid)
        }
    }

    private fun readDataJson(cid: String) {
        val defaultIdList = mutableListOf<Int>()
        val defaultNameList = mutableListOf<String>()

        /**Đọc list_student.json*/
        val jsonData = requireContext().resources?.openRawResource(
            requireContext().resources.getIdentifier(
                "list_student",
                "raw",requireContext().packageName
            )
        )?.bufferedReader().use { it?.readText() }
        val outputJsonArray = JSONObject(jsonData).getJSONArray("data") as JSONArray
        /**Gán Data vào List*/
        for (i in 0 until outputJsonArray.length()){
            val defaultId =  Integer.parseInt(outputJsonArray.getJSONObject(i).getString("studentId"))
            val defaultName = outputJsonArray.getJSONObject(i).getString("studentName")
            defaultIdList.add(defaultId)
            defaultNameList.add(defaultName)
        }

        addDefaultStudent(defaultIdList,defaultNameList,cid)
    }

    private fun addDefaultStudent(defaultId: MutableList<Int>, defaultName: MutableList<String>, cid: String) {
        val studentsRef = databaseReference.child(cid).child("students")

        studentsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (!dataSnapshot.exists()) {
                    for (i in 0 until defaultId.size) {
                        val roll = defaultId[i]
                        val name = defaultName[i]
                        auth.createUserWithEmailAndPassword("${roll}@gmail.com", roll.toString())
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d(TAG, "createUserWithEmail:success")
                                    val user = auth.currentUser
                                    val userId = user?.uid
                                    val userEmail = user?.email

                                    val newStudentRef = studentsRef.child(userId.toString())
                                    val studentItem = StudentDto(userId, roll, name, userEmail, roll.toString(), "Student")
                                    newStudentRef.setValue(studentItem)
                                } else {
                                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                                }
                            }

                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error if needed
                Log.e(TAG, "onCancelled", databaseError.toException())
            }
        })

    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            0 -> showUpdateDialog(item.groupId)
            1 -> showConfirmDialog(item.groupId)
        }
        return super.onContextItemSelected(item)
    }

    private fun showUpdateDialog(position: Int) {
        val dialog = MyDialog()
        dialog.show(parentFragmentManager, MyDialog.CLASS_UPDATE_DIALOG)
        dialog.onItemClassClick = { updateClass(position, it.className, it.subjectName) }
    }

    private fun updateClass(position: Int, className: String, subjectName: String) {
        val classId = classItems[position].cid ?: return // Ensure classId is not null
        val classRef = databaseReference.child(classId)

        val updatedClassItem = ClassItem(classId, className, subjectName)
        classRef.setValue(updatedClassItem)
            .addOnSuccessListener {
                classItems[position] = updatedClassItem
                classAdapter.notifyItemChanged(position)
                Toast.makeText(requireContext(), "Update Success", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Handle the failure
                Toast.makeText(requireContext(), "Update Failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showConfirmDialog(position: Int) {
        val dialog = MyDialog()
        dialog.show(parentFragmentManager, MyDialog.CONFIRM_DIALOG)
        dialog.confirmClick = {
            if (it) {
                deleteClass(position)
            }
        }
    }

    private fun deleteClass(position: Int) {
        if (classItems.isNotEmpty() && position >= 0 && position < classItems.size) {
            val classId = classItems[position].cid ?: return // Ensure classId is not null
            val classRef = databaseReference.child(classId)

            classRef.removeValue()
                .addOnSuccessListener {
//                    classItems.removeAt(position)
                    classAdapter.notifyItemRemoved(position)
                    Toast.makeText(requireContext(), "Delete Success", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    // Handle the failure
                    Toast.makeText(requireContext(), "Delete Failed", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "Invalid position", Toast.LENGTH_SHORT).show()
        }
    }
}
