package com.project.instaplan;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;

public class Intro extends Activity{
	MediaPlayer aSong;
	String tag = "MJ------>";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_intro);
		Log.i(tag,"In Intro");
		aSong = MediaPlayer.create(Intro.this, R.raw.kalimba);
		aSong.start();
		
		Thread timer = new Thread(){
			public void run(){
				try{
					sleep(2000);					
				}catch(InterruptedException error){
					error.printStackTrace();
				}finally{
					Intent showMenu = new Intent("com.project.instaplan.Menu");
					startActivity(showMenu);
				}
			}
		};
		timer.start();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		aSong.release();
		finish();
	}
	

}
