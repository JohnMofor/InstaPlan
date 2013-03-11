package com.project.instaplan;



import java.util.ArrayList;
import java.util.HashMap;

public class ClassUniverse {

	static ArrayList<ClassEvent> universeListOfAllEvents = new ArrayList<ClassEvent>();
	static HashMap<String, ClassEvent> universeEventLookUp = new HashMap<String, ClassEvent>();

	static HashMap<String, ClassPeople> universePhoneNumberLookUp = new HashMap<String, ClassPeople>();
	static HashMap<String, ClassPeople> universeEmailLookUp = new HashMap<String, ClassPeople>();
	static HashMap<String, ClassPeople> universeFacebookIDLookUp = new HashMap<String, ClassPeople>();
	static HashMap<String, ClassPeople> universeNameLookUp = new HashMap<String, ClassPeople>();
	static ArrayList<ClassEvent> allUpdates = new ArrayList<ClassEvent>();

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
			return Boolean.toString(event.invited_look_up.containsKey(person.name));
		}
		return "Invalid Person & Event";
	}
}
