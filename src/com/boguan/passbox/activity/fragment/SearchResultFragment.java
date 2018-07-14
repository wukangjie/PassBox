package com.boguan.passbox.activity.fragment;

import android.app.ListFragment;
import android.os.Bundle;
import android.widget.ArrayAdapter;

/**
 * Created by Lymons on 16/3/29.
 */
public class SearchResultFragment extends ListFragment {
    private final String[] rows = { "abc", "aab", "aac", "aaa", "abb",
            "acc", "cab", "ccc", "bbb" };

    public SearchResultFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // ListViewにFilterをかけれるようにする
        getListView().setTextFilterEnabled(true);

        // ListViewに表示するItemの設定
        setListAdapter(new ArrayAdapter(getActivity(),
                android.R.layout.simple_list_item_1, rows));
    }

    /**
     * ListViewにFilterをかける
     * @param s
     */
    public void setFilter(String s){
        getListView().setFilterText(s);
    }

    /**
     * ListViewのFilterをClearする
     */
    public void clearFilter(){
        getListView().clearTextFilter();
    }
}
