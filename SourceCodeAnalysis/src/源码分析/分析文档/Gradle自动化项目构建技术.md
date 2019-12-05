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