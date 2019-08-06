package org.taurus.aya.server.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.taurus.aya.server.EventRepository;
import org.taurus.aya.server.entity.Event;
import org.taurus.aya.server.services.EventService;
import org.taurus.aya.shared.GwtResponse;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/events")
public class EventController {

    private EventRepository eventRepository;

    private EventService eventService;

    public EventController(@Autowired EventRepository repository, @Autowired EventService service)
    {
        this.eventRepository = repository;
        this.eventService = service;
    }

    //2019-07-19T03:12:27.000
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());

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
        @RequestParam (required = false) String duration_d,     //Integer duration_d,
        @RequestParam (required = false) String duration_h,     //Integer duration_h,
        @RequestParam (required = false) String icon,            //String icon,
        @RequestParam (required = false) String state,          //Integer state,
        @RequestParam (required = false) String executor_name,   //String executor_name,
        @RequestParam (required = false) String isBacklog,             //Boolean is_backlog,
        @RequestParam (required = false) String spent_time,     //Integer spent_time,
        @RequestParam (required = false) String isGraph        //Boolean is_graph
    ) throws ParseException
    {
        Event event = null;
        if (id == null)
            event = new Event();
        else
            event = eventRepository.getOne(filterLongValue(id));

        assert event != null : "Cannot find event to update!";

        switch(_operationType){
            case "add":{}
            case "update":
            {
                event.setParent(filterIntValue(parent));
                event.setPrev(filterIntValue(prev));
                event.setLane(lane);
                event.setName(name);
                event.setDescription(description);
                event.setStartDate(filterDateValue(startDate));
                event.setEndDate(filterDateValue(endDate));
                event.setAuthor(filterLongValue(author));
                event.setWuser(filterIntValue(wuser));
                event.setWgroup(filterIntValue(wgroup));
                event.setRuser(filterIntValue(ruser));
                event.setRgroup(filterIntValue(rgroup));

                event.setEventWindowStyle(eventWindowStyle);
                event.setExecutor(filterIntValue(executor));
                event.setPriority(filterIntValue(priority));
                event.setDurationD(filterIntValue(duration_d));
                event.setDurationH(filterIntValue(duration_h));
                event.setIcon(icon);
                event.setState(filterIntValue(state));
                event.setExecutorName(executor_name);
                event.setIsBacklog(filterBooleanValue(isBacklog));
                event.setSpentTime(filterIntValue(spent_time));
                event.setIsGraph(filterBooleanValue(isGraph));

                event = eventRepository.save(event);
                System.out.println("Event saved");
            }; break;
            case "remove":
            {
                eventRepository.delete(event);
                return new GwtResponse(0,1,1,new Event[] {});
            }
        }
        return new GwtResponse(0,1,1,new Event[] {event});
    }

    private Integer filterIntValue(String value) {

        return value == null || value.equals("null") ? null : Integer.valueOf(value);
    }

    private Boolean filterBooleanValue(String value) {
        return value == null || value.equals("null") ? null : Boolean.valueOf(value);
    }

    private Long filterLongValue(String value) {
        return value == null || value.equals("null") ? null : Long.valueOf(value);
    }

    private Date filterDateValue(String value) throws ParseException {
        return value == null || value.equals("null") ? null : formatter.parse(value);
    }
}
