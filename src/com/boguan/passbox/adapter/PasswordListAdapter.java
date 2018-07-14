package com.boguan.passbox.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.boguan.passbox.R;
import com.boguan.passbox.activity.MainActivity;
import com.boguan.passbox.bean.JsonBean;
import com.boguan.passbox.model.Password;
import com.boguan.passbox.model.PasswordGroup;
import com.boguan.passbox.model.PasswordItem;
import com.boguan.passbox.service.ItemTouchHelperAdapter;
import com.boguan.passbox.service.LocalService.Mainbinder;
import com.boguan.passbox.view.FoldableLayout;

import cn.zdx.lib.annotation.FindViewById;
import cn.zdx.lib.annotation.XingAnnotationHelper;

/**
 * 主界面密码适配器
 *
 * @author zengdexing
 */
public class PasswordListAdapter extends RecyclerView.Adapter<ViewHolder> implements ItemTouchHelperAdapter  {
    private List<PasswordItem> passwords = new ArrayList<PasswordItem>();
    private ArrayList<JsonBean> list;
    private Map<Integer, Boolean> mFoldStates = new HashMap<>();
    private Context context;
    private int padding;
    private Mainbinder mainbinder;
    private String passwordGroup;
    private ViewHolder viewHolder;
    private View convertView;
    private int mHeight;


    private Comparator<PasswordItem> comparator = new Comparator<PasswordItem>() {
        @Override
        public int compare(PasswordItem lhs, PasswordItem rhs) {

            long value = rhs.password.getId() - lhs.password.getId();
            if (value > 0)
                return 1;
            else if (value == 0)
                return 0;
            else
                return -1;
        }
    };
    public int dip2px(float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public PasswordListAdapter(Context context , int height ) {
        this.context = context;
        mHeight = height;
        padding = dip2px(6);
    }


    public void setData(ArrayList<JsonBean> list, List<Password> passwords, Mainbinder mainbinder) {
        this.list = list;
        this.mainbinder = mainbinder;
        this.passwords.clear();
        for (Password password : passwords) {
            PasswordItem item = new PasswordItem(context, password);
            this.passwords.add(item);
        }
        notifyDataSetChanged();
    }

    public int getCount() {
        return passwords.size();
    }

    public PasswordItem getItem(int position) {
        return passwords.get(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        viewHolder = new ViewHolder(new FoldableLayout(context), context, passwordGroup, mainbinder, mHeight);
        convertView = LayoutInflater.from(context).inflate(R.layout.list_item_cover, parent, false);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        XingAnnotationHelper.findView(viewHolder, convertView);
        viewHolder.mCopyView.setOnClickListener(viewHolder);
        viewHolder.mDeleteView.setOnClickListener(viewHolder);
        viewHolder.mEditView.setOnClickListener(viewHolder);
        viewHolder.mMaxView.setOnClickListener(viewHolder);
        viewHolder.mLayoutCover.setMinimumHeight(mHeight / 3);
//        if (position == 0) {
//            convertView.setPadding(padding, padding, padding, padding);
//
//        } else {
//            convertView.setPadding(padding, 0, padding, padding);
//        }
        if (mFoldStates.containsKey(position)) {
            if (mFoldStates.get(position) == Boolean.TRUE) {
                if (!holder.mFoldableLayout.isFolded()) {
                    holder.mFoldableLayout.foldWithoutAnimation();
                }
            } else if (mFoldStates.get(position) == Boolean.FALSE) {
                if (holder.mFoldableLayout.isFolded()) {
                    holder.mFoldableLayout.unfoldWithoutAnimation();
                }
            }
        } else {
            holder.mFoldableLayout.foldWithoutAnimation();
        }
        holder.mFoldableLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.mFoldableLayout.isFolded()) {
                    holder.mFoldableLayout.unfoldWithAnimation();
                } else {
                    holder.mFoldableLayout.foldWithAnimation();
                }
            }
        });
        holder.mFoldableLayout.setFoldListener(new FoldableLayout.FoldListener() {
            @Override
            public void onUnFoldStart() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    holder.mFoldableLayout.setElevation(5);
                }
            }

            @Override
            public void onUnFoldEnd() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    holder.mFoldableLayout.setElevation(0);
                }
                mFoldStates.put(holder.getAdapterPosition(), false);
            }

            @Override
            public void onFoldStart() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    holder.mFoldableLayout.setElevation(5);
                }
            }

            @Override
            public void onFoldEnd() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    holder.mFoldableLayout.setElevation(0);
                }
                mFoldStates.put(holder.getAdapterPosition(), true);
            }
        });
        int reversePosition = this.passwords.size() - 1 - position;
        PasswordItem passwordItem = getItem(reversePosition);
        holder.bindView(passwordItem, reversePosition, list, this.passwords);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return passwords.size();
    }

    public void onNewPassword(Password password) {
        passwords.add(0, new PasswordItem(context, password));
//        Collections.sort(this.passwords, comparator);
        notifyDataSetChanged();
    }

    public void onDeletePassword(int id) {
        for (int i = 0; i < passwords.size(); i++) {
            PasswordItem passwordItem = passwords.get(i);
            if (passwordItem.password.getId() == id) {
                passwords.remove(i);
                break;
            }
        }
        notifyDataSetChanged();
    }

    public void onDeleteAllPassword() {
        this.passwords.clear();
        notifyDataSetChanged();
    }

    public void onUpdatePassword(Password newPassword) {
        boolean needSort = false;
        boolean hasFind = false;
        for (int i = 0; i < passwords.size(); i++) {
            Password oldPassword = passwords.get(i).password;
            if (oldPassword.getId() == newPassword.getId()) {
                if (newPassword.getCreateDate() != 0)
                    oldPassword.setCreateDate(newPassword.getCreateDate());
                if (newPassword.getTitle() != null)
                    oldPassword.setTitle(newPassword.getTitle());
                if (newPassword.getUserName() != null)
                    oldPassword.setUserName(newPassword.getUserName());
                if (newPassword.getPassword() != null)
                    oldPassword.setPassword(newPassword.getPassword());
                if (newPassword.getNote() != null)
                    oldPassword.setNote(newPassword.getNote());
                if (oldPassword.isTop() != newPassword.isTop()) {
                    oldPassword.setTop(newPassword.isTop());
                    needSort = true;
                }
                if (!oldPassword.getGroupName().equals(newPassword.getGroupName()))
                    passwords.remove(i);
                hasFind = true;
                break;
            }
        }
        if (!hasFind) {
            passwords.add(0, new PasswordItem(context, newPassword));
            needSort = true;
        }
//        if (needSort)
//            Collections.sort(this.passwords, comparator);
        notifyDataSetChanged();
    }

    public void setPasswordGroup(String passwordGroup) {
        this.passwordGroup = passwordGroup;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(passwords, passwords.size() - fromPosition - 1, passwords.size() - toPosition - 1);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(int position) {
        passwords.remove(position);
        notifyItemRemoved(position);
    }
}
