package com.isa.mp.siasat.model

data class Absensi(
    val id: String = "",
    val kelasId: String = "",
    val pertemuan: Int = 0,
    val topik: String = "",
    val tanggal: Long = 0,
    val status: String = STATUS_TERTUTUP,
    val dibukaPada: Long = 0,
    val ditutupPada: Long = 0,
    val mahasiswaHadir: List<String> = emptyList()
) {
    companion object {
        const val STATUS_TERBUKA = "TERBUKA"
        const val STATUS_TERTUTUP = "TERTUTUP"
    }
} 