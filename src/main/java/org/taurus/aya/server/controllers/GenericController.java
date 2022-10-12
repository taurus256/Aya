package org.taurus.aya.server.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.taurus.aya.server.UserRepository;
import org.taurus.aya.server.entity.Group;
import org.taurus.aya.server.entity.User;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/** SmartGWT sends string 'null' instead of null value.
 *  Methods below created for setting proper values in these cases
 * */
public class GenericController {

    //Format date based on template (old): 2019-07-19T03:12:27.000 - "yyyy-MM-dd'T'HH:mm:ss"
    //Now use short format for date-only
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    public ObjectMapper objectMapper = new ObjectMapper();

    protected String filterStringValue(String  value) {
        return value == null || value.equals("null") ? "" : value;
    }

    protected Integer filterIntValue(String value) {

        return StringUtils.isEmpty(value) || value.equals("null") ? null : Integer.valueOf(value);
    }

    protected Double filterDoubleValue(String value) {

        return StringUtils.isEmpty(value) || value.equals("null") ? null : Double.valueOf(value);
    }

    protected Boolean filterBooleanValue(String value) {
        return StringUtils.isEmpty(value) || value.equals("null") ? null : Boolean.valueOf(value);
    }

    protected Long filterLongValue(String value) {
        return StringUtils.isEmpty(value) || value.equals("null") ? null : Long.valueOf(value);
    }

    protected Date filterDateValue(String value) throws ParseException {
        return StringUtils.isEmpty(value) || value.equals("null") ? null : formatter.parse(value);
    }

    protected LocalDateTime filterLocalDateTimeValue(String value) throws ParseException {
        return StringUtils.isEmpty(value) || value.equals("null") ? null : LocalDateTime.ofInstant(formatter.parse(value).toInstant(), ZoneId.systemDefault());
    }

    public HashMap<String,String> parseCriteriaString(String[] criterias) throws IOException {

        HashMap<String,String> result = new HashMap<>();

        for (String crit: criterias)
            parseCriteria(objectMapper.readTree(crit),result);

        return result;
    }

    private void parseCriteria(JsonNode node, Map<String,String> result) throws IOException {

        if (node.has("fieldName") && node.has("value"))
            result.put(node.get("fieldName").textValue(), node.get("value").asText());
        if (node.has("criteria"))
            if (node.get("criteria").isArray())
                for (int e=0; e<node.get("criteria").size(); e++) {
                    JsonNode elem = node.get("criteria").get(e);
                    parseCriteria(elem,result);
                }
            else
                parseCriteria(node,result);
    }

    GenericUserData getUserData(HttpServletRequest request, UserRepository userRepository) {
        User user = getUser(request, userRepository);
        List<Long> groups = user.getGroups().parallelStream().map(Group::getId).collect(Collectors.toList());
        return new GenericUserData(user.getId(),groups);
    }

    protected User getUser(HttpServletRequest request, UserRepository userRepository) {
        if (request.getCookies() == null) throw new RuntimeException("There are no USID cookie!");

        String usid = Arrays.stream(request.getCookies()).filter(c -> c.getName().equals("usid")).map(Cookie::getValue).findFirst().orElseThrow(() -> new RuntimeException("Не могу прочитать USID"));
        List<User> users = userRepository.findUserByUsid(usid);
        if (users.size() != 1) throw new RuntimeException("Неверное число пользователей ( " + users.size() + ") с USID " + usid);
        User user = users.get(0);
        return user;
    }
}
