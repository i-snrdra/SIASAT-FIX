package com.isa.mp.siasat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.isa.mp.siasat.databinding.ActivityInitDataBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InitDataActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInitDataBinding
    private val db = FirebaseFirestore.getInstance()
    private val statusBuilder = StringBuilder()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInitDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnInitData.setOnClickListener {
            binding.btnInitData.isEnabled = false
            initializeData()
        }
    }

    private fun log(message: String) {
        val timestamp = dateFormat.format(Date())
        statusBuilder.append("[$timestamp] $message\n")
        binding.tvStatus.text = statusBuilder.toString()
    }

    private fun initializeData() {
        // Inisialisasi Kaprogdi
        val kaprogdi = hashMapOf(
            "kode" to "67001",
            "nama" to "Budhi Kristianto, S.Kom., M.Sc., Ph.D",
            "role" to "kaprogdi",
            "password" to "67001",
            "createdAt" to System.currentTimeMillis(),
            "lastLogin" to null
        )

        // Inisialisasi Data Dosen
        val dosenList = listOf(
            hashMapOf(
                "kode" to "67002",
                "nama" to "Prof. Dr. Kristoko D. Hartomo, M.Kom",
                "role" to "dosen",
                "password" to "67002",
                "createdAt" to System.currentTimeMillis(),
                "lastLogin" to null,
                "mengajar" to listOf<String>()
            ),
            hashMapOf(
                "kode" to "67003",
                "nama" to "Dr. Wiwin Sulistyo, S.T., M.Kom",
                "role" to "dosen",
                "password" to "67003",
                "createdAt" to System.currentTimeMillis(),
                "lastLogin" to null,
                "mengajar" to listOf<String>()
            ),
            hashMapOf(
                "kode" to "67004",
                "nama" to "Dr. Irwan Sembiring, S.T., M.Kom",
                "role" to "dosen",
                "password" to "67004",
                "createdAt" to System.currentTimeMillis(),
                "lastLogin" to null,
                "mengajar" to listOf<String>()
            ),
            hashMapOf(
                "kode" to "67005",
                "nama" to "Yessica Nataliani, S.Si., M.Kom., Ph.D",
                "role" to "dosen",
                "password" to "67005",
                "createdAt" to System.currentTimeMillis(),
                "lastLogin" to null,
                "mengajar" to listOf<String>()
            ),
            hashMapOf(
                "kode" to "67006",
                "nama" to "Hanna Prillysca Chernovita, S.Si., M.Cs",
                "role" to "dosen",
                "password" to "67006",
                "createdAt" to System.currentTimeMillis(),
                "lastLogin" to null,
                "mengajar" to listOf<String>()
            )
        )

        // Inisialisasi Data Mahasiswa
        val mahasiswaList = listOf(
            hashMapOf(
                "nim" to "672022708",
                "nama" to "Isa Noorendra",
                "role" to "mahasiswa",
                "password" to "672022708",
                "angkatan" to "2022",
                "createdAt" to System.currentTimeMillis(),
                "lastLogin" to null,
                "mataKuliahDiambil" to listOf<String>()
            ),
            hashMapOf(
                "nim" to "672022134",
                "nama" to "Aghus Fajar",
                "role" to "mahasiswa",
                "password" to "672022134",
                "angkatan" to "2022",
                "createdAt" to System.currentTimeMillis(),
                "lastLogin" to null,
                "mataKuliahDiambil" to listOf<String>()
            ),
            hashMapOf(
                "nim" to "672022177",
                "nama" to "Titus Candra",
                "role" to "mahasiswa",
                "password" to "672022177",
                "angkatan" to "2022",
                "createdAt" to System.currentTimeMillis(),
                "lastLogin" to null,
                "mataKuliahDiambil" to listOf<String>()
            ),
            hashMapOf(
                "nim" to "672022076",
                "nama" to "Ardiva Nugraheni",
                "role" to "mahasiswa",
                "password" to "672022076",
                "angkatan" to "2022",
                "createdAt" to System.currentTimeMillis(),
                "lastLogin" to null,
                "mataKuliahDiambil" to listOf<String>()
            )
        )

        // Inisialisasi Data Mata Kuliah
        val mataKuliahList = listOf(
            hashMapOf(
                "kode" to "TC001",
                "nama" to "Pemrograman Berorientasi Platform",
                "sks" to 3,
                "createdBy" to "67001",
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            ),
            hashMapOf(
                "kode" to "TC002",
                "nama" to "Grafika Komputer",
                "sks" to 3,
                "createdBy" to "67001",
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            ),
            hashMapOf(
                "kode" to "TC003",
                "nama" to "Pemrograman Mobile",
                "sks" to 3,
                "createdBy" to "67001",
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
        )

        // Inisialisasi Data Kelas
        val kelasList = listOf(
            hashMapOf(
                "kode" to "TC001-A",
                "mataKuliahId" to "TC001",
                "dosenId" to "67002",
                "semester" to "2023/2024 Genap",
                "jadwal" to hashMapOf(
                    "hari" to "Senin",
                    "jamMulai" to 7,
                    "jamSelesai" to 10
                ),
                "mahasiswa" to listOf<String>(),
                "kapasitas" to 30,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            ),
            hashMapOf(
                "kode" to "TC001-B",
                "mataKuliahId" to "TC001",
                "dosenId" to "67003",
                "semester" to "2023/2024 Genap",
                "jadwal" to hashMapOf(
                    "hari" to "Selasa",
                    "jamMulai" to 7,
                    "jamSelesai" to 10
                ),
                "mahasiswa" to listOf<String>(),
                "kapasitas" to 30,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            ),
            hashMapOf(
                "kode" to "TC002-A",
                "mataKuliahId" to "TC002",
                "dosenId" to "67004",
                "semester" to "2023/2024 Genap",
                "jadwal" to hashMapOf(
                    "hari" to "Rabu",
                    "jamMulai" to 7,
                    "jamSelesai" to 10
                ),
                "mahasiswa" to listOf<String>(),
                "kapasitas" to 30,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            ),
            hashMapOf(
                "kode" to "TC002-B",
                "mataKuliahId" to "TC002",
                "dosenId" to "67005",
                "semester" to "2023/2024 Genap",
                "jadwal" to hashMapOf(
                    "hari" to "Kamis",
                    "jamMulai" to 7,
                    "jamSelesai" to 10
                ),
                "mahasiswa" to listOf<String>(),
                "kapasitas" to 30,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            ),
            hashMapOf(
                "kode" to "TC003-A",
                "mataKuliahId" to "TC003",
                "dosenId" to "67006",
                "semester" to "2023/2024 Genap",
                "jadwal" to hashMapOf(
                    "hari" to "Jumat",
                    "jamMulai" to 7,
                    "jamSelesai" to 10
                ),
                "mahasiswa" to listOf<String>(),
                "kapasitas" to 30,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            ),
            hashMapOf(
                "kode" to "TC003-B",
                "mataKuliahId" to "TC003",
                "dosenId" to "67002",
                "semester" to "2023/2024 Genap",
                "jadwal" to hashMapOf(
                    "hari" to "Sabtu",
                    "jamMulai" to 7,
                    "jamSelesai" to 10
                ),
                "mahasiswa" to listOf<String>(),
                "kapasitas" to 30,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
        )

        // Mulai inisialisasi data ke Firestore
        log("Memulai inisialisasi data...")

        // Inisialisasi Kaprogdi
        db.collection("users").document(kaprogdi["kode"] as String)
            .set(kaprogdi)
            .addOnSuccessListener {
                log("✓ Berhasil menambahkan data Kaprogdi")
                
                // Inisialisasi Dosen
                var dosenSuccess = 0
                dosenList.forEach { dosen ->
                    db.collection("users").document(dosen["kode"] as String)
                        .set(dosen)
                        .addOnSuccessListener {
                            dosenSuccess++
                            log("✓ Berhasil menambahkan data Dosen: ${dosen["nama"]}")
                            if (dosenSuccess == dosenList.size) {
                                log("✓ Semua data Dosen berhasil ditambahkan")
                            }
                        }
                        .addOnFailureListener { e ->
                            log("✗ Gagal menambahkan data Dosen ${dosen["nama"]}: ${e.message}")
                        }
                }

                // Inisialisasi Mahasiswa
                var mahasiswaSuccess = 0
                mahasiswaList.forEach { mahasiswa ->
                    db.collection("users").document(mahasiswa["nim"] as String)
                        .set(mahasiswa)
                        .addOnSuccessListener {
                            mahasiswaSuccess++
                            log("✓ Berhasil menambahkan data Mahasiswa: ${mahasiswa["nama"]}")
                            if (mahasiswaSuccess == mahasiswaList.size) {
                                log("✓ Semua data Mahasiswa berhasil ditambahkan")
                            }
                        }
                        .addOnFailureListener { e ->
                            log("✗ Gagal menambahkan data Mahasiswa ${mahasiswa["nama"]}: ${e.message}")
                        }
                }

                // Inisialisasi Mata Kuliah
                var mataKuliahSuccess = 0
                mataKuliahList.forEach { mataKuliah ->
                    db.collection("mataKuliah").document(mataKuliah["kode"] as String)
                        .set(mataKuliah)
                        .addOnSuccessListener {
                            mataKuliahSuccess++
                            log("✓ Berhasil menambahkan data Mata Kuliah: ${mataKuliah["nama"]}")
                            if (mataKuliahSuccess == mataKuliahList.size) {
                                log("✓ Semua data Mata Kuliah berhasil ditambahkan")
                            }
                        }
                        .addOnFailureListener { e ->
                            log("✗ Gagal menambahkan data Mata Kuliah ${mataKuliah["nama"]}: ${e.message}")
                        }
                }

                // Inisialisasi Kelas
                var kelasSuccess = 0
                kelasList.forEach { kelas ->
                    db.collection("kelas").document(kelas["kode"] as String)
                        .set(kelas)
                        .addOnSuccessListener {
                            kelasSuccess++
                            log("✓ Berhasil menambahkan data Kelas: ${kelas["kode"]}")
                            if (kelasSuccess == kelasList.size) {
                                log("✓ Semua data Kelas berhasil ditambahkan")
                                binding.btnInitData.isEnabled = true
                                log("Selesai inisialisasi data!")
                            }
                        }
                        .addOnFailureListener { e ->
                            log("✗ Gagal menambahkan data Kelas ${kelas["kode"]}: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                log("✗ Gagal menambahkan data Kaprogdi: ${e.message}")
                binding.btnInitData.isEnabled = true
            }
    }
} 