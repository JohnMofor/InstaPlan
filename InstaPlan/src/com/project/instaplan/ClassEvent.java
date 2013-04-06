package com.project.instaplan;

import java.util.ArrayList;
import java.util.HashMap;

public class ClassEvent {

	String title, location, description, time, date;
	ArrayList<ClassPeople> attendees = new ArrayList<ClassPeople>();
	ArrayList<ClassPeople> invited = new ArrayList<ClassPeople>();
	ArrayList<String> chat_log = new ArrayList<String>();
	ArrayList<Message> messages = new ArrayList<Message>();
	int chatroom_size = 0;
	int externalUpdateCount = 0;
	int chatroomCursor = 0;
	boolean isMine=false;
	int eventCode = ClassUniverse.universeListOfAllEvents.size() + 1;
	String eventIdCode;
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

		this.title = a_title;
		this.location = a_location;
		this.description = a_description;
		this.time = a_time;
		this.date = a_date;
	}

	public void addAttendee(ClassPeople new_person) {
		this.attendees.add(new_person);
		this.attendee_look_up.put(new_person.phoneNumber, new_person);
		if (!invited_look_up.containsKey(new_person.phoneNumber)) {
			invite(new_person);
		}
	}

	public void makeHost(ClassPeople new_person) {
		this.host = new_person;
		if (!this.host.allEventsParticipating.contains(this)) {
			this.host.allEventsParticipating.add(this);
		}
		addAttendee(this.host);
	}

	public void invite(ClassPeople new_person) {
		if ((!this.invited_look_up.containsKey(new_person.phoneNumber) && !new_person.phoneNumber.equals(ClassUniverse.mPhoneNumber))) {
			this.invited.add(new_person);
			this.invited_look_up.put(new_person.phoneNumber, new_person);
			startListeningTo(new_person);
		}
		if (!new_person.allEventsParticipating.contains(this)) {
			new_person.allEventsParticipating.add(this);
		}
	}

	public void startListeningTo(ClassPeople new_person) {
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
		this.isFacebookEnabled = true;
	}

	public ArrayList<ClassPeople> getAttendees() {
		return this.attendees;
	}

	public ArrayList<ClassPeople> getIntived() {
		return this.invited;
	}

	public void kickParticipant(ClassPeople person) {
		if (this.attendee_look_up.containsKey(person.phoneNumber)) {
			this.attendees.remove(person.phoneNumber);
			this.attendee_look_up.remove(person.phoneNumber);
			person.allEventsParticipating.remove(this);
		}
		if (this.invited.contains(person)) {
			this.invited.remove(person);
			this.invited_look_up.remove(person.phoneNumber);
			person.allEventsParticipating.remove(this);
		}
	}

	public void updateChatLog(String source, String text, String sender) {
		// chat_log.add(text);
		this.messages.add(new Message(text, sender));
		this.chatroom_size++;
		if (source.contentEquals("external")) {
			ClassUniverse.allUpdates.add(this);
			this.externalUpdateCount++;
		}
	}

	public boolean hasUpdate() {
		return this.externalUpdateCount > 0;
	}

	public ClassPeople findPersonByphoneNumber(String cphoneNumber) {
		if (this.invited_look_up.containsKey(cphoneNumber)) {
			return this.invited_look_up.get(cphoneNumber);
		}
		return null;
	}

	public void delete() {
		ClassUniverse.universeEventLookUp.remove(this);
		ClassUniverse.universeListOfAllEvents
		.remove(this);
		this.title= null; this.location= null; this.description= null; this.time= null; this.date = null;
		this.attendees = null;
		this.invited = null;
		this.chat_log  = null;
		this.messages  = null;
		this.eventIdCode = null;
		this.host = null;
		this.isFacebookEnabled = null;
		this.invited_look_up = null;
		this.attendee_look_up = null;
	}
}
