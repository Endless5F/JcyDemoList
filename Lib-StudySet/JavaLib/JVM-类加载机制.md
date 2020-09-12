1. 类加载(class loading0)
    1. 在java代码中，类的加载、连接和初始化过程都是在程序运行期间完成的。（类从磁盘加载到内存中经历的三个阶段）
    2. 提供了更大的灵活性，增加了更多的可能性。
2. 类加载器深入剖析：
    1. Java虚拟机与程序的生命周期
    2. 在如下几种情况下，java虚拟机将结束生命周期
       1. 执行了System.exit()方法
       2. 程序正常执行结束
       3. 程序在执行过程中遇到了异常或错误而异常终止
       4. 由于操作系统出现错误而导致虚拟机进程终止
3. 类的加载、连接与初始化：
    1. 加载：查找并加载类的二进制数据到java虚拟机中
    2. 连接：
        1. 验证 : 确保被加载的类的正确性
        2. 准备：为类的静态变量分配内存，并将其初始化为默认值，但是到达初始化之前类变量都没有初始化为真正的初始值
        3. 解析：把类中的符号引用转换为直接引用，就是在类型的常量池中寻找类、接口、字段和方法的符号引用，把这些符号引用替换成直接引用的过程
    3. 初始化：为类的静态变量赋予正确的初始值
4. 类从磁盘上加载到内存中要经历五个阶段：加载、连接、初始化(类的初始化和对象的初始化概念不一样)、使用、卸载
    1. Java程序对类的使用方式可分为两种
        1. 主动使用
        2. 被动使用
    2. 所有的Java虚拟机实现必须在每个类或接口被Java程序“首次主动使用”时才能初始化他们
    3. 主动使用（七种）
        1. 创建类的实例
        2. 访问某个类或接口的静态变量 getstatic（助记符），或者对该静态变量赋值 putstatic
        3. 调用类的静态方法 invokestatic
        4. 反射（Class.forName(“com.test.Test”)）
        5. 初始化一个类的子类
        6. Java虚拟机启动时被标明启动类的类
        7. JDK1.7开始提供的动态语言支持（了解）
    4. 被动使用：除了上面七种情况外，其他使用java类的方式都被看做是对类的被动使用，都不会导致类的初始化
5. 类的加载详解：
    1. 类的加载指的是将类的.class文件中的二进制数据读入到内存中，将其放在运行时数据区的方法区内，然后在内存中创建一个java.lang.Class对象（规范并未说明Class对象位于哪里，HotSpot虚拟机将其放在方法区中）用来封装内在方法区内的数据结构。
    2. 加载.calss文件的方式
        1. 从本地系统中直接加载
        2. 通过网络下载.class文件
        3. 从zip，jar等归档文件中加载.class文件
        4. 从专用数据库中提取.class文件
        5. 将java源文件动态编译为.class文件 （动态代理）
