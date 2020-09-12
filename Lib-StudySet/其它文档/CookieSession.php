<?php
#Cookie技术
/*
 * cookie是一个小的信息包，保存在客户端，会在请求头中
 * 长连接：在http请求前会建立TCP连接，此连接会保留5秒，称为长连接
 * cookie的应用：
 *      1、在不同界面访问同一个值
 *      2、记住用户名和密码
 * cookie语法：
 *      1、setcookie(cookie名字[,coolie值,过期时间,有效目录,子域名]);
 *      思考：如下执行结果
 *          setcookie("name","aa");  echo $_COOKIE['name'];
 *      答：第一次访问不能取出name的值，第二次以后就可以访问啦
 *      2、支持下标：setcookie('arr[0]','tom');setcookie('arr[1]','rose');
 *          cookie在浏览器上没有数组概念，不过浏览器请求到服务器后，PHP会将同名的值转成数组
 *      3、删除cookie：setcookie("name",false);setcookie("name",'');
 *      4、清空cookie值：setcookie("name");
 * 临时性cookie和永久性cookie
 *      临时性：这种方法浏览器关闭cookie会被销毁(cookie被保存到内存中)
 *          setcookie("name","aa"); setcookie("name","aa",0);
 *      永久性：关闭浏览器不消失，给临时性添加一个过期时间即可，会以文件的形式保存在客户端，可以清除
 *          setcookie("name","aa",time()+10);此cookie保持10秒
 * cookie有效目录：默认只能在当前目录和下级目录中生效（限制临时性cookie的使用）
 *      setcookie("name","aa",0,'/');表示在根目录下有效
 * cookie支持子域名：默认情况下，cookie是区分子域名的--不同的子域名是独立的网站，只能访问自己的cookie
 *      setcookie("name","aa",0,'/','php.com');在php.com的域名下起作用，即*.php.com都起作用
 * cookie缺点：
 *      1、安全性低
 *      2、增加了请求时数据负载(cookie放在请求头中)，大部分浏览器对cookie的限制是4K(整个请求头信息)
 *      3、可控性差：浏览器可阻止cookie
 * */
setcookie("name","aa");
setcookie('arr[0]','tom');
setcookie('arr[1]','rose');
echo $_COOKIE['name'];
#Session技术
/*
 * 1、session(会话)技术基于cookie技术
 * 2、要使用一个会话，必须显示的开启一个会话：默认情况会话不会自动开启
 *      session_start();开启会话
 *      $_SESSION['name']='tom';//将tom保存在会话中
 *      var_dump($_SESSION['name']);//获取会话name的值，首先需要先开启会话
 *      session_id();//会话编号  echo '会话编号'.session_id();
 *      session_name();//会话名称  echo '会话名称'.session_name();
 * 3、session可以保存除资源以外的任何数据类型（例如：数据库查询结果是资源类型等）
 * 4、会话重复开启会提示性报错
 * 5、会话的执行过程
 *      1）使用session_start();函数，，PHP从session仓库中加载已经存储的会话
 *      2）当session第一次开启时，服务器给客户分配唯一的id保存到cookie中
 *      3）执行PHP时，通过会话编码去仓库中存入或读取值
 *      4）当PHP执行结束时，没有被销毁的session变量被自动保存到服务器session仓库中
 * 6、自动开启会话 需配置php.ini--》session.auto_start=1(自动开启会话)
 *      一般不自动开启session.auto_start()=0
 * 7.会话保存的地址(自定义)：需配置php.ini--》session.save_path="自定义路径"
 *      session.save_path="1;F:/session"：session.save_path可以设置通过N级目录来分类存放session文件，
 *          如果N为1，就是使用会话编号的第一个字母做文件夹名称；
 *          如果N为2，第一级用第一个字母，第二级用第二个字母做文件夹名
 * 8、配置php.ini--》session.name设置会话编号的名字
 * 9、php.ini中：
 *      session.hash_function=0 会话算法
 *      session.hash_bits_per_charset=5 一个字符占5个位 会话编号总32字符
 *      session.cookie_lifetime=0 会话编号在客户端cookie中存储时间
 *      session.cookie_path=/ 保存会话的cookie整站有效
 *      session.cookie_domain=  保存会话的cookie在当前域名下有效
 *      session.save_handler=files  保存会话的以文件格式
 *      session.gc_maxlifetime=1440  session在服务器存储的时间(生命周期)1440秒
 * 10、销毁会话 session_destory();--如果是自定义销毁会话函数，调用此函数会自动调用自定义销毁会话
 *      1、调用此函数会删除存储介质中的文件
 * 11、session修改存储（session入库(数据库)）
 *      1）创建数据库  create table session(id char(32) primary key commit '会话编号',value text not null comment '会话值',expires int not null comment '会话保存时间')engine=innodb charset=utf8;
 *      2）session_set_save_handler();
 *      3）一旦销毁会话就不执行写操作，正常情况应该执行
 *      4)垃圾回收GC：配置php.ini--》session.gc_probability=1 session.gc_divisor=1000
 *              即执行垃圾回收的概率是千分之一，默认就是千分之一
 */
#Session和Cookie区别
/*
 * 相同点：
 *      1、都是会话技术，有生命周期
 *      2、都是无状态性的：服务器将请求的内容发送到客户端后，服务器不在记录客户端的信息成为无状态信息
 * 不同点：
 *              cookie      session
 *   存储地方   浏览器中    服务器端
 *   安全性     低          高
 *   数据负载   4K          没有限制
 *   数据类型   字符串和数字 除资源外所有
 *   可控性     低          高
 * 禁用cookie：基于cookie的所有技术默认无法使用
 *      session.use_only_cookies=1 是否仅依赖于cookie，修改成0不仅仅依赖于cookie
 *      session.use_trans_sid=0  是否允许其他方式传递ID 修改成1为允许
 * 允许其他方式传递ID：
 *      PHP自动在URL地址上自动加上会话编号或者在表单中加上会话编号
 * */
#session入库
function open(){//打开会话
    echo 'open';
    mysql_connect('','','');
    mysql_query('set names utf8');
    mysql_query('use sess');
}
function close(){//关闭会话
    echo 'close';
}
function read($session_id){//读取会话
    echo 'read';
    $res=mysql_query("select session_value from session where session_id='$session_id'");
    if ($rows=mysql_fetch_row($res)){
        return $rows[0];
    }
}
function write($session_id,$session_value){//写入会话
    echo 'write';
    $time=time();
    return mysql_query("insert into session values('$session_id','$session_value','$time')
            on duplicate key update session_value='$session_value'");
}
function destroy($session_id){//销毁会话，只销毁自己的会话
    echo 'destroy';
    return mysql_query("delete from session where session_id='$session_id'");
}
function gc($maxfiletime){//垃圾回收，所有过期的会话
    echo 'gc';
    $time=time()-$maxfiletime;
    return mysql_query("delete from session where expires<$time");
}
session_set_save_handler('open','close','read','write','destroy','gc');
session_start();//开启会话  开启会话一定要在更改会话下面
//session_destroy();//销毁会话

