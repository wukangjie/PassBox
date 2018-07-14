package com.boguan.passbox.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.boguan.passbox.R;
import com.boguan.passbox.model.Password;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lymons on 16/3/29.
 */
public class PasswordAdapter extends ArrayAdapter<Password> implements Filterable {
    private List<Password> mOriginalValues;
    private List<Password> mObjects;
    private ArrayFilter filter;//过滤器
    private final Object mLock = new Object();

    public PasswordAdapter(Context context, List<Password> objects) {
        super(context, R.layout.search_result_item, new ArrayList<Password>(objects));
        this.mObjects = objects;
        this.mOriginalValues = new ArrayList<Password>(this.mObjects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater vi = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = vi.inflate(R.layout.search_result_item, parent, false);
        }

        Password p = getItem(position);
        TextView tv = (TextView) convertView.findViewById(R.id.sitename);
        tv.setText(p.getTitle());

        return convertView;
    }

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new ArrayFilter();
        }
        return filter;
    }

    private class ArrayFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            // TODO Auto-generated method stub
            FilterResults results = new FilterResults();
            if (prefix == null || prefix.length() == 0) {
                //没有过滤符就不过滤
                //new ArrayList<String>()表示让ListView一开始的时候什么都没有,而不是全部显示到ListView中
                //new ArrayList<String>(list)表示一开始就让Item全部显示到ListView中
                ArrayList<Password> l;
                synchronized (mLock) {
                    l = new ArrayList<Password>(mOriginalValues);
                }
                results.values = l;
                results.count = l.size();
            } else {

                String prefixString = prefix.toString().toLowerCase();
                ArrayList<Password> values;
                synchronized (mLock) {
                    values = new ArrayList<Password>(mOriginalValues);
                }
                final int count = values.size();
                final ArrayList<Password> newValues = new ArrayList<Password>();
                for (int i = 0; i < count; i++) {
                    final String value = values.get(i).getTitle();//原始字符串
                    final String valueText = value.toString().toLowerCase();
                    if (valueText.startsWith(prefixString)) {
                        newValues.add(values.get(i));
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            // TODO Auto-generated method stub
            mObjects = (List<Password>) results.values;
            clear();
            addAll(mObjects);
        }
    }
}