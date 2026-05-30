package com.example.jsfsample.backing;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

@Named
@RequestScoped
public class RegisterInputBacking {

    public String confirm() {
        return "register-confirm";
    }

    /**
     * フロー外からの直接 URL アクセスを弾く。
     * f:viewAction(onPostback="false") から呼び出す。
     * Flow が未開始なら index へリダイレクト。
     */
    public String guardFlow() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx.getApplication().getFlowHandler().getCurrentFlow(ctx) == null) {
            return "index?faces-redirect=true";
        }
        return null;
    }
}
