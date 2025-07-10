package com.isa.mp.siasat.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.firestore.FirebaseFirestore
import com.isa.mp.siasat.adapter.KelasAdapter
import com.isa.mp.siasat.adapter.KelasWithDetails
import com.isa.mp.siasat.databinding.FragmentKelasBinding
import com.isa.mp.siasat.model.KRS
import com.isa.mp.siasat.model.Kelas
import com.isa.mp.siasat.model.MataKuliah
import com.isa.mp.siasat.model.Nilai

class NilaiMahasiswaFragment : Fragment() {
    private var _binding: FragmentKelasBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: KelasAdapter
    private val mataKuliahList = mutableListOf<MataKuliah>()
    private var mahasiswaId: String = ""

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

        setupRecyclerView()
        loadData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        adapter = KelasAdapter(
            onEditClick = { /* Mahasiswa tidak bisa edit kelas */ },
            onDeleteClick = { /* Mahasiswa tidak bisa hapus kelas */ }
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
            .get()
            .addOnSuccessListener { documents ->
                mataKuliahList.clear()
                mataKuliahList.addAll(
                    documents.mapNotNull { doc ->
                        doc.toObject(MataKuliah::class.java).copy(id = doc.id)
                    }
                )

                // Load KRS yang diambil
                loadKRS()
            }
            .addOnFailureListener { e ->
                showError("Gagal memuat data mata kuliah: ${e.message}")
                showLoading(false)
            }
    }

    private fun loadKRS() {
        db.collection("krs")
            .whereEqualTo("mahasiswaId", mahasiswaId)
            .whereEqualTo("status", KRS.STATUS_APPROVED)
            .get()
            .addOnSuccessListener { krsDocuments ->
                val kelasIds = krsDocuments.documents.mapNotNull { doc ->
                    doc.toObject(KRS::class.java)?.kelasId
                }

                if (kelasIds.isEmpty()) {
                    showEmpty(true)
                    showLoading(false)
                    return@addOnSuccessListener
                }

                // Load kelas yang diambil
                db.collection("kelas")
                    .whereIn("id", kelasIds)
                    .get()
                    .addOnSuccessListener { kelasDocuments ->
                        val kelasList = kelasDocuments.documents.mapNotNull { doc ->
                            val kelas = doc.toObject(Kelas::class.java)?.copy(id = doc.id)
                                ?: return@mapNotNull null
                            val mataKuliah = mataKuliahList.find { it.id == kelas.mataKuliahId }
                                ?: return@mapNotNull null

                            // Get nilai
                            db.collection("nilai")
                                .whereEqualTo("kelasId", kelas.id)
                                .whereEqualTo("mahasiswaId", mahasiswaId)
                                .get()
                                .addOnSuccessListener { nilaiDocuments ->
                                    val nilai = nilaiDocuments.documents.firstOrNull()?.let {
                                        it.toObject(Nilai::class.java)?.nilai
                                    } ?: "-"

                                    // Get nama dosen
                                    db.collection("users")
                                        .document(kelas.dosenId)
                                        .get()
                                        .addOnSuccessListener { dosenDoc ->
                                            val dosenNama = dosenDoc.getString("nama") ?: "Unknown"
                                            adapter.submitList(
                                                listOf(
                                                    KelasWithDetails(
                                                        kelas.copy(
                                                            nama = "${mataKuliah.nama} (Nilai: $nilai)"
                                                        ),
                                                        mataKuliah,
                                                        dosenNama
                                                    )
                                                )
                                            )
                                        }
                                }

                            KelasWithDetails(kelas, mataKuliah, "Loading...")
                        }

                        adapter.submitList(kelasList)
                        showEmpty(kelasList.isEmpty())
                        showLoading(false)
                    }
                    .addOnFailureListener { e ->
                        showError("Gagal memuat data kelas: ${e.message}")
                        showLoading(false)
                    }
            }
            .addOnFailureListener { e ->
                showError("Gagal memuat data KRS: ${e.message}")
                showLoading(false)
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