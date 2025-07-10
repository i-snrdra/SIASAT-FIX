package com.isa.mp.siasat.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.isa.mp.siasat.adapter.KelasAdapter
import com.isa.mp.siasat.adapter.KelasWithDetails
import com.isa.mp.siasat.databinding.FragmentKelasBinding
import com.isa.mp.siasat.model.Kelas
import com.isa.mp.siasat.model.MataKuliah

class MengajarFragment : Fragment() {
    private var _binding: FragmentKelasBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: KelasAdapter
    private val mataKuliahList = mutableListOf<MataKuliah>()
    private var dosenId: String = ""
    private var dosenNama: String = ""

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

        // Get dosen ID from shared preferences
        val sharedPref = requireActivity().getSharedPreferences("auth", 0)
        dosenId = sharedPref.getString("userId", "") ?: ""
        dosenNama = sharedPref.getString("nama", "") ?: ""

        setupRecyclerView()
        loadData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        adapter = KelasAdapter(
            onEditClick = { /* Dosen tidak bisa edit kelas */ },
            onDeleteClick = { /* Dosen tidak bisa hapus kelas */ }
        )
        binding.rvKelas.adapter = adapter
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

                // Load kelas yang diajar
                loadKelas()
            }
            .addOnFailureListener { e ->
                showError("Gagal memuat data mata kuliah: ${e.message}")
                showLoading(false)
            }
    }

    private fun loadKelas() {
        db.collection("kelas")
            .whereEqualTo("dosenId", dosenId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    showError("Gagal memuat data: ${e.message}")
                    return@addSnapshotListener
                }

                val kelasList = snapshot?.documents?.mapNotNull { doc ->
                    val kelas = doc.toObject(Kelas::class.java)?.copy(id = doc.id) ?: return@mapNotNull null
                    val mataKuliah = mataKuliahList.find { it.id == kelas.mataKuliahId }
                        ?: return@mapNotNull null

                    KelasWithDetails(kelas, mataKuliah, dosenNama)
                } ?: emptyList()

                adapter.submitList(kelasList)
                showEmpty(kelasList.isEmpty())
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