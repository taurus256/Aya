package org.taurus.aya.server.entity;


import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.time.Instant;
import java.util.Date;

@Entity
public class Event {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private Integer parent;
  private Integer prev;
  private String lane;
  private String name;
  private String description;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
  @Column(name = "startdate")
  private Date startDate;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
  @Column(name = "enddate")
  private Date endDate;
  private Integer wuser;
  private Integer wgroup;
  private Integer ruser;
  private Integer rgroup;
  @Column(name = "eventwindowstyle")
  private String eventWindowStyle;
  private Integer executor;
  private Integer priority;

  private Double spentTime = 0.0; // реальное (посчитанное системой или заданное пользователем) время выполнения задачи в часах
  @Column(name="duration_h")
  private Double duration_h; // планируемое пользователем время выполения задачи в часах
  private String icon;
  private Integer state;
  private String executorName;
  private Integer rosTask;
  private Long author;
  private Date start;
  private Boolean isGraph;
  private Boolean userCorrectSpentTime = false;
  @ManyToOne
  @JoinColumn(name="executor", insertable = false, updatable = false)
  User user;

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


  public Integer getPrev() {
    return prev;
  }

  public void setPrev(Integer prev) {
    this.prev = prev;
  }


  public String getLane() {
    return lane;
  }

  public void setLane(String lane) {
    this.lane = lane;
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

    this.description = description==null?"":description;
  }


  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {

    this.startDate = startDate== null ? Date.from(Instant.now()) : startDate;
  }


  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {

    this.endDate = endDate== null?Date.from(Instant.now()):endDate;
  }


  public Integer getWuser() {
    return wuser;
  }

  public void setWuser(Integer wuser) {
    this.wuser = wuser;
  }


  public Integer getWgroup() {
    return wgroup;
  }

  public void setWgroup(Integer wgroup) {
    this.wgroup = wgroup;
  }


  public Integer getRuser() {
    return ruser;
  }

  public void setRuser(Integer ruser) {
    this.ruser = ruser;
  }


  public Integer getRgroup() {
    return rgroup;
  }

  public void setRgroup(Integer rgroup) {
    this.rgroup = rgroup;
  }

  public String getEventWindowStyle() {
    return eventWindowStyle;
  }

  public void setEventWindowStyle(String eventWindowStyle) {
    if (eventWindowStyle != null)
      this.eventWindowStyle = eventWindowStyle;
    else
      this.eventWindowStyle = "s3_event_new";
  }


  public Integer getExecutor() {
    return executor;
  }

  public void setExecutor(Integer executor) {
    this.executor = executor;
  }


  public Integer getPriority() {
    return priority;
  }

  public void setPriority(Integer priority) {
    this.priority = priority;
  }

  public Double getDuration_h() {
    return duration_h;
  }

  public void setDuration_h(Double duration_h) {

    this.duration_h = duration_h==null ? 0 : duration_h;
  }


  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    if (icon != null)
      this.icon = icon;
    else
      this.icon = "tree/task0.png";
  }

  public Integer getState() {
    return state;
  }

  public void setState(Integer state) {
    this.state = state==null? 0 : state;
  }


  public String getExecutorName() {
    if (user != null)
      return user.getShowedName();
    else return "";
  }

  public Long getAuthor() {
    return author;
  }

  public void setAuthor(Long author) {
    this.author = author;
  }


  public Double getSpentTime() {
    return spentTime;
  }

  public void setSpentTime(Double spent_time) {

    this.spentTime = (spent_time==null) ? 0.0 : spent_time;
  }


  public Date getStart() {
    return start;
  }

  public void setStart(Date start) {
    this.start = start;
  }


  public Boolean getIsGraph() {
    return isGraph;
  }

  public void setIsGraph(Boolean isGraph) {
    this.isGraph = (isGraph == null) ? false: isGraph;
  }

  public Boolean getUserCorrectSpentTime() {
    return userCorrectSpentTime;
  }
  public void setUserCorrectSpentTime(Boolean needRevision) {
    this.userCorrectSpentTime = needRevision;
  }


}
