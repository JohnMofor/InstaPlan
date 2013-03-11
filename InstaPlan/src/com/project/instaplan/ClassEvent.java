package com.project.instaplan;


import java.util.ArrayList;
import java.util.HashMap;

public class ClassEvent {

	String title, location, description, time, date;
	ArrayList<ClassPeople> attendees = new ArrayList<ClassPeople>();
	ArrayList<ClassPeople> invited = new ArrayList<ClassPeople>();
	ArrayList<String> chat_log = new ArrayList<String>();
	int chatroom_size = 0;
	int externalUpdateCount = 0;
	int chatroomCursor = 0;
	int eventCode=ClassUniverse.universeListOfAllEvents.size()+1;
	ClassPeople host;
	Boolean isFacebookEnabled = false;
	HashMap<String, ClassPeople> invited_look_up = new HashMap<String, ClassPeople>();
	HashMap<String, ClassPeople> attendee_look_up = new HashMap<String, ClassPeople>();

	public static void main(String[] args) {

	}

	/**
	 * @param Title
	 *            of Event
	 * @param Event
	 *            Location
	 * @param Event
	 *            Description
	 * @param Time
	 *            of Event
	 * @param Date
	 *            of Event
	 * @param Event
	 *            Host
	 */
	public ClassEvent(String a_title, String a_location, String a_description,
			String a_time, String a_date) {

		title = a_title;
		location = a_location;
		description = a_description;
		time = a_time;
		date = a_date;
	}

	public void addAttendee(ClassPeople new_person) {
		// TODO Auto-generated method stub
		attendees.add(new_person);
		attendee_look_up.put(new_person.name, new_person);
		if (!invited_look_up.containsKey(new_person.name)) {
			invite(new_person);
		}
	}

	public void makeHost(ClassPeople new_person) {
		host = new_person;
		if (!host.allEventsParticipating.contains(this)) {
			host.allEventsParticipating.add(this);
		}
		addAttendee(host);
	}

	public void invite(ClassPeople new_person) {
		if (!invited_look_up.containsKey(new_person.name)) {
			invited.add(new_person);
			invited_look_up.put(new_person.name, new_person);
			startListeningTo(new_person);
		}
		if (!new_person.allEventsParticipating.contains(this)) {
			new_person.allEventsParticipating.add(this);
		}
	}

	public void startListeningTo(ClassPeople new_person) {
		// TODO Auto-generated method stub
		if (new_person.hasEmail()) {
			ClassUniverse.universeEmailLookUp.put(new_person.email, new_person);
		}
		if (new_person.hasFacebookID()) {
			ClassUniverse.universeFacebookIDLookUp.put(new_person.facebookID,
					new_person);
		}
		if (new_person.hasPhoneNumber()) {
			ClassUniverse.universePhoneNumberLookUp.put(new_person.phoneNumber,
					new_person);
		}
		ClassUniverse.universeNameLookUp.put(new_person.name, new_person);

	}

	public void enableFacebook(Boolean status) {
		isFacebookEnabled = true;
	}

	public ArrayList<ClassPeople> getAttendees() {
		return attendees;
	}

	public ArrayList<ClassPeople> getIntived() {
		return invited;
	}

	public void kickParticipant(ClassPeople person) {
		if (invited_look_up.containsKey(person)) {
			attendees.remove(person);
			attendee_look_up.remove(person.name);
			person.allEventsParticipating.remove(this);
		}
		if (invited.contains(person)) {
			invited.remove(person);
			invited_look_up.remove(person.name);
			person.allEventsParticipating.remove(this);
		}
	}

	public void updateChatLog(String source, String text) {
		chat_log.add(text);
		chatroom_size++;
		if(source.contentEquals("external")){
		ClassUniverse.allUpdates.add(this);
		externalUpdateCount++;
		}
	}
	
	public boolean hasUpdate(){
		return externalUpdateCount>0;
	}
	
	public ClassPeople findPersonByName(String cname) {
		if (invited_look_up.containsKey(cname)) {
			return invited_look_up.get(cname);
		}
		return null;
	}
}
