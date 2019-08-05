package org.taurus.aya.server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.taurus.aya.server.UserRepository;
import org.taurus.aya.server.entity.Group;
import org.taurus.aya.server.entity.User;
import org.taurus.aya.shared.GwtResponse;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/groups")
public class GroupController {

    UserRepository userRepository;

    public GroupController(@Autowired UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @ResponseBody
    @PostMapping("/fetch")
    public GwtResponse getGroupsList(HttpServletRequest request, @RequestParam String _operationType, @RequestParam (required=false) String[] criteria)
    {
        String usid = null;
        for (Cookie cookie: request.getCookies())
            if (cookie.getName().equals("usid")) usid = cookie.getValue();
        if (usid == null) throw new RuntimeException("GroupController: Cannot find USID cookie!");

        List<User> u =  userRepository.findUserByUsid(usid);

        if (u.size()==0) throw new RuntimeException("GroupController: Cannot find user by USID: " + usid);
        assert (u.size()==1): "There is more than 1 user with USID: " + usid;

        List <Group> groups = u.get(0).getGroups();

        return new GwtResponse(0,groups.size(),groups.size(),groups);
    }
}
