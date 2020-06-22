package org.taurus.aya.server.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.taurus.aya.server.UserRepository;
import org.taurus.aya.server.entity.User;
import org.taurus.aya.servlets.UserServiceImpl;
import org.taurus.aya.shared.GwtResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController extends GenericController {

    UserRepository userRepository;

    public UserController(@Autowired UserRepository repository)
    {
        this.userRepository = repository;
    }

    @Autowired
    UserServiceImpl userService;

    @ResponseBody
    @GetMapping()
    public GwtResponse execute(HttpServletRequest request, @RequestParam String _operationType, String _operationId) throws RuntimeException
    {

        System.out.println("Operation_type=" + _operationType);
        System.out.println("request body is:" + _operationType);
        List<User> users = new ArrayList<User>();
        if (_operationType.equals("custom"))
            switch( _operationId) {
                case "fetchByNickname": {
                    if (request.getParameterMap().get("nickname").length==0)
                        throw new RuntimeException("Сервер получил пустое имя пользователя");
                    System.out.println("Fetch by nickname:: " + request.getParameterMap().get("nickname")[0]);
                    users = userRepository.findUserByNickname(request.getParameterMap().get("nickname")[0]);
                }; break;
                case "fetchByUSID":{
                    if (request.getParameterMap().get("usid").length==0)
                        throw new RuntimeException("Сервер получил пустой USID");
                    System.out.println("Fetch by USID:: " + request.getParameterMap().get("usid")[0]);
                    users = userRepository.findUserByUsid(request.getParameterMap().get("usid")[0]);
                };break;
                case "fetchAllDomainUsers": {
                    System.out.println("Fetch all by current domain:: ");
                    users = userRepository.findAll();
                }; break;
            }
        return new GwtResponse(0,0,0,users);
    }

    @PostMapping("/fetch")
    @ResponseBody
    public GwtResponse getUsers(HttpServletRequest request, @RequestParam String _operationType, @RequestParam (required=false) String[] criteria){
        List<User> users = userRepository.findAll(Sort.by("nickname"));
        return new GwtResponse(0, users.size() -1, users.size(), users);
    }

    @PostMapping("/modify")
    @ResponseBody
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public GwtResponse executePost(
            @RequestParam String _operationType,
        @RequestParam (required = false)  Long id,
        @RequestParam (required = false) String firstname,
        @RequestParam (required = false)  String surname,
        @RequestParam (required = false)  String patronymic,
        @RequestParam (required = false)  String nickname,
        @RequestParam (required = false)  String workphone,
        @RequestParam (required = false)  String mobphone,
        @RequestParam (required = false)  String usid,
        @RequestParam (required = false)  String passwordHash
    )
    {
        User user;
        if (id==null)
            user = new User();
        else
            user = userService.getUser(id);

        switch (_operationType)
        {
            case "add":
            case "update":{
                user.setFirstname(firstname);
                user.setSurname(surname);
                user.setPatronymic(patronymic);
                user.setNickname(nickname);
                user.setWorkphone(filterStringValue(workphone));
                user.setMobphone(filterStringValue(mobphone));
                user.setUsid(usid);
                user.setPasswordHash(passwordHash);
                userRepository.save(user);
                System.out.println("User saved");
                User[] users = {user};
                return new GwtResponse(0,1,1,users);
            }
            case "remove":{
                userRepository.delete(user);
                return new GwtResponse(0,0,0,new User[]{} );
            }
            default: return new GwtResponse(0,0,0,new User[]{} );
        }
    }
}
