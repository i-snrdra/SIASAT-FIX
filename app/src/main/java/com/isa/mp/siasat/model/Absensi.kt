package com.isa.mp.siasat.model

data class Absensi(
    val id: String = "",
    val kelasId: String = "",
    val mahasiswaId: String = "",
    val hadir: Int = 0,
    val izin: Int = 0,
    val alfa: Int = 0,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) 