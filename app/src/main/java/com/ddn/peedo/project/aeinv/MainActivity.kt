package com.ddn.peedo.project.aeinv


import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
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
import com.ddn.peedo.project.aeinv.services.QRCodeAnalyzer

class MainActivity : AppCompatActivity() {

    private val itemViewModel: ItemViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding


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



//        val itemName: TextView = findViewById(R.id.itemName)
//        val itemDescription: TextView = findViewById(R.id.itemDescription)
//        val itemPrice: TextView = findViewById(R.id.itemPrice)
//        val itemQuantity: TextView = findViewById(R.id.itemQuantity)
//        val fetchButton: Button = findViewById(R.id.fetchButton)
        with(binding) {
            fetchButton.setOnClickListener {
                val qrCodeText = qrCode.text.toString().trim()

                Log.d("ONFETCH_QRCODE", "Scanning $qrCodeText")
                Toast.makeText(this@MainActivity, "Scanning $qrCodeText", Toast.LENGTH_SHORT).show()

                if (qrCodeText.isEmpty()) {
                    Toast.makeText(this@MainActivity, "Please enter a QR Code!", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }

                itemViewModel.fetchItem(qrCodeText)
            }

            itemViewModel.items.observe(this@MainActivity) { itemList ->
                if (itemList.isNotEmpty()) {
                    val item = itemList[0]  // ✅ Pick first item or show list
                    Log.d("ONFETCH_QRCODE", "Item: ${item.Description}")

                    itemName.text = "ID: ${item.ITEMID}"
                    itemDescription.text = "Description: ${item.Description}"
                    itemPrice.text = "Property No.: ${item.PropertyNo}"
                    itemQuantity.text = "Quantity: ${item.Amount}"
                } else {
                    Log.d("ONFETCH_QRCODE", "No item found")
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
                            itemViewModel.fetchItem(qrCode) // Fetch from your API
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
