package com.boguan.passbox.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import com.boguan.passbox.R;
import com.boguan.passbox.activity.SettingsActivity;

/**
 * Created by linfangzhou on 15/9/8.
 */
public class SettingListView extends ListView {

    public interface SettingsItemLayoutChange {
        View changeItemLayout(View convertView, ItemEntity data);
    }

    public enum ItemStyle {
        ITEM_STYLE_DEFAULT,
        ITEM_STYLE_VALUE,
        ITEM_STYLE_DEFAULT_ARROW,
        ITEM_STYLE_VALUE_ARROW,
        ITEM_STYLE_DEFAULT_CHECK,
        ITEM_STYLE_DEFAULT_SWITCH,
        ITEM_STYLE_SECTION
    }

    private Context mContext;
    private ItemEntity[] itemEntities;
    private SettingListAdapter adapter;
    private LayoutInflater layoutInflater;
    private SettingsItemLayoutChange itemLayoutChangeInterface;

    //默认子项的颜色
    private int itemBackgroundId = R.drawable.setting_list_item_bg;

    //默认section背景色
    private int sectionBackgroundId = R.color.item_section_bg;

    //默认section字体颜色
    private int sectionTxtColorId = R.color.text_color;

    //默认itemKey字体颜色
    private int itemKeyTxtColorId = R.color.item_key_txt_color;

    //默认itemValue字体颜色
        private int itemValueTxtColorId = R.color.text_color;

        public SettingListView(Context context) {
        super(context);
    }

