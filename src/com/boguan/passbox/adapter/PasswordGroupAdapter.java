package com.boguan.passbox.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.boguan.passbox.R;
import com.boguan.passbox.model.PasswordGroup;

public class PasswordGroupAdapter extends BaseAdapter {
    private List<PasswordGroup> mPasswordGroups = new ArrayList<>();
    private Context context;
    private String mCurrentGroupName;
    private int mCurrentGruopId;

    public PasswordGroupAdapter(Context context) {
        super();
        this.context = context;
    }

    public void setmCurrentGroupName(String mCurrentGroupName) {
        this.mCurrentGroupName = mCurrentGroupName;
        notifyDataSetChanged();
    }

    public String getmCurrentGroupName() {
        return mCurrentGroupName;
    }

    public int getmCurrentGruopId() {
        return mCurrentGruopId;
    }

    public void setmCurrentGruopId(int mCurrentGruopId) {
        this.mCurrentGruopId = mCurrentGruopId;
    }

    public void setData(List<PasswordGroup> passwordGroups) {
        this.mPasswordGroups.clear();
        this.mPasswordGroups.addAll(passwordGroups);
        notifyDataSetChanged();
    }

    public void addPasswordGroup(PasswordGroup passwordGroup) {
        mPasswordGroups.add(passwordGroup);
        notifyDataSetChanged();
    }

    /**
     * 移除密码分组
     *
     * @param passwordGroupName 密码分组名字
     * @return 是否移除成功
     */
    public boolean removePasswordGroup(String passwordGroupName) {
        boolean result = false;
        for (int i = 0; i < mPasswordGroups.size(); i++) {
            PasswordGroup passwordGroup = mPasswordGroups.get(i);
            if (passwordGroup.getGroupName().equals(passwordGroupName)) {
                result = true;
                mPasswordGroups.remove(i);
                break;
            }
        }
        notifyDataSetChanged();
        return result;
    }

    @Override
    public int getCount() {
        return mPasswordGroups.size();
    }

    @Override
    public PasswordGroup getItem(int position) {
        return mPasswordGroups.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.password_group_item, null);
            viewHolder.name = (TextView) convertView.findViewById(R.id.fragment_password_group_nameView);
            viewHolder.img = (ImageView) convertView.findViewById(R.id.fragment_password_group_arrow);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        PasswordGroup passwordGroup = getItem(position);
        viewHolder.name.setText(passwordGroup.getGroupName());
        viewHolder.img.setImageResource(passwordGroup.getImgId());
        return convertView;
    }

    private class ViewHolder {
        TextView name;
        ImageView img;
    }
}
