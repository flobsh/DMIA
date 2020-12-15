package com.flobsh.todo.tasklist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.flobsh.todo.R
import com.flobsh.todo.network.Api
import com.flobsh.todo.network.TasksRepository
import com.flobsh.todo.task.TaskActivity
import com.flobsh.todo.task.TaskActivity.Companion.ADD_TASK_REQUEST_CODE
import com.flobsh.todo.userinfo.UserInfoActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class TaskListFragment : Fragment() {
    private val adapter = TaskListAdapter()
    private val viewModel: TaskListViewModel by viewModels()
    lateinit var userView: TextView
    lateinit var userAvatar: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_task_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            val response = Api.userService.getInfo()
            Log.e("Created", response.body()?.email ?: "none")
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter

        viewModel.taskList.observe(viewLifecycleOwner) { newList ->
            adapter.submitList(newList)
            adapter.notifyDataSetChanged()
        }

        adapter.onDeleteClickListener = {
            task -> viewModel.deleteTask(task)
        }

        userView = view.findViewById<TextView>(R.id.user_info)
        userAvatar = view.findViewById(R.id.user_avatar)
        userAvatar.setOnClickListener {
            val intent = Intent(activity, UserInfoActivity::class.java)
            startActivity(intent)
        }

        val button = view.findViewById<FloatingActionButton>(R.id.floatingActionButton)
        button.setOnClickListener() {
            val intent = Intent(activity, TaskActivity::class.java)
            startActivityForResult(intent, ADD_TASK_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ADD_TASK_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val task = data!!.getSerializableExtra(TaskActivity.TASK_KEY) as Task
            viewModel.addTask(task)
        }
        else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val userInfo = Api.userService.getInfo().body()!!
            userView.text = "${userInfo.firstName} ${userInfo.lastName}"
            userAvatar.load(userInfo.avatar)
        }
        viewModel.loadTasks()
    }
}