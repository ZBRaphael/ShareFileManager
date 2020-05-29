package com.example.listview

import android.app.Activity
import android.app.PendingIntent.getActivity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Result : AppCompatActivity() {

    companion object {
        const val RESULT_LOAD_IMAGE = 3//选择图片
        const val RESULT_TAKE_PHOTO = 4//拍照
        const val RESULT_SELECT_FILE = 5//选择文件
    }

    public override fun onActivityResult(
        requestCode: Int, resultCode: Int,
        resultData: Intent?
    ) {


        super.onActivityResult(requestCode, resultCode, resultData)


        var currentUri: Uri? = null



        if (resultCode == Activity.RESULT_OK) {
            Log.v("lslsls","result")

            if (requestCode == RESULT_LOAD_IMAGE) {

                if (resultData != null) {

                    Toast.makeText(this, "LOAD_IMAGE", Toast.LENGTH_SHORT)
                        .show()

                }

            } else if (requestCode == RESULT_TAKE_PHOTO) {

                resultData?.let { it ->

                    currentUri = it.data

                    currentUri?.let {
                        Toast.makeText(this, getPath(it), Toast.LENGTH_SHORT)
                            .show()


                    }

                }

            } else if (requestCode == RESULT_SELECT_FILE) {
                resultData?.let { it ->

                    currentUri = it.data

                    currentUri?.let {
                        Toast.makeText(this, getPath(it), Toast.LENGTH_SHORT)
                            .show()


                    }
                }
            }

        }

    }

    private fun getPath(uri: Uri): String? {
        if ("file" == uri.scheme) {
            return uri.path;
        }
        return null;
    }

}


