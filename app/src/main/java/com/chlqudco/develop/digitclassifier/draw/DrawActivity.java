package com.chlqudco.develop.digitclassifier.draw;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.chlqudco.develop.digitclassifier.R;
import com.divyanshu.draw.widget.DrawView;

import java.io.IOException;
import java.util.Locale;

public class DrawActivity extends AppCompatActivity {
    Classifier cls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        //그림 그리는 곳 초기화
        DrawView drawView = findViewById(R.id.drawView);
        //선의 굵기
        drawView.setStrokeWidth(100.0f);
        drawView.setBackgroundColor(Color.BLACK);
        drawView.setColor(Color.WHITE);

        TextView resultView = findViewById(R.id.resultView);

        Button classifyBtn = findViewById(R.id.classifyBtn);
        //분석 버튼 클릭시
        classifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //이미지 부터 꺼내오기
                Bitmap image = drawView.getBitmap();

                //분석 고고씽
                Pair<Integer, Float> res = cls.classify(image);

                //결과에 집어넣어
                String outStr = String.format(Locale.ENGLISH, "%d, %.0f%%", res.first, res.second * 100.0f);
                resultView.setText(outStr);
            }
        });

        //지우기는 지우기
        Button clearBtn = findViewById(R.id.clearBtn);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawView.clearCanvas();
            }
        });

        //모델 생성 및 초기화
        cls = new Classifier(this);
        try {
            cls.init();
        } catch (IOException e){
            Log.d("DigitClassifier", "failed to init Classifier", e);
        }
    }

    @Override
    protected void onDestroy() {
        cls.finish();
        super.onDestroy();
    }
}