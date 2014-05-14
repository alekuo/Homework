package com.example.rssreader;

import java.io.InputStream;
import java.net.URL;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.widget.ImageView;

/**
 * This class creates a drawable out of a URL and assigns in to an icon view,
 * in the background.
 * This task can be cancelled, in which case it will not assign the background.
 * @author Alena
 *
 */
public class LoadIconTask extends AsyncTask<ImageView, Void, Drawable>
{
    private ImageView view;

    @Override
    protected Drawable doInBackground(ImageView... imageViews) {
    	this.view = imageViews[0];
    	Drawable d = null;
        try {
            InputStream is = (InputStream) new URL((String)view.getTag(R.id.icon_url)).getContent();
            d = Drawable.createFromStream(is, "Article");
    		                     	
        } catch (Exception e) {
            System.out.println("Exc=" + e);
        }
        
        if (!isCancelled()) {
            return d;
        }
        
        return null;
    }

    @Override
    protected void onCancelled() {
    }
    
    @Override
    protected void onPreExecute() {
    }
    
    @Override
    protected void onPostExecute(Drawable result) {
        if (result != null) {
			// Using the deprecated method to accommodate API Levels below 16.
        	view.setBackgroundDrawable(result);
        }
    }
    
    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}
