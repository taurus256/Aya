package org.taurus.aya.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.taurus.aya.client.TabManager.ResourceType;

/** Класс, представляющий команду S5. Команда - это некое действие, выполяемое пользователем
 * Параметры:
 * @param type тип команды,
 * @param type тип ресурса,
 * @param id идентификатор ресурса
 * @param message сообщение
 * Команды передаются по сети, поэтому типы полей в команде должны иметь возможность трансформироваться в примитивные типы
 * */
public class Command implements IsSerializable {

	public enum CommandType {
		UPDATE_LANES,		//fully re-open all opened TaskView 
		UPDATE_TASK_STATE, // Update task list on side panel and all opened TaskView (but dont 'update lane order)
		UPDATE_TASK_ARRANGEMENT, // Update only tasks in all opened TaskView (but dont 'update lane order and task list on the side panel)
		UPDATE_TASK_PANEL, // Update only tasks on the side panel
		UPDATE_DOC_LIST, 
		UPDATE_SCRIPT_LIST,
		SHOW_MESSAGE, 
		USER_CONNECT, 
		USER_DISCONNECT,
		UPDATE_GROUP_LIST,
		OPEN_USER_CHAT,
		OPEN_GROUP_CHAT,
		OPEN_RESOURCE,
		CREATE_LINK,
		CREATE_BACKLOG_TASK,
	}
	
	private String type;
	private Integer id = null;
	private String message = "";
	private String resourceTypeString;
	
	public Command(){}
	
	public Command(CommandType type, String message, Integer id)
	{
		this.type = type.name();
		this.id = id;
		this.message = message;
	}

	public Command(CommandType type, ResourceType resourceType, String message, Integer id)
	{
		this.type = type.name();
		this.resourceTypeString = resourceType.name();
		this.id = id;
		this.message = message;
	}
	
	public Command(CommandType type, String message)
	{
		this.message = message;
		this.type = type.name();
	}
	
	public Command(CommandType type)
	{
		this.message = "";
		this.type = type.name();
	}
	public String getMessage() {
		return message;
	}
	
	public Integer getId()
	{
		return id;
	}

	public CommandType getType() {
		return CommandType.valueOf(type);
	}

	public ResourceType getResourceType()
	{
		return ResourceType.valueOf(resourceTypeString);
	}
	
	public String getResourceTypeString() {
		return resourceTypeString;
	}

	
}
