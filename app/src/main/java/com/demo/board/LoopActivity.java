package com.demo.board;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;

public class LoopActivity extends AppCompatActivity {
    LoopScaleView mLsv1;
    LoopScaleView mLsv2;
    LoopScaleView mLsv3;
    LoopScaleView mLsv4;
    TextView mTvValue1;
    TextView mTvValue2;
    TextView mTvValue3;
    TextView mTvValue4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loop);
        mLsv1 = findViewById(R.id.lsv_1);
        mLsv2 = findViewById(R.id.lsv_2);
        mLsv3 = findViewById(R.id.lsv_3);
        mLsv4 = findViewById(R.id.lsv_4);
        mTvValue1 = findViewById(R.id.tv_value1);
        mTvValue2 = findViewById(R.id.tv_value2);
        mTvValue3 = findViewById(R.id.tv_value3);
        mTvValue4 = findViewById(R.id.tv_value4);
        mLsv1.setOnValueChangeListener(new ValueChangeListener(1));
        mLsv2.setOnValueChangeListener(new ValueChangeListener(2));
        mLsv3.setOnValueChangeListener(new ValueChangeListener(3));
        mLsv4.setOnValueChangeListener(new ValueChangeListener(4));
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10000; i++) {
                    try {
                        Thread.sleep(1_000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    final int finalI = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("aaa",finalI+1+"");
                            mLsv1.setCurrentValue(finalI*180 + 1);
                        }
                    });
                }
            }
        }).start();
    }

    class ValueChangeListener implements LoopScaleView.OnValueChangeListener {
        private int type;

        public ValueChangeListener(int type) {
            this.type = type;
        }

        @Override
        public void OnValueChange(int newValue) {
            switch (type) {
                case 1:
                    mTvValue1.setText("身高 " + newValue + " cm");
                    break;
                case 2:
                    mTvValue2.setText("身高 " + newValue + " cm");
                    break;
                case 3:
                    mTvValue3.setText("重量 " + newValue + " g");
                    break;
                case 4:
                    mTvValue4.setText("长度 " + newValue + " cm");
                    break;
            }
        }
    }
}
