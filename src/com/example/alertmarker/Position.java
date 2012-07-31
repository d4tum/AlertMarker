package com.example.alertmarker;

import android.location.Address;

public class Position {
	private long id;
	private String address;
	private double latitude;
	private double longitude;

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddress() {
		return address;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setAddress(Address a) {
		if (null == a) {
			address = null;
			latitude = longitude = 0;
		} else {
			int maxAddressLine = a.getMaxAddressLineIndex();
			StringBuffer sb = new StringBuffer("");
			for (int i = 0; i < maxAddressLine; i++) {
				sb.append(a.getAddressLine(i) + " ");
			}
			address = sb.toString();
			latitude = a.getLatitude();
			longitude = a.getLongitude();
		}
	}
}
