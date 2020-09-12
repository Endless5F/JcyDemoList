<?php
/**
 * Content：mysql命令行操作
 */
#数据库系统级别命令行操作
/*
 * 停止数据库服务：net stop mysql
 * 开始数据库服务：net start mysql
 *      停止开启服务（必须是管理员身份）：即打开命令行的方式是以管理员的身份打开
 *
 * 登录数据库操作系统：mysql -hlocalhost -uroot [-P端口号：3306默认] -p
 *      Enter password: ****（填写密码）
 * -h：服务器地址  -u：用户名  -p：密码
 *
 * 退出：quit;或者exit;
 *
 * 注意：登录数据库系统成功后，需要使用set names 编码名(一般使用utf8); 来设定当前跟数据库打交道的客户端编码
 *
 * 数据备份(为了携带和传送)：mysqldump -h服务器地址 -u登录名 -p(密码)  数据库名>文件名
 *          注意：mysqldump -h服务器地址 -u登录名 -p(密码)  数据库名>文件名 后面没有;号
 *      数据备份必须是管理员身份：即打开命令行的方式是以管理员的身份打开
 *      没有报错就是备份正确的，文件路径使用完整路径
 * 数据恢复(恢复为完整数据库)：mysql -h服务器地址 -u登录名 -p(密码)  数据库名<文件名(db.sql)
 *      密码是可以最后输入，恢复文件名可以是之前备份时的名字，也可以是一个新文件名
 * 成功登陆mysql数据恢复：source 文件完整路径;
 * */
#数据库定义语句命令行操作
/*
 * 显示mysql中字符编码种类（39种）：show charset;
 * 显示mysql中所有可用排序规则：show collation;
 *
 * 创建数据库：create database 数据库名 [charset 字符编码名称(通常utf8)] [collate 排序规则];
 *      排序规则（基本每种字符编码有两种排序方式）：大小关系（先后顺序）--》默认不写
 *          例：create database db1 charset utf8 collate utf8_general_ci;
 * 删除数据库：drop database [if exists] 数据库名;
 *      [if exists]是一种安全运行，即使数据库不存在也不会报错
 *
 * 修改数据库(字符编码)：基本上就是修改数据库的属性：只有2个
 *      修改编码、修改排序规则：alter database 数据库名 charset 新的编码名 collate 新的规则名;
 *
 * 显示所有数据库： show databases;
 *
 * 显示一个数据库的创建语句：show create database 数据库名;
 *
 *进入（选择）某个数据库：use 数据库名;
 *
 * */
# 字段类型（数据类型）：三大类常用（理论一行最多65535）
/*
 * 数字型（int最常用）：tinyint（占一个字节，8位）、int（占4个字节，32位）、float、decimal(定点)
 *      create table 表名(f1 int,f2 tinyint,f3 bigint);基本情况
 *      create table 表名(f1 int unsigned,
 *                        f2 tinyint zerofill,
 *                        f3 bigint(10) zerofill);
 *          unsigned:无符号数，表示其中的数值是‘非负’数字
 *          zerofill：如果类型后没有设类似于‘(M)--》显示长度，
 *              会默认将所有数左边补0到该类型长度的最大位数’的，表示具备unsigned修饰
 *      decimal(定点数)：整数可以存到65位小数部分可以有30位
 *      create table 表名(f1 float,f2 double,f3 decimal(10,3));
 *          decimal(10,3)表示整数部分有7位，小数部分有3位
 * 字符型（varchar最常用）：set、enum、varchar、char  （mysql字符型字符应该使用"单引号"引起来）
 *      create table 表名(postcode char(6),name varchar(10));
 *      varchar类型：可变类型字符串，使用时必须设定其长度，理论最大65535，实际最大65533，原因：会有两个字符来存储varchar类型字符里的实际长度
 *              但考虑到存储编码不同，会进一步减少，比如：中文gbk，最多65533/2；中文utf8，最多65533/3个；
 *      char类型：定长字符串，默认长度1，理论最长255，一般存储固定长度的字符，比如手机号；也可存比设置少的字符但是会以空格补满
 *      text类型：长文本类型：通常其中存储数据不占表格中的数据容量限制，其本身最长可存储65535个字符
 *          text不占本行空间
 *
 *      create table enum_set(id int auto_increment primary key,sex enum('男','女'),fav set('篮球','排球','足球','台球','乒乓球','羽毛球'));
 *      enum类型：单选项字符串，非常适合存储表单界面的“单选项值”，enum类型选项值下标为1,2,3,4,5...最多65535个
 *      set类型：多选项字符串，非常适合存储表单界面的“多选项值”  set类型对应的选项值下标为1,2,4,8,16,32,64...最多64个
 *          enum和set类型插入数据时，可使用想插入数据的角标进行存入数据库
 *      insert into enum_set(id,sex,fav)values(null,'男','乒乓球');
 *      insert into enum_set(id,sex,fav)values(null,1,16);同上
 *      insert into enum_set(id,sex,fav)values(null,'男','篮球,乒乓球,羽毛球');
 *      insert into enum_set(id,sex,fav)values(null,1,49);同上（49=1+16+32）--》不会出现问题
 *
 *      其他（了解）
 *          binary：定长二进制字符串类型，存储二进制值
 *          varbinary：变长二进制字符串类型
 *          blob类型：二进制数据类型。存二进制值，但是其适用存储图片、其他文件等，但极少用
 *
 * 时间型（datetime最常用）：timestamp、date、datetime
 *      datetime类型：时间日期
 *      date类型：日期
 *      time类型：时间
 *      year类型：年份
 *      timestamp类型：时间戳
 *      create table date_time(dt datetime,d date,t time,y year,ts timestamp);
 *      insert into date_time(dt,d,t,y)values('2017-8-25 12:12','2017-8-25','12:12','2017');
 *      insert into date_time(dt,d,t,y)values(now(),now(),now(),'2017');
 *
 * */
#PHP中操作mysql数据库函数
/*
 * 万能数据库访问方法
 *      $result=mysql_query("select/update/insert/desc/show tables/drop...");
 *          增删改，创建等返回值为true和false
 *          查询、show tables、show databases、desc table等返回值失败false成功返回‘结果集（包括表结构等）’
 * 连接数据库系统
 *      $link=mysql_connect("数据库服务器地址","用户名","密码");
 * 设置页面连接编码
 *      mysql_query("set names 网页文件编码");
 *      mysql_set_charset("网页文件编码");
 * 连接数据库
 *      mysql_query("use 数据库名");
 *      mysql_select_db("数据库名");
 *  获取mysql执行失败时的错误信息
 *      mysql_error();
 *  获取表里数据 $result返回的是结果集
 *      $result=mysql_query("select * from xiaoshu");
 *  获取结果集中的一行数据$res,该$res是一个数组
 *      $res=mysql_fetch_assoc($result);//访问该数组下标必须是数据库行字段名
 *      $res=mysql_fetch_row($result);//访问该数组必须是0,1,2...
 *      $res=mysql_fetch_array($result);//同时拥有上面两种方式
 *      $res=mysql_fetch_object($result);//将资源匹配成对象
 *      while($res=mysql_fetch_array($result)){//遍历结果集
 *          print_r($res);
 *          var_dump($res);
 *      }
 * */
