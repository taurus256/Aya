package org.taurus.aya.server.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.taurus.aya.client.EventState;
import org.taurus.aya.server.EventRepository;
import org.taurus.aya.server.entity.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Logger;

@Service
public class EventService {

    private EventRepository eventRepository;

    private Logger logger = Logger.getLogger(this.getClass().getName());

    //2019-07-19T03:12:27.000
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    public EventService(@Autowired EventRepository repository)
    {
        this.eventRepository = repository;
    }


    public List<Event> getData(String[] criteria) throws RuntimeException, ParseException
    {
        List<Event> eventList;
        HashMap<String,String> criteriaMap = parseCriteria(criteria);

        //System.out.println(criteriaMap);
        if (Boolean.valueOf(criteriaMap.getOrDefault("isGraph","false")))
            eventList = eventRepository.findAllByStartDateLessThanAndEndDateGreaterThanAndIsGraphIsTrue(
                formatter.parse(criteriaMap.getOrDefault("startDate","2000-01-0")),
                formatter.parse(criteriaMap.getOrDefault("endDate","2050-01-01"))
            );
        else
            eventList = eventRepository.findAllByStartDateGreaterThanAndEndDateLessThanAndIsGraphIsFalse(
                formatter.parse(criteriaMap.getOrDefault("startDate","2000-01-01")),
                formatter.parse(criteriaMap.getOrDefault("endDate","2050-01-01"))
            );

        // Перевод в целый вид для вывода в интерфейсе
        eventList.stream().forEach(e -> e.setSpentTime(
                new Long(Math.round(e.getSpentTime())).doubleValue()
        ));

        return eventList;
    }


    private HashMap<String,String> parseCriteria(String[] criteria)
    {
        HashMap<String,String> result = new HashMap<>();
        if (criteria != null)
            for (String c: criteria) {
                //System.out.println("input:'" + c + "'");
                if (c.contains("fieldName")) {
                    if (c.contains("AdvancedCriteria")) {
                        result.putAll(parseCriteria(
                                c.substring(c.indexOf("[") + 1, c.indexOf("]") - 1).split("\\},\\{")
                                )
                        );
                    }
                    else {
                        String[] params = c.substring(1, c.length() - 1).split(",");
                        HashMap<String, String> parameterParts = new HashMap<>();
                        for (String param : params) {
                            String rawParam = param.replace("\"", "");
                            parameterParts.put(rawParam.substring(0, rawParam.indexOf(":")), rawParam.substring(rawParam.indexOf(":") + 1, rawParam.length()));
                        }
                        //System.out.println("Name=" + parameterParts.get("fieldName") + " Value=" + parameterParts.get("value"));
                        result.put(parameterParts.get("fieldName"), parameterParts.get("value"));
                    }
                }
            }

        return result;
    }

    /**
    * Установка времени выполнения задачи.
    * Если задача начала выполняться - запоминает время старта, если прекратила (встала на паузу и т.п) -
    * записывает в задачу время выполнения как разницу между временем старта и текущим.
    * Если задача выполнялась более 1 дня - записывает время исходя из длительности "условного дня" (8 часов)
    * @param event- задача, состояние которой изменилось
    * */
    public void setEventSpentTime(Event event, Integer newEventState)
    {
        if (event.getIsGraph())
            if (!newEventState.equals(EventState.PROCESS.ordinal())) //это переключение ИЗ режима process в какой-то другой
            {
                if (event.getStart() == null ) throw new RuntimeException("setEventSpentTime: event state was changed to (NEW/READY/PAUSE/FAIL) but start timestamp is NULL ");
                if (Duration.between(event.getStart().toInstant(),Instant.now()).toHours() >24) // если больше суток - считаем по длительностям "условного дня"
                    event.setSpentTime(event.getSpentTime() + Duration.between(event.getStart().toInstant(),Instant.now()).toDays() * 8 * 60);
                else
                    event.setSpentTime(event.getSpentTime() + Duration.between(event.getStart().toInstant(),Instant.now()).toMinutes());
            }
            else
            {
                //если это переключение в режим process - запоминаем время
                event.setStart(new Date());
            }
    }
}
