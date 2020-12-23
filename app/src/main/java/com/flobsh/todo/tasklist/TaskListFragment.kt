package com.flobsh.todo.tasklist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import androidx.work.WorkManager
import coil.load
import com.flobsh.todo.MainActivity
import com.flobsh.todo.R
import com.flobsh.todo.authentication.AuthenticationActivity
import com.flobsh.todo.authentication.SHARED_PREF_TOKEN_KEY
import com.flobsh.todo.network.Api
import com.flobsh.todo.network.TasksRepository
import com.flobsh.todo.task.TaskActivity
import com.flobsh.todo.task.TaskActivity.Companion.ADD_TASK_REQUEST_CODE
import com.flobsh.todo.task.TaskActivity.Companion.EDIT_TASK_REQUEST_CODE
import com.flobsh.todo.userinfo.UserInfoActivity
import com.flobsh.todo.userinfo.UserInfoViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class TaskListFragment : Fragment() {
    private val adapter = TaskListAdapter()
    private val viewModel: TaskListViewModel by viewModels()
    private val userViewModel: UserInfoViewModel by viewModels()

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
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter

        viewModel.taskList.observe(viewLifecycleOwner) { newList ->
            adapter.submitList(newList)
            adapter.notifyDataSetChanged()
        }

        userViewModel.userInfo.observe(viewLifecycleOwner) { userInfo ->
            userView.text = "${userInfo.firstName} ${userInfo.lastName}"
            userAvatar.load(userInfo.avatar)
        }

        adapter.onEditClickListener = { task ->
            val intent = Intent(activity, TaskActivity::class.java)
            intent.putExtra("TASK_TO_EDIT", task)
            startActivityForResult(intent, EDIT_TASK_REQUEST_CODE)
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

        val signOutButton = view.findViewById<ImageButton>(R.id.sign_out_button)
        signOutButton.setOnClickListener {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putString(SHARED_PREF_TOKEN_KEY, "")
            editor.apply()

            val intent = Intent(activity, AuthenticationActivity::class.java)
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
        else if (requestCode == EDIT_TASK_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val task = data!!.getSerializableExtra(TaskActivity.TASK_KEY) as Task
            viewModel.editTask(task)
        }
        else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onResume() {
        super.onResume()
        userViewModel.loadUserInfo()
        viewModel.loadTasks()
    }
}