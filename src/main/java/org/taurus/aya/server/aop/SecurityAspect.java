package org.taurus.aya.server.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;
import org.taurus.aya.server.UserRepository;
import org.taurus.aya.server.entity.Event;
import org.taurus.aya.server.entity.Group;
import org.taurus.aya.server.entity.User;
import org.taurus.aya.shared.GwtResponse;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Aspect
@Component
public class SecurityAspect {
//    @Pointcut("within(org.taurus.aya.server.services.EventService)")
//    public void inEventService(){}

    UserRepository userRepository;

    public SecurityAspect(@Autowired UserRepository userRepository)
    {
        this.userRepository = userRepository;
    }
//
//    @Pointcut(value = "within(org.taurus.aya.server.controllers.EventController)  && args(request, operationType, criteria)")
//    public void getEventsPointcut(HttpServletRequest request, String operationType, String[] criteria) {}

    @Around("execution(* org.taurus.aya.server.controllers.EventController.getEvents(..))  && args(request, operationType, criteria)")
    public Object filterData(ProceedingJoinPoint pjp, HttpServletRequest request, String operationType, String[] criteria) throws Throwable
    {
        System.out.println("Data filtering...");

        GwtResponse gwtResponse = (GwtResponse)pjp.proceed(new Object[]{request, operationType,criteria});

        String usid = Arrays.stream(request.getCookies()).filter(c -> c.getName().equals("usid")).map(Cookie::getValue).findFirst().orElseThrow(() -> new RuntimeException("Не могу прочитать USID"));
        List<User> users =userRepository.findUserByUsid(usid);
        if (users.size() != 1) throw new RuntimeException("Неверное число пользователей ( " + users.size() + ") с USID " + usid);
        User user = users.get(0);
        Set<Long> groups = user.getGroups().parallelStream().map(Group::getId).collect(Collectors.toSet());
        List<Event> events = ((List<Event>) gwtResponse.getData()).stream().filter(
                e -> e.getExecutor().equals(user.getId()) || e.getRgroup()== null || groups.contains(e.getRgroup())
                ).collect(Collectors.toList());

        gwtResponse.setData(events);
        System.out.println("retValList.data() = " + gwtResponse.getData());
        return new GwtResponse(0,events.size(),events.size(),events);
    }
}
