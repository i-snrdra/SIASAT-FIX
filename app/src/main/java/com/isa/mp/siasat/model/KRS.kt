package com.isa.mp.siasat.model

data class KRS(
    val id: String = "",
    val mahasiswaId: String = "",
    val kelasId: String = "",
    val status: String = STATUS_PENDING,
    val createdAt: Long = 0,
    val updatedAt: Long = 0
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_APPROVED = "APPROVED"
        const val STATUS_REJECTED = "REJECTED"
    }
} 