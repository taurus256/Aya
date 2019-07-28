package org.taurus.aya.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.ContentsType;
import com.smartgwt.client.widgets.HTMLPane;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import org.taurus.aya.client.TabManager.ResourceType;
import org.taurus.aya.shared.ScriptResult;

public class ScriptView extends ContentPane{
	
	static HTMLPane editorPane = null;
	static HTMLPane bottomPanel = null;
	ToolStrip toolstrip = null;
	ToolStripButton buttonShowHideConsole = null;
	Integer scriptId  = -1;	// id записи в таблице script (входной параметр, приходит в конструкторе)
	Integer contentId;	// id записи в таблице script_content
	Record rec;
	int tabUID;
	
	public ScriptView(Record rec, int tabUID)
	{
		this.rec = rec;
		scriptId = rec.getAttributeAsInt("id");
		this.tabUID = tabUID;
		
		this.setPadding(0);
		this.setMargin(0);
		
		this.setHeight100();
		this.setWidth100();
		this.addMember(createTootStrip());
		this.addMember(createEditor());
		this.addMember(createPanel());
	}
	
	ToolStrip createTootStrip()
	{
		ToolStrip toolstrip = new ToolStrip();
		toolstrip.setMargin(0);
		toolstrip.setPadding(0);
		toolstrip.setWidth("100%");
		toolstrip.setHeight(28);
		toolstrip.setStyleName("s3_toolstrip");
		
		ToolStripButton runScriptButton = new ToolStripButton("Запустить");
		runScriptButton.setIcon("buttons/play.png");
		runScriptButton.setIconSize(20);
		toolstrip.addButton(runScriptButton);
		runScriptButton.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				executeScript(tabUID);
			}
			
		});
		
		return toolstrip;
	}
	
	HTMLPane createEditor()
	{
		editorPane  = new HTMLPane();
		editorPane.setContentsType(ContentsType.PAGE);
		editorPane.setContentsURL("ace/editor.html?tabuid=" + String.valueOf(tabUID));
		editorPane.setWidth("100%");
		editorPane.setHeight("100%");
		editorPane.setStyleName("s3_editor");

		return editorPane;
	}
	
	VLayout createPanel()
	{
		VLayout panel = new VLayout();
		toolstrip = new ToolStrip();
		toolstrip.setHeight(28);
		toolstrip.setStyleName("s3_toolstrip");
		
		buttonShowHideConsole = new ToolStripButton("Скрыть консоль вывода");
		buttonShowHideConsole.setIcon("buttons/eye-hidden.png");
		buttonShowHideConsole.setIconSize(20);
		
		buttonShowHideConsole.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				if (bottomPanel.getHeight() > 0)
				{
					bottomPanel.setHeight(0);
					buttonShowHideConsole.setTitle("Показать консоль вывода");
					buttonShowHideConsole.setIcon("buttons/eye.png");
					buttonShowHideConsole.setIconSize(24);
				}
				else
				{
					bottomPanel.setHeight(200);
					buttonShowHideConsole.setTitle("Скрыть консоль вывода");
					buttonShowHideConsole.setIcon("buttons/eye-hidden.png");
					buttonShowHideConsole.setIconSize(24);
				}
				
			}});
		toolstrip.addButton(buttonShowHideConsole);
		
		bottomPanel = new HTMLPane();
		bottomPanel.setStyleName("s3_console");
		bottomPanel.setSize("100%", "200px");
		bottomPanel.setBorder("1px solid lightgray");
		
		panel.addMember(toolstrip);
		panel.addMember(bottomPanel);
		
		return panel;
	}
	
	public static void executeScript(int tabUID)
	{
		GlobalData.getStatusBar().indicateProcess("Выполняю скрипт...");
		String content = getEditorContent(TabManager.getFrameNumber(tabUID));
		GlobalData.getScriptService().execute(content, new AsyncCallback<ScriptResult>(){

			@Override
			public void onFailure(Throwable caught) {
				bottomPanel.setContents(caught.getMessage());
				GlobalData.getStatusBar().indicateMessage("Скрипт завершен с ошибкой!");
			}

			@Override
			public void onSuccess(ScriptResult result) {
				bottomPanel.setContents(result.getLog() + "<br/><span style='color:green; font-weight:bold; font-size: 10pt;'>OK</span>");
				
				GlobalData.getStatusBar().indicateMessage("Скрипт отработал успешно");
				CommandExecutor.load(result.getCommandList());
			}});
	}
	
//	public void loadContent()
//	{
//		Criteria criteria = new Criteria();
//		criteria.addCriteria("script_id", scriptId);
//
//		GlobalData.getDataSource_script_content().fetchData(criteria,new DSCallback(){
//
//			@Override
//			public void execute(DSResponse dsResponse, Object data,
//					DSRequest dsRequest) {
//				if (dsResponse.getData().length == 0)
//				{ 
//					SC.logWarn("ScriptView. Set text for empty script");
//					// Записи для данного документа ещё нет, задаем её id = -1
//					editor.setText("//Скрипт создан " + (new Date()).toString() + ", пользователем " + GlobalData.getCurrentUser().getAttributeAsString("firstname") + " " + GlobalData.getCurrentUser().getAttributeAsString("surname"));
//				}
//				else
//				{
//					SC.logWarn("ScriptView. Set text for script with id " + scriptId);
//					// Запись найдена, извлекаем значения полей
//					String content = dsResponse.getData()[0].getAttribute("content");
//					contentId =  dsResponse.getData()[0].getAttributeAsInt("id");
//					editor.setText(content);
//				}
//			}
//		});
//
//	}
	
	private static native String getEditorContent(int frameNumber)
	/*-{
		return window.top.frames[frameNumber].editor.getValue();
	}-*/;
	
	public Record getResource()
	{
		return rec;
	}

	public ResourceType getResourceType() 
	{
		return ResourceType.SCRIPT;
	}
	
	public void updateContent(Boolean all)
	{
		TabManager.loadScriptContent(tabUID);
	}
}
