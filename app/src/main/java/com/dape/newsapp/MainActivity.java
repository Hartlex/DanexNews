package com.dape.newsapp;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.dape.newsapp.adapter.NewsAdapter;
import com.dape.newsapp.loader.NewsLoader;
import com.dape.newsapp.model.News;
import com.dape.newsapp.utils.NewsQueryUtils;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.*;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<News>> {
    private static final String LOG_TAG = MainActivity.class.getName();
    private static final String API_KEY = "7792f6fb-2648-4b4c-a5ac-e8ee3f49fd96";
    private static String NEWS_REQUEST_URL ="";
    private static String[] tagResult;
    private static final int NEWS_LOADER_ID = 1;
    private NewsAdapter newsAdapter;
    private EditText searchText;
    private ImageButton advancedSearchButton;
    private CardView advancedSearch;
    private Spinner tags1;
    private Spinner tags2;
    private EditText dateText;
    private Button searchButton;
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
        advancedSearchButton = findViewById(R.id.advancedSearchButton);
        advancedSearch = findViewById(R.id.advancedSearch);
        tags1 = findViewById(R.id.dropdown1);
        tags2 = findViewById(R.id.dropdown2);
        dateText = findViewById(R.id.dateText);
        tags1.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    final String request = "https://content.guardianapis.com/tags?api-key="+API_KEY;
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try  {
                                tagResult = NewsQueryUtils.getTags(request);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                    });

                    thread.start();

                }

                return false;
            }
        });
        advancedSearchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showAdvancedSearch();
            }
        });
        searchText = findViewById(R.id.searchText);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String search= convertStringForApi(searchText.getText().toString(),false);
                String date = convertStringForApi(dateText.getText().toString(),true);
                if(date==""){
                    date="2014-01-01";
                }
                NEWS_REQUEST_URL =
                        "https://content.guardianapis.com/"
                                +"search?q=" + search
                                +"&from-date=" + date
                                +"&api-key=" + API_KEY
                                +"&show-tags=contributor";

            }
        });
        searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                searchOnClick();
            }
        });
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
        tv_emptyData.setVisibility(GONE);
        pgb_news.setVisibility(VISIBLE);
        lsv_news.setVisibility(GONE);
        return new NewsLoader(this, NEWS_REQUEST_URL);
    }
    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> news) {
        if (networkInfo != null && networkInfo.isConnected()) {
            if (news == null || news.size() == 0){
                pgb_news.setVisibility(GONE);
                lsv_news.setVisibility(GONE);
                tv_emptyData.setVisibility(VISIBLE);
                tv_emptyData.setText(getString(R.string.no_news));
            }else{
                tv_emptyData.setVisibility(GONE);
                pgb_news.setVisibility(GONE);
                lsv_news.setVisibility(VISIBLE);
                newsAdapter.addItems(news);
            }
        }else{
            pgb_news.setVisibility(GONE);
            lsv_news.setVisibility(GONE);
            tv_emptyData.setVisibility(VISIBLE);
            tv_emptyData.setText(getString(R.string.no_data));
        }
    }
    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
            newsAdapter.clear();
    }
    public void searchOnClick(){
        loaderManager.destroyLoader(NEWS_LOADER_ID);
        newsAdapter = new NewsAdapter(this, new ArrayList<News>());
        loaderManager = getLoaderManager();
        loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connMgr != null;
        networkInfo = connMgr.getActiveNetworkInfo();
        lsv_news.setAdapter(newsAdapter);
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
    public void showAdvancedSearch() {
        switch(advancedSearch.getVisibility()){
            case GONE:
                advancedSearch.setVisibility(VISIBLE);
                break;
            case VISIBLE:
                advancedSearch.setVisibility(GONE);
                break;
        }
    }
    public String convertStringForApi(String str, boolean isDate){

        String ret = "";
        if(isDate){
            String[] split = str.split("\\.");
            if(split[0]!="");
                ret = split[2]+"-"+split[1]+"-"+split[0];
        }
        else {
            StringBuilder sb= new StringBuilder();
            String[] split = str.split(",");
            for (String s : split
            ) {
                if (s != "")
                    sb.append(s).append("%20AND%20");

            }
            ret= sb.toString();
        }
        return ret;
    }
}