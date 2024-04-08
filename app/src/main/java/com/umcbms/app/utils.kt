package com.umcbms.app

import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.StrictMode
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.google.gson.Gson
import com.google.gson.JsonParser
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.text.InputFilter
import android.widget.EditText
import java.time.LocalDate
import java.time.Period

import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.TypedValue
import com.google.gson.JsonParseException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val INSERT = "insert"
const val UPDATE = "update"

fun changeDateTimeFormat(
    inputDate: String,
    inputDateFormat: String = "yyyy-MM-dd HH:mm:ss",
    outputDateFormat: String
): String {
    if (inputDate.isNullOrBlank()) {
        return ""
    }
    val inputFormat = SimpleDateFormat(inputDateFormat, Locale("en", "EN"))
    val outputFormat = SimpleDateFormat(outputDateFormat, Locale("en", "EN"))

    val date = inputFormat.parse(inputDate)

    val formattedDate = outputFormat.format(date)
    return formattedDate
}

@RequiresApi(Build.VERSION_CODES.O)
fun getCurrentDateTime(): String {
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return currentDateTime.format(formatter)
}

fun validateText(text: String = "", minChar: Int, maxChar: Int): Boolean {
    return text.length in minChar..maxChar
}

fun isValidNumber(input: String, minLimit: Long = 0, maxLimit: Long = 0): Boolean {
    return try {
        val number = input.toLong()
        number in minLimit..maxLimit
    } catch (e: NumberFormatException) {
        false
    }
}


fun isSelectionValid(selectedItem: String): Boolean {
    val multiSelect = false
    val valueRequired = true

    if (multiSelect) {
        return selectedItem.isNotEmpty()
    } else {
        return valueRequired && selectedItem.isNotEmpty()
    }
}

fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun updateJsonValue(jsonString: String, key: String, newValue: String): String {
    val jsonElement = JsonParser.parseString(jsonString)
    if (jsonElement.isJsonObject) {
        val jsonObject = jsonElement.asJsonObject
        jsonObject.addProperty(key, newValue)
        return jsonObject.toString()
    } else {
        // Handle other JSON types if needed
        return jsonString
    }
}

private fun getSharedPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences("SharedPref", Context.MODE_PRIVATE)
}

fun setPrefStringData(context: Context, key: String? = "", value: String? = "") {
    getSharedPreferences(context).edit().putString(key, value).apply()
}

fun getPrefStringData(context: Context, key: String? = ""): String? {
    return getSharedPreferences(context).getString(key, null)
}

fun setPrefIntData(context: Context, key: String? = "", value: Int = 0) {
    getSharedPreferences(context).edit().putInt(key, value).apply()
}

fun getPrefIntData(context: Context, key: String? = ""): Int {
    return getSharedPreferences(context).getInt(key, 0)
}

fun setPrefBooleanData(context: Context, key: String? = "", value: Boolean? = false) {
    if (value != null) {
        getSharedPreferences(context).edit().putBoolean(key, value).apply()
    }
}

fun getPrefBooleanData(context: Context, key: String? = ""): Boolean {
    return getSharedPreferences(context).getBoolean(key, false)
}

fun isJsonArray(input: String): Boolean {
    return try {
        Gson().fromJson(input, Any::class.java) is ArrayList<*>
    } catch (e: JsonParseException) {
        false
    }
}
 fun getPathFromUri(context: Context,uri: Uri): String? {
    val projection = arrayOf(MediaStore.Images.Media.DATA)
    val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)
    cursor?.use {
        it.moveToFirst()
        val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        return it.getString(columnIndex)
    }
    return null
}


