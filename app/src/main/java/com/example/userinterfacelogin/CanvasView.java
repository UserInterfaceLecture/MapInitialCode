package com.example.userinterfacelogin;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
//선 두께 바꿀수 있게하기, 지우개 지원하기

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class CanvasView extends View {
    private List<Pair<Path, Paint>> paths; // 그림 경로와 해당 경로의 설정을 저장하는 리스트
    private Bitmap bitmap;
    private Canvas drawCanvas;
    private float lineWidth = 5; // 초기 선 두께를 5로 설정
    private int currentColor = Color.BLACK; // 초기 색상을 검정으로 설정
    private boolean eraseMode = false; // 지우개 모드 설정

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paths = new ArrayList<>();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Bitmap 및 Canvas 초기화
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(bitmap);
        drawCanvas.drawColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 그리기 작업을 Canvas에 그립니다.
        for (Pair<Path, Paint> pathPair : paths) {
            canvas.drawPath(pathPair.first, pathPair.second);
        }
        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 그리기 작업 시작
                startDrawing(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                // 그리기 작업 진행
                continueDrawing(x, y);
                break;
            case MotionEvent.ACTION_UP:
                // 그리기 작업 종료
                stopDrawing();
                break;
        }
        // View를 다시 그립니다.
        invalidate();
        return true;
    }

    public void startDrawing(float x, float y) {
        Path path = new Path();
        Paint paint = new Paint();

        paint.setColor(eraseMode ? Color.WHITE : currentColor); // 현재 색상 또는 지우개 모드 설정
        paint.setStyle(Paint.Style.STROKE); // 그리기 스타일: 선
        paint.setStrokeWidth(lineWidth); // 현재 선 두께 설정

        path.moveTo(x, y);
        drawCanvas.drawPath(path, paint);

        paths.add(new Pair<>(path, paint));
    }

    public void continueDrawing(float x, float y) {
        Paint paint = new Paint();

        paint.setColor(eraseMode ? Color.WHITE : currentColor); // 현재 색상 또는 지우개 모드 설정
        paint.setStyle(Paint.Style.STROKE); // 그리기 스타일: 선
        paint.setStrokeWidth(lineWidth); // 현재 선 두께 설정

        if (!paths.isEmpty()) {
            Pair<Path, Paint> lastPathPair = paths.get(paths.size() - 1);
            Path path = lastPathPair.first;
            path.lineTo(x, y);
            drawCanvas.drawPath(path, paint);

        }
    }

    public void stopDrawing() {
        // 그리기 작업 종료
    }

    public Bitmap getCanvasBitmap() {
        return bitmap;
    }

    public void setLineWidth(float width) {
        lineWidth = width;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setCurrentColor(int color) {
        currentColor = color;
    }

    public int getCurrentColor() {
        return currentColor;
    }

    public void setEraseMode(boolean erase) {
        eraseMode = erase;
    }

    public boolean isEraseMode() {
        return eraseMode;
    }

    public void clearCanvas() {
        paths.clear(); // 경로 리스트를 초기화하여 화면을 지웁니다.
        bitmap.eraseColor(Color.WHITE); // 비트맵을 흰색으로 지우기

        invalidate(); // 뷰 다시 그리기
    }
}
