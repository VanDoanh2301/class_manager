package com.ngxqt.classmanagementmvvm.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ngxqt.classmanagementmvvm.R
import com.ngxqt.classmanagementmvvm.databinding.FragmentSheetListBinding
import com.ngxqt.classmanagementmvvm.utils.DbHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SheetListFragment : Fragment() {
    private var _binding: FragmentSheetListBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ArrayAdapter<Any>
    private var listItems = ArrayList<String>()
    private var cid: Long? = null
    private lateinit var className: String
    private lateinit var subjectName: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSheetListBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setToolbar()

        cid = arguments?.getLong("cid",-1)

        loadListItems()
        adapter = ArrayAdapter(requireContext(), R.layout.sheet_list, R.id.date_list_item, listItems as List<String>)
        binding.sheetList.adapter = adapter

        binding.sheetList.setOnItemClickListener { parent, view, position, id ->
            openSheetActivity(position)
        }
    }

    private fun setToolbar() {
        className = arguments?.getString("className").toString()
        subjectName = arguments?.getString("subjectName").toString()
        binding.toolbarSheetList.apply {
            titleToolbar.setText(className)
            subtitleToolbar.setText(subjectName+" | Attendance List")
            back.setOnClickListener { requireActivity().onBackPressed() }
            save.isInvisible = true
        }
    }

    private fun openSheetActivity(position: Int) {
        val idArray  = arguments?.getLongArray("idArray")
        val rollArray = arguments?.getIntArray("rollArray")
        val nameArray = arguments?.getStringArray("nameArray")

        val bundle = bundleOf(
            "idArray" to idArray,
            "rollArray" to rollArray,
            "nameArray" to nameArray,
            "month" to listItems.get(position),
            "className" to className,
            "subjectName" to subjectName
        )
        findNavController().navigate(R.id.action_sheetListFragment_to_sheetFragment,bundle)

    }

    private fun loadListItems() {
        val cursor = DbHelper(requireContext()).getDistincMonths(cid!!)

        listItems.clear()
        while (cursor.moveToNext()){
            val date = cursor.getString(cursor.getColumnIndex(DbHelper.DATE_KEY))
            listItems.add(date.substring(3))
        }
    }
}