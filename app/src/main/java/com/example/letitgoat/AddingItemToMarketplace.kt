package com.example.letitgoat

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.*
import com.example.letitgoat.db_models.Item
import com.google.firebase.firestore.FirebaseFirestore
import org.jetbrains.anko.toast
import java.io.ByteArrayOutputStream
import java.lang.NumberFormatException
import java.util.*
import kotlin.collections.ArrayList


class AddingItemToMarketplace : AppCompatActivity() {

    private lateinit var database: FirebaseFirestore
    private var stringsOfBitmapsOfItems: List<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_adding_item_to_marketplace)

        Log.d("check_add_item", (intent.getSerializableExtra("extra_item")==null).toString())

        if (intent.getSerializableExtra("extra_item")!=null) {
            val item = intent.getSerializableExtra("extra_item") as Item
            Log.d("ItemActivity", item.name)

            val img = findViewById<ImageView>(R.id.itemAboutToBeSoldPicture)
            val name = findViewById<TextView>(R.id.itemNameField)
            val price = findViewById<TextView>(R.id.priceField)
            val description = findViewById<TextView>(R.id.descriptionField)

            name.setText(item.name)
            price.setText(item.price.toString())
            description.setText(item.description)

            val encodeByte: ByteArray = Base64.decode(
                item.stringsOfBitmapofPicuresOfItem.get(0),
                Base64.DEFAULT
            )
            val b = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)

            val matrix = Matrix()

            matrix.postRotate(90f)

            val scaledBitmap = Bitmap.createScaledBitmap(b, b.width, b.height, true)

            val rotatedBitmap = Bitmap.createBitmap(
                scaledBitmap,
                0,
                0,
                scaledBitmap.width,
                scaledBitmap.height,
                matrix,
                true
            )

            img.setImageBitmap(
                rotatedBitmap
            )
        }


        database = FirebaseFirestore.getInstance()

        val addingItemToMarketplace = findViewById<Button>(R.id.addToMarketPlaceButton)
        addingItemToMarketplace.setOnClickListener{
            addItemToMarketplace()
        }

        val sellerName = findViewById<TextView>(R.id.sellerName)
        sellerName.text = "Being sold by: ${MainActivity.user.name}"

        val takePhotoButton = findViewById<ImageButton>(R.id.newItemPictureButton)
        takePhotoButton.setOnClickListener{
            dispatchTakePictureIntent()
        }
    }

    val REQUEST_IMAGE_CAPTURE = 1

    private fun dispatchTakePictureIntent() {
        val inten = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(inten, REQUEST_IMAGE_CAPTURE)
    }

    //Camera returns
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data!!.extras!!.get("data") as Bitmap
            val picOfAboutToSellItem = findViewById<ImageView>(R.id.itemAboutToBeSoldPicture)
            picOfAboutToSellItem.setImageBitmap(imageBitmap)
            picOfAboutToSellItem.rotation = 90f

            val baos = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val b = baos.toByteArray()
            val bitmapAsString = Base64.encodeToString(b, Base64.DEFAULT)
            this.stringsOfBitmapsOfItems += bitmapAsString
        }
    }

    private fun addItemToMarketplace(){
        var validInput = true

        val name = findViewById<EditText>(R.id.itemNameField).text.toString()
        var price = -1.0

        try {
            price = findViewById<EditText>(R.id.priceField).text.toString().toDouble()
        } catch (e: NumberFormatException){
            validInput = false
        }

        val user = MainActivity.user
        val description = findViewById<EditText>(R.id.descriptionField).text.toString()
        val currTime = Date()

        if(name == "" || description == ""){
            validInput = false
        }

        if(this.stringsOfBitmapsOfItems.isEmpty()){
            validInput = false
        }

        val item = Item(
            name = name,
            price = price,
            user = user,
            description = description,
            postedTimeStamp = currTime,
            stringsOfBitmapofPicuresOfItem = this.stringsOfBitmapsOfItems
        )

        //Adds single_buy to db
        if (validInput) {
            database.collection("Items").add(item)
            startActivity(Intent(this, Home::class.java))
        } else {
            toast("One or more of the fields is/are invalid")
        }
    }
}
