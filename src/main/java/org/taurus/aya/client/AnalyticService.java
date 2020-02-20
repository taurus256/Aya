package org.taurus.aya.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.taurus.aya.shared.GraphData;
import org.taurus.aya.shared.TaskAnalyseData;

@RemoteServiceRelativePath("analytic")
public interface AnalyticService extends RemoteService {
    public TaskAnalyseData getPrognosis(Long userId) throws Exception;
    public GraphData getMonthGraph(Long userId) throws IllegalArgumentException;
}