#扩展PHP中操作mysql的几个函数
/*
 * 获取结果集数据的行列数
 *      $n1=mysql_num_rows(结果集);//结果集当中记录的条目数，行
 *      $n2=mysql_num_fields(结果集);//结果集当中的字段数量，列
 * 获取结果集第i个字段的名字
 *      $name=mysql_field_name(结果集,$i);
 *
 * */
#PHP操作mysql数据库
$link=@mysql_connect("localhost","root","root");//@屏蔽mysql错误
mysql_set_charset("utf8");
mysql_query("set names utf8");
mysql_query("use dbdemo");
$result=mysql_query("select field from demo1");
$res=mysql_fetch_row($result);
print_r($res);
#创建表及结构语句
/*
 * 基本语法形式：
 *      create table [if exists] 表名 (字段列表[,索引或约束列表])[表选项列表];
 * 字段设定形式：
 *      字段名 类型 [字段属性类表]（字段属性可有多个，用空格隔开）
 *          字段属性
 *              auto_increment:只用于整数类型，让该字段自增长
 *              primary key：用于设定该字段为主键
 *              unique key：设定该字段是唯一的，不重复的
 *              not null：用于设定该字段不能为空
 *              default xx值：用于设定该字段默认值，如果insert没有给值，就使用默认值
 *              comment‘字段说明文字’
 *      create table 表名(id int auto_increment primary key,user_name varchar(20) not null unique key,
 *                          password varchar(16) not null,age tinyint default 18,email varchar(50) comment '电子邮箱');
 * 索引（index==下面的key）：索引是系统内部维护的‘数据表’，会极大的加快数据查找速度，其中的数据是自动排好序的
 *      建立索引，其实就是指定一个表的某个或者某些字段作为‘索引字段类型’
 * 索引形式：
 *      普通索引：key 字段名--》就是一个索引，只能加快查找速度
 *      唯一索引：unique key 字段名--》还可以设定其字段的值唯一性
 *      主键索引：primary key 字段名--》是一个索引，还具有区分该表中的任意一行数据，比唯一性unique多一点功能，unique可以为空
 *      全文索引：fulltext 字段名
 *      外键索引：foreign key (字段名) references 其他表(对应其它表字段名)
 * 语法：
 * create table 表名(
 *      id int auto_increment,
 *      user_name varchar(20),
 *      email varchar(50),
 *      age int,//没有索引
 *      key (eamil),//普通索引
 *      primary key (id),//这就是主键索引
 *      unique key (user_name);//唯一索引
 * );此时该表中如果以id 、user_name、email做条件查找会很快，以age为条件查找会很慢
 * 外键索引语法：
 * create table class(
 *      id int auto_increment primary key,
 *      classNum varchar(10) unique key comment '班级号',
 *      classTeca varchar(10) comment '班主任老师',
 * );
 * create table student(
 *      stu_id int auto_increment primary key,
 *      name varchar(20),
 *      age tinyint,
 *      class_id int comment '班级id',
 *      foreign key (class_id) references class(id)//foreign-->外国，references-->引用
 * );此时，插入student表中的数据时，class_id字段的值，不能随便填写，必须是class表中id字段所已经有的数据值，才可以插入
 *
 * 约束：就是要求数据需要满足什么条件的一种规定
 *      主键约束：primary key(字段名)-->其实就是设置主键
 *      唯一约束：unique key(字段名)-->使该设定字段值具有唯一性，与主键不同之处就是可为null
 *      外键约束：foreign key references 其它表名(字段名)-->使该设定字段值，必须在设定的对应表中对应字段中有该值
 *          其实主键约束、唯一约束、外键约束，只是‘同一种事的不同说法’，同时也叫主键索引、唯一索引、外键索引
 *      非空约束（也叫属性）：not null--》这个只能写在字段属性上
 *      默认约束（也叫属性）：default xx值--》这个只能写在字段属性上
 *      检查约束：check (某种判断字段)，比如
 *          create table 表名(
 *              age tinyint,
 *              check (age>=0 and age<100) //这就是检查约束
 *          );mysql数据库识别这种语法，但是无效，目前这个版本不生效，其它数据库有生效的
 * 表选项列表：就是，创建表时对该表的整体设置
 *      charset=要使用的字符编码
 *      engine=要使用的存储引擎（也叫表类型）
 *      auto_increment=设定当前表的自增长字段的初始值，默认是1
 *      comment='该表的一些说明文字'
 *  设定字符编码是为了和数据库设定的不一样，一样就不需要设定啦，会默认使用数据库的字符编码
 *  engine（存储引擎）在代码层面，就是一个名次，InnoDB，MyIsam，BDB，archive，Memory
 *      存储引擎：将数据存储到硬盘的机制
 *  不同存储引擎，其实主要从2个大的层面来设计存储机制：
 *      尽可能快的速度
 *      尽可能多的功能
 *  通常使用：InnoDB，MyIsam（批量插入快，无事物安全），默认InnoDB（事物安全，批量插入慢）
 * create table student(
 *      stu_id int auto_increment primary key,
 *      name varchar(20),
 *      age tinyint,
 * )
 * charset=gbk,//现在数据库编码为utf8
 * engine=MyIsam,
 * auto_increment=1000;
 * comment='说明文字'
 * ;
 * 修改表
 *  1.修改表，是指修改表的结构--同创建表类似
 *          alter table 旧表名 rename [to] 新表名
 *  2.创建表能做的事，修改表几乎都能做--但是不推荐
 *  3.大体功能：
 *      3.1 对字段进行：添加，修改，删除
 *          alter table 表名 add [column] 新字段名 字段类型 [字段属性列表]--》添加字段
 *          alter table 表名 change [column] 旧字段名 新字段名 字段类型 [字段属性列表]--》修改字段
 *          alter table 表名 drop [column] 字段名--》删除字段
 *      3.2 对索引进行：添加，删除（drop）
 *          alter table 表名 add key(key等同与index，对于索引来说) [索引名] (字段1[,字段2...])--》添加普通索引
 *          alter table 表名 add unique key [索引名] (字段1[,字段2...])--》添加唯一索引
 *          alter table 表名 add primary key [索引名] (字段1[,字段2...])--》添加主键索引
 *  4.表的选项，通常都是修改，即使不写也会有默认值
 *          alter table 表名 engine=MyIsam
 * 删除表：drop table [if exists] 表名;
 * 其它的表的相关语句：
 *      show tables;显示所有表
 *      desc 表名;或describe 表名;显示某表的结构
 *      show create table 表名;显示某表的创建语句
 *      rename table 旧表名 to 新表名;重命名表
 *      create table [if not exists] 新表名 like 原表名;从已有表复制表结构
 *      create [unique | fulltext] index 索引名 on 表名(字段1[,字段2...]);创建索引，省略unique | fulltext即为普通索引--》会被映射成alter table...添加索引语句
 *      drop index 索引名 on 表名;删除索引--》会被映射成alter table...删除索引语句
 * 视图：就是一个select语句（通常比较复杂），我们给定一个名字（视图名），以后要执行该select语句就方便的使用：视图名
 *      视图创建：creat view 视图名 [(字段名1,字段名2...)] as select语句;
 * creat view 视图名 as
 *      select id,f1,name,age,email,p_id,f3 from 表名 where id>7 and id<1000 or f1<1000 and age>10;
 *      使用视图：基本上当做一个表用，跟表同一级别，但不是真的表，用法类似于表
 *          select * from 视图名 limit 0,10;--》通过视图名查询0行到10行的数据
 * select id,name from 视图名 where id>10;--》查询id大于10的id和名字
 * 删除视图：drop view [if exists] 视图名;
 *
 * */
