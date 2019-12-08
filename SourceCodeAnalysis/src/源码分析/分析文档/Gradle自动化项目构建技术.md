## Groovy
### Groovy介绍
1. 是一种基于JVM的敏捷开发语言
2. 结合了Python、Ruby、Smalltalk的许多强大特性
3. Groovy可以与Java完美结合，而且可以使用Java所有的库

### Groovy特性
1. 语法上支持动态类型，闭包等新一代语言特性
2. 无缝集成所有已存在的Java类库
3. 即支持面向对象编程也支持面向过程编程

### Groovy优势
1. 一种更加敏捷的编程语言
2. 入门非常容易，但功能非常强大
3. 即可以作为编程语言也可以作为脚本语言
4. 熟悉Java会非常容易掌握Groovy

### Groovy基础语法
1. Groovy中语句结束符可以使用“;”，也可以不使用；
2. Groovy中类、变量和方法，默认是public。
3. 输出语句
    ```
    // 1. 可以和Java一样
    class HelloWorld {
        public static void main(String[] args) {
            System.out.println("Hello Groovy")
        }
    }

    // 2. 直接输出(两种)
    println "Hello Groovy"
    println("Hello Groovy")
    ```
4. Groovy中的变量

    1. 变量的类型：基本类型（java中的int/float/double/byte/char/long/short）和对象类型(String等) ，在Groovy中最终都是对象类型。
        ```
        int x = 10
        println x.class //结果为：class java.lang.Integer

        double y = 3.14
        println y.class //结果为：class java.lang.Double
        ```
        由此可见，Groovy中的基本类型最终会被编译器包装成对象类型
    2. 变量的定义：强类型定义方式和弱类型def定义方式
        ```
        def x1 = 10
        def y1 = 3.14
        def str1 = 'groovy str'
        x1 = 'grovvy def'

        println x1.class //class java.lang.Integer
        println y1.class //class java.math.BigDecimal
        println str1.class //class java.lang.String
        println x1.class //class java.lang.String
        ```
        强类型定义及定义的时候写明变量的类型，而def则由编译器自行推导变量的类型
        
        强类型定义方式和弱类型def定义方式的选择：
        
        * 变量用于本类或者本模块而不会用于其它类或者其他模块，推荐使用def类型，这样可以随时动态的转换为其它类型；
        * 变量要用于其它类或是其它模块，强烈建议使用强类型定义方式。使用强类型定义的方式不能动态转换类型，才能使外界传入或者调用的时候不会对于数据的类型产生疑惑，这样就保证外界传入的数据一定是我们想要的正确的类型的数据。
5. 字符串详解

    1. 两种字符串：String(同Java中的String一样)、GString
    2. 字符串常用的三种定义方式
        ```
        //1. 单引号定义的就是java中的String，内容即为''内的字符串，并且不可更改
        def str1 = 'a single string'
        println str1.class  //class java.lang.String
        // 有特殊字符同样的通过反斜杠转义
        def str2 = 'a single \'special\' string'

        //2. 三个单引号定义的是有格式的字符串，会直接按照我们写的格式进行输出，而不用像Java中进行拼接
        def trebleStr = '''line one
                line two
            line three '''
        
        //3. 双引号
        def name = "Groovy"
        println name.class  //class java.lang.String
        // 字符串模板
        def sayHello = "Hello $name"
        // 字符串模板也可以是表达式
        def sum = "the sum of 2 and 3 equals ${2 + 3}"
        ```
    3. String方法来源
        
        * java.lang.String原有的方法
        * DefaultGroovyMethods：是Groovy对所有对象的一个扩展
        * StringGroovyMethods：继承自DefaultGroovyMethods，重写了DefaultGroovyMethods中的许多方法，使这些方法更加适用于String使用。
    4. Groovy中的两种字符串，对于开发者都是字符串，和之前正常定义即可。两者之间的转换都是编译器完成的，开发者不关注这些。即便定义的是Java的String，可以使用GString的方法，最终的转换都是编译器完成。因此，String 和GString之间可以相互调用和传递，不需要考虑它们之间的转换问题。
    5. 字符串方法
        ```
        //字符串填充：
        // 1. center(Number numberOfChars,CharSequence padding) ,将字符串作为中心进行填充
        // 当numberOfChars小于或等于str本身的长度时，不进行填充操作，大于则用pandding扩展至长度numberOfChars，从字符串的右边（尾）进行填充，再到左边（头）
        def str = "groovy"
        println str.center(8)     //结果： groovy ，不传padding代表以空格填充
        println str.center(5,"a") //结果： groovy
        println str.center(6,"a") //结果：groovy
        println str.center(7,"a") //结果：groovya
        println str.center(8,"a") //结果：agroovya
        println str.center(9,"a") //结果：agroovyaa
        // 2. padLeft(Number numberOfChars,CharSequence padding) ,在字符串的左边进行填充
        // 3. padRight(Number numberOfChars,CharSequence padding),在字符串的右边进行填充
        
        //字符串比较：
        def string = "groovy"
        def string1 = "Groovy"
        println string.compareTo(string1)             // 32     结果大于0，str大于Str2
        println string.compareToIgnoreCase(string1)   // 0      结果等于0，str等于忽略大小写的Str
        println string1.compareTo(str)                // -32    结果小于0，str2小于str
        println string > string1                      // true   可用操作符直接进行比较
        println string == string1.toLowerCase()       // true
        
        //获取字符串中的字符：
        def string2 = "groovy"
        println string2.getAt(0) // g
        println string2.getAt(0..1)     // gr
        println string2[0]              // g
        println string2[0..1]           // gr
        
        //字符串中的减法（取差集）：
        def string3 = "groovy"
        def string4 = "hello"
        def string5 = "hello groovy"
        def string6 = "groovy hello "
        
        println string3.minus(string4) // groovy,       str中没有包含str2
        println string3.minus(string5) // groovy,       str中没有包含str3
        println string5.minus(string4) // groovy,       str3中包含了str2 (注意结果包含了空格)
        println string5.minus(string6) // hello groovy, str3z中没有包含str4
        println string5 - string3      // hello,        str3z中包含了str(注意结果包含了空格)
        
        // 其它方法
        def string7 = "hello groovy"
        println string7.reverse() // yvoorg olleh,字符串反转
        println string7.capitalize()// Hello groovy,首字母大写
        println string7.isNumber() // false，是否全是数字
        def string8 = "1234"
        println string8.toInteger() // 1234
        println string8.toBigDecimal() // 1234
        println string8.toDouble() // 1234.0
        ```
