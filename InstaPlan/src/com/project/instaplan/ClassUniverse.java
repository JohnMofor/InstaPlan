package com.project.instaplan;

import java.util.ArrayList;
import java.util.HashMap;

import android.util.SparseArray;

public class ClassUniverse {

	static boolean GCMEnabled = false;
	public static boolean GCMPossible = false;
	public static String device_id = "";
	public static String mUserName = "";
	public static String regId = "";
	public static String GcmRegError = "";
	public static String mPhoneNumber = "";
	public static String mEmail = "";
	public static int numberOfAllMyEventsEverCreated = 1;
	public static boolean phoneUnlocked= false;
	public static String registrationTag="Welcome to Instaplan!";
	

	static ArrayList<ClassEvent> universeListOfAllEvents = new ArrayList<ClassEvent>();
	static ArrayList<ClassEvent> allUpdates = new ArrayList<ClassEvent>();
	static ArrayList<ClassEvent> universeListOfAllMyEvents = new ArrayList<ClassEvent>();
	static ArrayList<ClassEvent> universeListOfAllOthersEvents = new ArrayList<ClassEvent>();

	public static HashMap<String, ClassPeople> universePhoneNumberLookUp = new HashMap<String, ClassPeople>();
	// public static HashMap<String, ClassPeople> universeEmailLookUp = new
	// HashMap<String, ClassPeople>();
	// public static HashMap<String, ClassPeople> universeFacebookIDLookUp = new
	// HashMap<String, ClassPeople>();
	// public static HashMap<String, ClassPeople> universeNameLookUp = new
	// HashMap<String, ClassPeople>();

	public static SparseArray<ClassEvent> universeAllEventHashLookUp = new SparseArray<ClassEvent>();
	public static SparseArray<ClassEvent> universeAllMyEventCreationNumberLookUp = new SparseArray<ClassEvent>();

	public static boolean registerEvent(ClassEvent new_event) {
		int eventHash = (new_event.title + new_event.host.phoneNumber)
				.hashCode();
		eventHash = (eventHash < 0 ? -eventHash:eventHash);
		if ((universeAllEventHashLookUp.get(eventHash) == null)) {
			//Add to look-up
			universeAllEventHashLookUp.put(eventHash, new_event);
			//Add to Arrays... (why??-I'll take it off)
			universeListOfAllEvents.add(new_event);
			//If mine, set stuffs... :)
			if (new_event.isMine) {
				universeListOfAllMyEvents.add(new_event);
				new_event.creationNumber = numberOfAllMyEventsEverCreated;
				universeAllMyEventCreationNumberLookUp.put(numberOfAllMyEventsEverCreated, new_event);
				numberOfAllMyEventsEverCreated++;
				return true;
			}else{
				universeListOfAllOthersEvents.add(new_event);
			}
		}
		return false;
	}
}
