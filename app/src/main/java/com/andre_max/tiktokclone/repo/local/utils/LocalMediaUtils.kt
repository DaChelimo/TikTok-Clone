package com.andre_max.tiktokclone.repo.local.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import java.io.File

//
//import android.annotation.SuppressLint
//import android.content.ContentUris
//import android.content.Context
//import android.database.Cursor
//import android.net.Uri
//import android.os.Build
//import android.os.Environment
//import android.provider.DocumentsContract
//import android.provider.MediaStore
//import androidx.annotation.RequiresApi
//import androidx.core.net.toUri
//import androidx.loader.content.CursorLoader
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import timber.log.Timber
//import java.io.File
//
///**
// * Gets the actual file:// path of the file from the Uri. This function creates the cursor, performs a query using the id and returns
// * the file path.
// *
// * @param contentUri content uri of the file
// */
//suspend fun Context.getRealPathFromURI(contentUri: Uri): String? = withContext(Dispatchers.IO) {
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//        getRealPathFromURI_Above19(this@getRealPathFromURI, contentUri)
//    } else {
//        getRealPathFromURI_API11to18(this@getRealPathFromURI, contentUri)
//    }
//    return@withContext null
//}
//
///**
// * Gets the actual file:// path of the file from the Cursor. Since the cursor is on the current file, we only need to
// * retrieve the file path using DATA or RELATIVE_PATH
// */
//@Suppress("DEPRECATION")
//suspend fun Cursor.getRealPath(context: Context): String? =
//    when {
//        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
//            val uri = getString(getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH)).toUri()
//            Timber.d("LocalMediaRepo: uri is $uri")
//            getRealPathFromURI_Above19(context, uri)
//        }
//        Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
//            val uri = getString(getColumnIndex(MediaStore.MediaColumns.DATA)).toUri()
//            Timber.d("LocalMediaRepo: uri is $uri")
//            getRealPathFromURI_Above19(context, uri)
//        }
//        else -> {
//            getString(getColumnIndex(MediaStore.Images.Media.DATA))
//        }
//
//    }
//
//
//fun getDataColumn(
//    context: Context, uri: Uri?, selection: String?,
//    selectionArgs: Array<String>?
//): String? {
//    var cursor: Cursor? = null
//    val column = getFilePathColumn()
//    val projection = arrayOf(
//        column
//    )
//    try {
//        if (uri == null) return null
//        cursor = context.contentResolver.query(
//            uri, projection, selection, selectionArgs,
//            null
//        )
//        if (cursor != null && cursor.moveToFirst()) {
//            val index = cursor.getColumnIndexOrThrow(column)
//            return cursor.getString(index)
//        }
//    } finally {
//        cursor?.close()
//    }
//    return null
//}
//
//
//fun getFilePathForDownloadsDocument(context: Context, uri: Uri?): String? {
//    var cursor: Cursor? = null
//    val projection = arrayOf(
//        MediaStore.MediaColumns.DISPLAY_NAME
//    )
//    try {
//        if (uri == null) return null
//        cursor = context.contentResolver.query(
//            uri, projection, null, null,
//            null
//        )
//        if (cursor != null && cursor.moveToFirst()) {
//            val index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
//            return cursor.getString(index)
//        }
//    } finally {
//        cursor?.close()
//    }
//    return null
//}
//
///**
// * @param uri The Uri to check.
// * @return Whether the Uri authority is ExternalStorageProvider.
// */
//fun isExternalStorageDocument(uri: Uri): Boolean {
//    return "com.android.externalstorage.documents" == uri.authority
//}
//
///**
// * @param uri The Uri to check.
// * @return Whether the Uri authority is DownloadsProvider.
// */
//fun isDownloadsDocument(uri: Uri): Boolean {
//    return "com.android.providers.downloads.documents" == uri.authority
//}
//
///**
// * @param uri The Uri to check.
// * @return Whether the Uri authority is MediaProvider.
// */
//fun isMediaDocument(uri: Uri): Boolean {
//    return "com.android.providers.media.documents" == uri.authority
//}
//
///**
// * @param uri The Uri to check.
// * @return Whether the Uri authority is Google Photos.
// */
//fun isGooglePhotosUri(uri: Uri): Boolean {
//    return "com.google.android.apps.photos.content" == uri.authority
//}
//
///**
// * @return The column representing the file path based on the Android Version.
// */
//@Suppress("DEPRECATION")
//fun getFilePathColumn() =
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
//                MediaStore.Images.Media.RELATIVE_PATH
//            else
//                MediaStore.Images.Media.DATA
//
//
//@SuppressLint("NewApi")
//fun getRealPathFromURI_API11to18(context: Context, contentUri: Uri): String? {
//    val proj = arrayOf(MediaStore.Images.Media.DATA)
//    var result: String? = null
//    val cursorLoader = CursorLoader(
//        context,
//        contentUri, proj, null, null, null
//    )
//    val cursor: Cursor? = cursorLoader.loadInBackground()
//    cursor?.let {
//        val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
//        cursor.moveToFirst()
//        result = cursor.getString(column_index)
//    }
//    return result
//}
//
//@RequiresApi(Build.VERSION_CODES.KITKAT)
//suspend fun getRealPathFromURI_Above19(context: Context, uriToChange: Uri) = withContext(Dispatchers.IO){
//    when {
//        // DocumentProvider
//        DocumentsContract.isDocumentUri(context, uriToChange) -> {
//            when {
//                // ExternalStorageProvider
//                isExternalStorageDocument(uriToChange) -> {
//                    val docId = DocumentsContract.getDocumentId(uriToChange)
//                    val split = docId.split(":").toTypedArray()
//                    val type = split[0]
//                    // This is for checking Main Memory
//                    return@withContext if ("primary".equals(type, ignoreCase = true)) {
//                        if (split.size > 1) {
//                            Environment.getExternalStorageDirectory().toString() + "/" + split[1]
//                        } else {
//                            Environment.getExternalStorageDirectory().toString() + "/"
//                        }
//                        // This is for checking SD Card
//                    } else {
//                        "storage" + "/" + docId.replace(":", "/")
//                    }
//                }
//                isDownloadsDocument(uriToChange) -> {
//                    val fileName = getFilePathForDownloadsDocument(context, uriToChange)
//                    if (fileName != null) {
//                        return@withContext Environment.getExternalStorageDirectory()
//                            .toString() + "/Download/" + fileName
//                    }
//                    var id = DocumentsContract.getDocumentId(uriToChange)
//                    if (id.startsWith("raw:")) {
//                        id = id.replaceFirst("raw:".toRegex(), "")
//                        val file = File(id)
//                        if (file.exists()) return@withContext id
//                    }
//                    val contentUri = ContentUris.withAppendedId(
//                        Uri.parse("content://downloads/public_downloads"),
//                        java.lang.Long.valueOf(id)
//                    )
//                    return@withContext getDataColumn(
//                        context,
//                        contentUri,
//                        null,
//                        null
//                    )
//                }
//                isMediaDocument(uriToChange) -> {
//                    val docId = DocumentsContract.getDocumentId(uriToChange)
//                    val split = docId.split(":").toTypedArray()
//                    val type = split[0]
//                    var contentUri: Uri? = null
//                    when (type) {
//                        "image" -> {
//                            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//                        }
//                        "video" -> {
//                            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
//                        }
//                        "audio" -> {
//                            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
//                        }
//                    }
//                    val selection = "_id=?"
//                    val selectionArgs = arrayOf(split[1])
//                    return@withContext getDataColumn(
//                        context,
//                        contentUri,
//                        selection,
//                        selectionArgs
//                    )
//                }
//                else -> return@withContext null
//            }
//        }
//        "content".equals(uriToChange.scheme, ignoreCase = true) -> {
//            // Return the remote address
//            return@withContext if (isGooglePhotosUri(uriToChange)) uriToChange.lastPathSegment else getDataColumn(
//                context,
//                uriToChange,
//                null,
//                null
//            )
//        }
//        "file".equals(uriToChange.scheme, ignoreCase = true) -> {
//            return@withContext uriToChange.path
//        }
//        else -> return@withContext null
//    }
//}

