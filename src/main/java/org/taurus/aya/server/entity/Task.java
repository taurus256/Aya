package org.taurus.aya.server.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.LazyCollectionOption;
import org.taurus.aya.client.EventState;

import javax.persistence.*;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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

    private Double plannedDuration=0.0;

    private Long wuser;
    private Long wgroup;
    private Long ruser;
    private Long rgroup;

    private Date startDate;
    private Date endDate;

    private Date start = null; // время последнего переключения задачи в режим, отличный от NEW и READY. Используется для определения того, сколько времени задача провела в данном режиме.
    private Double spentTime = 0.0; //  реальное (посчитанное системой или заданное пользователем) время выполнения задачи в часах

    private Integer state = EventState.NEW.ordinal();

    private Boolean showInBacklog = true;

    private String externalJiraTaskId; //ID внешней задачи в JIRA

    private String externalUrl;

    @OneToMany(cascade = {CascadeType.PERSIST,CascadeType.REMOVE}, orphanRemoval = true, mappedBy = "task")
    @org.hibernate.annotations.LazyCollection(LazyCollectionOption.EXTRA)
    @OrderBy("index ASC")
    private List<Event> events;

    @ManyToOne
    @JoinColumn(name="executor", insertable = false, updatable = false)
    User user;

    public Task(){}

    public Task(String name,
                String description,
                String lane,
                Long author,
                Long executor,
                Integer priority,
                Long wuser,
                Long wgroup,
                Long ruser,
                Long rgroup,
                Double plannedDuration,
                Boolean showInBacklog,
                String externalUrl,
                String externalJiraTaskId) {
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
        this.showInBacklog = showInBacklog;
        this.externalUrl=externalUrl;
        this.externalJiraTaskId = externalJiraTaskId;
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

    public void setShowInBacklog(Boolean showInBacklog) {
        System.out.println("showInBacklog(setter) = " + showInBacklog);
        this.showInBacklog = showInBacklog; }

    public String getIcon() {
        switch (priority)
        {
            case 2: return "tree/task_high.png"; //high priority
            case 0: return "tree/task_low.png"; // low priority
            default: return "tree/task_normal.png";
        }
    }

    public String getExternalJiraTaskId() {
        return externalJiraTaskId;
    }

    public void setExternalJiraTaskId(String externalJiraTaskId) {
        this.externalJiraTaskId = externalJiraTaskId;
    }

    @JsonIgnore
    public List<Event> getEvents() { return events; }

    public String getExecutorName()
    {
        return user!=null? user.getShowedName() : "";
    }

    public String getExternalUrl() {
        return externalUrl != null ? externalUrl : "";
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }


    public void recalculateFields()
    {
        if (events.size()>0) {
            startDate = events.stream().map(Event::getStartDate).min(Date::compareTo).orElseThrow(IllegalArgumentException::new);
            endDate = events.stream().map(Event::getEndDate).max(Date::compareTo).orElseThrow(IllegalArgumentException::new);
            spentTime = events.stream().map(Event::getSpentTime).mapToDouble(Double::doubleValue).sum();
        }
    }
}
