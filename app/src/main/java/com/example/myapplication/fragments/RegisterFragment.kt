package com.example.myapplication.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentRegisterBinding
import com.example.myapplication.models.User
import com.example.myapplication.utils.DateTimeUtils
import com.example.myapplication.utils.SharedPrefsManager

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var prefsManager: SharedPrefsManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsManager = SharedPrefsManager(requireContext())
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            performRegistration()
        }

        binding.tvLogin.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }
    }

    private fun performRegistration() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()
        val agreeToTerms = binding.checkboxTerms.isChecked

        if (name.isEmpty()) {
            binding.etName.error = getString(R.string.error_empty_field)
            return
        }

        if (email.isEmpty()) {
            binding.etEmail.error = getString(R.string.error_empty_field)
            return
        }

        if (!isValidEmail(email)) {
            binding.etEmail.error = getString(R.string.error_invalid_email)
            return
        }

        if (password.isEmpty()) {
            binding.etPassword.error = getString(R.string.error_empty_field)
            return
        }

        if (password.length < 6) {
            binding.etPassword.error = getString(R.string.error_weak_password)
            return
        }

        if (password != confirmPassword) {
            binding.etConfirmPassword.error = getString(R.string.error_password_mismatch)
            return
        }

        if (!agreeToTerms) {
            Toast.makeText(requireContext(), "Please agree to terms and conditions", Toast.LENGTH_SHORT).show()
            return
        }

        val user = User(
            name = name,
            email = email,
            password = password,
            registeredDate = DateTimeUtils.getCurrentDate()
        )

        prefsManager.saveUser(user)
        Toast.makeText(requireContext(), getString(R.string.success_registration), Toast.LENGTH_SHORT).show()

        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
