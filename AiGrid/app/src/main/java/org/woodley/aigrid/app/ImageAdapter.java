package org.woodley.aigrid.app;

import android.content.Context;
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
    private Context _Context;

    // constants
    int _nRows = 5;
    int _nCols = 8; // this is also in the layout xml.
    int _level = 10;

    int _currentlyDisplaying;
    ArrayList<Integer> _positions;
    ArrayList<Integer> _numerals;
    List<ImageView> _imageViews;

    public ImageAdapter(Context con) {
        _Context = con;
    }
    public void NewGame() {
        _positions = new ArrayList<Integer>();
        _numerals = new ArrayList<Integer>();
        _currentlyDisplaying = 0;
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
                pos = new Integer((int) (Math.random() * 10));
            }
            tempHS.add(pos);
            _numerals.add(pos);     // 0 thru 9
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

        int index = _positions.indexOf(position);
        if (index > -1) {
            _imageViews.set(index, imageView);

            int numeral = _numerals.get(index);
            Log.i(TAG, "Placing " + numeral + " at " + position);

            imageView.setImageResource(mThumbIds[numeral]);
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

    public void handleClick(View view, int position) {
        // get numeral at position
        int index = _positions.indexOf(position);
        if (index == -1) {
            view.playSoundEffect(SoundEffectConstants.NAVIGATION_UP);
            return;
        }
        int numeral = _numerals.get(index);
        Log.i(TAG, "You touched " + numeral);
        _imageViews.get(index).setImageResource(R.drawable.flower);
    }
}