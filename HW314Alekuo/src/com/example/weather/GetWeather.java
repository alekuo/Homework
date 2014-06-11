package com.example.weather;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
//import android.widget.Toast;


/**
 * This class displays the current weather for a specific location,
 * as well as several days of forecast. 
 * @author Alena
 *
 */
public class GetWeather extends Activity {

	TextView title;
	TextView countryTitle;
	ImageView weather_pic;
	TextView tempWeather;
	TextView condWeather;
	TextView otherWeather;
	String mWoeid;
	String mUnits;
	TableLayout dailyForecasts;
	
	ScrollView weathers;
	
	// Showing 5 days, including today, worth of forecast
	int forecastLength = 5;
	
	class MyWeather {
		
		String description;
		String city;
		String region;
		String country;

		String windChill;
		String windDirection;
		Double windSpeed;

		String sunrise;
		String sunset;

		int conditionCode;
		String conditiontext;
		String conditiondate;
		String conditiontemp;

		String forecastDay;
		String forecastDate;
		String forecastLow;
		String forecastHigh;
		String forecastText;

		DayForecast[] forecasts;

		// Returning the temperature with units symbol
		public String tempWeather() {

			String s;
			s = conditiontemp + " " + mUnits.toUpperCase() + (char) 0x00B0;
			return s;
		}
		
		// Returning the description of the current condition
		public String condWeather() {

			String s;
			s = conditiontext;
			return s;
		}
		
		// Returning other information, about wind and sun
		public String otherWeather() {

			String s;
						
			// The weather date is currently not displayed, but it is here if it's decided to put it in.
			
//			String newDate = "";			
//	    	// Getting the long representing the date of the article
//	    	SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy h:mm a Z", Locale.getDefault());
//	    	long k = 0;
//			try {
//				k = dateFormat.parse(conditiondate).getTime();
//			} catch (ParseException e) {
//				// Auto-generated catch block
//				e.printStackTrace();
//			}
//	    	// and convert it to the human-readable string
//	    	newDate = new SimpleDateFormat("EEEE, d MMMM yyyy HH:mm").format(new Date(k));

			s = "Wind\nDirection: " + windDirection  + (char) 0x00B0 + "\nSpeed: " + String.format("%.1f", windSpeed);
			s += (mUnits.compareTo("f") == 0) ? " mph" : " kph";
			s += "\n\nSunrise: " + sunrise + "\n" + "Sunset: " + sunset +"\n";

			return s;
		}
		
		// Returning the location name and optionally region
		public String locationTitle() {
			String k;
			k = city; 
			if(region.length() > 0) {
				k +=  ", " + region;
			}
			return k;
		}
		
		public String countryTitle() {
			String k = "";
			if(country.length() > 0) {
				k += country;
			}
			return k;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_weather);
		title = (TextView) findViewById(R.id.title);
		countryTitle = (TextView) findViewById(R.id.countryTitle);
		weather_pic = (ImageView) findViewById(R.id.weather_pic);
		tempWeather = (TextView) findViewById(R.id.weatherTemp);
		condWeather = (TextView) findViewById(R.id.weatherCond);
		otherWeather = (TextView) findViewById(R.id.otherWeather);
		dailyForecasts = (TableLayout) findViewById(R.id.dailyForecasts);

	    // Check from the saved Instance 
		// The default is Celsius (metric) for units and Seattle, WA for location 
	    mUnits = (savedInstanceState == null) ? "c" : (String) savedInstanceState.getString("SEL_UNITS");
	    mWoeid = (savedInstanceState == null) ? "2490383" : (String) savedInstanceState.getString("SEL_WOEID");

	    // Or passed from the other activity
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null && savedInstanceState == null) {
			mWoeid = (String) bundle.getCharSequence("SEL_WOEID");
			mUnits = (String) bundle.getCharSequence("SEL_UNITS");
		}
		new MyQueryYahooWeatherTask(mWoeid, mUnits).execute();

		//Toast.makeText(getApplicationContext(), mWoeid + " " + mUnits, Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	  super.onSaveInstanceState(savedInstanceState);
	  // Save UI state changes to the savedInstanceState.
	  // This bundle will be passed to onCreate if the process is
	  // killed and restarted.
	  
