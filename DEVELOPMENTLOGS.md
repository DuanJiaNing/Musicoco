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
