package org.taurus.aya.client;

import com.smartgwt.client.util.SC;
import org.taurus.aya.shared.Command;

/**
 * This class is used for process incoming system messages: it either executes commands immediately or indicate them on the statusBar  
 * */
public class CommandRouter {
	static boolean executeImmediately;

	public static boolean isExecuteImmediately() {
		return executeImmediately;
	}

	public static void setExecuteImmediately(boolean executeImmediately) {
		CommandRouter.executeImmediately = executeImmediately;
	}
	
	public static void processCommand(final Command command)
	{
		SC.logWarn("StatusBar: Adding notification for command " + command.getType().toString());
		if (executeImmediately)
			CommandExecutor.exec(command);
		else
			GlobalData.getStatusBar().addNotification(command);
	}

}
