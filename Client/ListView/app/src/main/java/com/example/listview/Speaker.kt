package com.example.listview

import java.net.Socket

@ExperimentalStdlibApi
class Speaker(host: String = "0.0.0.0", port: Int = 5001, text:String = "你好") {
    private val mHost = host
    private val mPort = port
    private val mBytes = ByteArray(1024)
    private val FLAG = "success"
    @ExperimentalStdlibApi
    private fun once(string: String = "你好") {
        try {

            val socket = Socket(mHost, mPort)

            socket.apply {
//                getOutputStream().write("C".encodeToByteArray())//传输字符串
//                getInputStream().read(mBytes)
//                println("123")
//                println(String(mBytes))
//                println(FLAG)
//                if (String(mBytes) == FLAG){
//                    println(String(mBytes))
//                    getOutputStream().write(string.encodeToByteArray())
////                    getInputStream().read(mBytes)
////                    println(String(mBytes))
//                }
                getOutputStream().write(string.encodeToByteArray())
                close()
            }
        } catch (e: Exception) {
            println(e)
            throw e
        }
    }

    init {
        println("已进入发送者模式")
        try {
            once(text)
        } catch (e: Exception) {
        }
    }
}