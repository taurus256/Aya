package org.taurus.aya.client;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.smartgwt.client.data.*;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.OperatorId;
import com.smartgwt.client.types.SortDirection;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.events.DropEvent;
import com.smartgwt.client.widgets.events.DropHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.TextAreaItem;
import com.smartgwt.client.widgets.form.fields.events.KeyPressEvent;
import com.smartgwt.client.widgets.form.fields.events.KeyPressHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import org.taurus.aya.client.TabManager.ResourceType;
import org.taurus.aya.client.widgets.ChatGrid;

import java.util.Date;

public class ChatView extends ContentPane{

	Record dialog, statistics, thisUser, remoteUser = null;
	ListGrid messagesGrid;
	TextAreaItem textArea;
	Integer remoteUserId = null;
	int localCounter = 0;		// Contains message count, that is calculated locally, based on amount of incoming messages 
	
	public ChatView(Record r)
	{
		
		dialog = r;
		thisUser = GlobalData.getCurrentUser();
		localCounter = dialog.getAttributeAsInt("count");
		
		if (dialog.getAttributeAsInt("group_id") == null)
		{
			// Configure this view to show user messages
			if (r.getAttributeAsInt("first_user").equals(thisUser.getAttributeAsInt("id")))
				remoteUserId = r.getAttributeAsInt("second_user");
			else
				remoteUserId = r.getAttributeAsInt("first_user");
			
			for (Record R: GlobalData.getUsers())
				if (R.getAttributeAsInt("id").equals(remoteUserId))
					remoteUser=R;
			SC.logWarn("ChatView constructor configured for user chatting");
		} else	
		{
			// Configure this view to show group messages
			SC.logWarn("ChatView constructor configured for group chatting");
		}
		
		setBackgroundColor("white");
		setCanDrop(true);
		setCanAcceptDrop(true);
		addDropHandler(new DropHandler(){

			@Override
			public void onDrop(DropEvent event) {
				SC.logWarn("ChatView: DROP");
				event.cancel();
			}});
		
		//here we need to find or create a dialog_statistics record
		AdvancedCriteria statisticsCriteria = new AdvancedCriteria(OperatorId.AND,
				new Criterion[]{
					new Criterion("dialog_id", OperatorId.EQUALS,dialog.getAttributeAsInt("id")),
					new Criterion("user_id", OperatorId.EQUALS,GlobalData.getCurrentUser().getAttribute("id"))
					});
		GlobalData.getDataSource_dialog_statistics().fetchData(statisticsCriteria, new DSCallback(){

			@Override
			public void execute(DSResponse dsResponse, Object data,
                                DSRequest dsRequest) {
				if (dsResponse.getData().length > 0)
				{
					statistics = dsResponse.getData()[0];
					// Construct user interface
					addMember(createLayout());
					fetchPreviousData();
				}
				else
				{
					statistics = new Record();
					statistics.setAttribute("dialog_id", dialog.getAttribute("id"));
					statistics.setAttribute("user_id", GlobalData.getCurrentUser().getAttribute("id"));
					if (remoteUserId != null)
						statistics.setAttribute("remote_user", remoteUserId);
					else
						statistics.setAttribute("remote_group", dialog.getAttributeAsInt("group_id"));
					
					GlobalData.getDataSource_dialog_statistics().addData(statistics, new DSCallback(){

						@Override
						public void execute(DSResponse dsResponse, Object data,
                                            DSRequest dsRequest) {
							
							statistics = dsResponse.getData()[0];
							
							// Construct user interface							
							addMember(createLayout());
							fetchPreviousData();
						}
						
					});
				}
			}});
	}
	
