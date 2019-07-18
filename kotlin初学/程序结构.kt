package main.kotlin

/**
 * Created by Administrator on 2018/4/9.
 */
const val FINAL_HELLO_WORLD:String="HelloWorld"//等同于java中的final,编译期常量
val FINAL_HELLO_CHINA="Hello Chaina"//可推到出来该常量为字符串类型,类似于final,运行时常量,运行时才真正知道值为多少

//函数
fun method1(aa:Int,bb:Int): Int {
    return aa+bb
}//等同于下面这种方式
fun method11(aa:Int,bb:Int)=aa+bb

//匿名函数
val method2=fun(aa:Int):Unit{}//Unit代表没有返回值,可不写

//*************************Lambda表达式**********************
/**
 * Lambda表达式:其实就是匿名函数
 * Lambda表达式的类型:每个Lambda表达式都有自己的类型,其类型为该Lambda表达式的
 *      参数组类型->返回值类型  例如:(Int,Int)->Int 或者(T)->Unit等
 * Lambda表达式的最后一行为该Lambda表达式的返回值,可在最后一行前,做相应处理
 *
* */
fun method3(aa:Int,bb:Int)=aa+bb//Lambda表达式可以改成
val sum={arg1:Int,arg2:Int->//类型,该Lambda表达式类型为(Int,Int)->Int
    arg1+arg2}//Lambda表达式的返回值为最后一行

//类成员
/**
 * 类成员
 *  类成员变量,在kotlin中都有默认的get set方法,在调用成员变量时,会自动调用set get方法
 *  field:代表当前成员变量的值,field只有在get和set方法中才能访问的到,field代表变量后面真正指定的值
 *  lateinit 延迟初始化,只能用在var上,但是使用的时候,千万注意,使用前初始化,否则会报错
 *  问:有时val常量也需要类似的延时,怎么做?
 *      val c:X by lazy {  //val常量只有get方法
 *          X()//延时调用c,虽然在这里初始化啦,但是其实还是在真正使用的时候才会真正初始化
 *      }
 * 类构造参数中:
 *      var 性格:String  代表成员变量
 *      value:Int        代表就是普通参数
 * 记1：构造方法参数中val / var 修饰的都是属性
 * 记2:类内部也可以定义属性(属性就是成员变量),类似于java,下面示例 类A
 * 注:类成员最好还是在类构造函数时就被确定
 * */
class X
open class A{
    var a:Int=0//每个类成员变量,在kotlin中都有默认的get set方法
        get() {
            println("如果不写该行打印,则可不重写get方法,该get方法kotlin默认实现   $field")
            return field//field只有在get和set方法中才能访问的到,field代表a变量后面真正指定的值
        }
        set(value) {
            field=value
        }
    lateinit var b:String //lateinit 延迟初始化,只能用在var上
    val c:X by lazy {
        X()//延时调用c,虽然在这里初始化啦,但是其实还是在真正使用的时候才会真正初始化
    }
    var cc:String?=null
}

//基本运算符,本质上就是一个函数
/**
 *  + - * / in .. ++ --
 *  a[i] == a.get(i)  a[i,j]
 *  invoke
 * */
//自定义重载运算符
class Complex(var real:Double,var imaginary:Double){//Complex复数类,real实数部分,imaginary虚数
    //复数类里定义加法运算
    operator fun plus(other: Complex):Complex{// + 号运算符方法,传进来一个复数,用来运算
        //返回复数,当打印该复数时,该类中重写toString方法即可
        return Complex(real+other.real,imaginary + other.imaginary)
    }
    /**
     * 说明:运算符方法的重载定义,要求名字一致,有operator关键字,参数个数对的上,但是参数的类型和返回的类型无所谓
     * */
    operator fun plus(other: Int):Int{// + 号运算符方法,传进来一个复数,用来运算
        return real.toInt()
    }
    //重载invoke方法,载调用该类invoke方法时,取复数的模
    operator fun invoke():Double{
        return Math.hypot(real,imaginary)
    }

    override fun toString(): String {
        return "$real+${imaginary}i"
    }
}
//自定义操作符
class Book{//定义书类
    /**
     * infix关键字,代表中缀表达式
     * 中缀表达式:可以不需要() . 等形式调用方法
     * */
    infix fun on(any: Any):Boolean{//定义 on 操作符
        return false
    }
}
class Desk//定义课桌类

//中缀表达式
/**
 * 中缀表达式:若函数或方法只有一个参数,且使用infix修饰的函数
 *      可以去掉 . .() 来调用该方法
 *      示例: class a{ fun b(x:C){} }  ==》 a() b C() 方式调用,等同于a().b.(C())
 * */
//分支表达式
/**
 * if表达式,if是有返回值得,if语句的返回值是语句块的最后一行,类似于lambda表达式
 *      val mode=if(true){ 1 }else{ 0 }//if表达式,表达式中有if就必须有else,否则mode可能无法被赋值
 * switch:kotlin中没有switch分支结构
 * when表达式:kotlin中when完全代替了switch,而且比switch更强大,支持任意类型,表达式完备性:必须有else
 * */
