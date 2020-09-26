package com.opensource.rct.model;

import com.google.gson.JsonObject;

public class MagicNumber {
	
	private String magicNumber;
	private String description;
	private String dataType;
	private String databaseName;
	private String measurementName;
	private boolean dataReady = false;
	private JsonObject dataJson = null;
	
	
	public boolean isDataReady() {
		return dataReady;
	}
	public void setDataReady(boolean dataReady) {
		this.dataReady = dataReady;
	}
	public JsonObject getDataJson() {
		return dataJson;
	}
	public void setDataJson(JsonObject dataJson) {
		this.dataJson = dataJson;
	}
	public String getMagicNumber() {
		return magicNumber;
	}
	public void setMagicNumber(String magicNumber) {
		this.magicNumber = magicNumber;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public String getDatabaseName() {
		return databaseName;
	}
	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}
	public String getMeasurementName() {
		return measurementName;
	}
	public void setMeasurementName(String measurementName) {
		this.measurementName = measurementName;
	}
	
	

}
