------

- 2017-05-24 16:44

昨天的忘了记录，今天开始记录。<br>
昨天是开发第一天，主要完成了如下任务：<br>
1 完成 aidl 中三个类的编写，并在 java 文件夹中实现了 IPlayControl.Stub 接口（PlayControlImpl.java）<br>
2 完成歌曲播放逻辑控制的核心类（PlayManager.java），以及 java/../service 包下的其他两个类

今天的:<br>
1 完成歌曲信息实体类（SongInfo.java）<br>
2 完成部分歌曲信息获取类(MediaManager.java)<br>
3 实现 IOnSongChangeListener.Stub 接口（OnSongChangeListener.java）<br>
并检测通过。

- 2017-05-25

1 完成 PermissionManager.java ，对权限申请进行判断<br>
2 搭好了 PlayActivity 的 MVP 架构<br>
3 从 PlayActivity 中抽离出 PlayServiceConnection 和 PlayServiceManager<br>

- 2017-05-29

1 增加服务绑定权限验证<br>
2 增加 OnPlayStatusChangeListener aidl 文件及对应抽象类

- 2017-05-30

1 添加 getAudioSessionId 方法<br>
2 添加三个 fragment 及其对应包<br>
3 完成 Visualizer 部分功能定义<br>
4 完善各模块的 mvp 架构<br>

- 2017-05-30

1 完成 AlbumVisualizerSurfaceView 的部分功能<br>
 截取图片为圆形，获取图片中的颜色 etc<br>

- 2017-06-01

1 完成 LyricFragment 和 ListFragment 的显示和隐藏动画<br>

- 2017-06-03

1 添加 PlayPreference 类<br>
2 完成 PlayActivity 从隐藏到显示时 VisualizerFragment 的状态恢复<br>

- 2017-06-08

1 将 AlbumVisualizer 应用到 AlbumVisualizerSurfaceView<br>

- 2017-06-09

1 添加 BitmapCache 缓存类<br>
2 添加 BitmapUtils 中的 jpgTopng<br>

- 2017-06-12

- 在 AlbumVisualizerSurfaceView 中移除 AlbumVisualizer 以及 Gummy 部分<br>
- 处理设备中没有歌曲文件时的情况<br>

- 2017-06-12

1 修改播放界面整体 UI <br>
2 添加 AlbumPicture 和 PictureBuilder 类<br>
3 修改 BitmapUtils 中的 getCircleBitmap 方法 <br>

- 2017-06-14

1 完善 AlbumPicture 和 PictureBuilder<br>
2 添加 SkipView <br>

- 2017-06-19

1 添加 PlayPreference <br>
2 完善 AlbumPicture<br>
3 添加 MediaView PlayView SkipView 到项目<br>
4 实现 Theme default mode 随歌曲切换变换颜色（控件颜色，字体颜色）<br>

- 2017-06-21

1 添加 DiscreteSeekBar 替换 SeekBar<br>

- 2017-06-21

1 添加 logo ，默认专辑图片，修复专辑图片旋转逻辑<br>

- 2017-06-22

1 修改 logo ，修改默认专辑图片<br>
2 添加闪屏图片<br>
3 添加 RealtimeBlurView 到 activity <br>
4 实现播放列表的显示和隐藏<br>
5 播放模式的切换<br>

- 2017-06-23

1 播放列表添加 adapter<br>
2 aidl IPlayControl 添加 remove 方法<br>

- 2017-06-24

1 添加对 白色 和 黑金 主题的支持<br>

- 2017-06-26

1 播放列表显示时使背景变暗<br>

- 2017-06-26

1 使播放列表可以隐藏和只显示头部<br>

- 2017-07-01

1 添加 DBHelper DBMusicocoController <br>
2 完善 BottomNavigation<br>

- 2017-07-03

1 aidl IPlayControl 中添加 getPlayMode 方法<br>
2 修改 AlbumPictureController 的构造方法，传入图片直径，避免 getWidth 方法获取到 0 的一系列麻烦。<br>

- 2017-07-04

1 添加 OnThemeChange OnViewVisibilityChange 接口<br>
2 修改 PlayListAdapter ，使其从 PlayActivity 中分离出，以进行重用<br>
3 将播放界面的播放列表从 PlayActivity 中抽离，提取出 PlayListController 类<br>

- 2017-07-07

1 主界面底部播放导航可以正确弹出播放列表<br>
2 主界面和播放界面的播放曲目和播放状态可以同步<br>

- 2017-07-08

1 在 style 中添加完成夜间主题和日间主题<br>

- 2017-07-09

