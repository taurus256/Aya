package org.taurus.aya.client;

import com.smartgwt.client.data.*;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.OperatorId;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.events.DoubleClickEvent;
import com.smartgwt.client.widgets.events.DoubleClickHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.DataArrivedEvent;
import com.smartgwt.client.widgets.grid.events.DataArrivedHandler;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;
import org.taurus.aya.shared.Command;
import org.taurus.aya.shared.Command.CommandType;

import java.util.ArrayList;
import java.util.HashSet;

public class ChatPanel extends VLayout {
	
	TabSet tabset;
	Tab usersTab, groupsTab;
	final ListGrid usersGrid, groupsGrid;
	private int unreadCounter = 0;
	boolean usersLoaded=false, groupsLoaded=false;
	
	HashSet<Integer> dialogIdSet = new HashSet<Integer>();
	HashSet<Integer> statisticsIdSet = new HashSet<Integer>();
	
	public ChatPanel()
	{
		tabset = new TabSet();
		tabset.setHeight100();
		
		usersTab = new Tab("Пользователи");
		groupsTab = new Tab("Группы");
		
		usersGrid = new ListGrid();
		usersGrid.setDataSource(GlobalData.getDataSource_user());
		usersGrid.setSize("100%", "100%");
		usersGrid.setPadding(0);
		usersGrid.setMargin(0);
		usersGrid.setShowHeader(false);
// Now it come from main Aya module
//		usersGrid.addDataArrivedHandler(new DataArrivedHandler() {
//			@Override
//			public void onDataArrived(DataArrivedEvent event) {
//				GlobalData.setUsers(usersGrid.getDataAsRecordList().toArray());
//				usersLoaded = true;
//				if (groupsLoaded) setMessagesInitialCount();
//			}
//		});
		
		ListGridField stateUserIndicatorField = new ListGridField("state","Онлайн");
		stateUserIndicatorField.setWidth(20);
		stateUserIndicatorField.setType(ListGridFieldType.IMAGE);
		stateUserIndicatorField.setImageURLPrefix("forms/");
		stateUserIndicatorField.setImageURLSuffix(".png");
		stateUserIndicatorField.setDefaultValue("offline");
		ListGridField userNameField = new ListGridField("showedName","Имя");
		ListGridField counterUserMessagesField = new ListGridField("counter","Пользователь");
		counterUserMessagesField.setWidth(20);
		usersGrid.setFields(stateUserIndicatorField,userNameField,counterUserMessagesField);
		
		groupsGrid = new ListGrid();
		Criteria groupCriteria = new Criteria("userid",GlobalData.getCurrentUser().getAttributeAsString("id"));
		groupsGrid.setInitialCriteria(groupCriteria);
		
		groupsGrid.setDataSource(GlobalData.getDataSource_relation_user_group());

		groupsGrid.setSize("100%", "100%");
		groupsGrid.setShowHeader(false);
//		groupsGrid.setShowFilterEditor(false);
//		groupsGrid.setShowHeaderContextMenu(false);
//		groupsGrid.setShowHeaderMenuButton(false);
		ListGridField stateGroupIndicatorField = new ListGridField("state","Онлайн");
		stateGroupIndicatorField.setWidth(20);
		ListGridField counterGroupMessagesField = new ListGridField("counter","Пользователь");
		counterUserMessagesField.setWidth(20);
		ListGridField nameGroupField = new ListGridField("description","Название группы");
		groupsGrid.setFields(stateGroupIndicatorField,nameGroupField,counterGroupMessagesField);
		
		usersTab.setPane(usersGrid);
		groupsTab.setPane(groupsGrid);
		
		//setting criteria to match all users that belongs to "our" group exclude current user (me)
		AdvancedCriteria advancedCriteria = new AdvancedCriteria(OperatorId.AND,new Criterion[]{new Criterion("id", OperatorId.NOT_EQUAL,GlobalData.getCurrentUser().getAttributeAsInt("id")),GlobalData.getUserFilterCriteria()});
		usersGrid.fetchData(advancedCriteria);
		
		groupsGrid.fetchData(groupCriteria);
		groupsGrid.addDataArrivedHandler(new DataArrivedHandler(){

			@Override
			public void onDataArrived(DataArrivedEvent event) {
				groupsLoaded = true;
				if (usersLoaded) setMessagesInitialCount();
			}});
		tabset.setTabs(usersTab,groupsTab);
		
		addMember(tabset);
		
		/*usersGrid.addRecordClickHandler(new RecordClickHandler(){

			@Override
			public void onRecordClick(RecordClickEvent event) {
				openUserDialog(event.getRecord().getAttributeAsInt("id"));
			}});*/
		usersGrid.addDoubleClickHandler( new DoubleClickHandler(){

			@Override
			public void onDoubleClick(DoubleClickEvent event) {
				if (usersGrid.getSelectedRecord() != null) 
				{
					// decrement messages counter
					if (usersGrid.getSelectedRecord().getAttributeAsInt("counter") != null)
					{
						changeSectionCounter(-usersGrid.getSelectedRecord().getAttributeAsInt("counter"));
						usersGrid.getSelectedRecord().setAttribute("counter",(String)null);
						usersGrid.refreshRow(usersGrid.getRowNum(usersGrid.getSelectedRecord()));
					}
					
					// retrieve dialog data from the DB
					CommandExecutor.exec(new Command(CommandType.OPEN_USER_CHAT,"Opening user chat",usersGrid.getSelectedRecord().getAttributeAsInt("id")));
					//openUserDialog(usersGrid.getSelectedRecord().getAttributeAsInt("id"));
				}
			}});
		
		groupsGrid.addDoubleClickHandler( new DoubleClickHandler(){

			@Override
			public void onDoubleClick(DoubleClickEvent event) {
				if (groupsGrid.getSelectedRecord() != null) 
				{
					// decrement messages counter
					if (groupsGrid.getSelectedRecord().getAttributeAsInt("counter") != null)
					{
						changeSectionCounter(-groupsGrid.getSelectedRecord().getAttributeAsInt("counter"));
						groupsGrid.getSelectedRecord().setAttribute("counter",(String)null);
						groupsGrid.refreshRow(groupsGrid.getRowNum(groupsGrid.getSelectedRecord()));
					}
					
					// retrieve dialog data from the DB
					CommandExecutor.exec(new Command(CommandType.OPEN_GROUP_CHAT,"Opening group chat",groupsGrid.getSelectedRecord().getAttributeAsInt("id")));
					//openGroupDialog(groupsGrid.getSelectedRecord().getAttributeAsInt("id"));
				}
			}});
		
	}
	
