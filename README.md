# AnnularMenuView
环状扇形菜单

[![](https://jitpack.io/v/eknow314/AnnularMenuView.svg)](https://jitpack.io/#eknow314/AnnularMenuView)

![image](https://cdn.jsdelivr.net/gh/eknow314/blog_pic/img/202204061735760.jpg)

### 依赖配置

```groovy
allprojects {
    repositories {
        maven { url 'https://www.jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.eknow314:AnnularMenuView:+'
}
```

---

### 基本使用

布局代码
```
<com.eknow.annularmenu.AnnularMenuView
            android:id="@+id/annularMenuView"
            android:layout_width="180dp"
            android:layout_height="180dp"
            android:layout_marginBottom="16dp"
            app:amv_menuDeviationAngle="90"
            app:amv_menuMargin="6dp"
            app:amv_menuNormalBgColor="#138DAF"
            app:amv_menuNormalBgGradientColor0="#138DAF"
            app:amv_menuNormalBgGradientColor1="#065FB3"
            app:amv_menuNum="three"
            app:amv_menuPressedBgColor="#003F77"
            app:amv_menuStrokeColor="#73C4FF"
            app:amv_menuStrokeSize="2dp"
            app:amv_radiusRatio="3"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintEnd_toEndOf="parent"
            app:layout_constraintStart_toStartOf="parent" />
```
如果需要设置按钮图标
``` java
annularMenuView.setMenuDrawable(0, ContextCompat.getDrawable(this, R.drawable.ic_rotate_left))
```

菜单单击事件监听
``` java
annularMenuView.setOnMenuClickListener(object : OnMenuClickListener {
            override fun OnMenuClick(position: Int) {
                Log.e(TAG, "onClick position: $position")
            }
        })
```

菜单长按事件监听
``` java
annularMenuView.setOnMenuLongClickListener(object : OnMenuLongClickListener {
            override fun OnMenuLongClick(position: Int) {
                Log.e(TAG, "onLongClick position: $position")
            }
        })
```

菜单触摸事件监听
``` java
annularMenuView.setOnMenuTouchListener(object : OnMenuTouchListener {
            override fun OnTouch(event: MotionEvent?, position: Int) {
                Log.e(TAG, "onTouch position: $position  event: ${event?.action}")
            }
        })
```

---

### 全部属性

| 属性 | 功能 | 默认值 |
| --- | --- | --- |
| amv_menuNum | 菜单数量，默认3 | three |
| amv_menuDeviationAngle | 菜单顺时针旋转偏移角度 | 0 |
| amv_menuStrokeSize | 边框大小 | 1dp |
| amv_menuStrokeColor | 边框颜色 | #73C4FF |
| amv_menuMargin | 按钮边距 | 10dp |
| amv_radiusRatio | 外圆半径和内圆半径比例 | 3 |
| amv_menuPressedBgColor | 菜单按钮按压状态颜色 | #003F77 |
| amv_menuNormalBgColor | 菜单按钮普通状态颜色 | #138DAF |
| amv_menuNormalBgGradientColor0 | 菜单按钮普通状态渐变色0 | Color.TRANSPARENT |
| amv_menuNormalBgGradientColor1 | 菜单按钮普通状态渐变色1 | Color.TRANSPARENT |
