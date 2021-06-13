package com.example.myloactiontracker;

public class User {

    public String firstname;
    public String lastname;
    public String PhoneNumber;
    public String Address;

    public User(String firstname, String lastname, String phoneNumber, String address) {
        this.firstname = firstname;
        this.lastname = lastname;
        PhoneNumber = phoneNumber;
        Address = address;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }


}
