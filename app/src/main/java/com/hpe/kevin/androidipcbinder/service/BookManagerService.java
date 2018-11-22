package com.hpe.kevin.androidipcbinder.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.hpe.kevin.androidipcbinder.aidl.IBookManager;
import com.hpe.kevin.androidipcbinder.data.Book;

import java.util.ArrayList;
import java.util.List;

public class BookManagerService extends Service {
    private static final String TAG = "Binder";
    private ArrayList<Book> mBookList = new ArrayList<Book>();
    private Binder mBinder = new IBookManager.Stub() {
        @Override
        public List<Book> getBookList() throws RemoteException {
            return mBookList;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            mBookList.add(book);
        }
    };

    public BookManagerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "---->远程服务启动");
        mBookList.add(new Book(1, "book1"));
        mBookList.add(new Book(2, "book2"));
    }
}
