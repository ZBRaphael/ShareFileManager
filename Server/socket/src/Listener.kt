package com.example.listview

import java.io.*
import java.net.ServerSocket

@ExperimentalStdlibApi
class Listener(host: Int) {
    private val server = ServerSocket(host)
    private val bts = ByteArray(1024)
    private val serverAddress = "0.0.0.0"
    private val dirPath = "sharefile/"

    init {
        Thread {
            while (true) {
                println("[*] Listening as {$serverAddress}:{$host}")
                server.accept().apply {
                    println("[+] {$inetAddress} is connected.")
                    getInputStream().read(bts)
                    var type = indexFlag(String(bts))
                    when(type){
                        "0"->{
                            val fileNames: MutableList<String> = mutableListOf()
                            //在该目录下走一圈，得到文件目录树结构
                            val fileTree: FileTreeWalk = File(dirPath).walk()
                            fileTree.maxDepth(1) //需遍历的目录层次为1，即无须检查子目录
                                    .filter { it.isFile } //只挑选文件，不处理文件夹
                                    .filter { !it.isHidden }
//        .filter { it.extension == "txt"  } //选择扩展名为txt的文本文件

                                    .forEach { fileNames.add(it.name) }//循环 处理符合条件的文件
                            fileNames.forEach(::println)
                            getOutputStream().write(preProcess(fileNames.toString()).encodeToByteArray())
                        }
                        "1"->{
                            getInputStream().read(bts)
                            var fileName = indexFlag(String(bts))
                            println(fileName)
                            getOutputStream().write(preProcess(fileName).encodeToByteArray())
                            fileName = fileName.substring(fileName.lastIndexOf("/")+1)
                            println(fileName)
                            BufferedInputStream(getInputStream()).use { bis ->
                                FileOutputStream(dirPath+fileName).use { fos ->
                                    bis.copyTo(fos)
                                    println("接收完成！")
                                }
                            }
                        }
                        "2"->{
                            getInputStream().read(bts)
                            var fileName = dirPath+indexFlag(String(bts))
                            println(fileName)
                            getOutputStream().write(preProcess(fileName).encodeToByteArray())
                            try {
                                BufferedOutputStream(getOutputStream()).use { bos ->
                                    FileInputStream(fileName).use { fis ->
                                        println(fis)

                                        fis.copyTo(bos)
                                        println("上传成功！")
                                    }
                                }
                            }catch (e:Exception){
                                println(e)
                            }


                        }

                    }

                }
            }
        }.start()
    }
    /*
    返回去标志位后的字符串
     */
    private fun indexFlag(string: String): String {
        var string = string
        string = string.substring(0, string.indexOf("|"))
        return string
    }

    /*
    给字符串加标志位
     */
    private fun preProcess(string: String): String {
        var string: String = string
        string += "|"
        return string
    }

}
