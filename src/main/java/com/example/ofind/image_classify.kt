package com.example.ofind

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*

class image_classify : AppCompatActivity() {
    private val tfliteOptions = Interpreter.Options()

    private var tflite: Interpreter? = null
    private var labelList: List<String> = ArrayList()
    private  var imgData: ByteBuffer? = null
    private var labelProbArray: Array<FloatArray>? = null
    private var topLabel:String? = null
    private var topConfidence: String? = null
    private var chosen: String? = null


    private val DIM_IMG_SIZE_X = 299
    private val DIM_IMG_SIZE_Y = 299
    private val DIM_PIXEL_SIZE = 3

    private var intValues:IntArray= intArrayOf (1)

    private var selected_image: ImageView? = null
    private var classify_button: Button? = null
    private var back_button: Button? = null
    private var detail_button: Button? = null
    private var result: TextView? = null
    private var detail_label:TextView?=null
    private var result_banner: TextView? = null
    private var processDialog: ProgressDialog? = null

    private val sortedLabels = PriorityQueue<Map.Entry<String, Float>>(
            RESULTS_TO_SHOW
    ) { o1, o2 -> o2.value.compareTo(o1.value) }

    override fun onCreate(savedInstanceState: Bundle?) {
        processDialog = ProgressDialog(this)
        chosen = intent.getStringExtra("chosen")
        intValues = IntArray(DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y)
        super.onCreate(savedInstanceState)
        try {
            tflite = Interpreter(loadModelFile(), tfliteOptions)
            labelList = loadLabelList()

        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        imgData = ByteBuffer.allocateDirect(
                4 * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE)
        imgData?.order(ByteOrder.nativeOrder())

        for(i in labelList) {
            Log.i("taaa",i)
        }
        labelProbArray = Array(1){ FloatArray(labelList.size) }
        Log.i("taaa","9898")
        for (i in labelProbArray!!){

            Log.i("TAG",i.toString())}

        setContentView(R.layout.activity_image_classify)
        result = findViewById<View>(R.id.result) as TextView
        selected_image = findViewById<View>(R.id.selected_image) as ImageView
        result_banner = findViewById<View>(R.id.tv_result_banner) as TextView
        detail_label=findViewById<View>(R.id.detail_label) as TextView
        detail_button = findViewById<View>(R.id.btn_object_detail) as Button

        detail_button!!.setOnClickListener {
            val intent = Intent(this@image_classify, DetailActivity::class.java)
            intent.putExtra("object", topLabel)
            startActivity(intent)
        }
        back_button = findViewById<View>(R.id.back_button) as Button
        back_button!!.setOnClickListener {
            val i = Intent(this@image_classify, MainActivity::class.java)
            startActivity(i)
            finish()
        }
        classify_button = findViewById<View>(R.id.classify_image) as Button
        classify_button!!.setOnClickListener { // get current bitmap from imageView
            processDialog!!.show()

            val bitmap_orig = (selected_image!!.drawable as BitmapDrawable).bitmap
            // resize the bitmap to the required input size to the CNN
            val bitmap = getResizedBitmap(bitmap_orig, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y)
            // convert bitmap to byte array
            convertBitmapToByteBuffer(bitmap)
            processDialog!!.setMessage("Processing....")
            tflite!!.run(imgData, labelProbArray)
            // display the results
            printTopLabel()
            detail_label?.visibility=View.VISIBLE
            detail_button?.visibility=View.VISIBLE
            processDialog!!.dismiss()
        }

        val uri = intent.getParcelableExtra<Parcelable>("resID_uri") as Uri?
        Log.e("uri",uri.toString())
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            selected_image!!.setImageBitmap(bitmap)
            selected_image!!.rotation = selected_image!!.rotation + 360
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = this.assets.openFd(chosen!!)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap) {
        if (imgData == null) {
            return
        }
        imgData!!.rewind()
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        for (i in 0 until DIM_IMG_SIZE_X) {
            for (j in 0 until DIM_IMG_SIZE_Y) {
                val `val` = intValues[pixel++]
                imgData!!.putFloat(((`val` shr 16 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                imgData!!.putFloat(((`val` shr 8 and 0xFF) - IMAGE_MEAN) / IMAGE_STD)
                imgData!!.putFloat(((`val` and 0xFF) - IMAGE_MEAN) / IMAGE_STD)

            }
        }
    }

    @Throws(IOException::class)
    private fun loadLabelList(): List<String> {
        val labelList: MutableList<String> = ArrayList()
        val reader = BufferedReader(InputStreamReader(this.assets.open("labels.txt")))
        var line: String?=null
        while (true) {
            line = reader.readLine() ?: break
            labelList.add(line)
            Log.d("uu",line)
        }
        reader.close()
        return labelList
    }
    private fun printTopLabel() {
        for (i in labelList!!.indices) {
            sortedLabels.add(AbstractMap.SimpleEntry(labelList!![i], labelProbArray!![0][i]))
        }
        val label = sortedLabels.peek()
        topLabel = label.key
        topConfidence= String.format("%.0f%%", label.value * 100)

        result_banner!!.text = "I am "+topConfidence+" sure this is a"
        result!!.text = topLabel?.toUpperCase()

    }
    fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false)
    }
    companion object {
        /// presets for rgb conversion
        private const val RESULTS_TO_SHOW = 1
        private const val IMAGE_MEAN = 128
        private const val IMAGE_STD = 128.0f
    }
    override fun onBackPressed() {
        finish()
        return
    }

}