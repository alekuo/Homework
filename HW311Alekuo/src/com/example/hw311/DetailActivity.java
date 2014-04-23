package com.example.hw311;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class DetailActivity extends Activity {

	// Setting up the member variables
	private TextView aTitle;
	private ImageView aIcon;
	private TextView aContent;
	private TextView aDate;
	private ScrollView aContentScroll;
	
	private Uri aUri;
	
	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
	    setContentView(R.layout.activity_detail);

	    aTitle = (TextView) findViewById(R.id.detail_title);
	    aContentScroll = (ScrollView) findViewById(R.id.detail_content);
	    // The next three will be inside the scroll view.
	    aIcon = new ImageView(this);
	    aContent = new TextView(this);
	    aDate = new TextView(this);

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
		String[] projection = { DatabaseHelper.ArticleID, DatabaseHelper.ArticleTitle, DatabaseHelper.ArticleContent, DatabaseHelper.ArticleIcon, DatabaseHelper.ArticleDate };
	    Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
	    if (cursor != null) {
	    	cursor.moveToFirst();
	      
	    	LinearLayout lL = new LinearLayout(this);
	    	lL.setOrientation(LinearLayout.VERTICAL);

	    	aTitle.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ArticleTitle)));
	    	aContent.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ArticleContent)));

	    	// Optional icon field
	    	String icon = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ArticleIcon));
	    	if(icon != null && icon.length() > 0) {
	    		int validImage = this.getResources().getIdentifier(icon, "drawable", this.getPackageName());
	    		if (validImage != 0) {
	    			Drawable drawImage = getResources().getDrawable(this.getResources().getIdentifier(icon, "drawable", this.getPackageName()));
	
	    			if (drawImage != null ) {
	    				aIcon.setBackground(drawImage);
	    				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	    				layoutParams.gravity = Gravity.CENTER;
	    				aIcon.setLayoutParams(layoutParams);
		    		  
	    				lL.addView(aIcon);
	    			}
	    		}
	    	}
	      
	    	// Adding the mandatory content field
	    	lL.addView(aContent);
	    	
	    	// Optional date field
	    	String artDate = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.ArticleDate));
	    	if(artDate != null && artDate.length() > 0) {
	    		aDate.setText(artDate);
	    		aDate.setGravity(Gravity.RIGHT);
	    		lL.addView(aDate);
	    	}
	    	this.aContentScroll.addView(lL);

	    	// Always close the cursor
	    	cursor.close();
	    }
	}
}
