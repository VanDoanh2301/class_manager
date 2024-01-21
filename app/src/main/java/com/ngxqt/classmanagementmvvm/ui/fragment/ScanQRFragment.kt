package com.ngxqt.classmanagementmvvm.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.ngxqt.classmanagementmvvm.databinding.FragmentScanQrBinding


class ScanQRFragment : Fragment() {

    private lateinit var binding: FragmentScanQrBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScanQrBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.scanButton.setOnClickListener {
            // Gọi phương thức để bắt đầu quét mã QR khi người dùng nhấn nút
            startQRScan()
        }
    }

    private fun startQRScan() {
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setOrientationLocked(false)
        integrator.setPrompt("Scan a QR Code")
        integrator.initiateScan()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result.contents != null) {
            // Xử lý dữ liệu từ mã QR code ở đây
            val qrData = result.contents

            // Hiển thị dữ liệu từ mã QR code (ví dụ: thông báo)
            binding.resultTextView.text = "QR Code Data: $qrData"
        } else {
            // Xử lý khi không quét được mã QR
            binding.resultTextView.text = "Failed to scan QR Code"
        }
    }
}
