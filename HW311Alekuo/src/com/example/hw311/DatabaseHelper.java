package com.example.hw311;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;



public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String TAG = "DatabaseHelper";
	
	public static final String TEXT_TYPE = " TEXT";
	public static final String DATE_TYPE = " DATE";
	public static final String comma = ",";
	public static final String TableName = "articles";
	
	public static final String ArticleID = "_id";
	public static final String ArticleTitle = "ArticleTitle";
	public static final String ArticleContent = "ArticleContent";
	public static final String ArticleIcon = "ArticleIcon";
	public static final String ArticleDate = "ArticleDate";
	
	public static final String CreateMainTable =
			"create table if not exists " + TableName + " (" +
					ArticleID + " INTEGER PRIMARY KEY autoincrement" + comma +
					ArticleTitle + TEXT_TYPE + comma +
					ArticleContent + TEXT_TYPE + comma +
					ArticleIcon + TEXT_TYPE + comma +
					ArticleDate + TEXT_TYPE + 
				" ) ";

	public static final String DropTable = "DROP TABLE IF EXISTS " + TableName;
	
	public static final String SelectFromTable = "SELECT * FROM " + TableName;
	
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Homework311Alekuo.db";
    
    public final Context myContext;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        myContext = context;
    }
    
	@Override
	public void onCreate(SQLiteDatabase db) {
		
    	db.execSQL(CreateMainTable);

    	String xmlName;
    	
        // Get xml resource file
        Resources res = myContext.getResources();
        
        ContentValues conValues = new ContentValues();;
        
        // Open xml file
        XmlResourceParser xmlParser = res.getXml(R.xml.hw311_data);
        try
        {
            int eventType = xmlParser.getEventType();
            // Check for end of document        	
            while (eventType != XmlPullParser.END_DOCUMENT) {
            	xmlName = xmlParser.getName();

                switch (eventType){
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    
                    if (xmlName.equalsIgnoreCase("item")) {
                        //Add default records to articles
                        conValues = new ContentValues();   
                    } else if (conValues != null) {
                    	// Getting the individual values
						if (xmlName.equalsIgnoreCase("title")){
							conValues.put(ArticleTitle, xmlParser.nextText());
						} else if (xmlName.equalsIgnoreCase("content")) {
							conValues.put(ArticleContent, xmlParser.nextText());
						} else if (xmlName.equalsIgnoreCase("icon")) {
							conValues.put(ArticleIcon, xmlParser.nextText());
						} else if (xmlName.equalsIgnoreCase("date")) {
							conValues.put(ArticleDate, xmlParser.nextText());
						}  
                    }
                    break;
                case XmlPullParser.END_TAG:
                	// Inserting the row with all the values into the database
                    if (xmlName.equalsIgnoreCase("item") && conValues != null){
                     	db.insert(TableName, null, conValues);
                    	conValues = null;
                   } 
                }
                eventType = xmlParser.next();
            }
        }
        
        //Catch errors
        catch (XmlPullParserException e)
        {       
            Log.e(TAG, e.getMessage(), e);
        }
        catch (IOException e)
        {
            Log.e(TAG, e.getMessage(), e);
             
        }           
        finally
        {           
            //Close the xml file
        	xmlParser.close();
//         	db.close();
        }
       
	}

	@Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simply discard the data and start over

        db.execSQL(DropTable);
        onCreate(db);
    }
	
	public void refresh(SQLiteDatabase db) {
        // Dropping the table "articles" and re-populating it

        db.execSQL(DropTable);
        onCreate(db);
	}
}