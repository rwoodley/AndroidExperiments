package org.woodley.aigrid.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ImageAdapter extends BaseAdapter {
    private static final String    TAG                 = "ImageAdapter  ";
    private MainActivity _Context;
    AlertDialog _alertDialog;

    // constants
    int _nRows = 5;
    int _nCols = 8; // this is also in the layout xml.
    int _level = 3; // numbers go from 0 to _level - 1
    boolean _okToClick = false;

    boolean _dontHide;  // used only for _milliseconds == -2 ('you control it' mode).
    int _gameId = 0;    // use to deactivate obsolete timer events if user keeps hitting replay
    int _milliseconds = 250;    // chimp mode.
    int _currentlyDisplaying;
    ArrayList<Integer> _positions;
    ArrayList<Integer> _numerals;
    List<ImageView> _imageViews;
    private Handler _handler = new Handler();
    int _lastClick;

    public ImageAdapter(MainActivity con) {
        _Context = con;
        _alertDialog = new AlertDialog.Builder(_Context).create();
        _alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                _alertDialog.cancel();
            }
        });
        //alertDialog.setIcon(R.drawable.icon);
    }
    public void newGame() {
        _dontHide = false;
        _handler.removeCallbacks(hideNumbers);
        _positions = new ArrayList<Integer>();
        _numerals = new ArrayList<Integer>();
        _okToClick = false;
        _currentlyDisplaying = 0;
        _lastClick = -1;
        _imageViews = new ArrayList<ImageView>(_level);

        _imageViews.clear();
        HashSet<Integer> tempHS = new HashSet<Integer>();
        for (int i = 0; i < _level; i++) {
            _imageViews.add(i, null);
            Integer pos = -1;
            while (pos < 0 || _positions.contains(pos)) {
                pos = new Integer((int) (Math.random() * _nRows*_nCols));
            }
            _positions.add(pos);    // 0 thru 63

            pos = -1;
            while (pos < 0 || tempHS.contains(pos)) {
                pos = new Integer((int) (Math.random() * _level));
            }
            tempHS.add(pos);
            _numerals.add(pos);     // 0 thru 9
        }
        if (_milliseconds > 0)
            _handler.postDelayed(hideNumbers, _milliseconds);
        else
            _okToClick = true;
    }
    public void setMode(int ms) {
        _milliseconds = ms;
    }
    public void setLevel(int newLevel) {
        _level = newLevel;
        if (newLevel > 10) _level = 10;
        if (newLevel < 3) _level = 3;

        _Context.setTitle("You are at Level " + _level + ".");
    }
    private Runnable hideNumbers = new Runnable() {
        @Override
        public void run() {
            for (int position = 0; position < getCount(); position++) {
                int index = _positions.indexOf(position);
                if (index == -1) continue;
                int numeral = _numerals.get(index);
                Log.i(TAG, "Hiding" + numeral);
                _imageViews.get(index).setImageResource(R.drawable.flower);
            }
            _okToClick = true;
        }
    };
    private void showAll() {
        for (int position = 0; position < getCount(); position++) {
            int index = _positions.indexOf(position);
            if (index == -1) continue;
            int numeral = _numerals.get(index);
            _imageViews.get(index).setImageResource(mThumbIds[numeral]);
        }
    }
    public int getCount() {
        return _nCols * _nRows;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(_Context);
            imageView.setLayoutParams(new GridView.LayoutParams(65, 65));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        Log.i(TAG, "Looking up " + position);
        int index = _positions.indexOf(position);
        if (index > -1) {
            _imageViews.set(index, imageView);

            int numeral = _numerals.get(index);
            Log.i(TAG, "Placing " + numeral + " at " + position);

            imageView.setImageResource(mThumbIds[numeral]);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setLayoutParams(new GridView.LayoutParams(200, 200));
        }
        return imageView;
    }

    // references to our images
    private Integer[] mThumbIds = {
            R.drawable.numeral_0,
            R.drawable.numeral_1,
            R.drawable.numeral_2,
            R.drawable.numeral_3,
            R.drawable.numeral_4,
            R.drawable.numeral_5,
            R.drawable.numeral_6,
            R.drawable.numeral_7,
            R.drawable.numeral_8,
            R.drawable.numeral_9,
    };

    public void handleClick(ImageView imageView, int position) {
        if (!_okToClick) return;
        // get numeral at position
        if (_milliseconds == -2 && !_dontHide) {
            hideNumbers.run();
            _dontHide = true;
        }
        int index = _positions.indexOf(position);
        if (index == -1) {
            return;
        }
        int numeral = _numerals.get(index);
        Log.i(TAG, "You touched " + numeral);
        imageView.setImageResource(mThumbIds[numeral]);
//        _imageViews.get(index).setImageResource(R.drawable.flower);
//        ImageView imageView = (ImageView) view.getItemAtPosition(position);
//        _imageViews.get(index).setImageResource(mThumbIds[numeral]);

        if (numeral > ++_lastClick)
            gameOver();
        else {
            if (numeral == _level - 1) {
                setLevel(_level + 1);
                showAlert("Good Job!!", "You Won!");
            }
        }
    }
    private void gameOver() {
        _okToClick = false;
        showAll();
        setLevel(_level - 1);
        showAlert("Fail!", "Game Over");
    }
    private void showAlert(String mess, String title) {
        _alertDialog.setTitle(title);
        _alertDialog.setMessage(mess + " Your level is now " + _level);
        _alertDialog.show();
    }
}