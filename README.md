# UserGuideView
用户指引view
====
应用推出新功能需要给给用户的提示指引一下，简单的做法让设计给个覆盖层图片盖上去。
但是android 分辨率众多，不是每个机子都适应。需要很多图片。那是个很麻烦的过程
使用过@鸿洋 的 hightLight。但是发现在使用到需要给一个gridview 或者 listview 的某个item 设置一个高亮提示的时候。因为hightLight需要
传入一个需要高亮的view ID 但是gridview 或 listview 的item 里面ID 没法定位某一个view。
所以改成 传入一个需要高亮的view 根据其位置绘出提示view

![](https://raw.githubusercontent.com/yilylong/ImageResource/master/user_guide_view.png)

How To Useage
----
布局文件中引入UserGuideView然后：

    guideView.setHighLightView(UserGuideTestActivity.this,convertView);

传入当前需要高亮view 所在Activity 和 需要高亮的view即可

支持高亮框形状 属性app:HighlightViewStyle="oval" 方形 圆形 椭圆 可选

提示的图片  属性 app:tipView="@mipmap/tip_view"

蒙版层颜色 属性 app：maskColor

默认去掉了状态栏高度 当主题设置了android:windowTranslucentStatus = true 需要设置状态栏高度为0
guideView.setStatusBarHeight(0);

详情参考Demo