6. 示例代码说明

        示例一：类的主动使用和被动使用
        /**
         * 对于静态字段来说，只有直接定义了该字段的类才会被初始化
         * 当一个类在初始化时，要求父类全部都已经初始化完毕
         * <p>
         * -XX:+TraceClassLoading，用于追踪类的加载信息并打印出来
         * <p>
         * -XX:+<option>，表示开启option选项
         * -XX:-<option>，表示关闭option选项
         * -XX:<option>=value，表示将option的值设置为value
         */
        public class MyTest {
            public static void main(String[] args) {
                System.out.println(MyChild.str);    //输出：MyParent static block 、 hello world（因为对MyChild不是主动使用）
                System.out.println(MyChild.str2);  //输出：MyParent static block  、MyChild static block、welcome
            }
        }

        class MyParent {
            public static String str = "hello world";

            static {
                System.out.println("MyParent static block");
            }
        }

        示例二：使用编译器可确认的(即，编译期就可确认其值的)static final 类型的常量，并不会引起类的初始化
        /**
         * 常量在编译阶段会存入到调用这个常量的方法所在的类的常量池中
         * 本质上，调用类并没有直接调用到定义常量的类，因此并不会触发定义常量的类的初始化
         * 注意：这里指的是将常量存到MyTest2的常量池中，之后MyTest2和MyParent就没有任何关系了。
         * 甚至我们可以将MyParent2的class文件删除
         * <p>
         * 助记符 ldc：表示将int、float或者String类型的常量值从常量池中推送至栈顶
         * 助记符 bipush：表示将单字节（-128-127）的常量值推送到栈顶
         * 助记符 sipush：表示将一个短整型值（-32768-32369）推送至栈顶
         * 助记符 iconst_1：表示将int型的1推送至栈顶（iconst_m1到iconst_5）
         */
        public class MyTest2 {
            public static void main(String[] args) {
                System.out.println(MyParent2.str);    //输出 hello world
                System.out.println(MyParent2.s);
                System.out.println(MyParent2.i);
                System.out.println(MyParent2.j);
            }
        }

        class MyParent2 {
            public static final String str = "hello world";
            public static final short s = 7;
            public static final int i = 129;
            public static final int j = 1;

            static {
                System.out.println("MyParent static block");
            }
        }

        示例三：使用编译器不可确认的(即，编译期不可确认其值的)static final 类型的常量，会引起类的初始化
        /**
         * 当一个常量的值并非编译期间可以确定的，那么其值就不会放到调用类的常量池中
         * 这时在程序运行时，会导致主动使用这个常量所在的类，显然会导致这个类被初始化
         */
        public class MyTest3 {
            public static void main(String[] args) {
                System.out.println(MyParent3.str);  //输出MyParent static block、kjqhdun-baoje21w-jxqioj1-2jwejc9029
            }
        }

        class MyParent3 {
            public static final String str = UUID.randomUUID().toString();

            static {
                System.out.println("MyParent static block");
            }
        }

        示例四：对象的初始化会引起类的初始化，但是对象数组的初始化(此时只是初始化，并未使用)并不会引起类的初始化。
        /**
         * 对于数组实例来说，其类型是由JVM在运行期动态生成的，表示为 [L com.hisense.classloader.MyParent4 这种形式。
         * 对于数组来说，JavaDoc经构成数据的元素成为Component，实际上是将数组降低一个维度后的类型。
         * <p>
         * 助记符：anewarray：表示创建一个引用类型（如类、接口）的数组，并将其引用值压入栈顶
         * 助记符：newarray：表示创建一个指定原始类型（int boolean float double）d的数组，并将其引用值压入栈顶
         */
        public class MyTest4 {
            public static void main(String[] args) {
                MyParent4 myParent4 = new MyParent4();        //创建类的实例，属于主动使用，会导致类的初始化
                MyParent4[] myParent4s = new MyParent4[1];    //不是主动使用
                System.out.println(myParent4s.getClass());          //输出 [L com.hisense.classloader
                // .MyParent4
                System.out.println(myParent4s.getClass().getSuperClass());    //输出Object

                int[] i = new int[1];
                System.out.println(i.getClass());          //输出 [ I
                System.out.println(i.getClass().getSuperClass());    //输出Object
            }
        }

        class MyParent4 {
            static {
                System.out.println("MyParent static block");
            }
        }

        示例五：使用自接口中自己的静态变量和可确认的常量时，并不会引起父接口的初始化
        /**
         * 当一个接口在初始化时，并不要求其父接口都完成了初始化
         * 只有在真正使用到父接口的时候（如引用接口中定义的常量），才会初始化
         */
        public class MyTest5 {
            public static void main(String[] args) {
                System.out.println(MyChild5.b)
            }
        }

        interface MParent5 {
            public static Thread thread = new thread() {
                System.out.println(" MParent5 invoke")
            };
        }

        interface MyChild5 extends MParent5 {     //接口属性默认是 public static final
            public static int b = 6;
        }

        示例六：准备阶段会为静态变量附默认值，只有类初始化阶段才会为静态变量附真实值，赋值过程自上而下(即，若此静态变量在类中的位置在构造方法下边，则后给静态变量附真实值)
        /**
         * 准备阶段和初始化的顺序问题
         */
        public class MyTest6 {
            public static void main(String[] args) {
                Singleton Singleton = Singleton.getInstance();
                System.out.println(Singleton.counter1);     //输出1，1
                System.out.println(Singleton.counter2);
            }
        }

        class Singleton {
            public static int counter1;
            public static int counter2 = 0;               /
            private static Singleton singleton = new Singleton();

            private Singleton() {
                counter1++;
                counter2++;
            }

            // public static int counter2=0;       //   若改变此赋值语句的位置，输出  1，0
            public static Singleton getInstance() {
                return singleton;
            }
        }
