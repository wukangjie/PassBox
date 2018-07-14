package com.boguan.passbox.activity;

import android.app.ActionBar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.boguan.passbox.R;
import com.boguan.passbox.view.SettingListView;
import com.boguan.passbox.view.SettingListView.ItemEntity;
import com.boguan.passbox.view.SettingListView.ItemStyle;
import com.boguan.passbox.view.SettingListView.SettingsItemLayoutChange;

/**
 * Created by linfangzhou on 15/9/9.
 */
public class SettingsActivity extends BaseActivity implements SettingsItemLayoutChange{

    private SettingListView mListView;
    private ItemEntity[] itemEntities;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initActionBar();
        initData();
        initView();
    }

    private void initData(){
        itemEntities = new ItemEntity[]{new ItemEntity(""),
                new ItemEntity(ItemStyle.ITEM_STYLE_VALUE_ARROW, "张三", "Test1", "linfangzhou@fenrir-tec.com", null),
                new ItemEntity("test"),
                new ItemEntity("邮件设置"),
                new ItemEntity(ItemStyle.ITEM_STYLE_VALUE, "新邮件提醒", "Test1", "收件箱", null),
                new ItemEntity("声音", "Test1", new Boolean(true)),
                new ItemEntity(ItemStyle.ITEM_STYLE_VALUE_ARROW, "显示邮件中的图片", "Test1", "仅wifi", null),
                new ItemEntity("转发邮件时带上附件", "Test1", new Boolean(false)),
                new ItemEntity(ItemStyle.ITEM_STYLE_VALUE_ARROW, "默认发件人", "Test1", "linfangzhou@fenrir-tec.com",null),
                new ItemEntity(ItemStyle.ITEM_STYLE_VALUE_ARROW, "邮件正文自动下载", "Test1", "仅wifi", null),
                new ItemEntity(ItemStyle.ITEM_STYLE_VALUE_ARROW, "签名", "Test1", "来自CloudMail-Android", null),
                new ItemEntity("安全"),
                new ItemEntity(ItemStyle.ITEM_STYLE_VALUE_ARROW, "手势密码", "Test2", "关闭", null),
                new ItemEntity(ItemStyle.ITEM_STYLE_DEFAULT_ARROW, "联系人屏蔽列表", "Test2", null, null),
                new ItemEntity(ItemStyle.ITEM_STYLE_DEFAULT_ARROW, "扫一扫", "Test2", null, null),
                new ItemEntity("其它"),
                new ItemEntity(ItemStyle.ITEM_STYLE_DEFAULT_ARROW, "关于阿里云邮", "Test3", null, null),
                new ItemEntity(ItemStyle.ITEM_STYLE_DEFAULT_ARROW, "意见反馈", "Test3", null, null),
                new ItemEntity("退出", "Test3")};
    }

    private void initView(){
        mListView = (SettingListView) findViewById(R.id.mListview);
        mListView.setLisViewData(this, itemEntities);
        mListView.setItemLayoutChangeInterface(this);
//        mListView.setItemBackgroundColor(R.drawable.kuaipan_login_btn);
//        mListView.setSectionBackgroundColor(R.color.title_color);
//        mListView.setSectionKeyColor(R.color.title_color);
//        mListView.setItemValueTxtColor(R.color.title_color);

    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    public void Test1(ItemEntity data, Object arg){
        if (arg instanceof Boolean == false) {
            Toast.makeText(this, "执行了Test1方法", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "执行了Test1方法,switchState==="+arg, Toast.LENGTH_SHORT).show();
//        data.setDataValue("jkfjsdlfjsk");
//        mListView.refreshSettingListView();
    }
    public void Test2(ItemEntity data, Object arg){
        if (arg instanceof Boolean == false) {
            Toast.makeText(this, "执行了Test2方法", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "执行了Test2方法,switchState==="+arg, Toast.LENGTH_SHORT).show();
//        data.setDataValue("jkfjsdlfjsk");
//        mListView.refreshSettingListView();
    }
    public void Test3(ItemEntity data, Object arg){
        if (arg instanceof Boolean == false) {
            Toast.makeText(this, "执行了Test3方法", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "执行了Test3方法,switchState==="+arg, Toast.LENGTH_SHORT).show();
//        data.setDataValue("jkfjsdlfjsk");
//        mListView.refreshSettingListView();
    }

    @Override
    public View changeItemLayout(View convertView, ItemEntity data) {
        if (data.getDataKey().equals("张三")) {
            portraitLayout(convertView);
        }

        if (data.getDataKey().equals("test")) {
            convertView = LayoutInflater.from(this).inflate(R.layout.test_item, null);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplicationContext(),"点击了自定义子项",Toast.LENGTH_SHORT).show();
                }
            });
            return convertView;
        }
        return null;
    }

    @SuppressWarnings("ResourceType")
    public void portraitLayout(View convertView) {

        TextView keyView = (TextView) convertView.findViewById(R.id.settings_list_item_key);
        RelativeLayout.LayoutParams keyLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        keyLayoutParams.leftMargin = dip2px(3);
        keyLayoutParams.topMargin = dip2px(10);
        keyLayoutParams.addRule(RelativeLayout.RIGHT_OF, 2);
        keyView.setId(1);
        keyView.setLayoutParams(keyLayoutParams);

        TextView valueView = (TextView) convertView.findViewById(R.id.settings_list_item_value);
        ViewGroup vg = (ViewGroup) valueView.getParent();
        vg.removeView(valueView);
        RelativeLayout.LayoutParams valueLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        valueLayoutParams.addRule(RelativeLayout.BELOW,1);
        valueLayoutParams.leftMargin = dip2px(60);

        ImageView imageView = new ImageView(this);
        RelativeLayout.LayoutParams imageLayoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        imageView.setId(2);
        imageView.setImageResource(R.drawable.alm_mine);
        imageView.setLayoutParams(imageLayoutParams);
        imageLayoutParams.leftMargin = dip2px(10);
        imageLayoutParams.rightMargin = dip2px(10);
        imageLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);

        RelativeLayout layout = (RelativeLayout) convertView.findViewById(R.id.settings_list_not_section_layout);
        layout.getLayoutParams().height = dip2px(60);
        layout.addView(valueView,valueLayoutParams);
        layout.addView(imageView,imageLayoutParams);

    }

    public int dip2px(float dpValue) {
        final float scale = this.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