	private HLayout createLayout()
	{
		HLayout hLayout = new HLayout();
		hLayout.setWidth100();
		hLayout.setHeight100();
		hLayout.setAlign(Alignment.CENTER);
			
		VLayout vLayout = new VLayout();
		vLayout.setWidth(500);
		vLayout.setHeight100();
		vLayout.setAlign(Alignment.RIGHT);
		
		messagesGrid = (remoteUser != null)? new ChatGrid(thisUser, remoteUser) : new ChatGrid(thisUser);
		
		Criteria crit = new Criteria();
		crit.addCriteria("dialog_id", dialog.getAttributeAsInt("id"));
		messagesGrid.setInitialCriteria(crit);
		messagesGrid.setDataSource(GlobalData.getDataSource_messages());
		messagesGrid.setDataPageSize(10);
		messagesGrid.setShowAllRecords(false);
		messagesGrid.setWidth100();
		messagesGrid.setHeight100();
		messagesGrid.setShowHeader(false);
		messagesGrid.setEmptyMessage("Нет сообщенией в этом диалоге");
//		messagesGrid.addDataArrivedHandler(new DataArrivedHandler(){
//
//			@Override
//			public void onDataArrived(DataArrivedEvent event) {
//			}});
		//messagesGrid.setAutoFetchData(true);

		
		DynamicForm df = new DynamicForm();
		textArea = new TextAreaItem();
		textArea.setTitle("Сообщение");
		textArea.setShowTitle(false);
		textArea.setWidth(450);
		textArea.setHeight(100);
		textArea.addKeyPressHandler(new KeyPressHandler(){

			@Override
			public void onKeyPress(KeyPressEvent event) {
				if (event.getKeyName().equals("Enter") && event.isCtrlKeyDown())
					sendMessage();
			}});
		df.setFields(textArea);
		df.setWidth100();
		df.setHeight(100);
		
		
		IButton sendButton = new IButton("");
		sendButton.setTooltip("CTRL+ENTER");
		sendButton.setIcon("buttons/send.svg");
		sendButton.setAlign(Alignment.CENTER);
		sendButton.setWidth(50);
		sendButton.setHeight(100);
		sendButton.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				sendMessage();
			}});
		
		vLayout.addMember(messagesGrid);
		//vLayout.addMember(df);
		HLayout hLayoutButton = new HLayout();
		hLayoutButton.setWidth100();
		hLayoutButton.setAlign(Alignment.RIGHT);
		hLayoutButton.addMember(df);
		hLayoutButton.addMember(sendButton);
		
		vLayout.addMember(hLayoutButton);
		
		hLayout.addMember(vLayout);
		
		return hLayout;
	}
	
	private void sendMessage()
	{
		final Record message = new Record();
		message.setAttribute("dialog_id",dialog.getAttributeAsInt("id"));
		message.setAttribute("sender_id",GlobalData.getCurrentUser().getAttributeAsInt("id"));
		message.setAttribute("sender", GlobalData.getCurrentUser().getAttributeAsString("showedName"));
		message.setAttribute("content",textArea.getValueAsString());
		message.setAttribute("stamp",new Date());
		textArea.clearValue();
		
		
		GlobalData.getDataSource_messages().addData(message, new DSCallback(){

			@Override
			public void execute(DSResponse dsResponse, Object data,
                                DSRequest dsRequest) {
			
//				if (remoteUserId != null)
//					Connector.sendChatMessageToUser(message,remoteUserId);
//				else
//					Connector.sendChatMessageToGroup(message,dialog.getAttributeAsInt("group_id"));
				
				addMessage(message);
				
				localCounter++;
			}});
	}
	
	private void fetchPreviousData()
	{
		localCounter = dialog.getAttributeAsInt("count");
		
		Integer unread = dialog.getAttributeAsInt("count") - statistics.getAttributeAsInt("count");
		statistics.setAttribute("count", localCounter);					
		GlobalData.getDataSource_dialog_statistics().updateData(statistics);
		
		Criteria crit = new Criteria();
		crit.addCriteria("dialog_id", dialog.getAttributeAsInt("id"));
		DSRequest dsRequest = new DSRequest();
		SortSpecifier sort = new SortSpecifier("stamp", SortDirection.DESCENDING);
		dsRequest.setSortBy(new SortSpecifier[]{sort});
		
		dsRequest.setStartRow(0);
		if (unread != null && unread > 0) 
			dsRequest.setEndRow(unread);
		else
			dsRequest.setEndRow(10);
		
		GlobalData.getDataSource_messages().fetchData(crit, new DSCallback(){

			@Override
			public void execute(DSResponse dsResponse, Object data, DSRequest dsRequest) {
				SC.logWarn("ChatView: i received " +  dsResponse.getDataAsRecordList().getLength() + " unread messages" );
					
					// adding messages to the table view
					for (int i=0; i< dsResponse.getDataAsRecordList().getLength(); i++)
						messagesGrid.addData(dsResponse.getDataAsRecordList().get(dsResponse.getDataAsRecordList().getLength()-i-1));
			}},dsRequest);
	}
	
	// Добавление сообщения в окно чата
	public void addMessage(Record message)
	{
		
		messagesGrid.addData(message);

		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            public void execute() {
            	int length = messagesGrid.getRecords().length;
        		SC.logWarn("ChatView: DataArrivedHandler [r]: current grid length = " + length);
        		//messagesGrid.scrollToCell(length+1, 0);
        		messagesGrid.scrollToRow(length+1);
        		messagesGrid.reflowNow();
            }
        });
	}
	
	// Добавление сообщения в окно чата и пометка его в БДкак прочитанное. Вызывается TabManager-ом.
	public void addExternalMessage(Record message)
	{
		addMessage(message);
		
		// update message counter in the statistics DS
		
		localCounter++;
		statistics.setAttribute("count", localCounter);
		GlobalData.getDataSource_dialog_statistics().updateData(statistics);
	}

	public Record getResource() {
		return dialog;
	}

	public ResourceType getResourceType() {
		return ResourceType.CHAT;
	}

	public void updateContent(Boolean all)
	{
		// для чата пока этот метод не имеет смысла
	}

}
