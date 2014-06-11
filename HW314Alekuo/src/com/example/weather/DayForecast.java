package com.example.weather;

import android.content.Context;
import android.widget.ImageView;

/**
 * This class stores and manipulates the information for the daily forecast,
 * and assigns the appropriate graphics, as defined in the Yahoo API. 
 * @author Alena
 *
 */
public class DayForecast {

    public int forecastID;    
    public String forecastDay;
    public String forecastDate;
    public String forecastLow;
    public String forecastHigh;
    public String forecastText;
    public int forecastCode;
	
    public ImageView forecastIcon;
    
    // The weather codes run form 0 to 47.
    // Each index in the array element is the Yahoo weather code,
    // and each string is the name of the corresponding drawable.
    public static String[] weatherCodes = {
    	"windy", 
    	"thundersun",
    	"windy",
    	"thunderday",
    	"thunderday", //4
    	"rainsnow",
    	"rainsnow",
    	"snow",
    	"rainyicy",
    	"mist", // 9
    	"rainice",
    	"rainsun",
    	"rainnight",
    	"mistsnow",
    	"mistsnow", // 14
    	"snowwind",
    	"snow",
    	"hail",
    	"rainice",
    	"dust", // 19
    	"fog",
    	"dust",
    	"dust",
    	"windy",
    	"windy", // 24
    	"ice",
    	"cloudy",
    	"cloudynight",
    	"cloudyday",
    	"partcloudnight", // 29
    	"partcloudday",
    	"clearnight",
    	"clearday",
    	"clearnight",
    	"clearday", // 34
    	"rain",
    	"hot",
    	"thunderday",
    	"thundersun",
    	"thundernight", // 39
    	"rain", 
    	"snow",
    	"rainsnow",
    	"snownight",
    	"partcloudday", // 44
    	"thunderday", 
    	"rainsnow",
    	"thundersun", // 47
    };
    
	
	@SuppressWarnings("deprecation")
	
	public DayForecast (Context c) {
		forecastDay = "EMPTY";
		forecastDate = "EMPTY";
		forecastLow = "EMPTY";
		forecastHigh = "EMPTY";
		forecastText = "EMPTY";
		forecastCode = 3200;
		
		forecastIcon = new ImageView(c);
		forecastIcon.setBackgroundDrawable(c.getResources().getDrawable(R.drawable.none));
	}
    
    @SuppressWarnings("deprecation")
	public void setIcon(Context c, ImageView view, int condition) {
    	if(condition >= 0 && condition <= 47) {
    		int resID = c.getResources().getIdentifier(DayForecast.weatherCodes[condition] , "drawable", c.getPackageName());
    		view.setBackgroundDrawable(c.getResources().getDrawable(resID));
    	} else {
       		view.setBackgroundDrawable(c.getResources().getDrawable(R.drawable.none));
    	}
    }
}
