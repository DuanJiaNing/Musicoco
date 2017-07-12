// IOnSongChange.aidl
package com.duan.musicoco.aidl;
import com.duan.musicoco.aidl.Song;

// Declare any non-default types here with import statements

interface IOnPlayListChangedListener {

    void onPlayListChange(in Song current,int index,int id);
}
