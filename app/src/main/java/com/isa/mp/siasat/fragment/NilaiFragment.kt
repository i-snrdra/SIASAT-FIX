package com.isa.mp.siasat.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.isa.mp.siasat.adapter.KelasAdapter
import com.isa.mp.siasat.adapter.KelasWithDetails
import com.isa.mp.siasat.databinding.DialogNilaiBinding
import com.isa.mp.siasat.databinding.FragmentKelasBinding
import com.isa.mp.siasat.model.Kelas
import com.isa.mp.siasat.model.MataKuliah
import com.isa.mp.siasat.model.Nilai

class NilaiFragment : Fragment() {
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
            onEditClick = { kelas ->
                showNilaiDialog(kelas)
            },
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

    private fun showNilaiDialog(kelas: Kelas) {
        val dialogBinding = DialogNilaiBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        // Setup mahasiswa spinner
        val mahasiswaList = kelas.mahasiswa.toMutableList()
        val mahasiswaAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            mahasiswaList
        )
        (dialogBinding.spinnerMahasiswa as? AutoCompleteTextView)?.setAdapter(mahasiswaAdapter)

        // Setup nilai spinner
        val nilaiList = listOf("A", "B", "C", "D", "E")
        val nilaiAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            nilaiList
        )
        (dialogBinding.spinnerNilai as? AutoCompleteTextView)?.setAdapter(nilaiAdapter)

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSave.setOnClickListener {
            val mahasiswaId = dialogBinding.spinnerMahasiswa.text.toString()
            val nilai = dialogBinding.spinnerNilai.text.toString()

            if (validateInput(mahasiswaId, nilai, dialogBinding)) {
                saveNilai(
                    kelas.id,
                    mahasiswaId,
                    nilai,
                    dialog
                )
            }
        }

        dialog.show()
    }

    private fun validateInput(
        mahasiswaId: String,
        nilai: String,
        dialogBinding: DialogNilaiBinding
    ): Boolean {
        if (mahasiswaId.isEmpty()) {
            showDialogError("Pilih mahasiswa", dialogBinding)
            return false
        }

        if (nilai.isEmpty()) {
            showDialogError("Pilih nilai", dialogBinding)
            return false
        }

        return true
    }

    private fun saveNilai(
        kelasId: String,
        mahasiswaId: String,
        nilai: String,
        dialog: AlertDialog
    ) {
        val data = Nilai(
            kelasId = kelasId,
            mahasiswaId = mahasiswaId,
            nilai = nilai,
            updatedBy = dosenId,
            updatedAt = System.currentTimeMillis()
        )

        db.collection("nilai")
            .add(data)
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Berhasil menyimpan nilai",
                    Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Gagal menyimpan nilai: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun showDialogError(message: String, dialogBinding: DialogNilaiBinding) {
        dialogBinding.tvError.apply {
            text = message
            visibility = View.VISIBLE
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