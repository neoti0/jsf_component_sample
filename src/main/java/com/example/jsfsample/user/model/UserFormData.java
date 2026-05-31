package com.example.jsfsample.user.model;

import com.example.jsfsample.model.AddressFormData;
import jakarta.faces.flow.FlowScoped;
import jakarta.inject.Named;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

@Named
@FlowScoped("register")
public class UserFormData implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "名前は必須です")
    private String name;

    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "メールアドレスの形式が正しくありません")
    private String email;

    @NotBlank(message = "電話番号は必須です")
    private String phoneNumber;

    @Valid
    private AddressFormData address = new AddressFormData();

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public AddressFormData getAddress() { return address; }
    public void setAddress(AddressFormData address) { this.address = address; }
}
