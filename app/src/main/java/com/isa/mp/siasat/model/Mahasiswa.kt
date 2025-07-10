package com.isa.mp.siasat.model

data class Mahasiswa(
    val id: String = "", // NIM
    val nama: String = "",
    val email: String = "",
    val angkatan: String = "",
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) 