package com.isa.mp.siasat.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.isa.mp.siasat.adapter.KelasAdapter
import com.isa.mp.siasat.adapter.KelasWithDetails
import com.isa.mp.siasat.databinding.DialogKelasBinding
import com.isa.mp.siasat.databinding.FragmentKelasBinding
import com.isa.mp.siasat.databinding.DialogJadwalDosenBinding
import com.isa.mp.siasat.adapter.JadwalDosenAdapter
import com.isa.mp.siasat.model.Jadwal
import com.isa.mp.siasat.model.Kelas
import com.isa.mp.siasat.model.MataKuliah

class KelasFragment : Fragment() {
    private var _binding: FragmentKelasBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: KelasAdapter

    private val mataKuliahList = mutableListOf<MataKuliah>()
    private val dosenList = mutableListOf<Pair<String, String>>() // id to nama

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
                showEditDialog(kelas)
            },
            onDeleteClick = { kelas ->
                showDeleteConfirmation(kelas)
            }
        )
        binding.rvKelas.adapter = adapter
    }

    fun showEditDialog(kelas: Kelas? = null) {
        val dialogBinding = DialogKelasBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialogBinding.apply {
            tvTitle.text = if (kelas == null) "Tambah Kelas" else "Edit Kelas"

            // Setup spinners
            setupMataKuliahSpinner(dialogBinding, kelas?.mataKuliahId)
            setupDosenSpinner(dialogBinding, kelas?.dosenId)
            setupSemesterSpinner(dialogBinding, kelas?.semester)
            setupHariSpinner(dialogBinding, kelas?.jadwal?.hari)
            setupJamSpinner(dialogBinding, kelas?.jadwal)

            // Pre-fill form if editing
            if (kelas != null) {
                etKapasitas.setText(kelas.kapasitas.toString())
            }

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            btnSave.setOnClickListener {
                val mataKuliahId = mataKuliahList.find {
                    "${it.nama} (${it.kode})" == spinnerMataKuliah.text.toString()
                }?.id
                val dosenId = dosenList.find {
                    it.second == spinnerDosen.text.toString()
                }?.first
                val semester = spinnerSemester.text.toString()
                val hari = spinnerHari.text.toString()
                val jamMulai = spinnerJamMulai.text.toString().substringBefore(":").toIntOrNull()
                val jamSelesai = spinnerJamSelesai.text.toString().substringBefore(":").toIntOrNull()
                val kapasitas = etKapasitas.text.toString().toIntOrNull()

                validateInput(
                    mataKuliahId,
                    dosenId,
                    semester,
                    hari,
                    jamMulai,
                    jamSelesai,
                    kapasitas,
                    dialogBinding,
                    kelas?.id
                ) {
                    saveKelas(
                        kelas?.id,
                        mataKuliahId!!,
                        dosenId!!,
                        semester.replace(" ", ""), // Remove space
                        Jadwal(hari, jamMulai!!, jamSelesai!!),
                        kapasitas!!,
                        kelas?.mahasiswa ?: emptyList(),
                        dialog
                    )
                }
            }
        }

        dialog.show()
    }

    private fun setupMataKuliahSpinner(dialogBinding: DialogKelasBinding, selectedId: String?) {
        val items = mataKuliahList.map { "${it.nama} (${it.kode})" }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, items)
        (dialogBinding.spinnerMataKuliah as? AutoCompleteTextView)?.apply {
            setAdapter(adapter)
            if (selectedId != null) {
                val selected = mataKuliahList.find { it.id == selectedId }
                setText("${selected?.nama} (${selected?.kode})", false)
            }
        }
    }

    private fun showJadwalDialog(dosenId: String) {
        val dialogBinding = DialogJadwalDosenBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()

        // Setup adapter
        val adapter = JadwalDosenAdapter()
        dialogBinding.rvJadwal.adapter = adapter

        // Set dosen name
        val dosenNama = dosenList.find { it.first == dosenId }?.second ?: ""
        dialogBinding.tvDosen.text = dosenNama

        // Load jadwal
        db.collection("kelas")
            .whereEqualTo("dosenId", dosenId)
            .get()
            .addOnSuccessListener { documents ->
                val jadwalList = documents.mapNotNull { doc ->
                    val kelas = doc.toObject(Kelas::class.java).copy(id = doc.id)
                    val mataKuliah = mataKuliahList.find { it.id == kelas.mataKuliahId }
                        ?: return@mapNotNull null

                    KelasWithDetails(kelas, mataKuliah, dosenNama)
                }
                adapter.setJadwal(jadwalList)
            }

        dialogBinding.btnClose.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setupDosenSpinner(dialogBinding: DialogKelasBinding, selectedId: String?) {
        val items = dosenList.map { it.second }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, items)
        (dialogBinding.spinnerDosen as? AutoCompleteTextView)?.apply {
            setAdapter(adapter)
            if (selectedId != null) {
                val selected = dosenList.find { it.first == selectedId }
                setText(selected?.second, false)
            }
            
            // Show jadwal when dosen is selected
            setOnItemClickListener { _, _, position, _ ->
                val dosenId = dosenList[position].first
                showJadwalDialog(dosenId)
            }
        }
    }

    private fun setupSemesterSpinner(dialogBinding: DialogKelasBinding, selected: String?) {
        val items = listOf("20231", "20232").map { semester ->
            val tahun = semester.substring(0, 4)
            val jenis = when (semester.last()) {
                '1' -> "Ganjil"
                '2' -> "Genap"
                else -> ""
            }
            "$tahun $jenis"
        }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, items)
        (dialogBinding.spinnerSemester as? AutoCompleteTextView)?.apply {
            setAdapter(adapter)
            if (selected != null) {
                val tahun = selected.substring(0, 4)
                val jenis = when (selected.last()) {
                    '1' -> "Ganjil"
                    '2' -> "Genap"
                    else -> ""
                }
                setText("$tahun $jenis", false)
            }
        }
    }

    private fun setupHariSpinner(dialogBinding: DialogKelasBinding, selected: String?) {
        val items = listOf("Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, items)
        (dialogBinding.spinnerHari as? AutoCompleteTextView)?.apply {
            setAdapter(adapter)
            if (selected != null) {
                setText(selected, false)
            }
        }
    }

    private fun setupJamSpinner(dialogBinding: DialogKelasBinding, jadwal: Jadwal?) {
        val items = (7..17).map { String.format("%02d:00", it) }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, items)
        
        (dialogBinding.spinnerJamMulai as? AutoCompleteTextView)?.apply {
            setAdapter(adapter)
            if (jadwal != null) {
                setText(String.format("%02d:00", jadwal.jamMulai), false)
            }
        }

        (dialogBinding.spinnerJamSelesai as? AutoCompleteTextView)?.apply {
            setAdapter(adapter)
            if (jadwal != null) {
                setText(String.format("%02d:00", jadwal.jamSelesai), false)
            }
        }
    }

    private fun validateJadwalBentrok(
        kelasId: String?,
        dosenId: String,
        hari: String,
        jamMulai: Int,
        jamSelesai: Int,
        dialogBinding: DialogKelasBinding,
        onSuccess: () -> Unit
    ) {
        db.collection("kelas")
            .whereEqualTo("dosenId", dosenId)
            .get()
            .addOnSuccessListener { documents ->
                var bentrok = false
                for (doc in documents) {
                    // Skip jika sedang edit dan ini adalah kelas yang sedang diedit
                    if (kelasId != null && doc.id == kelasId) continue

                    val kelas = doc.toObject(Kelas::class.java)
                    if (kelas.jadwal.hari == hari) {
                        // Cek apakah jadwal bentrok
                        val existingStart = kelas.jadwal.jamMulai
                        val existingEnd = kelas.jadwal.jamSelesai
                        
                        if (
                            // Kasus 1: Jam mulai baru di antara jadwal yang ada
                            (jamMulai >= existingStart && jamMulai < existingEnd) ||
                            // Kasus 2: Jam selesai baru di antara jadwal yang ada
                            (jamSelesai > existingStart && jamSelesai <= existingEnd) ||
                            // Kasus 3: Jadwal baru mencakup jadwal yang ada
                            (jamMulai <= existingStart && jamSelesai >= existingEnd)
                        ) {
                            bentrok = true
                            val mataKuliah = mataKuliahList.find { it.id == kelas.mataKuliahId }
                            showDialogError(
                                "Jadwal bentrok dengan kelas ${mataKuliah?.nama} " +
                                "(${kelas.jadwal.hari}, " +
                                String.format("%02d:00-%02d:00", existingStart, existingEnd)+")",
                                dialogBinding
                            )
                            break
                        }
                    }
                }
                
                if (!bentrok) {
                    onSuccess()
                }
            }
            .addOnFailureListener { e ->
                showDialogError("Gagal memeriksa jadwal: ${e.message}", dialogBinding)
            }
    }

    private fun validateInput(
        mataKuliahId: String?,
        dosenId: String?,
        semester: String,
        hari: String,
        jamMulai: Int?,
        jamSelesai: Int?,
        kapasitas: Int?,
        dialogBinding: DialogKelasBinding,
        kelasId: String? = null,
        onSuccess: () -> Unit
    ) {
        if (mataKuliahId == null) {
            showDialogError("Pilih mata kuliah", dialogBinding)
            return
        }

        if (dosenId == null) {
            showDialogError("Pilih dosen", dialogBinding)
            return
        }

        if (semester.isEmpty()) {
            showDialogError("Pilih semester", dialogBinding)
            return
        }

        if (hari.isEmpty()) {
            showDialogError("Pilih hari", dialogBinding)
            return
        }

        if (jamMulai == null) {
            showDialogError("Pilih jam mulai", dialogBinding)
            return
        }

        if (jamSelesai == null) {
            showDialogError("Pilih jam selesai", dialogBinding)
            return
        }

        if (jamMulai >= jamSelesai) {
            showDialogError("Jam selesai harus lebih besar dari jam mulai", dialogBinding)
            return
        }

        if (kapasitas == null || kapasitas < 1) {
            showDialogError("Kapasitas minimal 1", dialogBinding)
            return
        }

        // Validasi bentrok jadwal
        validateJadwalBentrok(
            kelasId,
            dosenId,
            hari,
            jamMulai,
            jamSelesai,
            dialogBinding
        ) {
            onSuccess()
        }
    }

    private fun showDialogError(message: String, dialogBinding: DialogKelasBinding) {
        dialogBinding.tvError.apply {
            text = message
            visibility = View.VISIBLE
        }
    }

    private fun saveKelas(
        id: String?,
        mataKuliahId: String,
        dosenId: String,
        semester: String,
        jadwal: Jadwal,
        kapasitas: Int,
        mahasiswa: List<String>,
        dialog: MaterialAlertDialogBuilder
    ) {
        val data = hashMapOf(
            "mataKuliahId" to mataKuliahId,
            "dosenId" to dosenId,
            "semester" to semester.replace(" ", ""), // Remove space
            "jadwal" to jadwal,
            "kapasitas" to kapasitas,
            "mahasiswa" to mahasiswa,
            "updatedAt" to System.currentTimeMillis()
        )

        if (id == null) {
            // Add new
            data["createdAt"] = System.currentTimeMillis()

            db.collection("kelas")
                .add(data)
                .addOnSuccessListener {
                    Toast.makeText(
                        requireContext(),
                        "Berhasil menambahkan kelas",
                        Toast.LENGTH_SHORT
                    ).show()
                    dialog.dismiss()

                    // Update dosen's mengajar array
                    db.collection("users").document(dosenId)
                        .update("mengajar", com.google.firebase.firestore.FieldValue.arrayUnion(it.id))
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        requireContext(),
                        "Gagal menambahkan: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            // Update existing
            db.collection("kelas")
                .document(id)
                .update(data)
                .addOnSuccessListener {
                    Toast.makeText(
                        requireContext(),
                        "Berhasil mengubah kelas",
                        Toast.LENGTH_SHORT
                    ).show()
                    dialog.dismiss()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        requireContext(),
                        "Gagal mengubah: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun showDeleteConfirmation(kelas: Kelas) {
        val mataKuliah = mataKuliahList.find { it.id == kelas.mataKuliahId }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Kelas")
            .setMessage("Apakah Anda yakin ingin menghapus kelas ${mataKuliah?.nama}?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteKelas(kelas)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteKelas(kelas: Kelas) {
        db.collection("kelas")
            .document(kelas.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Berhasil menghapus kelas",
                    Toast.LENGTH_SHORT
                ).show()

                // Remove from dosen's mengajar array
                db.collection("users").document(kelas.dosenId)
                    .update("mengajar", com.google.firebase.firestore.FieldValue.arrayRemove(kelas.id))
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