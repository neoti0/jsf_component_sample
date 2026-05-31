package com.example.jsfsample.user.backing;

import com.example.jsfsample.user.model.UserFormData;
import com.example.jsfsample.user.model.UserListBean;
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
