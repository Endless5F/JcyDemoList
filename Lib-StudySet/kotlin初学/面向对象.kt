package main.kotlin

import kotlin.reflect.KProperty

/**
 * Created by Administrator on 2018/4/11.
 *
 */
//接口和抽象类
/**
 * 接口:可以看作规则
 * 抽象类:可以看作半成品
 * */
abstract class A1{ }
interface B{}
class C:A1(),B{   } //继承类需要加(),实现接口只需要将接口名写在后面
//继承
abstract class Persion(open val age:Int){
    open fun work(){  } //方法想被继承,必须重写,或者定义抽象类
}
/**
 * age在Persion类内是成员属性，age在Manong类中是参数,
 *  而 MaNong(age: Int): Persion(age) 代表将传进MaNong的参数age赋值到Persion类的age中
 *  又由于Persion类是MaNong类的父类,因此MaNong类天生就有age属性,所以最后其实是将参数age赋值给自己类中的age属性
 *  即:继承类时实际上调用了父类的构造方法
 * */
class MaNong(age: Int): Persion(age) {
    override val age: Int
        get() = 0
    override fun work() {//覆写方法必须加 override
        super.work()
        println("我是码农,我在敲代码")
    }
}
class Doctor(age: Int):Persion(age){
    override fun work() {
        super.work()
        println("我是医生,我在医治病人")
    }
}
/**
* 小示例
* */
interface Driver{//驾驶,开车
    fun drive()
}
interface Write{//写
    fun write()
}
class Manager:Driver,Write{//一个经理,实现两种能力:开车和写报告
    override fun drive() {

    }

    override fun write() {

    }
}
//接口代理
    /**
     * 资深经理,有权利雇佣一个开车的和一个写报告的,
     *  关键字: by(用于代理:接口代理和属性代理) ,这里是接口代理
     *      SeniorManager类实现两个接口,本应该实现其中的方法,但是由 by 关键字,
     *      代理给了,传进来的两个成员属性,driver和writer来实现
     * */
class SeniorManager(val driver:Driver,val writer:Write):Driver by driver,Write by writer
class CarDriver:Driver{
    override fun drive() {
        println("开小轿车")
    }
}
class PPTWriter:Write{
    override fun write() {
        println("做ppt呢")
    }
}
/**
 * 接口方法冲突:
 *  签名一致(签名:函数名一致,参数一致)且返回值相同的冲突
 *  子类(实现类)必须覆写冲突方法,覆写方法中可通过判断返回不同接口或类的方法
 *  返回不同接口或类的方法的实现形式: super<[父类](接口)名>.[函数名]([参数列表])
 * */
//成员可见性
class House{
    val aaa:Int=0//默认public
    private var bbb:String=""//私有
    internal val ccc:Char='a'//模块内可见
    protected val ddd=2//子类可见
}
//单例
/**
 * 只有一个实例的类
 * 不能自定义构造方法
 * 可以实现接口/继承父类
 * 本质上就是单例模式最基本的实现
 * */
object MusicPlayers{
    val state:Int=0
    fun play(url:String){

    }
}
//伴生对象和静态成员
class Latitude private constructor(val value:Double){
    companion object {//伴生对象
        /**
         * 伴生对象内部方法即静态成员
         * kotlin中调用:val latitude=Latitude.ofDouble(111.0)
         * java中调用:val latitude=Latitude.Companion.ofDouble(111.0)
         * @JvmStatic(方法前)  @JvmField(成员前)
         *  若方法上或者成员前使用泛型标注该方法或者成员为静态,此时java中调用就和kotlin中一样
         *      java中: val latitude=Latitude.ofDouble(111.0)
         * */
        @JvmStatic
        fun ofDouble(double: Double):Latitude{
            return Latitude(double)
        }
        @JvmField
        val TAG:String="latitude"
    }
}
//方法重载只会跟方法名和参数有关,跟返回值无关   默认参数
/**
 * java代码中没有默认参数,因此想在java代码中调用默认参数的方法,需要标注@JvmOverloads
 * */
class Overloads{
    /*fun a():Int{
        return 0
    }
    fun a(value:Int):Int{
        return value
    }*/
    //前两个重载方法可以写成
    @JvmOverloads
    fun a(value:Int=0):Int{
        return value
    }
}

