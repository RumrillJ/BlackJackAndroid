package com.cs380.blackjackgame;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


public class profile extends AppCompatActivity {

    public Button apply;
    public Button chgPf;
    public Button bailMe;
    public Button back;

    public TextView name;
    public TextView bank;

    public EditText botPlayers;
    public EditText decksNum;

    public Switch sound;
    public Switch music;

    public boolean soundState;
    public boolean musicState;
    MediaPlayer player;

    //STRUCTURE INFO:
    //I have two SharedPreferences here:
    //curProf keeps track of the total number of profiles, the currently selected profile, and each profile's name
    //settings stores the actual settings of each profile which can be fetched when getting the sharedPreferences associated with the name of the profile
    //curProf will always = getSharedPreferences("CurrentProfile", 0); since that's the preferences where all that is needed for it is stored
    //settings will = getSharedPreferences(<INSERT PROFILE NAME HERE>, 0); to fetch whatever settings are needed

    //you can fetch the current profile by calling curProfile.getString("profile", "") and storing it whatever temporary string you want.
    //you can fetch a setting from the current profile by entering the name of the setting
    //settings.getBoolean("sound", true) will return the current setting for if sound is enabled, for example.

    //current settings are:
    //curProf:
    //String profile        -the currently selected profile
    //int numProfiles       -the current total number of profiles
    //String "profile"+n    -where n is some int, where each profile's name is stored, you can iterate over these if need be with numProfiles
    //
    //settings:
    //long bank             -the total money the current profile has in the bank
    //int bots              -the number of bots the current profile has set
    //int decks             -the number of decks the current profile has set
    //boolean music         -whether music is currently toggled on or off
    //boolean sound         -whether sound is currently toggled on or off
    //
    public SharedPreferences settings;
    public SharedPreferences.Editor edit;

    public SharedPreferences curProf;

    public String profileName;

    public static final String classLogName = profile.class.getSimpleName() + ".java";

    public InputMethodManager imm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);

        apply = findViewById(R.id.apply);
        apply.setOnClickListener(view -> {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            pressApply();
        });

        chgPf = findViewById(R.id.changeProfile);
        chgPf.setOnClickListener(view -> open_selectProfile());

        bailMe = findViewById(R.id.bailOut);
        bailMe.setOnClickListener(view -> bailOut());

        back = findViewById(R.id.back);
        back.setOnClickListener(view -> exitProfile());

        name = findViewById(R.id.profileName);
        bank = findViewById(R.id.bankNum);

        botPlayers = findViewById(R.id.BotPlayers);
        decksNum = findViewById(R.id.decksNum);

        sound = findViewById(R.id.soundToggle);
        music = findViewById(R.id.musicToggle);

        curProf = getSharedPreferences("CurrentProfile", 0);


        //if there is a current profile set, load it
        //if not create a default profile
        if(curProf.contains("profile")){
            profileName = curProf.getString("profile", "");
            settings = getSharedPreferences(profileName, 0);
            edit = settings.edit();

            //if the current profile is initialized, load the settings
            //if not apply the default settings to the profile
            if(settings.contains("bank")){
                loadSettings();
            } else{
                Log.i(classLogName, "Reached Load Default Settings, THIS SHOULDN'T HAPPEN!");
                loadDefaultSettings();  //if everything is going well, this shouldn't be reached
            }
        }else{
            Log.i(classLogName, "Reached Create Default Profile, THIS SHOULDN'T HAPPEN!");
            createDefaultProfile(); //this also shouldn't be reached
        }

        sound.setOnCheckedChangeListener(
                (buttonView, isChecked) -> soundState = isChecked);

        music.setOnCheckedChangeListener(
                (buttonView, isChecked) -> musicState = isChecked);

        //TODO: add ability to change profile name
        //TODO: add ability to delete profile

    }

    public void open_selectProfile() {
        Intent intent = new Intent(this, selectprofile.class);
        startActivity(intent);
    }

    public void pressApply(){

        //apply/save stuff here

        String check = botPlayers.getText().toString();
        int bots = 0;
        if(!check.equals("")){
            bots = Integer.parseInt(check);
        }


        check = decksNum.getText().toString();
        int decks = 2;
        if(!check.equals("")){
            decks = Integer.parseInt(check);
        }


        edit.putInt("bots", bots);
        edit.putInt("decks", decks);
        edit.putBoolean("music", musicState);
        edit.putBoolean("sound", soundState);
        edit.commit();

        Toast.makeText(this,"Updated Profile", Toast.LENGTH_SHORT).show();
    }

    public void bailOut(){

        //set the current bank to $5000 if it's less than $5000
        long temp = settings.getLong("bank", 500000);

        if(temp < 500000){
            edit.putLong("bank", 500000);
            edit.commit();

            loadSettings();

            String difference = String.format("%.2f", (500000 - temp)/100.00);
            Toast.makeText(this,"Bank has been given: $" + difference, Toast.LENGTH_SHORT).show();
        } else{
            Toast.makeText(this,"Bank not low enough!", Toast.LENGTH_SHORT).show();
        }

    }

    //this shouldn't be called, the default settings should be pre-loaded in selectprofile when the profile is created, here as backup
    public void loadDefaultSettings(){
        edit.putLong("bank", 500000);
        edit.putInt("bots", 0);
        edit.putInt("decks", 2);
        edit.putBoolean("music", true);
        edit.putBoolean("sound", true);
        edit.commit();

        loadSettings();
    }

    //this shouldn't be called, the default profile should only need to happen on the title screen. this is just present as backup
    public void createDefaultProfile(){
        SharedPreferences.Editor curProfEdit = curProf.edit();
        curProfEdit.putString("profile", "DEFAULT");
        curProfEdit.putString("profile0", "DEFAULT");
        curProfEdit.putInt("numProfiles", 1);
        curProfEdit.commit();

        profileName = "DEFAULT";
        settings = getSharedPreferences(profileName, 0);
        edit = settings.edit();


        loadDefaultSettings();
    }

    public void loadSettings(){
        name.setText(profileName);

        double bankVal = settings.getLong("bank", 500000)/100.00;
        String bankDisplay = String.format("%.2f",bankVal);
        bank.setText(bankDisplay);

        botPlayers.setText("" + settings.getInt("bots", 0));
        decksNum.setText("" + settings.getInt("decks", 2));

        musicState = settings.getBoolean("music", true);
        soundState = settings.getBoolean("sound", true);

        music.setChecked(musicState);
        sound.setChecked(soundState);
    }

    public void exitProfile(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        exitProfile();
    }

}