@RequiresApi(Build.VERSION_CODES.KITKAT)
fun getRealPathFromURI(context: Context, uri: Uri): String? {
    when {
        // DocumentProvider
        DocumentsContract.isDocumentUri(context, uri) -> {
            when {
                // ExternalStorageProvider
                isExternalStorageDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    // This is for checking Main Memory
                    return if ("primary".equals(type, ignoreCase = true)) {
                        if (split.size > 1) {
                            Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                        } else {
                            Environment.getExternalStorageDirectory().toString() + "/"
                        }
                        // This is for checking SD Card
                    } else {
                        "storage" + "/" + docId.replace(":", "/")
                    }
                }
                isDownloadsDocument(uri) -> {
                    val fileName = getFilePath(context, uri)
                    if (fileName != null) {
                        return Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName
                    }
                    var id = DocumentsContract.getDocumentId(uri)
                    if (id.startsWith("raw:")) {
                        id = id.replaceFirst("raw:".toRegex(), "")
                        val file = File(id)
                        if (file.exists()) return id
                    }
                    val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))
                    return getDataColumn(context, contentUri, null, null)
                }
                isMediaDocument(uri) -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    var contentUri: Uri? = null
                    when (type) {
                        "image" -> {
                            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        }
                        "video" -> {
                            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        }
                        "audio" -> {
                            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        }
                    }
                    val selection = "_id=?"
                    val selectionArgs = arrayOf(split[1])
                    return getDataColumn(context, contentUri, selection, selectionArgs)
                }
            }
        }
        "content".equals(uri.scheme, ignoreCase = true) -> {
            // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(context, uri, null, null)
        }
        "file".equals(uri.scheme, ignoreCase = true) -> {
            return uri.path
        }
    }
    return null
}