fun Bitmap.saveImage(context: Context): Uri? {
    // Get the directory for storing images
    val imagesDirectory = File(
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
        "camera"
    )

    // Create the directory if it doesn't exist
    if (!imagesDirectory.exists()) {
        imagesDirectory.mkdirs()
    }

    // Create a file to save the image
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFile = File(imagesDirectory, "IMG_$timeStamp.jpg")

    return try {
        // Write the bitmap data to the file
        val fileOutputStream = FileOutputStream(imageFile)
        this.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()

        // Return the Uri of the saved image file
        Uri.fromFile(imageFile)
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}
fun convertUrlToByteArray(context: Context, url: String): String? {
    var base64: String? = ""
    try {
        val policy: StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        val inputStream = context.contentResolver.openInputStream(url.toUri())
        val file = File(context.cacheDir, "image.jpg")
        file.copyInputStreamToFile(inputStream!!)
        val fileSize = getFileSize(file.path)
        val formattedSize = formatFileSize(fileSize)
        println("File size: $formattedSize")
        var byteArray = compressImage(file.path)
        val formattedSize1 = formatFileSize(byteArray!!.size.toLong())
        println("File size1: $formattedSize1")


        base64 = byteArray.toString()
        Log.d("TAG", "convertUrlToBYTEARRAY: " + byteArray)

    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
    return base64
}
fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
    return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
}
fun pathToBitmap(path: String): Bitmap {
    return BitmapFactory.decodeFile(path)
}
fun isLocalFilePath(path: String): Boolean {
    val file = File(path)
    return file.exists()
   /* val uri = Uri.parse(path)
    return uri.scheme == null || uri.scheme == ContentResolver.SCHEME_CONTENT*/
}
fun getPathToBase64String(path: String): String {
    val file = File(path)
//    val bytes = file.readBytes()
    val fileSize = getFileSize(file.path)
    val formattedSize = formatFileSize(fileSize)
    println("File size: $formattedSize")
    var byteArray = compressImage(file.path)
    val formattedSize1 = formatFileSize(byteArray!!.size.toLong())
    println("File size1: $formattedSize1")
//    return Base64.getEncoder().encodeToString(bytes)
    return Base64.encodeToString(byteArray, Base64.URL_SAFE)
}

fun createFolderInInternalStorage(context: Context?, folderName: String): File {
    // Get the directory specific to your app's package name in the internal storage
    val directory = context?.getDir(folderName, Context.MODE_PRIVATE)

    // Create the folder if it doesn't exist
    val folder = File(directory, folderName)
    Log.e("already existing folder",folder?.path.toString())

    if (!folder.exists()) {
        Log.e("already existing folder",folder.exists().toString())
        folder.mkdirs()
    }
    Log.e("already path existing",folder.path)

    return folder
}

fun deleteLastUploadedImages(context: Context, folderName: String, numberOfImagesToDelete:Int,files:ArrayList<File>) {
    val folder = File(folderName).name
    var length = files.size

    // Delete the last N files (N = numberOfImagesToDelete)
    var deletedCount = 0

    for (i in 0 until numberOfImagesToDelete) {
        if (length > 0) {
            val file = files.removeAt(length - 1)

            // Check if the removed file is not null before attempting deletion
            if (file != null) {
                val deleted = file.delete()

                if (deleted) {

                    Log.e("deleted successfully: ",file.path)
                } else {
                    Log.e("Failed to delete: ${file.absolutePath}",deletedCount.toString())
                }
            } else {
                Log.e("Removed file is null.",deletedCount.toString())
            }
        } else {
            println("List of files is empty.")
        }
        length --
        // Return false if the desired number of images were not deleted
    }

}

fun getAllFilesInFolder(folderPath: String): ArrayList<File> {
    val folder = File(folderPath)
    val filesList = ArrayList<File>()
    Log.e("asdjoj files",folderPath.toString())

    if (folder.exists() && folder.isDirectory) {
        Log.e("fol ed files",folder.toString())
        val files = folder.listFiles()

        if (files != null) {
            Log.e("asdjoj not files",files.toString())

            filesList.addAll(files)
        }
    }
    Log.e("asdjoj out files",filesList.toString())

    return filesList
}
fun saveImageToInternalStorage(context: Context, folderName: String, imageName:String, imageData: ByteArray): File? {

    // folder name should data/time/millisec and  Every form has one folder
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val folder = createFolderInInternalStorage(context, folderName)

    // Create a file to store the image
    val imageFile = File(folder,"${timeStamp}_$imageName")
    return try {

        // Write the image data to the file
        val outputStream = FileOutputStream(imageFile)
        outputStream.write(imageData)
        outputStream.flush()
        outputStream.close()
        imageFile
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

fun Base64ToBitmap(string: String): Bitmap? {
    try {
//        val imageBytes = android.util.Base64.decode(string,Base64.NO_WRAP)
        val compressedData = Base64.decode(string, Base64.URL_SAFE)

        // Decompress the byte array using gzip
        /*  val inputStream = ByteArrayInputStream(compressedData)
          val outputStream = ByteArrayOutputStream()
          GZIPInputStream(inputStream).use { it.copyTo(outputStream) }*/
//        val decompressedData = outputStream.toString(Charsets.UTF_8)
        val image = BitmapFactory.decodeByteArray(compressedData, 0, compressedData.size)
        return image
    } catch (e: java.lang.Exception) {
        return null
    }
}

fun deleteLastUploadedFile(folderPath: String) {
    // Get list of files in the folder
    val folder = File(folderPath)
    val files = folder.listFiles()
    if (files != null && files.isNotEmpty()) {
        // Sort files by last modified time to get the last uploaded file
        val lastUploadedFile = files.maxByOrNull { it.lastModified() }

        // Delete the last uploaded file
        lastUploadedFile?.let {
            val deleted = it.delete()
            if (deleted) {
                println("Last uploaded file deleted successfully.")
            } else {
                println("Failed to delete the last uploaded file.")
            }
        }
    } else {
        println("No files found in the folder.")
    }
}

fun convertUrlToBase64(context: Context, url: String): String? {
    var base64: String? = ""
    try {
        val policy: StrictMode.ThreadPolicy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        val inputStream = context.contentResolver.openInputStream(url.toUri())
        val file = File(context.cacheDir, "image.jpg")
        file.copyInputStreamToFile(inputStream!!)
        val fileSize = getFileSize(file.path)
        val formattedSize = formatFileSize(fileSize)
        println("File size: $formattedSize")
        var byteArray = compressImage(file.path)
        val formattedSize1 = formatFileSize(byteArray!!.size.toLong())
        println("File size1: $formattedSize1")
        /*// Compress the input data using GZIP
        val outputStream = ByteArrayOutputStream()
        val deflater = Deflater(Deflater.BEST_COMPRESSION)
        val deflaterStream = DeflaterOutputStream(outputStream, deflater)
        deflaterStream.write(byteArray)
        deflaterStream.close()*/
        // Compress the byte array using gzip
        /*  val outputStream = ByteArrayOutputStream()
          GZIPOutputStream(outputStream).use { it.write(byteArray) }
          val compressedData = outputStream.toByteArray()*/

        base64 = Base64.encodeToString(byteArray, Base64.URL_SAFE)
        Log.d("TAG", "convertUrlToBase64: " + base64)
        Log.d("TAG", "convertUrlToBYTEARRAY: " + byteArray)
//        base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
//        println("base64String: $base64")
//        base64=byteArray.toString()
//        base64=byteArrayToString(byteArray)
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
    return base64
}

fun File.copyInputStreamToFile(inputStream: InputStream) {
    this.outputStream().use { fileOut ->
        inputStream.copyTo(fileOut)
    }
}

fun getFileSize(filePath: String): Long {
    val file = File(filePath)
    return if (file.exists()) {
        file.length()
    } else {
        0
    }
}

fun getByteArraySizeInKB(byteArray: ByteArray): Double {
    val sizeInBytes = byteArray.size.toDouble()
    return sizeInBytes / 1024 // Convert bytes to kilobytes
}

fun formatFileSize(fileSize: Long): String {
    val kilobyte = 1024
    val megabyte = kilobyte * 1024

    return when {
        fileSize < kilobyte -> "$fileSize B"
        fileSize < megabyte -> "%.2f KB".format(fileSize.toFloat() / kilobyte)
        else -> "%.2f MB".format(fileSize.toFloat() / megabyte)
    }
}

fun compressImage(filePath: String): ByteArray? {
    val maxFileSize = 512 * 1024 // 512 KB in bytes
    var quality = 100 // Initial quality
    var compressedBitmap: Bitmap? = null
    var byteArray: ByteArray? = null

    // Decode the image file into a Bitmap
    val bitmap = BitmapFactory.decodeFile(filePath)

    // Compress the bitmap until its size is less than the maximum file size
    val outputStream = ByteArrayOutputStream()
    do {
        outputStream.reset() // Reset the stream for each compression attempt
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        byteArray = outputStream.toByteArray()

        // Check the size of the compressed image
        if (byteArray.size > maxFileSize) {
            quality -= 10 // Reduce the quality by 10 units
        } else {

            compressedBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        }
    } while (byteArray!!.size > maxFileSize && quality > 0)
    return byteArray
}

fun byteArrayToString(byteArray: ByteArray): String {
    return String(byteArray, Charsets.UTF_8)
}

fun stringToByteArray(str: String): ByteArray {
    return str.toByteArray(Charsets.UTF_8)
}

internal var progressDialog: ProgressDialog? = null

fun showLoader(context: Context, message: String) {
    progressDialog = ProgressDialog(context)
    progressDialog!!.setMessage(message)
    progressDialog!!.setCancelable(false)
    progressDialog!!.setCanceledOnTouchOutside(false)

    if (!progressDialog!!.isShowing)
        progressDialog!!.show()
}

fun hideLoader() {
    if (progressDialog!=null)
    if (progressDialog!!.isShowing)
        progressDialog!!.dismiss()
}
fun setMinMaxValues(editText: EditText, minValue: Int, maxValue: Int) {
    val inputFilter = InputFilter { source, start, end, dest, dstart, dend ->
        val input = (dest.subSequence(0, dstart).toString() + source + dest.subSequence(dend, dest.length)).toIntOrNull()
        if (input == null || (input in minValue..maxValue)) null else ""
    }
    editText.filters = arrayOf(inputFilter)
}

fun setMinMaxValuesLong(editText: EditText, minValue: Long, maxValue: Long) {
    val inputFilter = InputFilter { source, start, end, dest, dstart, dend ->
        val input = (dest.subSequence(0, dstart).toString() + source + dest.subSequence(dend, dest.length)).toIntOrNull()
        if (input == null || (input in minValue..maxValue)) null else ""
    }
    editText.filters = arrayOf(inputFilter)
}

@RequiresApi(Build.VERSION_CODES.O)
fun calculateAge(dob: String): Int {
    val dobDate = LocalDate.parse(dob)
    val currentDate = LocalDate.now()
    val period = Period.between(dobDate, currentDate)
    return period.years
}

fun dpToPx(dp: Int): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()
}
