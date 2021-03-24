package org.taurus.aya.server.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

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
  private java.util.Date created;
  private Boolean useJira;
  private String jiraLogin;
  private String jiraPass;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
          name="relation_user_group",
          joinColumns = @JoinColumn(name="userid"),
          inverseJoinColumns = @JoinColumn(name="groupid")
  )
  @JsonIgnore
  private Set<Group> groups = new HashSet<>();

  public User(){
    setCreated(new Date());
  }

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

  @JsonIgnore
  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date imageDateCreated) {
    this.created = imageDateCreated;
  }

  public String getShowedName() {
    return firstname + " " + surname;
  }

  @OrderBy("id")
  public Set<Group> getGroups() {
    return groups;
  }

  public void setGroups(Set<Group> groups) {
    this.groups = groups;
  }

  public Boolean getUseJira() {
    return useJira;
  }

  public void setUseJira(Boolean useJira) {
    this.useJira = useJira;
  }

  public String getJiraLogin() {
    return jiraLogin;
  }

  public void setJiraLogin(String jiraLogin) {
    this.jiraLogin = jiraLogin;
  }

  public String getJiraPass() {
    return jiraPass;
  }

  public void setJiraPass(String jiraPass) {
    this.jiraPass = jiraPass;
  }

}
