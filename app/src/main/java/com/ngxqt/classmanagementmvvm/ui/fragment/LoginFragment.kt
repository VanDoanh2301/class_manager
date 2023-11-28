package com.ngxqt.classmanagementmvvm.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.ngxqt.classmanagementmvvm.R
import com.ngxqt.classmanagementmvvm.databinding.FragmentLoginBinding


class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private var mAuth: FirebaseAuth? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        binding.btnLogin.setOnClickListener(OnClickListener {
            if (binding.email.text.toString().trim { it <= ' ' }.isEmpty()) {
                Toast.makeText(context, "Enter Email", Toast.LENGTH_LONG).show()
            } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.email.text.toString()).matches()) {
                Toast.makeText(context, "Enter Valid Email", Toast.LENGTH_LONG).show()
            } else if (binding.password.text.toString().trim { it <= ' ' }.isEmpty()) {
                Toast.makeText(context, "Enter Password", Toast.LENGTH_LONG) .show()
            } else {
                setUpSignIn(binding.email.getText().toString(), binding.password.text.toString())
            }
        })
    }

    private fun setUpSignIn(email: String, password: String) {
        mAuth!!.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                val user = mAuth!!.currentUser
                findNavController().navigate(R.id.action_loginFragment_to_classFragment)
            } else {
                Toast.makeText(
                    context, "Authentication failed.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}