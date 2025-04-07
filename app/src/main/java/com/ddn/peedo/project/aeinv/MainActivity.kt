package com.ddn.peedo.project.aeinv


import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.SoundPool
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ddn.peedo.project.aeinv.adapter.ItemDetailsAdapter
import com.ddn.peedo.project.aeinv.databinding.ActivityMainBinding
import com.ddn.peedo.project.aeinv.databinding.PropertyDetailsBinding
import com.ddn.peedo.project.aeinv.model.ItemResponse
import com.ddn.peedo.project.aeinv.services.QRCodeAnalyzer
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val itemViewModel: ItemViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding
    private lateinit var propertyDetailsBinding: PropertyDetailsBinding
    private lateinit var dialog: Dialog

    private var isitemDisplayed: Boolean = false;

    private lateinit var soundPool: SoundPool
    private var soundId: Int = 0
    private var isScanning = false

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemDetailsAdapter

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 101 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        soundPool = SoundPool.Builder().setMaxStreams(1).build()
        soundId = soundPool.load(this, R.raw.beep, 1)


//        val itemName: TextView = findViewById(R.id.itemName)
//        val itemDescription: TextView = findViewById(R.id.itemDescription)
//        val itemPrice: TextView = findViewById(R.id.itemPrice)
//        val itemQuantity: TextView = findViewById(R.id.itemQuantity)
//        val fetchButton: Button = findViewById(R.id.fetchButton)
        with(binding) {
            fetchButton.setOnClickListener {
                val qrCodeText = filter.text.toString().trim()

                Log.d("ONFETCH_QRCODE", "Scanning $qrCodeText")
                Toast.makeText(this@MainActivity, "Scanning $qrCodeText", Toast.LENGTH_SHORT).show()

                if (qrCodeText.isEmpty()) {
                    Toast.makeText(this@MainActivity, "Please enter a QR Code!", Toast.LENGTH_LONG)
                        .show()
                } else {
                    itemViewModel.fetchItem(qrCodeText)
                }

            }

            itemViewModel.items.observe(this@MainActivity) { itemList ->

                if (itemList.isNotEmpty()) {
                    val item = itemList[0]  // ✅ Pick first item or show list

                    Log.d("ONFETCH_QRCODE", "Item: $item")

                    if (!isitemDisplayed) {
                        showItem(item)
                    }
                } else {
                    Log.d("ONFETCH_QRCODE", "No Property Found with this QR Code.")
                    Toast.makeText(
                        this@MainActivity,
                        "No Property Found with this QR Code.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            itemViewModel.errorMessage.observe(this@MainActivity) { errorMessage ->
                errorMessage?.let {
                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                }
            }

        }

        // Request camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
        } else {
            // Initialize camera if permission is already granted
            startCamera()
        }

    }

    @SuppressLint("SetTextI18n")
    private fun showItem(item: ItemResponse) {

        propertyDetailsBinding = PropertyDetailsBinding.inflate(this.layoutInflater)

        isitemDisplayed = true

        val builder = AlertDialog.Builder(this)

        builder.setView(propertyDetailsBinding.root)
        builder.setCancelable(false)
        dialog = builder.create()
//        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // <-- Add this here

        with(propertyDetailsBinding) {
            loadingIndicator.visibility = View.VISIBLE
            title.visibility = View.GONE
            scroll.visibility = View.GONE
            btnClose.visibility = View.GONE

            Handler(Looper.getMainLooper()).postDelayed({

                module.setText(item.Module)
                qrNo.setText(item.QRCode)
                propertyNo.setText(item.PropertyNo)
                description.setText(item.Description)
                serialNo.setText(item.SerialNo)
                cost.setText(item.Amount.toString())
                dateAcquired.setText(item.Date_Acquired)
                if (item.TranferFlag) {
                    transferType.visibility = View.VISIBLE
                    transferReason.visibility = View.VISIBLE
                    transferType.setText(
                        if ((item.TransferType?.lowercase() ?: "") != "others") {
                            item.TransferType
                        } else {
                            item.TransferOthersType
                        }
                    )
                    transferReason.setText(item.TransferReason)
                } else {
                    transferType.visibility = View.GONE
                    transferReason.visibility = View.GONE
                }
                if (item.ReturnFlag) {
                    returnType.visibility = View.VISIBLE
                    returnType.setText(
                        if ((item.ReturnType?.lowercase() ?: "") != "others") {
                            item.ReturnType
                        } else {
                            item.ReturnOthersType
                        }
                    )
                } else {
                    returnType.visibility = View.GONE
                }

                issuedBy.setText(item.IssuedBy)
                receivedBy.setText(item.ReceivedBy)
                if (item.TranferFlag || item.ReturnFlag) {
                    approvedBy.setText(item.ApprovedBy)
                }
                createdBy.setText(item.CreatedBy)

                btnClose.setOnClickListener {
                    dialog.dismiss()
                    isitemDisplayed = false
                }


                loadingIndicator.visibility = View.GONE
                title.visibility = View.VISIBLE
                scroll.visibility = View.VISIBLE
                btnClose.visibility = View.VISIBLE

            }, 500) // Simulate short loading delay (optional)

        }
        /*if(dialogForgotPassword.window != null){
            dialogForgotPassword.window!!.setBackgroundDrawable(ColorDrawable(0))
        }*/
        dialog.show()

    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(
                        ContextCompat.getMainExecutor(this),
                        QRCodeAnalyzer { qrCode ->
                            Log.d("ONFETCH_QRCODE", "Scanned: $qrCode")

                            if (isitemDisplayed) {
                                return@QRCodeAnalyzer
                            }
                            // Check if QR code is found or is empty
                            if (!isScanning && qrCode.isNotEmpty()) {
                                itemViewModel.fetchItem(qrCode) // Fetch from your API
                                isScanning = true
                                soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
                                itemViewModel.fetchItem(qrCode)

                                Handler(Looper.getMainLooper()).postDelayed({
                                    isScanning = false
                                }, 3000) // delay for 2 seconds
                            }
                        }
                    )
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            } catch (e: Exception) {
                Log.e("ONFETCH_QRCODE", "Camera binding failed", e)
            }

        }, ContextCompat.getMainExecutor(this))
    }

}
