package com.android.customwidget.kotlin.fragment

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.*
import com.android.customwidget.R
import com.android.customwidget.kotlin.ext.dispatchMainLoopWork
import com.android.customwidget.kotlin.ext.dispatchSerialWork
import com.android.customwidget.kotlin.ext.getAssetsFileJson
import com.android.customwidget.kotlin.widget.linkage.adapter.LeftNavigationAdapter
import com.android.customwidget.kotlin.widget.linkage.adapter.RightNavigationAdapter
import com.android.customwidget.kotlin.widget.linkage.bean.Navigation
import com.android.customwidget.kotlin.widget.linkage.bean.NavigationBean
import com.google.gson.Gson
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.constant.SpinnerStyle
import kotlinx.android.synthetic.main.fragment_navigation.*


class Navigation3Fragment : androidx.fragment.app.Fragment() {

    var currentPosition = 0
    var leftList = mutableListOf<NavigationBean>()

    //初始化左侧recyclerview的adapter
    private val leftNavigationAdapter: LeftNavigationAdapter by lazy {
        LeftNavigationAdapter(context as Activity)
    }

    //初始化右侧recyclerview的adapter
    private val rightNavigationAdapter: RightNavigationAdapter by lazy {
        RightNavigationAdapter(context as Activity)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        getNavi()
        return inflater.inflate(R.layout.fragment_navigation, null)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        recyclerViewLinkage()
        super.onActivityCreated(savedInstanceState)
    }

    protected var mRefreshView: SmartRefreshLayout? = null

    /**
     * 左右两个RecyclerView联动
     */
    private fun recyclerViewLinkage() {
        rvLeft.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        rvRight.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)

//        closeDefaultAnimator(rvRight)
        (rvRight.itemAnimator as androidx.recyclerview.widget.SimpleItemAnimator).supportsChangeAnimations = false

        rvLeft.adapter = leftNavigationAdapter
        rvRight.adapter = rightNavigationAdapter

        if (mRefreshView == null) {
            val parent = rvRight.parent as ViewGroup
            parent.removeView(rvRight)

            mRefreshView = createRefreshView()
            mRefreshView?.addView(rvRight)
            parent.addView(mRefreshView)

            mRefreshView?.setRefreshHeader(ClassicsHeader(context));
            mRefreshView?.setRefreshFooter(ClassicsFooter(context).setSpinnerStyle(SpinnerStyle.Scale))
        }

        mRefreshView?.apply {
            setOnRefreshListener {
                Log.e("mRefreshView", "setOnRefreshListener")
                finishRefresh(true)
                updata()
                return@setOnRefreshListener
            }

            setOnLoadMoreListener {
                Log.e("mRefreshView", "setOnLoadMoreListener")
                finishLoadMore(true)
                downdata()
                return@setOnLoadMoreListener
            }
        }

        /**
         * 既然是动画，就会有时间，我们把动画执行时间变大一点来看一看效果
         */
//        val defaultItemAnimator = DefaultItemAnimator()
//        defaultItemAnimator.addDuration = 300
//        defaultItemAnimator.removeDuration = 300
//        rvRight.itemAnimator = null

        //左边联动右边
        leftNavigationAdapter.setOnItemClickListener { position ->
            if (currentPosition == position) {
                return@setOnItemClickListener
            }
            currentPosition = position
            update(currentPosition)
        }
    }

    private fun closeDefaultAnimator(mRvCustomer: androidx.recyclerview.widget.RecyclerView?) {
        if (null == mRvCustomer) return
        mRvCustomer.itemAnimator!!.addDuration = 0
        mRvCustomer.itemAnimator!!.changeDuration = 0
        mRvCustomer.itemAnimator!!.moveDuration = 0
        mRvCustomer.itemAnimator!!.removeDuration = 0
        (mRvCustomer.itemAnimator as androidx.recyclerview.widget.SimpleItemAnimator?)!!.supportsChangeAnimations = false
    }

    private fun updata() {
        if (currentPosition > 0) {
            currentPosition--
            update(currentPosition)
        }
    }

    private fun downdata() {
        if (currentPosition < leftList.size) {
            currentPosition++
            update(currentPosition)
        }
    }

    private fun update(position: Int) {
        rvRight.postDelayed({
            anim()
            leftNavigationAdapter.setChoose(position)
            val singleList = mutableListOf<NavigationBean>()
            singleList.add(leftList[position])
            singleList.add(leftList[position])
            singleList.add(leftList[position])
            singleList.add(leftList[position])
            singleList.add(leftList[position])
            rightNavigationAdapter.setDataList(singleList)
        }, 500)
    }

    private fun anim() {
        val animationSet = AnimationSet(false)
        animationSet.addAnimation(AlphaAnimation(0f, 1f))
        animationSet.addAnimation(TranslateAnimation(0f, 0f, 1800f, 0f))
        animationSet.duration = 1000
        //如果不添加setFillEnabled和setFillAfter则动画执行结束后会自动回到远点
        animationSet.isFillEnabled = true //使其可以填充效果从而不回到原地
        animationSet.fillAfter = true //不回到起始位置
//        animationSet.interpolator = AccelerateDecelerateInterpolator()
        animationSet.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationEnd(animation: Animation?) {
                animationSet.reset()
            }

            override fun onAnimationStart(animation: Animation?) {

            }

        })
        rvRight.startAnimation(animationSet)
    }

    /**
     * 获取导航数据
     */
    private fun getNavi() {
        dispatchSerialWork {
            val data = context?.getAssetsFileJson("Navigation")
            val nav = Gson().fromJson(data, Navigation::class.java)

            dispatchMainLoopWork {
                leftList = nav.data
                //默认左侧第一个为选中状态
                leftList[0].isChoose = true
                //分别给左右两个adapter填充数据
                leftNavigationAdapter.setDataList(leftList)
                val singleList = mutableListOf<NavigationBean>()
                singleList.add(nav.data[0])
                rightNavigationAdapter.setDataList(singleList)
                //右侧recyclerview悬浮置顶效果
//                val top = TopItemDecoration(context as Activity).apply {
//                    this.tagListener = {
//                        leftList[it].name
//                    }
//                }
//                rvRight.addItemDecoration(top)
            }
        }
    }

    private fun createRefreshView(): SmartRefreshLayout {
        val refreshView = SmartRefreshLayout(context)
        refreshView.id = R.id.refresh_view
        refreshView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        return refreshView
    }
}