package org.taurus.aya.server.controllers;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.HibernateProxyHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.taurus.aya.server.GroupRepository;
import org.taurus.aya.server.UserRepository;
import org.taurus.aya.server.entity.Group;
import org.taurus.aya.server.entity.User;
import org.taurus.aya.shared.GwtResponse;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/user_groups")
public class UserGroupController extends GenericController{

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    UserRepository userRepository;

    public UserGroupController(@Autowired UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @ResponseBody
    @PostMapping("/fetch")
    public GwtResponse getUserGroupsList(HttpServletRequest request, @RequestParam String _operationType, @RequestParam (required=true) Long userid) throws IOException {
        Optional<User> optUser = userRepository.findById(userid);
        if (!optUser.isPresent()) throw new IllegalArgumentException("Нет пользователя с таким userid!");
        Set<Group> groups = optUser.get().getGroups();

        return new GwtResponse(0,groups.size(),groups.size(),groups);
    }

    @ResponseBody
    @PostMapping("/modify")
    public GwtResponse modifyGroup(HttpServletRequest request,
                                   @RequestParam String _operationType,
                                   @RequestParam (required = false)  String id
                                   ){

        User user = getCurrentUserFromCookie(request);

        Group group;
        Long idLong = filterLongValue(id);
        if (idLong == null)
            throw new IllegalArgumentException("Group ID cannot be NULL!");
        else
            group = groupRepository.getOne(idLong);

        switch(_operationType){
            case "add":{
                if (user.getGroups().contains(group))
                    return new GwtResponse(0,0,0,new Group[]{});

                user.getGroups().add(group);
                userRepository.save(user);
                if (group instanceof HibernateProxy)
                    group = (Group) ((HibernateProxy) group).getHibernateLazyInitializer().getImplementation();

                return new GwtResponse(0,1,1,new Group[]{group});
            }
            case "remove": {
                user.getGroups().remove(group);
                userRepository.save(user);
                return new GwtResponse(0,0,0,new Group[]{});
            }
            default: return new GwtResponse(0,0,0,new Group[]{});
        }
    }

    private User getCurrentUserFromCookie(HttpServletRequest request) {
        String usid = null;
        for (Cookie cookie: request.getCookies())
            if (cookie.getName().equals("usid")) usid = cookie.getValue();
        if (usid == null) throw new RuntimeException("GroupController: Cannot find USID cookie!");

        List<User> u =  userRepository.findUserByUsid(usid);

        if (u.size()==0) throw new RuntimeException("GroupController: Cannot find user by USID: " + usid);
        assert (u.size()==1): "There is more than 1 user with USID: " + usid;
        return u.get(0);
    }


}
