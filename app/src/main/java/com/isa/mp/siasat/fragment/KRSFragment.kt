package com.isa.mp.siasat.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.isa.mp.siasat.adapter.KelasAvailableAdapter
import com.isa.mp.siasat.adapter.KelasWithDetails
import com.isa.mp.siasat.databinding.FragmentKelasBinding
import com.isa.mp.siasat.model.KRS
import com.isa.mp.siasat.model.Kelas
import com.isa.mp.siasat.model.MataKuliah

class KRSFragment : Fragment() {
    private var _binding: FragmentKelasBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: KelasAvailableAdapter
    private val mataKuliahList = mutableListOf<MataKuliah>()
    private var mahasiswaId: String = ""
    private var mahasiswaNama: String = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKelasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get mahasiswa ID from shared preferences
        val sharedPref = requireActivity().getSharedPreferences("auth", 0)
        mahasiswaId = sharedPref.getString("userId", "") ?: ""
        mahasiswaNama = sharedPref.getString("nama", "") ?: ""

        setupRecyclerView()
        loadData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        adapter = KelasAvailableAdapter(
            onAmbilClick = { kelas ->
                showKonfirmasiDialog(kelas)
            }
        )
        binding.rvKelas.adapter = adapter
        binding.rvKelas.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
    }

    private fun loadData() {
        showLoading(true)

        // Load mata kuliah
        db.collection("mataKuliah")
            .orderBy("kode", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                mataKuliahList.clear()
                mataKuliahList.addAll(
                    documents.mapNotNull { doc ->
                        doc.toObject(MataKuliah::class.java).copy(id = doc.id)
                    }
                )

                // Load kelas yang tersedia
                loadKelas()
            }
            .addOnFailureListener { e ->
                showError("Gagal memuat data mata kuliah: ${e.message}")
                showLoading(false)
            }
    }

    private fun loadKelas() {
        // Get kelas yang sudah diambil
        db.collection("krs")
            .whereEqualTo("mahasiswaId", mahasiswaId)
            .get()
            .addOnSuccessListener { krsDocuments ->
                val kelasIdsAmbil = krsDocuments.documents.mapNotNull { doc ->
                    doc.toObject(KRS::class.java)?.kelasId
                }

                // Load semua kelas yang belum diambil
                db.collection("kelas")
                    .whereNotIn("id", if (kelasIdsAmbil.isEmpty()) listOf("") else kelasIdsAmbil)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            showError("Gagal memuat data: ${e.message}")
                            return@addSnapshotListener
                        }

                        val kelasList = snapshot?.documents?.mapNotNull { doc ->
                            val kelas = doc.toObject(Kelas::class.java)?.copy(id = doc.id)
                                ?: return@mapNotNull null
                            val mataKuliah = mataKuliahList.find { it.id == kelas.mataKuliahId }
                                ?: return@mapNotNull null

                            // Get nama dosen
                            db.collection("users")
                                .document(kelas.dosenId)
                                .get()
                                .addOnSuccessListener { dosenDoc ->
                                    val dosenNama = dosenDoc.getString("nama") ?: "Unknown"
                                    adapter.submitList(listOf(KelasWithDetails(kelas, mataKuliah, dosenNama)))
                                }

                            KelasWithDetails(kelas, mataKuliah, "Loading...")
                        } ?: emptyList()

                        adapter.submitList(kelasList)
                        showEmpty(kelasList.isEmpty())
                        showLoading(false)
                    }
            }
            .addOnFailureListener { e ->
                showError("Gagal memuat data KRS: ${e.message}")
                showLoading(false)
            }
    }

    private fun showKonfirmasiDialog(kelasWithDetails: KelasWithDetails) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Konfirmasi KRS")
            .setMessage(
                """
                Apakah Anda yakin ingin mengambil mata kuliah ini?
                
                Kode: ${kelasWithDetails.mataKuliah.kode}
                Nama: ${kelasWithDetails.mataKuliah.nama}
                SKS: ${kelasWithDetails.mataKuliah.sks}
                Dosen: ${kelasWithDetails.dosenNama}
                Jadwal: ${kelasWithDetails.kelas.jadwal}
                """.trimIndent()
            )
            .setPositiveButton("Ya") { _, _ ->
                ambilKelas(kelasWithDetails.kelas)
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun ambilKelas(kelas: Kelas) {
        // Check kapasitas
        if (kelas.mahasiswa.size >= kelas.kapasitas) {
            showError("Maaf, kelas sudah penuh")
            return
        }

        // Check bentrok jadwal
        db.collection("krs")
            .whereEqualTo("mahasiswaId", mahasiswaId)
            .get()
            .addOnSuccessListener { krsDocuments ->
                val kelasIds = krsDocuments.documents.mapNotNull { doc ->
                    doc.toObject(KRS::class.java)?.kelasId
                }

                if (kelasIds.isEmpty()) {
                    saveKRS(kelas)
                    return@addOnSuccessListener
                }

                // Get jadwal kelas yang sudah diambil
                db.collection("kelas")
                    .whereIn("id", kelasIds)
                    .get()
                    .addOnSuccessListener { kelasDocuments ->
                        val jadwalList = kelasDocuments.documents.mapNotNull { doc ->
                            doc.toObject(Kelas::class.java)?.jadwal
                        }

                        // Check bentrok
                        if (jadwalList.any { it == kelas.jadwal }) {
                            showError("Maaf, jadwal bentrok dengan kelas lain")
                            return@addOnSuccessListener
                        }

                        saveKRS(kelas)
                    }
            }
            .addOnFailureListener { e ->
                showError("Gagal mengecek jadwal: ${e.message}")
            }
    }

    private fun saveKRS(kelas: Kelas) {
        val now = System.currentTimeMillis()
        val krs = KRS(
            mahasiswaId = mahasiswaId,
            kelasId = kelas.id,
            status = KRS.STATUS_PENDING,
            createdAt = now,
            updatedAt = now
        )

        db.collection("krs")
            .add(krs)
            .addOnSuccessListener {
                // Update array mahasiswa di kelas
                db.collection("kelas")
                    .document(kelas.id)
                    .update("mahasiswa", kelas.mahasiswa + mahasiswaId)
                    .addOnSuccessListener {
                        Toast.makeText(
                            requireContext(),
                            "Berhasil mengambil kelas",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        showError("Gagal mengupdate kelas: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                showError("Gagal menyimpan KRS: ${e.message}")
            }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showEmpty(isEmpty: Boolean) {
        binding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
} 