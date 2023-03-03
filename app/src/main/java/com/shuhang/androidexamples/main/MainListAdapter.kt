package com.shuhang.androidexamples.main

import android.content.Context
import android.view.ViewGroup
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.DataBindingHolder
import com.shuhang.androidexamples.R
import com.shuhang.androidexamples.databinding.ItemMainListBinding

class MainListAdapter: BaseQuickAdapter<MainListBean, DataBindingHolder<ItemMainListBinding>>() {
    override fun onBindViewHolder(
        holder: DataBindingHolder<ItemMainListBinding>,
        position: Int,
        item: MainListBean?
    ) {
        holder.binding.bean = item
        holder.binding.executePendingBindings()
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): DataBindingHolder<ItemMainListBinding> {
        return DataBindingHolder(R.layout.item_main_list, parent)
    }
}