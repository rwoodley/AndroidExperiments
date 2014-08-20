package rwoodley.org.colorfiltercam;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

// based on: http://www.yougli.net/android/a-photoshop-like-color-picker-for-your-android-application/

public class ColorPickerDialog extends Dialog {
    public interface OnColorChangedListener {
        void colorChanged(String key, int color1, int color2);
    }

    private OnColorChangedListener mListener;
    private int mColor1, mColor2;
    private String mKey;

    private static class ColorPickerView extends View {
        private Paint mPaint;
        private float mCurrentHue = 0;
        private int mCurrentHueAsColor = Color.RED;
        private int mCurrentX = 0, mCurrentY = 0;
        private int mColor1, mColor2;
        private final int[] mHueBarColors = new int[258];
        private int[] mMainColors = new int[65536];
        private OnColorChangedListener mListener;

        ColorPickerView(Context c, OnColorChangedListener l, int color1, int color2) {
            super(c);
            mListener = l;

            mColor1 = color1;
            mColor2 = color2;

            float[] hsv = new float[3];
            Color.colorToHSV(color1, hsv);
            mCurrentHue = hsv[0];
            mCurrentHueAsColor = color1;

            // Initialize the colors of the hue slider bar
            int index = 0;
            for (float i=0; i<256; i += 256/42) // Red (#f00) to pink (#f0f)
            {
                mHueBarColors[index] = Color.rgb(255, 0, (int) i);
                index++;
            }
            for (float i=0; i<256; i += 256/42) // Pink (#f0f) to blue (#00f)
            {
                mHueBarColors[index] = Color.rgb(255-(int) i, 0, 255);
                index++;
            }
            for (float i=0; i<256; i += 256/42) // Blue (#00f) to light blue (#0ff)
            {
                mHueBarColors[index] = Color.rgb(0, (int) i, 255);
                index++;
            }
            for (float i=0; i<256; i += 256/42) // Light blue (#0ff) to green (#0f0)
            {
                mHueBarColors[index] = Color.rgb(0, 255, 255-(int) i);
                index++;
            }
            for (float i=0; i<256; i += 256/42) // Green (#0f0) to yellow (#ff0)
            {
                mHueBarColors[index] = Color.rgb((int) i, 255, 0);
                index++;
            }
            for (float i=0; i<256; i += 256/42) // Yellow (#ff0) to red (#f00)
            {
                mHueBarColors[index] = Color.rgb(255, 255-(int) i, 0);
                index++;
            }

            // Initializes the Paint that will draw the View
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setTextAlign(Paint.Align.CENTER);
            mPaint.setTextSize(12);
        }

        // Get the current selected color from the hue bar
        private int getCurrentMainColor()
        {
            int translatedHue = 255-(int)(mCurrentHue*255/360);
            int index = 0;
            for (float i=0; i<256; i += 256/42)
            {
                if (index == translatedHue)
                    return Color.rgb(255, 0, (int) i);
                index++;
            }
            for (float i=0; i<256; i += 256/42)
            {
                if (index == translatedHue)
                    return Color.rgb(255-(int) i, 0, 255);
                index++;
            }
            for (float i=0; i<256; i += 256/42)
            {
                if (index == translatedHue)
                    return Color.rgb(0, (int) i, 255);
                index++;
            }
            for (float i=0; i<256; i += 256/42)
            {
                if (index == translatedHue)
                    return Color.rgb(0, 255, 255-(int) i);
                index++;
            }
            for (float i=0; i<256; i += 256/42)
            {
                if (index == translatedHue)
                    return Color.rgb((int) i, 255, 0);
                index++;
            }
            for (float i=0; i<256; i += 256/42)
            {
                if (index == translatedHue)
                    return Color.rgb(255, 255-(int) i, 0);
                index++;
            }
            return Color.RED;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int translatedHue = 255-(int)(mCurrentHue*255/360);
            // Display all the colors of the hue bar with lines
            for (int x=0; x<256; x++)
            {
                // If this is not the current selected hue, display the actual color
                if (translatedHue != x)
                {
                    mPaint.setColor(mHueBarColors[x]);
                    mPaint.setStrokeWidth(1);
                }
                else // else display a slightly larger black line
                {
                    mPaint.setColor(Color.BLACK);
                    mPaint.setStrokeWidth(3);
                }
                canvas.drawLine(x+10, 0, x+10, 40, mPaint);
            }

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mColor1);
            canvas.drawRect(10, 50, 138, 90, mPaint);
            mPaint.setColor(Color.WHITE);
            canvas.drawText(getHSVString(mColor1), 70, 70, mPaint);

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mColor2);
            canvas.drawRect(138, 50, 266, 90, mPaint);
            mPaint.setColor(Color.WHITE);
            canvas.drawText(getHSVString(mColor2), 190, 70, mPaint);

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(Color.BLACK);
            canvas.drawRect(10, 100, 266, 150, mPaint);
            mPaint.setColor(Color.WHITE);
            canvas.drawText("GO!", 125, 130, mPaint);
        }
        private String getHSVString(int color) {
            float[] hsv = new float[3];
            Color.colorToHSV(color, hsv);
            return Integer.toString((int) hsv[0]);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            setMeasuredDimension(276, 366);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() != MotionEvent.ACTION_DOWN) return true;
            float x = event.getX();
            float y = event.getY();

            // If the touch event is located in the hue bar
            if (x > 10 && x < 266 && y > 0 && y < 40)
            {
                mCurrentHue = (255-x)*360/255;
                mCurrentHueAsColor = Color.HSVToColor( new float[]{ mCurrentHue, 1f, 1f });
                invalidate();
            }

            if (x > 10 && x < 138 && y > 50 && y < 90) {
                mColor1 = mCurrentHueAsColor;
//                mListener.colorChanged("", mColor2, mColor2);
                // Force the redraw of the dialog
                invalidate();
            }

            if (x > 138 && x < 266 && y > 50 && y < 90) {
                mColor2 = mCurrentHueAsColor;
//                mListener.colorChanged("", mColor2, mColor2);
                // Force the redraw of the dialog
                invalidate();
            }
            if (x > 10 && x < 266 && y > 100 && y < 150) {
                mListener.colorChanged("", mColor1, mColor2);
            }

            return true;
        }
    }

    public ColorPickerDialog(Context context, OnColorChangedListener listener, String key, int color1, int color2) {
        super(context);

        mListener = listener;
        mKey = key;
        mColor1 = color1;
        mColor2 = color2;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OnColorChangedListener l = new OnColorChangedListener() {
            public void colorChanged(String key, int color1, int color2) {
                mListener.colorChanged("", color1, color2);
                dismiss();
            }
        };

        setContentView(new ColorPickerView(getContext(), l, mColor1, mColor2));
        setTitle("Color Chooser");
    }
}
