package com.example.overlayapp;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import static android.content.Context.WINDOW_SERVICE;

public class Window {

    private final Context context;
    public final View mView;
    public WindowManager.LayoutParams mParams;
    private final WindowManager mWindowManager;

    public Window(Context context, int layout){
        this.context=context;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // set the layout parameters of the window
            mParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                    // Display it on top of other application windows
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    // Don't let it grab the input focus
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    // Make the underlying application window visible
                    // through any transparent parts
                    PixelFormat.TRANSLUCENT);
        }

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = layoutInflater.inflate(layout, null);

        mParams.gravity = Gravity.CENTER;
        mWindowManager = (WindowManager)context.getSystemService(WINDOW_SERVICE);
    }

    public void setPosition(int x, int y) {
        mParams.x = x;
        mParams.y = y;
        update();
    }

    private void update() {
        try {
            mWindowManager.updateViewLayout(mView, mParams);
        } catch (Exception e) {
            Log.e("Update Function", e.toString());
        }
    }

    public WindowManager.LayoutParams getParams() {return mParams;}
    public View getView() {return mView;}

    public void open() {
        try {
            if(mView.getWindowToken()==null) {
                if(mView.getParent()==null) mWindowManager.addView(mView, mParams);
            }
        } catch (Exception e) {
            Log.d("Error1",e.toString());
        }

    }

    public void close() {
        try {
            ((WindowManager)context.getSystemService(WINDOW_SERVICE)).removeView(mView);
            mView.invalidate();
            ((ViewGroup)mView.getParent()).removeAllViews();
        } catch (Exception e) {
            Log.d("Error2",e.toString());
        }
    }
}

