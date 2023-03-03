package com.ngxqt.classmanagementmvvm.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ngxqt.classmanagementmvvm.R
import com.ngxqt.classmanagementmvvm.data.model.ClassItem
import com.ngxqt.classmanagementmvvm.databinding.FragmentClassBinding
import com.ngxqt.classmanagementmvvm.databinding.ToolbarBinding
import com.ngxqt.classmanagementmvvm.ui.adapter.ClassAdapter
import com.ngxqt.classmanagementmvvm.ui.dialog.MyCalendar
import com.ngxqt.classmanagementmvvm.ui.dialog.MyDialog
import com.ngxqt.classmanagementmvvm.utils.DbHelper
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONArray
import org.json.JSONObject

@AndroidEntryPoint
class ClassFragment : Fragment() {
    private var _binding: FragmentClassBinding? = null
    private val binding get() = _binding!!
    private lateinit var toolbarBinding: ToolbarBinding
    private lateinit var calendar: MyCalendar
    private lateinit var dbHelper: DbHelper
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
        dbHelper = DbHelper(requireContext())

        binding.fabMain.setOnClickListener { showDialog() }
        setToolbar()
        loadData()

        /**Cài đặt RecyclerView và Adapter để hiển thị item*/
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

        addDefaultClass("Lập Trình Ứng Dụng Di Động","ET4710 - 137364")

        //Chặn Back
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finishAffinity()
        }
    }

    /**Load data Class từ Database*/
    private fun loadData() {
        val cursor = dbHelper.getClassTabale()

        classItems.clear()
        while (cursor.moveToNext()){
            val cid = cursor.getLong(cursor.getColumnIndex(DbHelper.C_ID))
            val className = cursor.getString(cursor.getColumnIndex(DbHelper.CLASS_NAME_KEY))
            val subjectName = cursor.getString(cursor.getColumnIndex(DbHelper.SUBJECT_NAME_KEY))

            classItems.add(ClassItem(cid,className,subjectName))
        }
    }

    /**Hiển thị Toolbar*/
    private fun setToolbar() {
        toolbarBinding = binding.toolbarMain
        toolbarBinding.apply {
            titleToolbar.setText("Class Management App")
            subtitleToolbar.setText("Class List")
            back.isInvisible = true
            save.isInvisible = true
        }
    }

    private fun gotoStudentFragment(position: Int) {
        val bundle = bundleOf(
            "className" to classItems.get(position).className,
            "subjectName" to classItems.get(position).subjectName,
            "position" to position,
            "cid" to classItems.get(position).cid
        )
        findNavController().navigate(R.id.action_classFragment_to_studentFragment,bundle)
    }

    private fun gotoMapFragment(position: Int) {
        val bundle = bundleOf(
            "className" to classItems.get(position).className,
            "subjectName" to classItems.get(position).subjectName,
            "position" to position,
            "cid" to classItems.get(position).cid
        )
        findNavController().navigate(R.id.action_classFragment_to_mapFragment,bundle)
    }

    /**Hiển thị Dialog Add New Class*/
    private fun showDialog() {
        val dialog = MyDialog()
        dialog.show(parentFragmentManager, MyDialog.CLASS_AND_DIALOG)
        dialog.onItemClassClick = {
            addClass(it.className, it.subjectName)
        }
    }

    private fun addClass(className: String, subjectName: String) {
        val cid = dbHelper.addClass(className,subjectName)
        val classItem = ClassItem(cid,className,subjectName)
        classItems.add(classItem)
        classAdapter.notifyDataSetChanged()
        Toast.makeText(requireContext(),"Add Success",Toast.LENGTH_SHORT).show()
    }

    /**Đặt giá trị mặc định cho danh sách Class*/
    private fun addDefaultClass(className: String, subjectName: String) {
        val cursor = dbHelper.getClassTabale()
        if (cursor.count == 0){
            val cid = dbHelper.addClass(className,subjectName)
            val classItem = ClassItem(cid,className,subjectName)
            classItems.add(classItem)
            classAdapter.notifyDataSetChanged()
            readDataJson(cid)
        }
        cursor.close()
    }

    private fun readDataJson(cid: Long) {
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

    /**Đặt giá trị mặc định cho danh sách Student*/
    private fun addDefaultStudent(defaultId: MutableList<Int>, defaultName: MutableList<String>, cid: Long) {
        val cursor = dbHelper.getStudentTabale(cid!!)
        //studentItems.clear()
        if (cursor.count == 0){
            for (i in 0..defaultId.size-1){
                val roll = defaultId[i]
                val name = defaultName[i]
                Log.i("LOG_DEFAUL", i.toString()+" "+roll.toString()+" "+i.toString()+" "+name)
                val sid = dbHelper.addStudent(cid!!,roll,name)
                dbHelper.addStatus(sid,cid!!,calendar.getDate(),"P")
            }
        }
        cursor.close()
    }

    /**Bấm giữ item để Delete hoặc Update Class*/
    override fun onContextItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            0 -> showUpdateDialog(item.groupId)
            1 -> showComfirmDialog(item.groupId)
        }
        return super.onContextItemSelected(item)
    }

    private fun showUpdateDialog(position: Int) {
        val dialog = MyDialog()
        dialog.show(parentFragmentManager, MyDialog.CLASS_UPDATE_DIALOG)
        dialog.onItemClassClick = {
            updateClass(position,it.className,it.subjectName)
        }
    }

    private fun updateClass(position: Int,className: String, subjectName: String) {
        dbHelper.updateClass(classItems.get(position).cid!!,className,subjectName)
        classItems.get(position).className = className
        classItems.get(position).subjectName = subjectName
        classAdapter.notifyItemChanged(position)
        Toast.makeText(requireContext(),"Update Success",Toast.LENGTH_SHORT).show()
    }

    private fun showComfirmDialog(position: Int) {
        val dialog = MyDialog()
        dialog.show(parentFragmentManager, MyDialog.CONFIRM_DIALOG)
        dialog.confirmClick = {
            if (it){
                deleteClass(position)
            }
        }
    }

    private fun deleteClass(position: Int) {
        dbHelper.deleteClass(classItems.get(position).cid!!)
        classItems.removeAt(position)
        classAdapter.notifyItemRemoved(position)
    }
}