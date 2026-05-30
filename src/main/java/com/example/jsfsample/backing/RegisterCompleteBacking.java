package com.example.jsfsample.backing;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named
@RequestScoped
public class RegisterCompleteBacking {

    public String toList() {
        return "returnToList";
    }
}
