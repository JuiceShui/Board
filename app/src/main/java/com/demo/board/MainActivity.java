package com.demo.board;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText etSpHead, etHp, etVp, etSpFoot, etSpBoard;
    private AppCompatButton btnSp, btnHp, btnVp;
    private SouthPointer sp;
    private HorizontalPointer hp;
    private VerticalPointer vp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etSpHead = findViewById(R.id.et_sp_head);
        etSpFoot = findViewById(R.id.et_sp_foot);
        etSpBoard = findViewById(R.id.et_sp_board);
        etHp = findViewById(R.id.et_hp);
        etVp = findViewById(R.id.et_vp);
        btnSp = findViewById(R.id.btn_sp);
        btnHp = findViewById(R.id.btn_hp);
        btnVp = findViewById(R.id.btn_vp);
        btnVp.setOnClickListener(this);
        btnSp.setOnClickListener(this);
        btnHp.setOnClickListener(this);
        hp = findViewById(R.id.hp);
        sp = findViewById(R.id.sp);
        vp = findViewById(R.id.vp);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sp:
                float headDegree = Float.parseFloat(etSpHead.getText().toString());
                float footDegree = Float.parseFloat(etSpFoot.getText().toString());
                float boardDegree = Float.parseFloat(etSpBoard.getText().toString());
                sp.changeDegree(headDegree, footDegree, boardDegree);
                break;
            case R.id.btn_hp:
                float degree = Float.parseFloat(etHp.getText().toString());
                hp.changeDegree(degree);
                break;
            case R.id.btn_vp:
                float vDegree = Float.parseFloat(etVp.getText().toString());
                vp.changeDegree(vDegree);
                break;
        }
    }
}
