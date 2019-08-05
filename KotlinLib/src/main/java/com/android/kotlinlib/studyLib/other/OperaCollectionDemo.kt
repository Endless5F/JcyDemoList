package com.android.kotlinlib.studyLib.other

/**
 * 描述 ：    处理集合的高级函数
 * anthor :  Jetictors
 * time :    2018/4/1  22:59
 * version : v1.0.1
 */


fun main(args: Array<String>) {

    listToArray() // 集合转数组
    arrayToList() // 数组转集合
    listToList() // 集合转集合
    getElement() // 获取列表或数组元素
    optValue() // 元素（值）操作符
    optCount() // 统计操作符
    optSort() // 顺序操作符
    optMapped() // 映射操作符
    optFilter() // 过滤操作符
    optProduce() // 生产操作符
}

/**
 * 集合转数组
 */
fun listToArray(){
    val list = listOf(1,2,3,4,5,6)

    val listArray = list.toIntArray()

    println("变量list的类型为：${list.javaClass}")
    println("变量listArray的类型为：${listArray.javaClass}")
    println(listArray[1])
}

/**
 * 数组转集合
 */
fun arrayToList() {

    val arr = arrayOf(1,3,5,7,9)
    val list = arr.toList()
    println("变量arr的类型为：${arr.javaClass}")
    println("变量list的类型为：${list.javaClass}")
    println(list[1])
}

/**
 * 集合转集合
 */
fun listToList(){
    val set = setOf(1)
    val list = set.toList()

    println("变量set的类型为：${set.javaClass}")
    println("变量list的类型为：${list.javaClass}")
    println(list[0])
}

/**
 * 获取列表或数组元素
 */
fun getElement() {

    val list = listOf<String>("kotlin","Android","Java","PHP","Python","IOS")
    /*
        1. 使用get(index)获取
     */
    val element1 = list.get(0)

    /*
        2. 使用arr[index]获取
     */
    val element2 = list[0]

    /*
        3. 使用arr.get
     */
    val element3 = list.component1()

    println("element1 = $element1 \t element2 = $element2 \t element3 = $element3")

//    val arr = listOf(1,2)
//    arr.component4()
}

/**
 * 测试元素操作符
 *
 * contains(元素) : 检查集合中是否包含指定的元素，若存在则返回true，反之返回false
 * elementAt(index) : 获取对应下标的元素。若下标越界，会抛出IndexOutOfBoundsException（下标越界）异常，同get(index)一样
 * elementAtOrElse(index,{...}) : 获取对应下标的元素。若下标越界，返回默认值，此默认值就是你传入的下标的运算值
 * elementAtOrNull(index) : 获取对应下标的元素。若下标越界，返回null
 * first() : 获取第一个元素，若集合为空集合，这会抛出NoSuchElementException异常
 * first{} : 获取指定元素的第一个元素。若不满足条件，则抛出NoSuchElementException异常
 * firstOrNull() : 获取第一个元素，若集合为空集合，返回null
 * firstOrNull{} : 获取指定元素的第一个元素。若不满足条件，返回null
 * getOrElse(index,{...}) : 同elementAtOrElse一样
 * getOrNull(index) : 同elementAtOrNull一样
 * last() : 同first()相反
 * last{} : 同first{}相反
 * lastOrNull{} : 同firstOrNull()相反
 * lastOrNull() : 同firstOrNull{}相反
 * indexOf(元素) : 返回指定元素的下标，若不存在，则返回-1
 * indexOfFirst{...} : 返回第一个满足条件元素的下标，若不存在，则返回-1
 * indexOfLast{...} : 返回最后一个满足条件元素的下标，若不存在，则返回-1
 * single() : 若集合的长度等于0,则抛出NoSuchElementException异常，若等于1，则返回第一个元素。反之，则抛出IllegalArgumentException异常
 * single{} : 找到集合中满足条件的元素，若元素满足条件，则返回该元素。否则会根据不同的条件，抛出异常。这个方法慎用
 * singleOrNull() : 若集合的长度等于1,则返回第一个元素。否则，返回null
 * singleOrNull{} : 找到集合中满足条件的元素，若元素满足条件，则返回该元素。否则返回null
 * forEach{...} : 遍历元素。一般用作元素的打印
 * forEachIndexed{index,value} : 遍历元素，可获得集合中元素的下标。一般用作元素以及下标的打印
 * componentX() ： 这个函数在前面的章节中提过多次了。用于获取元素。其中的X只能代表1..5。详情可看下面的例子
 *
 */
