package com.example.listview

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.clans.fab.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import java.lang.Thread.sleep
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {


    var arrayList = ArrayList<String>();
    private var imageUri: Uri? = null
    private val SD_APP_DIR_NAME = Environment.getExternalStorageDirectory().getAbsolutePath(); //存储程序在外部SD卡上的根目录的名字
    private val SHARE_DIR_NAME = "/shareFiles/";    //存储照片在根目录下的文件夹名字
    private val TAG = "lslsls"
    private var temppath:String = ""
    companion object {


        const val RESULT_LOAD_IMAGE = 3//选择图片
        const val RESULT_TAKE_PHOTO = 4//拍照
        const val RESULT_SELECT_FILE = 5//选择文件
        const val HOST = "192.168.1.103"
        const val PORT = 5001
        const val UPLOAD = 1
        const val DOWNLOAD = 2
        const val GETLIST = 0
        const val SIZE = 1024
    }

    private var mBytes = ByteArray(SIZE)
    private val FLAG = "success"

    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        CreateFile(SD_APP_DIR_NAME+SHARE_DIR_NAME+"init.txt")
        getPath()
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        initView()
//        initData()


        val butPhoto: FloatingActionButton = fab12
        butPhoto.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                Log.v(TAG, "打开相册")
                var intent: Intent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
//                getList()
            }
        })

        val butCamera: FloatingActionButton = fab22
        butCamera.setOnClickListener {
            val currentapiVersion = android.os.Build.VERSION.SDK_INT
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            if (hasSdcard()) {
                val simpleDateFormat = SimpleDateFormat("yyyy_MM_dd_mm_ss")
                val fileName = simpleDateFormat.format(Date())
                temppath = SD_APP_DIR_NAME+SHARE_DIR_NAME+fileName + ".jpg"
                val tempFile = File(SD_APP_DIR_NAME+SHARE_DIR_NAME, fileName + ".jpg")
                if (currentapiVersion < 24) {
                    //从文件中创建uri
                    imageUri = Uri.fromFile(tempFile)
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                } else {
                    //兼容android7.0 使用共享文件的形式
                    val contentValues = ContentValues(1)
                    contentValues.put(MediaStore.Images.Media.DATA, tempFile.absolutePath)
                    //检查是否有存储权限，以免崩溃
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        //申请WRITE_EXTERNAL_STORAGE权限
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            0
                        )

                        Toast.makeText(this, "请开启存储权限", Toast.LENGTH_SHORT).show()
                    }

                    imageUri = contentResolver.insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        contentValues
                    )
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                }
            }
            Log.v(TAG, "打开相机")


            // 开启一个带有返回值的Activity，请求码为PHOTO_REQUEST_CAREMA
            startActivityForResult(intent, RESULT_TAKE_PHOTO);
        }
        val butFile: FloatingActionButton = fab32
        butFile.setOnClickListener {
            Log.v(TAG, "资源管理器")

            val intent = Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT)

            startActivityForResult(
                Intent.createChooser(intent, "Select a file"),
                RESULT_SELECT_FILE
            )
        }
        val butFlash: FloatingActionButton = fab42
        butFlash.setOnClickListener(){
            Log.e(TAG,"Flash")
//            arrayList = ArrayList<String>()
//            getPath()
//            println(arrayList)
//            initView()
            reFresh()
        }

