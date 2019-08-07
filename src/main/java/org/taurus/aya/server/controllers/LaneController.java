package org.taurus.aya.server.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.taurus.aya.server.LaneRepository;
import org.taurus.aya.server.entity.Lane;
import org.taurus.aya.shared.GwtResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/lanes")
public class LaneController extends GenericController {

    private LaneRepository laneRepository;

    public LaneController(@Autowired LaneRepository repository)
    {
        this.laneRepository = repository;
    }

    @ResponseBody
    @PostMapping("fetch")
    public GwtResponse execute(HttpServletRequest request, @RequestParam String _operationType, String _operationId) throws RuntimeException
    {

        System.out.println("Operation_type=" + _operationType);
        System.out.println("request body is:" + _operationType);
        List<Lane> lanes = new ArrayList<>();
        if (_operationType.equals("fetch")) {
            lanes = laneRepository.findAll();
        }

        return new GwtResponse(0,lanes.size(),lanes.size(),lanes);
    }

    @PostMapping("modify")
    @ResponseBody
    public GwtResponse executePost(
        @RequestParam String _operationType,
        @RequestParam (required = false) Long id,
        @RequestParam (required = false) String parent,
        @RequestParam (required = false) String name,
        @RequestParam (required = false) String description,
        @RequestParam (required = false) String laneOrder,
        @RequestParam (required = false) String visible,
        @RequestParam (required = false) String author,
        @RequestParam (required = false) String isFolder,
//      @RequestParam (required = false) Boolean lane,
        @RequestParam (required = false) String wuser,
        @RequestParam (required = false) String wgroup,
        @RequestParam (required = false) String ruser,
        @RequestParam (required = false) String rgroup

    )
    {
        Lane lane;

        if (id == null)
            lane = new Lane();
        else {
            Optional<Lane> optLane = laneRepository.findById(id);
            if (!optLane.isPresent()) throw new RuntimeException("Cannot find lane with id=" + id);
            lane = optLane.get();
        }

        switch(_operationType){
            case "add":
            case "update":
            {
                lane.setParent(filterIntValue(parent));
                lane.setName(name);
                lane.setDescription(description);
                lane.setLaneOrder(filterIntValue(laneOrder));
                lane.setVisible(filterBooleanValue(visible));
                lane.setAuthor(filterLongValue(author));
                lane.setIsFolder(filterBooleanValue(isFolder));
                lane.setWuser(filterIntValue(wuser));
                lane.setWgroup(filterIntValue(wgroup));
                lane.setRuser(filterIntValue(ruser));
                lane.setRgroup(filterIntValue(rgroup));

                laneRepository.save(lane);
                System.out.println("Lane saved");
            } break;
            case "remove":
            {
                laneRepository.delete(lane);
            }
        }
        Lane[] lanes = {lane};
        return new GwtResponse(0,lanes.length,lanes.length,lanes);
    }
}