fun optValue() {
    val list = listOf("kotlin","Android","Java","PHP","Python","IOS")

    println("  ------   contains -------")
    println(list.contains("JS"))

    println("  ------   elementAt -------")

    println(list.elementAt(2))
    println(list.elementAtOrElse(10,{it}))
    println(list.elementAtOrNull(10))

    println("  ------   get -------")
    println(list.get(2))
    println(list.getOrElse(10,{it}))
    println(list.getOrNull(10))

    println("  ------   first -------")
    println(list.first())
    println(list.first{ it == "Android" })
    println(list.firstOrNull())
    println(list.firstOrNull { it == "Android" })

    println("  ------   last -------")
    println(list.last())
    println(list.last{ it == "Android" })
    println(list.lastOrNull())
    println(list.lastOrNull { it == "Android" })

    println("  ------   indexOf -------")
    println(list.indexOf("Android"))
    println(list.indexOfFirst { it == "Android" })
    println(list.indexOfLast { it == "Android" })

    println("  ------   single -------")
    val list2 = listOf("list")
    println(list2.single())     // 只有当集合只有一个元素时，才去用这个函数，不然都会抛出异常。
    println(list2.single { it == "list" }) //当集合中的元素满足条件时，才去用这个函数，不然都会抛出异常。若满足条件返回该元素
    println(list2.singleOrNull()) // 只有当集合只有一个元素时，才去用这个函数，不然都会返回null。
    println(list2.singleOrNull { it == "list" }) //当集合中的元素满足条件时，才去用这个函数，不然返回null。若满足条件返回该元素

    println("  ------   forEach -------")
    list.forEach { println(it) }
    list.forEachIndexed { index, it -> println("index : $index \t value = $it") }

    println("  ------   componentX -------")
    println(list.component1())  // 等价于`list[0]  <=> list.get(0)`
    println(list.component2())  // 等价于`list[1]  <=> list.get(1)`
    println(list.component3())  // 等价于`list[2]  <=> list.get(2)`
    println(list.component4())  // 等价于`list[3]  <=> list.get(3)`
    println(list.component5())  // 等价于`list[4]  <=> list.get(4)`
}

/**
 * 统计操作符
 *
 * any() : 判断是不是一个集合，若是，则在判断集合是否为空，若为空则返回false,反之返回true,若不是集合，则返回hasNext
 * any{...} : 判断集合中是否存在满足条件的元素。若存在则返回true,反之返回false
 * all{...} : 判断集合中的所有元素是否都满足条件。若是则返回true,反之则返回false
 * none() : 和any()函数的作用相反
 * none{...} : 和all{...}函数的作用相反
 * max() : 获取集合中最大的元素，若为空元素集合，则返回null
 * maxBy{...} : 获取方法处理后返回结果最大值对应那个元素的初始值，如果没有则返回null
 * min() : 获取集合中最小的元素，若为空元素集合，则返回null
 * minBy{...} : 获取方法处理后返回结果最小值对应那个元素的初始值，如果没有则返回null
 * sum() : 计算出集合元素累加的结果。
 * sumBy{...} : 根据元素运算操作后的结果，然后根据这个结果计算出累加的值。
 * sumByDouble{...} : 和sumBy{}相似，不过sumBy{}是操作Int类型数据，而sumByDouble{}操作的是Double类型数据
 * average() : 获取平均数
 * reduce{...} : 从集合中的第一项到最后一项的累计操作。
 * reduceIndexed{...} : 和reduce{}作用相同，只是其可以操作元素的下标(index)
 * reduceRight{...} : 从集合中的最后一项到第一项的累计操作。
 * reduceRightIndexed{...} : 和reduceRight{}作用相同，只是其可以操作元素的下标(index)
 * fold{...} : 和reduce{}类似，但是fold{}有一个初始值
 * foldIndexed{...} : 和reduceIndexed{}类似，但是foldIndexed{}有一个初始值
 * foldRight{...} : 和reduceRight{}类似，但是foldRight{}有一个初始值
 * foldRightIndexed{...} : 和reduceRightIndexed{}类似，但是foldRightIndexed{}有一个初始值
 *
 */
