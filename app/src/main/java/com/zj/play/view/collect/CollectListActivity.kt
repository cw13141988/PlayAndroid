package com.zj.play.view.collect

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.zj.core.view.StaggeredDividerItemDecoration
import com.zj.play.R
import com.zj.play.view.home.ArticleCollectBaseActivity
import kotlinx.android.synthetic.main.activity_collect_list.*
import kotlin.system.measureTimeMillis

class CollectListActivity : ArticleCollectBaseActivity() {

    private val viewModel by lazy { ViewModelProvider(this).get(CollectListViewModel::class.java) }

    override fun getLayoutId(): Int {
        return R.layout.activity_collect_list
    }

    private lateinit var articleAdapter: CollectAdapter
    private var page = 1

    override fun initData() {
        collectTitleBar.setTitle("我的收藏")
        viewModel.dataLiveData.observe(this, {
            if (it.isSuccess) {
                val articleList = it.getOrNull()
                if (articleList != null) {
                    loadFinished()
                    if (page == 1 && viewModel.dataList.size > 0) {
                        viewModel.dataList.clear()
                    }
                    viewModel.dataList.addAll(articleList.datas)
                    articleAdapter.notifyDataSetChanged()
                } else {
                    showLoadErrorView()
                }
            } else {
                showBadNetworkView { getArticleList() }
            }
        })
        getArticleList()
    }

    override fun initView() {
        when (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            true -> {
                collectRecycleView.layoutManager = LinearLayoutManager(this)
            }
            false -> {
                val spanCount = 2
                val layoutManager =
                    StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
                collectRecycleView.layoutManager = layoutManager
                layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE;
                collectRecycleView.itemAnimator = null
                collectRecycleView.addItemDecoration(StaggeredDividerItemDecoration(this))
                collectRecycleView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        val first = IntArray(spanCount)
                        layoutManager.findFirstCompletelyVisibleItemPositions(first)
                        if (newState == RecyclerView.SCROLL_STATE_IDLE && (first[0] == 1 || first[1] == 1)) {
                            layoutManager.invalidateSpanAssignments()
                        }
                    }
                })
            }
        }
        articleAdapter = CollectAdapter(
            this,
            viewModel.dataList
        )
        articleAdapter.setHasStableIds(true)
        collectRecycleView.adapter = articleAdapter
        collectSmartRefreshLayout.apply {
            setOnRefreshListener { reLayout ->
                reLayout.finishRefresh(measureTimeMillis {
                    page = 1
                    getArticleList()
                }.toInt())
            }
            setOnLoadMoreListener { reLayout ->
                val time = measureTimeMillis {
                    page++
                    getArticleList()
                }.toInt()
                reLayout.finishLoadMore(if (time > 1000) time else 1000)
            }
        }
    }

    private fun getArticleList() {
        if (viewModel.dataList.size <= 0) startLoading()
        viewModel.getDataList(page)
    }

    companion object {
        fun actionStart(context: Context) {
            val intent = Intent(context, CollectListActivity::class.java)
            context.startActivity(intent)
        }
    }

}
