package org.taurus.aya.server;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.taurus.aya.server.entity.Event;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e WHERE e.id > 0")
    List<Event> findAll();

    //Метод для запроса данных боковой панели
    @Query("SELECT e FROM Event e inner join e.task WHERE (e.startDate > :startDate OR e.startDate IS NULL) AND (e.endDate <= :endDate OR e.endDate IS NULL) AND e.task.showInBacklog = true AND e.task.lane in (:laneNames)")
    List<Event> findAllByStartDateGreaterThanAndEndDateLessThanEqualAndIsGraphIsFalse(Date startDate, Date endDate, List<String> laneNames);

    //Метод для запроса данных графиков (используется инверсный выбор дат)
    @Query("select e from Event e inner join e.task where e.startDate<=:startdate and e.endDate > :enddate and e.isGraph = true and e.task.lane in (:laneNames)")
    List<Event> findAllByStartDateLessThanEqualAndEndDateGreaterThanAndIsGraphIsTrue(Date startdate, Date enddate, List<String> laneNames);

    @Query("select e from Event e inner join e.task where e.startDate <= :startdate and e.endDate>:enddate and e.task.lane in (:laneNames) and e.task.executor=:executor")
    List<Event> findAllByStartDateLessThanEqualAndEndDateGreaterThanAndIsGraphIsTrueAndExecutor(Date startdate, Date enddate, List<String> laneNames, Long executor);

    @Query("select e from Event e inner join e.task where e.endDate > ?1 and e.endDate<?2 and e.task.state=?3 and e.task.lane in (?4)")
    List<Event> findAllByEndDateGreaterThanAndEndDateLessThanAndState(Date startDate, Date endDate, Integer state, List<String> laneNames);
    //@Query(value = "select * from event e inner join task t on e.task_id=t.id where e.endDate > ?1 and e.endDate<?2 and e.state=?3 AND e.lane IN (?4)", nativeQuery = true)
    //List<Event> findAllByEndDateGreaterThanAndEndDateLessThanAndState(Date startDate, Date endDate, Integer state, List<String> laneNames);

    //Метод для выборки списка задач, завершенных в заданном интервале
    @Query("select e from Event e inner join e.task where e.endDate > :intervalStart and e.endDate<:intervalEnd and e.task.state=:state and e.task.lane in (:laneNames)")
    LinkedList<Event> findAllByEndDateGreaterThanAndEndDateLessThanAndIsGraphIsTrueAndState(Date intervalStart, Date intervalEnd, Integer state, List<String> laneNames);

    //Метод для выборки "будущих" задач
    @Query("select e from Event e inner join e.task where e.endDate > :today and e.task.state=0 and e.task.lane in (:laneNames)")
    LinkedList<Event> findAllByStartDateGreaterThanAndIsState(Date today, List<String> laneNames);

    //Метод для выборки всех event-ов одного task-а
    LinkedList<Event> findAllByTaskId(Long taskId);

    //Метод, увеличивающий endDate у задач в состоянии PROCESS
    @Modifying
    @Query("UPDATE Event e SET e.endDate=current_timestamp WHERE e IN (select e1 from Event e1 inner join e1.task where e1.endDate < current_timestamp and e1.task.state=1)")
    void updateEndDateForTasksInProcess();

    //Метод, увеличивающий состояние FAIL для задач, время старта которых прошло, но они не были взяты в работу
    //Также переставляет startTime задачи на текущую дату
    @Modifying
    @Query("UPDATE Event e SET e.state=4, e.startDate=current_timestamp WHERE e IN (select e1 from Event e1 inner join e1.task where e1.startDate < current_timestamp and current_timestamp < e1.endDate  and e1.task.state=0)")
    void updateStateForForgottenTasks();
}
