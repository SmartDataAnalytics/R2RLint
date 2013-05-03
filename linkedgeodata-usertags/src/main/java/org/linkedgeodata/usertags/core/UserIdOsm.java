package org.linkedgeodata.usertags.core;

public class UserIdOsm implements UserId {
	private long userIdOsm;

	public UserIdOsm(long userIdOsm) {
		this.userIdOsm = userIdOsm;
	}

	public long getUserId() {
		return userIdOsm;
	}
}
