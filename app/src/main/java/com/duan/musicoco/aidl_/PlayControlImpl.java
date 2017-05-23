package com.duan.musicoco.aidl_;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

import com.duan.musicoco.aidl.Song;

import java.util.List;

/**
 * Created by DuanJiaNing on 2017/5/23.
 * 手写的 aidl 自动生成的 .java 文件
 * 类中的方法运行在 Binder 线程池中，需要自己处理线程同步
 */

public class PlayControlImpl extends Binder implements IPlayControl {

    /**
     * 未知错误
     */
    public static final int ERROR_UNKNOWN = -1;

    /**
     * 歌曲文件解码错误
     */
    public static final int ERROR_DECODE = 0x1;

    /**
     * 正在播放
     */
    public static final int STATUS_PLAYING = 0x2;

    /**
     * 播放结束
     */
    public static final int STATUS_END = 0x3;

    /**
     * 播放暂停
     */
    public static final int STATUS_PAUSE = 0x4;


    public PlayControlImpl() {
        this.attachInterface(this, DESCRIPTOR);
    }

    public static IPlayControl asInterface(IBinder obj) {
        if (obj == null)
            return null;
        IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
        if ((iin != null) && (iin instanceof IPlayControl))
            return (IPlayControl) iin;

        return new Proxy(obj);
    }

    @Override
    protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case TRANSACTION_play: {
                data.enforceInterface(DESCRIPTOR);
                Song which = null;
                int result = ERROR_UNKNOWN;
                if (0 != data.readInt()) {
                    which = Song.CREATOR.createFromParcel(data);
                    result = this.play(which);
                    reply.writeNoException();
                    reply.writeInt(result);
                }
                return true;
            }
            case TRANSACTION_pause: {
                data.enforceInterface(DESCRIPTOR);
                int result = this.pause();
                reply.writeNoException();
                reply.writeInt(result);

                return true;
            }
            case TRANSACTION_resume: {
                data.enforceInterface(DESCRIPTOR);
                int result = this.resume();
                reply.writeNoException();
                reply.writeInt(result);

                return true;
            }
            case TRANSACTION_currentSong: {
                data.enforceInterface(DESCRIPTOR);
                Song result = this.currentSong();
                reply.writeNoException();
                result.writeToParcel(reply, 0);
                return true;
            }
            case TRANSACTION_status: {
                data.enforceInterface(DESCRIPTOR);
                int result = this.status();
                reply.writeNoException();
                reply.writeInt(result);
                return true;
            }
            case TRANSACTION_setPlayList: {
                data.enforceInterface(DESCRIPTOR);
                List<Song> songs;
                Song result = null;
                if (0 != data.readInt()) {
                    songs = data.createTypedArrayList(Song.CREATOR);
                    result = this.setPlayList(songs);
                }
                reply.writeNoException();
                result.writeToParcel(reply, 0);
                return true;
            }
            case TRANSACTION_getPlayList: {
                data.enforceInterface(DESCRIPTOR);
                List<Song> result = this.getPlayList();
                reply.writeNoException();
                reply.writeTypedList(result);
                return true;
            }
        }

        return super.onTransact(code, data, reply, flags);
    }

    @Override
    public IBinder asBinder() {
        return this;
    }

    private static class Proxy implements IPlayControl {

        private IBinder mRemote;

        public Proxy(IBinder obj) {
            this.mRemote = obj;
        }

        public String getInterfaceDescriptor() {
            return DESCRIPTOR;
        }


        @Override
        public int play(Song which) throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            int result = ERROR_UNKNOWN;
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                if (which != null) {
                    data.writeInt(1);
                    which.writeToParcel(data, 0);
                } else
                    data.writeInt(0);

                mRemote.transact(TRANSACTION_play, data, reply, 0);
                reply.readException();
                result = reply.readInt();
            } finally {
                data.recycle();
                reply.recycle();
            }
            return result;
        }

        @Override
        public int pause() throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            int result = ERROR_UNKNOWN;
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                mRemote.transact(TRANSACTION_pause, data, reply, 0);
                reply.readException();
                result = reply.readInt();

            } finally {
                data.recycle();
                reply.recycle();
            }
            return result;
        }

        @Override
        public int resume() throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            int result = ERROR_UNKNOWN;
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                mRemote.transact(TRANSACTION_resume, data, reply, 0);
                reply.readException();
                result = reply.readInt();

            } finally {
                data.recycle();
                reply.recycle();
            }
            return result;
        }

        @Override
        public Song currentSong() throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            Song result = null;
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                mRemote.transact(TRANSACTION_currentSong, data, reply, 0);
                reply.readException();
                result = result.CREATOR.createFromParcel(reply);

            } finally {
                data.recycle();
                reply.recycle();
            }
            return result;
        }

        @Override
        public int status() throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            int result = -1;
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                mRemote.transact(TRANSACTION_status, data, reply, 0);
                reply.readException();
                result = reply.readInt();
            } finally {
                data.recycle();
                reply.recycle();
            }
            return result;
        }

        @Override
        public Song setPlayList(List<Song> songs) throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            Song result = null;
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                if (songs != null) {
                    data.writeInt(1);
                    data.writeTypedList(songs);
                } else {
                    data.writeInt(0);
                }

                mRemote.transact(TRANSACTION_setPlayList, data, reply, 0);
                reply.readException();
                result = result.CREATOR.createFromParcel(reply);
            } finally {
                data.recycle();
                reply.recycle();
            }
            return result;
        }

        @Override
        public List<Song> getPlayList() throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            List<Song> result = null;
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                mRemote.transact(TRANSACTION_getPlayList, data, reply, 0);
                reply.readException();
                result = reply.createTypedArrayList(Song.CREATOR);
            } finally {
                data.recycle();
                reply.recycle();
            }
            return result;
        }

        @Override
        public IBinder asBinder() {
            return mRemote;
        }
    }

    @Override
    public int play(Song which) {
        return ERROR_UNKNOWN;
    }

    @Override
    public int pause() {
        return ERROR_UNKNOWN;
    }

    @Override
    public int resume() {
        return ERROR_UNKNOWN;
    }

    @Override
    public Song currentSong() {
        return null;
    }

    @Override
    public int status() {
        return -1;
    }

    @Override
    public Song setPlayList(List<Song> songs) {
        return null;
    }

    @Override
    public List<Song> getPlayList() {
        return null;
    }

}
