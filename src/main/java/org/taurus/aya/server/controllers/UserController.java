package org.taurus.aya.server.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.taurus.aya.server.UserRepository;
import org.taurus.aya.server.entity.Users;
import org.taurus.aya.servlets.UserServiceImpl;
import org.taurus.aya.shared.GwtResponse;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

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
        List<Users> users = new ArrayList<Users>();
        if (_operationType.equals("custom"))
            switch( _operationId) {
                case "fetchByNickname": {
                    if (request.getParameterMap().get("nickname").length==0)
                        throw new RuntimeException("Сервер получил пустое имя пользователя");
                    System.out.println("Fetch by nickname:: " + request.getParameterMap().get("nickname")[0]);
                    users = userRepository.findUsersByNickname(request.getParameterMap().get("nickname")[0]);
                }; break;
                case "fetchByUSID":{
                    if (request.getParameterMap().get("usid").length==0)
                        throw new RuntimeException("Сервер получил пустой USID");
                    System.out.println("Fetch by USID:: " + request.getParameterMap().get("usid")[0]);
                    users = userRepository.findUsersByUsid(request.getParameterMap().get("usid")[0]);
                };break;
            }
        return new GwtResponse(0,0,0,users);
    }

    @PostMapping
    @ResponseBody
    public GwtResponse executePost(
            @RequestParam String _operationType,
        @RequestParam Long id,
        @RequestParam String firstname,
        @RequestParam String surname,
        @RequestParam String patronymic,
        @RequestParam String nickname,
        @RequestParam String workphone,
        @RequestParam String mobphone,
        @RequestParam String usid,
        @RequestParam String passwordHash,
        @RequestParam String showed_name
    )
    {
        Users user = userService.getUser(id);
        if (_operationType.equals("update"))
        {
            user.setFirstname(firstname);
            user.setSurname(surname);
            user.setPatronymic(patronymic);
            user.setNickname(nickname);
            user.setWorkphone(workphone);
            user.setMobphone(mobphone);
            user.setUsid(usid);
            user.setPasswordHash(passwordHash);
            user.setShowedName(showed_name);
            userRepository.save(user);
            System.out.println("User saved");
        }
        Users[] users = {user};
        return new GwtResponse(0,1,1,users);
    }
}
