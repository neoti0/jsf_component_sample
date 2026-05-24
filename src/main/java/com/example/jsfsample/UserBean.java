package com.example.jsfsample;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@SessionScoped
public class UserBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "名前は必須です")
    private String name;
    @NotBlank(message = "メールアドレスは必須です")
    private String email;
    @NotBlank(message = "郵便番号は必須です")
    @Pattern(regexp = "\\d{3}-?\\d{4}", message = "郵便番号の形式が正しくありません（例: 100-0004）")
    private String postalCode;
    @NotBlank(message = "都道府県は必須です")
    private String prefecture;
    @NotBlank(message = "市区町村は必須です")
    private String city;
    @NotBlank(message = "町村番地は必須です")
    private String streetAddress;
    private String buildingName;
    @NotBlank(message = "電話番号は必須です")
    private String phoneNumber;
    private List<User> users = new ArrayList<>();

    public String register() {
        users.add(new User(name, email, postalCode, prefecture, city, streetAddress, buildingName, phoneNumber));
        name = null;
        email = null;
        postalCode = null;
        prefecture = null;
        city = null;
        streetAddress = null;
        buildingName = null;
        phoneNumber = null;
        return "list?faces-redirect=true";
    }

    public String searchAddress() {
        // TODO: 実際のAPIに差し替える
        prefecture = "東京都";
        city = "千代田区";
        streetAddress = "大手町";
        return null;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getPrefecture() { return prefecture; }
    public void setPrefecture(String prefecture) { this.prefecture = prefecture; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getStreetAddress() { return streetAddress; }
    public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }

    public String getBuildingName() { return buildingName; }
    public void setBuildingName(String buildingName) { this.buildingName = buildingName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public List<User> getUsers() { return users; }

    public static class User implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String name;
        private final String email;
        private final String postalCode;
        private final String prefecture;
        private final String city;
        private final String streetAddress;
        private final String buildingName;
        private final String phoneNumber;

        public User(String name, String email, String postalCode, String prefecture,
                    String city, String streetAddress, String buildingName, String phoneNumber) {
            this.name = name;
            this.email = email;
            this.postalCode = postalCode;
            this.prefecture = prefecture;
            this.city = city;
            this.streetAddress = streetAddress;
            this.buildingName = buildingName;
            this.phoneNumber = phoneNumber;
        }

        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getPostalCode() { return postalCode; }
        public String getPrefecture() { return prefecture; }
        public String getCity() { return city; }
        public String getStreetAddress() { return streetAddress; }
        public String getBuildingName() { return buildingName; }
        public String getPhoneNumber() { return phoneNumber; }
    }
}
