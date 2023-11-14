package com.example.opsc_birdapp

import android.graphics.Bitmap
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso


class DisplayAdapter(private val birdList: ArrayList<BirdsData>) : RecyclerView.Adapter<DisplayAdapter.MyViewHolder>() {
    private lateinit var auth: FirebaseAuth
    val storage = FirebaseStorage.getInstance()
    val storageRef: StorageReference = storage.reference

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvSpecies: TextView = itemView.findViewById(R.id.tvSpecies)
        val imageBird: ImageView = itemView.findViewById(R.id.bird_Image)
        val tvLocation: TextView = itemView.findViewById(R.id.tvLocation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.lis_birds, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        auth = FirebaseAuth.getInstance()
        val currentuser = auth.currentUser?.uid
        val species = birdList[position].species

        holder.tvName.text = birdList[position].name
        holder.tvSpecies.text = species
        holder.tvLocation.text = birdList[position].location

        val imageRef = storageRef.child("images/${currentuser.toString()}/${species.toString()}")

        imageRef.downloadUrl
            .addOnSuccessListener { uri ->
                println("success $uri")
                  Picasso.get().load(uri).into(holder.imageBird)
            }
            .addOnFailureListener {

            }
    }



    override fun getItemCount(): Int {
        return birdList.size
    }
}


//-------------------------------------ooo000EndOfFile000ooo----------------------------------------