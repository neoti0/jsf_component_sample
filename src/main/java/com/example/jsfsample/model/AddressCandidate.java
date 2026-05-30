package com.example.jsfsample.model;

import java.io.Serializable;

public class AddressCandidate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String prefecture;
    private final String city;
    private final String streetAddress;

    public AddressCandidate(String prefecture, String city, String streetAddress) {
        this.prefecture = prefecture;
        this.city = city;
        this.streetAddress = streetAddress;
    }

    public String getPrefecture() { return prefecture; }
    public String getCity() { return city; }
    public String getStreetAddress() { return streetAddress; }

    public String getFullAddress() {
        return prefecture + city + streetAddress;
    }
}
