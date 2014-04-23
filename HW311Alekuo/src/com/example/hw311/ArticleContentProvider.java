package com.example.hw311;

import java.util.Arrays;
import java.util.HashSet;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.example.hw311.DatabaseHelper;

import android.net.Uri;

public class ArticleContentProvider extends ContentProvider {

	public static final String AUTHORITY = "com.example.hw311";
	private static final String BASE_PATH = "articles";
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + BASE_PATH;
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE  +  "/" + BASE_PATH + "/#";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY  + "/" + BASE_PATH);
	
	// Database handler
	private DatabaseHelper dbHelper;
	
	// Used for the UriMatcher
	private static final int ArticlesTable = 10;
	private static final int Article_ID = 20;

	private static final UriMatcher hw311URIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		hw311URIMatcher.addURI(AUTHORITY, BASE_PATH, ArticlesTable);
		hw311URIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", Article_ID);
	}

	@Override
	public boolean onCreate() {
		dbHelper = new DatabaseHelper(this.getContext());
		return false;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}
	
	// Invoked when the database needs to be refreshed while running
	public void resetDatabase() {
		dbHelper.refresh(dbHelper.getWritableDatabase());
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		
	    // Check if the caller has requested a column which does not exists
	    checkColumns(projection);
	    
	    // Using SQLiteQueryBuilder instead of query() method
	    SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

	    // Set the table
	    queryBuilder.setTables(DatabaseHelper.TableName);

		int uriType = hw311URIMatcher.match(uri);
		switch (uriType) {
		case ArticlesTable:
			break;
		case Article_ID:
			// adding the ID to the original query
			queryBuilder.appendWhere(DatabaseHelper.ArticleID + "=" + uri.getLastPathSegment());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
	    
	    SQLiteDatabase db = dbHelper.getWritableDatabase();
	    Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
	    
	    //Make sure that potential listeners are getting notified
	    cursor.setNotificationUri(getContext().getContentResolver(), uri);
	    return cursor;
	}

	// not needed to implement
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}
	
	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		return 0;
	}

	
	// Utility function 
	private void checkColumns(String[] reqColumns) {
		String[] available = { DatabaseHelper.ArticleID, DatabaseHelper.ArticleTitle, DatabaseHelper.ArticleContent, DatabaseHelper.ArticleIcon, DatabaseHelper.ArticleDate };
		
		if (reqColumns != null) {
			HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(reqColumns));
			HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
			  
			// check if all columns which are requested are available
			if (!availableColumns.containsAll(requestedColumns)) {
				throw new IllegalArgumentException("Unknown columns requested.");
			}
		}
	}
}
