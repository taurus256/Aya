package org.taurus.aya.server.entity;

import javax.persistence.*;

@Entity
public class Lane {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private Integer parent;
  private String name;
  private String description;
  private Long wuser;
  private Long wgroup;
  private Long ruser;
  private Long rgroup;
  @Column(name="lane_order")
  private Integer laneOrder;
  private Boolean visible;
  private Long author;
  @Column(name="isfolder")
  private Boolean isFolder;
  private Boolean lane;


  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }


  public Integer getParent() {
    return parent;
  }

  public void setParent(Integer parent) {
    this.parent = parent;
  }


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    if (description == null || description.equals("null")) description = "";
    this.description = description;
  }


  public Long getWuser() {
    return wuser;
  }

  public void setWuser(Long wuser) {
    this.wuser = wuser;
  }


  public Long getWgroup() {
    return wgroup;
  }

  public void setWgroup(Long wgroup) {
    this.wgroup = wgroup;
  }


  public Long getRuser() {
    return ruser;
  }

  public void setRuser(Long ruser) {
    this.ruser = ruser;
  }


  public Long getRgroup() {
    return rgroup;
  }

  public void setRgroup(Long rgroup) {
    this.rgroup = rgroup;
  }


  public Integer getLaneOrder() {
    return laneOrder;
  }

  public void setLaneOrder(Integer laneOrder) {
    this.laneOrder = laneOrder;
  }


  public Boolean getVisible() {
    return visible;
  }

  public void setVisible(Boolean visible) {
    this.visible = visible;
  }


  public Long getAuthor() {
    return author;
  }

  public void setAuthor(Long author) {
    this.author = author;
  }


  public Boolean getIsFolder() {
    return isFolder;
  }

  public void setIsFolder(Boolean isFolder) {
    this.isFolder = isFolder;
  }


  public Boolean getLane() {
    return lane;
  }

  public void setLane(Boolean lane) {
    this.lane = lane;
  }

}
