package com.android.architecture.demolist.paging.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.paging.DataSource
import android.arch.paging.LivePagedListBuilder
import android.arch.paging.PageKeyedDataSource
import android.arch.paging.PagedList
import android.os.Handler
import android.os.Looper
import com.android.architecture.demolist.paging.db.Student
import com.android.architecture.demolist.paging.db.StudentDb
import java.util.concurrent.Executors

/**
 * PagedList：一个继承了 AbstractList 的 List 子类， 包括了数据源获取的数据
 * DataSource或DataSource.Factory ：数据源，DataSource将数据转变成PagedList，DataSource.Factory则用来创建DataSource
 * LivePagedListBuilder：用来生成LiveData<PagedList<T>>，需要DataSource.Factory 和 PagedList.Config参数
 * PagedList.Config: 配置PagedList从Datasource加载数据的方式， 其中包含以下属性：
 *  pageSize：设置每页加载的数量
 *  prefetchDistance：预加载的数量，默认为pagesize
 *  initialLoadSizeHint：初始化数据时加载的数量，默认为pageSize*3
 *  enablePlaceholders：当item为null是否使用PlaceHolder(空占位符)展示
 * 构建LiveData<PagedList>
 * */
class CommonViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = StudentDb.get(app).studentDao()

    fun getRefreshLiveData(): LiveData<PagedList<Student>> =
            LivePagedListBuilder(dao.getAllStudent(), PagedList.Config.Builder()
                    .setPageSize(PAGE_SIZE)                         //配置分页加载的数量
                    .setEnablePlaceholders(ENABLE_PLACEHOLDERS)     //配置是否启动PlaceHolders
                    .setInitialLoadSizeHint(PAGE_SIZE)              //初始化加载的数量
                    .build()).build()

    // adapter数据加载
//    adapter.submitList(getRefreshNormalData())

    companion object {

        private const val PAGE_SIZE = 15

        private const val ENABLE_PLACEHOLDERS = false
    }

    //-------------------------非LiveData数据1--Room数据库-----------------------------------
    // 普通的非LiveData数据
    val config = PagedList.Config.Builder()
            .setPageSize(15)
            .setPrefetchDistance(2)
            .setInitialLoadSizeHint(15)
            .setEnablePlaceholders(false)
            .build()

    // NotifyExecutor 和  FetchExecutor 是必须设置的。
    fun getRefreshNormalData(): PagedList<Student> =
    // Room数据库的方式：dao.getAllStudent().create()
            // 非Room数据库的方式：
            PagedList.Builder(dao.getAllStudent().create(), config)
                    .setNotifyExecutor {
                        Handler(Looper.getMainLooper()).post { it.run() }
                    }
                    .setFetchExecutor(Executors.newFixedThreadPool(2))
                    .build()

    //-------------------------非LiveData数据2--非Room数据库-----------------------------------

    // NotifyExecutor 和  FetchExecutor 是必须设置的。
    fun getRefreshNormalData2(): PagedList<Student> =
    // Room数据库的方式：dao.getAllStudent().create()
            // 非Room数据库的方式：
            PagedList.Builder(MyDataSourceFactory().create(), config)
                    .setNotifyExecutor {
                        Handler(Looper.getMainLooper()).post { it.run() }
                    }
                    .setFetchExecutor(Executors.newFixedThreadPool(2))
                    .build()

    inner class MyDataSourceFactory : DataSource.Factory<Int, Student>() {
        override fun create(): DataSource<Int, Student> {
            return getDataSource()
        }
    }

    fun getDataSource(): DataSource<Int, Student> {
        return object : PageKeyedDataSource<Int, Student>() {
            // 初始化数据
            override fun loadInitial(params: LoadInitialParams<Int>, callback: LoadInitialCallback<Int, Student>) {
                println("loadInitial size ===: ${params.requestedLoadSize} ")
                // 初始化第5页 params.requestedLoadSize：该值为上面 config 中设置的 setInitialLoadSizeHint(15) 的大小
                getDataBackground(5, params.requestedLoadSize).apply {
                    // 这里的previousPageKey，和nextPageKey决定了前后是否有数据
                    // 如果你传个null，那么就表示前边或者后边没有数据了。也就是下边的loadBefore或者LoadAfter不会执行了
                    // 我们测试初始化用了个key=5，即【getDataBackground(5,...)】，因此前后就用4和6，这里的key你也可用26个英文字母
                    // 如果上面初始化是从第1页开始，则下面参数就为 previousPageKey == null, nextPageKey == 2
                    callback.onResult(this, 4, 6)
                }
            }

            //往上滑动会不停的调用这个方法，往回滑动的时候不调用任何方法
            override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Student>) {
                println("loadAfter size =======: ${params.requestedLoadSize}  page:${params.key}")
                // params.key：当前页数  params.requestedLoadSize：该值为上面 config 中设置的 setPageSize(15) 的大小
                getDataBackground(params.key, params.requestedLoadSize).let {
                    callback.onResult(it, params.key + 1)
                }
            }

            override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Student>) {
                println("loadBefore size====: ${params.requestedLoadSize}  page:${params.key}")
                if (params.key < 0) {
                    return
                }
                // params.key：当前页数  params.requestedLoadSize：该值为上面 config 中设置的 setPageSize(15) 的大小
                getDataBackground(params.key, params.requestedLoadSize).let {
                    callback.onResult(it, params.key - 1)
                }
            }
        }
    }

    fun getDataBackground(page: Int, size: Int): List<Student> {
        println("FragmentPageKeyedDS  getData=====================${Thread.currentThread().name}") //打印的结果是2个线程来回切换pool-4-thread-1，pool-4-thread-2
        var lists = arrayListOf<Student>()
        var startPosition = page * size
        // 循环操作：取代for(int i=0;i<size;i++)
        repeat(size) {
            lists.add(Student(startPosition + it + 1, "stu ${startPosition + it + 1}"))
        }
        return lists
    }
}