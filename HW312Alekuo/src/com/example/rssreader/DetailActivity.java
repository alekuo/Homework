package com.example.rssreader;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This class implements detail view of each article, with the image, title, date, and the content.
 * 
 * The RSS feeds often include an image in two places - as a media attached to the article
 * and embedded in the html in the content of the actual article. I am not parsing the content html
 * but displaying it inside a WebView, which may display the embedded image, if it exists,
 * in addition to my drawable next to the title.
 * @author Alena
 *
 */
public class DetailActivity extends Activity {

	// Setting up the member variables
	private TextView mTitle;
	private ImageView mIcon;
	private WebView mContent;
	private TextView mDate;
	
	private Uri aUri;
	
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
	    setContentView(R.layout.activity_detail);

	    // These are the views containing the data for the article.
	    mTitle = (TextView) findViewById(R.id.detail_txtTitle);
	    mIcon = (ImageView) findViewById(R.id.detail_imgIcon);
	    mDate = (TextView) findViewById(R.id.detail_txtDate);
	    // The content is inside the scroll view, in case it is too long.
	    // The header consisting of the other three view is always visible.
	    mContent = (WebView) findViewById(R.id.detail_txtContent);

	    Bundle extras = getIntent().getExtras();
	    // Check from the saved Instance
	    aUri = (bundle == null) ? null : (Uri) bundle.getParcelable(ArticleContentProvider.CONTENT_ITEM_TYPE);

	    // Or passed from the other activity
	    if (extras != null) {
	    	aUri = extras.getParcelable(ArticleContentProvider.CONTENT_ITEM_TYPE);
	    	fillData(aUri);
	    }
	}

	private void fillData(Uri uri) {
		// Creating a projection to use in the query to fetch the data for the detail view.
		String[] projection = { DatabaseHelper.ArticleID, DatabaseHelper.ArticleTitle, DatabaseHelper.ArticleContent, DatabaseHelper.ArticleIcon, DatabaseHelper.ArticleDate };
	    Cursor cursor = getContentResolver().query(uri, projection, null, null, DatabaseHelper.ArticleDate);

	    // If the data exists, populate it.
	    if (cursor != null) {
	    	cursor.moveToFirst();

	    	// Title and content are easy, no need to manipulate it before assigning to the views.
	    	mTitle.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ArticleTitle)));
	    	//mContent.setText(Html.fromHtml(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ArticleContent))));
	    	mContent.loadData(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ArticleContent)), "text/html", null);
	    	
	    	// Getting the long representing the date of the article
	    	long k = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.ArticleDate));
	    	// and convert it to the human-readable string
	    	String t = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss").format(new Date(k));
	    	
	    	// setting the date string to the date view
	    	mDate.setText(t);
	    	
	    	// Getting the string of the URL to the image from the database
	    	String icon = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ArticleIcon));
	 
	    	// Checking if the icon view has a load icon task assigned, and if so, canceling it. 
			LoadIconTask oldTask = (LoadIconTask) mIcon.getTag(R.id.icon_task);
			if (oldTask != null) {
				oldTask.cancel(true);
			}
			// Setting the background to the default image.
			// Using the deprecated method to accommodate API Levels below 16.
			mIcon.setBackgroundDrawable(getResources().getDrawable(R.drawable.article));

			// If the URL exists, assigned a new icon load task to the icon view
			// and set the view's background to the drawable by executing the task.
	    	if(icon != null && icon.length() > 0) {
				mIcon.setTag(R.id.icon_url, icon);
				LoadIconTask newTask = new LoadIconTask();
				mIcon.setTag(R.id.icon_task, newTask);
				newTask.execute(mIcon);
			}

	    	// Always close the cursor
	    	cursor.close();
	    }
	}
}
