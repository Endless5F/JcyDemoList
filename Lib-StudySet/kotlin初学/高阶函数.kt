package main.kotlin

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.OutputStream
import java.nio.charset.Charset

/**
 * Created by Administrator on 2018/4/27.
 *  传入或者返回函数的函数
 *  函数引用  ::println
 *  带有Receiver的引用 hello::world
 */
class Hello{
    fun world(){
        println("Hello world")
    }
}

fun factorial(n:Int):Int{//阶乘
    if (n==0)return 1
    return (1..n).reduce{acc,i->acc*i}
}

data class Person(val name:String,val age:Int){
    fun work(){
        println("$name is working!!!")
    }
}
fun findPerson():Person?{
    return null
}

fun main(args: Array<String>) {
    val helloWorld=Hello::world
    val helloWorld1= { hello: Hello -> hello.world() }
    args.forEach (::println)
    args.filter(String::isNotEmpty)//过滤/筛选
    //常见的高阶函数
    /**
     * map/flatMap
     * fold/reduce
     * filter/takeWhile
     * let/apply/with/use
     * */
    val list= listOf(1,2,3,5,6,8,9,3,2)
    val newList=ArrayList<Int>()
    list.forEach{//遍历迭代+映射
        val newElement=it*2+3
        newList.add(newElement)
    }
    newList.forEach(::println)
    val newList2=list.map{//专门用来转换,映射
        it*2+3
    }
    val newList3=list.map(Int::toDouble)

    val flatList= listOf(1..20,2..5,100..333)
    val aal=flatList.flatMap { it }//打平集合中的集合,成为一个list,同时还可以做变换
    val aalist=flatList.flatMap {
        it.map { //it指,集合中的集合
            "No.$it"//it指,集合中的集合中的值
        }
    }
    println(aal.reduce { acc, intRange -> acc+intRange })//计算集合内的和
    (0..6).map(::factorial).forEach(::println)//先转换,映射成各个阶乘结果,再分别打印
    (0..6).map(::factorial).reduce { acc, i -> acc+i }//先转换,映射成各个阶乘结果,再将结果相加
    //若想再相加阶乘前给一个初始值
    (0..6).map(::factorial).fold(5){
        acc, i -> acc+i
    }
    //拼接
    (0..6).map(::factorial).fold(StringBuilder()){//括号里是什么,则后面acc代表什么
        acc, i -> acc.append(",")
    }
    println((0..6).joinToString(","))//拼接字符串神器
    (0..6).map(::factorial).foldRight(StringBuilder()){//倒过来拼接
        i, acc -> acc.append(",")
    }
    //筛选,根据位置
    println((0..6).map(::factorial).filterIndexed { index, i -> index%2==1 })
    //筛选,遇到第一个不符合条件的,筛选终止
    println((0..6).map(::factorial).takeWhile { it%2==1 })
    //let apply with
    findPerson()?.let { (name, age) ->//方便直接使用属性成员
        println(name)
        println(age)
    }
    findPerson()?.apply {//方便直接使用Person内部成员及方法
        work()
        println(age)
    }
    //读文件
    val br=BufferedReader(FileReader("frist.kt"))
    with(br){
        var line:String?
        while (true){
            line=readLine()?:break
            println(line)
        }
        close()
    }
    val content=BufferedReader(FileReader("frist.kt")).readText()
    BufferedReader(FileReader("frist.kt")).use {
        var line:String?
        while (true){
            line=readLine()?:break
            println(line)
        }
    }
    //尾递归  tailrec
    /**
     * 递归的一种特殊形式
     * 调用自身后无其他操作
     * tailrec关键词提示编译器尾递归优化
     * */
    val  MAX_NODE_COUNT=100000
    val head=ListNode(0)
    var p=head
    for (i in 1..MAX_NODE_COUNT){
        p.next= ListNode(i)
        p=p.next!!//!!告诉编译器,我知道该变量可为空,并且已处理
    }
    println(findListNode(head,MAX_NODE_COUNT-2)?.value)
    //闭包
    for (i in fibonacciList()){
        if (i>100)break
        println(i)
    }
    //复合函数，调用示例
    val add5AndmultipyBy2=add5 andThen multiplyBy2
    println(add5AndmultipyBy2(8))
    //偏函数
    val stringFromGbk=makeStringFromGbkBytes("我是中国人".toByteArray(charset("GBK")))
    println(stringFromGbk)

    //总结小示例,统计文件中字符个数
    val map=HashMap<Char,Int>()
    File("frist.kt").readText().toCharArray().filterNot(Char::isWhitespace)
            .groupBy { it }.map {
        it.key to it.value.size
    }.forEach(::println)
}
//尾递归
data class ListNode(val value:Int,var next:ListNode?=null)
tailrec fun findListNode(head:ListNode?,value:Int):ListNode?{
    head?:return null
    if (head.value==value)return head
    return findListNode(head.next,value)
}
//闭包
/**
 * 函数运行的环境
 * 持有函数运行状态:比如函数的变量无法释放
 * 函数内部可以定义函数:即可以返回函数,lambda表达式即为该返回函数的类型
 * 函数内部也可以定义类
 * */