//扩展成员 ==>扩展方法
/**
 * kotlin调用:"abc".multiply(16)
 * */
fun String.multiply(int:Int):String{
    val stringBuilder=StringBuilder()
    for (i in 0 until int){
        stringBuilder.append(this)
    }
    return stringBuilder.toString()
}
/**
 * times:时代,乘等含义
 * kotlin中调用:"abc" * 16
 * java中调用:当前文件名(面向对象).times("abc",16)
 * */
operator fun String.times(int:Int):String{
    val stringBuilder=StringBuilder()
    for (i in 0 until int){
        stringBuilder.append(this)
    }
    return stringBuilder.toString()
}
//扩展属性
/**
 * 扩展属性不能初始化,类似于接口属性
 * java调用扩展成员 类似于调用静态方法
 * */
val String.a:String
    get() = "abc"
var String.b:Int
    set(value) {}
    get()=0
//属性代理
class Delegates{//委派
    val hello by lazy {
        "Hello world"
    }
    /**
     * 属性代理:by 后面的 表达式必须有getValue和setValue
     *      val 只需要有getValue   var必须get和set都有
     * */
    val hello2 by X1()
    var hello3 by X1()
}
class X1{
    private var value:String?=null
    operator fun getValue(thisRef: Any?, property: KProperty<*>): String{
        return value?:""
    }
    operator fun setValue(thisRef: Any?, property: KProperty<*>,value:String){
        this.value = value
    }
}
//数据类
data class Country(val id:Int,val name:String)
/**
 *  数据类,用来代替java中的javabean
 *  使用data关键字声明后,字节码中会生成很多有用的方法
 *      toString    copy    component1(对应第一个成员属性)  component2(对应第二个成员属性)
 *  val (id,name)=Country(0,"中国")//实际上调用了component1  component2
 * `allOpen 和 noArg 插件 ：解决data calss 字节码文件中将类 final 以及 没有无参构造方法的问题
 * */
//自定义componentX
class ComponentX{
    operator fun component1():String{
        return "您好,我是"
    }
    operator fun component2():Int{
        return 1
    }
    operator fun component3():Int{
        return 1
    }
    operator fun component4():Int{
        return 0
    }
}

//内部类
/**
 *
 * */
open class Outer{
    val a:Int=0
    class Inner{//默认是静态内部类
        fun hello(){

        }
    }
    inner class Inner1{//非静态内部类
        val a:Int=6
        fun hello(){
            println(this@Outer.a)//使用的外部类a
        }
    }
}
//案例:
interface OnClickListener{
    fun onClick()
}
class View{
    var onClickListener:OnClickListener?=null
}
//枚举:暂不关注
//密封类
/**
 * 形式: sealed class className
 *  子类可数,密封类就是将同一组件的一系列类封装起来
 *      比如说:播放器==>
 *          播放类  快进类  暂停类  重播类  停止类
 * */
sealed class PlayerCmd{//播放器
    class Play(val url:String,val position:Long=0):PlayerCmd()
    class Seek(val position:Long):PlayerCmd()
    object Pause:PlayerCmd()
    object Resume:PlayerCmd()
    object Stop:PlayerCmd()
}

fun main(args: Array<String>) {
    val doc=Doctor(24)
    println(doc.age)
    /* 代理 */
    val driver=CarDriver()
    val writer=PPTWriter()
    val senior=SeniorManager(driver,writer)
    senior.drive()
    senior.write()
    val latitude=Latitude.ofDouble(111.0)
    //数据类
    val china=Country(0,"中国")
    val (id,name)=china
    //自定义ComponentX
    val component=ComponentX()
    val (a,b,c,d)=component
    //内部类
    val view=View()
    /**
     * Object 声明 : 用来定义单例
     * Companion Objects :
     *      定义的成员类似于Java中的静态成员，因为Kotlin中没有static成员
     * Object Expression 表达式
     * object 也可以实现一个接口或者成为父类的子类，
     * 这样创建的对象就是实现了某个接口的单例对象了
     *  例如:  object:Outer(),OnClickListener
     *  object可以访问外围作用域的变量
     * */
    view.onClickListener=object :Outer(),OnClickListener{//匿名内部类
        override fun onClick() {
        println(china)
        }
    }
}