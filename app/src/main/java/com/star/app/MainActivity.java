package com.star.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mIP, mNum;
    private Button mPing;
    private TextView mContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mIP = findViewById(R.id.main_ip);
        mNum = findViewById(R.id.main_num);
        mPing = findViewById(R.id.main_ping);
        mContent = findViewById(R.id.main_content);
        mContent.setMovementMethod(ScrollingMovementMethod.getInstance());
        mPing.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (null == mIP.getText() || 0 == mIP.getText().toString().trim().length()) {
            Toast.makeText(this, "IP啊 ，，大哥", Toast.LENGTH_SHORT).show();
            return;
        }
        if (null == mNum.getText() || 0 == mNum.getText().toString().trim().length()) {
            Toast.makeText(this, "请求次数啊 ，，大哥", Toast.LENGTH_SHORT).show();
            return;
        }
        setScheduler();
    }

    /**
     * 线程设置
     */
    private void setScheduler() {
        Observable
                .create(new ObservableOnSubscribe<String>() { //定义被观察者
                    @Override
                    public void subscribe(final ObservableEmitter<String> emitter) throws Exception {
                        //进行网络请求
                        ping(emitter);
                    }
                })
                .subscribeOn(Schedulers.io())  //被观察者
                .observeOn(AndroidSchedulers.mainThread()) //观察者在主线程中实现
                .subscribe(new Observer<String>() {        //观察者
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String value) {
                        mContent.setText("##########ping结果:::\n" + value);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    /**
     * 请求ping方法
     *
     * @param emitter
     */
    private void ping(Emitter emitter) {
        String line = null;
        Process process = null;
        BufferedReader bufferedReader = null;
        StringBuffer stringBuffer = new StringBuffer();
        String command = "ping -c " + mNum.getText().toString().trim() + " -W 10 " + mIP.getText().toString().trim();
        try {
            process = Runtime.getRuntime().exec(command);
            if (null == process) {
                Toast.makeText(this, "一点ping不通", Toast.LENGTH_SHORT).show();
            } else {
                int status = process.waitFor();
                append(stringBuffer, "response status == " + status + "\n");
                bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                while (null != (line = bufferedReader.readLine())) {
                    append(stringBuffer, line);
                }
            }

//            if (0 == status) {
//                append(stringBuffer, "response status == " + status + "\n");
//            } else {
//                append(stringBuffer, "response status == " + status + "\n");
//                bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//                while (null != (line = bufferedReader.readLine())) {
//                    append(stringBuffer, line);
//                }
//            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (null != process)
                process.destroy();
            if (null != bufferedReader)
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            emitter.onNext(stringBuffer.toString());
        }
    }

    private static void append(StringBuffer stringBuffer, String text) {
        if (stringBuffer != null) {
            stringBuffer.append(text + "\n");
        }
    }
}
