package com.serkanemek.emlakfoto.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.serkanemek.emlakfoto.R
import com.serkanemek.emlakfoto.databinding.ActivityArtBinding
import java.io.ByteArrayOutputStream
import java.lang.Exception

class RealtyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArtBinding
    private lateinit var activityResultLauncher :ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    var selectedBitmap : Bitmap? = null

    private lateinit var database :SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        database =this.openOrCreateDatabase("RealEstate", MODE_PRIVATE, null)

        registerLauncher()

        val intent = intent
        val info =intent.getStringExtra("info")

        if (info.equals("new")){  //if new page
            binding.adressText.setText("")
            binding.dateText.setText("")
            binding.imageView
            binding.saveButton.visibility = View.VISIBLE
            binding.imageView.setImageResource(R.drawable.select_image)
        }else{ //if info is old. Show from database which we clicked from recycler item

            binding.saveButton.visibility = View.INVISIBLE

            val selectedId = intent.getIntExtra("id", 1)

            val cursor = database.rawQuery("SELECT * FROM realestate WHERE id = ?", arrayOf(selectedId.toString()))

            val adressNameIx = cursor.getColumnIndex("adressname")
            val dateIx = cursor.getColumnIndex("date")
            val imageIx = cursor.getColumnIndex("image")

            while (cursor.moveToNext()){
                binding.adressText.setText(cursor.getString(adressNameIx))
                binding.dateText.setText(cursor.getString(dateIx))

                val byteArray = cursor.getBlob(imageIx)
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0 , byteArray.size)
                binding.imageView.setImageBitmap(bitmap)
            }
            cursor.close()

        }

    }

    fun saveButtonClicked(view: View){

        val adressname = binding.adressText.text.toString()
        val date = binding.dateText.text.toString()

        if(selectedBitmap != null){
            val smallBitmap =makeSmallerBitmap(selectedBitmap!!, 300)

            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val byteArray = outputStream.toByteArray()


            try {
                database.execSQL("CREATE TABLE IF NOT EXISTS realestate (id INTEGER PRIMARY KEY, adressname VARCHAR, date VARCHAR, image BLOB)")
                val sqlString ="INSERT INTO realestate(adressname,date,image) VALUES(?,?,?)"
                val statement = database.compileStatement(sqlString)
                statement.bindString(1,adressname)
                statement.bindString(2,date)
                statement.bindBlob(3,byteArray)
                statement.execute()



            }catch (e : Exception){
                e.printStackTrace()
            }

            val intent =Intent(this@RealtyActivity, RecyclerActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)

        }


    }

    fun selectImage(view: View){

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                Snackbar.make(view,"Permission need for Gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", View.OnClickListener {

                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)

                }).show()
            }else{

                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }


        } else { // izin verildiyse devam et
            val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)

        }

    }

    fun registerLauncher(){

        activityResultLauncher =registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if(result.resultCode == RESULT_OK){
                val intentFromResult = result.data
                if(intentFromResult != null){
                    val imageData = intentFromResult.data

                    if(imageData != null){
                        try {
                            if(Build.VERSION.SDK_INT >= 28){
                                val source = ImageDecoder.createSource(this@RealtyActivity.contentResolver, imageData)
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            }else {
                                selectedBitmap =MediaStore.Images.Media.getBitmap(contentResolver,imageData)
                            }



                        }catch (e : Exception){
                            e.printStackTrace()
                        }
                    }
                }
            }

        }


        permissionLauncher =registerForActivityResult(ActivityResultContracts.RequestPermission()) {result ->
            if(result){
                val intentToGallery =Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }else{
                Toast.makeText(this@RealtyActivity,"Permission needed!!!", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun makeSmallerBitmap(image: Bitmap, maximumSize : Int) : Bitmap{

        var width = image.width
        var height = image.height
        var bitmapRaito : Double = width.toDouble() / height.toDouble()

        if(bitmapRaito > 1){
            //if ratio is bigger than 1 it's meaning image is landscape
            width = maximumSize
            val scaledHeight = width / bitmapRaito
            height = scaledHeight.toInt()

        }else {
            //image is portrait
            height = maximumSize
            val scaledWidth = height * bitmapRaito
            width = scaledWidth.toInt()
        }

        return Bitmap.createScaledBitmap(image,width, height, true)
    }
    
    
     override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(applicationContext,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
    
    
}
