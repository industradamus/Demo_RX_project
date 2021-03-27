package com.example.demorxproject

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.demorxproject.databinding.ListItemUserBinding

class UsersAdapter : RecyclerView.Adapter<UsersAdapter.ViewHolder>() {

    private lateinit var binding: ListItemUserBinding

    private val items: MutableList<User> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        binding = ListItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun update(users: List<User>) {
        val result = DiffUtil.calculateDiff(UserDiffUtil(items, users))

        items.clear()
        items.addAll(users)
        result.dispatchUpdatesTo(this)
    }


    class ViewHolder(private val binding: ListItemUserBinding) : RecyclerView.ViewHolder(binding.root) {


        fun bind(user: User) {
            with(binding) {
                name.text = user.name
                surname.text = user.surname
                isAdult.setBackgroundColor(if (user.isAdult) Color.GREEN else Color.RED)
                info.text = "Add Data = ${user.additionalData}"
                queue.text = "Number = ${user.queue}"
            }
        }
    }
}
