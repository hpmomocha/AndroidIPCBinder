package com.hpe.kevin.androidipcbinderclient;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.hpe.kevin.androidipcbinder.aidl.IBookManager;
import com.hpe.kevin.androidipcbinder.data.Book;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Binder";

    @BindView(R.id.bind_service)
    public Button bindService;

    @BindView(R.id.unbind_service)
    public Button unBindService;

    @BindView(R.id.addData)
    public Button addData;

    @BindView(R.id.getData)
    public Button getData;

    @BindView(R.id.txt)
    TextView tvInfo;

    @BindView(R.id.scrollView)
    ScrollView scrollView;

    /** 服务是否连接 */
    public boolean isBound = false;
    /** 远程service */
    public IBookManager bookManager = null;
    /** ScrollView里表示文字列 **/
    private SpannableStringBuilder stringBuilder = new SpannableStringBuilder();

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            showMessage("-----远程服务链接成功-----", R.color.praised_num);
            isBound = true;
            bookManager = IBookManager.Stub.asInterface(service);
            try {
                //设置死亡代理
                service.linkToDeath(mDeathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            bookManager = null;
            showMessage("-----远程服务已断开连接-----", R.color.red_deep);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        ButterKnife.bind(MainActivity.this);
        showMessage("-----远程服务等待连接-----", R.color.green_pure);
//        start();
    }

    @OnClick({R.id.bind_service, R.id.unbind_service, R.id.addData, R.id.getData})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bind_service:
                if (!isBound) {
                    //用Intent匹配的方式绑定service
                    doBindService();
                }
                break;
            case R.id.unbind_service:
                if (isBound && bookManager != null && serviceConnection != null) {
                    unbindService(serviceConnection);
                    isBound = false;
                    showMessage("-----主动断开远程连接-----", R.color.red_deep);
                }
                break;
            case R.id.addData:
                addData();
                break;
            case R.id.getData:
                getData();
                break;
            default:
                break;
        }
    }

    private void addData() {
        if (isBound) {
            // 添加书籍
            Book book = new Book();
            try {
                int size = bookManager.getBookList().size();
                int id = size + 1;
                book.setBookId(id);;
                book.setBookName("Book" + id);
                showMessage(book.toString(), R.color.blue_color);
                bookManager.addBook(book);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this,"服务还未连接！", Toast.LENGTH_SHORT).show();
        }
    }

    private void getData() {
        if (isBound) {
            try {
                int size = bookManager.getBookList().size();
                showMessage("Data size: "+ size, R.color.blue_color);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this,"服务还未连接！", Toast.LENGTH_SHORT).show();
        }
    }

    private void start() {
        Intent intent = new Intent("com.hpe.kevin.androidipcbinder.service.AIDL_SERVICE");
        intent.setPackage("com.hpe.kevin.androidipcbinder");
        if (serviceConnection == null) {
            serviceConnection = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    IBookManager bookManager = IBookManager.Stub.asInterface(service);
                    // 添加书籍
                    try {
                        bookManager.addBook(new Book(3, "book3"));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    try {
                        List<Book> bookList = bookManager.getBookList();
                        for (Book book : bookList) {
                            Log.e(TAG, "---->book:" + book.toString());
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {

                }
            };
        }
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showMessage(final String info, final int color) {
        tvInfo.post(new Runnable() {
            @Override
            public void run() {
                int startPos = stringBuilder.length();
                stringBuilder.append("\n"+info);
                // 设置颜色
                stringBuilder.setSpan(new ForegroundColorSpan(getResources().getColor(color)), startPos, stringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                tvInfo.setText(stringBuilder);
            }
        });
        tvInfo.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(null != scrollView) scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        },100);
    }

    private void doBindService() {
        showMessage("-----开始链接远程服务-----", R.color.light_yellow);
        Intent intent = new Intent("com.hpe.kevin.androidipcbinder.service.AIDL_SERVICE");
        intent.setPackage("com.hpe.kevin.androidipcbinder");
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    //客户端使用死亡代理，可以重启service
    //http://blog.csdn.net/liuyi1207164339/article/details/51706585
    //服务端使用死亡回调回收数据
    //http://www.cnblogs.com/punkisnotdead/p/5158016.html
    //死亡通知原理分析
    //http://light3moon.com/2015/01/28/Android%20Binder%20%E5%88%86%E6%9E%90%E2%80%94%E2%80%94%E6%AD%BB%E4%BA%A1%E9%80%9A%E7%9F%A5[DeathRecipient]/
    /**
     * 监听Binder是否死亡
     */
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            if (bookManager == null) {
                return;
            }
            //死亡后解除绑定
            bookManager.asBinder().unlinkToDeath(mDeathRecipient, 0);
            bookManager = null;
            //重新绑定
            doBindService();
            showMessage("--DeathRecipient后重链远程服务--", R.color.red_wine);
        }
    };
}
