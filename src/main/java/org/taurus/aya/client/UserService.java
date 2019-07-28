package org.taurus.aya.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.taurus.aya.shared.UserDTO;

import java.util.List;

@RemoteServiceRelativePath("user")
public interface UserService extends RemoteService {
    public List<UserDTO> findUsersByNickname(String nickname);
    public void updateUSID(Long id, String USID);
    public List<UserDTO> getUserBuUSID(String USID) throws RuntimeException;
}

