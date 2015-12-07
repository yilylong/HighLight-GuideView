package com.zhl.userguideview;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import com.zhl.userguideview.userguideview.R;

/**
 * 描述：
 * Created by zhaohl on 2015-11-26.
 */
public class UserGuideTestActivity extends Activity {
    private String[] datas = new String[]{"收藏","字体大小","软件设置","夜间模式"};
    GridView mGridView;
    private UserGuideView guideView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_userguide);
        guideView = (UserGuideView) findViewById(R.id.guideView);
        mGridView = (GridView) findViewById(R.id.gridview);
        mGridView.setAdapter(new MyAaapter());
    }

    private class MyAaapter extends BaseAdapter {

        @Override
        public int getCount() {
            return datas.length;
        }

        @Override
        public Object getItem(int position) {
            return datas[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
           if(convertView==null){
               viewHolder = new ViewHolder();
               convertView = LayoutInflater.from(UserGuideTestActivity.this).inflate(R.layout.grid_item,parent,false);
               viewHolder.textView = (TextView) convertView.findViewById(R.id.tx);
               convertView.setTag(viewHolder);
           }else{
               viewHolder = (ViewHolder) convertView.getTag();
           }
            viewHolder.textView.setText(datas[position]);
            if(position==3){
//                guideView.setTipView(BitmapFactory.decodeResource(getResources(),R.mipmap.sidebar_photo));
                // 当前主题设置了android:windowTranslucentStatus = true 需要设置状态栏高度为0
                guideView.setStatusBarHeight(0);
                guideView.setHighLightView(convertView);
            }
           return convertView;
        }

        private class ViewHolder{
           public TextView textView;
        }
    }
}
