package org.taurus.aya.client;

import com.smartgwt.client.data.Record;
import com.smartgwt.client.util.SC;
import org.taurus.aya.client.TabManager.ResourceType;
import org.taurus.aya.shared.Command;
import org.taurus.aya.shared.Command.CommandType;

/***
 * Этот объект содержит методы для выполнения стандартных действий при создании/удалении/обновлении свойств ресурсов.
 * Под этими действиями понимается вывод и рассылка разного рода сообщений.
 * Необходимость в нем появилась потому, что эти действия с ресурсами могут быть инициированы пользователями из разных объектов приложения
 * (например, задача может быть создана как с помощью диалога создания ресурса, так и с помощью "рисования" мышью на TimeLine)  
 */

public class ResourceLifeCycleManager {
	public static void resourceCreated(ResourceType resType, Record record)
	{
		switch (resType){
		case DOCUMENT:{ 
						GlobalData.getStatusBar().indicateMessage("Документ создан");
							sendSystemMessage(CommandType.UPDATE_DOC_LIST, ResourceType.DOCUMENT, "Пользователь <b>" + GlobalData.getCurrentUser().getAttributeAsString("nickname") + "</b> создал документ <b>" + record.getAttribute("name") + "</b>", record);
					}
			break;
		case SCRIPT: GlobalData.getStatusBar().indicateMessage("Скрипт создан");
			break;
		case TAG: GlobalData.getStatusBar().indicateMessage("Тэг добавлен");
			break;
		case TASK:
		{
			sendSystemMessage(CommandType.UPDATE_TASK_STATE, ResourceType.TASK, "Пользователь <b>" + GlobalData.getCurrentUser().getAttributeAsString("nickname") + "</b> создал новую задачу <b>" + record.getAttribute("name") + "</b>",record);
			GlobalData.getStatusBar().indicateMessage("Задача добавлена");
		}
			break;
		default:
			break;}
	}
	
	public static void resourceChanged(ResourceType resType, Record record)
	{
		switch (resType)
		{
		case DOCUMENT:
		{
			sendSystemMessage(CommandType.UPDATE_DOC_LIST, ResourceType.DOCUMENT, "Пользователь <b>" + GlobalData.getCurrentUser().getAttributeAsString("nickname") + "</b> изменил свойства документа <b>" + record.getAttribute("name") + "</b>",record);
			GlobalData.getStatusBar().indicateMessage("Изменения документа сохранены");
		}
			break;
		case LANE: 	{
						CommandExecutor.exec(new Command(Command.CommandType.UPDATE_LANES));
						sendSystemMessage(Command.CommandType.UPDATE_LANES, ResourceType.LANE, "Пользователь <b>" + GlobalData.getCurrentUser().getAttribute("nickname") + "</b> отредактировал список потоков",record);
					}
			break;
		case SCRIPT: GlobalData.getStatusBar().indicateMessage("Изменения скрипта сохранены");
			break;
		case TAG: GlobalData.getStatusBar().indicateMessage("Свойства тега изменены");
			break;
		case TASK:
				{ 
					sendSystemMessage(CommandType.UPDATE_TASK_STATE, ResourceType.TASK, "Пользователь <b>" + GlobalData.getCurrentUser().getAttributeAsString("nickname") + "</b> изменил задачу <b>" + record.getAttribute("name") + "</b>", record);
					GlobalData.getStatusBar().indicateMessage("Свойства задачи сохранены");
				}
			break;
		default: 
			break;
			}
	}
	
	public static void resourceDeleted(ResourceType resType, Record record)
	{
		switch (resType)
		{
		case DOCUMENT:
				{
					GlobalData.getStatusBar().indicateMessage("Документ удален");
					sendSystemMessage(CommandType.UPDATE_DOC_LIST, ResourceType.DOCUMENT,"Пользователь <b>" + GlobalData.getCurrentUser().getAttributeAsString("nickname") + "</b> удалил документ <b>" + record.getAttribute("name") + "</b>", record);
				}
			break;
		case LANE:	{
						sendSystemMessage(CommandType.UPDATE_LANES, ResourceType.LANE, "Пользователь <b>" + GlobalData.getCurrentUser().getAttributeAsString("nickname") + "</b> удалил поток <b>" + record.getAttribute("name") + "</b>", record);
						GlobalData.getStatusBar().indicateMessage("Поток \"" +  record.getAttribute("name") + "\" удален");
					}
			break;
		case SCRIPT: GlobalData.getStatusBar().indicateMessage("Скрипт удален");
			break;
		case TAG: GlobalData.getStatusBar().indicateMessage("Тег удален");
			break;
		case TASK: 	{
						sendSystemMessage(CommandType.UPDATE_TASK_STATE, ResourceType.TASK, "Пользователь <b>" + GlobalData.getCurrentUser().getAttributeAsString("nickname") + "</b> удалил задачу <b>" + record.getAttribute("name") + "</b>", record);
						GlobalData.getStatusBar().indicateMessage("Задача \"" + record.getAttribute("name") + "\" удалена");
					}
			break;
		default:
			break;
			}
	}

	private static void sendSystemMessage(CommandType command, ResourceType resource, String message, Record record)
	{
		//assert(record.getAttributeAsInt("id") != null);
		SC.logWarn("1");

		// Если документ доступен только одному пользователю, и этот пользователь - текущий, то уведомления не посылать
		if (record.getAttributeAsInt("rgroup") != null && record.getAttributeAsInt("rgroup") == GlobalData.ACCESS_ONLY_AUTHOR && record.getAttributeAsInt("author").equals(GlobalData.getCurrentUser().getAttributeAsInt("id"))) return;

		//Выбор адресата: все, пользователь или группа, в зависимости от значения дескриптора доступа
		if (record.getAttribute("rgroup") == null)
		{
			SC.logWarn("ResourceLifeCycleManager - ВСЕМ: " + message);
//			Connector.sendSystemMessageToAll(command, resource, message, record.getAttributeAsInt("id"));
			return;
		}

		if (record.getAttributeAsInt("rgroup") == GlobalData.ACCESS_ONLY_AUTHOR)
		{
			assert (record.getAttributeAsInt("author") != null);
			SC.logWarn("ResourceLifeCycleManager - АВТОРУ: " + message);
//			Connector.sendSystemMessageToUser(command, resource, message, record.getAttributeAsInt("id"), record.getAttributeAsInt("author"));
			return;
		}
		SC.logWarn("ResourceLifeCycleManager - ГРУППЕ: " + message);
//		Connector.sendSystemMessageToGroup(command, resource, message, record.getAttributeAsInt("id"), record.getAttributeAsInt("rgroup"));
	}
}
