package com.example.imagefilterstudio

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.imagefilterstudio.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var python: Python

    // remembers the last filtered file path
    private var lastResultPath: String? = null
    private var currentUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { showImagePreview(it) }
        }

    // --- new camera support ---
    private var cameraUri: Uri? = null
    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && cameraUri != null) {
                showImagePreview(cameraUri!!)
            }
        }
    // ---------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Start Chaquopy
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        python = Python.getInstance()

        binding.btnPick.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        binding.btnApply.setOnClickListener {
            applySelectedFilter()
        }
        binding.btnSave.setOnClickListener {
            saveCurrentImage()
        }

        // --- hook up the new Camera button ---
        binding.btnCamera.setOnClickListener {
            // prepare a file in your app’s private storage
            val photoFile = File(filesDir, "camera_input.jpg").apply {
                if (exists()) delete()
                createNewFile()
            }
            // get a content:// URI via FileProvider
            cameraUri = FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider",
                photoFile
            )
            // launch system camera
            cameraLauncher.launch(cameraUri)
        }
        // ---------------------------------------
    }

    private fun showImagePreview(uri: Uri) {
        currentUri = uri
        contentResolver.openInputStream(uri)?.use { stream ->
            val bmp = BitmapFactory.decodeStream(stream)
            binding.ivPreview.setImageBitmap(bmp)
        }
    }

    private fun applySelectedFilter() {
        val uri = currentUri ?: return

        // Copy into app’s private storage
        val inputFile  = File(filesDir, "in.jpg")
        val outputFile = File(filesDir, "out.jpg")
        contentResolver.openInputStream(uri)?.use { inp ->
            inputFile.outputStream().use { out -> inp.copyTo(out) }
        }

        val filterName = binding.spinnerFilter.selectedItem.toString()

        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                // Run filter off the UI thread
                val resultPath = withContext(Dispatchers.IO) {
                    python.getModule("filters")
                        .callAttr(filterName,
                            inputFile.absolutePath,
                            outputFile.absolutePath)
                        .toString()
                }
                lastResultPath = resultPath

                // Decode & display
                val bmp = BitmapFactory.decodeFile(resultPath)
                binding.ivPreview.setImageBitmap(bmp)

            } catch (e: Exception) {
                Toast.makeText(this@MainActivity,
                    "Filter error: ${e.message}",
                    Toast.LENGTH_LONG).show()
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun saveCurrentImage() {
        val path = lastResultPath
        if (path.isNullOrEmpty()) {
            Toast.makeText(this, "Nothing to save", Toast.LENGTH_SHORT).show()
            return
        }

        val bmp = BitmapFactory.decodeFile(path)
        val filename = "IMG_${System.currentTimeMillis()}.jpg"

        // Prepare metadata
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + "/ImageFilterStudio")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        // Insert into MediaStore
        val uri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
        )
        if (uri != null) {
            contentResolver.openOutputStream(uri)?.use { out ->
                bmp.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                contentResolver.update(uri, values, null, null)
            }
            Toast.makeText(this, "Saved to gallery", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show()
        }
    }
}
