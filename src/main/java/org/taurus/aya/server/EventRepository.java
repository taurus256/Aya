package org.taurus.aya.server;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.taurus.aya.server.entity.Event;
import org.taurus.aya.server.entity.Lane;

import java.util.Date;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e WHERE e.id > 0")
    List<Event> findAll();

    //Метод для запроса данных боковой панели
    @Query("SELECT e FROM Event e WHERE e.parent=:parent AND (e.startDate > :startDate OR e.startDate = NULL) AND (e.endDate < :endDate OR e.endDate = NULL) AND e.isGraph = false")
    List<Event> findAllByParentAndStartDateGreaterThanAndEndDateLessThanAndIsGraphIsFalse(Integer parent, Date startDate, Date endDate);
    //Метод для запроса данных графиков (используется инверсный выбор дат)
    List<Event> findAllByParentAndStartDateLessThanAndEndDateGreaterThanAndIsGraphIsTrue(Integer parent, Date startdate, Date enddate);
}
