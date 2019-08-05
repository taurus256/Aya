package org.taurus.aya.server.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="users") // Name 'user' used as keyword in PostgreSQL
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String firstname;
  private String surname;
  private String patronymic;
  private String nickname;
  private String workphone;
  private String mobphone;
  private String usid;
  private String passwordHash;
  private java.sql.Timestamp imageDateCreated;
  private String showedName;

  @ManyToMany
  @JoinTable(
          name="relation_user_group",
          joinColumns = @JoinColumn(name="userid"),
          inverseJoinColumns = @JoinColumn(name="groupid")
  )
  @JsonIgnore
  private List<Group> groups;

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

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public java.sql.Timestamp getImageDateCreated() {
    return imageDateCreated;
  }

  public void setImageDateCreated(java.sql.Timestamp imageDateCreated) {
    this.imageDateCreated = imageDateCreated;
  }

  public String getShowedName() {
    return showedName;
  }

  public void setShowedName(String showedName) {
    this.showedName = showedName;
  }

  public List<Group> getGroups() {
    return groups;
  }

  public void setGroups(List<Group> groups) {
    this.groups = groups;
  }
}