fun optCount() {
    val list1 = listOf(1,2,3,4,5)

    println("  ------   any -------")
    // 判断是不是一个集合，若是，则在判断集合是否为空，若为空则返回`false`,反之返回true,若不是集合，则返回`hasNext`
    println(list1.any())
    // 判断集合中是否有大于10的元素。若存在则返回true,反之返回false
    println(list1.any{it > 10})

    println("  ------   all -------")
    // 判断集合中是否所有的元素都大于2。若满足条件则返回true,反之返回false
    println(list1.all { it > 2 })

    println("  ------   none -------")
    // 判断是不是一个集合，若是，则在判断集合是否为空，若为空则返回`false`,反之返回true,若不是集合，则返回`hasNext`
    println(list1.none())
    // 判断集合中是否所有的元素都大于2。若满足条件则返回true,反之返回false
    println(list1.none{ it > 2})

    println("  ------   max -------")
    println(list1.max())
    println(list1.maxBy { it + 2 })
//    println(list1.maxWith(Comparator { num1, num2 -> num1 + num2 }))

    println("  ------   min -------")
    println(list1.min())        // 返回集合中最小的元素
    println(list1.minBy { it + 2 })
//    println(list1.minWith(Comparator { num1, num2 -> num1 + num2 }))

    println("  ------   sum -------")
    println(list1.sum())
    println(list1.sumBy { it + 2 })
    println(list1.sumByDouble { it.toDouble() })

    println(" ------  average -----")
    println(list1.average())

    println("  ------   reduce  -------")
    println(list1.reduce { result, next -> result  + next})
    println(list1.reduceIndexed { index, result, next ->
        index + result + next
    })
    println(list1.reduceRight { result, next -> result  + next })
    println(list1.reduceRightIndexed {index, result, next ->
        index + result + next
    })

    println("  ------   fold  -------")
    println(list1.fold(3){result, next -> result  + next})
    println(list1.foldIndexed(3){index,result, next ->
        index + result  + next
    })
    println(list1.foldRight(3){result, next -> result  + next})
    println(list1.foldRightIndexed(3){index,result, next ->
        index + result  + next
    })
}


/**
 * 顺序操作符
 *
 * reversed() : 反序。即和初始化的顺序反过来。
 * sorted() : 自然升序。
 * sortedBy{} : 根据条件升序，即把不满足条件的放在前面，满足条件的放在后面
 * sortedDescending() : 自然降序。
 * sortedByDescending{} : 根据条件降序。和sortedBy{}相反
 *
 */
fun optSort() {
    val list1 = listOf(-1,-3,1,3,5,6,7,2,4,10,9,8)

    // 反序
    println(list1.reversed())

    // 升序
    println(list1.sorted())

    // 根据条件升序，即把不满足条件的放在前面，满足条件的放在后面
    println(list1.sortedBy { it % 2 == 0})

    // 降序
    println(list1.sortedDescending())

    // 根据条件降序，和`sortedBy{}`相反
    println(list1.sortedByDescending { it % 2 == 0 })

}

/**
 * 映射操作符
 *
 * map{...} : 把每个元素按照特定的方法进行转换，组成一个新的集合。
 * mapNotNull{...} : 同map{}函数的作用相同，只是过滤掉转换之后为null的元素
 * mapIndexed{index,result} : 把每个元素按照特定的方法进行转换，只是其可以操作元素的下标(index)，组成一个新的集合。
 * mapIndexedNotNull{index,result} : 同mapIndexed{}函数的作用相同，只是过滤掉转换之后为null的元素
 * flatMap{...} : 根据条件合并两个集合，组成一个新的集合。
 * groupBy{...} : 分组。即根据条件把集合拆分为为一个Map<K,List<T>>类型的集合。具体看实例
 *
 */
