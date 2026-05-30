package com.example.jsfsample.list;

import com.example.jsfsample.model.UserFormData;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Named
@SessionScoped
public class UserListBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<User> users = new ArrayList<>();

    public void add(UserFormData formData) {
        users.add(new User(
            formData.getName(),
            formData.getEmail(),
            formData.getPhoneNumber(),
            formData.getAddress().getPostalCode(),
            formData.getAddress().getPrefecture(),
            formData.getAddress().getCity(),
            formData.getAddress().getStreetAddress(),
            formData.getAddress().getBuildingName()
        ));
    }

    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }

    public static class User implements Serializable {

        private static final long serialVersionUID = 1L;

        private final String name;
        private final String email;
        private final String phoneNumber;
        private final String postalCode;
        private final String prefecture;
        private final String city;
        private final String streetAddress;
        private final String buildingName;

        public User(String name, String email, String phoneNumber,
                    String postalCode, String prefecture, String city,
                    String streetAddress, String buildingName) {
            this.name = name;
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.postalCode = postalCode;
            this.prefecture = prefecture;
            this.city = city;
            this.streetAddress = streetAddress;
            this.buildingName = buildingName;
        }

        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPhoneNumber() { return phoneNumber; }
        public String getPostalCode() { return postalCode; }
        public String getPrefecture() { return prefecture; }
        public String getCity() { return city; }
        public String getStreetAddress() { return streetAddress; }
        public String getBuildingName() { return buildingName; }
    }
}
