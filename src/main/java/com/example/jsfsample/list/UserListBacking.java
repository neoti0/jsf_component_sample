package com.example.jsfsample.list;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.List;

@Named
@RequestScoped
public class UserListBacking {

    @Inject
    private UserListBean userListBean;

    public List<String> getTableHeaders() {
        return List.of("名前", "メールアドレス", "電話番号", "郵便番号", "住所");
    }

    public List<List<String>> getTableRows() {
        return userListBean.getUsers().stream()
            .map(u -> {
                String address = u.getPrefecture() + u.getCity() + u.getStreetAddress();
                if (u.getBuildingName() != null && !u.getBuildingName().isEmpty()) {
                    address += " " + u.getBuildingName();
                }
                return List.of(
                    u.getName(),
                    u.getEmail(),
                    u.getPhoneNumber(),
                    u.getPostalCode(),
                    address
                );
            })
            .toList();
    }
}