6. 逻辑控制
    
    1. if/else：同Java
    2. switch/case：Java中switch只能传入int类型、byte,char和short类型能自动提升为int类型、String类型和后来扩展的enum类型。而在Groovy中，switch可以传入任性类型的数据进行匹配。
        ```
        String judgeType(Object x) {
            def result
            switch (x) {
                case "string":
                    result = "x is string"
                    break
                case [4, 5, 6, 7,'inList']: //列表（数据结构中讲解）
                    result = "x is in list [4, 5, 6, 7,'inList']"
                    break
                case 10..15: //范围range（数据结构中讲解）
                    result = "x is in range 10..15"
                    break
                case Integer:
                    result = "x is Integer"
                    break
                case BigDecimal:
                    result = "x is BigDecimal"
                    break
                case List:
                    result = "x is List"
                    break
                default:
                    result = "no match"
                    break
            }
            return result
        }
       ```
7. 循环逻辑
    
    1. while循环：同Java
    2. for循环
        ```
        // 1. 范围中的for循环
        def sum = 0
        for (i in 0..9) {
            sum += i
        }
        println sum // 45
        
        sum = 0
        // 2. list中的for循环
        for (i in [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]) {
            sum += i
        }
        println sum // 45
        
        // 3. map中的for循环
        for (i in ['java': 1, ' groovy': 2, 'python': 3]) {
            println "key:${i.key} value:${i.value}"
        }
        ```
