package org.taurus.aya.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

public class UserDTO implements IsSerializable {

    private Long id;
    private String firstname;
    private String surname;
    private String patronymic;
    private String nickname;
    private String workphone;
    private String mobphone;
    private String usid;
    private String passwordHash;
    private String showedName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPatronymic() {
        return patronymic;
    }

    public void setPatronymic(String patronymic) {
        this.patronymic = patronymic;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getWorkphone() {
        return workphone;
    }

    public void setWorkphone(String workphone) {
        this.workphone = workphone;
    }

    public String getMobphone() {
        return mobphone;
    }

    public void setMobphone(String mobphone) {
        this.mobphone = mobphone;
    }

    public String getUsid() {
        return usid;
    }

    public void setUsid(String usid) {
        this.usid = usid;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String password_hash) {
        this.passwordHash = password_hash;
    }

    public String getShowed_name() {
        return showedName;
    }

    public void setShowed_name(String showedName) {
        this.showedName = showedName;
    }
}
