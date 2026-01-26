package com.example.myapplication.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.myapplication.databinding.DialogAddWaterBinding
import com.example.myapplication.utils.SharedPrefsManager

class AddWaterDialog(private val onWaterAdded: () -> Unit) : DialogFragment() {

    private var _binding: DialogAddWaterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddWaterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.sliderWaterAmount.addOnChangeListener { _, value, _ ->
            val ml = (value * 50).toInt() 
            binding.tvWaterAmount.text = "${ml}ml"
        }

        val initialMl = (binding.sliderWaterAmount.value * 50).toInt()
        binding.tvWaterAmount.text = "${initialMl}ml"

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnAdd.setOnClickListener {
            val amount = (binding.sliderWaterAmount.value * 50).toInt() 
            SharedPrefsManager(requireContext()).addWaterIntake(amount)
            onWaterAdded()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