    public SettingListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SettingListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SettingListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setLisViewData(Context context,ItemEntity[] data){
        this.mContext = context;
        adapter = new SettingListAdapter(data);
        setAdapter(adapter);

        this.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ItemEntity data = itemEntities[position];
                ItemStyle itemStyle = data.getItemStyle();
                if (itemStyle == ItemStyle.ITEM_STYLE_DEFAULT_CHECK || itemStyle == ItemStyle.ITEM_STYLE_DEFAULT_SWITCH) {
                    return;
                }
                invokeOnItemClickMethod(data, null);
            }
        });
    }

    //设置子项背景颜色
    public void setItemBackgroundColor(int drawableId) {
        itemBackgroundId = drawableId;

    }

    //设置section背景颜色
    public void setSectionBackgroundColor(int colorId) {
        sectionBackgroundId = colorId;

    }

    //设置section字体颜色
    public void setSectionTxtColor(int colorId) {
        sectionTxtColorId = colorId;

    }

    //设置section字体颜色
    public void setItemKeyTxtColor(int colorId) {
        itemKeyTxtColorId = colorId;

    }

    //设置section字体颜色
    public void setItemValueTxtColor(int colorId) {
        itemValueTxtColorId = colorId;

    }

    public void refreshSettingListView() {
        adapter.notifyDataSetChanged();
    }

    public void invokeOnItemClickMethod(ItemEntity data, Object arg) {
        Class c = SettingsActivity.class;
        try {
            String methodName = data.getOnClickMethodName();
            if(methodName == null)
                return;
            Method m1 = c.getDeclaredMethod(methodName, new Class[]{ItemEntity.class, Object.class});
            m1.invoke(mContext, data, arg);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void setItemLayoutChangeInterface(SettingsItemLayoutChange itemLayoutChangeInterface){
        this.itemLayoutChangeInterface = itemLayoutChangeInterface;
    }


    public class SettingListAdapter extends BaseAdapter {

        public SettingListAdapter(ItemEntity[] data) {
            itemEntities = data;
            layoutInflater = LayoutInflater.from(mContext);
        }

        @Override
        public int getCount() {
            return itemEntities.length;
        }

        @Override
        public Object getItem(int position) {
            return itemEntities[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final ItemEntity data = itemEntities[position];

            convertView = layoutInflater.inflate(R.layout.settings_list_item, null);

            TextView keyView = (TextView) convertView.findViewById(R.id.settings_list_item_key);
            TextView valueView = (TextView) convertView.findViewById(R.id.settings_list_item_value);
            ImageView nextImageView = (ImageView) convertView.findViewById(R.id.settings_list_item_nextPage);
            final CheckBox checkBoxView = (CheckBox) convertView.findViewById(R.id.settings_list_item_checkbox);
            RelativeLayout notSectionLayout = (RelativeLayout) convertView.findViewById(R.id.settings_list_not_section_layout);
            TextView sectionKeyView = (TextView) convertView.findViewById(R.id.settings_list_item_section_key);
            final ToggleButton switchView = (ToggleButton) convertView.findViewById(R.id.settings_list_item_switch);

            // 调整convertView的Layout
            if (itemLayoutChangeInterface != null) {
                View customView = itemLayoutChangeInterface.changeItemLayout(convertView, data);
                if (customView != null) {
                    convertView = customView;
                    return convertView;
                }
            }

            notSectionLayout.setBackgroundDrawable(getResources().getDrawable(itemBackgroundId));
            sectionKeyView.setBackgroundColor(getResources().getColor(sectionBackgroundId));
            sectionKeyView.setTextColor(getResources().getColor(sectionTxtColorId));
            keyView.setTextColor(getResources().getColor(itemKeyTxtColorId));
            valueView.setTextColor(getResources().getColor(itemValueTxtColorId));

            sectionKeyView.setVisibility(View.GONE);
            notSectionLayout.setVisibility(View.VISIBLE);

            keyView.setText(data.getDataKey());
            valueView.setText(data.getDataValue());
            switchView.setChecked(data.getOptionValue());
            checkBoxView.setChecked(data.getOptionValue());

            ItemStyle itemStyle = data.getItemStyle();
            switch (itemStyle) {
                case ITEM_STYLE_SECTION:
                    // Section header
                    sectionKeyView.setVisibility(View.VISIBLE);
                    sectionKeyView.setText(data.getDataKey());
                    notSectionLayout.setVisibility(View.GONE);
                    return convertView;
                case ITEM_STYLE_VALUE:
                    //只有key、value时
                    valueView.setVisibility(View.VISIBLE);
                    valueView.setText(data.getDataValue());
                    nextImageView.setVisibility(View.GONE);
                    checkBoxView.setVisibility(View.GONE);
                    switchView.setVisibility(View.GONE);
                    break;
                case ITEM_STYLE_DEFAULT_ARROW:
                    //只有key、下页图标时
                    nextImageView.setVisibility(View.VISIBLE);
                    valueView.setVisibility(View.INVISIBLE);
                    checkBoxView.setVisibility(View.GONE);
                    switchView.setVisibility(View.GONE);
                    break;
                case ITEM_STYLE_VALUE_ARROW:
                    //只有key、value、下页图标时
                    nextImageView.setVisibility(View.VISIBLE);
                    valueView.setVisibility(View.VISIBLE);
                    checkBoxView.setVisibility(View.GONE);
                    switchView.setVisibility(View.GONE);
                    break;
                case ITEM_STYLE_DEFAULT_CHECK:
                    //只有key、开关按钮时
                    checkBoxView.setVisibility(View.VISIBLE);
                    nextImageView.setVisibility(View.GONE);
                    valueView.setVisibility(View.GONE);
                    switchView.setVisibility(View.GONE);
                    break;
                case ITEM_STYLE_DEFAULT_SWITCH:
                    //只有key、开关按钮时
                    switchView.setVisibility(View.VISIBLE);
                    nextImageView.setVisibility(View.GONE);
                    valueView.setVisibility(View.GONE);
                    checkBoxView.setVisibility(View.GONE);
                    break;
                default:
                    //只有key时
                    valueView.setVisibility(View.GONE);
                    nextImageView.setVisibility(View.GONE);
                    checkBoxView.setVisibility(View.GONE);
                    switchView.setVisibility(View.GONE);
                    break;
            }

            switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    invokeOnItemClickMethod(data, new Boolean(isChecked));
                }
            });

            checkBoxView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    invokeOnItemClickMethod(data, new Boolean(isChecked));

                }
            });

            return convertView;
        }

    }

    public static class ItemEntity implements Serializable {

        // 该项目的UI风格
        private ItemStyle itemStyle;

        //子项的标题
        private String dataKey;

        //子项的值
        private String dataValue;

        //子项是否有下页图标
        private boolean optionValue;

        //子项的点击后所触发的方法名
        private String onClickMethodName;

        public ItemEntity(ItemStyle style, String key, String clickMethodName, String value, Boolean option) {
            this.itemStyle = style;
            this.dataKey = key;
            this.onClickMethodName = clickMethodName;
            this.optionValue = (option == null) ? false : option.booleanValue();
            this.dataValue = value;
        }

        public ItemEntity(String key, String clickMethodName, Boolean option) {
            this.itemStyle = ItemStyle.ITEM_STYLE_DEFAULT_SWITCH;
            this.dataKey = key;
            this.onClickMethodName = clickMethodName;
            this.optionValue = (option == null) ? false : option.booleanValue();
            this.dataValue = null;
        }

        public ItemEntity(String key, String clickMethodName) {
            this.itemStyle = ItemStyle.ITEM_STYLE_DEFAULT;
            this.dataKey = key;
            this.onClickMethodName = clickMethodName;
            this.optionValue = false;
            this.dataValue = null;
        }

        public ItemEntity(String key) {
            this.itemStyle = ItemStyle.ITEM_STYLE_SECTION;
            this.dataKey = key;
            this.onClickMethodName = null;
            this.optionValue = false;
            this.dataValue = null;
        }

        public ItemStyle getItemStyle() {
            return this.itemStyle;
        }

        public String getOnClickMethodName() {
            return this.onClickMethodName;
        }

        public String getDataKey() {
            return this.dataKey;
        }

        public String getDataValue() {
            return this.dataValue;
        }

        public boolean getOptionValue() {
            return this.optionValue;
        }
    }

}