7. 类加载器
    1. Java虚拟机自带的加载器
        1. 根类加载器（Bootstrap）：该加载器没有父加载器，它负责加载虚拟机中的核心类库。此加载器由C++实现，不是ClassLoader的子类。根类加载器从系统属性sun.boot.class.path所指定的目录中加载类库。类加载器的实现依赖于底层操作系统，属于虚拟机的实现的一部分，它并没有集成java.lang.ClassLoader类。
        2. 扩展类加载器（Extension）：它的父加载器为根类加载器。它从java.ext.dirs系统属性所指定的目录中加载类库，或者从JDK的安装目录的jre\lib\ext子目录（扩展目录）下加载类库，如果把用户创建的jar文件放在这个目录下，也会自动由扩展类加载器加载，扩展类加载器是纯java类，是java.lang.ClassLoader的子类。
        3. 系统应用类加载器（System）：是指 Sun公司实现的sun.misc.Launcher$AppClassLoader，也称为应用类加载器，它的父加载器为扩展类加载器，它从环境变量classpath或者系统属性java.class.path所指定的目录中加载类，他是用户自定义的类加载器的默认父加载器。系统类加载器时纯java类，是java.lang.ClassLoader的子类。
    2. 用户自定义的类加载器
        1. java.lang.ClassLoader的子类
        2. 用户可以定制类的加载方式
    3. 类加载器用来把类加载到java虚拟机中。从JDK1.2版本开始，类的加载过程采用父亲委托机制，这种机制能更好地保证Java平台的安全。在此委托机制中，除了java虚拟机自带的根类加载器以外，其余的类加载器都有且只有一个父加载器。当java程序请求加载器loader1加载Sample类时，loader1首先委托自己的父加载器去加载Sample类，若父加载器能加载，则有父加载器完成加载任务，否则才由加载器loader1本身加载Sample类。
8. 类的加载
    1. 类加载器并不需要等到某个类被“首次主动使用”时再加载它
    2. JVM规范允许类加载器在预料某个类将要被使用时就预先加载它，如果在预先加载的过程中遇到了.class文件缺失或存在错误，类加载器必须在程序首次主动使用该类才报告错误（LinkageError错误），如果这个类没有被程序主动使用，那么类加载器就不会报告错误。
9. 类的连接
    1. 类被加载后，就进入连接阶段。连接阶段就是将已经读入到内存的类的二进制数据合并到虚拟机的运行时环境中去。
    2. 类的连接-验证
       1. 类文件的结构检查
       2. 语义检查
       3. 字节码验证
       4. 二进制兼容性的验证
    3. 类的连接-准备：在准备阶段，java虚拟机为类的静态变量分配内存，并设置默认的初始值。例如：在准备阶段，将为int类型的静态变量a分配4个字节的内存空间，并且赋予默认值0，为long类型的静态变量b分配8个字节的内存空间，并且赋予默认值0；
    4. 初始化：在初始化阶段，Java虚拟机执行类的初始化语句，为类的静态变量赋予初始值。在程序中，静态变量的初始化有两种途径：（1）在静态变量的声明处进行初始化；（2）在静态代码块中进行初始化。
        1. 类的初始化步骤：
            1. 假如这个类还没有被加载和连接，那就先进行加载和连接
            2. 假如类存在直接父类，并且这个父类还没有被初始化，那就先初始化直接父类
            3. 假如类中存在初始化语句，那就依次执行这些初始化语句
        2. 当java虚拟机初始化一个类时，要求它的所有父类都已经被初始化，但是这条规则不适用于接口。因此，一个父接口并不会因为它的子接口或者实现类的初始化而初始化。只有当程序首次使用特定的接口的静态变量时，才会导致该接口的初始化。
        3. ·调用ClassLoader类的loadClass方法加载一个类，并不是对类的主动使用，不会导致类的初始化。
