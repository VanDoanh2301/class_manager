package com.ngxqt.classmanagementmvvm.ui.fragment

import android.Manifest
import android.app.PendingIntent
import android.content.Context.TELEPHONY_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.PhoneStateListener
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import com.ngxqt.classmanagementmvvm.R
import com.ngxqt.classmanagementmvvm.databinding.FragmentContactBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ContactFragment : Fragment() {
    private var _binding: FragmentContactBinding? = null
    private val binding get() = _binding!!
    private lateinit var studentName: String
    private lateinit var studentId: String
    private lateinit var studentPhone: String

    private val TAG = ContactFragment::class.java.simpleName
    private val MY_PERMISSIONS_REQUEST= 1
    private lateinit var mTelephonyManager: TelephonyManager
    private lateinit var mListener: MyPhoneCallListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentContactBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        studentName = arguments?.getString("studentName").toString()
        studentId = arguments?.getInt("studentId",20180000).toString()
        studentPhone = arguments?.getString("studentPhone").toString()

        setToolbar()

        binding.apply {
            tvName.setText("Name: $studentName")
            tvId.setText("ID: $studentId")
            tvPhone.setText("Phone number: $studentPhone")
            buttonCall.visibility = View.INVISIBLE
            buttonCall.setOnClickListener {
                callNumber()
            }
            buttonMess.visibility = View.INVISIBLE
            buttonMess.setOnClickListener {
                smsSendMessage()
            }
        }

        // Create a telephony manager.
        mTelephonyManager = requireActivity().getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        // Check to see if Telephony is enabled.
        if (isTelephonyEnabled()) {
            Log.d(TAG, "Telephony is enabled")
            // Check for phone permission.
            checkForPermission()
            // Register the PhoneStateListener to monitor phone activity.
            mListener = MyPhoneCallListener()
            mTelephonyManager.listen(mListener, PhoneStateListener.LISTEN_CALL_STATE)
        } else {
            Toast.makeText(requireContext(), "TELEPHONY NOT ENABLED!", Toast.LENGTH_LONG).show()
            Log.d(TAG, "TELEPHONY NOT ENABLED!")
            // Disable the call button.
            disableCallButton()
        }
    }

    private fun setToolbar() {
        binding.toolbarContact.apply {
            titleToolbar.setText("Contact")
            subtitleToolbar.setText(studentName)
            back.setOnClickListener { requireActivity().onBackPressed() }
            save.isInvisible = true
        }
    }

    private fun isTelephonyEnabled(): Boolean {
        if (mTelephonyManager != null) {
            if (mTelephonyManager.simState == TelephonyManager.SIM_STATE_READY) {
                return true
            }
        }
        return false
    }

    private fun checkForPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            enableCallButton()
            enableSmsButton()
        } else {
            Log.d(TAG, "PERMISSION NOT GRANTED!")
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(
                Manifest.permission.CALL_PHONE,
                Manifest.permission.SEND_SMS), MY_PERMISSIONS_REQUEST)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Check if permission is granted or not for the request.
        when (requestCode) {
            MY_PERMISSIONS_REQUEST -> {
                if (permissions[0].equals(Manifest.permission.CALL_PHONE, ignoreCase = true)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    // Permission was granted. Enable call button.
                    enableCallButton()
                } else {
                    // Permission denied.
                    Log.d(TAG, getString(R.string.failure_permission))
                    Toast.makeText(requireContext(), getString(R.string.failure_permission), Toast.LENGTH_LONG).show()
                    // Disable the call button.
                    disableCallButton()
                }
                if (permissions[1].equals(Manifest.permission.SEND_SMS, ignoreCase = true)
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                ) {
                    // Permission was granted. Enable sms button.
                    enableSmsButton()
                } else {
                    // Permission denied.
                    Log.d(TAG, getString(R.string.failure_permission))
                    Toast.makeText(requireContext(), getString(R.string.failure_permission), Toast.LENGTH_LONG).show()
                    // Disable the sms button.
                    disableSmsButton()
                }
            }
        }
    }

    private fun callNumber() {
        // Use format with "tel:" and phone number to create phoneNumber.
        val phoneNumber = String.format("tel: %s", studentPhone)
        // Log the concatenated phone number for dialing.
        Log.d(TAG, "Phone Status: DIALING: " + phoneNumber)
        //Toast.makeText(this, "Phone Status: DIALING: " + phoneNumber, Toast.LENGTH_LONG).show()
        // Create the intent.
        val callIntent = Intent(Intent.ACTION_CALL)
        // Set the data for the intent as the phone number.
        callIntent.data = Uri.parse(phoneNumber)
        // If package resolves to an app, check for phone permission,
        // and send intent.
        if (callIntent.resolveActivity(requireActivity().packageManager) != null) {
            checkForPermission()
            startActivity(callIntent)
        } else {
            Log.e(TAG, "Can't resolve app for ACTION_CALL Intent.")
        }
    }

    private fun smsSendMessage() {
        // Set the destination phone number to the string in editText.
        val destinationAddress = studentPhone
        // Get the text of the sms message.
        val smsMessage = binding.edtMess.text.toString().trim()
        // Set the service center address if needed, otherwise null.
        val scAddress: String? = null
        // Set pending intents to broadcast
        // when message sent and when delivered, or set to null.
        val sentIntent: PendingIntent? = null
        val deliveryIntent: PendingIntent? = null
        // Check for permission first.
        checkForPermission()
        // Use SmsManager.
        val smsManager = SmsManager.getDefault()
        if (smsMessage.isNotEmpty()) {
            smsManager.sendTextMessage(
                destinationAddress, scAddress, smsMessage,
                sentIntent, deliveryIntent
            )
            Toast.makeText(requireContext(), "Sent", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Please enter your message", Toast.LENGTH_SHORT).show()
        }

        /**Cách này không còn được sử dụng từ Android 11 trở lên, muốn dùng cần thêm <queries> vào Manifest*/
        /*val smsNumber = String.format("smsto: %s", studentPhone)
        val smsMessage = binding.edtMess.text.toString().trim()
        val smsIntent = Intent(Intent.ACTION_SENDTO)
        smsIntent.data = Uri.parse(smsNumber)
        smsIntent.putExtra("sms_body",smsMessage)
        if (smsIntent.resolveActivity(packageManager) != null) {
            checkForPermission()
            startActivity(smsIntent)
        } else {
            Log.e(TAG, "Can't resolve app for ACTION_SENDTO Intent.")
        }*/
    }

    inner class MyPhoneCallListener : PhoneStateListener() {
        private var returningFromOffHook = false
        override fun onCallStateChanged(state: Int, incomingNumber: String) {
            // Define a string for the message to use in a toast.
            var message = "Phone Status: "
            when (state) {
                TelephonyManager.CALL_STATE_RINGING -> {
                    // Incoming call is ringing (not used for outgoing call).
                    message = message + "RINGING, number: "+ incomingNumber
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    Log.i(TAG, message)
                }
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    // Phone call is active -- off the hook.
                    message = message + "OFFHOOK"
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    Log.i(TAG, message)
                    returningFromOffHook = true
                }
                TelephonyManager.CALL_STATE_IDLE -> {
                    // Phone is idle before and after phone call.
                    // If running on version older than 19 (KitKat),
                    // restart activity when phone call ends.
                    message = message + "IDLE"
                    //Toast.makeText(this@ContactActivity, message, Toast.LENGTH_SHORT).show()
                    Log.i(TAG, message)
                    if (returningFromOffHook) {
                        // No need to do anything if >= version KitKat.
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                            Log.i("MainActivity.TAG", "Restarting app")
                            // Restart the app.
                            //val intent: Intent = getPackageManager().getLaunchIntentForPackage(getPackageName())!!
                            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            //startActivity(intent)
                        }
                    }
                }
                else -> {
                    message = message + "Phone off"
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    Log.i(TAG, message)
                }
            }
        }
    }

    private fun disableCallButton() {
        Toast.makeText(requireContext(), "Phone calling disabled.", Toast.LENGTH_LONG).show()
        binding.buttonCall.visibility = View.INVISIBLE
    }

    private fun enableCallButton() {
        binding.buttonCall.visibility = View.VISIBLE
    }


    private fun disableSmsButton() {
        Toast.makeText(requireContext(), R.string.sms_disabled, Toast.LENGTH_LONG).show()
        binding.buttonMess.visibility = View.INVISIBLE
    }

    private fun enableSmsButton() {
        binding.buttonMess.visibility = View.VISIBLE
    }
}