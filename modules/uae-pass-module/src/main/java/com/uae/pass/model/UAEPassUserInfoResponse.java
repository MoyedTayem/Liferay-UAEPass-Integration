package com.uae.pass.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UAEPassUserInfoResponse {

	@JsonProperty("sub")
	private String sub;
	@JsonProperty("fullnameAR")
	private String fullnameAR;
	@JsonProperty("idType")
	private String idType;
	@JsonProperty("gender")
	private String gender;
	@JsonProperty("mobile")
	private String mobile;
	@JsonProperty("lastnameEN")
	private String lastnameEN;
	@JsonProperty("fullnameEN")
	private String fullnameEN;
	@JsonProperty("uuid")
	private String uuid;
	@JsonProperty("lastnameAR")
	private String lastnameAR;
	@JsonProperty("idn")
	private String idn;
	@JsonProperty("nationalityEN")
	private String nationalityEN;
	@JsonProperty("firstnameEN")
	private String firstnameEN;
	@JsonProperty("userType")
	private String userType;
	@JsonProperty("nationalityAR")
	private String nationalityAR;
	@JsonProperty("firstnameAR")
	private String firstnameAR;
	@JsonProperty("email")
	private String email;

	@JsonProperty("sub")
	public String getSub() {
		return sub;
	}

	@JsonProperty("sub")
	public void setSub(String sub) {
		this.sub = sub;
	}

	@JsonProperty("fullnameAR")
	public String getFullnameAR() {
		return fullnameAR;
	}

	@JsonProperty("fullnameAR")
	public void setFullnameAR(String fullnameAR) {
		this.fullnameAR = fullnameAR;
	}

	@JsonProperty("idType")
	public String getIdType() {
		return idType;
	}

	@JsonProperty("idType")
	public void setIdType(String idType) {
		this.idType = idType;
	}

	@JsonProperty("gender")
	public String getGender() {
		return gender;
	}

	@JsonProperty("gender")
	public void setGender(String gender) {
		this.gender = gender;
	}

	@JsonProperty("mobile")
	public String getMobile() {
		return mobile;
	}

	@JsonProperty("mobile")
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	@JsonProperty("lastnameEN")
	public String getLastnameEN() {
		return lastnameEN;
	}

	@JsonProperty("lastnameEN")
	public void setLastnameEN(String lastnameEN) {
		this.lastnameEN = lastnameEN;
	}

	@JsonProperty("fullnameEN")
	public String getFullnameEN() {
		return fullnameEN;
	}

	@JsonProperty("fullnameEN")
	public void setFullnameEN(String fullnameEN) {
		this.fullnameEN = fullnameEN;
	}

	@JsonProperty("uuid")
	public String getUuid() {
		return uuid;
	}

	@JsonProperty("uuid")
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@JsonProperty("lastnameAR")
	public String getLastnameAR() {
		return lastnameAR;
	}

	@JsonProperty("lastnameAR")
	public void setLastnameAR(String lastnameAR) {
		this.lastnameAR = lastnameAR;
	}

	@JsonProperty("idn")
	public String getIdn() {
		return idn;
	}

	@JsonProperty("idn")
	public void setIdn(String idn) {
		this.idn = idn;
	}

	@JsonProperty("nationalityEN")
	public String getNationalityEN() {
		return nationalityEN;
	}

	@JsonProperty("nationalityEN")
	public void setNationalityEN(String nationalityEN) {
		this.nationalityEN = nationalityEN;
	}

	@JsonProperty("firstnameEN")
	public String getFirstnameEN() {
		return firstnameEN;
	}

	@JsonProperty("firstnameEN")
	public void setFirstnameEN(String firstnameEN) {
		this.firstnameEN = firstnameEN;
	}

	@JsonProperty("userType")
	public String getUserType() {
		return userType;
	}

	@JsonProperty("userType")
	public void setUserType(String userType) {
		this.userType = userType;
	}

	@JsonProperty("nationalityAR")
	public String getNationalityAR() {
		return nationalityAR;
	}

	@JsonProperty("nationalityAR")
	public void setNationalityAR(String nationalityAR) {
		this.nationalityAR = nationalityAR;
	}

	@JsonProperty("firstnameAR")
	public String getFirstnameAR() {
		return firstnameAR;
	}

	@JsonProperty("firstnameAR")
	public void setFirstnameAR(String firstnameAR) {
		this.firstnameAR = firstnameAR;
	}

	@JsonProperty("email")
	public String getEmail() {
		return email;
	}

	@JsonProperty("email")
	public void setEmail(String email) {
		this.email = email;
	}

}
