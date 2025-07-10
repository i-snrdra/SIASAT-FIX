package com.isa.mp.siasat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.isa.mp.siasat.databinding.ItemKelasAvailableBinding
import com.isa.mp.siasat.model.Kelas
import com.isa.mp.siasat.model.MataKuliah

class KelasAvailableAdapter(
    private val onAmbilClick: (KelasWithDetails) -> Unit
) : ListAdapter<KelasWithDetails, KelasAvailableAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemKelasAvailableBinding.inflate(
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
        private val binding: ItemKelasAvailableBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: KelasWithDetails) {
            binding.apply {
                tvKode.text = item.mataKuliah.kode
                tvNama.text = item.mataKuliah.nama
                tvSks.text = "SKS: ${item.mataKuliah.sks}"
                tvDosen.text = "Dosen: ${item.dosenNama}"
                tvJadwal.text = "Jadwal: ${item.kelas.jadwal}"
                tvKapasitas.text = "Kapasitas: ${item.kelas.mahasiswa.size}/${item.kelas.kapasitas}"

                // Disable tombol jika kelas penuh
                btnAmbil.isEnabled = item.kelas.mahasiswa.size < item.kelas.kapasitas
                btnAmbil.text = if (btnAmbil.isEnabled) "Ambil" else "Penuh"

                btnAmbil.setOnClickListener {
                    onAmbilClick(item)
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<KelasWithDetails>() {
            override fun areItemsTheSame(
                oldItem: KelasWithDetails,
                newItem: KelasWithDetails
            ): Boolean {
                return oldItem.kelas.id == newItem.kelas.id
            }

            override fun areContentsTheSame(
                oldItem: KelasWithDetails,
                newItem: KelasWithDetails
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
} 