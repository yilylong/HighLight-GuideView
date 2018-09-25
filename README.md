[![](https://jitpack.io/v/yilylong/UserGuideView.svg)](https://jitpack.io/#yilylong/UserGuideView)
# UserGuideView
用户指引view
====
应用推出新功能需要给给用户提示指引一下.传入需要指引的View即可

![](/guide1.png)</br>
![](/guide2.png)</br>
![](/guide3.png)</br>
<img src='/GIF.gif'/>

How To Useage
----
引入依赖

step1.Add it in your root build.gradle at the end of repositories:
-
    allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

Step 2. Add the dependency
-
    dependencies {
	        implementation 'com.github.yilylong:UserGuideView:1.0.5'
	}


布局文件中引入UserGuideView然后：

<del>guideView.setHighLightView(UserGuideTestActivity.this,convertView);</del>

    guideView.setHighLightView(targetView);
  
传入当前需要高亮的view即可 之前的方法持有一个activity的引用不太好  去掉了

支持高亮框形状 属性app:HighlightViewStyle="oval" 方形 圆形 椭圆 可选

提示的图片  属性 app:tipView="@mipmap/tip_view"

蒙版层颜色 属性 app：maskColor

高亮框边缘模糊效果 属性  app:MaskBlurStyle="solid" normal/solid

需要设置状态栏高度时候调用guideView.setStatusBarHeight(0)

v1.0.3新增
-
1.0.1可以支持批量设置高亮view 但漏了设置每个高亮view对应的tipview，1.0.3补上

    setHighLightView(LinkedHashMap<View,Integer> targetsWithTipViews);
    
实际应用中，tipview 和 箭头等设计图不一样，很难做到精准定位，所以增加了设置每个箭头和tipview位移的方法，来微调位置以达到最合适的布局。

    setArrowDownCenterMoveX(int jtDownCenterMoveX)
    setArrowUpRightMoveX(int jtUpRightMoveX)
    setArrowUpLeftMoveX(int jtUpLeftMoveX)
    setArrowUpRightMoveX(int jtUpRightMoveX)
    setArrowUpCenterMoveX(int jtUpCenterMoveX)
    setArrowDownRightMoveX(int jtDownRightMoveX)
    setArrowDownLeftMoveX(int jtDownLeftMoveX)
    setArrowDownCenterMoveX(int jtDownCenterMoveX)
    setTipViewMoveX(View highlightView,int tipViewMoveX)
    setTipViewMoveY(View highlightView,int tipViewMoveY)
    
等几个方法

v1.0.2新增
-
增加是否显示箭头的方法
    
v1.0.1新增
-

支持同时设置多个需要高亮的View并将按顺序显示

    guideView.setHightLightView(top,icon,back);

支持设置指示箭头

    guideView.setArrowUpCenter(R.mipmap.up_arrow);

支持将自定义View作为tipview

    guideView.setTipView(tipTextView,400,200);

详情参考Demo




