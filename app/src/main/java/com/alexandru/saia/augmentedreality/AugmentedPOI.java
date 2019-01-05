package com.alexandru.saia.augmentedreality;
/**
 * Created by krzysztofjackowski on 24/09/15.
 * and alexandru
 */
public class AugmentedPOI {
	private int mId;
	private String mName;
	private String mDescription;
	private double mLatitude;
	private double mLongitude;

	public AugmentedPOI(String newName, String newDescription,
						double newLatitude, double newLongitude) {
		this.mName = newName;
        this.mDescription = newDescription;
        this.mLatitude = newLatitude;
        this.mLongitude = newLongitude;
	}

	public String getPoiName() {
		return mName;
	}
	public double getPoiLatitude() {
		return mLatitude;
	}
	public void setPoiLatitude(double poiLatitude) {
		this.mLatitude = poiLatitude;
	}
	public double getPoiLongitude() {
		return mLongitude;
	}
	public void setPoiLongitude(double poiLongitude) {
		this.mLongitude = poiLongitude;
	}
}
