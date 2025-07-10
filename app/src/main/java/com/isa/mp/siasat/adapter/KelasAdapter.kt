package com.isa.mp.siasat.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.isa.mp.siasat.R
import com.isa.mp.siasat.databinding.ItemKelasBinding
import com.isa.mp.siasat.model.Kelas
import com.isa.mp.siasat.model.MataKuliah

class KelasAdapter(
    private val onEditClick: (Kelas) -> Unit,
    private val onDeleteClick: (Kelas) -> Unit
) : ListAdapter<KelasWithDetails, KelasAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemKelasBinding.inflate(
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
        private val binding: ItemKelasBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(kelasWithDetails: KelasWithDetails) {
            binding.apply {
                tvMataKuliah.text = "${kelasWithDetails.mataKuliah.nama} (${kelasWithDetails.mataKuliah.kode})"
                tvDosen.text = "Dosen: ${kelasWithDetails.dosenNama}"
                tvJadwal.text = "${kelasWithDetails.kelas.jadwal.hari}, " +
                        String.format(
                            "%02d:00-%02d:00",
                            kelasWithDetails.kelas.jadwal.jamMulai,
                            kelasWithDetails.kelas.jadwal.jamSelesai
                        )
                tvKapasitas.text = "Kapasitas: ${kelasWithDetails.kelas.mahasiswa.size}/${kelasWithDetails.kelas.kapasitas}"
                chipSemester.text = formatSemester(kelasWithDetails.kelas.semester)

                btnMore.setOnClickListener { view ->
                    PopupMenu(view.context, view).apply {
                        menuInflater.inflate(R.menu.menu_item_kelas, menu)
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.menu_edit -> {
                                    onEditClick(kelasWithDetails.kelas)
                                    true
                                }
                                R.id.menu_delete -> {
                                    onDeleteClick(kelasWithDetails.kelas)
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

        private fun formatSemester(semester: String): String {
            val tahun = semester.substring(0, 4)
            val jenis = when (semester.last()) {
                '1' -> "Ganjil"
                '2' -> "Genap"
                else -> ""
            }
            return "$tahun $jenis"
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<KelasWithDetails>() {
            override fun areItemsTheSame(oldItem: KelasWithDetails, newItem: KelasWithDetails): Boolean {
                return oldItem.kelas.id == newItem.kelas.id
            }

            override fun areContentsTheSame(oldItem: KelasWithDetails, newItem: KelasWithDetails): Boolean {
                return oldItem == newItem
            }
        }
    }
}

data class KelasWithDetails(
    val kelas: Kelas,
    val mataKuliah: MataKuliah,
    val dosenNama: String
) 