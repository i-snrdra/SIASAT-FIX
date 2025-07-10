package com.isa.mp.siasat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.isa.mp.siasat.R
import com.isa.mp.siasat.model.Kelas
import com.isa.mp.siasat.model.MataKuliah

class JadwalDosenAdapter : RecyclerView.Adapter<JadwalDosenAdapter.ViewHolder>() {
    private val jamList = (7..17).toList() // 07:00 - 17:00
    private val hariList = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")
    private val jadwalMap = mutableMapOf<String, MutableMap<Int, KelasWithDetails>>()

    fun setJadwal(jadwalList: List<KelasWithDetails>) {
        // Reset map
        jadwalMap.clear()
        hariList.forEach { hari ->
            jadwalMap[hari] = mutableMapOf()
        }

        // Populate map
        jadwalList.forEach { kelasWithDetails ->
            val hari = kelasWithDetails.kelas.jadwal.hari
            val jamMulai = kelasWithDetails.kelas.jadwal.jamMulai
            jadwalMap[hari]?.put(jamMulai, kelasWithDetails)
        }

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_jadwal_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val jam = jamList[position]
        holder.bind(jam)
    }

    override fun getItemCount(): Int = jamList.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvJam: TextView = itemView.findViewById(R.id.tvJam)
        private val cells: List<MaterialCardView> = listOf(
            itemView.findViewById(R.id.cell1),
            itemView.findViewById(R.id.cell2),
            itemView.findViewById(R.id.cell3),
            itemView.findViewById(R.id.cell4),
            itemView.findViewById(R.id.cell5),
            itemView.findViewById(R.id.cell6)
        )
        private val cellTexts: List<TextView> = listOf(
            itemView.findViewById(R.id.tvCell1),
            itemView.findViewById(R.id.tvCell2),
            itemView.findViewById(R.id.tvCell3),
            itemView.findViewById(R.id.tvCell4),
            itemView.findViewById(R.id.tvCell5),
            itemView.findViewById(R.id.tvCell6)
        )

        fun bind(jam: Int) {
            tvJam.text = String.format("%02d:00", jam)

            // Reset cells
            cells.forEach { it.visibility = View.INVISIBLE }
            cellTexts.forEach { it.text = "" }

            // Fill cells
            hariList.forEachIndexed { index, hari ->
                jadwalMap[hari]?.get(jam)?.let { kelasWithDetails ->
                    cells[index].visibility = View.VISIBLE
                    cellTexts[index].text = "${kelasWithDetails.mataKuliah.kode}\n" +
                            "(${kelasWithDetails.kelas.jadwal.jamMulai}:00-" +
                            "${kelasWithDetails.kelas.jadwal.jamSelesai}:00)"

                    // Set height based on duration
                    val duration = kelasWithDetails.kelas.jadwal.jamSelesai - kelasWithDetails.kelas.jadwal.jamMulai
                    val params = cells[index].layoutParams
                    params.height = 40 * duration // 40dp per jam
                    cells[index].layoutParams = params
                }
            }
        }
    }
} 