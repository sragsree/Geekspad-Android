package com.example.sreer.geekspad.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sreer on 28-04-2017.
 */

public class User implements Parcelable {

    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private String birthDate;
    private String country;
    private String state;
    private String city;
    private String latitude;
    private String longitude;
    private Map<String, Skill> skills;


    public User(){

    }

    public User(String firstname, String email) {
        this.firstname = firstname;
        this.email = email;

    }


    public User(String firstname, String email, String country, String state) {
        this.firstname = firstname;
        this.email = email;
        this.country = country;
        this.state = state;
    }

    public User(Parcel in){
        String[] data = new String[10];

        in.readStringArray(data);
        // the order needs to be the same as in writeToParcel() method
        this.firstname = data[0];
        this.lastname = data[1];
        this.email = data[2];
        this.phone= data[3];
        this.birthDate = data[4];
        this.country = data[5];
        this.state = data[6];
        this.city = data[7];
        this.latitude = data[8];
        this.longitude = data[9];
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String fname) {
        this.firstname = fname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lname) {
        this.lastname = lname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public Map getSkills() {
        return skills;
    }

    public void setSkills(Map skills) {
        this.skills = skills;
    }

    public void addSkill(Skill skill){
        if(skills==null)
            skills = new LinkedHashMap<>();
        skills.put(skill.skillname, skill);
    }

    public  void removeSkill(Skill skill){
        if(skills !=null){
            skills.remove(skill.skillname);
        }
    }
    public String cleanEmailAddress(){
        return this.email.replaceAll("\\.", "-");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {this.firstname,
                this.lastname,
                this.email,
                this.phone,
                this.birthDate,
                this.country,
                this.state,
                this.city,
                this.latitude,
                this.longitude});
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public static User findUser(List<User> users, String title){
        User user = null;
        for(User userItem: users)
            if(userItem.getEmail().equals(title)) {
                return userItem;
            }
        return user;
    }

    public Skill getSkill(String skill){
        return skills.get(skill);
    }

}
