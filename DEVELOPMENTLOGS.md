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