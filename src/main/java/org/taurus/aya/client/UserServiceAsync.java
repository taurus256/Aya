package org.taurus.aya.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.smartgwt.client.data.Record;
import org.taurus.aya.shared.UserDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface UserServiceAsync {
    void findUsersByNickname(String nickname, AsyncCallback<List<UserDTO>> async);
    void updateUSID(Long id, String USID, AsyncCallback<Void> async);

    void getUserBuUSID(String USID, AsyncCallback<List<UserDTO>> async);
}
