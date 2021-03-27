package com.example.demorxproject

import androidx.recyclerview.widget.DiffUtil

class UserDiffUtil(
    private val oldUsers: List<User>,
    private val newUsers: List<User>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldUsers.size

    override fun getNewListSize(): Int = newUsers.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldUsers[oldItemPosition].id == newUsers[newItemPosition].id

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldUsers[oldItemPosition] == newUsers[newItemPosition]
}