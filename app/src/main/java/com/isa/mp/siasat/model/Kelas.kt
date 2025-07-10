package com.isa.mp.siasat.model

data class Kelas(
    val id: String = "",
    val mataKuliahId: String = "",
    val dosenId: String = "",
    val semester: String = "", // Format: 20231 (2023 Ganjil), 20232 (2023 Genap)
    val jadwal: Jadwal = Jadwal(),
    val kapasitas: Int = 30,
    val mahasiswa: List<String> = emptyList(), // List of NIM
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)

data class Jadwal(
    val hari: String = "", // Senin-Sabtu
    val jamMulai: Int = 0, // Format 24 jam: 7-17
    val jamSelesai: Int = 0 // Format 24 jam: 7-17
) 