class Player{
    private var state=State.IDLE
    enum class State{
        IDLE,BUFFERING,PLAYING,PAUSED
    }
    fun pause(){
        when(state){//when表达式,完全兼容switch
            Player.State.IDLE-> println("IDLE")
            Player.State.BUFFERING-> print("BUFFERING")
            is State-> print("true")
            //in 1..100-> print("isInt")
            else->{
                println("else")
            }
        }
        val int=when(1){//when表达式,完备性,必须有else
            is Int->0
            in 1..100->100
            else->0
        }
    }
}

//循环语句
/**
 * for循环:给任意类实现Iterator的方法，即该类内部有Iterator，就可被循环
 * while语句 do..while..
 * 跳过 continue  ;  终止 break
 * 多层循环嵌套的终止结合标签使用(不常用)
 * Outter@for(...){  // Outter@ 和 Inner@ 均为标签
 *      Inner@while(i<0){ if(..)break@Outter }
 * }
 * */

//异常捕获
/**
 * try{ }catch(e:Exception){ e.printStackTrace() }finally{ }
 * 注意下列写法:  无异常返回x/y,有异常返回0,不过都会执行finally
 * return try{ x/y }catch(e:Exception){ e.printStackTrace() 0 }finally{ }
 * */

//参数
/**
 * 具名参数:举例
 *  fun sum(arg1:Int,arg2:Int)=arg1+arg2
 *  使用函数:sum(arg2=1,arg1=3) //具名传参,不分顺序
 * 变长参数：vararg
 *  fun aaa(vararg args:String){  }
 *  注：在java中变长参数只能在最后一个，但是kotlin有具名参数，因此可以放任何位置，但是若放在前面，则其它参数需要使用具名参数
 *      val array=intArrayOf(1,3,5,7,9)
 *      aaa(*array)  // 这里的 * 代表将array展开成变长参数,目前只支持变长参数场景,只能展开数组,即只支持Array
 * 默认参数：
 *  fun bbb(a:Int=1,b:Int,c:String="hello"){  }
 *      默认参数放在参数前面,则后面的参数只能使用具名参数,若放在最后则最后一位可不传
 * */

fun main(args: Array<String>) {
    println("请输入:")
    readLine()//控制台输入
    println(FINAL_HELLO_CHINA)
    println(sum.invoke(1,2))//等价于println(sum(1,2))
    val intArr = intArrayOf(1, 2, 3)
    /**
     * foreach是扩展方法
     * kotlin中如果lambda表达式的参数只有一个,可以默认为it
     * intArr.forEach{ println(it)}等价于intArr.forEach({ it->println(it)})
     * 等价于intArr.forEach(){ it->println(it)}等价于intArr.forEach(::println)
     * 原因：intArr.forEach({ it->println(it)})原型
     *  kotlin中如果参数中最后一个参数是一个lambda表达式,
     *      则可以把括号放到外面,intArr.forEach(){ it->println(it)}
     *  若此时小括号内无参数,可将括号省略,intArr.forEach{ println(it)}
     *  看foreach源码可知,foreach的参数为T泛型的Lambda表达式
     *      而println输出的类型是Any类型,而Any类型是所有类型的始祖，
     *      而T泛型是Any的子类，因此println的类型为(Any?)->Unit,foreach的参数类型为(T)->Unit
     *      即foreach方法参数的action的类型和println的类型一致,
     *      所以可以直接把println函数也算是Lambda表达式直接传进去调用，intArr.forEach(::println)
     *    即,入参、返回值与形参一致的函数，可以用函数的引用的方式作为实参传入
     * */
    intArr.forEach{ println(it)}
    /**
     * 想要在高阶循环中终止循环,则可以给forEach的大括号{}自定义起一个名字,
     *      End@,然后在return时直接返回该名字@End,就可以跳出循环
     * */
    intArr.forEach End@{
        if (it == 2) return@End
        println(it)}

    val a=A()
    a.a=2
    println(a.cc?.length)

    val c1=Complex(3.0,4.0)
    val c2=Complex(2.0,5.0)
    println(c1+c2)//调用的是 自定义重载运算符里的复数相加方法
    println(c1())//调用的是 自定义重载运算符里的复数取模方法
    val arg:String ="-name Name"
    if ("-name" in arg){
        println(arg[arg.indexOf("-name")+1])//输出-name后面的Name
    }
    //有 in 操作符,还可以自定义操作符,比如 on
    if (Book() on Desk()){//在DSL中使用较多
        println("书在课桌上")
    }
    val mode=if(true){ 1 }else{ 0 }//if表达式,表达式中有if就必须有else,否则mode可能无法被赋值

    val player=Player()
    player.pause()

    //异常
    try {
        val agr1=args[0].toInt()
    }catch (e:Exception){
        e.printStackTrace()
        println("该参数不存在")
    }finally {
        println("异常结束")
    }
}