10. 类加载器的父亲委托机制：在父亲委托机制中，各个加载器按照父子关系形成了树形结构，除了根加载器之外，其余的类加载器都有一个父加载器
    1. 若有一个类能够成功加载Test类(我们自己写的)，那么这个类加载器被称为定义类加载器，所有能成功返回Class对象引用的类加载器（包括定义类加载器）称为初始类加载器。
11. 类加载器的示例

        示例一：根加载器和系统应用类加载器
        /**
         * java.lang.String是由根加载器加载，在rt.jar包下
         * 调用ClassLoader的loaderClass方法加载一个类，并不是对类的主动使用，不会导致类的初始化
         */
        public class MyTest8 {
            public static void main (String[]args){
                Class<?> clazz = Class.forName("java.lang.String");
                System.out.println(clazz.getClassLoader());  //返回null
                Class<?> clazz2 = Class.forName("C");
                System.out.println(clazz2.getClassLoader());  //输出sun.misc.Launcher$AppClassLoader@18b4aac2  其中AppClassLoader:系统应用类加载器

                ClassLoader loader = ClassLoader.getSystemClassLoader();
                Class<?> clazz1 = loader.loadClass("CL"); //不会初始化
                System.out.println(clazz1);
                System.out.println("-------------------");
                Class<?> clazz = Class.forName("CL");
                System.out.println(clazz);  //反射初始化

                ClassLoader loader=ClassLoader.getSystemClassLoader();
                System.out.println(loader); // 输出AppClassLoader
                while(loader!=null){
                    // 获取加载器的父类
                    loader=loader.getParent();
                    System.out.println(loader); // 输出ExtClassLoader和null(根加载器)
                }
            }
        }

        class CL {
            static {
                System.out.println("FinalTest static block);
            }
        }

        示例二：根据当前线程的上下文类加载器，输入指定类的字节码全路径
        public class MyTest14 {
            public static void main(String[] args) {
                ClassLoader loader = Thread.currentThread().getContextClassLoader();
                System.out.println(loader);         //输出AppClassLoader
                //字节码资源路径
                String resourceName = "com/android/javalib/jvm/JvmMemoryClass.class";
                Enumeration<URL> urls = loader.getResources(resourceName);
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    System.out.println(url);
                }
            }
        }

        示例三：数组加载器
        /**
         * 对于数组，它对应的class对象不是由类加载器加载，而是由JVM
         * 在运行期动态的创建。然而对于数组类的类加载器来说，它返回的类加载器和数组内元素的类加载器是一样的。如果数组类元素是原生类，那么数组是没有类加载器的。
         */
        public class MyTest15 {
            public static void main(String[] args) {
                String[] strings = new String[2];
                System.out.println(strings.getClass());
                System.out.println(strings.getClass().getClassLoader());    //输出null，即此加载器为启动类加载器或者根加载器

                MyTest15[] mytest15 = new MyTest15[2];
                System.out.println(mytest15.getClass().getClassLoader());   //输出应用类加载器

                int[] arr = new int[2];
                System.out.println(arr.getClass().getClassLoader());        //输出null，此null非彼null，基础数据类型数组，没有加载器
            }
        }
12. 获取类加载器的途径：
    1. clazz.getClassLoader(); --获取当前类的加载器
    2. Thread.currentThread().getContextClassLoader(); --获取当前线程上下文的加载器
    3. ClassLoader.getSystemClassLoader(); --获取系统的加载器
    4. DriverManager.getCallerClassLoader(); --获取调用者的加载器
