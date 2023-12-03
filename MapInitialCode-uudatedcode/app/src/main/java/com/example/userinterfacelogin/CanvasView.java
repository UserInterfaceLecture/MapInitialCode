package com.example.userinterfacelogin;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
//선 두께 바꿀수 있게하기, 지우개 지원하기

public class CanvasView extends View {
    private Paint paint;
    private Path path;
    private Bitmap bitmap;
    private Canvas drawCanvas;

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 그리기 설정 초기화
        paint = new Paint();
        paint.setColor(Color.BLACK); // 그리기 색상
        paint.setStyle(Paint.Style.STROKE); // 그리기 스타일: 선
        paint.setStrokeWidth(5); // 선의 두께
        path = new Path();
        path.reset();
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
        path.moveTo(x, y);
    }

    public void continueDrawing(float x, float y) {
        path.lineTo(x, y);
        drawCanvas.drawPath(path, paint);
    }

    public void stopDrawing() {
        // 그리기 작업 종료
    }

    public Bitmap getCanvasBitmap() {
        return bitmap;
    }
}


