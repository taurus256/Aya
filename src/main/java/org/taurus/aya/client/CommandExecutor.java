package org.taurus.aya.client;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.data.*;
import com.smartgwt.client.util.SC;
import org.taurus.aya.client.TabManager.ResourceType;
import org.taurus.aya.client.dialogs.BacklogTaskDialog;
import org.taurus.aya.shared.Command;
import org.taurus.aya.shared.Command.CommandType;

import java.util.ArrayList;

public class CommandExecutor {
	
	public CommandExecutor(){}

	
	// Исполняет всего одну команду, но делает это хорошо
	public static void exec(Command c)
	{
		SC.logWarn("CommandExecutor: " + c.getType().toString() + " " + c.getMessage());
		switch (c.getType())
		{
			case UPDATE_LANES: updateLanes(); break;
			case UPDATE_TASK_ARRANGEMENT: updateTaskViews(); break;
			case UPDATE_TASK_STATE: updateTasks(); break;
			case UPDATE_TASK_PANEL: updateTasksPanel(); break;
			case UPDATE_DOC_LIST: updateDocList(); break;
			case UPDATE_SCRIPT_LIST: updateScriptList(); break;
			case SHOW_MESSAGE: { showMessage(c.getMessage());} break;
			case USER_CONNECT: {userConnect(c.getId());} break;
			case USER_DISCONNECT: {userDisconnect(c.getId());} break;
			case UPDATE_GROUP_LIST: {updateGroups();} break;
			case OPEN_USER_CHAT: openDialog(c.getId()); break;
			case OPEN_GROUP_CHAT: openGroupDialog(c.getId()); break;
			case OPEN_RESOURCE: openResource(c.getResourceType(),c.getId()); break;
			case CREATE_LINK: createLink(c.getResourceType(),c.getId(),c.getMessage()); break;
			case CREATE_BACKLOG_TASK: createBacklogTask(); break;
			default: break;
		}
		SC.logWarn("CommandExecutor: " + c.getType().toString() + " OK");
	}
	
	// исполняет список команд 
	public static void load(ArrayList<Command> commandList)
	{
		SC.logWarn("CommandExecutor: receive commands:" + commandList.size());
		
		for (Command c: commandList)
		{
			exec(c);
		}
		GlobalData.getStatusBar().indicateMessage("Выполнение скрипта завершено");
	}
	
	/*Функция-обертка для вызова из JavaScript. Принимает только строки и целые числа как параметры*/
	public static void jsExec(String strCommandType, String strResourceType, int id)
	{
		CommandType commandType = CommandType.valueOf(strCommandType);
		ResourceType resourceType = ResourceType.valueOf(strResourceType);
		Command c = new Command(commandType,resourceType,"called from JS",id);
		exec(c);
	}
	
	private static void updateLanes()
	{
		TabManager.updateAllOpen(ResourceType.GRAPH, true);
	}
	
	private static void updateTaskViews()
	{
		TabManager.updateAllOpen(ResourceType.TASK,false);
	}

	private static void updateTasks()
	{
		GlobalData.getNavigationArea().getTaskPanel().update();
		TabManager.updateAllOpen(ResourceType.TASK,false);
	}

	private static void updateTasksPanel()
	{
		GlobalData.getNavigationArea().getTaskPanel().update();
	}
	
	private static void updateDocList()
	{
		SC.logWarn("CommandExecutor: updateDocList");
		GlobalData.getNavigationArea().getDocPanel().update();
	}
	
	private static void updateScriptList()
	{
		SC.logWarn("CommandExecutor: updateScriptList");
	}
	
	private static void showMessage(String message)
	{
		SC.logWarn("CommandExecutor: showMessage");
			SC.say(message);
	}
	
	private static void userConnect(Integer id)
	{
		SC.logWarn("CommandExecutor: userConnect:" + id);
		if (id == null) return;
		GlobalData.getNavigationArea().getChatPanel().userConnected(id);
	}
	
	private static void userDisconnect(Integer id)
	{
		SC.logWarn("CommandExecutor: userDisconnect: " + id);
		if (id == null) return;
		GlobalData.getNavigationArea().getChatPanel().userDisconnected(id);
	}
	
	private static void updateGroups()
	{
		Criteria criteria_group = new Criteria();
		criteria_group.addCriteria("userid", GlobalData.getCurrentUser().getAttributeAsInt("id"));
		
		GlobalData.getDataSource_relation_user_group().fetchData(criteria_group,new DSCallback()
		{
			public void execute(DSResponse dsResponse, Object data, DSRequest dsRequest) {
				GlobalData.setCurrentUserGroups(dsResponse.getData());
			}
		});
	}
	