13. 自定义加载器

        /**
                  * 创建自定义加载器，继承ClassLoader
                  */
                 public class MyTest16 extends ClassLoader {
                     private String classLoaderName;
                     private String path;
                     private final String fileExtension = ".class";

                     public MyTest16(String classLoaderName) {
                         super();        //将系统类当做该类的父加载器
                         this.classLoaderName = classLoaderName;
                     }

                     public MyTest16(ClassLoader parent, String classLoaderName) {
                         super(parent);      //显式指定该类的父加载器
                         this.classLoaderName = classLoaderName;
                     }

                     public MyTest16(ClassLoader parent) {
                         super(parent);      //显式指定该类的父加载器
                     }

                     // 设置Class文件包所在的路径
                     public void setPath(String path) {
                         this.path = path;
                     }

                     @Override
                     public Class <?> findClass(String className) {
                         System.out.println("calssName=" + className);
                         className = className.replace(".", File.separator);
                         byte[] data = loadClassData(className);
                         return defineClass(className, data, 0, data.length); //define方法为父类方法
                     }

                     private byte[] loadClassData(String name) {
                         InputStream is = null;
                         byte[] data = null;
                         ByteArrayOutputStream baos = null;
                         try {
                             is = new FileInputStream(new File(this.path + name + this.fileExtension));
                             baos = new ByteArrayOutputStream();
                             int ch;
                             while (-1 != (ch = is.read())) {
                                 baos.write(ch);
                             }
                             data = baos.toByteArray();

                         } catch (Exception e) {
                             e.printStackTrace();
                         } finally {
                             try {
                                 is.close();
                                 baos.close();
                             } catch (IOException e) {
                                 e.printStackTrace();
                             }
                             return data;
                         }
                     }

                     public static void test(ClassLoader classLoader) {
                         Class<?> clazz = null;
                         try {
                             clazz = classLoader.loadClass("com.android.javalib.jvm.JvmMemoryClass");
                             //loadClass是父类方法，在方法内部调用findClass
                             System.out.println(clazz.hashCode());
                             Object object = clazz.newInstance();
                             System.out.println(object);
                         } catch (ClassNotFoundException e) {
                             e.printStackTrace();
                         } catch (IllegalAccessException e) {
                             e.printStackTrace();
                         } catch (InstantiationException e) {
                             e.printStackTrace();
                         }
                     }

                     public static void main(String[] args) {
                         //父亲是系统类加载器，根据父类委托机制，MyTest1由系统类加载器加载了
                         MyTest16 loader1 = new MyTest16("loader1");
                         test(loader1);
                     }
                 }
14. 类的命名空间
    1. 每个类加载器都有自己的命名空间，命名空间由该加载器及所有父加载器所加载的类构成；
    2. 在同一个命名空间中，不会出现类的完整名字（包括类的包名）相同的两个类；
    3. 在不同的命名空间中，有可能会出现类的完整名字（包括类的包名）相同的两个类；
    4. 同一命名空间内的类是互相可见的，非同一命名空间内的类是不可见的；
    5. 子加载器可以见到父加载器加载的类，父加载器不能见到子加载器加载的类。

15. 类的卸载
    1. 当一个类被加载、连接和初始化之后，它的生命周期就开始了。当此类的Class对象不再被引用，即不可触及时，Class对象就会结束生命周期，类在方法区内的数据也会被卸载。
    2. 一个类何时结束生命周期，取决于代表它的Class对象何时结束生命周期。
    3. 由Java虚拟机自带的类加载器所加载的类，在虚拟机的生命周期中，始终不会被卸载。Java虚拟机本身会始终引用这些加载器，而这些类加载器则会始终引用他们所加载的类的Class对象，因此这些Class对象是可触及的。
    4. 由用户自定义的类加载器所加载的类是可以被卸载的。
    5. 查看类的卸载：1.配置Jvm选项 -XX:+TraceClassLoading；2.将某对象置空；3.调用System.gc();
