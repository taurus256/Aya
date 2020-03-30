package org.taurus.aya.server.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.taurus.aya.client.EventState;

import javax.persistence.*;
import java.time.Instant;
import java.util.Date;

@Entity
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "startdate")
    private Date startDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "enddate")
    private Date endDate;
    private Long wuser;
    private Long wgroup;
    private Long ruser;
    private Long rgroup;
    @Column(name = "eventwindowstyle")
    private String eventWindowStyle;

    private Double spentTime = 0.0; // реальное (посчитанное системой или заданное пользователем) время выполнения задачи в часах

    private String icon;
    private Integer state;
    private Boolean isGraph;
    private Boolean userCorrectSpentTime = false;

    private Integer index = 0;

    @Column(insertable = false, updatable = false)
    private Long taskId;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name="taskId")
    private Task task;

    public Event(){}
    public Event(
            Task t,
            Integer index,
            Long executor,
            Date startDate,
            Date endDate,
            String eventWindowStyle,
            String icon,
            Integer state
    ){
        this.task = t;
        this.index = index;
        if (executor != null)
            task.setExecutor(executor);
        else
            throw  new IllegalArgumentException("Event constructor: executor cannot be null");

        this.startDate = startDate;
        this.endDate = endDate;
        this.eventWindowStyle = eventWindowStyle;
        this.icon = icon;
        this.state = state;
        this.isGraph = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLane() {
        return task.getLane();
    }

    public void setLane(String lane) {
        task.setLane(lane);
    }


    public String getName() {
        return task.getName();
    }

    public void setName(String name) {
        task.setName(name);
    }


    public String getDescription() {
        return task.getDescription();
    }

    public void setDescription(String description) {

        task.setDescription(description);
    }


    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {

        this.startDate = startDate == null ? Date.from(Instant.now()) : startDate;
    }


    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {

        this.endDate = endDate== null?Date.from(Instant.now()):endDate;
    }


    public Integer getWuser() {
        return task.getWuser();
    }

    public void setWuser(Integer wuser) {
        task.setWuser(wuser);
    }


    public Integer getWgroup() {
        return task.getWgroup();
    }

    public void setWgroup(Integer wgroup) {
        task.setWgroup(wgroup);
    }


    public Integer getRuser() {
        return task.getRuser();
    }

    public void setRuser(Integer ruser) {
        task.setRuser(ruser);
    }


    public Integer getRgroup() {
        return task.getRgroup();
    }

    public void setRgroup(Integer rgroup) {
        task.setRgroup(rgroup);
    }

    public String getEventWindowStyle() {
        switch(EventState.values()[task.getState()]) {
            case NEW: return "s3_event_new";
            case PROCESS: return "s3_event_process";
            case PAUSE: return "s3_event_pause";
            case READY: return "s3_event_ready";
            case FAIL: return "s3_event_fail";
        }
        return "s3_event_new";
    }

    public void setEventWindowStyle(String eventWindowStyle) {
        if (eventWindowStyle != null)
            this.eventWindowStyle = eventWindowStyle;
        else
            this.eventWindowStyle = "s3_event_new";
    }


    public Long getExecutor() {
        return task.getExecutor();
    }

    public void setExecutor(Long executor) {
        task.setExecutor(executor);
    }


    public Integer getPriority() {
        return task.getPriority();
    }

    public void setPriority(Integer priority) {
        task.setPriority(priority);
    }

    public Double getDuration_h() {
        return task.getPlannedDuration();
    }

    public void setDuration_h(Double duration_h) {

        task.setPlannedDuration(duration_h==null ? 0 : duration_h);
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
        return task.getState();
    }

    public void setState(Integer state) {
        task.setState(state==null? 0 : state);
    }


    public String getExecutorName() {
        if (task.getExecutorName() != null)
            return task.getExecutorName();
        else return "";
    }

    public Long getAuthor() {
        return task.getAuthor();
    }

    public void setAuthor(Long author) {
        task.setAuthor(author);
    }


    public Double getSpentTime() {
        return spentTime;
    }

    public void setSpentTime(Double spent_time) {

        spentTime = (spent_time==null)? 0.0 : spent_time;
    }

    @JsonGetter("spentTime")
    public double getSpentTimeJson(){
        return Math.round(spentTime*10.0)/10.0;
    }


    public Date getStart() {
        return task.getStart();
    }

    public void setStart(Date start) {
        task.setStart(start);
    }


    public Boolean getIsGraph() {
        return true;
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

    public Long getTaskId() { return taskId; }

    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public Task getTask() {
        return task;
    }

    public Boolean getFragmented()
    {
        return task.getFragmented();
    }

    public Integer getIndex() { return index; }

}
