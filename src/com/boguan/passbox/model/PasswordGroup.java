package com.boguan.passbox.model;

public class PasswordGroup {
	private String groupName;
	private int imgId;

	public PasswordGroup(String groupName, int imgId) {
		this.groupName = groupName;
		this.imgId = imgId;
	}

	public PasswordGroup() {
	}

	public int getImgId() {
		return imgId;
	}

	public void setImgId(int imgId) {
		this.imgId = imgId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	@Override
	public String toString() {
		return "PasswordGroup [groupName=" + groupName + "]";
	}
}