16. 三种类加载器所加载类的路径

        /**
         * 在运行期，一个Java类是由该类的完全限定名（binary name）和用于加载该类的定义类加载器所共同决定的。
         * 如果同样名字（完全相同限定名）是由两个不同的加载器所加载，那么这些类就是不同的，即便.class文件字节码相同，并且从相同的位置加载亦如此。
         * <p>
         * 在oracle的hotspot，系统属性sun.boot.class.path如果修改错了，则运行会出错：
         * Error occurred during initialization of VM
         * java/lang/NoClassDeFoundError: java/lang/Object
         */
        public class MyTest18 {
            public static void main(String[] args) {
                System.out.println(System.getProperty("sun.boot.class.path"));//结果为根加载器路径
                System.out.println(System.getProperty("java.ext.dirs"));//结果为扩展类加载器路径
                System.out.println(System.getProperty("java.calss.path"));//结果为应用类加载器路径

                /*
                 *
                 * 类加载器本身也是类加载器，类加载器又是谁加载的呢？
                 *  类加载器是由启动类加载器去加载的，启动类加载器是C++写的，内嵌在JVM中。
                 *  内嵌于JVM中的启动类加载器会加载java.lang.ClassLoader以及其他的Java平台类。当JVM启动时，一块特殊的机器码会运行，它会加载扩展类加载器以及系统类加载器，这块特殊的机器码叫做启动类加载器。
                 *  启动类加载器并不是java类，其他的加载器都是java类。
                 *  启动类加载器是特定于平台的机器指令，它负责开启整个加载过程。
                 *  启动加载器还会负责加载提供JRE正常运行所需要的基本组件，这包括java.util与java.lang包中的类等等。
                 *
                 */
            }
        }
17. 自定义类加载器命名空间可能导致的问题以及双亲委托模型的优点

        /**
         * 类加载器双亲委托模型的好处：
         * 1. 可以确保Java和核心库的安全：所有的Java应用都会引用java.lang中的类，也就是说在运行期java.lang中的类会被加载到虚拟机中，
         * 如果这个加载过程如果是由自己的类加载器所加载，那么很可能就会在JVM中存在多个版本的java.lang中的类，而且这些类是相互不可见的（命名空间的作用）。
         * 借助于双亲委托机制，Java核心类库中的类的加载工作都是由启动根加载器去加载，从而确保了Java应用所使用的的都是同一个版本的Java核心类库，他们之间是相互兼容的；
         * 2. 确保Java核心类库中的类不会被自定义的类所替代；
         * 3. 同的类加载器可以为相同名称的类（binary name）创建额外的命名空间。相同名称的类可以并存在Java虚拟机中，只需要用不同的类加载器去加载即可。相当于在Java
         * 虚拟机内部建立了一个又一个相互隔离的Java类空间。
         */
        public class MyTest21 {
            public static void main(String[] args) {
                MyTest16 loader1 = new MyTest16("loader1");
                MyTest16 loader2 = new MyTest16("loader2");
                loader1.setPath("C:\Users\xxx\Desktop");
                loader2.setPath("C:\Users\xxx\Desktop");
                // Build后删掉classpath下的Person.class文件
                // 以保证应用类加载器加载的路径下不存在Person.class，此时就可以满足使用自己的类加载MyTest16来加载Person类
                Class<?> clazz1 = loader1.loadClass("Person");
                Class<?> clazz2 = loader2.loadClass("Person");
                //clazz1和clazz由loader1和loader2加载，两者不存在父子委托，属于两个不同的加载器，因此结果为false
                System.out.println(clazz1 == clazz2);
                // 由于loader1和loader2属于两个不同的加载器，因此object1和object2也属于不同的对象(由于命名空间引起)
                Object object1 = clazz1.getInstance();
                Object object2 = clazz2.getInstance();

                Method method = clazz1.getMethod("setPerson", Object.class);
                //此处报错，loader1和loader2所处不用的命名空间
                method.invoke(object1, object2);
            }
        }

        class Person {
            private Person person;

            public void setPerson(Object object) {
                // 此处强制转换会报错
                this.person = (Person) object;
            }
        }