fun getDataColumn(context: Context, uri: Uri?, selection: String?,
                  selectionArgs: Array<String>?): String? {
    var cursor: Cursor? = null
    val column = "_data"
    val projection = arrayOf(
        column
    )
    try {
        if (uri == null) return null
        cursor = context.contentResolver.query(uri, projection, selection, selectionArgs,
            null)
        if (cursor != null && cursor.moveToFirst()) {
            val index = cursor.getColumnIndexOrThrow(column)
            return cursor.getString(index)
        }
    } finally {
        cursor?.close()
    }
    return null
}


fun getFilePath(context: Context, uri: Uri?): String? {
    var cursor: Cursor? = null
    val projection = arrayOf(
        MediaStore.MediaColumns.DISPLAY_NAME
    )
    try {
        if (uri == null) return null
        cursor = context.contentResolver.query(uri, projection, null, null,
            null)
        if (cursor != null && cursor.moveToFirst()) {
            val index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            return cursor.getString(index)
        }
    } finally {
        cursor?.close()
    }
    return null
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is ExternalStorageProvider.
 */
fun isExternalStorageDocument(uri: Uri): Boolean {
    return "com.android.externalstorage.documents" == uri.authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is DownloadsProvider.
 */
fun isDownloadsDocument(uri: Uri): Boolean {
    return "com.android.providers.downloads.documents" == uri.authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is MediaProvider.
 */
fun isMediaDocument(uri: Uri): Boolean {
    return "com.android.providers.media.documents" == uri.authority
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is Google Photos.
 */
fun isGooglePhotosUri(uri: Uri): Boolean {
    return "com.google.android.apps.photos.content" == uri.authority
}