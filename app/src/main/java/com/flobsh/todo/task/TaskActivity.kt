package com.flobsh.todo.task

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.flobsh.todo.R
import com.flobsh.todo.tasklist.Task
import java.util.*

class TaskActivity : Activity() {
    companion object {
        const val ADD_TASK_REQUEST_CODE = 666
        const val TASK_KEY = "NEW_TASK"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.task_editor)

        val titleEditor = findViewById<EditText>(R.id.title_edit)
        val descriptionEditor = findViewById<EditText>(R.id.description_edit)

        val button = findViewById<ImageButton>(R.id.ok_button)
        button.setOnClickListener {
            val newTask = Task(id = UUID.randomUUID().toString(), title = titleEditor.text.toString(), description = descriptionEditor.text.toString())
            intent?.putExtra(TASK_KEY, newTask)
            setResult(ADD_TASK_REQUEST_CODE, intent)
            finish()
        }
    }
}