#数据库设计(三范式3NF)
/*
 * 第一范式：原子性，数据（字段值）不可再分
 * 第二范式：唯一性，消除部分依赖，达到完全依赖，每一行数据具有唯一性：
 *      1、只要给表设置主键，就可保证唯一性
 *      2、消除数据之间的"依赖"
 *          依赖：就是其中某个字段A可以由另一个字段值B来决定
 *          同理：对于有主键的表中，确定主键的值，则其它字段就已经确定啦
 *              主键决定其它字段，其它字段依赖于主键
 *      部分依赖：如果某个字段，只依赖于"部分主键"，此时就称为"部分依赖"
 *             该情况前提：主键字段有多个
 *      完全依赖：某个字段，依赖于"主键所有字段"--》主键只要一个字段，则肯定是完全依赖
 *
 * 第三范式：独立性，消除传递依赖：消除其中部分非主键字段的内部依赖，这个内部依赖会构成传递依赖
 *
 * */
#数据操作语言(增删改)
/*
 * 增（插入数据）：
 *      形式1：insert [into] 表名 [(字段1,字段2,...)]values(值表达式1,值表达式2,...),(第二行数据...),...;
 *          最常用，可以一次插入多行数据，用逗号隔开，每次都是以行为单位进行插入数据，字段名不推荐省略，并且值表达式应与字段名列表一一对应
 *      形式2：replace [into] 表名 [(字段1,字段2,...)]values(值表达式1,值表达式2,...),(第二行数据...),...;
 *          跟insert几乎一样，唯一区别：如果插入的数据的主键或者唯一键有重复，则会变成修改已存在的改行数据
 *      形式3：insert [into] 表名 [(字段1,字段2,...)]select 字段1,字段2,... from 其它表名;
 *          复制已有表数据
 *      形式4：insert [into] 表名 set 字段名1=值表达式1,字段名2=值表达式2,...;
 *  load data（载入数据）语法：
 *      适用于载入如下“结构整齐的纯文本数据”，前提：有一个这样的对应结构的现有的表
 *      load data infile "完整的数据文件的路径(.txt等)" into table 表名;
 *          --注意文件的编码需要符合要求
 * 删（删除数据）：
 *      delete from 表名 [where 条件] [order by 排序] [limit 限定行数];
 *          删除数据仍然以行为单位，通常删除数据需要带where条件，否则数据会被全部删除
 *          order by排序设定，用于指定这些数据的删除顺序，通常跟limit配合使用才有意义
 *          limit限定用于设定删除多少行，根据orderby设定的顺序
 *          通常很少使用order by和limit，基本使用语法
 *      delete from 表名 where 条件;
 * 改（修改数据）：
 *      update 表名 set 字段1=值1,字段2=值2，...[where 条件] [order by 排序] [limit 限定行数];
 *          update通常也都需要where条件，不加where会将全部数据都改变
 *          order by排序设定，用于指定这些数据的修改顺序，通常跟limit配合使用才有意义
 *          limit限定用于设定删除多少行，根据orderby设定的顺序；通常很少使用order by和limit
 *          常规使用形式：
 *      update 表名 set 字段1=值1,字段2=值2，... where 条件;
 * update 表名 set name='张三',age=108,birthday='1600-6-16' where id=13;
 * update 表名 set name=get_name(),age=108,birthday=now() where id=13;//get_name()mysql的自定义函数(需要自定义)，now()mysql的系统函数
 *
 * */
