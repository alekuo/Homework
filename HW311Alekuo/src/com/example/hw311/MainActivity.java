package com.example.hw311;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.example.hw311.ArticleContentProvider;

public class MainActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor>  {

	private TextView statusBox;

    // The following are used for the shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    private SimpleCursorAdapter adapter;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		statusBox = (TextView) findViewById(R.id.status);

        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {
 
            @Override
            public void onShake(int count) {

                handleShakeEvent(count);
            }
        });

       	refreshList();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void handleShakeEvent(int c) {

		statusBox.setVisibility(View.VISIBLE);
		updateDatabase();
	    // SLEEP 2 SECONDS HERE ...
	    Handler handler = new Handler(); 
	    handler.postDelayed(new Runnable() { 
	         public void run() { 
	        	 statusBox.setVisibility(View.GONE);
	         } 
	    }, 2000);
	}
	
	@Override
    public void onResume() {
        super.onResume();
        // Add the following line to register the Session Manager Listener onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }
 
    @Override
    public void onPause() {
        // Add the following line to unregister the Sensor Manager onPause
    	mSensorManager.unregisterListener(mShakeDetector, mAccelerometer);
        super.onPause();
    }
    
    // Opens the second activity if an entry is clicked
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	Intent i = new Intent(this, DetailActivity.class);
    	Uri aUri = Uri.parse(ArticleContentProvider.CONTENT_URI + "/" + id);
    	i.putExtra(ArticleContentProvider.CONTENT_ITEM_TYPE, aUri);

    	startActivity(i);
    }
    
    
    private void refreshList() {

    	// Get the cursor for the adapter
		String[] projection = { DatabaseHelper.ArticleID, DatabaseHelper.ArticleTitle, DatabaseHelper.ArticleContent, DatabaseHelper.ArticleIcon, DatabaseHelper.ArticleDate };
	    Cursor cursor = getContentResolver().query(ArticleContentProvider.CONTENT_URI, projection, null, null, null);
	    
		// Fields from the database (projection)
		// Must include the _id column for the adapter to work
		String[] from = new String[] { DatabaseHelper.ArticleIcon, DatabaseHelper.ArticleTitle };
		// Fields on the UI to which we map
		int[] to = new int[] {R.id.icon, R.id.title };
		
	    adapter = new SimpleCursorAdapter(this, R.layout.list_article, cursor, from, to, 0);

	    this.setListAdapter(adapter);
		getLoaderManager().initLoader(0, null, this);

    }

    // Calling into the local ArticleContentProvider object to reset the database table
    private void updateDatabase() {
    	ContentResolver resolver = this.getContentResolver();
    	ContentProviderClient client = resolver.acquireContentProviderClient(ArticleContentProvider.AUTHORITY);
    	ArticleContentProvider provider = (ArticleContentProvider) client.getLocalContentProvider();
    	provider.resetDatabase();
    	client.release();    
    }
     
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
	    String[] projection = { DatabaseHelper.ArticleID, DatabaseHelper.ArticleIcon, DatabaseHelper.ArticleTitle };
	    CursorLoader cursorLoader = new CursorLoader(this, ArticleContentProvider.CONTENT_URI, projection, null, null, null);

	    return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
		// Not implementing
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// Not implementing	
	}
}
