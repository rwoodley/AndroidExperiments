package org.woodley.aigrid.app;

import android.app.ActionBar;
import android.content.ClipData;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {
    private static final String TAG = "MainActivity";

    ImageAdapter _imageAdapter;
    MenuItem _modeMenuItem = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final GridView gridview = (GridView) findViewById(R.id.gridview);
        _imageAdapter = new ImageAdapter(this);
        _imageAdapter.newGame();        // scale will be all wrong, but we'll redraw it later. need to do this here for initialization.
        gridview.setAdapter(_imageAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Log.i(TAG, "At position " + position);
                _imageAdapter.handleClick((ImageView) v, position);
                //gridview.playSoundEffect(SoundEffectConstants.CLICK);
            }
        });
        //ActionBar ab = this.getActionBar();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            getActionBar().setHomeButtonEnabled(true);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override

    public boolean onPrepareOptionsMenu(Menu menu) {
        this.getActionBar().setIcon(R.drawable.ic_action_refresh);
        _modeMenuItem = menu.findItem(R.id.action_settings);
        MenuItem item = menu.findItem(R.id.chimpMode);
        item.setChecked(true);
        setMode(R.id.chimpMode, item);
        _imageAdapter.setLevel(3);
        GridView gridview = (GridView) findViewById(R.id.gridview);
        _imageAdapter.setWH(gridview.getWidth(), gridview.getHeight());
        newGame();
        return super.onPrepareOptionsMenu(menu);
    }
    private boolean setMode(int id, MenuItem item) {
        int icon = 0;
        if (id == R.id.chimpMode) {
//            setTitle("Chimp Mode");
            icon = R.drawable.chump;
            _imageAdapter.setMode(250);
        }
        else if (id == R.id.childMode) {
//            setTitle("Child Mode");
            icon = R.drawable.child;
            _imageAdapter.setMode(500);
        }
        else if (id == R.id.adultMode) {
//            setTitle("Adult Mode");
            icon = R.drawable.adult;
            _imageAdapter.setMode(1000);
        }
        else if (id == R.id.practiceMode) {
//            setTitle("Practice Mode");
            icon = R.drawable.targetpractice;
            _imageAdapter.setMode(-1);
        }
        else if (id == R.id.youControlMode) {
//            setTitle("Baby Chimp Mode");
            icon = R.drawable.babychimp;
            _imageAdapter.setMode(-2);
        }
        else
            return false;

        _modeMenuItem.setIcon(icon);
        return false;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
        if (item.getTitle().equals(this.getActionBar().getTitle())) { // there has got to be a better way.
//        if (id == R.id.action_replay) {
            newGame();
            return true;
        }
        if (setMode(id, item)) {
            newGame();
            item.setChecked(true);
        }
        return super.onOptionsItemSelected(item);
    }
    private void newGame() {
        _imageAdapter.newGame();
        final GridView gridview = (GridView) findViewById(R.id.gridview);
        gridview.setAdapter(_imageAdapter);
    }
}
