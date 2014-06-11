package com.example.weather;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.app.Activity;
import android.content.Intent;

/**
 * This class displays a simple text field where the user can type in
 * a location - either a zipcode, or city/country/region name.
 * The activity then pulls all applicable WOEIDs, if possible, 
 * and chooses the first one, if there are more than one, to use 
 * in accessing the Yahoo weather API.
 * @author Alena
 *
 */

public class LookupActivity extends Activity {

	// Example for "New York"
	// http://query.yahooapis.com/v1/public/yql?q=select*from geo.places where
	// text="New York"&format=xml
	final String yahooPlaceApisBase = "http://query.yahooapis.com/v1/public/yql?q=select*from%20geo.places%20where%20text=";
	final String yahooapisFormat = "&format=xml";
	String yahooPlaceAPIsQuery;

	EditText place;
	Button search;
	ListView listviewWOEID;
	String units;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		place = (EditText) findViewById(R.id.place);
		search = (Button) findViewById(R.id.search);
		listviewWOEID = (ListView) findViewById(R.id.woeidlist);

	    // Check from the saved Instance
	    units = (savedInstanceState == null) ? "c" : (String) savedInstanceState.getString("SEL_UNITS");

	    // Or passed from the other activity
	    Bundle extras = getIntent().getExtras();
	    if (extras != null) {
	    	units = (String) extras.getString("SEL_UNITS");
	    }

		search.setOnClickListener(searchOnClickListener);
	}
	
	// Implementing the Get Weather button
	Button.OnClickListener searchOnClickListener = new Button.OnClickListener() {

		@Override
		public void onClick(View arg0) {
			if (place.getText().toString().equals("")) {
				Toast.makeText(getBaseContext(), "Enter place!", Toast.LENGTH_LONG).show();
			} else {
				new MyQueryYahooPlaceTask().execute();
			}
		}
	};

	// This class accesses the Yahoo API to find all matching WOEIDs.
	private class MyQueryYahooPlaceTask extends AsyncTask<Void, Void, Void> {

		//ArrayList<String> l;
		String location;

		@Override
		protected Void doInBackground(Void... arg0) {
			//l = QueryYahooPlaceAPIs();
			location = QueryYahooFirstPlaceAPI();
			return null;
		}

		// If the WOEID is found, it is passed to GetWeather class to look up the weather.
		// If not, feedback is given.
		@Override
		protected void onPostExecute(Void result) {

			if(!location.isEmpty()) {
				String selWoeid = location;
	
				/*
				 * Toast.makeText(getApplicationContext(), selWoeid,
				 * Toast.LENGTH_LONG).show();
				 */
	
				Intent intent = new Intent();
				intent.setClass(LookupActivity.this, GetWeather.class);
				Bundle bundle = new Bundle();
				bundle.putString("SEL_WOEID", selWoeid);
				bundle.putString("SEL_UNITS", units);
				intent.putExtras(bundle);
				startActivity(intent);
			} else {
				 Toast myToast = Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.wrongLocation), Toast.LENGTH_LONG);
				 myToast.setGravity(Gravity.TOP|Gravity.CENTER_HORIZONTAL, 0, 600);
				 myToast.show();
				
				 place.selectAll();
			}
			super.onPostExecute(result);
		}
	}

	// Not needed now, but may be in the future
//	private ArrayList<String> QueryYahooPlaceAPIs() {
//		String uriPlace = Uri.encode(place.getText().toString());
//
//		yahooPlaceAPIsQuery = yahooPlaceApisBase + "%22" + uriPlace + "%22" + yahooapisFormat;
//
//		String woeidString = QueryYahooWeather(yahooPlaceAPIsQuery);
//		Document woeidDoc = convertStringToDocument(woeidString);
//		return parseWOEID(woeidDoc);
//	}

	// Getting all the results from Yahoo and returning the first one (if more than one) 
	private String QueryYahooFirstPlaceAPI() {
		String loc = "";
		String uriPlace = Uri.encode(place.getText().toString());

		yahooPlaceAPIsQuery = yahooPlaceApisBase + "%22" + uriPlace + "%22" + yahooapisFormat;

		String woeidString = QueryYahooWeather(yahooPlaceAPIsQuery);
		Document woeidDoc = convertStringToDocument(woeidString);
		
		ArrayList<String> locs = parseWOEID(woeidDoc);
		if(locs.size() > 0){
			loc = locs.get(0);
		}
		return loc;
	}

	// Parsing the results returned by the API into individual strings for each WOEID
	private ArrayList<String> parseWOEID(Document srcDoc) {
		ArrayList<String> listWOEID = new ArrayList<String>();

		NodeList nodeListDescription = srcDoc.getElementsByTagName("woeid");
		if (nodeListDescription.getLength() >= 0) {
			for (int i = 0; i < nodeListDescription.getLength(); i++) {
				listWOEID.add(nodeListDescription.item(i).getTextContent());
			}
		} else {
			listWOEID.clear();
		}

		return listWOEID;
	}

	// Creating a Document
	private Document convertStringToDocument(String src) {
		Document dest = null;

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser;

		try {
			parser = dbFactory.newDocumentBuilder();
			dest = parser.parse(new ByteArrayInputStream(src.getBytes()));
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return dest;
	}

	// Accessing the Yahoo API with the string typed in by the user. 
	private String QueryYahooWeather(String queryString) {
		String qResult = "";

		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(queryString);

		try {
			HttpEntity httpEntity = httpClient.execute(httpGet).getEntity();

			if (httpEntity != null) {
				InputStream inputStream = httpEntity.getContent();
				Reader in = new InputStreamReader(inputStream);
				BufferedReader bufferedreader = new BufferedReader(in);
				StringBuilder stringBuilder = new StringBuilder();

				String stringReadLine = null;

				while ((stringReadLine = bufferedreader.readLine()) != null) {
					stringBuilder.append(stringReadLine + "\n");
				}

				qResult = stringBuilder.toString();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			//;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return qResult;
	}
}
