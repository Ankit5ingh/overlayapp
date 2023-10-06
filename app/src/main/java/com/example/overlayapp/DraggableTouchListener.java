package com.example.overlayapp;

import android.content.Context;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import java.util.Timer;
import java.util.TimerTask;

public class DraggableTouchListener implements View.OnTouchListener {

    private final View view;
    private final Point initialPosition;
    CallBackListener callBackListener;

    private int touchSlop;
    private long longClickInterval;
    private int pointerStartX = 0;
    private int pointerStartY = 0;
    private int initialX = 0;
    private int initialY = 0;
    private boolean moving = false;
    private boolean longClickPerformed = false;
    private Timer timer = null;

    public DraggableTouchListener(Context context, View view, Point initialPosition, CallBackListener callBackListener) {
        this.view = view;
        this.initialPosition = initialPosition;
        this.callBackListener = callBackListener;

        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        longClickInterval = ViewConfiguration.getLongPressTimeout();

        view.setOnTouchListener(this);
    }
    public void setInitialPosition(int x, int y) {

    }
    private void scheduleLongClickTimer() {
        if (timer == null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!moving && !longClickPerformed) {
                        view.post(view::performLongClick);
                        longClickPerformed = true;
                    }
                    cancelLongClickTimer();
                }
            }, longClickInterval);
        }
    }

    private void cancelLongClickTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent motionEvent) {
        double timerStart = 0;
        double timerEnd = 0;

        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                timerStart = System.currentTimeMillis();
                pointerStartX = (int) motionEvent.getRawX();
                pointerStartY = (int) motionEvent.getRawY();
                initialX = initialPosition.x;
                initialY = initialPosition.y;
                moving = false;
                longClickPerformed = false;
                scheduleLongClickTimer();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!longClickPerformed) {
                    float deltaX = motionEvent.getRawX() - pointerStartX;
                    float deltaY = motionEvent.getRawY() - pointerStartY;
                    if (moving || Math.hypot(deltaX, deltaY) > touchSlop) {
                        cancelLongClickTimer();
                        callBackListener.onPositionChanged((int) (initialX + deltaX), (int) (initialY + deltaY));
                        moving = true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                timerEnd = System.currentTimeMillis();
                if(timerEnd - timerStart < 400){
                    view.performClick();
                }
                cancelLongClickTimer();
                if (!moving && !longClickPerformed) {
                    view.performClick();
                }
                break;
        }
        return true;
    }



}
