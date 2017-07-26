package com.example.shivam.youtubeproject;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

public class MainActivity extends YouTubeBaseActivity {

    private YouTubePlayerView ypview;
    private YouTubePlayer.OnInitializedListener onInitializedListener;
    private Button b, stop;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private Button recordButton;
    private YouTubePlayer yPlayer;
    private long t = 0;
    private ListView lv;
    private String URL;
    private String my_youtube_api_key = "your_api_key";
    private boolean isInitialised;
    List<String> your_array_list;
    ArrayList<CommentsVoiceNote> commentsArrayList;
    CommentsVoiceNoteAdapter commentAdapter;

    JSONArray commentStored;
    JSONArray youTubeIDStored;
    JSONArray durationStored;

    Databasehelper dbhelper;
    private SharedPreferences.Editor editor;

    private SharedPreferences mSharedPreference;

    JSONObject json;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbhelper = new Databasehelper(this);
        isInitialised = false;

        ypview = (YouTubePlayerView) findViewById(R.id.youtubeVideoPlayer);
        recordButton = (Button) findViewById(R.id.recordButton);
        stop = (Button) findViewById(R.id.stopButton);

        your_array_list = new ArrayList<String>();

        //creating a database with a dummy value when the user installs for the first time
        if(mSharedPreference.getString("FirstTime", "True") == "True"){
            storeData("Data");
            editor.putString("FirstTime", "False");
            editor.commit();
        }

        lv = (ListView) findViewById(R.id.voiceNoteList);

        commentsArrayList = new ArrayList<CommentsVoiceNote>();

        commentAdapter = new CommentsVoiceNoteAdapter(this, commentsArrayList);

        lv.setAdapter(commentAdapter);

        json = new JSONObject();


        //when the user clicks on the particular item on the list, the youtube player seeks to the particular time after getting the youTubeID and Duration of that item
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                CommentsVoiceNote products = commentsArrayList.get(position);

                String sku = products.getComment();
                String youtubeID = products.getYoutubeID();
                long time = products.getDuration();

                yPlayer.loadVideo(youtubeID, (int) time);

            }


        });

        onInitializedListener = new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
                yPlayer = youTubePlayer;
                //Fetching the URL sharedpreference coming from intro screen
                String ypURL = mSharedPreference.getString("URL", "youtube");
                yPlayer.loadVideo(ypURL);
                URL=ypURL;
                isInitialised = true;
                loadComments(URL);
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {

            }
        };

        //initaialising the youtubeplayerview with the api

        ypview.initialize(my_youtube_api_key, onInitializedListener);

        b = findViewById(R.id.buttonPlay);

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                yPlayer.seekToMillis(0);
                yPlayer.pause();
            }
        });

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(yPlayer.isPlaying()){
                    yPlayer.pause();
                }
                else if(!yPlayer.isPlaying()) {
                    yPlayer.play();
                }
                else{
                    yPlayer.loadVideo(URL);
                }
            }
        });

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               if(isInitialised) {
                   promptSpeechInput();
                   t = yPlayer.getCurrentTimeMillis();
               }

               else{
                   Toast.makeText(MainActivity.this, "Wait for a moment", Toast.LENGTH_SHORT).show();
               }
            }
        });

    }

    //releasing the youtubeplayer when the user goes back
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        yPlayer.release();
    }


    //SpeechToText intent is called
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    String responseString = result.get(0);

                    //addComment is called after the voice note is is converted into text and that text, youtubeID, and the time it was recorder are passed as params
                    addComment(responseString, URL, (int) yPlayer.getCurrentTimeMillis());
                }
                break;
            }

        }
    }

    //add comment function to add the Comment, youtubeID, and time hit as params and then it gets added to the JSON
    void addComment(String comment, String youtubeurl, long timeSeek)   {
        try {

            json.accumulate("Comments", comment);
            json.accumulate("YoutubeID", youtubeurl);
            json.accumulate("duration", timeSeek);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        dbhelper.updateTable(json.toString());

        loadComments(URL);

    }


    /*
    loadComments to populate the listview according to the youtubeID user has entered, fetching the Comments, YoutubeID, and Duration from JSON
    where youtubeID matches the one entered by user
    */
    void loadComments(String url) {

        try {
            json = new JSONObject(getData());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            commentStored = json.getJSONArray("Comments");
            youTubeIDStored = json.getJSONArray("YoutubeID");
            durationStored = json.getJSONArray("duration");
            commentsArrayList = new ArrayList<>();
            for (int i = 0; i < commentStored.length(); i++) {

                if (youTubeIDStored.get(i).toString().equalsIgnoreCase(url)) {
                    commentsArrayList.add(new CommentsVoiceNote(commentStored.get(i).toString(), youTubeIDStored.get(i).toString(), Long.parseLong(durationStored.get(i).toString())));
                }
            }
            commentAdapter = new CommentsVoiceNoteAdapter(this, commentsArrayList);
            lv.setAdapter(commentAdapter);


        } catch (JSONException e) {
            try {
                String commentStored2 = json.get("Comments").toString();
                String youTubeIDstored2 = json.get("YoutubeID").toString();
                String durationStored2 = json.get("duration").toString();

                commentsArrayList = new ArrayList<>();
                if(youTubeIDstored2.equalsIgnoreCase(url)) {
                    commentsArrayList.add(new CommentsVoiceNote(commentStored2, youTubeIDstored2, Long.parseLong(durationStored2)));
                }
                commentAdapter = new CommentsVoiceNoteAdapter(this, commentsArrayList);
                lv.setAdapter(commentAdapter);

            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }

    }

    // storeData function to store the JSON in string format to the database in phone memory
    //the data is stored in string which is JSON which contains Comments, YoutubeID and duration in a serialised way
    public void storeData(String newData){
        dbhelper.addData(newData);
    }


    //getData function to fetch the first value from the database which is string
    public String getData()   {

        String n = "NO COMMENT";

        Cursor data = dbhelper.getData();
        if (data != null && data.moveToFirst());
        {
            n = String.valueOf(data.getString(1));
         }

         return n;
    }
}

