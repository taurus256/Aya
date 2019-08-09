package org.taurus.aya.server.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.taurus.aya.server.EventRepository;
import org.taurus.aya.server.entity.Event;
import org.taurus.aya.server.services.EventService;
import org.taurus.aya.shared.GwtResponse;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController extends GenericController {

    private EventRepository eventRepository;

    private EventService eventService;

    public EventController(@Autowired EventRepository repository, @Autowired EventService service)
    {
        this.eventRepository = repository;
        this.eventService = service;
    }

    @ResponseBody
    @PostMapping("/fetch")
    public GwtResponse execute(HttpServletRequest request, @RequestParam String _operationType, @RequestParam (required=false) String[] criteria) throws RuntimeException, ParseException
    {

        System.out.println("Operation_type=" + _operationType);
        System.out.println("request body is:" + _operationType);

        List<Event> events = eventService.getData(criteria);
        return new GwtResponse(0,events.size(),events.size(), events);
    }

    @PostMapping("/modify")
    @ResponseBody
    public GwtResponse executePost(
        @RequestParam String _operationType,
        @RequestParam (required = false) String id,          //integer,
        @RequestParam (required = false) String parent,          //integer
        @RequestParam (required = false) String prev,           //Integer
        @RequestParam (required = false) String lane,
        @RequestParam (required = false) String name,
        @RequestParam (required = false) String description,
        @RequestParam (required = false) String startDate,         //Date startDate,
        @RequestParam (required = false) String endDate,           //Date endDate,
        @RequestParam (required = false) String author,            //Long author,
        @RequestParam (required = false) String wuser,          //Integer wuser,
        @RequestParam (required = false) String wgroup,         //Integer wgroup,
        @RequestParam (required = false) String ruser,          //Integer ruser,
        @RequestParam (required = false) String rgroup,         //Integer rgroup,
        @RequestParam (required = false) String eventWindowStyle,//String eventwindowstyle,
        @RequestParam (required = false) String executor,       //Integer executor,
        @RequestParam (required = false) String priority,                          //  Integer priority
        @RequestParam (required = false) String duration,     //Integer duration_d,
        @RequestParam (required = false) String duration_h,     //Integer duration_h,
        @RequestParam (required = false) String icon,            //String icon,
        @RequestParam (required = false) String state,          //Integer state,
        @RequestParam (required = false) String executor_name,   //String executor_name,
        @RequestParam (required = false) String isBacklog,             //Boolean is_backlog,
        @RequestParam (required = false) String spent_time,     //Integer spent_time,
        @RequestParam (required = false) String isGraph        //Boolean is_graph
    ) throws ParseException
    {
        Event event;
        if (id == null)
            event = new Event();
        else
            event = eventRepository.getOne(filterLongValue(id));

        assert event != null : "Cannot find event to update!";

        switch(_operationType){
            case "add":
            case "update":
            {
                event.setParent(filterIntValue(parent));
                event.setPrev(filterIntValue(prev));
                event.setLane(lane);
                event.setName(name);
                event.setDescription(description);

                Date d = filterDateValue(startDate);
//                if (d != null) {
//                    ZonedDateTime t = d.toInstant().atZone(ZoneId.of("Europe/Moscow"));
//                    int hours = t.get(ChronoField.CLOCK_HOUR_OF_DAY);
//                    if (hours > 12) t = t.plus(1, ChronoUnit.DAYS);
//                    t = t.withHour(0);
//                    d = Date.from(t.toInstant());
//                }
                event.setStartDate(d);

                d = filterDateValue(endDate);
//                if (d != null) {
//                    ZonedDateTime t = d.toInstant().atZone(ZoneId.of("Europe/Moscow"));
//                    int hours = t.get(ChronoField.CLOCK_HOUR_OF_DAY);
//                    if (hours < 12)
//                    t = t.minus(1, ChronoUnit.DAYS);
//                    t = t.withHour(23);
//                    d = Date.from(t.toInstant());
//                }
                event.setEndDate(d);

                event.setAuthor(filterLongValue(author));
                event.setWuser(filterIntValue(wuser));
                event.setWgroup(filterIntValue(wgroup));
                event.setRuser(filterIntValue(ruser));
                event.setRgroup(filterIntValue(rgroup));

                event.setEventWindowStyle(eventWindowStyle);
                event.setExecutor(filterIntValue(executor));
                event.setPriority(filterIntValue(priority));
                event.setDuration(filterIntValue(duration));
                event.setDurationH(filterIntValue(duration_h));
                event.setIcon(icon);
                event.setState(filterIntValue(state));
                event.setExecutorName(executor_name);
                event.setIsBacklog(filterBooleanValue(isBacklog));
                event.setSpentTime(filterIntValue(spent_time));
                event.setIsGraph(filterBooleanValue(isGraph));

                event = eventRepository.save(event);
                System.out.println("Event saved");
            } break;
            case "remove":
            {
                eventRepository.delete(event);
                return new GwtResponse(0,1,1,new Event[] {});
            }
        }
        return new GwtResponse(0,1,1,new Event[] {event});
    }
}
