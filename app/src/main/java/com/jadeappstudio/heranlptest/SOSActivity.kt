package com.jadeappstudio.heranlptest

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_sos.*


class SOSActivity : AppCompatActivity() {

    val SMS_SENT_ACTION = "com.jadeappstudio.heranlptest.SMS_SENT_ACTION"
    val SMS_DELIVERED_ACTION = "com.jadeappstudio.heranlptest.SMS_DELIVERED_ACTION"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sos)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED) {
                Log.d("permission", "permission denied to SEND_SMS - requesting it")
                val permissions = arrayOf(Manifest.permission.SEND_SMS)
                requestPermissions(permissions, 1)
            }
        }

        btnSend.setOnClickListener {
            var phoneNum = etPhoneNumber.editText?.text.toString()
            var smsBody = etMessage.editText?.text.toString()

            if (phoneNum.isEmpty()) {
                Toast.makeText(applicationContext, "Please Enter a Phone Number", Toast.LENGTH_LONG)
                    .show()
            } else {
                sendSMS(phoneNum, smsBody)
            }
        }

        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                var message: String? = null
                when (resultCode) {
                    RESULT_OK -> message = "Message Sent Successfully !"
                    SmsManager.RESULT_ERROR_GENERIC_FAILURE -> message = "Error."
                    SmsManager.RESULT_ERROR_NO_SERVICE -> message = "Error: No service."
                    SmsManager.RESULT_ERROR_NULL_PDU -> message = "Error: Null PDU."
                    SmsManager.RESULT_ERROR_RADIO_OFF -> message = "Error: Radio off."
                }
                smsStatus.setText(message)
            }
        }, IntentFilter(SMS_SENT_ACTION))

        registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                etPhoneNumber.editText?.setText("")
                etMessage.editText?.setText("")
                deliveryStatus.setText("SMS Delivered")
            }
        }, IntentFilter(SMS_DELIVERED_ACTION))

        /*if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) +
                    ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS))
            != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    "Manifest.permission.READ_SMS"
                ) ||
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    "Manifest.permission.READ_SMS"
                )
            ) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(
                    this,
                    new String { "Manifest.permission.READ_SMS, Manifest.permission.SEND_SMS" },
                    REQUEST_CODE
                );

                // REQUEST_CODE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }*/
    }

    fun sendSMS(phoneNumber: String?, smsMessage: String?) {
        val sms: SmsManager = SmsManager.getDefault()
        val messages: List<String> = sms.divideMessage(smsMessage)
        for (message in messages) {

            /*
            * sendTextMessage (String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent)
            *
            * Sent Intent: fired when the message is sent and indicates if it's successfully sent or not
            *
            * Delivery Intent: fired when the message is sent and delivered
            * */
            sms.sendTextMessage(
                phoneNumber, null, message, PendingIntent.getBroadcast(
                    this, 0, Intent(SMS_SENT_ACTION), 0
                ), PendingIntent.getBroadcast(this, 0, Intent(SMS_DELIVERED_ACTION), 0)
            )
        }
    }
}