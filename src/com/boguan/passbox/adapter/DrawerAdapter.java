package com.boguan.passbox.adapter;

import android.app.Notification;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boguan.passbox.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by wukangjie on 16/12/7.
 */

public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.DrawerViewHolder> {

    private static final int TYPE_DIVIDER = 0;
    private static final int TYPE_NORMAL = 1;
    private static final int TYPE_HEADER = 2;
    private LayoutInflater mInflater;

    private List<Boolean> isClicks;//控件是否被点击,默认为false，如果被点击，改变值，控件根据值改变自身颜色

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, DrawerItem data);
    }

    public DrawerAdapter() {
        isClicks = new ArrayList<Boolean>();
        for (int i = 0; i < dataList.size(); i++) {
            if (i == 0) {
                isClicks.add(true);
            }
            isClicks.add(false);
        }
    }

    private List<DrawerItem> dataList = Arrays.asList(
//            new DrawerItemHeader(),
            new DrawerItemNormal(R.drawable.ic_my_collection, R.string.action_my_collection),
            new DrawerItemDivider(),
            new DrawerItemNormal(R.drawable.ic_action_import_export, R.string.action_import_and_export),
            new DrawerItemDivider(),
            new DrawerItemNormal(R.drawable.ic_lock, R.string.action_about_us),
            new DrawerItemDivider(),
            new DrawerItemNormal(R.drawable.ic_lockk, R.string.action_login_password),
            new DrawerItemDivider(),
            new DrawerItemNormal(R.drawable.ic_action_exit, R.string.action_exit),
            new DrawerItemDivider()
    );


    @Override
    public int getItemViewType(int position) {
        DrawerItem drawerItem = dataList.get(position);
        if (drawerItem instanceof DrawerItemDivider) {
            return TYPE_DIVIDER;
        } else if (drawerItem instanceof DrawerItemNormal) {
            return TYPE_NORMAL;
        }
//        else if(drawerItem instanceof DrawerItemHeader){
//            return TYPE_HEADER;
//        }
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return (dataList == null || dataList.size() == 0) ? 0 : dataList.size();
    }

    @Override
    public DrawerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        DrawerViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case TYPE_DIVIDER:
                viewHolder = new DividerViewHolder(inflater.inflate(R.layout.item_drawer_divider, parent, false));
                break;
            /**
             * 添加头布局
             */
//            case TYPE_HEADER:
//                viewHolder = new HeaderViewHolder(inflater.inflate(R.layout.item_drawer_header, parent, false));
//                break;
            case TYPE_NORMAL:
                viewHolder = new NormalViewHolder(inflater.inflate(R.layout.item_drawer_normal, parent, false));
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(DrawerViewHolder holder, final int position) {
        final DrawerItem item = dataList.get(position);
        if (holder instanceof NormalViewHolder) {
            final NormalViewHolder normalViewHolder = (NormalViewHolder) holder;
            final DrawerItemNormal itemNormal = (DrawerItemNormal) item;
            normalViewHolder.iv.setBackgroundResource(itemNormal.iconRes);
            normalViewHolder.tv.setText(itemNormal.titleRes);
            normalViewHolder.itemView.setTag(dataList.get(position));
            if (isClicks.get(position)) {
                normalViewHolder.itemView.setBackgroundColor(Color.parseColor("#4F903A"));
            } else {
                normalViewHolder.itemView.setBackgroundColor(Color.parseColor("#42425A"));
            }
            normalViewHolder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {

                        listener.itemClick(itemNormal);
                        for (int i = 0; i < isClicks.size(); i++) {
                            isClicks.set(i, false);
                        }
                        isClicks.set(position, true);
                        notifyDataSetChanged();
                    }
                }

            });
        }


//        else if(holder instanceof HeaderViewHolder){
//            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
//        }
        //将数据保存在itemView的Tag中，以便点击时进行获取


    }

    public OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void itemClick(DrawerItemNormal drawerItemNormal);
    }


    //-------------------------item数据模型------------------------------
    // drawerlayout item统一的数据模型
    public interface DrawerItem {
    }


    //有图片和文字的item
    public class DrawerItemNormal implements DrawerItem {
        public int iconRes;
        public int titleRes;

        public DrawerItemNormal(int iconRes, int titleRes) {
            this.iconRes = iconRes;
            this.titleRes = titleRes;
        }

    }

    //分割线item
    public class DrawerItemDivider implements DrawerItem {
        public DrawerItemDivider() {
        }
    }

    //头部item
    public class DrawerItemHeader implements DrawerItem {
        public DrawerItemHeader() {
        }
    }


    //----------------------------------ViewHolder数据模型---------------------------
    //抽屉ViewHolder模型
    public class DrawerViewHolder extends RecyclerView.ViewHolder {

        public DrawerViewHolder(View itemView) {
            super(itemView);
        }
    }

    //有图标有文字ViewHolder
    public class NormalViewHolder extends DrawerViewHolder {
        public View view;
        public TextView tv;
        public ImageView iv;

        public NormalViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            tv = (TextView) itemView.findViewById(R.id.tv);
            iv = (ImageView) itemView.findViewById(R.id.iv);
        }
    }

    //分割线ViewHolder
    public class DividerViewHolder extends DrawerViewHolder {

        public DividerViewHolder(View itemView) {
            super(itemView);
        }
    }

    //头部ViewHolder
//    public class HeaderViewHolder extends DrawerViewHolder {

//        private SimpleDrawerView sdv_icon;
//        private TextView tv_login;
//
//        public HeaderViewHolder(View itemView) {
//            super(itemView);
//            sdv_icon = (SimpleDraweeView) itemView.findViewById(R.id.sdv_icon);
//            tv_login = (TextView) itemView.findViewById(R.id.tv_login);
//        }
//    }
}