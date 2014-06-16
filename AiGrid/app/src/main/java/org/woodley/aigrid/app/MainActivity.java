package org.woodley.aigrid.app;

import android.app.ActionBar;
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
    private static final String    TAG                 = "MainActivity";

    ImageAdapter _imageAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final GridView gridview = (GridView) findViewById(R.id.gridview);
        _imageAdapter = new ImageAdapter(this);
        _imageAdapter.NewGame();
        gridview.setAdapter(_imageAdapter);

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Log.i(TAG, "At position " + position);
                _imageAdapter.handleClick((ImageView) v, position);
                //gridview.playSoundEffect(SoundEffectConstants.CLICK);
            }
        });
        //ActionBar ab = this.getActionBar();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_replay) {
            _imageAdapter.NewGame();
            final GridView gridview = (GridView) findViewById(R.id.gridview);
            gridview.setAdapter(_imageAdapter);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
