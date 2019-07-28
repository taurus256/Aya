package org.taurus.aya.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.ArrayList;

public class ScriptResult implements IsSerializable {
	
	private String log;
	private ArrayList<Command> commandList;
	
	public ScriptResult(){}
	
	public ScriptResult(String log, ArrayList<Command> list)
	{
		this.log = log;
		this.commandList = list;
	}
	
	
	public String getLog() {
		return log;
	}

	public ArrayList<Command> getCommandList() {
		return commandList;
	}
}