#mysql数据基本查询语句
/*
 * 基本查询：
 * select [all | distinct] [from子句] [where子句] [group by子句] [having子句] [order by子句] [limit子句];
 * 字段或表达式列表
 *     1、字段，自然是来源于表，依赖于from
 *     2、表达式是类似于：8,8+3，now()
 *  select 后面可以随意放置表达式，最后会将select后的结果集当结果返回
 *      select 8;--》会显示8
 *  select now();--》会显示当前时间，如下
+---------------------+
| now()               |
+---------------------+
| 2017-08-28 11:19:23 |
+---------------------+
 *      3、每个输出项（字段或表达式结果），都可以给其设定一个别名，形式为：字段或表达式 as 别名;
 *  select now() as '时间';如下：
 +---------------------+
| 时间
+---------------------+
| 2017-08-28 11:22:35 |
+---------------------+
 *      注意：as，实际上表的字段本身并没有改变，而是改变了‘结果集’的字段名
 * all和distinct：用于设定select出来的数据，是否消除‘重复行’，可以不写，默认all
 *      all：表示不消除，所有都出来
 *      distinct：表示消除
 * from子句：表示select部分从中取得数据的数据源--》表
 *      通常，其后面就是表名from tab1;；但也可能是其它数据来源from tab1,tab2;--》连接表
 * where子句：就是对from子句中数据源中数据进行筛选的条件设定，筛选机制是“一行一行”就行判断的
 *      where子句依赖于from子句
 *      where子句中通常需要使用各种运算符
 *          1、算术运算符：+ - * / %
 *          2、比较运算符：> >= <= =(等于) <>(不等于) 前面几个是标准sql运算符   ==(等于) !=(不等于)不建议用后两个
 *          3、逻辑运算符：and or not
 *      select * from 表名 where id>2 and not(sex='男');
 *          4、is语法：空值或布尔值的判断（xx代表某个字段）
 *              xx is null：判断某个字段是“null”--》没有值
 *              xx is not null：判断某个字段不是“null”
 *              xx is true：判断某个字段为“真”--》true
 *              xx is false：判断某个字段为“假”--》false：0,0.0，''，null
 *          5、between运算符：用于判断某个字段的数据值是否在某个字段范围内--适用于数字类型
 *              xx between 值1 and 值2;--》xx字段的值介于值1和值2之间
 *          6、in运算符：给定确定数据的范围判断（如果它罗列出来的数据存在一定规律，则其实也可以使用逻辑运算符或between代替）
 *              xx in (值1，值2，值3，...);--》表示字段的值满足所列出的这些值中的一个即可
 *          7、like运算符：对字符串进行模糊查找
 *              xx like '要查找的内容';-->依赖于%(代表任何个数的任何字符)和_(代表一个任何字符)
 *      常见：name like '%的%'; name like '的%';  name like '%的';  name like '_的%';
 *              注意：如果要找%字符的话，需要转义\
 * group by子句：分组--》是对前面筛选过的数据，进行某种指定标准的分组
 *      形式：group by 字段1 [desc | asc],字段2 [desc | asc],...;
 *      分组结果，可以同时指定其排序方式：desc（倒序）、asc（正序）
 *      通常分组就1个字段，2个以上很少
 *      分组：就是将多行数据，以某种标准（就是指定字段）来进行“分类”存放
 *          分组后的结果，一定要理解为：只有一个一个的组啦
 *      应用中，分组之后，通常只有以下几种可用的“组信息”：
 *          1、分组依据本身字段的信息
 *          2、每一组的“数量信息”：就是用count(*)获得的
 *         select id,count(*) from insert1 group by value desc;
 *          3、原来数据中的“数值类型字段的聚合信息”，包括：（以下其实都是系统内部函数）
 *              最大值：max(字段名)、最小值min(字段名)、平均值：avg(字段名)、总和值：sum(字段名)
          select id,count(*) as '数量',max(id) as '最大ID',avg(id) as '平均ID' from insert1 group by value desc;
+----+------+--------+---------+
| id | 数量     | 最大ID     | 平均ID     |
+----+------+--------+---------+
|  8 |    2 |     10 |  9.0000 |
|  1 |    1 |      1 |  1.0000 |
| 11 |    2 |     12 | 11.5000 |
+----+------+--------+---------+
 * having 子句：having的作用跟where完全一样，但其只对“分组的结果数据”进行筛选
 *      where对原始数据进行筛选；
 *      having对分组之后的数据行(分组后一行即为一组)进行删选；
      select id,count(*) as '数量',max(id) as '最大',avg(id) as '平均' from insert1 group by value desc having id>1;
+----+------+------+---------+
| id | 数量     | 最大     | 平均       |
+----+------+------+---------+
|  8 |    2 |   10 |  9.0000 |
| 11 |    2 |   12 | 11.5000 |
+----+------+------+---------+
 *      还有一种用法：此时count(*)是独立计算的结果，即对每一组进行原始数据行的统计，并用该条件进行删选
select id,max(id) as '最大',avg(id) as '平均' from insert1 group by value desc having count(*)>1;
+----+------+---------+
| id | 最大     | 平均       |
+----+------+---------+
|  8 |   10 |  9.0000 |
| 11 |   12 | 11.5000 |
+----+------+---------+
 * order by子句：将前面“取得”的数据以设定的标准（字段）来进行排序以输出结果
 *      形式：order by 字段1 [desc | asc]，字段2 [desc | asc]，...
 *      对前面的结果数据以指定的一个或多个字段排序
 *      排序可以规定正序（asc默认）或倒序（desc）
 *      多个字段的排序，都是在前一个字段排序的基础上，如果还有“相等值”，才继续以后续字段排序
select id,max(id) as '最大',avg(id) as '平均' from insert1 group by value desc having count(*)>1 order by id desc;
+----+------+---------+
| id | 最大     | 平均       |
+----+------+---------+
| 11 |   12 | 11.5000 |
|  8 |   10 |  9.0000 |
+----+------+---------+
 *  limit子句：将“前面取得的数据”，按指定的行取出来：从第几行开始取出多少行
 *      形式：limit 起始行号,要取出的行数;--》从0行开始，行号跟字段无关
 *      limit x;--》代表从第0行开始算，取出x行，x>0
select * from insert1 limit 2,5;--》取出的行数大于应有的数据，则从第几行开始一直取到最后
+----+-------+
| id | value |
+----+-------+
| 10 | 8     |
| 11 | 0     |
| 12 | 0     |
+----+-------+
 *
 * select语句总结：
 *      1、虽然形式上，select的很多子句都是可以省略的，但是他们的顺序（如果出现），就不能打乱：必须任然按照给出的顺序写出
 *      2、where子句依赖于from子句：即没有from子句就不能有where子句；
 *      3、having子句依赖于group by子句：即没有group by子句就不能有having子句；
 *      4、select中的“字段”也是依赖于from子句；
 *      5、上述各子句“内部执行过程”，基本上也都是按照顺序执行的：
 *          即从from的数据源中获取“所有数据”，然后使用where对这些数据进行筛选，之后再使用group by子句对筛选出来的数据进行“分组”，
 *      接下来才可以使用having对这些分组的数组进行筛选，然后才可以order by和limit；
 *
 * */
#mysql数据连接查询语句
/*
 * 连接查询：将两个或者两个以上的表，“连接起来”，当做一个数据源，从获取所需数据
 *      连接：将每个表的每一行数据两两之间相互对接，每次对接的结果都是连接结果的“一行”数据
 *          两个表每一行两两对接：就是表1的每一行会和表2的每一个都会对接
select * from link1;            select * from link2;
+------+------+------+          +------+------+------+
| id1  | f2   | f3   |          | id2  | name | age  |
+------+------+------+          +------+------+------+
|    1 |    1 |    1 | +(连接)  |    1 |liming|    18 |
|    2 |    2 |    2 |          |    2 |zhangsan| 20 |
|    3 |    3 |    3 |          +------+------+------+
+------+------+------+
  mysql> select * from link1,link2;   --》以下为普通连接后的结果
 * +------+------+------+------+----------+------+
 * | id1  | f2   | f3   | id2   | name     | age  |
 * +------+------+------+------+----------+------+
 * |    1 |    1 |    1 |    1 | liming   |   18 |
 * |    1 |    1 |    1 |    2 | zhangsan |   20 |
 * |    2 |    2 |    2 |    1 | liming   |   18 |
 * |    2 |    2 |    2 |    2 | zhangsan |   20 |
 * |    3 |    3 |    3 |    1 | liming   |   18 |
 * |    3 |    3 |    3 |    2 | zhangsan |   20 |
 * +------+------+------+------+----------+------+
 *      无条件连接，形式：--》这些无条件连接也叫“交叉连接”，其实是没有意义的
 * select * from 表1,表2;
 * select * from 表1 join 表2;
 * select * from 表1 cross join 表2;
 * select * from 表1 inner join 表2;--》没有内连接的条件也可当交叉连接使用
 * 连接基本形式：
 *      表1 [连接形式] join 表2 [on 连接条件];--》2个表连接
 *      表1 [连接形式] join 表2 [on 连接条件] [连接形式] join 表3 [on 连接条件];--》3个表连接
 *      ...
 * 连接的分类：
 *      交叉连接：没有条件，只是按连接的基本概念，将所有数据行都连接起来的结果，又叫“笛卡尔积”
 *      内连接 inner：
 *          形式：select * from 表1 [inner] join 表2 on 连接条件;
 *    根据表link1和表link2内连接结果：(link1 inner join link2 on link1.id1=link2.id2)--》整体算数据源
select * from link1 inner join link2 on link1.id1=link2.id2;
select * from link1,link2 where link1.id1=link2.id2;--》结果同上
+-----+------+------+-----+----------+------+
| id1 | f2   | f3   | id2 | name     | age  |
+-----+------+------+-----+----------+------+
|   1 |    1 |    1 |   1 | liming   |   18 |
|   2 |    2 |    2 |   2 | zhangsan |   20 |
+-----+------+------+-----+----------+------+
 *        可见，这里查询的结果，都是无条件的交叉查询中有意义的那些数据；
 *            并且，link1.id1=link2.id2被称为：“连接条件”，基本就是“外键关系”的一个描述
 *        注意：这种的表跟表之间的内连接查询，虽然可以视为表跟表之间的“关系”--通常就是外键关系--但并不是有外键关系才能使用
 *        一些其它形式：
 *  select * from link1 as l1 inner join link2 as l2 on l1.id1=l2.id2;
 *  select l1.*,l2.name,l2.age from link1 as l1 inner join link2 as l2 on l1.id1=l2.id2;
 *      左(外)连接left (outer) join：将两个表的内连接的结果，再“加上”左边表的不符合内连接所设定的条件的那些数据
 *               select * from 表1(左表) left [outer] join 表2(右表) on 连接条件;
mysql> select * from link1 left join link2 on link1.id1=link2.id2;
+-----+------+------+------+----------+------+
| id1 | f2   | f3   | id2  | name     | age  |
+-----+------+------+------+----------+------+
|   1 |    1 |    1 |    1 | liming   |   18 |
|   2 |    2 |    2 |    2 | zhangsan |   20 |
|   3 |    3 |    3 | NULL | NULL     | NULL |
+-----+------+------+------+----------+------+
 *      右(外)连接right (outer) join：将两个表的内连接的结果，再“加上”右边表的不符合内连接所设定的条件的那些数据
 *        --link2表添加id为4的数据，为保证此时link2表存在连接不符合的数据
mysql> insert link2 values(4,'wangwu',22);
mysql> select * from link1 right join link2 on link1.id1=link2.id2;
+------+------+------+-----+----------+------+
| id1  | f2   | f3   | id2 | name     | age  |
+------+------+------+-----+----------+------+
|    1 |    1 |    1 |   1 | liming   |   18 |
|    2 |    2 |    2 |   2 | zhangsan |   20 |
| NULL | NULL | NULL |   4 | wangwu   |   22 |
+------+------+------+-----+----------+------+
 *      全(外)连接：没有形式，mysql不支持全连接的语法
 *          其实就是将两个表的内连接结果，再加上左边表的不符合内连接所设定的条件的那些数据结果，
 *          以及再加上右边表的不符合内连接所设定的条件的那些数据结果；
 *
 * */