	  // This is probably overkill, but does not harm anything.
	  savedInstanceState.putString("SEL_UNITS", mUnits);
	  savedInstanceState.putString("SEL_WOEID", mWoeid);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
	    // Always call the superclass so it can restore the view hierarchy
	    super.onRestoreInstanceState(savedInstanceState);
	   
	    // Restore state members from saved instance
	    mUnits = savedInstanceState.getString("SEL_UNITS");
	    mWoeid = savedInstanceState.getString("SEL_WOEID");
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.switch_units:
	            switchUnits(item);
	            return true;
	        case R.id.new_location:
	            newLocation();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	// Creating the option menus and name for the Switch Units option. 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    
	    MenuItem unitItem = menu.findItem(R.id.switch_units);
	    nameUnitsMenu(unitItem);
	    return super.onCreateOptionsMenu(menu);
	}
	
	// Changing the units in the app.
	private void switchUnits(MenuItem item) {
		mUnits = mUnits.compareTo("c") == 0?"f":"c";
	    new MyQueryYahooWeatherTask(mWoeid, mUnits).execute();
	    nameUnitsMenu(item);
	}
	
	// Starting LookupActivity to find a new location.
	private void newLocation() {
		Intent intent = new Intent();
		intent.setClass(this, LookupActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("SEL_UNITS", mUnits);
		intent.putExtras(bundle);
		startActivity(intent);
	}
	
	// Setting the correct text on the Switch Units menu option
	private void nameUnitsMenu(MenuItem item) {
	    
	    String newTitle = this.getResources().getString(R.string.switch_to);
	    if (mUnits.compareTo("c") == 0) {
	    	newTitle += " " + this.getResources().getString(R.string.fahrenheit);
	    } else if (mUnits.compareTo("f") == 0){
	    	newTitle += " " + this.getResources().getString(R.string.celsius);	    	
	    }
	    item.setTitle(newTitle);
	}
	
	/**
	 * This class accesses the Yahoo Weather API in the background
	 * and sets up the applicable data in GUI. 
	 * @author Alena
	 *
	 */
	private class MyQueryYahooWeatherTask extends AsyncTask<Void, Void, Void> {

		String woeid;
		String units;
		String weatherTitle;
		String cTitle;
		int weatherCode;
		String weatherResultTemp;
		String weatherResultCond;
		String weatherResultOther;
		String weatherString;
		DayForecast[] dailies;

		MyQueryYahooWeatherTask(String w, String u) {
			woeid = w;
			units = u;
		}

		// Accessing the information from Yahoo and populating the variables with it.
		@Override
		protected Void doInBackground(Void... arg0) {
			weatherString = QueryYahooWeather();
			Document weatherDoc = convertStringToDocument(weatherString);

			if (weatherDoc != null) {
				MyWeather w = parseWeather(weatherDoc);
				weatherTitle = w.locationTitle();
				cTitle = w.countryTitle();
				weatherCode = w.conditionCode;
				weatherResultTemp = w.tempWeather();
				weatherResultCond = w.condWeather();
				weatherResultOther = w.otherWeather();
				dailies = getForecast(weatherDoc);
			} else {
				weatherTitle = "";
				cTitle = "";
				weatherCode = 3200;
				weatherResultTemp = "Cannot convert String To Document!";
				weatherResultCond = "";
				weatherResultOther = "";
				dailies = null;
			}

			return null;
		}

		// Populating the interface with data
		@Override
		protected void onPostExecute(Void result) {
			title.setText(weatherTitle);
			countryTitle.setText(cTitle);
			DayForecast d = new DayForecast(getApplicationContext());
			d.setIcon(getApplicationContext(), weather_pic, weatherCode);
			weather_pic.getLayoutParams().height = weather_pic.getMeasuredWidth();
			tempWeather.setText(weatherResultTemp);
			condWeather.setText(weatherResultCond);
			otherWeather.setText(weatherResultOther);
			setForecast();
			super.onPostExecute(result);
		}
		
		// Populating the bottom part of the GUI - several days of forecast
		private void setForecast() {
			String tempString;
			int colWidth;
			// If there are already rows in the table layout, remove them
	        dailyForecasts.removeAllViewsInLayout();
	        colWidth = dailyForecasts.getMeasuredWidth() / forecastLength;
	        
	        //Create the new rows to populate with data 
			TableRow tr1 = new TableRow(getApplicationContext());
			TableRow tr2 = new TableRow(getApplicationContext());
			TableRow tr3 = new TableRow(getApplicationContext());
			TableRow tr4 = new TableRow(getApplicationContext());
	        tr1.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	        tr2.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	        tr3.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	        tr4.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
	        
	        // Loop over the forecast days
	        for(int i = 0; i < forecastLength; i++) {
	        	// Day of the week
	        	TextView tDay = new TextView(getApplicationContext());
	        	tDay.setMaxWidth(colWidth);
	        	tDay.setTextColor(getResources().getColor(R.color.my_white));
	        	tDay.setTypeface(null, Typeface.BOLD);
	        	float wt_px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
	        	tDay.setTextSize(wt_px);
	        	tDay.setGravity(Gravity.CENTER);
	        	tDay.setText(dailies[i].forecastDay);
	        	tr1.addView(tDay);
	        	
	        	// Weather graphic
				ImageView iPic = new ImageView(getApplicationContext());

				TableRow.LayoutParams l = new TableRow.LayoutParams(colWidth, colWidth);
				iPic.setLayoutParams(l);
				dailies[i].setIcon(getApplicationContext(), iPic, dailies[i].forecastCode);
				tr2.addView(iPic);
	        	
				// Weather Condition text descriptor
				TextView tCond = new TextView(getApplicationContext());
				tCond.setMaxWidth(colWidth);
				tCond.setTextColor(getResources().getColor(R.color.my_white));
				tCond.setText(dailies[i].forecastText);
				tr3.addView(tCond);
				
				// High and Low temps in current degrees
				TextView tTemp = new TextView(getApplicationContext());
				tTemp.setMaxWidth(colWidth);
				tTemp.setTextColor(getResources().getColor(R.color.my_white));
				tempString = 
						"H: " + dailies[i].forecastHigh + units.toUpperCase() + (char) 0x00B0 + "\n" +
						"L: " + dailies[i].forecastLow + units.toUpperCase() + (char) 0x00B0 + "\n";
				tTemp.setText(tempString);
				tr4.addView(tTemp);

	        }
	        
	        // Add the rows
	        dailyForecasts.addView(tr1, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
	        dailyForecasts.addView(tr2, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
	        dailyForecasts.addView(tr3, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
	        dailyForecasts.addView(tr4, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));	        
		}

		// Accessing Yahoo API using the location and the units
		private String QueryYahooWeather() {
			String qResult = "";
			String queryString = "http://weather.yahooapis.com/forecastrss?w=" + woeid + "&u=" + units;

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
			} catch (IOException e) {
				e.printStackTrace();
			}
			return qResult;
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

		private MyWeather parseWeather(Document srcDoc) {

			MyWeather myWeather = new MyWeather();
			myWeather.forecasts = new DayForecast[5];

			// <description>Yahoo! Weather for New York, NY</description>
			NodeList descNodelist = srcDoc.getElementsByTagName("description");
			if (descNodelist != null && descNodelist.getLength() > 0) {
				myWeather.description = descNodelist.item(0).getTextContent();
			} else {
				myWeather.description = "EMPTY";
			}

			// <yweather:location city="New York" region="NY"
			// country="United States"/>
			NodeList locationNodeList = srcDoc.getElementsByTagName("yweather:location");
			if (locationNodeList != null && locationNodeList.getLength() > 0) {
				Node locationNode = locationNodeList.item(0);
				NamedNodeMap locNamedNodeMap = locationNode.getAttributes();

				myWeather.city = locNamedNodeMap.getNamedItem("city").getNodeValue().toString();
				myWeather.region = locNamedNodeMap.getNamedItem("region").getNodeValue().toString();
				myWeather.country = locNamedNodeMap.getNamedItem("country").getNodeValue().toString();
			} else {
				myWeather.city = "EMPTY";
				myWeather.region = "EMPTY";
				myWeather.country = "EMPTY";
			}

			// <yweather:wind chill="60" direction="0" speed="0"/>
			NodeList windNodeList = srcDoc.getElementsByTagName("yweather:wind");
			if (windNodeList != null && windNodeList.getLength() > 0) {
				Node windNode = windNodeList.item(0);
				NamedNodeMap windNamedNodeMap = windNode.getAttributes();

				myWeather.windChill = windNamedNodeMap.getNamedItem("chill").getNodeValue().toString();
				myWeather.windDirection = windNamedNodeMap.getNamedItem("direction").getNodeValue().toString();
				myWeather.windSpeed = Double.parseDouble(windNamedNodeMap.getNamedItem("speed").getNodeValue().toString());
			} else {
				myWeather.windChill = "EMPTY";
				myWeather.windDirection = "EMPTY";
				myWeather.windSpeed = 0.0;
			}

			// <yweather:astronomy sunrise="6:52 am" sunset="7:10 pm"/>
			NodeList astNodeList = srcDoc.getElementsByTagName("yweather:astronomy");
			if (astNodeList != null && astNodeList.getLength() > 0) {
				Node astNode = astNodeList.item(0);
				NamedNodeMap astNamedNodeMap = astNode.getAttributes();

				myWeather.sunrise = astNamedNodeMap.getNamedItem("sunrise").getNodeValue().toString();
				myWeather.sunset = astNamedNodeMap.getNamedItem("sunset").getNodeValue().toString();
			} else {
				myWeather.sunrise = "EMPTY";
				myWeather.sunset = "EMPTY";
			}

			// <yweather:condition text="Fair" code="33" temp="60"
			// date="Fri, 23 Mar 2012 8:49 pm EDT"/>
			NodeList conditionNodeList = srcDoc.getElementsByTagName("yweather:condition");
			if (conditionNodeList != null && conditionNodeList.getLength() > 0) {
				Node conditionNode = conditionNodeList.item(0);
				NamedNodeMap conditionNamedNodeMap = conditionNode.getAttributes();

				myWeather.conditionCode = Integer.parseInt(conditionNamedNodeMap.getNamedItem("code").getNodeValue().toString());
				myWeather.conditiontext = conditionNamedNodeMap.getNamedItem("text").getNodeValue().toString();
				myWeather.conditiondate = conditionNamedNodeMap.getNamedItem("date").getNodeValue().toString();
				myWeather.conditiontemp = conditionNamedNodeMap.getNamedItem("temp").getNodeValue().toString();
			} else {
				myWeather.conditiontext = "EMPTY";
				myWeather.conditiondate = "EMPTY";
				myWeather.conditiontemp = "EMPTY";
			}
			
			return myWeather;
		}
		
		// Reading and storing the data on weather forecasts for the predefinednumber of days
		private DayForecast[] getForecast(Document srcDoc) {
			DayForecast[] allForecasts = new DayForecast[forecastLength];
			int currForecast = 0;
			
			// <yweather:forecast day="Sun" date="1 Jun 2014" low="63" high="87" text="Clear" code="31"/>
			NodeList forecastNodeList = srcDoc.getElementsByTagName("yweather:forecast");
			if (forecastNodeList != null && forecastNodeList.getLength() > 0) {
				for(int i = 0; i < forecastNodeList.getLength(); i++) {
					Node forecastNode = forecastNodeList.item(i);
					NamedNodeMap forecastNamedNodeMap = forecastNode.getAttributes();
					DayForecast forecast = new DayForecast(getApplicationContext());
					
					forecast.forecastDay = forecastNamedNodeMap.getNamedItem("day").getNodeValue().toString();
					forecast.forecastDate = forecastNamedNodeMap.getNamedItem("date").getNodeValue().toString();
					forecast.forecastLow = forecastNamedNodeMap.getNamedItem("low").getNodeValue().toString();
					forecast.forecastHigh = forecastNamedNodeMap.getNamedItem("high").getNodeValue().toString();
					forecast.forecastText = forecastNamedNodeMap.getNamedItem("text").getNodeValue().toString();
					try {
						forecast.forecastCode = Integer.parseInt(forecastNamedNodeMap.getNamedItem("code").getNodeValue().toString());
					} catch(NumberFormatException nfe) {
						
					}
					
					if(currForecast < forecastLength) {
						allForecasts[currForecast] = forecast;
						currForecast++;
					}
				}
			} else {
				
				allForecasts[0] = new DayForecast(getApplicationContext());
			}			
			return allForecasts;			
		}
	}
}