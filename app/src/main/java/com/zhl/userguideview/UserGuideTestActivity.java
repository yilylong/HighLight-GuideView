package com.zhl.userguideview;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.zhl.cbpullrefresh.CBPullRefreshListView;
import com.zhl.userguideview.userguideview.R;

/**
 * 描述：
 * Created by zhaohl on 2015-11-26.
 */
public class UserGuideTestActivity extends Activity {
    private String[] datas = new String[]{"收藏","字体大小","软件设置","换肤"};
    GridView mGridView;
    private UserGuideView guideView;
    private ImageView icon ,back,top;
    private CBPullRefreshListView listView;
    View tipTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_userguide);
        guideView = (UserGuideView) findViewById(R.id.guideView);
        guideView.setTouchOutsideDismiss(false);
        tipTextView = LayoutInflater.from(this).inflate(R.layout.custom_tipview,null);
//        icon = (ImageView) findViewById(R.id.icon);
//        guideView.setHighLightView(icon);
//        guideView.setTipView(tipTextView);
        mGridView = (GridView) findViewById(R.id.gridview);
        mGridView.setAdapter(new MyAaapter());
        icon = (ImageView) findViewById(R.id.icon);
        back = (ImageView) findViewById(R.id.back);
        top = (ImageView) findViewById(R.id.top);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guideView.setTipView(tipTextView,400,200);
                guideView.setHighLightView(icon);
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guideView.setTipView(tipTextView,400,200);
                guideView.setHighLightView(back);
            }
        });
        top.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guideView.setArrowUpCenter(R.mipmap.up_arrow);
                guideView.setTipView(R.mipmap.tip_view);
                guideView.setHighLightView(top);
            }
        });
        guideView.setHightLightView(top,icon,back);
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
//            if(position==1){
////                guideView.setTipView(BitmapFactory.decodeResource(getResources(),R.mipmap.sidebar_photo));
//                guideView.setHighLightView(convertView);
//            }
           return convertView;
        }

        private class ViewHolder{
           public TextView textView;
        }
    }
}
