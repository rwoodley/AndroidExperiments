package rwoodley.org.colorfiltercam;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import java.io.IOException;


public class MainActivity extends Activity implements ColorPickerDialog.OnColorChangedListener {
    private static final String    TAG                 = "MainActivity";

    private static int HUE_LIMIT1 = 359;
    private static int HUE_LIMIT2 = 1;
    private static int _color1 = Color.HSVToColor(new float[]{HUE_LIMIT1, 1f, 1f});
    private static int _color2 = Color.HSVToColor( new float[]{ HUE_LIMIT2, 1f, 1f });;

    private byte[] _pictureData;
    private Object processorLocker = new Object();
    private Object cameraLocker = new Object();
    private Camera mCamera;

    private SurfaceView _surfaceView;
    ImageView _imageView = null;
    private Handler _processImageHandler;
    private Bitmap _postProcessedBmp;
    private boolean _firstTime = true;  // first time this instance.
    private boolean _backgroundThreadShouldRun = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            Log.e("onCreate", "Density is " + metrics.density);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            new Thread(binarizeImage).start();
            _processImageHandler = new Handler();

            initializeCamera();
            
            ImageView imageView = (ImageView) findViewById(R.id.processedImage);
            Log.w("onCreate", "imageView WxH = " + imageView.getWidth() + "," + imageView.getHeight());
            ViewTreeObserver vto = imageView.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (_firstTime) {
                        _imageView = (ImageView) findViewById(R.id.processedImage);
                        Log.w("onCreate", "_imageView WxH = " + _imageView.getWidth() + "," + _imageView.getHeight());
                        initBitmap();
                    }
                    _firstTime = false;
                }
            });
        } catch (Exception exception) {
            Log.e(TAG, "Error in onCreate()", exception);
        }
    }
    private void initializeCamera() {
        synchronized (cameraLocker) {
            Log.w(TAG, "initializeCamera() called.");
            try {
                if (mCamera == null) {
                    _surfaceView = (SurfaceView) findViewById(R.id.cameraSurface);
                    SurfaceHolder.Callback sh_callback = my_callback();
                    _surfaceView.getHolder().addCallback(sh_callback);
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
    SurfaceHolder.Callback my_callback() {
        SurfaceHolder.Callback ob1 = new SurfaceHolder.Callback() {

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.w("_surfaceView.getHolder()", "-----surfaceDestroyed");
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.w("_surfaceView.getHolder()", "-----surfaceCreated");
                try {
                    mCamera = Camera.open();
                    mCamera.setPreviewDisplay(holder);
                } catch (IOException exception) {
                    mCamera.release();
                    mCamera = null;
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                       int height) {
                Log.w("_surfaceView.getHolder()", "-----surfaceChanged");
                mCamera.startPreview();
                mCamera.takePicture(null, null, mPictureCallback);
            }
        };
        return ob1;
    }
    private void initBitmap() {
        Log.w("initBitmap 1", "_imageView WxH = " + _imageView.getWidth() + "," + _imageView.getHeight());
        Bitmap postProcessedBmp = Bitmap.createBitmap(_imageView.getWidth(), _imageView.getHeight(), Config.RGB_565);
        Log.w(TAG, "******in initBitmap(),w = " + _imageView.getWidth() + ", h = " + _imageView.getHeight());
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
        _imageView.setImageBitmap(postProcessedBmp);
        Log.w("initBitmap 2", "_imageView WxH = " + _imageView.getWidth() + "," + _imageView.getHeight());
        Log.w(TAG, "******in initBitmap(),w = " + _imageView.getWidth() + ", h = " + _imageView.getHeight());
    }
    public void onStop() {
        super.onStop();  // Always call the superclass method first
        Log.w(TAG, "---onStop() called");

        _backgroundThreadShouldRun = false;
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
            if (!_backgroundThreadShouldRun) return;
            synchronized (processorLocker) {
                _pictureData = data;
                mCamera.startPreview();
            }
        }
    };
    public ImageView getimageView() {
        return _imageView;
    }
    private Runnable postProcessedBinaryImage = new Runnable() {
        @Override
        public void run() {
            if (_backgroundThreadShouldRun) {
                Log.w(TAG, "******setting new bitmap(), w = " + getimageView().getWidth() + ", h = " + getimageView().getHeight());
                getimageView().setImageBitmap(_postProcessedBmp);
            }
        }
    };
    private void takePic() {
        if (!_backgroundThreadShouldRun) return;
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
                if (nTries > 5) break; // give up, maybe this class is being killed due to a rotate.
            }
        }
    }
    private int getRotation() {
        int rotation = _imageView.getDisplay().getRotation();
        String mess = "";
        if (rotation == Surface.ROTATION_0)
            mess = "Surface.ROTATION_0";
        else if (rotation == Surface.ROTATION_90)
            mess = "Surface.ROTATION_90";
        else if (rotation == Surface.ROTATION_180)
            mess = "Surface.ROTATION_180";
        else if (rotation == Surface.ROTATION_270)
            mess = "Surface.ROTATION_270";
        Log.w("getRotation()", mess);
        return rotation;
    }
    private Runnable binarizeImage = new Runnable() {

        @Override
        public void run() {
            boolean firstTime = true;

            while(_backgroundThreadShouldRun){

                if (mCamera == null) {
                    Log.e(TAG, "=====null camera, waiting 500ms=====");
                    try { Thread.sleep(500);} catch (InterruptedException e) { e.printStackTrace(); }
                    continue;
                }

                if (_pictureData == null) {
                    Log.w(TAG, "==== no picture data, pause and retry");
                    try { Thread.sleep(300);} catch (InterruptedException e) { e.printStackTrace(); }
                    continue;
                }

                Bitmap cameraBmp;
                synchronized (processorLocker) {

                    Log.w(TAG, "==== got picture data");
                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inJustDecodeBounds = true;
                    BitmapFactory.decodeByteArray(_pictureData, 0, _pictureData.length, opts);
                    Log.w("binarizeImage", opts.outWidth + ", " + opts.outHeight);
                    opts.inSampleSize = opts.outWidth/_imageView.getWidth();
                    opts.inJustDecodeBounds = false;
                    Log.w(TAG, "inSampleSize = " + opts.inSampleSize);
                    cameraBmp = BitmapFactory.decodeByteArray(_pictureData, 0, _pictureData.length, opts);
                    Log.w(TAG, "==== begin process, w = " + cameraBmp.getWidth() + ", h = " + cameraBmp.getHeight());

                    _pictureData = null;
                }
                try{
                    //The size of this bmp must equals the one on the setPictureSize...
                    // But note, we're transposing the axes to rotate....
                    Log.w("bg thread", "_imageView WxH = " + _imageView.getWidth() + "," + _imageView.getHeight());

                    int rotation = getRotation();
                    Bitmap postProcessedBmp =
                        (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) ?
                        Bitmap.createBitmap(cameraBmp.getHeight(), cameraBmp.getWidth(), Config.RGB_565) :
                        Bitmap.createBitmap(cameraBmp.getWidth(), cameraBmp.getHeight(), Config.RGB_565);

                    //outer_loop:
                    int[] pixels = new int[cameraBmp.getWidth() * cameraBmp.getHeight()];
                    cameraBmp.getPixels(pixels, 0, cameraBmp.getWidth(), 0, 0, cameraBmp.getWidth(), cameraBmp.getHeight());
                    for(int i = 0; i < cameraBmp.getHeight(); i++){
                        for(int j = 0; j < cameraBmp.getWidth(); j++){
                            if (!_backgroundThreadShouldRun) return;    // should do some sort of thread interrupt probably.

                            //int pixel = cameraBmp.getPixel(j, i);
                            int pixel = pixels[i*cameraBmp.getWidth()+j];
                            float[] hsv = new float[3];
                            Color.RGBToHSV(Color.red(pixel), Color.green(pixel), Color.blue(pixel), hsv);
                            int hue = (int)hsv[0];

                            int ii = postProcessedBmp.getWidth() - i - 1;
                            int jj = j;

                            if (rotation == Surface.ROTATION_90) {
                                ii = j; jj = i;
                            }
                            else
                            if (rotation == Surface.ROTATION_180) {
                                ii = i;
                                jj = postProcessedBmp.getHeight() - j - 1;
                            }
                            else
                            if (rotation == Surface.ROTATION_270) {
                                ii = postProcessedBmp.getWidth() - j - 1;
                                jj = postProcessedBmp.getHeight() - i - 1;
                            }

                            if(hue <= HUE_LIMIT1  && hue >= HUE_LIMIT2 ){
                                postProcessedBmp.setPixel(ii, jj, pixel);
                            }else{
                                postProcessedBmp.setPixel(ii, jj, Color.BLACK);
                            }
                        }
                    }
                    if (!_backgroundThreadShouldRun) return;    // should do some sort of thread interrupt probably.
                    if(MainActivity.this._postProcessedBmp != null){
                        MainActivity.this._postProcessedBmp.recycle();
                    }
                    Log.w("binarizeImage", "res " + postProcessedBmp.getWidth() + "," + postProcessedBmp.getHeight());
                    MainActivity.this._postProcessedBmp = postProcessedBmp;

                    Log.w(TAG, "==== end process");

                    _processImageHandler.post(postProcessedBinaryImage);
                }
                catch(Exception e){e.printStackTrace();}

                if(mCamera == null) {
                    break;  // in other words, the thread will died. Phone must be rotated to restart it.
                }
                takePic();
            }
            Log.w(TAG, "=====background thread exited=====");
        }
    };
}
