package com.example.shivam.youtubeproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class introScreen extends AppCompatActivity {

    private EditText URLEditText;
    private String URL;
    private Button playIntroScreen;
    private SharedPreferences mSharedPreference;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro_screen);

        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());

        editor = mSharedPreference.edit();

        URLEditText = (EditText) findViewById(R.id.URLeditText);
        playIntroScreen = (Button) findViewById(R.id.playIntroScreen);

        playIntroScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(introScreen.this, MainActivity.class);

                URL = URLEditText.getText().toString();

                //Checking the length of the URL, youtube contains 7, so if it is less than this, it shows invalid
                if (URL.length() > 7) {
                    if (URL.contains(".be")) {
                        String[] yID = URL.split(".be/");
                        URL = yID[1];
                        Log.e("m", URL);
                        startActivity(i);
                    } else if (URL.contains("v=")) {
                        String[] yID = URL.split("v=");
                        URL = yID[1];
                        Log.e("m", URL);
                        startActivity(i);
                    } else {
                        Toast.makeText(introScreen.this, "Enter a valid URL", Toast.LENGTH_SHORT).show();
                    }

                    //setting the sharedpreference URL by the youtubeID
                    editor.putString("URL", URL);
                    editor.commit();


                }

                else{
                    Toast.makeText(introScreen.this, "Enter a valid URL", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