1 添加 RecentMostPlayController ,封装对应的布局及其处理逻辑<br>

- 2017-07-11

1 添加 ImageTextView<br>
2 添加 MainSheetController，并将代码补全，完成主要的三个歌单（我的收藏，最近播放，全部歌曲）的操作入口<br>

- 2017-07-12

1 完善播放界面底部导航<br>
2 添加收藏功能的入口，完善主界面【我的收藏】歌单信息<br>
3 添加 IOnDataIsReadyListener 接口，服务器数据初始化完成时回调<br>
4 添加 IOnPlayListChangedListener 接口，用于在播放列表改变时回调<br>

- 2017-07-12

1 添加 MySheetsAdapter 和 MySheetsController<br>
2 数据库歌单表添加 歌曲数 字段，添加对应方法<br>

- 2017-07-15

1 添加 rxJava ，重构 MainActivity 中使用到的 OnContentUpdate 接口<br>
2 添加 OnUpdateStatusChanged 接口和 SubscriberAbstract 抽象类<br>

- 2017-07-16

1 修改 MySheets 部分的 ui <br>
2 添加 OptionsDialog <br>
3 添加 PullDownListHelper <br>

- 2017-07-17

1 添加 DialogUtils PullDownLinearLayout 和 PullDownViewListenerHelper，但是不能用<br>
2 PlayActivity 添加歌曲选项(SongOptions)中的显示更多，完成收藏歌曲到歌单功能<br>

- 2017-07-18

1 添加 OptionsAdapter，BroadcastManager<br>
2 向歌曲选项中添加从歌单中移除和彻底删除并完成功能<br>
3 歌曲选项列表随主题变化

- 2017-07-19

1 添加 ActivityManager 管理 Activity 启动<br>
2 歌曲选项的查看详情改用单独的 Activity(SongDetailActivity)展示<br>
3 歌曲选项的添加到歌单改用 DialogManager，不使用 AlertDialog<br>
4 歌曲详情 Activity 可以调用系统图片查看功能查看图片，可以将图片保存到Download 目录下<br>
5 播放页点击名字可直接查看详情<br>

- 2017-07-20

1 将 SongInfo 和 Sheet 从 DBMusicocoControler 中抽离<br>
2 添加 MainSheethelper<br>
3 使用广播同步创建歌单的播放状态，即歌单的切换<br>
4 使服务器在移除歌曲时也回调 onPlayListChanged<br>
5 添加 SongController，抽离对歌曲的操作<br>
6 修复歌曲移除时 UI 同步问题<br>

- 2017-07-21

1 添加 MySheetOperation <br>
2 向 DBMusicocoController 中添加 updateSheet 方法<br>
3 修改 OptionsAdapter ，可以通过添加 Option 的方式增加列表项<br>
4 修改 TextInputHelper，添加错误信息闪动提示<br>
5 从 Github 添加 RotateLoading 到项目，作为进度条<br>
6 android studio 的 lint 提示 call new method on old api ，设置回<br>
7 修复 MainSheetHelper 获取数据时未实时更新的问题<br>
8 为 PlayList 的 item 添加 编号，主界面的播放列表显示的时候滚动到当前曲目<br>

- 2017-07-22

1 播放列表切换逻辑完善

- 2017-07-23

1 将 Manager 都改为单例模式<br>
2 添加歌单详情 SheetDetaiilActivity <br>

- 2017-07-24

1 歌单详情页顶部歌单信息展示开发完成<br>
2 App 类添加静态获得 Context 的方法<br>

- 2017-07-25

1 歌单详情页可随机播放所有歌曲<br>

- 2017-07-26

1 歌单详情页可以展示歌曲<br>
2 修正主页面【我的歌单】列表 item 背景色未响应主题变更<br>

- 2017-07-27

1 歌单详情页歌曲选项可以点击查看并执行<br>

- 2017-07-28

1 MainActivity 和 SheetDetailActivity 的 toolbar 可以随主题变更颜色<br>
2 主页底部播放进度添加背景<br>

- 2017-07-29

1 歌单详情页可以收藏所有歌曲到【我的收藏】<br>
2 歌单详情页歌曲点击可以播放，收藏所有歌单到【我的收藏】时通知 MainActivity 更新 MainSheetController 更新<br>

- 2017-07-30

1 歌单修改和添加歌单使用单独的 Activity 处理：SheetModifyActivity <br>
2 修复歌单修改/添加时描述字数超限却仍可以正常保存的错误<br>
3 添加播放界面背景【虚化专辑图片】和【暗色描边遮罩】两种模式<br>

