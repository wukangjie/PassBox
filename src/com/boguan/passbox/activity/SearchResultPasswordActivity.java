package com.boguan.passbox.activity;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import com.boguan.passbox.R;
import com.boguan.passbox.activity.fragment.SearchResultFragment;

public class SearchResultPasswordActivity extends BaseActivity {
    private SearchResultFragment current = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_password);

        initActionBar();
        
        // ListViewを表示するFragment
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        current = new SearchResultFragment();
        ft.replace(R.id.container, current, "SearchResultFragment");
        ft.commit();

    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        doSearchQuery(intent);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
    
    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.search_result);
    }
    
    private void doSearchQuery(Intent intent){
        if(intent == null)
            return;
        
        String queryAction = intent.getAction();
        if(Intent.ACTION_SEARCH.equals(queryAction)){
            String queryString = intent.getStringExtra(SearchManager.QUERY);
            Log.w("Search", "搜索内容：" + queryString);
        }       
    }

}
