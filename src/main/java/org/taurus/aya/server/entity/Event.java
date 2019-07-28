package org.taurus.aya.server.entity;


import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
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
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss")
  @Column(name = "startdate")
  private Date startDate;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss")
  @Column(name = "enddate")
  private Date endDate;
  private Integer wuser;
  private Integer wgroup;
  private Integer ruser;
  private Integer rgroup;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss")
  private Date limitdate;
  @Column(name = "eventwindowstyle")
  private String eventWindowStyle;
  private Integer executor;
  private Integer priority;
  @Column(name="duration_d")
  private Integer durationD;
  @Column(name="duration_h")
  private Integer durationH;
  private String icon;
  private Integer state;
  private String executorName;
  private Integer rosTask;
  private Long author;
  private String sublane;
  private Boolean isBacklog;
  private Integer spent_time;
  private Date start;
  private Boolean isGraph;


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
    this.description = description;
  }


  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }


  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
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


  public Date getLimitdate() {
    return limitdate;
  }

  public void setLimitdate(Date limitdate) {
    this.limitdate = limitdate;
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


  public Integer getDurationD() {
    return durationD;
  }

  public void setDurationD(Integer durationD) {
    this.durationD = durationD;
  }


  public Integer getDurationH() {
    return durationH;
  }

  public void setDurationH(Integer durationH) {
    this.durationH = durationH;
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
    this.state = state;
  }


  public String getExecutorName() {
    return executorName;
  }

  public void setExecutorName(String executorName) {
    this.executorName = executorName;
  }


  public Integer getRosTask() {
    return rosTask;
  }

  public void setRosTask(Integer rosTask) {
    this.rosTask = rosTask;
  }


  public Long getAuthor() {
    return author;
  }

  public void setAuthor(Long author) {
    this.author = author;
  }


  public String getSublane() {
    return sublane;
  }

  public void setSublane(String sublane) {
    this.sublane = sublane;
  }


  public Boolean getIsBacklog() {
    return isBacklog;
  }

  public void setIsBacklog(Boolean isBacklog) {
    this.isBacklog = isBacklog;
  }


  public Integer getSpentTime() {
    return spent_time;
  }

  public void setSpentTime(Integer spent_time) {
    this.spent_time = spent_time;
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
    this.isGraph = isGraph;
  }
}
