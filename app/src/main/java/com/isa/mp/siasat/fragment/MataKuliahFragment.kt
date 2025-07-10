package com.isa.mp.siasat.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.isa.mp.siasat.adapter.MataKuliahAdapter
import com.isa.mp.siasat.databinding.DialogMataKuliahBinding
import com.isa.mp.siasat.databinding.FragmentMataKuliahBinding
import com.isa.mp.siasat.model.MataKuliah

class MataKuliahFragment : Fragment() {
    private var _binding: FragmentMataKuliahBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: MataKuliahAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMataKuliahBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadMataKuliah()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        adapter = MataKuliahAdapter(
            onEditClick = { mataKuliah ->
                showEditDialog(mataKuliah)
            },
            onDeleteClick = { mataKuliah ->
                showDeleteConfirmation(mataKuliah)
            }
        )
        binding.rvMataKuliah.adapter = adapter
    }

    private fun loadMataKuliah() {
        showLoading(true)
        db.collection("mataKuliah")
            .orderBy("kode", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    showError("Gagal memuat data: ${e.message}")
                    return@addSnapshotListener
                }

                val mataKuliahList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(MataKuliah::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                adapter.submitList(mataKuliahList)
                showEmpty(mataKuliahList.isEmpty())
                showLoading(false)
            }
    }

    private fun showEditDialog(mataKuliah: MataKuliah? = null) {
        val dialogBinding = DialogMataKuliahBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialogBinding.apply {
            tvTitle.text = if (mataKuliah == null) "Tambah Mata Kuliah" else "Edit Mata Kuliah"

            // Pre-fill form if editing
            if (mataKuliah != null) {
                etKode.setText(mataKuliah.kode)
                etNama.setText(mataKuliah.nama)
                etSks.setText(mataKuliah.sks.toString())
            }

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            btnSave.setOnClickListener {
                val kode = etKode.text.toString().trim()
                val nama = etNama.text.toString().trim()
                val sks = etSks.text.toString().trim()

                if (validateInput(kode, nama, sks, dialogBinding)) {
                    saveMataKuliah(
                        mataKuliah?.id,
                        kode,
                        nama,
                        sks.toInt(),
                        dialog
                    )
                }
            }
        }

        dialog.show()
    }

    fun showAddDialog() {
        showEditDialog()
    }

    private fun validateInput(
        kode: String,
        nama: String,
        sks: String,
        dialogBinding: DialogMataKuliahBinding
    ): Boolean {
        if (kode.isEmpty()) {
            showDialogError("Masukkan kode mata kuliah", dialogBinding)
            return false
        }

        if (!kode.matches(Regex("^[A-Z]{2}\\d{3}\$"))) {
            showDialogError("Format kode: 2 huruf + 3 angka (contoh: IN123)", dialogBinding)
            return false
        }

        if (nama.isEmpty()) {
            showDialogError("Masukkan nama mata kuliah", dialogBinding)
            return false
        }

        if (sks.isEmpty()) {
            showDialogError("Masukkan jumlah SKS", dialogBinding)
            return false
        }

        val sksInt = sks.toIntOrNull()
        if (sksInt == null || sksInt < 1 || sksInt > 6) {
            showDialogError("SKS harus antara 1-6", dialogBinding)
            return false
        }

        return true
    }

    private fun showDialogError(message: String, dialogBinding: DialogMataKuliahBinding) {
        dialogBinding.tvError.apply {
            text = message
            visibility = View.VISIBLE
        }
    }

    private fun saveMataKuliah(
        id: String?,
        kode: String,
        nama: String,
        sks: Int,
        dialog: AlertDialog
    ) {
        val data = hashMapOf<String, Any>(
            "kode" to kode,
            "nama" to nama,
            "sks" to sks,
            "updatedAt" to System.currentTimeMillis()
        )

        if (id == null) {
            // Add new
            data["createdAt"] = System.currentTimeMillis()
            data["createdBy"] = requireActivity().getSharedPreferences("SIASAT", 0)
                .getString("userId", "") ?: ""

            db.collection("mataKuliah")
                .add(data)
                .addOnSuccessListener {
                    Toast.makeText(
                        requireContext(),
                        "Berhasil menambahkan mata kuliah",
                        Toast.LENGTH_SHORT
                    ).show()
                    dialog.dismiss()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        requireContext(),
                        "Gagal menambahkan: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            // Update
            db.collection("mataKuliah")
                .document(id)
                .update(data)
                .addOnSuccessListener {
                    Toast.makeText(
                        requireContext(),
                        "Berhasil mengupdate mata kuliah",
                        Toast.LENGTH_SHORT
                    ).show()
                    dialog.dismiss()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        requireContext(),
                        "Gagal mengupdate: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun showDeleteConfirmation(mataKuliah: MataKuliah) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Mata Kuliah")
            .setMessage("Apakah Anda yakin ingin menghapus mata kuliah ${mataKuliah.nama}?")
            .setPositiveButton("Ya") { _, _ ->
                deleteMataKuliah(mataKuliah)
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun deleteMataKuliah(mataKuliah: MataKuliah) {
        db.collection("mataKuliah")
            .document(mataKuliah.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Berhasil menghapus mata kuliah",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Gagal menghapus: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
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