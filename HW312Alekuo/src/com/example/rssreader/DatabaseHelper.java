package com.example.rssreader;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * This class implements creating, updating, and communicating with SQLite database.
 * @author Alena
 *
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	// Setting up strings for the commands
	public static final String TEXT_TYPE = " TEXT";
	public static final String DATE_TYPE = " DATETIME";
	public static final String comma = ",";
	public static final String TableName = "articles";
	
	public static final String ArticleID = "_id";
	public static final String ArticleTitle = "ArticleTitle";
	public static final String ArticleContent = "ArticleContent";
	public static final String ArticleIcon = "ArticleIcon";
	public static final String ArticleDate = "ArticleDate";

	private static final String TAG = "DatabaseHelper";

	private static final String CreateMainTable =
			"create table if not exists " + TableName + " (" +
					ArticleID + " INTEGER PRIMARY KEY autoincrement" + comma +
					ArticleTitle + TEXT_TYPE + comma +
					ArticleContent + TEXT_TYPE + comma +
					ArticleIcon + TEXT_TYPE + comma +
					ArticleDate + DATE_TYPE + 
				" ) ";

	private static final String DropTable = "DROP TABLE IF EXISTS " + TableName;	
	//private static final String SelectFromTable = "SELECT * FROM " + TableName + "ORDER BY " + ArticleDate;
	
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Homework312Alekuo.db";
	
    private static final String CreateDateIndex = "CREATE INDEX 'dateIndex' ON "+ TableName + " (" + ArticleDate + ");"; 

    private ContentValues conValues = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
	@Override
	public void onCreate(SQLiteDatabase db) {
		// Creating the main and only table
    	db.execSQL(CreateMainTable);
    	
    	// Grab the information from the two RSS Feeds
    	populateDatabase(db, "https://news.google.com/news/section?topic=w&output=rss");
    	populateDatabase(db, "http://news.yahoo.com/rss/world/"); 
    	
    	// Creating an index
    	db.execSQL(CreateDateIndex);
	}

	// Iterate of the items in the XML feed, picking the ones of interest
	// arrange them into records, and record them into the database
	private void populateDatabase(SQLiteDatabase db, String urlToRssFeed) {
    	String xmlName;
    	XmlPullParser xmlParser = null;
    	
    	// Pulling the data into a parser
	    try {
	        URL url = new URL(urlToRssFeed);
	        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	        factory.setNamespaceAware(false);
	        xmlParser = factory.newPullParser();
	        xmlParser.setInput(url.openStream(), "UTF_8");
	    }
	    catch (Exception e) {
	        e.printStackTrace();
	    }

	    // Iterating over the XML and recording the values, when found,
	    // making sure to convert the dates into longs. 
	    if(xmlParser != null) {
	    	try {
	    		int eventType = xmlParser.getEventType();
	    		// Check for end of document        	
	    		while (eventType != XmlPullParser.END_DOCUMENT) {
	    			xmlName = xmlParser.getName();

	    			switch (eventType) {
	    				case XmlPullParser.START_DOCUMENT:
	    					break;
	    				case XmlPullParser.START_TAG:
          
	    					if (xmlName.equalsIgnoreCase("item")) {
	    						//Add default records to articles
	    						conValues = new ContentValues();   
	    					} else if (conValues != null) {
	    						// Getting the individual values
	    						if (xmlName.equalsIgnoreCase("title")) {
	    							conValues.put(ArticleTitle, xmlParser.nextText());
	    						} else if (xmlName.equalsIgnoreCase("pubDate")) {
									SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.getDefault());
									Date date = dateFormat.parse(xmlParser.nextText());
	    							conValues.put(ArticleDate, date.getTime());									
	    						} else if (xmlName.equalsIgnoreCase("description")) {
	    							conValues.put(ArticleContent, xmlParser.nextText());
								} else if (xmlName.equalsIgnoreCase("media:content")) {
									conValues.put(ArticleIcon, xmlParser.getAttributeValue(null, "url"));
	    						}  
	    					}
	    					break;
	    				case XmlPullParser.END_TAG:
	    					// Inserting the row with all the values into the database
	    					if (xmlName.equalsIgnoreCase("item") && conValues != null) {
	    						db.insert(TableName, null, conValues);
	    						conValues = null;
	    					} 
	    			}
	    			eventType = xmlParser.next();
	    		}      
	    	}
  
			//Catch errors
			catch (XmlPullParserException e) {       
				Log.e(TAG, e.getMessage(), e);
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);      
			} catch (ParseException e) {
				e.printStackTrace();
			} 
	    }
	}

	@Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simply discard the data and start over
        db.execSQL(DropTable);
        onCreate(db);
    }
	
	// This function is called at the beginning, and 
	// every time a mobile device is shaken.
	public void refresh(SQLiteDatabase db) {
        // Dropping the table "articles" and re-populating it
        db.execSQL(DropTable);
        onCreate(db);
	}
}