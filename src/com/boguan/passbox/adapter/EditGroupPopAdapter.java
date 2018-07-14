package com.boguan.passbox.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.boguan.passbox.R;
import com.boguan.passbox.bean.EditGroupPopBean;

import java.util.List;

/**
 * Created by wukangjie on 2016/12/27.
 */

public class EditGroupPopAdapter extends BaseAdapter {
    private List<EditGroupPopBean> mEditGroupPopBeen;
    private Context context;

    public EditGroupPopAdapter(Context context) {
        this.context = context;
    }

    public void setmEditGroupPopBeen(List<EditGroupPopBean> mEditGroupPopBeen) {
        this.mEditGroupPopBeen = mEditGroupPopBeen;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mEditGroupPopBeen.size();
    }

    @Override
    public Object getItem(int position) {
        return mEditGroupPopBeen.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_group, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        EditGroupPopBean editGroupPopBean = (EditGroupPopBean) getItem(position);
        viewHolder.groupTv.setText(editGroupPopBean.getGroupName());
        return convertView;
    }

    class ViewHolder {
        TextView groupTv;
        public ViewHolder(View v) {
            groupTv = (TextView) v.findViewById(R.id.item_group_text);
        }
    }
}
