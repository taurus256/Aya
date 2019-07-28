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
public class LaneController {

    private LaneRepository laneRepository;

    public LaneController(@Autowired LaneRepository repository)
    {
        this.laneRepository = repository;
    }

    @ResponseBody
    @GetMapping()
    public GwtResponse execute(HttpServletRequest request, @RequestParam String _operationType, String _operationId) throws RuntimeException
    {

        System.out.println("Operation_type=" + _operationType);
        System.out.println("request body is:" + _operationType);
        List<Lane> lanes = new ArrayList<>();
        if (_operationType.equals("fetch")) {
            lanes = laneRepository.findAll();
        }
//            switch( _operationId) {
//                case "fetchByNickname": {
//                    if (request.getParameterMap().get("nickname").length==0)
//                        throw new RuntimeException("Сервер получил пустое имя пользователя");
//                    System.out.println("Fetch by nickname:: " + request.getParameterMap().get("nickname")[0]);
//                    users = userRepository.findUsersByNickname(request.getParameterMap().get("nickname")[0]);
//                }; break;
//                case "fetchByUSID":{
//                    if (request.getParameterMap().get("usid").length==0)
//                        throw new RuntimeException("Сервер получил пустой USID");
//                    System.out.println("Fetch by USID:: " + request.getParameterMap().get("usid")[0]);
//                    users = userRepository.findUsersByUsid(request.getParameterMap().get("usid")[0]);
//                };break;
//            }
        return new GwtResponse(0,lanes.size(),lanes.size(),lanes);
    }

    @PostMapping
    @ResponseBody
    public GwtResponse executePost(
            @RequestParam String _operationType,
        @RequestParam Long id,
        @RequestParam Integer parent,
        @RequestParam String name,
        @RequestParam String description,
        @RequestParam Integer lane_order,
        @RequestParam Boolean visible,
        @RequestParam Long author,
        @RequestParam Boolean is_folder,
//        @RequestParam Boolean lane,
        @RequestParam Integer wuser,
        @RequestParam Integer wgroup,
        @RequestParam Integer ruser,
        @RequestParam Integer rgroup

    )
    {
        Optional<Lane> optLane = laneRepository.findById(id);

        if (!optLane.isPresent()) throw new RuntimeException("Cannot find lane with id=" + id);
        Lane lane = optLane.get();
        switch(_operationType){
            case "add":
            case "update":
            {
                lane.setParent(parent);
                lane.setName(name);
                lane.setDescription(description);
                lane.setLaneOrder(lane_order);
                lane.setVisible(visible);
                lane.setAuthor(author);
                lane.setIsfolder(is_folder);
                lane.setWuser(wuser);
                lane.setWgroup(wgroup);
                lane.setRuser(ruser);
                lane.setRgroup(rgroup);

                laneRepository.save(lane);
                System.out.println("Lane saved");
            }; break;
            case "delete":
            {
                laneRepository.delete(lane);
            }
        }
        Lane[] lanes = {lane};
        return new GwtResponse(0,0,0,lanes);
    }
}