18. 线程上下文加载器

        示例一：初识线程上下文加载器
        /**
         * 当前类加载器(Current ClassLoader)
         * 每个类都会尝试使用自己的类加载器去加载依赖的类。
         * <p>
         * 线程上下文类加载器(Context ClassLoader)
         * 线程上下文加载器 @ jdk1.2
         * 线程类中的 getContextClassLoader() 与 setContextClassLoader(ClassLoader c)
         * 如果没有通过setContextClassLoader()
         * 方法设置，线程将继承父线程的上下文类加载器，java应用运行时的初始线程的上下文类加载器是系统类加载器。该线程中运行的代码可以通过该类加载器加载类和资源。
         * <p>
         * 线程上下文类加载器的作用：
         * SPI：Service Provide Interface
         * 父ClassLoader可以使用当前线程Thread.currentThread().getContextClassLoader()
         * 所制定的ClassLoader加载的类，这就改变了父加载器加载的类无法使用子加载器或是其他没有父子关系的ClassLoader加载的类的情况，即改变了双亲委托模型。
         * <p>
         * 在双亲委托模型下，类加载是由下至上的，即下层的类加载器会委托父加载器进行加载。但是对于SPI来说，有些接口是Java核心库所提供的的（如JDBC），Java
         * 核心库是由启动类记载器去加载的，而这些接口的实现却来自不同的jar包（厂商提供），Java的启动类加载器是不会加载其他来源的jar包，这样传统的双亲委托模型就无法满足SPI
         * 的要求。通过给当前线程设置上下文类加载器，就可以由设置的上下文类加载器来实现对于接口实现类的加载。
         */
        public class MyTest24 {
            public static void main(String[] args) {
                System.out.println(Thread.currentThread().getContextClassLoader());
                System.out.println(Thread.class.getClassLoader());
            }
        }

        示例二：线程上下文的使用
        /**
         * 线程上下文类加载器的一般使用模式：（获取-使用-还原）
         * // 获取线程上下文加载器
         * ClassLoader classLoader=Thread.currentThread().getContextLoader();
         * try{
         * // 设置目标加载器
         * Thread.currentThread().setContextLoader("目标加载器");
         * myMethod(); // 通过线程上下文加载器使用目标加载器
         * }finally{
         * // 还原线程上下文加载器
         * Thread.currentThread().setContextLoader(classLoader);
         * }
         * 在myMethod中调用Thread.currentThread().getContextLoader()做某些事情
         * ContextClassLoader的目的就是为了破坏类加载委托机制
         * <p>
         * 在SPI接口的代码中，使用线程上下文类加载器就可以成功的加载到SPI的实现类。
         * <p>
         * 当高层提供了统一的接口让底层去实现，同时又要在高层加载（或实例化）底层的类时，就必须通过上下文类加载器来帮助高层的ClassLoader找到并加载该类。
         */
        public class MyTest26 {
            public static void main(String[] args) {

                //一旦加入下面此行，将使用ExtClassLoader去加载Driver.class， ExtClassLoader不会去加载classpath，因此无法找到MySql的相关驱动。
                //Thread.getCurrentThread().setContextClassLoader(MyTest26.class.getClassLoader().parent());

                // 需要先添加依赖 implementation "mysql:mysql-connector-java:5.1.34"
                // JDBC ServiceLoader服务提供者，加载实现的服务   Driver：sql驱动
                ServiceLoader<Driver> loader = ServiceLoader.load(Driver.class);
                Iterator<Driver> iterator = loader.iterator();
                while (iterator.hasNext()) {
                    Driver driver = iterator.next();
                    System.out.println("driver:" + driver.class + ",loader" + driver.class.getClassLoader());
                }
                System.out.println("当前上下文加载器" + Thread.currentThread().getContextClassLoader());
                System.out.println("ServiceLoader的加载器" + ServiceLoader.class.getClassLoader());
            }
        }
19. jar hell(冲突)问题以及解决办法

        // 当一个类或者一个资源文件存在多个jar中，就会存在jar hell问题。可通过以下代码解决问题：
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String resource = "java/lang/String.class";
        Enumeration<URL> urls = classLoader.getResources(resource);
        while (urls.hasMoreElements()) {
            URL element = urls.nextElement();
            System.out.println(element);
        }

