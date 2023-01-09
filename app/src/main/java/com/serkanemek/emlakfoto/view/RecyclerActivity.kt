package com.serkanemek.emlakfoto.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.serkanemek.emlakfoto.R
import com.serkanemek.emlakfoto.model.RealtyModule
import com.serkanemek.emlakfoto.adapter.RealtyAdapter
import com.serkanemek.emlakfoto.databinding.ActivityRecyclerBinding

class RecyclerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecyclerBinding
    private lateinit var realtyList : ArrayList<RealtyModule>
    private lateinit var realtyAdapter: RealtyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecyclerBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        realtyList = ArrayList<RealtyModule>()

        realtyAdapter = RealtyAdapter(realtyList)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = realtyAdapter


        try { //We showing data in recyclerview

            val database = this.openOrCreateDatabase("RealEstate", MODE_PRIVATE,null)

            val cursor = database.rawQuery("SELECT * FROM realestate" , null)
            val adressNameIx = cursor.getColumnIndex("adressname")
            val idIx = cursor.getColumnIndex("id")

            while (cursor.moveToNext()){
                val adressName =cursor.getString(adressNameIx)
                val id = cursor.getInt(idIx)
                val artForRecycler = RealtyModule(adressName,id)

                realtyList.add(artForRecycler)
            }

            realtyAdapter.notifyDataSetChanged()
            cursor.close()


        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = getMenuInflater()
        menuInflater.inflate(R.menu.art_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId == R.id.add_art_item){
            val intent = Intent(this@RecyclerActivity, RealtyActivity::class.java)
            intent.putExtra("info", "new")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    
      override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(applicationContext,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

}