fun makeFun():()->Unit{
    var count=0
    return fun(){
        println(++count)
    }
}
fun fibonacciNum():()->Long{//斐波那契数列,:F(0)=1，F(1)=1F(n)=F(n-1)+F(n-2)（n>2，n∈N*）
    var first=0L
    var second=1L
    return fun():Long{
        val result=second
        second+=first
        first=second-first
        return result
    }
}
fun fibonacciList():Iterable<Long>{//斐波那契数列,:F(0)=1，F(1)=1F(n)=F(n-1)+F(n-2)（n>2，n∈N*）
    var first=0L
    var second=1L
    return Iterable{
        object :LongIterator(){
            override fun hasNext()=true

            override fun nextLong(): Long {
                val result=second
                second+=first
                first=second-first
                return result
            }
        }
    }
}
    /**
     * fun add(x:Int)=fun(y:Int)=x+y
     * 等号后面为前一个函数的返回值
     * */
fun add(x:Int)=fun(y:Int)=x+y
//函数复合
/**
 * multiplyBy2(add5(8))
 * */
val add5={i:Int->i+5}
val multiplyBy2={j:Int->j*2}
/**
 * Function1 : 代表函数，kotlin中，一共有23个Function，从Function0到Function22
 * 代表从无参到最多22个参数的函数,下列复合函数，使用了，中缀表达式，和扩展方法，以及泛型参数
 * */
infix fun <P1,P2,R> Function1<P1,P2>.andThen(function:Function1<P2,R>):Function1<P1,R>{
    return fun(p1:P1):R{
        return function.invoke(this.invoke(p1))
    }
}
//科理化 Currying ：多参数函数，变换成，一系列单参数函数
/**
 * 多元函数变换成一元函数调用链
 * */
fun log(tag:String,target:OutputStream,message:Any?){
    target.write("{$tag}$message\n".toByteArray())
}
fun log(tag:String)=fun(target:OutputStream)=fun(message:Any?)=target.write("{$tag}$message\n".toByteArray())
//定义，科理化扩展方法，可用于转换成,科理化,三个参数
/**
 * 调用该扩展方法:
 * ::log 获取函数引用，
 * ::log.curried()("jj")(System.out)("Hello World Currrying")
 * */
fun <P1,P2,P3,R> Function3<P1,P2,P3,R>.curried()
    =fun(p1:P1)=fun(p2:P2)=fun(p3:P3)=this(p1,p2,p3)
//反科理化:自行了解
//偏函数
/**
 * 多参数函数，当指定，给部分参数时，所获取的常量，依就是一个函数，这就是偏函数
 * 即:科理化给定部分参数所获取的常量,仍旧是函数,此为偏函数
 * val consoleWithTag=::log.curried()("jj")(System.out)
 * consoleWithTag("Hello World Currrying Again")
 * 即:传入部分参数后得到新的函数
 * */
//案例
val makeString=fun(byteArray:ByteArray,charset:Charset):String{
    return String(byteArray,charset)
}
/**
 * partial : 偏函数
 * partial2 : 第二个参数指定
 * 调用:val stringFromGbk=makeStringFromGbkBytes("我是中国人".toByteArray(charset("GBK")))
 * */
val makeStringFromGbkBytes= makeString.partial2(charset("GBK"))
//扩展方法定义
fun <P1,P2,R> Function2<P1,P2,R>.partial2(p2:P2)=fun (p1:P1)=this(p1,p2)
fun <P1,P2,R> Function2<P1,P2,R>.partial1(p1:P1)=fun (p2:P2)=this(p1,p2)
