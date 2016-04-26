# SSJ

	1.MainTabActivity.java是继承自TabHostActivity，TabHostActivity是继承  自TabActivity的抽象类，用来实现多个页面的切换。
TabActivity的Layout文件必须包含<FrameLayout>和 <TabWidget>这两个布  局，每一个进行切换的activity放在framelayout中。
必须为TabActivity的布局文件根节点设置为TabHost。
TabItems采用List存放

	2.SplashScreenActivity是数据库操作类
