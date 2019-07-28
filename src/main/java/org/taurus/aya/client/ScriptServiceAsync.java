package org.taurus.aya.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.taurus.aya.shared.ScriptResult;

public interface ScriptServiceAsync {
	public void execute(String script, AsyncCallback<ScriptResult> callback);
}
