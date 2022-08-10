package com.cs380.blackjackgame;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
	private static final boolean attemptToActuallyKillApp = true;

	public Button Play_BJ;
	public Button Profile_Page;
	public Button dialogue;
	public ImageButton Play;
	public ImageButton MuteMusic;
	MediaPlayer player;

	SharedPreferences curProf;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Play_BJ = findViewById(R.id.Play_BJ);
		Play_BJ.setOnClickListener((View v) -> openPlay_Blackjack());

		Profile_Page = findViewById(R.id.Profile_Page);
		Profile_Page.setOnClickListener((View v) -> open_Profiles());

		Play.findViewById(R.id.Play);
		Play.setOnClickListener((View v) -> playMusic());

		MuteMusic.findViewById(R.id.MuteMusic);
		MuteMusic.setOnClickListener((View v) -> MuteMusic());


		curProf = getSharedPreferences("CurrentProfile", 0);

		//makes a default profile when the app is opened if there is no default profile
		if (!curProf.contains("profile")) {
			makeDefaultProfile();
		}

		dialogue = findViewById(R.id.help_button);
		dialogue.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showPopUP();
			}
		});


	}

	public void showPopUP() {
		//create new dialog that sets the popup view the designated xml file and sets background as well.
		Dialog dialog = new Dialog(this, R.style.dialog_popup);
		dialog.setContentView(R.layout.popup_dialogue);
		dialog.getWindow().setBackgroundDrawableResource(R.drawable.background_color);

		//blurring background by retrieving parameters for window
		//WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
		//set dim amount to 0
		//lp.dimAmount = 0.6f;
		//update parameters
		//dialog.getWindow().setAttributes(lp);
		//dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
		//background is made translucent in themes.xml

		dialog.show();
	}

	// Taken from 02-22-2022 13:43:33 PST: https://stackoverflow.com/a/24359962 and https://stackoverflow.com/a/26608409
	@Override
	public void onBackPressed() {
//		super.onBackPressed();
		// TODO: Prompt user if they want to close the app.

		this.finishAffinity();

		if (attemptToActuallyKillApp) {
			Log.i(this.getClass().getSimpleName(), "Attempted to kill app.");

			int pid = android.os.Process.myPid();
			android.os.Process.killProcess(pid);

			System.exit(0);
		} else {
			Log.i(this.getClass().getSimpleName(), "Closed app.");
		}
	}

	public void openPlay_Blackjack() {
		Intent intent = new Intent(this, Play_Blackjack.class);
		startActivity(intent);
	}

	public void open_Profiles() {
		Intent intent = new Intent(this, profile.class);
		startActivity(intent);
	}

	public void makeDefaultProfile() {
		SharedPreferences.Editor curProfEdit = curProf.edit();
		SharedPreferences settings = getSharedPreferences("DEFAULT", 0);
		SharedPreferences.Editor edit = settings.edit();

		curProfEdit.putString("profile", "DEFAULT");
		curProfEdit.putString("profile0", "DEFAULT");
		curProfEdit.putInt("numProfiles", 1);
		curProfEdit.commit();

		edit.putLong("bank", 500000);
		edit.putInt("bots", 0);
		edit.putInt("decks", 2);
		edit.putBoolean("music", true);
		edit.putBoolean("sound", true);
		edit.commit();

	}

	public void playMusic(){
		Play.setVisibility(View.INVISIBLE);
		MuteMusic.setVisibility(View.VISIBLE);
		if (player == null){
			player = MediaPlayer.create(this, R.raw.elevator);
		}
		player.start();
		player.setLooping(true);
	}
	public void MuteMusic() {
		MuteMusic.setVisibility(View.INVISIBLE);
		Play.setVisibility(View.VISIBLE);
		if (player != null) {
			player.release();
			player.setLooping(false);
			player = null;
		}
	}


}