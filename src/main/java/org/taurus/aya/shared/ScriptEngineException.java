package org.taurus.aya.shared;

import java.io.Serializable;

public class ScriptEngineException extends Exception implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String description;
	
	public ScriptEngineException()
	{
	}
	
	public ScriptEngineException(String description)
	{
		this.description = description;
	}

	public String getMessage() {
		return description;
	}

}
