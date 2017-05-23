// IPlayControl.aidl
package com.duan.musicoco.aidl;
import com.duan.musicoco.aidl.Song;
import com.duan.musicoco.aidl.IOnSongChangedListener;
// Declare any non-default types here with import statements

interface IPlayControl {

        //播放指定歌曲
       int play(in Song which);

       //上一首
       Song pre();

       //下一首
       Song next();

       //暂停
       int pause();

       //继续
       int resume();

       //当前播放歌曲
       Song currentSong();

       //播放状态
       int status();

       //设置播放列表，返回下一首播放歌曲
       Song setPlayList(in List<Song> songs);

       //获得播放列表
       List<Song> getPlayList();

       //注册播放曲目改变时回调
       void registerOnSongChangedListener(IOnSongChangedListener li);

       //取消注册播放曲目改变时回调
       void unregisterOnSongCHangedListener(IOnSongChangedListener li);

       //设置播放模式 0 列表播放（默认，播放到列表最后时停止播放），1 单曲循环，2列表循环，3 随机播放
       void setPlayMode(int mode);

       //获得当前播放曲目进度
       int getProgress();

       //定位到指定位置
       int seekTo(int pos);

}

