package Fragmentes

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.ImageCapture
import androidx.core.content.ContextCompat
import com.example.recipeai.R

class CameraFragment : Fragment() {
    private lateinit var imageCapture: ImageCapture

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_camera, container, false)

        view.btn_take_photo.setOnClickListener { takePhoto() }
        view.btn_upload.setOnClickListener { openGallery() }

        return view
    }

    private fun takePhoto() {
        val imageCapture = ImageCapture.Builder().build()

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(viewFinder.surfaceProvider)
        }

        ProcessCameraProvider.getInstance(requireContext()).also { provider ->
            provider.unbindAll()
            provider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
        }

        val photoFile = File.createTempFile("recipe_photo", ".jpg", requireContext().externalCacheDir)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val imageUri = Uri.fromFile(photoFile)
                    view.image_preview.setImageURI(imageUri)
                    analyzeImage(photoFile)
                }
                override fun onError(exc: ImageCaptureException) {
                    Log.e("Camera", "Error al tomar foto: ${exc.message}")
                }
            })
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data
            imageUri?.let {
                view.image_preview.setImageURI(it)
                analyzeImage(it)
            }
        }
    }

    private fun analyzeImage(imageFile: File) {
        // Aquí llamarás a tu API de IA
        val retrofit = Retrofit.Builder()
            .baseUrl("https://tu-api-de-ia.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(RecipeApiService::class.java)
        val requestBody = RequestBody.create("image/*".toMediaType(), imageFile)
        val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestBody)

        service.analyzeImage(imagePart).enqueue(object : Callback<RecipeResponse> {
            override fun onResponse(call: Call<RecipeResponse>, response: Response<RecipeResponse>) {
                if (response.isSuccessful) {
                    val recipe = response.body()
                    navigateToRecipeFragment(recipe)
                }
            }
            override fun onFailure(call: Call<RecipeResponse>, t: Throwable) {
                // Manejar error
            }
        })
    }
}