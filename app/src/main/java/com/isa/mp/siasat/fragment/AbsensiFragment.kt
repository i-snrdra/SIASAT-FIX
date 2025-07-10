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
import com.isa.mp.siasat.adapter.KelasAdapter
import com.isa.mp.siasat.adapter.KelasWithDetails
import com.isa.mp.siasat.databinding.DialogAbsensiBinding
import com.isa.mp.siasat.databinding.FragmentKelasBinding
import com.isa.mp.siasat.model.Absensi
import com.isa.mp.siasat.model.Kelas
import com.isa.mp.siasat.model.MataKuliah

class AbsensiFragment : Fragment() {
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
                checkAbsensiStatus(kelas)
            },
            onDeleteClick = { /* Dosen tidak bisa hapus kelas */ }
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

    private fun checkAbsensiStatus(kelas: Kelas) {
        db.collection("absensi")
            .whereEqualTo("kelasId", kelas.id)
            .whereEqualTo("status", Absensi.STATUS_TERBUKA)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Tidak ada absensi yang terbuka, tampilkan dialog buka absensi
                    showAbsensiDialog(kelas)
                } else {
                    // Ada absensi yang terbuka, tampilkan dialog tutup absensi
                    val absensi = documents.documents[0].toObject(Absensi::class.java)!!
                        .copy(id = documents.documents[0].id)
                    showTutupAbsensiDialog(absensi)
                }
            }
            .addOnFailureListener { e ->
                showError("Gagal mengecek status absensi: ${e.message}")
            }
    }

    private fun showAbsensiDialog(kelas: Kelas) {
        val dialogBinding = DialogAbsensiBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        // Get last pertemuan number
        db.collection("absensi")
            .whereEqualTo("kelasId", kelas.id)
            .orderBy("pertemuan", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                val lastPertemuan = if (documents.isEmpty) 0
                    else documents.documents[0].toObject(Absensi::class.java)?.pertemuan ?: 0
                dialogBinding.etPertemuan.setText((lastPertemuan + 1).toString())
            }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnSave.setOnClickListener {
            val pertemuan = dialogBinding.etPertemuan.text.toString()
            val topik = dialogBinding.etTopik.text.toString()

            if (validateInput(pertemuan, topik, dialogBinding)) {
                bukaAbsensi(
                    kelas.id,
                    pertemuan.toInt(),
                    topik,
                    dialog
                )
            }
        }

        dialog.show()
    }

    private fun showTutupAbsensiDialog(absensi: Absensi) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Tutup Absensi")
            .setMessage("Apakah Anda yakin ingin menutup absensi untuk pertemuan ke-${absensi.pertemuan}?")
            .setPositiveButton("Tutup") { _, _ ->
                tutupAbsensi(absensi)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun validateInput(
        pertemuan: String,
        topik: String,
        dialogBinding: DialogAbsensiBinding
    ): Boolean {
        if (pertemuan.isEmpty()) {
            showDialogError("Masukkan nomor pertemuan", dialogBinding)
            return false
        }

        if (topik.isEmpty()) {
            showDialogError("Masukkan topik pertemuan", dialogBinding)
            return false
        }

        return true
    }

    private fun bukaAbsensi(
        kelasId: String,
        pertemuan: Int,
        topik: String,
        dialog: MaterialAlertDialogBuilder
    ) {
        val now = System.currentTimeMillis()
        val data = Absensi(
            kelasId = kelasId,
            pertemuan = pertemuan,
            topik = topik,
            tanggal = now,
            status = Absensi.STATUS_TERBUKA,
            dibukaPada = now
        )

        db.collection("absensi")
            .add(data)
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Berhasil membuka absensi",
                    Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            }
            .addOnFailureListener { e ->
                showError("Gagal membuka absensi: ${e.message}")
            }
    }

    private fun tutupAbsensi(absensi: Absensi) {
        db.collection("absensi")
            .document(absensi.id)
            .update(
                mapOf(
                    "status" to Absensi.STATUS_TERTUTUP,
                    "ditutupPada" to System.currentTimeMillis()
                )
            )
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Berhasil menutup absensi",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                showError("Gagal menutup absensi: ${e.message}")
            }
    }

    private fun showDialogError(message: String, dialogBinding: DialogAbsensiBinding) {
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