8. 闭包
    1. 定义：Groovy中的闭包是一个开放的，匿名的代码块，可以接受参数，可以返回值并且可以赋值给闭包变量。闭包可以引用在其周围范围内声明的变量，与闭包的正式定义相反，Groovy语言中的Closure也可以包含在其周围范围之外定义的自由变量。
    2. 语法：{ [closureParameters -> ] statements } 。其中[closureParameters->]是一个以逗号分隔的可选参数列表，而且statements 有0条或更多条Groovy语句，参数看起来类似于方法参数列表，这些参数可以是类型化的或非类型化的。指定参数列表时， - >字符是必需的，用于将参数列表与Groovy语句分开。
    3. 代码示例
        ```
        { item++ }          //一个引用名为item的变量的闭包

        { -> item++ }       //通过添加箭头（ - >）可以明确地将闭包参数与代码分开

        { println it }      //使用隐式参数（it）的闭包

        { it -> println it }    //上面的一个替代版本，它是一个显式参数

        { name -> println name }    //在这种情况下，通常最好为参数使用显式名称

        { String x, int y ->        //一个闭包接受两个类型参数
            println "hey ${x} the value is ${y}"
        }

        { reader ->                 //闭包可以包含多个语句
            def line = reader.readLine()
            line.trim()
        }

        // 上面代码定义一个名为 closure_name 的闭包，用途由 closure body 中的代码定义。
        // 匿名闭包指不声明闭包变量名，只有闭包方法体{ //closure body }
        def closure_name = {
            // closure body
        }
        ```
    4. 闭包类：一个闭包是groovy.lang.Closure类的一个实例，它可以像任何其他变量一样赋值给变量或字段，尽管它是一个代码块。Java8中lambda表达式也引入了闭包概念，类com.sun.corba.se.spi.orbutil.closure.Closure。Groovy将闭包定义为Closure类的实例，与Java 8中的lambda表达式截然不同。
    5. 参数
        1. 普通参数：闭包的参数和常规方法的参数一样，遵循相同的原则。
        2. 隐式参数：当闭包没有显式定义参数列表（使用 - >定义）时，闭包总是定义一个名为it的隐式参数。
        3. 可变参数：闭包可以像任何其他方法一样声明可变参数。可变参数方法的特点是：参数的数量是可变的。有2种情况：最后一个参数是可变长度的参数，或者是一个数组参数。
    6. 闭包调用：def closure_name = { // closure body }
        1. closure_name.call()
        2. closure_name()
    7. 闭包使用：闭包可以用作方法的参数。在Groovy中，很多用于数据类型（例如列表和集合）的内置方法都有闭包作为参数类型。
        ```
        def clos = { param -> println "${str1} ${param}" }
        clos("Groovy")
        clos.call("World");
        // 闭包和列表
        def lst = [11, 12, 13, 14];
        lst.each {println it}
        // 闭包和映射
        def mp = ["TopicName" : "Maps", "TopicDescription" : "Methods in Maps"]
        mp.each {println it}
        mp.each {println "${it.key} maps to: ${it.value}"}
        ```
    8. 闭包进阶
9. 范围
    1. 定义：范围是指定值序列的速记。范围由序列中的第一个和最后一个值表示，Range可以是包含或排除。包含范围包括从第一个到最后一个的所有值，而独占范围包括除最后一个之外的所有值。
    2. 使用示例：
        ```
        1..10 - 包含范围的示例
        1 .. <10 - 独占范围的示例
        'a'..'x' - 范围也可以由字符组成
        10..1 - 范围也可以按降序排列
        'x'..'a' - 范围也可以由字符组成并按降序排列。
        def range = 1..10
        ```
    3. 常用方法：
        ```
        contains()      检查范围是否包含特定值
        get()           返回此范围中指定位置处的元素。
        getFrom()       获得此范围的下限值。
        getTo()         获得此范围的上限值。
        isReverse()     这是一个反向的范围，反向迭代
        size()          返回此范围的元素数。
        subList()       返回此指定的fromIndex（包括）和toIndex（排除）之间的此范围部分的视图
        ```
10. 列表
    1. 定义：列表是用于存储数据项集合的结构。在Groovy中，List保存了一系列对象引用。List中的对象引用占据序列中的位置，并通过整数索引来区分。列表文字表示为一系列用逗号分隔并用方括号括起来的对象。要处理列表中的数据，我们必须能够访问各个元素。Groovy列表使用索引操作符[]索引。列表索引从零开始，这指的是第一个元素。
    2. 使用示例：
        ```
        [11，12，13，14] - 整数值列表
        ['Angular'，'Groovy'，'Java'] - 字符串列表
        [1，2，[3，4]，5] - 嵌套列表
        ['Groovy'，21，2.11] - 异构的对象引用列表
        [] - 一个空列表
        def arrayList = [1, 2, 3, 4]
        ```
    3. 常用方法
        ```
        add()           将新值附加到此列表的末尾。
        contains()      如果此列表包含指定的值，则返回true。
        get()           返回此列表中指定位置的元素。
        isEmpty()       如果此列表不包含元素，则返回true
        minus()         创建一个由原始元素组成的新列表，而不是集合中指定的元素。
        plus()          创建由原始元素和集合中指定的元素组成的新列表。
        pop()           从此列表中删除最后一个项目
        remove()        删除此列表中指定位置的元素。
        reverse()       创建与原始列表的元素相反的新列表
        size()          获取此列表中的元素数。
        sort()          返回原始列表的排序副本。
        ```
11. 映射
    1. 定义：映射（也称为关联数组，字典，表和散列）是对象引用的无序集合。Map集合中的元素由键值访问。 Map中使用的键可以是任何类。当我们插入到Map集合中时，需要两个值：键和值。
    2. 使用示例：
        ```
        ['TopicName'：'Lists'，'TopicName'：'Maps'] - 具有TopicName作为键的键值对的集合及其相应的值。
        [：] - 空映射。
        def map = ['key': 'value']
        ```
    3. 常用方法
        ```
        containsKey()   此映射是否包含此键？
        get()           查找此Map中的键并返回相应的值。如果此映射中没有键的条目，则返回null。
        keySet()        获取此映射中的一组键。
        put()           将指定的值与此映射中的指定键相关联。如果此映射先前包含此键的映射，则旧值将替换为指定的值。
        size()          返回此地图中的键值映射的数量。
        values()        返回此地图中包含的值的集合视图。
        ```
12. 面向对象：
13. 异常处理：类似Java
## 参考链接
<a href="https://www.w3cschool.cn/groovy/">Groovy基础</a>

https://www.jianshu.com/p/8127742e0569

https://blog.csdn.net/liao_hb/article/details/88690400