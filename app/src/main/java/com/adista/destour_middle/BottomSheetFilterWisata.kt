package com.adista.destour_middle

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.adista.destour_middle.databinding.BottomSheetFilterWisataBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetFilterWisata(
    private val onSave: (filter: String) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFilterWisataBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFilterWisataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSimpan.setOnClickListener {
            val selectedFilter = when (binding.filter.checkedRadioButtonId) {
                binding.filterAll.id -> "Semua"
                binding.filterBookmark.id -> "Bookmark"
                binding.filterLike.id -> "Like"
                else -> "Semua"
            }

            onSave(selectedFilter)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
