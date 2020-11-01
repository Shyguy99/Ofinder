package com.example.ofind
import android.app.Activity
import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.soundcloud.android.crop.Crop
import java.io.File

class MainActivity : AppCompatActivity() {
    private var firebaseAuth: FirebaseAuth? = null
    private var camerabtn: Button? = null
    private var gallary: Button? = null
    private var imageUri: Uri? = null
    private var chosen: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        firebaseAuth = FirebaseAuth.getInstance()

        // request permission to use the camera on the user's phone
        if (ActivityCompat.checkSelfPermission(this.applicationContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_PERMISSION)
        }

        // request permission to write data (aka images) to the user's external storage of their phone
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_PERMISSION)
        }

        // request permission to read data (aka images) from the user's external storage of their phone
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_PERMISSION)
        }

        // on click for inception float model
        camerabtn = findViewById<View>(R.id.btn_camera) as Button
        camerabtn!!.setOnClickListener { // filename in assets
            chosen = "monu_classifier.tflite"
            // open camera
            openCameraIntent()
        }


        gallary = findViewById<View>(R.id.btn_gallary) as Button
        gallary!!.setOnClickListener { chosen = "monu_classifier.tflite"
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivityForResult(intent, PICK_FROM_GALLERY);


        }
    }

    // opens camera for user
    private fun openCameraIntent() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New Picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
        // tell camera where to store the resulting picture
        imageUri = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        // start camera, and wait for it to finish

        startActivityForResult(intent, REQUEST_IMAGE)
    }

    // checks that the user has allowed all the required permission of read and write and camera. If not, notify the user and close the application
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION) {
            if (!(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(applicationContext, "This application needs read, write, and camera permissions to run. Application now closing.", Toast.LENGTH_LONG).show()
                System.exit(0)
            }
        }
    }

    // dictates what to do after the user takes an image, selects and image, or crops an image
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // if the camera activity is finished, obtained the uri, crop it to make it square, and send it to 'Classify' activity
        when (requestCode) {

            PICK_FROM_GALLERY -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let { uri ->
                        try {
                            val source_uri = uri
                            val dest_uri = Uri.fromFile(File(cacheDir, "cropped"))
                            // need to crop it to square image as CNN's always required square input
                            Crop.of(source_uri, dest_uri).asSquare().start(this@MainActivity)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            Crop.REQUEST_CROP -> {

                if (resultCode == RESULT_OK) {
                    imageUri = Crop.getOutput(data)
                    val i = Intent(this@MainActivity, image_classify::class.java)
                    // put image data in extras to send
                    i.putExtra("resID_uri", imageUri)
                    // put filename in extras
                    i.putExtra("chosen", chosen)
                    // send other required data
                    startActivity(i)
                    finish()
                }
            }


            REQUEST_IMAGE -> {
                if (resultCode == RESULT_OK) {
                    try {
                        val source_uri = imageUri
                        val dest_uri = Uri.fromFile(File(cacheDir, "cropped"))
                        // need to crop it to square image as CNN's always required square input
                        Crop.of(source_uri, dest_uri).asSquare().start(this@MainActivity)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }


    private fun Logout() {
        firebaseAuth!!.signOut()
        finish()
        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logoutMenu -> {
                Logout()
            }
        }
        return super.onOptionsItemSelected(item)
    }



    companion object {
        const val REQUEST_PERMISSION = 300
        const val REQUEST_IMAGE = 100
        const val PICK_FROM_GALLERY=101
    }
}