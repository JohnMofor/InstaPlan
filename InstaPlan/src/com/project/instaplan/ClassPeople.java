package com.project.instaplan;

import java.util.ArrayList;

public class ClassPeople {

	String name, phoneNumber, email, facebookID;
	ArrayList<String> contactTypes = new ArrayList<String>(3);
	ArrayList<ClassEvent> allEventsParticipating = new ArrayList<ClassEvent>();
	boolean hasApp=true;
	boolean hasGCM=true;//change to false on release...

	public ClassPeople(String a_name, String the_contactType, String contact,
			ClassEvent the_event) {
		name = a_name;
//		ClassUniverse.universeNameLookUp.put(name, this);
		if (!allEventsParticipating.contains(the_event)) {
			allEventsParticipating.add(the_event);
			String contactType = the_contactType;
			if (contactType.contentEquals("phoneNumber")) {
				phoneNumber = contact;
				if (!hasPhoneNumber()) {
					contactTypes.add("phoneNumber");
					ClassUniverse.universePhoneNumberLookUp.put(phoneNumber,
							this);
				}
//			} else if (contactType.contentEquals("facebookID")) {
//				facebookID = contact;
//				if (!hasFacebookID()) {
//					contactTypes.add("facebookID");
//					ClassUniverse.universeFacebookIDLookUp
//							.put(facebookID, this);
//				}
//
//			} else if (contactType.contentEquals("email")) {
//				email = contact;
//				if (!hasEmail()) {
//					contactTypes.add("email");
//					ClassUniverse.universeEmailLookUp.put(email, this);
//				}
			}
		}
	}

	public ClassPeople(String a_name, String the_contactType, String contact) {
		this.name = a_name;
//		ClassUniverse.universeNameLookUp.put(name, this);
		String contactType = the_contactType;
		if (contactType.contentEquals("phoneNumber")) {
			this.phoneNumber = contact;
			if (!hasPhoneNumber()) {
				this.contactTypes.add("phoneNumber");
				ClassUniverse.universePhoneNumberLookUp.put(phoneNumber, this);
			}
//		} else if (contactType.contentEquals("facebookID")) {
//			facebookID = contact;
//			if (!hasFacebookID()) {
//				this.contactTypes.add("facebookID");
//				ClassUniverse.universeFacebookIDLookUp.put(facebookID, this);
			}
//		} else if (contactType.contentEquals("email")) {
//			email = contact;
//			if (!hasEmail()) {
//				this.contactTypes.add("email");
//				ClassUniverse.universeEmailLookUp.put(email, this);
//			}
//		}
	}

	public Boolean hasPhoneNumber() {
		return this.contactTypes.contains("phoneNumber");
	}

	public Boolean hasFacebookID() {
		return this.contactTypes.contains("facebookID");
	}

	public Boolean hasEmail() {
		return contactTypes.contains("email");
	}

	public void updateContact(String a_name, String the_contactType,
			String contact, ClassEvent the_event) {
		name = a_name;
		if (!allEventsParticipating.contains(the_event)) {
			allEventsParticipating.add(the_event);
		}
		String contactType = the_contactType;
		if (contactType.contentEquals("phoneNumber")) {
			phoneNumber = contact;
			if (!hasPhoneNumber()) {
				contactTypes.add("phoneNumber");
			}
		} else if (contactType.contentEquals("facebookID")) {
			facebookID = contact;
			if (!hasFacebookID()) {
				contactTypes.add("facebookID");
			}
		} else if (contactType.contentEquals("email")) {
			email = contact;
			if (!hasEmail()) {
				contactTypes.add("email");
			}
		}
	}

	public String getContact() {

		if (hasPhoneNumber()) {
			return phoneNumber;
		}
		if (hasFacebookID()) {
			return facebookID;
		}
		if (hasEmail()) {
			return email;
		}
		return "NO Contact";

	}
	
	public boolean isParticipatingIn(ClassEvent event){
		return this.allEventsParticipating.contains(event);
	}

}