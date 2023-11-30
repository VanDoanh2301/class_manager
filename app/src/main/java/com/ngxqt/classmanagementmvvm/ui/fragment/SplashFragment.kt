package com.ngxqt.classmanagementmvvm.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.ngxqt.classmanagementmvvm.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Handler(Looper.getMainLooper()).postDelayed({

            if (FirebaseAuth.getInstance().uid != null) {
                val bundle = bundleOf(
                    "uid" to FirebaseAuth.getInstance().uid
                )
                findNavController().navigate(R.id.classFragment, bundle)
            } else {
                findNavController().navigate(R.id.action_splashFragment_to_loginFragment)
            }
        }, 1000)
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }
}