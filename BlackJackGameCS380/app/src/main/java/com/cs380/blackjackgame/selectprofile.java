package com.cs380.blackjackgame;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.Locale;
import java.util.regex.Pattern;

public class selectprofile extends AppCompatActivity {

    LinearLayout profileButtons;
    int profileNum;

    public SharedPreferences curProf;

    public Button back;
    public Button makeProfile;
    public Button nameEntry;

    public RelativeLayout inputScreen;

    public InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectprofile);

        curProf = getSharedPreferences("CurrentProfile", 0);
        profileNum = curProf.getInt("numProfiles", 0);

        imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);

        profileButtons = findViewById(R.id.profileButtons);
        inputScreen = findViewById(R.id.nameInputScreen);

        updateProfileList();

        back = findViewById(R.id.backToProfile);
        back.setOnClickListener(view -> toProfile());

        makeProfile = findViewById(R.id.createNew);
        makeProfile.setOnClickListener(view -> createProfileVisible());

        nameEntry = findViewById(R.id.enterName);
        nameEntry.setOnClickListener(view -> {
            createProfile();
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        });

    }

    //sets the given name string as the current profile
    public void setProfile(String name){
        Toast.makeText(this,(name + " now active!"), Toast.LENGTH_SHORT).show();
        SharedPreferences.Editor curProfEdit = curProf.edit();

        curProfEdit.putString("profile", name);
        curProfEdit.commit();
    }

    public void updateProfileList(){
        //removes all views in the linearlayout within the scrolling layout from the second one to the last one
        //this is so "create new profile" never gets removed, just all the currently populated profiles
        profileButtons.removeViewsInLayout(1, profileButtons.getChildCount()-1);

        //iterates through the number of profiles, fetching their names and displaying them on new buttons in the scroll layout
        for(int i = 0; i < profileNum; i++){
            Button test = new Button(this);

            String profN = "profile"+i;
            String name = curProf.getString(profN, "ERROR").toString();

            test.setText(name);

            //this sets the profile to the name on the current button by fetching the text that was placed on the button and sending it to setProfile
            //this gets around needing to keep some complicated structure of all the names to profiles for the onclick to use
            test.setOnClickListener(view -> setProfile(test.getText().toString()));

            //this makes the buttons no longer exclusively capitalized when displayed
            test.setTransformationMethod(null);

            profileButtons.addView(test);

        }

    }

    public void createProfileVisible(){
        inputScreen.setVisibility(View.VISIBLE);
    }

    public void createProfile(){
        EditText nameField = findViewById(R.id.profileNameInput);
        String check = nameField.getText().toString();

        if(!check.equals("")){
            //this uses regex to determine if a string has any non alphanumeric characters
            Pattern regex = Pattern.compile("[^a-zA-Z0-9]");
            boolean hasSpecialChar = regex.matcher(check).find();

            //this makes sure the given name is alphanumeric, putting up an error toast if not
            if(!hasSpecialChar){
                boolean exists = false;

                //this checks the currently existing profiles for one that shares a name (with any capitalization)
                for(int n = 0; n < profileNum; n++){
                    String profCheck = "profile" + n;
                    String nameCheck = curProf.getString(profCheck, "ERROR").toLowerCase(Locale.ROOT);
                    String temp = check.toLowerCase(Locale.ROOT);

                    if(nameCheck.equals(temp)){
                        exists = true;
                    }
                }

                //if a profile with that name already exists, don't allow the creation of a new one.
                if(!exists){
                    //set new profile as the current profile
                    String profInput = "profile" + profileNum;
                    SharedPreferences.Editor curProfEdit = curProf.edit();

                    curProfEdit.putString(profInput, check);
                    curProfEdit.putString("profile", check);
                    curProfEdit.commit();

                    //sets all the default values in that profile's settings
                    setDefaults(check);

                    inputScreen.setVisibility(View.INVISIBLE);

                    //increase the number of profiles stored
                    profileNum++;
                    curProfEdit.putInt("numProfiles", profileNum);
                    curProfEdit.commit();

                    Toast.makeText(this,(check + "is now your current profile!"), Toast.LENGTH_SHORT).show();

                    //update the profile list to show the new profile
                    updateProfileList();
                }
                else{
                    Toast.makeText(this,("Profile already exists!"), Toast.LENGTH_SHORT).show();
                }


            }else{
                Toast.makeText(this,("Alphanumeric characters only!"), Toast.LENGTH_SHORT).show();
            }
        }

        //clears the input field for next entry
        nameField.setText("");
    }


    public void setDefaults(String prof){
        SharedPreferences settings = getSharedPreferences(prof, 0);
        SharedPreferences.Editor edit = settings.edit();

        edit.putLong("bank", 500000);
        edit.putInt("bots", 0);
        edit.putInt("decks", 2);
        edit.putBoolean("music", true);
        edit.putBoolean("sound", true);
        edit.commit();

    }


    public void toProfile(){
        if(inputScreen.getVisibility() == View.VISIBLE){
            inputScreen.setVisibility(View.INVISIBLE);
        }else{
            Intent intent = new Intent(this, profile.class);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        toProfile();
    }
}

