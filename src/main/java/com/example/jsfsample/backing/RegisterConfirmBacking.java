package com.example.jsfsample.backing;

import com.example.jsfsample.list.UserListBean;
import com.example.jsfsample.model.UserFormData;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@RequestScoped
public class RegisterConfirmBacking {

    @Inject
    private UserFormData userFormData;

    @Inject
    private UserListBean userListBean;

    public String register() {
        userListBean.add(userFormData);
        return "register-complete";
    }

    public String back() {
        return "register-input";
    }
}
