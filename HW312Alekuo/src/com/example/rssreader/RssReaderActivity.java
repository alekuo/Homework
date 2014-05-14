package com.example.rssreader;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.example.rssreader.ShakeDetector;
import android.app.ListActivity;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * The main activity which starts the process to create the database and
 * provides the UI. 
 * @author Alena
 *
 */
public class RssReaderActivity extends ListActivity {

	// These are used to display the RSS feed.
    private ArrayList<RSSItem> mItemlist = null;
    private RSSListAdaptor mRssAdaptor = null;

    // The following are used for the shake detection and feedback
	private TextView mStatusBox;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mStatusBox = (TextView) findViewById(R.id.status);
		mItemlist = new ArrayList<RSSItem>();
	
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
	
		// Populate all data and views on shake
	    mItemlist = new ArrayList<RSSItem>();
	    new RetrieveRSSFeeds().execute();
	}
	
	// Refresh all data and views on shake
	public void handleShakeEvent(int c) {
	
		new RetrieveRSSFeeds().execute();
	}


	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        
        RSSItem data = mItemlist.get(position);
        
    	Uri aUri = Uri.parse(ArticleContentProvider.CONTENT_URI + "/" + data.dbID);
    	Intent i = new Intent(this, DetailActivity.class);
    	i.putExtra(ArticleContentProvider.CONTENT_ITEM_TYPE, aUri);

    	startActivity(i);
    }
    
    // Calling into the local ArticleContentProvider object to reset the database table
    private void updateDatabase() {
    	final ContentResolver resolver = this.getContentResolver();
    	final ContentProviderClient client = resolver.acquireContentProviderClient(ArticleContentProvider.AUTHORITY);
    	final ArticleContentProvider provider = (ArticleContentProvider) client.getLocalContentProvider();
    	provider.resetDatabase();
    	client.release();    
    }

    private void refreshList() {
    	// Grab the data from the database into a cursor
    	String[] projection = { DatabaseHelper.ArticleID, DatabaseHelper.ArticleTitle, DatabaseHelper.ArticleContent, DatabaseHelper.ArticleIcon, DatabaseHelper.ArticleDate };
	    Cursor cursor = getContentResolver().query(ArticleContentProvider.CONTENT_URI, projection, null, null, DatabaseHelper.ArticleDate + " DESC");

	    //Iterate over a cursor to populate the applicable list items
	    if (cursor != null) {
	    	cursor.moveToFirst();

		    while(cursor.moveToNext()) {
		    	RSSItem i = new RSSItem();
		    	
		    	i.dbID = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.ArticleID));
		    	
		    	i.title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ArticleTitle));
		    	i.description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ArticleContent));
		    	i.icon = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ArticleIcon));
		    	
		    	// Convert the long from the database into a string to assign into the rss item
		    	long k = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.ArticleDate));
		    	i.date = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss").format(new Date(k));

		    	mItemlist.add(i);
	    	}
	    	// Always close the cursor
	    	cursor.close();
	    }
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
    
    
/**
 * Private class which implements reloading and updating the list of the articles
 * in the background while showing the refresh bar at the top. 
 * @author Alena
 *
 */
private class RetrieveRSSFeeds extends AsyncTask<Void, Void, Void>
{    
	// This is the main functionality - update the database, update the ui,
	// display the refreshing bar between.
    @Override
    protected Void doInBackground(Void... params) {

        updateDatabase();
        refreshList();
        mRssAdaptor = new RSSListAdaptor(RssReaderActivity.this, R.layout.rssitemview, mItemlist);
        
        return null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }
    
    @Override
    protected void onPreExecute() {
		mStatusBox.setVisibility(View.VISIBLE);
        
        super.onPreExecute();
    }
    
    @Override
    protected void onPostExecute(Void result) {
        setListAdapter(mRssAdaptor);
        
        mStatusBox.setVisibility(View.GONE);
        
        super.onPostExecute(result);
    }
    
    @Override
    protected void onProgressUpdate(Void... values) {
    	super.onProgressUpdate(values);
    }
}

/**
 * Private class which populates each individual article view. 
 * @author Alena
 *
 */
private class RSSListAdaptor extends ArrayAdapter<RSSItem>{
	// list of RSSItem objects from which to pick the data.
    private List<RSSItem> objects = null;
    
    public RSSListAdaptor(Context context, int textviewid, List<RSSItem> objects) {
        super(context, textviewid, objects);
        
        this.objects = objects;
    }
    
    @Override
    public int getCount() {
        return ((null != objects) ? objects.size() : 0);
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public RSSItem getItem(int position) {
        return ((null != objects) ? objects.get(position) : null);
    }
    
    /**
     * This function gets executed when an item on the list comes into view.
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        
        // If the view has not been created before, create it
        if(view == null)
        {
            LayoutInflater vi = (LayoutInflater)RssReaderActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.rssitemview, null);
        }
        
        // Find the applicable item in the list containing the articles 
        RSSItem data = objects.get(position);
        
        //Assign the values to the appropriate fields
        if(data != null)
        {
        	ImageView icon = (ImageView)view.findViewById(R.id.imgIcon);
		    TextView title = (TextView)view.findViewById(R.id.txtTitle);
		    TextView date = (TextView)view.findViewById(R.id.txtDate);
		
			title.setText(data.title);
			// Adding "on" before the date, just because
			date.setText("on " + data.date);                            
			
			// This following is to assign the images from the articles to the icon views properly
			// First, check if the view already has a load icon task assigned
			// and if so, cancel it, since we will be assigning a new image there
			LoadIconTask oldTask = (LoadIconTask) icon.getTag(R.id.icon_task);
			if (oldTask != null) {
				oldTask.cancel(true);
			}
			// Assigned the default image to hold place 
			// Using the deprecated method to accommodate API Levels below 16.
			icon.setBackgroundDrawable(getResources().getDrawable(R.drawable.article));

			// If there is an image associated with the article, assign a new load icon task
			// to the view and execute the image assigning in the background through it.
			if(data.icon != null) {
				icon.setTag(R.id.icon_url, data.icon);
				LoadIconTask newTask = new LoadIconTask();
				icon.setTag(R.id.icon_task, newTask);
				newTask.execute(icon);
			}
        }
        return view;
    }
}
}            

            
	
