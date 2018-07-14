package com.boguan.passbox.adapter;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.boguan.passbox.R;
import com.boguan.passbox.activity.EditPasswordActivity;
import com.boguan.passbox.activity.MainActivity;
import com.boguan.passbox.activity.PasswordDetailsActivity;
import com.boguan.passbox.bean.JsonBean;
import com.boguan.passbox.model.PasswordItem;
import com.boguan.passbox.service.ItemTouchHelperViewHolder;
import com.boguan.passbox.service.LocalService.Mainbinder;
import com.boguan.passbox.utils.Constant;
import com.boguan.passbox.view.FoldableLayout;

import java.util.ArrayList;
import java.util.List;

public class ViewHolder extends RecyclerView.ViewHolder implements android.view.View.OnClickListener, ItemTouchHelperViewHolder {
    private TextView mTitleView;
    private TextView mDateView;
    private TextView mNameView;
    private TextView mPasswordView;
    private TextView mNoteView;
    public LinearLayout mNoteConainer;
    private ImageView mTopIconView;
    public RelativeLayout mCopyView;
    public RelativeLayout mDeleteView;
    public RelativeLayout mEditView;
    public RelativeLayout mMaxView;
    private PasswordItem mPasswordItem;
    private Context context;
    private String mPasswordGroup;
    private Mainbinder mainbinder;
    public FoldableLayout mFoldableLayout;
    private View mShadowViewUp;
    private View mShadowViewDown;
    public TextView mTitleTVCover;
    public TextView mAccountTVCover;
    public ImageView mImageViewCover;
    public RelativeLayout mLayoutCover;
    public ImageView mImageViewDetail;
    private JsonBean mBean;