	private static void openDialog(final Integer remoteUserId)
	{
		Integer currentUserId = GlobalData.getCurrentUser().getAttributeAsInt("id");
		final int firstUser,secondUser;
		if (currentUserId < remoteUserId)
		{
			firstUser = currentUserId;
			secondUser = remoteUserId;
		}
		else
		{
			firstUser = remoteUserId;
			secondUser = currentUserId;
		}

		Criteria crit = new Criteria();
		crit.addCriteria("first_user", firstUser);
		crit.addCriteria("second_user", secondUser);
		GlobalData.getDataSource_dialogs().fetchData(crit, new DSCallback(){

			@Override
			public void execute(DSResponse dsResponse, Object data,
                                DSRequest dsRequest) {
				if (dsResponse.getData().length == 0) // dialog is not found. Create the new one.
				{
					SC.logWarn("Dialog for users " + String.valueOf(firstUser) + " and " +  String.valueOf(secondUser) + " is not found. Create the new one.");
					
					Record dialog = new Record();
					dialog.setAttribute("first_user", firstUser);
					dialog.setAttribute("second_user", secondUser);
					GlobalData.getDataSource_dialogs().addData(dialog, new DSCallback(){

						@Override
						public void execute(DSResponse dsResponse, Object data,
                                            DSRequest dsRequest) {
							
							if (dsResponse.getData().length == 1)
							{
								openUserChatView(dsResponse.getData()[0],remoteUserId);
							}
							else
								SC.warn("Не удалось создать диалог!");
							
						}});
				}
				else
					if (dsResponse.getData().length == 1)
						openUserChatView(dsResponse.getData()[0],remoteUserId);
					else
						SC.logWarn("There are many that one dialog in the dialogs table");
				
			}});
	}

	
	private static void  openGroupDialog(final Integer groupId)
	{
		SC.logWarn("ChatPanel: openGroupDialog");
		Criteria crit = new Criteria();
		crit.addCriteria("group_id", groupId);
		GlobalData.getDataSource_dialogs().fetchData(crit, new DSCallback(){

			@Override
			public void execute(DSResponse dsResponse, Object data,
                                DSRequest dsRequest) {
				if (dsResponse.getData().length == 0) // dialog is not found. Create the new one.
				{
					SC.logWarn("Dialog for group " + String.valueOf(groupId), "S5_messaging");
					
					Record dialog = new Record();
					dialog.setAttribute("group_id", groupId);
					GlobalData.getDataSource_dialogs().addData(dialog, new DSCallback(){

						@Override
						public void execute(DSResponse dsResponse, Object data,
                                            DSRequest dsRequest) {
							
							if (dsResponse.getData().length == 1)
							{
								openGroupChatView(dsResponse.getData()[0],groupId);
							}
							else
								SC.warn("Не удалось создать диалог!");
							
						}});
				}
				else
					if (dsResponse.getData().length == 1)
						openGroupChatView(dsResponse.getData()[0],groupId);
					else
						SC.logWarn("There are many than one dialog in the dialogs table", "S5_messaging");
				
			}});
	}
	
	private static void openUserChatView(Record r, Integer remoteUserId)
	{
		for (Record k: GlobalData.getUsers())
			if (k.getAttributeAsInt("id").equals(remoteUserId))
			{
				r.setAttribute("showedName",k.getAttributeAsString("showedName")); break;
			}
		TabManager.openTab(ResourceType.CHAT, r);
	}
	
	private static void openGroupChatView(Record r, Integer groupId)
	{
		for (Record k: GlobalData.getCurrentUserGroups())
			if (k.getAttributeAsInt("id").equals(groupId))
			{
				r.setAttribute("showedName",k.getAttributeAsString("description")); break;
			}
		TabManager.openTab(ResourceType.CHAT, r);	
	}
	
	private static void openResource(ResourceType resType,Integer id)
	{
		TabManager.openResource(resType,id);
	}
	
	private static void createLink(ResourceType resourceType,Integer id, String resourceName)
	{
		String strTitle = "";
		switch (resourceType)
		{
		case DOCUMENT: strTitle = "Ниже приведены ссылки на документ \"" + resourceName + "\"";
			break;
		case SCRIPT: strTitle = "Ниже приведены ссылки на скрипт \"" + resourceName + "\"";
			break;
		case TAG: strTitle = "Ниже приведены ссылки на тег \"" + resourceName + "\"";
			break;
		case TASK: strTitle = "Ниже приведены ссылки на задачу \"" + resourceName + "\""; break;
		default: return;
		}
		
		String httpLinkCode = GWT.getHostPageBaseURL() + "catcher?resourceType=" + resourceType.toString() + "&resourceId=" + id;
		String htmlLinkCode = "&lt;a href=\"" + httpLinkCode + "\" target=\"_blank\"&gt;" + resourceName + "&lt;/a&gt;";
		SC.say("Ссылки",strTitle + "<br/><br/>Для браузера:<br/><br/><b>" +  httpLinkCode + "</b><br/><br/>Для использования в S5:<br/><br/><b>" + htmlLinkCode + "</b>");

	}

	private static void createBacklogTask()
	{
		Record r = new Record();
		r.setAttribute("parent",0);
		r.setAttribute("author",GlobalData.getCurrentUser().getAttribute("id"));
		r.setAttribute("rgroup",GlobalData.ACCESS_ALL);
		r.setAttribute("wgroup",GlobalData.ACCESS_ALL);
		r.setAttribute("isBacklog", true);
		SC.logWarn("Set date for task");
		BacklogTaskDialog btd = new BacklogTaskDialog(r);
	}
}