//        val adapter = ArrayAdapter(
//            this,
//            R.layout.list_view, array
//        )
//        listview_1.setAdapter(adapter)
//        listview_1.onItemClickListener(){
//
//        }
    }


    private fun initData() {
        var i: Int = 1;
        var end: Int = 100;
        do {
            arrayList.add("我是第 $i 条数据 ")
            i++
        } while (i < end);

    }

    @ExperimentalStdlibApi
    private fun initView() {
        val myAdapter = MyAdapter(arrayList, this);
        listview_1.adapter = myAdapter

        listview_1.onItemClickListener = OnItemClickListener { parent, view, position, id ->


            downloadFile(arrayList[position])
            Toast.makeText(this,arrayList[position]+"has download in /sharefile", Toast.LENGTH_SHORT).show()
    //            val socket = Socket("0.0.0.0", 5001)


        }
    }

    /*
    * 判断sdcard是否被挂载
    */
    fun hasSdcard(): Boolean {
        return Environment.getExternalStorageState().equals(
            Environment.MEDIA_MOUNTED
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
            }
            else -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    0
                )
            }
        }
    }

    @ExperimentalStdlibApi
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)



        when (requestCode) {
            RESULT_LOAD_IMAGE -> {
                Log.v(TAG, "result")

                val slesctedImage = data!!.data
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

                val cursor = slesctedImage?.let {
                    contentResolver.query(
                        it,
                        filePathColumn, null, null, null
                    )
                }
                cursor?.moveToFirst()
                val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
                val picturePath = columnIndex?.let { cursor?.getString(it) }
                cursor?.close()

                uploadFile(picturePath.toString())
//                reFreash()
                

//                println(picturePath)
            }
            RESULT_TAKE_PHOTO -> {
                Log.e(TAG, "RESULT_TAKE_PHOTO")
                val imageUri = imageUri
                println(temppath)
                uploadFile(temppath)
//                reFreash()
                Log.e(TAG, imageUri.toString())
            }
            RESULT_SELECT_FILE -> {
                Log.v(TAG, "RESULT_TAKE_PHOTO")
                val slesctedFile = data!!.data
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

                val cursor = slesctedFile?.let {
                    contentResolver.query(
                        it,
                        filePathColumn, null, null, null
                    )
                }
                cursor?.moveToFirst()
                val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
                val picturePath = columnIndex?.let { cursor?.getString(it) }
                cursor?.close()
                uploadFile(picturePath.toString())
//                reFreash()
                println(picturePath)
            }
            else -> {
                Log.e(TAG, "123")
            }
        }
    }


    @ExperimentalStdlibApi
    private fun getPath() {
        Thread {
            try {
                Socket(HOST, PORT).apply {
                    getOutputStream().write(preProcess(GETLIST.toString()).encodeToByteArray())

                    getInputStream().read(mBytes)
                    var filenames = indexFlag(String(mBytes))
                    println(indexFlag(String(mBytes)))
                    var file_list = filenames.substring(1,filenames.length-1).split(", ")
                    for(i in file_list){
                        arrayList.add(i)
                    }
//                    Log.e(TAG,arrayList.toString())


                    close()
                }
            } catch (e: Exception) {
                Log.v(TAG, e.toString())
            }
        }.start()
    }

    @ExperimentalStdlibApi
    private fun uploadFile(Data: String) {
        Thread {
            try {
                Socket(HOST, PORT).apply {
                    getOutputStream().write(preProcess(UPLOAD.toString()).encodeToByteArray())
                    getOutputStream().write(preProcess(Data).encodeToByteArray())
                    getInputStream().read(mBytes)
                    println(indexFlag(String(mBytes)))
                    BufferedOutputStream(getOutputStream()).use { bos ->
                        FileInputStream(Data).use { fis ->
                            fis.copyTo(bos)
                            println("上传成功！")
                        }
                    }

                    close()
                }
            } catch (e: Exception) {
                Log.v(TAG, e.toString())
            }
        }.start()
    }

    @ExperimentalStdlibApi
    private fun downloadFile(fileName: String) {
        Thread {
            try {
                Socket(HOST, PORT).apply {
                    getOutputStream().write(preProcess(DOWNLOAD.toString()).encodeToByteArray())
                    getOutputStream().write(preProcess(fileName).encodeToByteArray())

                    getInputStream().read(mBytes)
                    println(indexFlag(String(mBytes)))

                    BufferedInputStream(getInputStream()).use { bis ->
                        FileOutputStream(SD_APP_DIR_NAME+SHARE_DIR_NAME+fileName).use { fos ->
                            bis.copyTo(fos)
                            println("接收完成！")


                        }
                    }


                    close()

                }
            } catch (e: Exception) {
                Log.v(TAG, e.toString())
            }
        }.start()
    }


    private fun indexFlag(string: String): String {
        var string = string
        string = string.substring(0, string.indexOf("|"))
        return string
    }


    private fun preProcess(string: String): String {
        var string: String = string
        string += "|"
        return string
    }
    public fun CreateFile(filePath:String): Int {
        var file:File = File(filePath);
        if (file.exists()) {
            Log.e(TAG,"The file [ " + filePath + " ] has already exists");
            return 1;
        }
        if (filePath.endsWith(File.separator)) {// 以 路径分隔符 结束，说明是文件夹
            Log.e(TAG,"The file [ " + filePath + " ] can not be a directory");
            return 1;
        }

        //判断父目录是否存在
        if (!file.getParentFile().exists()) {
            //父目录不存在 创建父目录
            Log.d(TAG,"creating parent directory...");
            if (!file.getParentFile().mkdirs()) {
                Log.e(TAG,"created parent directory failed.");
                return 0;
            }
        }

        //创建目标文件
        try {
            if (file.createNewFile()) {//创建文件成功
                Log.i(TAG, "create file [ " + filePath + " ] success");
                return 1;
            }
        } catch (e:IOException) {
            e.printStackTrace();
            Log.e(TAG,"create file [ " + filePath + " ] failed");
            return 0;
        }

        return 0;
    }
    @ExperimentalStdlibApi
    private fun reFresh(){
        Log.v("asd",arrayList.toString())
        arrayList.clear()
        getPath()
        sleep(600)
        (listview_1.adapter as MyAdapter).refresh(arrayList)

    }

}


