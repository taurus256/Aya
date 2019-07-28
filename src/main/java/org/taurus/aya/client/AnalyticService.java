package org.taurus.aya.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.taurus.aya.shared.TaskAnalyseData;

@RemoteServiceRelativePath("analytic")
public interface AnalyticService extends RemoteService {
    public TaskAnalyseData getPrognosis() throws Exception;
}
