package com.example.jsfsample.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;

public class AddressFormData implements Serializable {

    private static final long serialVersionUID = 1L;

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
}
