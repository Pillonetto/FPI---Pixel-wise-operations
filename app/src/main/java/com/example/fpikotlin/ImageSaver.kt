package com.example.fpikotlin

import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import java.io.ByteArrayOutputStream


class ImageSaver(val activity: MainActivity) {
    val CREATE_FILE = 1

    var bitmap: Bitmap?  = null

    fun saveImage(finalBitmap: Bitmap?) {
        if (finalBitmap == null) {
            return
        }
        this.bitmap = finalBitmap
        val intent: Intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE).setType("image/jpg")
            .putExtra(Intent.EXTRA_TITLE, "image${System.currentTimeMillis().toInt()}.jpeg")
        activity.startActivityForResult(intent, CREATE_FILE)
    }

    fun onCreateFileActivityResult(uri: Uri?, contentResolver: ContentResolver) {
        if(this.bitmap == null) {
            Log.d("DEBUG", "bitmap is null")
            return
        }
        val byteOS = ByteArrayOutputStream()
        this.bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, byteOS)
        val byteArray = byteOS.toByteArray()

        val os = contentResolver.openOutputStream(uri!!)
        try {
            if (os != null) {
                os.write(byteArray)
                os.close()
                return
            }
        } catch (e: Error) { }
    }
}