fun optMapped() {
    val list1 = listOf("kotlin","Android","Java","PHP","JavaScript")

    println(list1.map { "str-".plus(it) })

    println(list1.mapNotNull { "str-".plus(it) })

    println(list1.mapIndexed { index, str ->
        index.toString().plus("-").plus(str)
    })

    println(list1.mapIndexedNotNull { index, str ->
        index.toString().plus("-").plus(str)
    })

    println( list1.flatMap { listOf(it,"new-".plus(it)) })

    println(list1.groupBy { if (it.startsWith("Java")) "big" else "latter" })

}

/**
 * 过滤操作符
 *
 * filter{...} : 把不满足条件的元素过滤掉
 * filterIndexed{...} : 和filter{}函数作用类似，只是可以操作集合中元素的下标（index）
 * filterNot{...} : 和filter{}函数的作用相反
 * filterNotNull() : 过滤掉集合中为null的元素。
 * take(num) : 返回集合中前num个元素组成的集合
 * takeWhile{...} : 循环遍历集合，从第一个元素开始遍历集合，当第一个出现不满足条件元素的时候，退出遍历。然后把满足条件所有元素组成的集合返回。
 * takeLast(num) : 返回集合中后num个元素组成的集合
 * takeLastWhile{...} : 循环遍历集合，从最后一个元素开始遍历集合，当第一个出现不满足条件元素的时候，退出遍历。然后把满足条件所有元素组成的集合返回。
 * drop(num) : 过滤集合中前num个元素
 * dropWhile{...} : 相同条件下，和执行takeWhile{...}函数后得到的结果相反
 * dropLast(num) : 过滤集合中后num个元素
 * dropLastWhile{...} : 相同条件下，和执行takeLastWhile{...}函数后得到的结果相反
 * distinct() : 去除重复元素
 * distinctBy{...} : 根据操作元素后的结果去除重复元素
 * slice : 过滤掉所有不满足执行下标的元素。
 *
 */
fun optFilter() {

    val list1 = listOf(-1,-3,1,3,5,6,7,2,4,10,9,8)
    val list2 = listOf(1,3,4,5,null,6,null,10)
    val list3 = listOf(1,1,5,2,2,6,3,3,7,4,4,8)

    println("  ------   filter -------")
    println(list1.filter { it > 1  })
    println(list1.filterIndexed { index, result ->
        index < 5 && result > 3
    })
    println(list1.filterNot { it > 1 })
    println(list2.filterNotNull())

    println("  ------   take -------")
    println(list1.take(5))
    println(list1.takeWhile { it < 5 })
    println(list1.takeLast(5))
    println(list1.takeLastWhile { it > 5 })

    println("  ------   drop -------")
    println(list1.drop(5))
    println(list1.dropWhile { it < 5 })
    println(list1.dropLast(5))
    println(list1.dropLastWhile { it > 5 })

    println("  ------   distinct -------")
    println(list3.distinct())
    println(list3.distinctBy { it + 2 })

    println("  ------   slice -------")
    println(list1.slice(listOf(1,3,5,7)))
    println(list1.slice(IntRange(1,5)))

}

/**
 * 生产操作符
 *
 * plus() : 合并两个集合中的元素，组成一个新的集合。也可以使用符号+
 * zip : 由两个集合按照相同的下标组成一个新集合。该新集合的类型是：List<Pair>
 * unzip : 和zip的作用相反。把一个类型为List<Pair>的集合拆分为两个集合。看下面的例子
 * partition : 判断元素是否满足条件把集合拆分为有两个Pair组成的新集合。
 *
 */
fun optProduce() {
    val list1 = listOf(1,2,3,4)
    val list2 = listOf("kotlin","Android","Java","PHP","JavaScript")

    // plus() 和 `+`一样
    println(list1.plus(list2))
    println(list1 + list2)

    // zip
    println(list1.zip(list2))
    println(list1.zip(list2){       // 组成的新集合由元素少的原集合决定
        it1,it2-> it1.toString().plus("-").plus(it2)
    })

    // unzip
    val newList = listOf(Pair(1,"Kotlin"),Pair(2,"Android"),Pair(3,"Java"),Pair(4,"PHP"))
    println(newList.unzip())

    // partition
    println(list2.partition { it.startsWith("Ja") })
}