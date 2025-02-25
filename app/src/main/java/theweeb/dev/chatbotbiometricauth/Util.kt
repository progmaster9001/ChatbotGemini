package theweeb.dev.chatbotbiometricauth

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.InputStream

fun uriToBitmap(contentResolver: ContentResolver, uri: Uri): Bitmap? {
    return try {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun isValidImageFormat(uri: Uri): Boolean {
    val mimeType = uri.toString().substringAfterLast('.')
    return mimeType.equals("png", true) || mimeType.equals("jpg", true) || mimeType.equals("jpeg", true)
}

fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
    val stream = java.io.ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}