#mysql数据子查询语句
/*
 * 基本含义：一个select语句就是一个查询语句
 *      select 字段或表达式 from 数据源 where xx 条件判断;
 * 上述select部分，from部分，where部分，往往都是一些“数据”的组合
select * from link1 where id1 > (select avg(id1) from link1);
+-----+------+------+
| id1 | f2   | f3   |
+-----+------+------+       查询表link1中id1大于id1平均值的字段
|   3 |    3 |    3 |
+-----+------+------+
 *      子查询语句：(select avg(id1) from link1)
 * 子查询：就是在一个查询语句(select语句)内部中，某些位置，又出现“查询语句”
 *      通常，子查询是为主查询服务的，，而且都是子查询获得一定数据后，才会执行主查询；
 * 子查询形式：select 字段或表达式或子查询 [as 别名] from 表名或连接结果或子查询 where 字段或表达式或子查询的条件判断;
 *      即可以在这几个位置会出现子查询(其中having中也可以出子查询，因为having和where一样含义)；
 *  子查询分类(按结果)：
 *      表子查询(查询结果是一个表)：通常放到from后面
 *
 *      行子查询(查询结果是一行)：
 *          select row(字段1,字段2,...)=(select 行子查询)
 *      列子查询(查询结果是一列)：
 *      标量子查询(查询结果是一行一列)：可以理解为一个值，通常放到select后面
 *          select 5 as c;或select ... where id=17;或select ... where b>8;
 *  子查询分类(按位置)：
 *      作为主查询结果数据：select 后面
 *      作为主查询条件数据：where 后面
 *      作为主查询来源数据：from 后面
 *  常用子查询：
 *      比较运算符中的子查询：
 *          形式：操作数 比较运算符(标量子查询)--》id>(select id from 成绩表 where 成绩=80);
 *          说明：操作数--》其实就是比较运算符的2个数据之一，通常就是一个字段：id
 *              --》select * from 表名 where id>(select id from 成绩表 where 成绩=80);
 *          例：找出最高价的商品--》
 *              select * from product where price>(select max(price) from product);
 *      使用in的子查询：
 *          以前：xx in (值1,值2,...);
 *          则in子查询：操作数 in (列子查询)-->一列多行
 *          例：找出所有产品类别表中类别名带“精品”的商品（商品在产品表中）
 *              select * from product where product_id in (
 *                  select protype_id from product where protype_name like '%精品%';
 *              );//产品id和产品类别id一一对应
 *      使用any的子查询：当某个操作数(字段)对于该列子查询的其中任何一个值，都满足该比较运算符，则就算是满足了条件
 *          形式：操作数 比较运算符 any (列子查询);
 *          例：select * from link1 where id1 > any (select id2 from link2);
 *              --》即id1只要大于后面子查询语句中id2的任何一个值就可以当做结果获取
                                  +-----+------+------+
                                  | id1 | f2   | f3   |
                                  +-----+------+------+
                                  |   2 |    2 |    2 |
                                  |   3 |    3 |    3 |
                                  +-----+------+------+
 *      使用some的子查询：是any的同义词
 *      使用all的子查询：当某个操作数(字段)对于该列子查询的其中所有值，都满足该比较运算符，则就算是满足了条件
 *          形式：操作数 比较运算符 all (列子查询);
 *          例：select * from link1 where id1 > all (select id2 from link2);
 *              --》即id1只要大于后面子查询语句中id2的所有值就可以当做结果获取，因此上面查询语句无结果
            例：select * from link2 where id2 > all (select id1 from link1);
                                 +-----+--------+------+
                                 | id2 | name   | age  |
                                 +-----+--------+------+
                                 |   4 | wangwu |   22 |
                                 +-----+--------+------+
 *      使用exists的子查询：该子查询如果有数据，则exists的结果是true，否则是flase
 *          形式：where exists(子查询);
 *          说明：因为exists子查询的该含义，造成主查询往往出现这样的情况：要么全部取出要么全部不取出；
 *              若要局限在这个含义中，其基本就失去了它现实的意义
 *          但：实际应用中，该子查询，往往都不是独立的子查询，而是会需要跟“主查询”的数据源，建立某种关系
 *              --通常就是连接关系。建立的方式是“隐式的”，即没有在代码上体现关系，但却在内部有其连接的“实质”。
 *              此隐式方式，通常就体现在子查询中的where条件语句中，使用了主查询表中的数据(字段)
 *          例：找出所有产品类别表中类别名带“精品”的商品（商品在产品表中）
 *              select * from product where exists (
 *                  select * from product_type where protype_name like '%精品%' and product_id=product_type.protype_id;
 *              );//product_type.protype_id产品类别表中的id
 *          注意：想让exists在实际应用中有意义，往往需要两个表或者以上的连接查询中，在其子查询语句中的where条件判断通常会多使用一个判断条件(跟主查询表中字段有关)
 *      最后结论：
 *          如果一个查询需求，可以使用连接查询也可以使用子查询，则通常推荐使用连接查询，效率更高
 *
 * */
