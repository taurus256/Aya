package org.taurus.aya.server;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.taurus.aya.client.EventState;
import org.taurus.aya.server.entity.Event;
import org.taurus.aya.server.entity.Lane;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e WHERE e.id > 0")
    List<Event> findAll();

    //Метод для запроса данных боковой панели
    @Query("SELECT e FROM Event e WHERE (e.startDate > :startDate OR e.startDate = NULL) AND (e.endDate < :endDate OR e.endDate = NULL) AND e.isGraph = false")
    List<Event> findAllByStartDateGreaterThanAndEndDateLessThanAndIsGraphIsFalse(Date startDate, Date endDate);
    //Метод для запроса данных графиков (используется инверсный выбор дат)
    List<Event> findAllByStartDateLessThanAndEndDateGreaterThanAndIsGraphIsTrue(Date startdate, Date enddate);

    //Метод для выборки списка задач, завершенных в заданном интервале
    LinkedList<Event> findAllByEndDateGreaterThanAndEndDateLessThanAndIsGraphIsTrueAndState(Date intervalStart, Date intervalEnd, Integer state);

    //Метод для выборки "будущих" задач
    LinkedList<Event> findAllByStartDateGreaterThanAndIsGraphIsTrue(Date intervalStart);

    //Метод, увеличивающий endDate у задач в состоянии PROCESS
    @Modifying
    @Query("UPDATE Event e SET e.endDate=current_timestamp WHERE e.endDate<current_timestamp AND e.state=1")
    void updateEndDateForTasksInProcess();
}
