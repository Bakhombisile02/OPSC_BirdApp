package com.example.opsc_birdapp

import android.graphics.Bitmap
import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ImageDownloader {
    private val storage = FirebaseStorage.getInstance()

    fun downloadImage(imagePath: String): Task<Uri> {
        val storageRef: StorageReference = storage.reference.child(imagePath)
        return storageRef.downloadUrl
    }
}