package com.example.screenshotlocation

import android.Manifest
import android.R.attr.phoneNumber
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.screenshotlocation.dataModel.LocationData
import com.example.screenshotlocation.dataModel.UserInfo
import com.example.screenshotlocation.network.RetrofitInstance
import com.example.screenshotlocation.repository.BaseRepository
import com.example.screenshotlocation.utils.Resource
import com.example.screenshotlocation.viewModel.UserViewModel
import com.example.screenshotlocation.viewModel.UserViewModelProviderFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.otaliastudios.cameraview.CameraListener
import com.otaliastudios.cameraview.CameraView
import com.otaliastudios.cameraview.PictureResult
import retrofit2.Retrofit
import java.io.ByteArrayOutputStream


class MainActivity : AppCompatActivity() {
    private lateinit var cameraView: CameraView
    private lateinit var userViewModel: UserViewModel
    private lateinit var retrofit: Retrofit
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        cameraView = findViewById(R.id.cameraView)
        cameraView.setLifecycleOwner(this)
        retrofit = RetrofitInstance.instance
        val userRepository = BaseRepository(retrofit)
        val userViewModelProviderFactory = UserViewModelProviderFactory(userRepository)
        userViewModel = ViewModelProvider(this, userViewModelProviderFactory)[UserViewModel::class.java]
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        checkLocationPermission()
        findViewById<Button>(R.id.button).setOnClickListener {
            cameraView.takePictureSnapshot()
//            val shareIntent = Intent()
//            shareIntent.setAction(Intent.ACTION_SEND)
//            shareIntent.setType("image/*")
//            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("https://res.cloudinary.com/demo/image/upload/kitten_fighting.gif")) // URI of the image
//            shareIntent.putExtra("jid","+2348146397088" + "@s.whatsapp.net") // WhatsApp phone number with country code
//            shareIntent.setPackage("com.whatsapp")
//            startActivity(shareIntent)

        }

        cameraView.addCameraListener(object : CameraListener() {
            override fun onPictureTaken(result: PictureResult) {
                super.onPictureTaken(result)
                val pictureByteArray = result.getData()

                val bitmap = BitmapFactory.decodeByteArray(pictureByteArray, 0, pictureByteArray.size)
                val base64Image = convertBitmapToBase64(bitmap)
                getLastLocation(base64Image)
            }
        })

        userViewModel.userInfoState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    // Show a loading indicator
                    Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show()
                }
                is Resource.Success -> {
                    // Handle success
                    Toast.makeText(this, "Success: ${resource.data}", Toast.LENGTH_SHORT).show()
                }
                is Resource.Error -> {
                    // Handle error
                    Toast.makeText(this, "Error: ${resource.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun getLastLocation(base64Image: String) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener(this) { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    createApiRequest(base64Image, latitude, longitude)
                } else {
                    Toast.makeText(this, "Unable to retrieve location", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to get location: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createApiRequest(base64Image: String, latitude: Double, longitude: Double) {
        val userInfo = UserInfo(
            phoneNumbers = listOf("080333333333", "080444444444"),
            image = base64Image,
            location = LocationData(longitude.toString(),latitude.toString())
        )
        userViewModel.submitUserInfo(userInfo)
    }
        override fun onDestroy() {
        super.onDestroy()
        cameraView.destroy()
    }
}