    public ViewHolder(FoldableLayout foldableLayout, Context c, String group, Mainbinder binder, int height) {
        super(foldableLayout);
        context = c;
        mPasswordGroup = group;
        mainbinder = binder;
        mFoldableLayout = foldableLayout;
        Log.d("ViewHolder", "height:" + height);
        foldableLayout.setupViews(R.layout.list_item_cover, R.layout.list_item_detail,
                height /3, itemView.getContext());
        mTitleTVCover = (TextView) foldableLayout.findViewById(R.id.textview_title_cover);
        mAccountTVCover = (TextView) foldableLayout.findViewById(R.id.textview_account_cover);
        mImageViewCover = (ImageView) foldableLayout.findViewById(R.id.imageview_cover);
        mLayoutCover = (RelativeLayout) foldableLayout.findViewById(R.id.layout_cover);
        mTopIconView = (ImageView) foldableLayout.findViewById(R.id.main_item_top);
        mTitleView = (TextView) foldableLayout.findViewById(R.id.main_item_title);
        mCopyView = (RelativeLayout) foldableLayout.findViewById(R.id.main_item_copy);
        mDateView = (TextView) foldableLayout.findViewById(R.id.main_item_date);
        mNameView = (TextView) foldableLayout.findViewById(R.id.main_item_name);
        mPasswordView = (TextView) foldableLayout.findViewById(R.id.main_item_password);
        mNoteView = (TextView) foldableLayout.findViewById(R.id.main_item_note);
        mNoteConainer = (LinearLayout) foldableLayout.findViewById(R.id.main_item_note_container);
        mDeleteView = (RelativeLayout) foldableLayout.findViewById(R.id.main_item_delete);
        mEditView = (RelativeLayout) foldableLayout.findViewById(R.id.main_item_edit);
        mMaxView = (RelativeLayout) foldableLayout.findViewById(R.id.main_item_max);
        mShadowViewUp = foldableLayout.findViewById(R.id.shadow_up);
        mShadowViewDown = foldableLayout.findViewById(R.id.shadow_dowm);
        mImageViewDetail = (ImageView) foldableLayout.findViewById(R.id.main_item_img);
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_item_copy:
                onCopyClick();
                break;
            case R.id.main_item_delete:
                onDeleteClick();
                break;
            case R.id.main_item_edit:
                onEditClick();
                break;
            case R.id.main_item_max:
                onMaxClick();
                break;
            default:
                break;
        }
    }

    private void onCopyClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        String[] item = new String[]{context.getResources().getString(R.string.copy_name),
                context.getResources().getString(R.string.copy_password)};

        builder.setItems(item, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        // 复制名字
                        ClipboardManager cmbName = (ClipboardManager) context
                                .getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipDataName = ClipData.newPlainText(null, mPasswordItem.password.getUserName());
                        cmbName.setPrimaryClip(clipDataName);
                        Toast.makeText(context, R.string.copy_name_toast, Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        // 复制密码
                        ClipboardManager cmbPassword = (ClipboardManager) context
                                .getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipData = ClipData.newPlainText(null, mPasswordItem.password.getPassword());
                        cmbPassword.setPrimaryClip(clipData);
                        Toast.makeText(context, R.string.copy_password_toast, Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
            }
        });
        builder.show();
    }

    private void onEditClick() {
        Intent intent = new Intent(context, EditPasswordActivity.class);
        intent.putExtra(EditPasswordActivity.ID, mPasswordItem.password.getId());
        intent.putExtra(EditPasswordActivity.PASSWORD_GROUP, mPasswordGroup);
        context.startActivity(intent);
    }
    private void onMaxClick() {
        Intent intent = new Intent(context, PasswordDetailsActivity.class);
        intent.putExtra(PasswordDetailsActivity.ID, mPasswordItem.password.getId());
        intent.putExtra(PasswordDetailsActivity.PASSWORD_USERNAME, mPasswordItem.password.getUserName());
        intent.putExtra(PasswordDetailsActivity.PASSWORD_PASSWORD, mPasswordItem.password.getPassword());
        intent.putExtra(PasswordDetailsActivity.PASSWORD_GROUP, mPasswordGroup);
        intent.putExtra(PasswordDetailsActivity.PASSWORD_TITLE,mPasswordItem.password.getTitle());
        context.startActivity(intent);
    }

    private void onDeleteClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.alert_delete_message);
        builder.setTitle(mPasswordItem.password.getTitle());
        builder.setNeutralButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mainbinder.deletePassword(mPasswordItem.password.getId());
            }
        });
        builder.setNegativeButton(R.string.no, null);
        builder.show();
    }

    public void bindView(PasswordItem passwordItem, int position, ArrayList<JsonBean> list, List<PasswordItem> titleList) {
        Log.d("ViewHolder", "bindView");
        this.mPasswordItem = passwordItem;
        mDateView.setText(passwordItem.dataString);
        mNameView.setText(passwordItem.password.getUserName());
        mPasswordView.setText(passwordItem.password.getPassword());
        mAccountTVCover.setText(passwordItem.password.getUserName());
        String note = passwordItem.password.getNote();
        mNoteView.setText(note);
        if (passwordItem.password.isTop()) {
            mTopIconView.setVisibility(View.VISIBLE);
        } else {
            mTopIconView.setVisibility(View.GONE);
        }
        int selectPosition = 4;
        //根据标题名字数据判断显示的host和背景颜色
        for (int i = 0; i < list.size(); i++) {
            ArrayList<String> strs = (ArrayList<String>) list.get(i).getKey();
            for (int j = 0; j < strs.size(); j++) {
                if (titleList.get(position).password.getTitle().equals(strs.get(j))) {
                    selectPosition = i;
                    break;
                }
            }
        }
        mBean = list.get(selectPosition);
        //cover
        switch (mBean.getIcon()){
            case Constant.PASSWORD_ITEM_BAIDU:
                mImageViewCover.setImageResource(R.drawable.baidu);
                mImageViewDetail.setImageResource(R.drawable.baidu);
                break;
            case Constant.PASSWORD_ITEM_TUDOU:
                mImageViewCover.setImageResource(R.drawable.tudou);
                mImageViewDetail.setImageResource(R.drawable.tudou);
                break;
            case Constant.PASSWORD_ITEM_XUNLEI:
                mImageViewCover.setImageResource(R.drawable.xunlei);
                mImageViewDetail.setImageResource(R.drawable.xunlei);
                break;
            case Constant.PASSWORD_ITEM_WEIXIN:
                mImageViewCover.setImageResource(R.drawable.weixin);
                mImageViewDetail.setImageResource(R.drawable.weixin);
                break;
            default:
                mImageViewDetail.setImageResource(R.drawable.pan_kuaipan);
                mImageViewCover.setImageResource(R.drawable.pan_kuaipan);
                break;
        }
            mLayoutCover.setBackgroundColor(Color.parseColor(mBean.getColor()));
            mTitleTVCover.setText(mBean.getHost());
            mTitleView.setText(mBean.getHost());

    }

    @Override
    public void onItemSelected() {
        mShadowViewUp.setVisibility(View.VISIBLE);
        mShadowViewDown.setVisibility(View.VISIBLE);
    }

    @Override
    public void onItemClear() {
        itemView.setBackgroundColor(0);
        mShadowViewUp.setVisibility(View.GONE);
        mShadowViewDown.setVisibility(View.GONE);
    }
}

