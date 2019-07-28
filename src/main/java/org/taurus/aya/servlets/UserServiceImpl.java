package org.taurus.aya.servlets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.taurus.aya.server.UserRepository;
import org.taurus.aya.server.entity.Users;
import org.taurus.aya.shared.UserDTO;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

//@WebServlet(urlPatterns = "/user", loadOnStartup = 1)
@Service
public class UserServiceImpl /*extends RemoteServiceServlet implements UserService*/ {

    @Autowired
    UserRepository userRepository;

    public List<UserDTO> findUsersByNickname(String nickname)
    {
        return userRepository.findUsersByNickname(nickname).stream().map(this::convertUsersToUserDTO).collect(Collectors.toList());
    }

    public void updateUSID(Long id, String USID)
    {
        Optional<Users> u = userRepository.findById(id);
        if (!u.isPresent())
            throw new RuntimeException("Cannot find user with ID=" + id);
        else
            u.get().setUsid(USID);
        userRepository.save(u.get());
    }

    public List<UserDTO> getUserBuUSID(String USID) throws RuntimeException
    {
        List<Users> users = userRepository.findUsersByUsid(USID);
        if (users.size() > 1)
            throw new RuntimeException("Пользователей с данным USID больше оддного");
        else
            return users.stream().map(this::convertUsersToUserDTO).collect(Collectors.toList());
    }

    public Users getUser(Long id)
    {
        Optional<Users> optUser = userRepository.findById(id);
        if (!optUser.isPresent()) throw new RuntimeException("Cannot find user with id=" + id);
        return optUser.get();
    }

    private UserDTO convertUsersToUserDTO(Users u)
    {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(u.getId());
        userDTO.setFirstname(u.getFirstname());
        userDTO.setSurname(u.getSurname());
        userDTO.setPatronymic(u.getPatronymic());
        userDTO.setNickname(u.getNickname());
        userDTO.setWorkphone(u.getWorkphone());
        userDTO.setMobphone(u.getMobphone());
        userDTO.setUsid(u.getUsid());
        userDTO.setPasswordHash(u.getPasswordHash());
        userDTO.setShowed_name(u.getShowedName());
        return userDTO;
    }
}
