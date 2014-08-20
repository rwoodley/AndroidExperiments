package rwoodley.org.colorfiltercam;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import java.io.IOException;


public class MainActivity extends Activity implements ColorPickerDialog.OnColorChangedListener {
    private static final String    TAG                 = "MainActivity";

    private int HUE_LIMIT1 = 1;
    private int HUE_LIMIT2 = 359;
    private int _color1 = Color.HSVToColor(new float[]{HUE_LIMIT1, 1f, 1f});
    private int _color2 = Color.HSVToColor( new float[]{ HUE_LIMIT2, 1f, 1f });;

    private byte[] _pictureData;
    private Object processorLocker = new Object();
    private Object cameraLocker = new Object();
    private Camera mCamera;

    private SurfaceView _surfaceView;
    ImageView _postProcessedImg;
    private Handler _processImageHandler;
    private Bitmap _postProcessedBmp;
    private boolean _firstTime = true;  // first time this instance.
    private boolean _backgroundThreadShouldRun = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            new Thread(binarizeImage).start();
            _processImageHandler = new Handler();

            ImageView postProcessedImg = (ImageView) findViewById(R.id.processedImage);
            ViewTreeObserver vto = postProcessedImg.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (_firstTime) {
                        _postProcessedImg = (ImageView) findViewById(R.id.processedImage);
                        initBitmap();
                    }
                    _firstTime = false;
                }
            });
        } catch (Exception exception) {
            Log.e(TAG, "Error in onCreate()", exception);
        }
    }
    private void openCamera() {
        synchronized (cameraLocker) {
            Log.w(TAG, "openCamera() called.");
            try {
                if (mCamera == null) {
                    _surfaceView = new SurfaceView(this);
                    mCamera = Camera.open();
                    mCamera.setPreviewDisplay(_surfaceView.getHolder());
                }
            } catch (Exception exception) {
                Log.e(TAG, "Error creating camera", exception);
                if (mCamera == null) return;
                mCamera.release();
                mCamera = null;
                return;
            }
        }
    }
    private void initBitmap() {
        Bitmap postProcessedBmp = Bitmap.createBitmap(_postProcessedImg.getWidth(), _postProcessedImg.getHeight(), Config.RGB_565);
        Log.w(TAG, "******in initBitmap(),w = " + _postProcessedImg.getWidth() + ", h = " + _postProcessedImg.getHeight());
        for(int i = 0; i < postProcessedBmp.getHeight(); i++){
            for(int j = 0; j < postProcessedBmp.getWidth(); j++){
                int pixel = postProcessedBmp.getPixel(j, i);
                if(i*j%5==0){
                    postProcessedBmp.setPixel(j, i, Color.WHITE);
                }else{
                    postProcessedBmp.setPixel(j, i, Color.BLACK);
                }
            }
        }
        _postProcessedImg.setImageBitmap(postProcessedBmp);
        Log.w(TAG, "******in initBitmap(),w = " + _postProcessedImg.getWidth() + ", h = " + _postProcessedImg.getHeight());
    }
    public void onStop() {
        super.onStop();  // Always call the superclass method first
        Log.w(TAG, "---onStop() called");

        _backgroundThreadShouldRun = false;
    }
    public void onPause() {
        super.onPause();  // Always call the superclass method first
        Log.w(TAG, "---onPause() called");

        synchronized (cameraLocker) {
            mCamera.release();
            mCamera = null;
        }
    }
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        Log.w(TAG, "---onResume() called");
        openCamera();
    }
    public void onDestroy() {
        super.onDestroy();  // Always call the superclass method first
        Log.w(TAG, "---onDestroy() called");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
        if (item.getTitle().equals("Choose Color")) {
            new ColorPickerDialog(this, this, "BLAH", _color1, _color2)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void colorChanged(String key, int color1, int color2) {
        Log.i(TAG, "Key = " + key + ", Color = " + color1 + "," + color2);
        float[] hsv = new float[3];

        Color.RGBToHSV(Color.red(color1), Color.green(color1), Color.blue(color1), hsv);
        HUE_LIMIT1 = (int)hsv[0];

        Color.RGBToHSV(Color.red(color2), Color.green(color2), Color.blue(color2), hsv);
        HUE_LIMIT2 = (int)hsv[0];
        _color1 = color1;
        _color2 = color2;
        if (HUE_LIMIT1 < HUE_LIMIT2) {
            int temp = HUE_LIMIT1;
            HUE_LIMIT1 = HUE_LIMIT2;
            HUE_LIMIT2 = temp;
            temp = _color1;
            _color1 = _color2;
            _color2 = temp;
        }
        Log.w("Color chooser", _color1 + "," + _color2);
    }
    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
//            Log.w("mPictureCallback", "got pic,data = " + data);
            synchronized (processorLocker) {
                _pictureData = data;
//                Log.w("mPictureCallback", "_pictureData = " + _pictureData);
            }
        }
    };
    public ImageView getPostProcessedImg() {
        //Log.w(TAG, "******in getPostProcessedImg(),w = " + _postProcessedImg.getWidth() + ", h = " + _postProcessedImg.getHeight());
        return _postProcessedImg;
    }
    private Runnable postProcessedBinaryImage = new Runnable() {
        @Override
        public void run() {
            if (_backgroundThreadShouldRun) {
//                Log.w(TAG, "******setting new bitmap(), w = " + _postProcessedBmp.getWidth() + ", h = " + _postProcessedBmp.getHeight());
//                Log.w(TAG, "******setting new bitmap(), w = " + getPostProcessedImg().getWidth() + ", h = " + getPostProcessedImg().getHeight());
                getPostProcessedImg().setImageBitmap(_postProcessedBmp);
            }
        }
    };
    private void takePic() {
        boolean success = false;
        int nTries = 0;
        while (!success) {
            try {
                synchronized (cameraLocker) {
                    mCamera.startPreview();
                    mCamera.takePicture(null, null, mPictureCallback);
                    success = true;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                nTries++;
                if (nTries > 20) System.exit(0);    // yes, I know.
            }
        }
    }
    private Runnable binarizeImage = new Runnable() {

        @Override
        public void run() {
            boolean firstTime = true;

            BitmapFactory.Options opts = new BitmapFactory.Options();

            while(_backgroundThreadShouldRun){

//                Log.w("binarizeImage", "_pictureData = " + _pictureData);
                if (mCamera == null) {
//                    Log.e(TAG, "=====null camera, waiting 500ms=====");
                    try { Thread.sleep(500);} catch (InterruptedException e) { e.printStackTrace(); }
                    continue;
                }

                try {
                    if (firstTime) { // take a pic to kick the whole thing off.
                        firstTime = false;
                        takePic();
//                        Log.w(TAG, "==== sleep");
                        try { Thread.sleep(1000);} catch (InterruptedException e) { e.printStackTrace(); }
//                        Log.w(TAG, "==== wake");
                        continue;
                    }
                }
                catch(Exception e){e.printStackTrace();}

                Bitmap preProcessedBmp;
                if (_pictureData == null) {
//                    Log.w(TAG, "==== no picture data, pause and retry");
                    try { Thread.sleep(300);} catch (InterruptedException e) { e.printStackTrace(); }
                    continue;
                }

                synchronized (processorLocker) {

//                    Log.w(TAG, "==== got picture data");
                    preProcessedBmp = BitmapFactory.decodeByteArray(_pictureData, 0, _pictureData.length, opts);
                    _pictureData = null;
                }
                try{
                    //The size of this bmp must equals the one on the setPictureSize...
                    Bitmap postProcessedBmp = Bitmap.createBitmap(preProcessedBmp.getWidth(), preProcessedBmp.getHeight(), Config.RGB_565);
//                    Log.w(TAG, "==== begin process, w = " + preProcessedBmp.getWidth() + ", h = " + preProcessedBmp.getHeight());

                    //outer_loop:
                    for(int i = 0; i < preProcessedBmp.getHeight(); i++){
                        for(int j = 0; j < preProcessedBmp.getWidth(); j++){
                            int pixel = preProcessedBmp.getPixel(j, i);
                            float[] hsv = new float[3];
                            Color.RGBToHSV(Color.red(pixel), Color.green(pixel), Color.blue(pixel), hsv);
                            int hue = (int)hsv[0];
                            if(hue <= HUE_LIMIT1  && hue >= HUE_LIMIT2 ){
                                postProcessedBmp.setPixel(j, i, pixel);
                            }else{
                                postProcessedBmp.setPixel(j, i, Color.BLACK);
                            }
                        }
                    }
                    if(MainActivity.this._postProcessedBmp != null){
                        MainActivity.this._postProcessedBmp.recycle();
                    }
                    MainActivity.this._postProcessedBmp = postProcessedBmp;

                    //Get the regions out of the image
                    //regionDetector.startProcessingImg(postProcessedBmp);
//                    Log.w(TAG, "==== end process");

                    _processImageHandler.post(postProcessedBinaryImage);
                }
                catch(Exception e){e.printStackTrace();}

                if(mCamera == null) {
//                    Log.e(TAG, "=====null camera=====");
                    break;
                }
                takePic();
            }
            Log.w(TAG, "=====background thread exited=====");
        }
    };
}
