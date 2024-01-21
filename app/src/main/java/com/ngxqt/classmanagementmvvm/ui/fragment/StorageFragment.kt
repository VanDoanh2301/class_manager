package com.ngxqt.classmanagementmvvm.ui.fragment

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ngxqt.classmanagementmvvm.data.model.ClassItem

import com.ngxqt.classmanagementmvvm.databinding.FragmentStorageBinding
import com.ngxqt.classmanagementmvvm.ui.adapter.ClassAdapter

class StorageFragment : Fragment() {

    private var _binding: FragmentStorageBinding? = null
    private val binding get() = _binding!!
    private val attendanceList: MutableList<String> = mutableListOf()
    private var date: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentStorageBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbarStudent.back.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
        val recy = binding.studentRecycler
        val keyCid = arguments?.getString("keyCid")
        binding.toolbarStudent.subtitleToolbar.visibility = View.GONE
        binding.toolbarStudent.titleToolbar.text = "Storage"
        val databaseReference = FirebaseDatabase.getInstance().getReference("classes").child(keyCid!!)
            .child("attendance").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    attendanceList.clear()
                    for (childSnapshot in snapshot.children) {
                        val attendanceData = childSnapshot.key as String
                        attendanceData?.let {
                            attendanceList.add(it)
                        }
                    }

                    val adapter = ArrayAdapter<String>(requireContext(), R.layout.simple_list_item_1, attendanceList)
                    recy.adapter = adapter
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled event if needed
                }
            })

        recy.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, i, l ->
            date = attendanceList[i]
            val bundle = bundleOf(
                "date" to date,
                "keyCid" to keyCid
            )
            findNavController().navigate(com.ngxqt.classmanagementmvvm.R.id.action_storageFragment_to_historyFragment, bundle)
        }
    }



}