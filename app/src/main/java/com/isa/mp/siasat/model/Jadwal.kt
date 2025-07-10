package com.isa.mp.siasat.model

data class Jadwal(
    val hari: String = "",
    val jamMulai: Int = 0,
    val jamSelesai: Int = 0
) : Comparable<Jadwal> {
    companion object {
        private val HARI_ORDER = mapOf(
            "Senin" to 1,
            "Selasa" to 2,
            "Rabu" to 3,
            "Kamis" to 4,
            "Jumat" to 5,
            "Sabtu" to 6,
            "Minggu" to 7
        )
    }

    override fun compareTo(other: Jadwal): Int {
        val hariCompare = (HARI_ORDER[this.hari] ?: 0).compareTo(HARI_ORDER[other.hari] ?: 0)
        if (hariCompare != 0) return hariCompare
        return this.jamMulai.compareTo(other.jamMulai)
    }
} 