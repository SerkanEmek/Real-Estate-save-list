package com.serkanemek.emlakfoto.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.serkanemek.emlakfoto.view.RealtyActivity
import com.serkanemek.emlakfoto.model.RealtyModule
import com.serkanemek.emlakfoto.databinding.RecyclerRowBinding


class RealtyAdapter(val realtyList : ArrayList<RealtyModule>) : RecyclerView.Adapter<RealtyAdapter.RealtyHolder>() {

    class RealtyHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RealtyHolder {
        val binding =RecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RealtyHolder(binding)
    }

    override fun onBindViewHolder(holder: RealtyHolder, position: Int) {
        holder.binding.recyclerViewTextView.text = realtyList.get(position).name
        holder.itemView.setOnClickListener {
            val intent =Intent(holder.itemView.context, RealtyActivity::class.java)

            intent.putExtra("info", "old")
            intent.putExtra("id", realtyList.get(position).id)

            holder.itemView.context.startActivity(intent)

        }
    }

    override fun getItemCount(): Int {
        return realtyList.size
    }
}