- 2017-07-30

1 播放界面的背景虚化和遮罩可以与应用主题独立进行配置<br>
2 将 PlayActivity 中的颜色控制和背景变换部分抽离，使用单独的类控制<br>

- 2017-08-01

1 修复歌单详情歌曲列表正在播放图标位置错乱问题<br>
2 歌单详情页添加歌曲的多项选中功能<br>
3 多项选中可以在变换时使用动画，视图其它部分的变化也能同步<br>

- 2017-08-02

1 Toolbar 上的按钮能跟随歌曲多项操作变换<br>
2 修复 SongAdapter 动画使用时机错乱的问题<br>
3 修改歌单列表歌曲操作-收藏-图标<br>

- 2017-08-02

1 修改歌单详情-歌曲列表-歌曲编号左右间距<br>
2 在当前歌单删除另一歌单正在播放的同一首歌时服务器同步删除的问题 SongOperation#checkIsPlaying<br>
3 服务器切换 下一首/上一首 时返回值数组越界问题，在歌单中移除仅剩的最后一首歌时触发<br>

- 2017-08-03

1 完成多首歌曲添加到歌单功能<br>
2 完成多首已收藏歌曲取消收藏<br>
3 完成多首未收藏歌曲添加到收藏<br>
4 多首歌曲移除歌单功能<br>

- 2017-08-05

1 完成多首歌曲 彻底删除<br>

- 2017-08-05

1 添加搜索功能，SearchActivity，可以进行全部歌曲搜索和歌单内歌曲搜索<br>
2 搜索到的歌曲可以播放，查看详细信息<br>

- 2017-08-07

1 搜索到的歌曲可以快速在歌单中定位<br>
2 添加 历史最多播放 RecentMostPlayActivity<br>

- 2017-08-08

1 添加 RecentMostPlayActivity 和 RMPActivity 并完成功能<br>

- 2017-08-09

1 修改 MainActivity 界面，改用 CollapsingToolbarLayout <br>
2 可以定制状态栏和标题栏颜色以及'明显'(accentColor)文字和控件颜色<br>
3 为 PlayBackgroundModeEnum 添加 GRADIENT_COLOR （渐变色）模式<br>

- 2017-08-10

1 添加 FlowingDrawer ，LeftNavigationController<br>
2 移除 LeftNavigationController，改回 NavigationView<br>
3 BitmapProducer 添加 getKaleidoscope 方法，用于生成“万花筒”图片<br>

- 2017-08-11

1 照片墙可定制，数量，虚化度，透明度<br>
2 完成主页面左侧导航界面<br>

- 2017-08-12

1 将 MainActivity 中两个多余的广播接收器移除(contentUpdateReceiver & MainSheetChangeReceiver)<br>
2 白天和夜间主题切换<br>
3 修改两个 BottomNavigationController，解耦<br>
4 修复修改后的错误<br>

- 2017-08-14

1 播放界面的主题同步修复，主页面歌单删除广播同步<br>
2 应用退出功能实现，用广播通知服务器退出<br>

- 2017-08-16

1 添加 ThemeColorCustomActivity 并完成功能，可以动态设置标题栏颜色和图标颜色<br>

- 2017-08-17

1 添加 TimeSleepActivity 并完成界面设计和界面逻辑<br>
2 添加 AuxiliaryPreference，完成 TimeSleepActivity 逻辑<br>
3 添加 PlayThemeCustomActivity，完成播放界面风格定制逻辑<br>

- 2017-08-18

1 添加 ImageWallActivity 和 ImageAdapter 并完成功能<br>
2 主页左部导航添加【用户指南】<br>
3 添加 SettingsActivity 和 SettingFragment <br>

- 2017-08-18

1 添加 AudioFocusManager 处理音乐焦点问题<br>
2 添加 MediaSessionManager <br>
3 为 FeedBackActivity 更新 Layout<br>

- 2017-08-18

1 完成 FeedBackActivity 的逻辑<br>
2 更新 logo <br>

- 2017-08-22

1 添加 PlayNotifyManager，完成通知栏控制播放<br>

- 2017-08-23

1 完成 PlayNotifyManager 逻辑<br>
2 添加 SplashActivity，完成闪屏页动画<br>

- 2017-08-24

1 添加 WebActivity ，可以查看简叔上的【Musicoco 用户指南】<br>

- 2017-08-28

1 添加全球化资源【英语】，并完成校对<br>

- 2017-09-04

1 添加小米应用统计<br>
2 修复【自动】切换到夜间模式时间判断不准确的错误<br>
3 修改对话框动画<br>