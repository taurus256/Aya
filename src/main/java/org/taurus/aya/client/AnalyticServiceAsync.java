package org.taurus.aya.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.taurus.aya.shared.GraphData;
import org.taurus.aya.shared.TaskAnalyseData;

public interface AnalyticServiceAsync {
    public void getPrognosis(Long userId, AsyncCallback<TaskAnalyseData> callback) throws Exception;
    public void getMonthGraph(Long userId, AsyncCallback<GraphData> callback) throws IllegalArgumentException;
}
