package com.boguan.passbox.activity.fragment;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.boguan.passbox.R;
import com.boguan.passbox.activity.MainActivity;
import com.boguan.passbox.activity.PasswordDetailsActivity;
import com.boguan.passbox.activity.SearchActivity;
import com.boguan.passbox.adapter.PasswordAdapter;
import com.boguan.passbox.model.Password;
import com.boguan.passbox.service.LocalService.OnGetAllPasswordCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lymons on 16/3/29.
 */
public class SearchListFragment extends ListFragment implements OnGetAllPasswordCallback {
    private List<Password> mPasswords;
    private boolean mShowingOriginalData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View view = inflater.inflate(R.layout.search_result, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getAllPasswordTitle();
        // ListViewにFilterをかけれるようにする
        getListView().setTextFilterEnabled(true);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(getActivity(), PasswordDetailsActivity.class);
        Password p = (Password)l.getAdapter().getItem(position);
        intent.putExtra(PasswordDetailsActivity.ID, p.getId());
        intent.putExtra(PasswordDetailsActivity.PASSWORD_PASSWORD,p.getPassword());
        intent.putExtra(PasswordDetailsActivity.PASSWORD_USERNAME,p.getUserName());
        intent.putExtra(PasswordDetailsActivity.PASSWORD_GROUP,p.getGroupName());
        intent.putExtra(PasswordDetailsActivity.PASSWORD_TITLE,p.getTitle());
        startActivity(intent);
    }

    /**
     * ListViewにFilterをかける
     *
     * @param s
     */
    public void setFilter(String s) {
        if (this.mPasswords == null || this.mPasswords.size() < 1) {
            return;
        }
        if (mShowingOriginalData == false && this.mPasswords.size() > 0) {
            setListAdapter(new PasswordAdapter(getActivity(), this.mPasswords));
            this.mShowingOriginalData = true;
        }
        if (this.mPasswords.size() > 0) {
            getListView().setFilterText(s);
        }
    }

    /**
     * ListViewのFilterをClearする
     */
    public void clearFilter() {
        getListView().clearTextFilter();
    }

    public void getAllPasswordTitle() {
        MainActivity.mainbinder.getAllPassword(this);
    }

    @Override
    public void onGetAllPassword(String froupName, List<Password> passwords) {
        this.mPasswords = passwords;
        setListAdapter(new PasswordAdapter(getActivity(), this.mPasswords));
        this.mShowingOriginalData = true;
        setFilter(SearchActivity.mLastQueryString);
    }

    public void searchTitle(String target) {
        ArrayList<Password> meets = new ArrayList<Password>();
        for (Password password : this.mPasswords) {
            if (password.getTitle().indexOf(target) >= 0) {
                meets.add(password);
            }
        }
        setListAdapter(new PasswordAdapter(getActivity(), meets));
        this.mShowingOriginalData = false;
    }

}
