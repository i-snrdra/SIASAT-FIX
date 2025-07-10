package com.isa.mp.siasat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.isa.mp.siasat.R
import com.isa.mp.siasat.databinding.ItemMataKuliahBinding
import com.isa.mp.siasat.model.MataKuliah

class MataKuliahAdapter(
    private val onEditClick: (MataKuliah) -> Unit,
    private val onDeleteClick: (MataKuliah) -> Unit
) : ListAdapter<MataKuliah, MataKuliahAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMataKuliahBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemMataKuliahBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(mataKuliah: MataKuliah) {
            binding.apply {
                tvKode.text = mataKuliah.kode
                tvNama.text = mataKuliah.nama
                tvSks.text = "${mataKuliah.sks} SKS"

                btnMore.setOnClickListener { view ->
                    PopupMenu(view.context, view).apply {
                        menuInflater.inflate(R.menu.menu_item_mata_kuliah, menu)
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.menu_edit -> {
                                    onEditClick(mataKuliah)
                                    true
                                }
                                R.id.menu_delete -> {
                                    onDeleteClick(mataKuliah)
                                    true
                                }
                                else -> false
                            }
                        }
                        show()
                    }
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MataKuliah>() {
            override fun areItemsTheSame(oldItem: MataKuliah, newItem: MataKuliah): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: MataKuliah, newItem: MataKuliah): Boolean {
                return oldItem == newItem
            }
        }
    }
} 