package com.project.instaplan;



import java.util.ArrayList;
import java.util.HashMap;

public class ClassUniverse {

	static ArrayList<ClassEvent> universeListOfAllEvents = new ArrayList<ClassEvent>();
	static ArrayList<ClassEvent> allUpdates = new ArrayList<ClassEvent>();
	
	
	static boolean GCMEnabled=false;
	public static boolean GCMPossible=false;
	

	public static HashMap<String, ClassPeople> universePhoneNumberLookUp = new HashMap<String, ClassPeople>();
	public static HashMap<String, ClassPeople> universeEmailLookUp = new HashMap<String, ClassPeople>();
	public static HashMap<String, ClassPeople> universeFacebookIDLookUp = new HashMap<String, ClassPeople>();
	public static HashMap<String, ClassPeople> universeNameLookUp = new HashMap<String, ClassPeople>();
	public static HashMap<String, ClassEvent> universeEventLookUp = new HashMap<String, ClassEvent>();
	
	public static String device_id="";
	public static String mUserName="";
	public static String regId="";
	public static String GcmRegError="";
	public static String mPhoneNumber="";
	public static String mEmail="";

	public static void createEvent(ClassEvent new_event) {
		if (!universeListOfAllEvents.contains(new_event)) {
			universeListOfAllEvents.add(new_event);
			universeEventLookUp.put(new_event.title, new_event);
		}
	}

	public static String isPersonParticipatingInEvent(String inputContactType,
			String inputContact, String eventTitle) {
		ClassPeople person = null;
		ClassEvent event = null;
		if (universeEventLookUp.containsKey(eventTitle)) {
			event = universeEventLookUp.get(eventTitle);
		}
		if (inputContactType.contentEquals("phoneNumber")) {
			if (universePhoneNumberLookUp.containsKey(inputContact)) {
				person = universePhoneNumberLookUp.get(inputContact);
			}
		}
		if (inputContactType.contentEquals("facebookID")) {
			if (universeFacebookIDLookUp.containsKey(inputContact)) {
				person = universeFacebookIDLookUp.get(inputContact);
			}
		}
		if (inputContactType.contentEquals("email")) {
			if (universeEmailLookUp.containsKey(inputContact)) {
				person = universeEmailLookUp.get(inputContact);
			}
		}
		if (inputContactType.contentEquals("name")) {
			if (universeNameLookUp.containsKey(inputContact)) {
				person = universeNameLookUp.get(inputContact);
			}
		}
		if ((person == null) && (!(event == null))) {
			return "InValid Person But valid Event";
		}
		if ((!(person == null)) && (event == null)) {
			return "Valid Person But Invalid Event";
		}
		if (!((person == null) && (event == null))) {
			return Boolean.toString(event.invited_look_up.containsKey(person.phoneNumber));
		}
		return "Invalid Person & Event";
	}
}
