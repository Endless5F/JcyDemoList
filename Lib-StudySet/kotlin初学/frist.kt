package main.kotlin

/**
 * Created by Administrator on 2018/4/8.
 */

val aBoolean:Boolean=true
val bBoolean:Boolean=false
var cBoolean:Boolean=false

val aInt:Int=8
val bInt:Int=0xFF
val maxInt:Int=Int.MAX_VALUE
val minInt:Int= Int.MIN_VALUE

val aLong:Long=1234567890
val aFloat:Float=2.0f
val aDouble:Double=3.0
val aShort:Short=32767
val aByte:Byte=127

val aChar:Char='0'
val bChar:Char='中'
val cChar:Char='\u000f'

//基本类型转换
val anInt:Int=5
val anLong:Long= anInt.toLong()

val string:String="hello World"
val fromChars:String= String(charArrayOf('h','e','l','l','o','W','o','l','d'))

//类
//constructor()构造方法,只有一个时,可省略
//继承 :   Any是所有类的父类，类似于java中的Object
open class 人 constructor(var 性格:String, var 长相:String, var 声音:String){
    init {//代码快
        println("new 了一个${this.javaClass.simpleName},他性格$性格,长相$长相,声音$声音")
    }
}
class 妹子 constructor(性格:String, 长相:String, 声音:String): 人(性格, 长相, 声音){
    fun getName():String{
        return "你猜"
    }
}
class 帅哥 constructor(性格:String, 长相:String, 声音:String): 人(性格, 长相, 声音){//constructor()构造方法,只有一个时,可省略

}

//空类型和智能类型转换
fun getName():String?{// :String 代表有返回,类型为字符串 ,在类型后面 + ? 表示返回值可以为空
    return null
}

//区间
val aRange:IntRange=0..1024//闭区间
val bRange:IntRange=0 until 1024//半闭半开区间
val emptyRange:IntRange=0..-1//null

//数组
val arrayOfInt:IntArray = intArrayOf(1,2,3,4,5,6,7)
val arrayOfChar:CharArray = charArrayOf('h','e','l','l','o','W','o','l','d')
val arrayOfString:Array<String> = arrayOf("我","是","码","农","我是码农")//对象数组,使用这个,并将对象当成泛型传进去

fun main(args: Array<String>) {
    /*println(main.kotlin.getBInt)
    println(Math.pow(2.0,31.0)-1)
    println(main.kotlin.getString==main.kotlin.getFromChars)//true,比较内容是否相同
    println(main.kotlin.getString===main.kotlin.getFromChars)//false,比较对象是否相等
    println("接下来我们要输出:"+main.kotlin.getString)

    val arg1:Int=0
    val arg2:Int=0
    println(""+arg1+"+"+arg2+"="+(arg1+arg2))//等同下面写法
    println("$arg1+$arg2=${arg1+arg2}")//字符串模版,同php语法
    //salary==$1000
    val salary:Int=1000
    println("$$salary")
    val rawString:String="""
        sdjfasldfjalfd
        asdfa\n  \u
    """
    println(rawString.length)*/

    /*val 我喜欢的妹子:main.kotlin.妹子=main.kotlin.妹子("温柔","美丽","甜美")
    val 我膜拜的帅哥:main.kotlin.帅哥=main.kotlin.帅哥("彪悍","帅气","洪亮")
    println(我喜欢的妹子 is main.kotlin.人)*/

    /*val name=main.kotlin.getName()
    *//*if (name==null){
        println("name is invalid")
    }else{
        println(name.length)
    }*//* //可简化成下面一句话:
    println(name?.length)
    if (name==null)return//等同于 val name=main.kotlin.getName()?:return
    val value:String?="Hello World" //? 表示该字符串可为null
    println(value!!.length)//如果明知道该字符串不可能为空,则可在 . 之前加俩 !,表示明确无错*/

    /*val 我喜欢的妹子: 人 = 妹子("温柔", "美丽", "甜美")
    if (我喜欢的妹子 is 妹子){//智能转换
        我喜欢的妹子.getName()//在java中父类想调用子类特有的方法,此处必须类型转换
    }
    val child: 人? =我喜欢的妹子 as? 人//as类型转换  as?安全的类型转换,转换失败返回null
    val string:String="hello"
    string?.length//若string为空,则返回null
    if (string is String)//智能转换 main.kotlin.getString != null也可行
        println(string.length)*/

    /*println(emptyRange.isEmpty())
    println(aRange.contains(500))
    println(50 in aRange)
    for (i in aRange){
        print("$i,")
    }*/

    for (int in arrayOfString){
        println(int)
    }
    println(arrayOfChar.joinToString())//转换为字符串,默认以 , 分割
    println(arrayOfInt.slice(1..3))//切片
}