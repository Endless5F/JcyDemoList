package main.kotlin.协程

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.JButton
import javax.swing.JFrame
import javax.swing.JLabel

/**
 * Created by Administrator on 2018/4/28.
 */
const val LOGO_URL="http://www.imooc.com/static/img/index/logo.png?t=1.1"

fun main(args: Array<String>) {
    val frame=MainWindow()
    frame.title="Coroutine"
    frame.setSize(300,200)
    frame.isResizable=true
    frame.init()
    frame.isVisible=true

    frame.onButtonlick {
        我要开始协程啦(DownloadContext(LOGO_URL)){
            val imageData= 我要开始耗时操作了 {
                我要开始加载图片啦(LOGO_URL)
            }
            frame.setLogo(imageData)
        }
    }
}

class MainWindow : JFrame() {
    private lateinit var button:JButton
    private lateinit var image:JLabel

    fun init(){
        button= JButton("点我获取慕课网Logo")
        image= JLabel()
        image.size= Dimension(500,300)

        contentPane.add(button,BorderLayout.NORTH)
        contentPane.add(image,BorderLayout.CENTER)
    }

    fun  onButtonlick(listener:(ActionEvent)->Unit){
        button.addActionListener(listener)
    }

    fun setLogo(lagoData:ByteArray){

    }
}
