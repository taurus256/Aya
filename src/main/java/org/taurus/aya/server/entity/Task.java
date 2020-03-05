package org.taurus.aya.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.LazyCollectionOption;
import org.taurus.aya.client.EventState;

import javax.persistence.*;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/** Класс, сожержащий данные задачи. На графике задача представлена одним или несколькими объектами типа event, все они
 * ссылаются на информацию, которая хранится здесь*/
@Entity
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String lane;

    private Long author;
    private Long executor;

    private Integer priority;

    private Double plannedDuration;

    private Integer wuser;
    private Integer wgroup;
    private Integer ruser;
    private Integer rgroup;

    private Date startDate;
    private Date endDate;

    private Date start = null; // время последнего переключения задачи в режим, отличный от NEW и READY. Используется для определения того, сколько времени задача провела в данном режиме.
    private Double spentTime = 0.0; //  реальное (посчитанное системой или заданное пользователем) время выполнения задачи в часах
    private Integer pauseDays = 0; //  количество дней, в течении которых задача была неативна (в состоянии PAUSE или FAIL)

    private Integer state = EventState.NEW.ordinal();

    private Boolean showInBacklog = true;

    @OneToMany(cascade = {CascadeType.REMOVE, CascadeType.PERSIST}, orphanRemoval = true, mappedBy = "task")
    @org.hibernate.annotations.LazyCollection(LazyCollectionOption.EXTRA)
    private List<Event> events;

    public Task(){}

    public Task(String name,
                String description,
                String lane,
                Long author,
                Long executor,
                Integer priority,
                Integer wuser,
                Integer wgroup,
                Integer ruser,
                Integer rgroup,

                Double plannedDuration
                ) {
        this.name = name;
        this.description = description;
        this.lane = lane;
        this.author = author;
        this.executor = executor;
        this.priority = priority;
        this.wuser = wuser;
        this.wgroup = wgroup;
        this.ruser = ruser;
        this.rgroup = rgroup;
        this.plannedDuration = plannedDuration;
        events =  new LinkedList<>();
       }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getLane() {
        return lane;
    }

    public void setLane(String lane) {
        this.lane = lane;
    }

    public Long getAuthor() {
        return author;
    }

    public void setAuthor(Long author) {
        this.author = author;
    }

    public Long getExecutor() {
        return executor;
    }

    public void setExecutor(Long executor) {
        this.executor = executor;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
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

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Double getSpentTime() {
        return spentTime;
    }

    public Integer getPauseDays() {
        return pauseDays;
    }

    public void setPauseDays(Integer pauseDays) {
        this.pauseDays = pauseDays;
    }

    public Double getPlannedDuration() {
        return plannedDuration;
    }

    public void setPlannedDuration(Double plannedDuration) {
        this.plannedDuration = plannedDuration;
    }

    public Integer getEventCount() {
        return events.size();
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Boolean getFragmented()
    {
        return (events.size() > 1);
    }

    public Boolean getShowInBacklog() {return showInBacklog;}

    public void setShowInBacklog(Boolean showInBacklog) { this.showInBacklog = showInBacklog; }

    public String getIcon() { return "tree/task0.png";}

    @JsonIgnore
    public List<Event> getEvents() { return events; }

    public void recalculateFields()
    {
        if (events.size()>0) {
            startDate = events.stream().map(Event::getStartDate).min(Date::compareTo).orElseThrow(IllegalArgumentException::new);
            endDate = events.stream().map(Event::getEndDate).max(Date::compareTo).orElseThrow(IllegalArgumentException::new);
            spentTime = events.stream().map(Event::getSpentTime).mapToDouble(Double::doubleValue).sum();
        }
    }
}
