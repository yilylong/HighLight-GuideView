# UserGuideView
用户指引view
====
应用推出新功能需要给给用户提示指引一下.传入需要指引的View即可

![](/guide1.png)</br>
![](/guide2.png)</br>
![](/guide3.png)

How To Useage
----
引入依赖
step1.Add it in your root build.gradle at the end of repositories:

    allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

Step 2. Add the dependency

    dependencies {
	        compile 'com.github.yilylong:UserGuideView:v1.0.0'
	}


布局文件中引入UserGuideView然后：

<del>guideView.setHighLightView(UserGuideTestActivity.this,convertView);</del>

    guideView.setHighLightView(targetView);
  
传入当前需要高亮的view即可 之前的方法持有一个activity的引用不太好  去掉了

支持高亮框形状 属性app:HighlightViewStyle="oval" 方形 圆形 椭圆 可选

提示的图片  属性 app:tipView="@mipmap/tip_view"

蒙版层颜色 属性 app：maskColor

高亮框边缘模糊效果 属性  app:MaskBlurStyle="solid" normal/solid

默认去掉了状态栏高度 <del>当主题设置了android:windowTranslucentStatus = true 需要设置状态栏高度为0
guideView.setStatusBarHeight(0);</del> 修改了状态栏高度的获取方式不需要再调用这个方法。

详情参考Demo




