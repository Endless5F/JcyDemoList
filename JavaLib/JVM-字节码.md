1. 使用javap -verbose xxx.class 命令分析一个字节码文件时，将会分析该字节码文件的魔数，版本号，常量池，类信息，类的构造方法，类中的方法信息，类变量与成员变量的信息。
2. 魔数：所有的.class文件的前四个字节都是魔数，魔数值为固定值：0xCAFEBABE（咖啡宝贝）
3. 版本号：魔数后面4个字节是版本信息，前两个字节表示minor version（次版本号），后两个字节表示major version（主版本号），十六进制34=十进制52。所以该文件的版本号为1.8.0。低版本的编译器编译的字节码可以在高版本的JVM下运行，反过来则不行。
4. 常量池（constant pool）：版本号之后的就是常量池入口，一个java类定义的很多信息都是由常量池来维护和描述的，可以将常量池看作是class文件的资源仓库，包括java类定义的方法和变量信息，常量池中主要存储两类常量：字面量和符号引用。字面量如文本字符串、java中生命的final常量值等，符号引用如类和接口的全局限定名，字段的名称和描述符，方法的名称和描述符等。
5. 常量池的整体结构：Java类对应的常量池主要由常量池数量和常量池数组两部分共同构成，常量池数量紧跟在主版本号后面，占据两个字节，而常量池数组在常量池数量之后。常量池数组与一般数组不同的是，常量池数组中元素的类型、结构都是不同的，长度当然也就不同，但是每一种元素的第一个数据都是一个u1类型标志位，占据一个字节，JVM在解析常量池时，就会根据这个u1类型的来获取对应的元素的具体类型。 值得注意的是，常量池数组中元素的个数=常量池数-1,（其中0暂时不使用）。目的是满足某些常量池索引值的数据在特定的情况下需要表达不引用任何常量池的含义。根本原因在于索引为0也是一个常量，它是JVM的保留常量，它不位于常量表中。这个常量就对应null，所以常量池的索引从1而非0开始。
6. 在JVM规范中，每个变量/字段都有描述信息，主要的作用是描述字段的数据类型，方法的参数列表（包括数量、类型和顺序）与返回值。根据描述符规则，基本数据类型和代表无返回值的void类型都用一个大写字符来表示，而对象类型使用字符L+对象的全限定名称来表示。为了压缩字节码文件的体积，对于基本数据类型，JVM都只使用一个大写字母来表示。如下所示:B-byte，C-char，D-double，F-float，I-int，J-long，S-short，Z-boolean，V-void，L-对象类型，如Ljava/lang/String;
7. 对于数组类型来说，每一个维度使用一个前置的[ 来表示，如int[]表示为[I ，String [][]被记录为[[Ljava/lang/String;
8. 用描述符描述方法的时候，用先参数列表后返回值的方式来描述。参数列表按照参数的严格顺序放在一组（）之内，如方法：String getNameByID(int id ,String name) —— (I,Ljava/lang/String;)Ljava/lang/String;
9. Class字节码中有两种数据类型：
    1. 字节数据直接量：这是基本的数据类型。共细分为u1、u2、u4、u8四种，分别代表连续的1个字节、2个字节、4个字节、8个字节组成的整体数据。
    2. 表(数组)：表是由多个基本数据或其他表，按照既定顺序组成的大的数据集合。表是有结构的，它的结构体：组成表的成分所在的位置和顺序都是已经严格定义好的。
10. Access Falgs：访问标志信息包括了该class文件是类还是接口，是否被定义成public，是否是abstract，如果是类，是否被定义成final。

        标志名           标志值	        标志含义	                针对的对像
        ACC_PUBLIC	    0x0001	        public类型	            所有类型
        ACC_FINAL	    0x0010	        final类型	            类
        ACC_SUPER	    0x0020	        使用新的invokespecial语义	类和接口
        ACC_INTERFACE   0x0200	        接口类型	                接口
        ACC_ABSTRACT    0x0400	        抽象类型	                类和接口
        ACC_SYNTHETIC   0x1000	        该类不由用户代码生成	    所有类型
        ACC_ANNOTATION  0x2000          注解类型	                注解
        ACC_ENUM        0x4000	        枚举类型	                枚举
    0x0021是0x0020和0x0001的并集，表示ACC_PUBLIC和ACC_SUPER；0x0002:private
11. 字段表（Fields）：字段表用于描述类和接口中声明的变量。这里的字段包含了类级别变量和实例变量，但是不包括方法内部声明的局部变量。

        access_flags 项的值是用于定义字段被访问权限和基础属性的掩码标志。access_flags 的取值范围和相应含义见如下表：
        标志名称	                标志值	                含义
        ACC_PUBLIC              0x00 01	                字段是否为public
        ACC_PRIVATE	            0x00 02	                字段是否为private
        ACC_PROTECTED	        0x00 04	                字段是否为protected
        ACC_STATIC	            0x00 08	                字段是否为static
        ACC_FINAL	            0x00 10	                字段是否为final
        ACC_VOLATILE	        0x00 40	                字段是否为volatile
        ACC_TRANSTENT	        0x00 80	                字段是否为transient
        ACC_SYNCHETIC	        0x10 00	                字段是否为由编译器自动产生
        ACC_ENUM	            0x40 00	                字段是否为enum
12. 方法表
方法的属性结构：
    1. 方法中的每个属性都是一个attribute_info结构:
        1. JVM预定义了部分attribute，但是编译器自己也可以实现自己的attribute写入class文件里，供运行时使用；
        2. 不同的attribute通过attribute_name_index来区分。
    2. attribute_info格式：

            attribute_info{
                u2 attribute_name_index;
                u4 attribute_length;
                u1 info[attribute_length]
            }
    3. attribute_name_index值为Code，则为Code结构

            Code_attribute {
            	u2 attribute_name_index;
            	u4 attribute_length;
            	u2 max_stack;
            	u2 max_locals;
            	u4 code_length;
            	u1 code[code_length];
            	u2 exception_table_length;
            	{   u2 start_pc;
            		u2 end_pc;
            		u2 handler_pc;
            		u2 catch_type;
            	} exception_table[exception_table_length];
            	u2 attributes_count;
            	attribute_info attributes[attributes_count];
            }
            attribute_length：表示attribute所包含的字节数，不包含attribute_name_index和attribute_length
            max_stacks：表示这个方法运行的任何时刻所能达到的操作数栈的最大深度
            max_locals：表示方法执行期间创建的局部变量的数目，包含用来表示传入的参数的局部变量
            code_length：表示该方法所包含的字节码的字节数以及具体的指令码。具体的字节码是指该方法被调用时，虚拟机所执行的字节码
            exception_table：存放处理异常的信息，每个exception_table表，是由start_pc、end_pc、hangder_pc、catch_type组成
            start_pc、end_pc：表示在code数组中从start_pc到end_pc（包含start_pc，不包含end_pc）的指令抛出的异常会由这个表项来处理
            hangder_pc：表示处理异常的代码的开始处。
            catch_type：表示会被处理的异常类型，它指向常量池中的一个异常类。当catch_type=0时，表示处理所有的异常。
    4. 附加其他属性
        1. LineNumbeTable_attribute：这个属性表示code数组中，字节码与java代码行数之间的关系，可以在调试的时候定位代码执行的行数。
        2. LocalVariableTable ：结构类似于 LineNumbeTable_attribute，对于Java中的任何一个非静态方法，至少会有一个局部变量，就是this。
13. 字节码查看工具(jclasslib)：http://github.com/ingokegel/jclasslib
14. Java字节码对于异常的处理方式：
    1. 统一采用异常表的方式来对异常进行处理；
    2. 在jdk1.4.2之前的版本中，并不是使用异常表的方式对异常进行处理的，而是采用特定的指令方式；
    3. 当异常处理存在finally语句块时，现代化的JVM采取的处理方式是将finally语句内的字节码拼接到每个catch语句块后面。也就是说，程序中存在多少个catch，就存在多少个finally块的内容。
15. 栈帧（stack frame）：

        用于帮助虚拟机执行方法调用和方法执行的数据结构
        栈帧本身是一种数据结构，封装了方法的局部变量表，动态链接信息，方法的返回地址以及操作数栈等信息。
        符号引用：符号引用以一组符号来描述所引用的目标。符号引用可以是任何形式的字面量，只要使用时能无歧义地定位到目标即可，符号引用和虚拟机的布局无关。（在编译的时候一个每个java类都会被编译成一个class文件，但在编译的时候虚拟机并不知道所引用类的地址，多以就用符号引用来代替，而在这个解析阶段就是为了把这个符号引用转化成为真正的地址的阶段。）
        直接引用：（1）直接指向目标的指针（指向对象，类变量和类方法的指针）（2）相对偏移量。（指向实例的变量，方法的指针）（3）一个间接定位到对象的句柄。
16. 有些符号引用在加载阶段或者或是第一次使用时，转换为直接引用，这种转换叫做静态解析；另外一些符号引用则是在运行期转换为直接引用，这种转换叫做动态链接。

    执行方法的几种字节码指令：
    1. invokeinterface：调用接口的方法，在运行期决定调用该接口的哪个对象的特定方法。
    2. invokestatic：调用静态方法
    3. invokespecial：调用私有方法， 构造方法（），父类的方法
    4. invokevirtual：调用虚方法，运行期动态查找的过程
    5. invokedynamic：动态调用方法
17. 静态解析的四种情景：静态方法、父类方法、构造方法、私有方法(无法被重写)。以上四种方法称为非虚方法，在类加载阶段将符号引用转换为直接引用。
18. 方法的静态分派：Grandpa g = new Father(); 此代码g的静态类型是Grandpa，而g的实际类型(真正指向的类型)是Father。变量的静态类型是不会发生改变的，而实际类型则可以发生变化(多态的一种体现)，运行期方可确定。方法的重载，是一种静态类型，编译期就可以确定。
19. 方法的动态分派：invokevirtual字节码指令的多态查找流程。调用实例方法，基于类进行分派，先查找实际类型是否有此方法，若有直接调用，若无则查找其父类是否有此方法，重复此步骤，知道查找到此方法并执行。因此，方法的重写，是动态类型，是运行期行为，需要运行期才能确认。
20. 现代JVM执行Java的时候，通常都将解释执行和编译执行二者结合起来进行。
    1. 解释执行：通过解释器读取字节码，遇到相应指令就去执行该指令。
    2. 编译执行：就是通过即时编译器(Just in Time，JIT)，将字节码转换成本地机器码执行。现代JVM会根据代码热点来生成本地机器码。
21. 基于栈的指令集和基于寄存器的指令集之间的关系：
    1. JVM执行指令时所采用的方式是基于栈的指令集。
    2. 基于栈的指令集主要有入栈和出栈两种操作。
    3. 基于栈的指令集的优势在于它可以在不同的平台之间移植，而基于寄存器的指令集和硬件架构紧密相关，无法做到可移植。
    4. 基于栈的指令集的缺点在于完成相同的操作，指令数量通常要比基于寄存器的指令集多。基于栈的指令集是在内存中完成操作的，而基于寄存器的指令集直接由CPU执行，是在高速缓冲中执行的，速度要快很多。虽然虚拟机可以采用一些优化手段，但是总体来说，基于栈的指令集速度还是要慢一些。
22. 从字节码角度理解动态代理

        interface Subject {
            void request();
        }

        class RealSubject implements Subject {
            public void request() {
                System.out.println("real subject");
            }
        }

        class DynamicSubject implements InvocationHandler {
            private Object subject;     //　这个就是我们要代理的真实对象

            public DynamicSubject(Object subject) {
                this.subject = subject;        //    构造方法，给我们要代理的真实对象赋初值
            }

            @Override
            public Object invoke(Object object, Method method, Object[] args)
                    throws Throwable {
                //　在代理真实对象前我们可以添加一些自己的操作
                System.out.println("before rent house");
                System.out.println("Method:" + method);
                // 当代理对象调用真实对象的方法时，其会自动的跳转到代理对象关联的handler对象的invoke方法来进行调用
                method.invoke(subject, args);
                //　在代理真实对象后我们也可以添加一些自己的操作
                System.out.println("after rent house");
                return null;
            }
        }

        public class Client {
            public static void main(String[] args) {
                // 系统属性，一个开关，将动态代理生成的class文件写入到磁盘上
                System.getProperties().put("sun.misc.ProxyGenerator.saveGeneratedFiles", "true");
                // 我们要代理的真实对象
                Subject realSubject = new RealSubject();//这里指定被代理类
                // 我们要代理哪个真实对象，就将该对象传进去，最后是通过该真实对象来调用其方法的
                InvocationHandler handler = new DynamicSubject(realSubject);
                /*
                 * 通过Proxy的newProxyInstance方法来创建我们的代理对象，我们来看看其三个参数
                 * 第一个参数 handler.getClass().getClassLoader()，我们这里使用代理类的类加载器，也就是被代理的那个真实对象
                 * 第二个参数realSubject.getClass().getInterfaces()
                 * ，我们这里为代理对象提供的接口是真实对象所实行的接口，表示我要代理的是该真实对象，这样我就能调用这组接口中的方法了
                 * 第三个参数handler，我们这里将这个代理对象关联到了上方的InvocationHandler 这个对象上
                 */
                Subject subject = (Subject) Proxy.newProxyInstance(realSubject.getClass().getClassLoader(),
                        realSubject.getClass().getInterfaces(), handler);
                subject.request();
            }
        }
    1. 通过改变sun.misc.ProxyGenerator.saveGeneratedFiles系统属性，可以将动态代理生成的class文件输出（默认只在内存，不会写到硬盘），得到Proxy0.class文件，使用反编译工具可以得到 Proxy0.class文件，使用反编译工具可以得到Proxy0.class文件，使用反编译工具可以得到Proxy0的文件如下：

            final class $Proxy0 extends Proxy implements Subject {
                private static Method m1;
                private static Method m2;
                private static Method m3;
                private static Method m0;

                public $Proxy0(InvocationHandler paramInvocationHandler) {
                    super(paramInvocationHandler);
                }

                public final boolean equals(Object paramObject) {
                    try {
                        return ((Boolean) this.h.invoke(this, m1, new Object[]{paramObject})).booleanValue();
                    } catch (Error | RuntimeException localError) {
                        throw localError;
                    } catch (Throwable localThrowable) {
                        throw new UndeclaredThrowableException(localThrowable);
                    }
                }

                public final String toString() {
                    try {
                        return (String) this.h.invoke(this, m2, null);
                    } catch (Error | RuntimeException localError) {
                        throw localError;
                    } catch (Throwable localThrowable) {
                        throw new UndeclaredThrowableException(localThrowable);
                    }
                }

                public final void request() {
                    try {
                        this.h.invoke(this, m3, null);
                        return;
                    } catch (Error | RuntimeException localError) {
                        throw localError;
                    } catch (Throwable localThrowable) {
                        throw new UndeclaredThrowableException(localThrowable);
                    }
                }

                public final int hashCode() {
                    try {
                        return ((Integer) this.h.invoke(this, m0, null)).intValue();
                    } catch (Error | RuntimeException localError) {
                        throw localError;
                    } catch (Throwable localThrowable) {
                        throw new UndeclaredThrowableException(localThrowable);
                    }
                }

                static {
                    try {
                        m1 = Class.forName("java.lang.Object").getMethod("equals", new Class[]{Class.forName("java.lang.Object")});
                        m2 = Class.forName("java.lang.Object").getMethod("toString", new Class[0]);
                        m3 = Class.forName("controller.Subject").getMethod("request", new Class[0]);
                        m0 = Class.forName("java.lang.Object").getMethod("hashCode", new Class[0]);
                        return;
                    } catch (NoSuchMethodException localNoSuchMethodException) {
                        throw new NoSuchMethodError(localNoSuchMethodException.getMessage());
                    } catch (ClassNotFoundException localClassNotFoundException) {
                        throw new NoClassDefFoundError(localClassNotFoundException.getMessage());
                    }
                }
            }
    2. 由反编译$Proxy0.class文件内容，可轻易得知：
        1. $Proxy0是Proxy的子类，实现Subject接口；
        2. $Proxy0重写了Object中的equal()、toString()、hashCode()方法；
        3. 在调用$Proxy0中方法时，实际上调用父类Proxy的h属性的invoke()方法，其中h为Proxy.newInstance()方法传入的DynamicSubject，也就是说最终会调用DynamicSubject的invoke()方法，并将参数传入—this.h.invoke(this, m3, null);


