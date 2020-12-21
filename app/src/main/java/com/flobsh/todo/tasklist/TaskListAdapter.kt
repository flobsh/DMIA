package com.flobsh.todo.tasklist

import android.content.Intent
import android.database.DataSetObserver
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ListAdapter
import com.flobsh.todo.R
import com.flobsh.todo.task.TaskActivity

object TaskListDiffCallback : DiffUtil.ItemCallback<Task>() {
    override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
        return (areItemsTheSame(oldItem, newItem) && oldItem.title == newItem.title && oldItem.description == newItem.description)
    }
}

class TaskListAdapter() : ListAdapter<Task, TaskListAdapter.TaskViewHolder>(TaskListDiffCallback) {
    var onDeleteClickListener: ((Task) -> Unit)? = null
    var onEditClickListener: ((Task) -> Unit)? = null

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(task: Task) {
            itemView.apply {
                var textView = itemView.findViewById<TextView>(R.id.task_title)
                textView.text = task.title

                var descriptionView = itemView.findViewById<TextView>(R.id.task_description)
                descriptionView.text = task.description

                var editButton = itemView.findViewById<ImageButton>(R.id.edit_button)
                editButton.setOnClickListener { onEditClickListener?.invoke(task) }

                var deleteButton = itemView.findViewById<ImageButton>(R.id.delete_button)
                deleteButton.setOnClickListener { onDeleteClickListener?.invoke(task) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(currentList[position])
    }
}