	private void setMessagesInitialCount()
	{
		Criteria crit = new Criteria();
		crit.addCriteria("user_id",GlobalData.getCurrentUser().getAttribute("id"));
		GlobalData.getDataSource_dialog_statistics().fetchData(crit, new DSCallback(){

			@Override
			public void execute(DSResponse dsResponse, Object data,
                                DSRequest dsRequest) {
				
				// Phase 1. Set messages count based on existing statistics records
				int delta = 0;
				for (Record r: dsResponse.getData())
				{
					SC.logWarn("ChatPanel: setMessagesInitialCount: user: " + r.getAttributeAsInt("remote_user") + " group: " +  r.getAttributeAsInt("remote_group") + " dialog_count: " + r.getAttributeAsInt("dialog_count") + " count: " + r.getAttributeAsInt("count") );

					statisticsIdSet.add(r.getAttributeAsInt("dialog_id"));
					
					delta = r.getAttributeAsInt("dialog_count") - r.getAttributeAsInt("count");
					unreadCounter += delta;
					
					if (r.getAttributeAsInt("remote_user") != null)
					{
						incrementUserMessage(r.getAttributeAsInt("remote_user"), delta);
					}
					else
					{
						incrementGroupMessage(r.getAttributeAsInt("remote_group"), delta);
					}
				}
				
				// Phase 2. If there are users or groups, that have no statistics records, set messages count based on dialog records
				
				calculateMessagesCountPhase2(dsResponse.getData());
				
				if (unreadCounter > 0) GlobalData.getNavigationArea().getSection("messages").setTitle("Сообщения 	<b>+" + unreadCounter + "</b>");
				
			}});
	}
	
	private void calculateMessagesCountPhase2(Record[] statistics)
	{
		// формируем массив групп
		ArrayList<Integer> arr = new ArrayList<Integer>();
		for (Record r: GlobalData.getCurrentUserGroups())
			arr.add(r.getAttributeAsInt("id"));
		Integer[] arr1 = {};
		Integer[] arr2 = (Integer[])(arr.toArray(arr1));
		AdvancedCriteria crit = new AdvancedCriteria(OperatorId.OR,new Criterion[]{
				new Criterion("first_user", OperatorId.EQUALS, GlobalData.getCurrentUser().getAttributeAsInt("id")),
				new Criterion("second_user", OperatorId.EQUALS, GlobalData.getCurrentUser().getAttributeAsInt("id")),
				new Criterion("group_id", OperatorId.IN_SET, arr2)
				});
		
		GlobalData.getDataSource_dialogs().fetchData(crit,new DSCallback(){

			@Override
			public void execute(DSResponse dsResponse, Object data,
                                DSRequest dsRequest) {
				
				for (Record r: dsResponse.getData())
					dialogIdSet.add(r.getAttributeAsInt("id"));
				
				// getting set of dialogs, that have no statistics data				
				dialogIdSet.removeAll(statisticsIdSet);
				
				// setting indicators for this dialogs
				for (int i:dialogIdSet)
					for (Record r: dsResponse.getData())
						if (r.getAttributeAsInt("id") == i)
						{
							SC.logWarn("ChatPanel: dialog with id" + i + " has no statistics data, but " + r.getAttributeAsInt("count") + " unread records" );
							
							if (r.getAttributeAsInt("count") == 0) 
								break;
							else
							{
								unreadCounter += r.getAttributeAsInt("count");
							
								if (r.getAttributeAsInt("group_id") != null)
									incrementGroupMessage(r.getAttributeAsInt("group_id"), r.getAttributeAsInt("count"));
								else
									if (r.getAttributeAsInt("first_user") == GlobalData.getCurrentUser().getAttributeAsInt("id"))
										incrementUserMessage(r.getAttributeAsInt("second_user"), r.getAttributeAsInt("count"));
									else
										incrementUserMessage(r.getAttributeAsInt("first_user"), r.getAttributeAsInt("count"));
							}
						}
				
				if (unreadCounter > 0) GlobalData.getNavigationArea().getSection("messages").setTitle("Сообщения 	<b>+" + unreadCounter + "</b>");
			}});
	}
	