#mysql数据联合查询语句
/*
 * 联合查询(union)
 *      在mysql的手册中，将连接查询(join)翻译为联合查询，而联合查询union没有明确翻译
 *      但是通常的书籍或文章中，join被翻译为“连接”查询；而union翻译为联合查询
 * 基本概念：将两个具有相同字段数量的查询语句的结果，以“上下堆叠”的方式，合并为一个查询结果
 *  select * from link1;        select * from link2;              select * from link1 union select * from link2;
 * +-----+------+------+        +-----+----------+------+         +-----+----------+------+
 * | id1 | f2   | f3   |        | id2 | name     | age  |         | id1 | f2       | f3   |
 * +-----+------+------+        +-----+----------+------+         +-----+----------+------+
 * |   1 |    1 |    1 | union  |   1 | liming   |   18 |    =    |   1 | 1        |    1 |
 * |   2 |    2 |    2 |        |   2 | zhangsan |   20 |         |   2 | 2        |    2 |
 * |   3 |    3 |    3 |        |   4 | wangwu   |   22 |         |   3 | 3        |    3 |
 * +-----+------+------+        +-----+----------+------+         |   1 | liming   |   18 |
 * 注意：                                                         |   2 | zhangsan |   20 |
 *  两个select语句的查询结果的“字段数”必须一致                  |   4 | wangwu   |   22 |
 *  通常，应该让两个查询语句字段类型具有一致性                    +-----+----------+------+
 *      语法形式：此连接查询会默认“自动消除重复行”，即默认distinct
 *          select 语句1 union [all | distinct] select 语句2;
 *      联合查询中，order by和limit只能在最后一个select语句后面写（否则会报错或者无效），切是对整个表起作用
 * 可通过联合查询做到全外连接
 *
 * */
#mysql数据控制语言
/*
 * 用户管理：
 * 用户数据存储位置：mysql中的所有用户，都存储在mysql系统数据库中的user表中--不管哪个数据库的用户，都存储在这里
 * 创建用户：
 *      形式：create user '用户名'@'允许登陆的地址/服务器' identified by '密码';
 *          允许登陆的地址/服务器--》就是允许该设定位置的电脑，来使用用户名和密码登陆，其它位置(电脑)不行
 * create user 'root1'@'localhost' identified by 'root1';--》localhost代表本机可登陆mysql服务，其它电脑不可登陆
 * create user 'root2'@'192.168.0.140' identified by 'root2';--》192.168.0.140代表id为192.168.0.140的电脑可登陆
 *      登陆方式：mysql -h(mysql服务所在电脑的ip地址，本机为localhost) -u用户名 -p密码;
 * 删除用户：
 *      形式：drop user '用户名'@'允许登陆的地址/服务器';
 * 修改用户密码：
 *      修改自己密码形式：set password=password('密码');
 *      修改他人密码(前提：有权限)形式：set password for '用户名'@'允许登陆的地址/服务器'=password('密码');
 * 权限管理：
 *      mysql数据库，将其中所能做的所有事情，都分门别类到大约30多个权限中去了，其中每个权限都是一个“单词”而已
 * 授予权限：
 *      形式：grant 权限列表 on 某库.某个对象 to '用户名'@'允许登陆的地址/服务器' [identified by '密码'];
 *      说明：权限列表--就是多个权限的名词，相互之间用逗号分开，比如：select,create,update,...
 *            某库.某个对象，是给指定的数据库中的某个“下级单位”赋权；
 *                下级单位有：表名、视图名、存储过程名、存储函数名
 *                其中，有两个特殊语法：*.*  --》代表所有数据库中的所有下级单位   某库.*  --》代表某库中的所有下级单位
 *            [identified by '密码']是可省略的，如果不省略，则表示在赋权的同时，也在修改它的密码；若该用户不存在，就必须设置密码
 * 剥夺权限：
 *      形式：revoke 权限列表 on 某库.某个对象 from '用户名'@'允许登陆的地址/服务器';
 *          权限列表如果是all，则剥夺所有权限
 *  权限 权限级别   权限说明
 *    CREATE 数据库、表或索引 创建数据库、表或索引权限 
 *    DROP 数据库或表         删除数据库或表权限 
 *    GRANT OPTION 数据库、表或保存的程序 赋予权限选项 
 *    REFERENCES 数据库或表  
 *    ALTER 表                更改表，比如添加字段、索引等 
 *    DELETE 表               删除数据权限 
 *    INDEX 表                索引权限 
 *    INSERT 表               插入权限 
 *    SELECT 表               查询权限
 *    UPDATE 表               更新权限 
 *    CREATE VIEW 视图       创建视图权限 
 *    SHOW VIEW 视图         查看视图权限 
 *    ALTER ROUTINE 存储过程 更改存储过程权限 
 *    CREATE ROUTINE 存储过程  创建存储过程权限 
 *    EXECUTE 存储过程        执行存储过程权限
 *    FILE 服务器主机上的文件访问 文件访问权限 
 *    CREATE TEMPORARY TABLES 服务器管理    创建临时表权限
 *    LOCK TABLES 服务器管理     锁表权限 
 *    CREATE USER 服务器管理     创建用户权限 
 *    PROCESS      服务器管理     查看进程权限
 *    RELOAD       服务器管理      执行flush-hosts,flush-logs,flush-privileges,flush-status,flush-tables,flush-threads,refresh,reload等命令的权限
 *    REPLICATION CLIENT 服务器管理 复制权限 
 *    REPLICATION SLAVE 服务器管理  复制权限 
 *    SHOW DATABASES 服务器管理   查看数据库权限 
 *    SHUTDOWN     服务器管理      关闭数据库权限 
 *    SUPER        服务器管理       执行kill线程权限
 *                                   
 * 权限分布 可能的设置的权限 
 * 表权限 'Select', 'Insert', 'Update', 'Delete', 'Create', 'Drop',
 *        'Grant', 'References', 'Index', 'Alter' 
 * 列权限 'Select', 'Insert', 'Update', 'References' 
 * 过程权限 'Execute', 'Alter Routine', 'Grant'
 *
 * */
