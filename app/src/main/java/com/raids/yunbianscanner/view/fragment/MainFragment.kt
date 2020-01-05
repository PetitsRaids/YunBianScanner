package com.raids.yunbianscanner.view.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri

import com.raids.yunbianscanner.R
import com.raids.yunbianscanner.utils.MyUtils
import com.yalantis.ucrop.UCrop
import java.io.File

class MainFragment : Fragment() {

    private lateinit var takePhoto: Button
    private lateinit var scanPhoto: ImageView
    private lateinit var popupWindow: PopupWindow
    private lateinit var photo: File
    private lateinit var cameraUri: Uri
    private lateinit var destinationUri: Uri

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        takePhoto = view!!.findViewById(R.id.take_photo)
        takePhoto.setOnClickListener {
            requestPermission()
        }
        scanPhoto = view!!.findViewById(R.id.scan_photo)
        val temPath = File(context?.externalCacheDir, "bar_code")
        Log.d(MyUtils.TAG, temPath.absolutePath)

        photo = File(temPath, "scan_photo.jpg")
        photo.parentFile?.mkdirs()
        Log.d(MyUtils.TAG, photo.absolutePath)
        if (photo.exists()) {
            photo.delete()
        }
        photo.createNewFile()
        cameraUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context!!, MyUtils.PACKAGE_NAME, photo)
        } else {
            Uri.parse(photo.path)
        }
        destinationUri = Uri.EMPTY
        Log.d(MyUtils.TAG, cameraUri.toString())
    }

    private fun popupWindow() {
        val contentView =
            layoutInflater.inflate(resources.getLayout(R.layout.popup_choose), view as ViewGroup)
        popupWindow = PopupWindow(
            contentView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val collection: ImageButton = contentView.findViewById(R.id.collection_choose)
        val camera: ImageButton = contentView.findViewById(R.id.camera_choose)
        collection.setOnClickListener {
            openCollections()
        }
        camera.setOnClickListener {
            openCamera()
        }
//        popupWindow.showAtLocation(contentView, Gravity.BOTTOM, 0, 0)
    }

    private fun openCollections() {
        popupWindow.dismiss()
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, MyUtils.ALBUM_REQUEST_CODE)
    }

    private fun openCamera() {
        popupWindow.dismiss()
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri)
        startActivityForResult(intent, MyUtils.CAMERA_REQUEST_CODE)
    }

    private fun crop(source: Uri, destination: Uri) {
        UCrop.of(source, destination).start(context!!, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(MyUtils.TAG, "RequestCode is $requestCode.")
        if (resultCode == MyUtils.RESULT_OK) {
            when (requestCode) {
                MyUtils.CAMERA_REQUEST_CODE -> {
                    // 传入photo.toUri()
                    crop(photo.toUri(), photo.toUri())
                }
                MyUtils.ALBUM_REQUEST_CODE -> {
                    if (data?.data != null) {
                        crop(data.data!!, photo.toUri())
                    } else {
                        Toast.makeText(context!!, R.string.decode_fail, Toast.LENGTH_SHORT).show()
                    }
                }
                UCrop.REQUEST_CROP -> {
                    val cropUri = data?.let { UCrop.getOutput(it) }
                    cropUri?.let { showImage(it) }
                }
            }
        } else {
            when(requestCode){
                MyUtils.CAMERA_REQUEST_CODE ->{
                    Log.d(MyUtils.TAG, "CAMERA ERROR")
                }
                UCrop.REQUEST_CROP -> {
                    val throwable = data?.let { UCrop.getError(it) }
                    Log.d(MyUtils.TAG, throwable.toString())
                }
            }
        }
    }

    private fun showImage(uri: Uri) {
        val bitmap = BitmapFactory.decodeStream(context?.contentResolver?.openInputStream(uri))
        scanPhoto.setImageBitmap(bitmap)
    }

    private fun requestPermission() {
        val hasPermission = ContextCompat.checkSelfPermission(context!!, Manifest.permission.CAMERA)
        if (hasPermission == PackageManager.PERMISSION_GRANTED) {
            popupWindow()
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                MyUtils.PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MyUtils.PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, R.string.permission_granted, Toast.LENGTH_SHORT).show()
                openCamera()
            }
        }
    }
}