	public void userConnected(Integer id)
	{
		for (Record r:usersGrid.getDataAsRecordList().toArray())
		{
			if (r.getAttributeAsInt("id").equals(id))
				r.setAttribute("state", "online");
			//reshRow(usersGrid.getRowNum((ListGridRecord)r));
			//usersGrid.updateData(r);
		}
		usersGrid.redraw();
		//usersGrid.setData(usersGrid.getDataAsRecordList());
	}
	

	public void userDisconnected(Integer id)
	{
		for (Record r:usersGrid.getDataAsRecordList().toArray())
		{
			if (r.getAttributeAsInt("id").equals(id))
				r.setAttribute("state", "offline");
			//usersGrid.updateData(r);
		}
		usersGrid.redraw();
	}
	
	private void changeSectionCounter(int delta)
	{
		unreadCounter += delta;
		if (unreadCounter != 0)
			GlobalData.getNavigationArea().getSection("messages").setTitle("Сообщения 	<b>+" + unreadCounter + "</b>");
		else
			GlobalData.getNavigationArea().getSection("messages").setTitle("Сообщения");
	}
	
	public void addNewMessage(Record r)
	{
		SC.logWarn("ChatPanel : calling addMessage");
		changeSectionCounter(1);
		SC.logWarn("Message type is " + r.getAttributeAsString("type"));
		if (r.getAttributeAsInt("type") == 1)
			incrementUserMessage(r.getAttributeAsInt("sender_id"), 1);
		else
			incrementGroupMessage(r.getAttributeAsInt("group_id"), 1);
	}
	
	public void incrementUserMessage(int userId, int increment)
	{
		SC.logWarn("ChatPanel :  increment counter for user " + userId + " to 1");
		
		for (Record r:usersGrid.getRecords())
			if (r.getAttributeAsInt("id").equals(userId))
			{
				if (increment == 0) return;
				
				if (r.getAttributeAsInt("counter") != null)
				{
					SC.logWarn("ChatPanel :  Old counter value is " + r.getAttributeAsString("counter"));
					r.setAttribute("counter", r.getAttributeAsInt("counter") + increment);
					SC.logWarn("ChatPanel :  New counter value is " + r.getAttributeAsString("counter"));
				}
				else
				{
					r.setAttribute("counter",increment);
					SC.logWarn("ChatPanel :  Counter value is now " + r.getAttributeAsString("counter"));
				}
				usersGrid.refreshRow(usersGrid.getRowNum((ListGridRecord) r));
			}
	}
	
	public void incrementGroupMessage(int groupId, int increment)
	{
		SC.logWarn("ChatPanel :  increment counter for group " + groupId + " to 1");
		
		if (increment == 0) return;
		
		for (Record r:groupsGrid.getRecords())
			if (r.getAttributeAsInt("id").equals(groupId))
			{
				if (r.getAttributeAsInt("counter") != null)
					r.setAttribute("counter", r.getAttributeAsInt("counter") + increment);
				else
					r.setAttribute("counter",increment);
				groupsGrid.refreshRow(groupsGrid.getRowNum((ListGridRecord) r));
			}
	}
	/* НА УДАЛЕНИЕ
	private void  openUserDialog(final Integer remoteUserId)
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

	private void  openGroupDialog(final Integer groupId)
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
						SC.logWarn("There are many than one dialog in the dialogs table!");
				
			}});
	}
	
	private void openUserChatView(Record r, Integer remoteUserId)
	{
		for (Record k: GlobalData.getUsers())
			if (k.getAttributeAsInt("id").equals(remoteUserId))
			{
				r.setAttribute("nickname",k.getAttributeAsString("showedName")); break;
			}
		TabManager.openTab(ResourceType.CHAT, r);
	}
	
	private void openGroupChatView(Record r, Integer groupId)
	{
		for (Record k: GlobalData.getCurrentUserGroups())
			if (k.getAttributeAsInt("id").equals(groupId))
			{
				r.setAttribute("nickname",k.getAttributeAsString("description")); break;
			}
		TabManager.openTab(ResourceType.CHAT, r);
	}
	*/
}