package edu.csumb.weather;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;

import java.net.URL;
import java.util.Scanner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import edu.csumb.weather.domain.Weather;

public class MainActivity extends AppCompatActivity {

    static final String TAG = "MainActivity";
    static final String OPENWEATHER = "http://api.openweathermap.org/data/2.5/weather";
    static final String APIKEY = "4cdc90111e0528db2d929c9090ee6e3c";

    Handler handler;
    String city_name;
    AlertDialog dialog;
    TextView output_message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        handler = new Handler();

        output_message = findViewById(R.id.city_weather);
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get the city name entered by user
                EditText city = findViewById(R.id.city);
                city_name = city.getText().toString().trim();
                // create and show an AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Please wait. Fetching weather for "+ city_name);
                dialog =  builder.create();
                dialog.show();
                // this prevents user from submitting another request until this request finishes
                // create and start background task to get weather info from server
                Thread t = new Thread(new WorkerTask());
                t.start();
            }
        });
    }

    /**
     * background task to retrieve weather info from weather server
     */
    public class WorkerTask implements Runnable {
        String message_text;

        @Override
        public void run() {
            Log.d(TAG, "WorkerTask started.");
            try {
                Thread.sleep(5000);  //  delay to show dialog
                // get weather data for city.  reply is JSON string.
                String weather_json = getWeather(city_name);
                // extract necessary data from reply string and convert to double
                Gson gson = new Gson();
                Weather weather = gson.fromJson(weather_json, Weather.class);
                // TODO complete this code
                double deg_F = (weather.getMain().getTemp()-273.15)*9.0/5.0+32.0;
                message_text = String.format("Current temp is %.0f \u00B0F", deg_F);
            } catch (Exception e) {
                Log.d(TAG, "exception WorkerTask.run " + e.getMessage());
                message_text = "Error. Enter a valid city name.";
            }
            //  we are done. so dismiss the progress dialog
            //  and update layout textview with current temp
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "handler called.");
                    output_message.setText(message_text);
                    // dismiss the dialog and let user enter another request
                    dialog.dismiss();
                }
            });
            Log.d(TAG, "WorkerTask ending.");
        }



        /**
         * Given name of city, return weather information in JSON format
         *
         * @param city name
         * @return string value containing JSON data from server.  null value if there is error.
         */
        private String getWeather(String city) {
            Scanner reader = null;
            String urlstring = OPENWEATHER+"?q=" + city + "&appid=" + APIKEY;
            Log.d(TAG, "getWeather " + urlstring);

            try {
                URL url = new URL(urlstring);
                reader = new Scanner(url.openConnection().getInputStream());
                StringBuffer sb = new StringBuffer();
                while (reader.hasNext())
                    sb.append(reader.nextLine());
                Log.d(TAG, "getWeather end");
                String result = sb.toString();
                Log.d(TAG, result);
                return result;
            } catch (Exception e) {
                Log.d(TAG, "getWeather exception " + e.getMessage());
                return null;
            } finally {
                if (reader != null) reader.close();
            }
            }
    }
}