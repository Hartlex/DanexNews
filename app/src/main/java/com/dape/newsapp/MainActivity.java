package com.dape.newsapp;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dape.newsapp.adapter.NewsAdapter;
import com.dape.newsapp.loader.NewsLoader;
import com.dape.newsapp.model.News;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<News>> {
    private static final String LOG_TAG = MainActivity.class.getName();
    private static final String NEWS_REQUEST_URL =
            "https://content.guardianapis.com/search?q=debate&tag=politics/politics&from-date=2014-01-01&api-key=7792f6fb-2648-4b4c-a5ac-e8ee3f49fd96&show-tags=contributor";
    private static final int NEWS_LOADER_ID = 1;
    private NewsAdapter newsAdapter;
    private ProgressBar pgb_news;
    private TextView tv_emptyData;
    private RecyclerView lsv_news;
    private NetworkInfo networkInfo;
    private LoaderManager loaderManager;
    private ArrayList<News> listNews = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        init();
    }
    @Override
    protected void onResume() {
        super.onResume();
        loaderManager.restartLoader(NEWS_LOADER_ID, null, this);
    }
    @Override
    public Loader<List<News>> onCreateLoader(int i, Bundle bundle) {
        tv_emptyData.setVisibility(View.GONE);
        pgb_news.setVisibility(View.VISIBLE);
        lsv_news.setVisibility(View.GONE);
        return new NewsLoader(this, NEWS_REQUEST_URL);
    }
    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> news) {
        if (networkInfo != null && networkInfo.isConnected()) {
            if (news == null || news.size() == 0){
                pgb_news.setVisibility(View.GONE);
                lsv_news.setVisibility(View.GONE);
                tv_emptyData.setVisibility(View.VISIBLE);
                tv_emptyData.setText(getString(R.string.no_news));
            }else{
                tv_emptyData.setVisibility(View.GONE);
                pgb_news.setVisibility(View.GONE);
                lsv_news.setVisibility(View.VISIBLE);
                newsAdapter.addItems(news);
            }
        }else{
            pgb_news.setVisibility(View.GONE);
            lsv_news.setVisibility(View.GONE);
            tv_emptyData.setVisibility(View.VISIBLE);
            tv_emptyData.setText(getString(R.string.no_data));
        }
    }
    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
            newsAdapter.clear();
    }
    public void init() {
        lsv_news = findViewById(R.id.lsv_news);
        tv_emptyData = findViewById(R.id.tv_emptyData);
        pgb_news = findViewById(R.id.pgb_news);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        lsv_news.setLayoutManager(mLayoutManager);
        lsv_news.setItemAnimator(new DefaultItemAnimator());
        lsv_news.setHasFixedSize(true);
        newsAdapter = new NewsAdapter(this, new ArrayList<News>());
        loaderManager = getLoaderManager();
        loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connMgr != null;
        networkInfo = connMgr.getActiveNetworkInfo();
        lsv_news.setAdapter(newsAdapter);
    }
}