#mysql数据事物语言
/*
 * InnoDB（事物安全，批量插入慢）和BDB引擎才支持事务安全
 * 事例：汇款转账，需要执行两条数据
 *      update 存款表 set money=money-5000 where 账户='小明';
 *      断电啦...
 *      update 存款表 set money=money+5000 where 账户='小花';
 * 事务：就是用来保证多条“增删改”语句的执行的“一致性--》要么都执行，要么都不执行
 *      特点：
 *          原子性：要么都执行，要么都不执行
 *          一致性：一方加一方必减(汇款)或者一方加另一方加(生产)
 *          隔离性：多个事务同时并发执行，每个事务各自独立执行
 *          持久性：一个事务执行成功，则对数据来说应该是一个明确的硬盘数据更改（而不仅仅是内存的变化）
 * 事务模式：
 *      在cmd命令行模式中，是否开启了“一条语句就是一个事务”的这个开关，默认情况(安装后)这个模式是开启的，称“自动提交模式”
 *      我们可以把它关闭，称为“非自动提交”--即需要人为提交
 *          关闭该模式： set autocommit =0;  开启该模式： set autocommit =1;
 *  首先，之前的经验是：一条增删改语句，一旦回车，执行就完成啦（前提是不出错）
 *  现在，关闭该模式： set autocommit =0;
 *      执行insert语句，回车不会执行，还需要commit;提交语句再去提交
 * 事务执行的基本流程：
 *      1、开启一个事务：start transaction;//也可以写成begin
 *      2、执行多条增删改语句//也就是希望这些语句要作为“不可分割”的整体去执行任务
 *      3、判断这些语句执行的结果情况：
 *          if(没有出错){commit;}//提交事务，此时一次性完成
 *          else{rollback;}//回滚事务，此时就是都没有执行
 *
 * */
