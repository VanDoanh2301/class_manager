package com.ngxqt.classmanagementmvvm.ui.fragment

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.ngxqt.classmanagementmvvm.databinding.FragmentGenerateQrBinding
import java.time.LocalDate


class GenerateQRFragment : Fragment() {

    private lateinit var binding: FragmentGenerateQrBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGenerateQrBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.generateButton.setOnClickListener {
            // Gọi phương thức để tạo mã QR khi người dùng nhấn nút
            generateQRCode("Attendance ${LocalDate.now()}")
        }
    }

    private fun generateQRCode(qrData: String) {
        try {
            // Tạo BitMatrix từ dữ liệu QR
            val bitMatrix: BitMatrix = MultiFormatWriter().encode(
                qrData,
                BarcodeFormat.QR_CODE,
                500,
                500
            )

            // Tạo Bitmap từ BitMatrix
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(
                        x,
                        y,
                        if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
                    )
                }
            }

            // Hiển thị Bitmap trong ImageView
            binding.qrImageView.setImageBitmap(bitmap)

        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }
}
