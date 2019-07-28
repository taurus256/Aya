package org.taurus.aya.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.taurus.aya.shared.ScriptEngineException;
import org.taurus.aya.shared.ScriptResult;

@RemoteServiceRelativePath("script")
public interface ScriptService extends RemoteService {
	public ScriptResult execute(String script) throws ScriptEngineException;
}
