package com.project.instaplan;


import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.Bundle;

public class RuntimeStatusCheck extends Activity implements View.OnClickListener {

	// Instantiate ALl Public Variables Here.
	LinearLayout runtime_status_check_layout;
	Button runtime_status_check_update_button;
	TextView runtime_status_check_new_post;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO
		setContentView(R.layout.layout_runtime_status_check);
		initializeAllVariables();

		// Give the rest of the functions.
		runtime_status_check_update_button.setOnClickListener(this);
	}

	private void initializeAllVariables() {
		runtime_status_check_layout = (LinearLayout) findViewById(R.id.runtime_status_check_layout);
		runtime_status_check_update_button = (Button) findViewById(R.id.runtime_status_check_update_button);		
	}

	public void onClick(View viewClicked) {
		switch (viewClicked.getId()) {

		case R.id.runtime_status_check_update_button:
			// Enter code for this button.
			String post_content = "";
			for(ClassEvent event: ClassUniverse.universeListOfAllEvents){
				post_content+="\n----------------------------------------";
				post_content+="\nTitle:"+event.title;
				post_content+="\nTime: "+event.time +" Date: " +event.date;
				post_content+="\nLocation: "+event.location;
				post_content+="\nDescription: "+event.description;
				post_content+="\nIs Mine?: "+event.isMine;
				post_content+="\n---------------Invited------------------";
				post_content+="\nName----------Contact";
				for(ClassPeople person: event.invited){
				post_content+="\n"+person.name+"---------"+person.getContact();
				}
			}
			runtime_status_check_new_post = new TextView(this);
			runtime_status_check_new_post.setLayoutParams(new ViewGroup.LayoutParams(-1,-2));
			runtime_status_check_new_post.setText("\nNew Report:-----------------------------\n"+post_content);
			runtime_status_check_layout.addView(runtime_status_check_new_post, 0);
//			setContentView(runtime_status_check_layout)
			break;

		// ADD MORE button CASESE:
		}
	}

	@Override
	protected void onPause() {
		// TODO
		super.onPause();
	}
}