#mysql编程
/*
 * 语句块包含符：相当于PHP中的{...}
 *                                   if(条件判断)then
 *      [标识符:] begin              A:begin
 *          //语句......   --》         //语句......
 *      end [标识符];                end A;end if;
 *    A就是标识符(自定义的任意名字)，它的作用是“标识”，以期可以在该语句块中“使用它”--退出
 * 流程控制语句：
 *      if语句：if 条件语句 then ... elseif 条件语句 then ... else ... end if;
 *          ...代表语句块，通过语句块包含符包裹的语句
 *
 *      case语句1：case case_value when when_value then ... when when_value then ... else ... end case;
 *          case_value(@res-->代表1个变量，类似于PHP中$res)：和swith...case类似的结构，若when_value==case_value满足条件，执行后面...语句块
 *
 *      case语句2：case when 条件判断 then ... when 条件判断 then ... else ... end case;
 *          条件判断(类似于@res>1)为真，则执行后面...语句块
 *
 *      loop循环语句：[标识符:] loop ... end loop [标识符];
 *          说明：如果该循环不带标识符则该循环为死循环，即该循环需要借助标识符来退出语句；
 *              即...语句块中必须有一个“退出循环”的逻辑机制--形式：if(条件判断)then leave 标识符; end if;
 *
 *      while循环语句：[标识符:] while 条件 do ... end while [标识符];
 *          说明：该循环语句有两种退出方式，1、条件符合即退出 2、...语句块中通过判断+退出机制(leave 标识符;)
 *              ...语句块里没有++、--、+=等这些
 *          例：set @i=1;//赋值语句
 *              while @i<10 do
 *              begin
 *                  insert into table1 (id,num) values(null,@i);
 *                  set @i=@i+1;
 *              end;
 *              end while;
 *      repeat循环语句：[标识符:] repeat ... until 条件判断 end repeat [标识符];
 *          说明：until后的条件判断为真则跳出循环
 *          例：set @i=1;//赋值语句
 *              repeat begin
 *                  insert into table1 (id,num) values(null,@i);
 *                  set @i=@i+1;
 *              end; until @i>=9;
 *              end repeat;
 *      leave 语句：用于跳出begin ... end;结构，或者其它具有标识符的语句
 * mysql中的变量：两种变量形式
 *      普通变量：不带“@”符号
 *          定义形式： declare 变量名 类型名 [default 默认值];//普通变量必须这样
 *          赋值形式： set 变量名=值;
 *          取值：直接使用变量名
 *          使用“场所”：只能在“编程环境”中使用
 *              编程环境：1、定义函数内部  2、定义存储过程内部  3、定义触发器内部
 *      会话变量：带“@”符号
 *          定义(也是赋值)形式： set @变量名=值;  //跟PHP类似，无需定义，直接赋值，第一次就算定义
 *          取值：直接使用变量名
 *          使用“场所”：基本哪里都可以用
 *                                 set @i=1;
 *                                 select @i,@i+4;
 *                                 +------+------+
 *                                 | @i   | @i+4 |
 *                                 +------+------+
 *                                 |    1 |    5 |
 *                                 +------+------+
 *      变量赋值形式：
 *          set 变量名=值;(针对普通变量)
 *          set @变量名=值;(针对会话变量)
 *          select @变量名:表达式;(针对会话变量)
 *          select 表达式 into @表达式;(针对会话变量)
 * (存储)函数：函数也叫存储函数跟js或者PHP中函数的区别，必须返回一个数据(值)
 *      定义形式：create function 函数名(形参1 类型1,形参2 类型2,...)
 *                returns 返回类型
 *                begin
 *                  #函数中的语句
 *                  return xx值;
 *                end;
 *          注意：1、在函数内部，可以有各种变量和流程控制的使用  2、在函数内部，也可以有各种增删改语句
 *                3、在函数内部不可以有select或其它“返回结果集”的查询语句；
 *      调用形式：跟调用系统内部函数一样
 *          select now(),3+7 as f1,func();//now()是系统函数，func()是自定义函数
 *          或在编程语句中：set @i=now();    set @j=func();
 *        例：create function getMaxValue(p1 float,p2 float,p3 float)
 *            returns float #返回float类型                                      mysql> create function getMaxValue(p1 float,p2 float,p3 float)
 *            begin                                                             ->   returns float #返回float类型
 *               declare result float;#声明普通变量，没有默认值                             ->   begin
 *               if(p1>=p2 and p1>=p3) then                                     ->      declare result float;#声明普通变量，没有默认值
 *               begin                                                          ->      if(p1>=p2 and p1>=p3) then
 *                   set result=p1;                                             ->      begin
 *               end;                                                           ->          set result=p1;
 *               elseif(p2>=p1 and p2>=p3) then                                 ->      end;
 *               begin                                                          ->      elseif(p2>=p1 and p2>=p3) then
 *                   set result=p2;                                             ->      begin
 *               end;                                                           ->          set result=p2;
 *               else                                                           ->      end;
 *               begin                                                          ->      else
 *                   set result=p3;                                             ->      begin
 *               end;                                                           ->          set result=p3;
 *               end if;                                                        ->      end;
 *               return result;                                                 ->      end if;
 *            end;                                                              ->      return result;
 *      注意：在cmd中执行该代码，需要更换“语句结束符”--delimiter ///          ->   end;   ///;
                                       mysql> select now(),getMaxValue(1.2,2.3,3.6);
                                           -> ///;
                                       +---------------------+--------------------------+
                                       | now()               | getMaxValue(1.2,2.3,3.6) |
                                       +---------------------+--------------------------+
                                       | 2017-08-30 10:49:32 |       3.5999999046325684 |
                                       +---------------------+--------------------------+
 *      注意：这种比较数据不精确
 * 删除函数：
 *      形式：drop function 函数名;
 * 存储过程(procedure)：存储过程，其本质还是函数--但是规定：不能有返回值；
 *      定义形式：create procedure 函数名([in|on|inout]形参1 类型1,[in|on|inout]形参2 类型2,...)
 *                begin
 *                  #完整的过程语句
 *                  #各种流程控制
 *                  #还可以有增删改 查等
 *                  #其中查询语句会作为存储过程调用的结果，跟执行select语句一样，返回结果集
 *                end;
 *      说明：
 *          1、in：用于设定该变量是用来“接收实参数据”的，即“传入”；默认就是in
 *          2、out：用于设定该变量是用来“存储函数过程中数据”的，即“传出”；即函数中必须对它赋值
 *          3、inout：是in和out的结合，具有双向作用
 *          4、对于，out和inout设定，对应的实参，就必须是一个变量，因为该变量是用于“接收传出数据”
 *      创建一个下列实例数据表
 *          1、create table tab_int(f1 int,f2 tinyint,f3 bigint);///;
            2、mysql> insert tab_int(f1,f2,f3)values(11,12,13),(21,22,23),(31,32,33)///;
 *      例1：将3个数据写入到表tab：并返回该表第一个字段的最新的最大的3个值的行数据
 *      mysql> create procedure insert_get_data(p1 int,p2 tinyint,p3 bigint)
        -> begin
        -> insert into tab_int(f1,f2,f3)values(p1,p2,p3);
        -> select * from tab_int order by f1 desc limit 0,3;
        -> end;
        -> ///;
        例2：使用in、out、inout
        mysql> create procedure pro1(in p1 int,out p2 tinyint,inout p3 bigint)
        -> begin
        -> set p2=p1*2;#p2设置p1的2倍
        -> set p3=p3+p3*2;#p3设置为p3本身再加上p1的2倍
        -> insert into tab_int(f1,f2,f3)values(p1,p2,p3);
        -> end;///;
 * 调用存储过程： call 存储过程名(实参1,实参2，...)--》在“非编程环境中”调用
                 例1：mysql> call insert_get_data(41,42,43)///;
                 +------+------+------+
                 | f1   | f2   | f3   |
                 +------+------+------+
                 |   41 |   42 |   43 |
                 |   31 |   32 |   33 |
                 |   21 |   22 |   23 |
                 +------+------+------+
                 例2：mysql> set @s3=3;///;
                 mysql> call pro1(25,@s2,@3); ///;
                 mysql> select @s2,@s3;///;
                 +------+------+
                 | @s2  | @s3  |
                 +------+------+
                 |   50 |    3 |
                 +------+------+
 * 删除存储过程：drop procedure 存储过程名;
 * 在PHP中使用存储函数或存储过程示例：
 * <?php
 *      //调用存储函数：
 *      $v1=$_POST['a'];
 *      $v2=$_POST['b'];
 *      $sql="insert into tab (id,time,f3) values (null,now(),func1($v1,$v2))";
 *      $result=mysql_query($sql);
 *      //调用存储过程：
 *      $v1=$_POST['username'];
 *      $v2=$_POST['password'];
 *      $v3=$_POST['age'];
 *      $sql="call insert_user($v1,$v2,$v3)";//insert_user()是一个存储过程，带3个参数，会将3个参数写入某个表中
 *      $result=mysql_query($sql);
 *      //另一个使用存储过程返回结果集的例子
 *      $id=$_GET["id"];
 *      $sql="call get_user_info($id)";//get_user_info()是一个存储过程，其中会返回某个指定id的用户信息
 *      $result=mysql_query($sql);//这里得到的就是“结果集”
 * ?>
 * 触发器(trigger)：触发器也是一段定义好的代码(跟存储过程和存储函数一样)，并有个名字，
 *      但是不能调用，而是在某个表发生某个事件(增删改)时，会自动“触发”而调用起来
 *      定义形式：create trigger 触发器名 触发时机 触发事件 on 表名 for each row
 *                begin
 *                  //触发器内部语句
 *                end;
 *      说明：
 *          1、触发时机(2个)：before、after
 *          2、触发事件(3个)：insert、update、delete
 *          3、在触发器内部，有2个关键字代表某种特定含义，可以用来获取数据：
 *              new：代表当前正要执行insert或者update的时候“新行”数据；通过它可以获取这一新行数据的任一字段的值
 *                  形式：set @v1=new.id;--》获取该新行insert或updata的id字段值(前提是有id这个字段)
 *              old：代表当前正要执行delete的时候“旧行”数据；通过它可以获取这一旧行数据的任一字段的值
 *                  形式：set @v1=old.id;--》获取该旧行delete的id字段值(前提是有id这个字段)
 *      例1：在表tab_int插入一行数据时，能够同时将这个表中第一个字段的最大值的行，写入到另一个表中(tab_int_max3)
 *          注：两表结构一样   create table tab_int_max3 like tab_int;///;
    mysql>  create trigger tri1 after insert on tab_int for each row
    ->  begin
    ->  select max(f1) into @maxf1 from tab_int;#取得tab_int表中f1字段的最大值，并存入变量@maxf1中
    ->  select f2 into @v2 from tab_int where f1=@maxf1;
    ->  select f3 into @v3 from tab_int where f1=@maxf1;
    ->  insert into tab_int_max3(f1,f2,f3)values(@maxf1,@v2,@v3);
    -> end;///;
    mysql> insert tab_int values(51,52,53);///;
    mysql> select * from tab_int_max3;///;
    +------+------+------+                                   mysql> select * from tab_int;///; 
    | f1   | f2   | f3   |                                   +------+------+------+            
    +------+------+------+                                   | f1   | f2   | f3   |            
    |   51 |   52 |   53 |                                   +------+------+------+            
    +------+------+------+                                   |   11 |   12 |   13 |            
    mysql> insert tab_int values(61,62,56);///;              |   21 |   22 |   23 |
    mysql> select * from tab_int_max3;///;                   |   31 |   32 |   33 |
    +------+------+------+                                   |   41 |   42 |   43 |
    | f1   | f2   | f3   |                                   |   25 |   50 | NULL |
    +------+------+------+                                   |   51 |   52 |   53 |
    |   51 |   52 |   53 |                                   |   61 |   62 |   56 |
    |   61 |   62 |   56 |                                   +------+------+------+
    +------+------+------+
 *      例2：再建一个触发器，在表tab_int进行之前，将该行数据也同时插入到一个跟其类似结果表(tab_int_some)中：
 *  create table tab_int_some(id int,age tinyint(4));///;
    mysql> create trigger copy_data before insert on tab_int for each row
    -> begin
    -> set @v1=new.f1;#获取新行字段f1值
    -> set @v2=new.f2;                                       +------+------+
    -> insert tab_int_some(id,age)values(@v1,@v2);           | id   | age  |
    -> end;///;                                              +------+------+
    mysql> insert tab_int values(13,14,15);///;              |   13 |   14 |
    mysql> select * from tab_int_some;///;                   +------+------+
 *
 * */
