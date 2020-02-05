package org.taurus.aya.client;

import com.smartgwt.client.data.*;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.*;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.tile.TileGrid;
import com.smartgwt.client.widgets.tile.events.DataArrivedEvent;
import com.smartgwt.client.widgets.tile.events.DataArrivedHandler;
import com.smartgwt.client.widgets.tile.events.SelectionChangedEvent;
import com.smartgwt.client.widgets.tile.events.SelectionChangedHandler;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import org.taurus.aya.client.TabManager.ResourceType;
import org.taurus.aya.client.generic.GenericPanel;
import org.taurus.aya.client.widgets.CustomTagTile;
import org.taurus.aya.client.widgets.LinkWidget;

public class TagView  extends ContentPane {

	Record currentRecord;
	TagView tv;
	Canvas selectedTile = null;
	Record selectedRecord = null;
	final TileGrid tileGrid;
	
	public TagView(Record record){
		tv = this;
		currentRecord = record;
		

        tileGrid = new TileGrid();
        tileGrid.setTileWidth(110);  
        tileGrid.setTileHeight(150);  
        tileGrid.setHeight100();  
        tileGrid.setWidth100();
        tileGrid.setBackgroundColor("white");
        tileGrid.setTileMargin(0);
        
        tileGrid.setShowAllRecords(false);
        tileGrid.setAutoFetchData(true);
        final Criteria criteria = new Criteria();
        criteria.addCriteria("tag_id", currentRecord.getAttributeAsInt("id"));
        tileGrid.setInitialCriteria(criteria);

        //настройка полей для TileGrid
//        DetailViewerField linksIdField = new DetailViewerField("id");
//        linksIdField.setAttribute("primaryKey",true);
//        linksIdField.setAttribute("hidden",false);
//        DetailViewerField nameField = new DetailViewerField("name");
//        nameField.setHeight(76);
//        DetailViewerField imageField = new DetailViewerField("image");
//        imageField.setHeight(64);
//  
//        tileGrid.setFields(imageField, nameField);

        tileGrid.addSelectionChangedHandler(new SelectionChangedHandler() {
			
			@Override
			public void onSelectionChanged(SelectionChangedEvent event) {
				selectedRecord = event.getRecord();
				Canvas tile = (Canvas) tileGrid.getTile(selectedRecord);
				tile.setBorder("2px solid #157efb");
				if (selectedTile != null) selectedTile.setBorder("none");
				selectedTile = tile;
			}
		});
        
        tileGrid.addDoubleClickHandler(new DoubleClickHandler(){

			@Override
			public void onDoubleClick(DoubleClickEvent event) {
				if (selectedRecord != null)
				{
					AdvancedCriteria crit = new AdvancedCriteria();
					crit.addCriteria(GlobalData.createSearchCriteria());
					
					ResourceType resourceType = ResourceType.values()[selectedRecord.getAttributeAsInt("resource_type")];
					switch (resourceType)
					{
					case TASK: {
						crit.addCriteria("id", selectedRecord.getAttribute("task_id"));
						GlobalData.getStatusBar().indicateProcess("Открываю график задач...");
						GlobalData.getDataSource_events().fetchData(crit, new DSCallback(){

							@Override
							public void execute(DSResponse dsResponse, Object data, DSRequest dsRequest) {
								if (dsResponse.getDataAsRecordList().getLength() >0)
									TabManager.openTab(ResourceType.TASK, dsResponse.getDataAsRecordList().get(0));
								else
									SC.logWarn("TagView: Cannot open resource, possible incorrect criteria");
							}});
						
						break;
						}
					case DOCUMENT:{
						crit.addCriteria("id", selectedRecord.getAttribute("doc_id"));
						GlobalData.getDataSource_docs().fetchData(crit, new DSCallback(){

							@Override
							public void execute(DSResponse dsResponse, Object data, DSRequest dsRequest) {
								if (dsResponse.getDataAsRecordList().getLength() >0)
								{
									final Record selectedResource = dsResponse.getDataAsRecordList().get(0);
									
									if (selectedResource.getAttributeAsInt("type") == 1)
									{
										GlobalData.getStatusBar().indicateProcess("Открываю документ \"" + selectedResource.getAttribute("name") + "\"...");
										TabManager.openTab(TabManager.ResourceType.DOCUMENT, selectedResource);
									}
									if (selectedResource.getAttributeAsInt("type") == 2)
									{
										//user wants to open file...
										SC.confirm("Вы хотите скачать файл \"" + selectedResource.getAttributeAsInt("name") + "\"?", new BooleanCallback(){

											@Override
											public void execute(Boolean value) 
											{
												if (value)
												{
													Criteria crit = new Criteria();
													crit.addCriteria("document_id", selectedResource.getAttributeAsInt("id"));
													GlobalData.getDataSource_content().fetchData(crit, new DSCallback()
													{

														@Override
														public void execute(DSResponse dsResponse, Object data, DSRequest dsRequest)
														{
															if (dsResponse.getData().length == 1)
															{
																GlobalData.getStatusBar().indicateMessage("Начато скачивание файла \"" + selectedRecord.getAttribute("name") + "\"...");
																GlobalData.getDataSource_content().downloadFile(dsResponse.getData()[0]);
															}
															else
																SC.warn("При попытке скачать файл обнаружено более одной записи. Нарушение ссылочной целостности БД!");
														}
													});
												}
												
											}});
									}
								}
								else
									SC.logWarn("TafView: Cannot open resource, possible incorrect criteria");
							}});
						break;
					}
					case SCRIPT:{
						crit.addCriteria("id", selectedRecord.getAttribute("script_id"));
						GlobalData.getStatusBar().indicateProcess("Открываю скрипт \"" + selectedRecord.getAttribute("name") + "\"...");
						GlobalData.getDataSource_scripts().fetchData(crit, new DSCallback(){

							@Override
							public void execute(DSResponse dsResponse, Object data, DSRequest dsRequest) {
								if (dsResponse.getDataAsRecordList().getLength() >0)
									TabManager.openTab(ResourceType.SCRIPT, dsResponse.getDataAsRecordList().get(0));
								else
									SC.logWarn("TafView: Cannot open resource, possible incorrect criteria");
							}});
						break;
					}
					default : SC.warn("Это что-то экзотическое, его нельзя открыть!");
					}
				}
				
			}
			});
        tileGrid.setDataSource(GlobalData.getDataSource_links());
        tileGrid.setTileConstructor(CustomTagTile.class.getName());

        // Test panel layout
        HLayout hLayout = new HLayout();
        
        LinkWidget k= new LinkWidget(currentRecord);
  
        ToolStrip toolStrip = new ToolStrip();
        toolStrip.setWidth100();  
        toolStrip.setHeight(20);
  
        ToolStripButton iconButton = new ToolStripButton();
        iconButton.setIcon("panel_links2.svg");
        iconButton.setWidth(110);
        iconButton.setTitle("Ссылки");
        toolStrip.addButton(iconButton);
        
        ToolStripButton iconButton2 = new ToolStripButton();
        iconButton2.setIcon("panel_comments.svg");
        iconButton2.setWidth(110);
        iconButton2.setTitle("Комментарии");
        toolStrip.addButton(iconButton2);  
        
        this.addMember(k);
        this.addMember(toolStrip);
        k.setHeight100();  
        //this.addMember(tileGrid);
        
        //обработчик события Drop
        tileGrid.setCanAcceptDroppedRecords(true); 
        tileGrid.setCanAcceptDrop(true);
        tileGrid.addDropHandler(new DropHandler() {
            public void onDrop(DropEvent event) {
            	
            	GenericPanel sourcePanel = GlobalData.getCurrentPanel();
            	ResourceType resourceType = sourcePanel.getResourceType();
            	Record r = sourcePanel.getTreeSelectedRecord();
            	r.setAttribute("tag_id",currentRecord.getAttributeAsInt("id"));
            	r.setAttribute("resource_type", resourceType.ordinal());

            	switch (resourceType)
            	{
            	case TASK: 
            		{
            			r.setAttribute("task_id", r.getAttribute("id"));
            			r.setAttribute("image", "task.png");
            			break;
            		}
				case DOCUMENT: 
					{
						r.setAttribute("doc_id", r.getAttribute("id"));
						r.setAttribute("image", "document.png");
						break;
					}
					
				case SCRIPT: 
					{
						r.setAttribute("script_id", r.getAttribute("id"));
						r.setAttribute("image", "script.png");
						break;	
					}
				case TAG:
					break;
				default: SC.warn("Этот объект не может быть ассоциирован с тегом");
					break;
            	}
            	
//            	tileGrid.clear();
//            	tileGrid.fetchData(criteria, new DSCallback(){
//
//					@Override
//					public void execute(DSResponse dsResponse, Object data,
//							DSRequest dsRequest) {
//						SC.say("arrived fetch data!");
//						tileGrid.redraw();
//						tv.reflow();
//						//tileGrid.resizeTo("100%", "100%");						
//					}});
            	event.cancel();
            }
        });
        
        tileGrid.addKeyPressHandler(new KeyPressHandler(){

			@Override
			public void onKeyPress(KeyPressEvent event) {
				if (event.getKeyName().equals("Delete"))
					tileGrid.removeData(tileGrid.getSelectedRecord());;
			}});
//        
//        tileGrid.addFetchDataHandler(new FetchDataHandler() {
//			
//			@Override
//			public void onFilterData(FetchDataEvent event) {
//				SC.say("fetch!");
//				tileGrid.redraw();
//				tv.reflow();	
//			}
//		});
			
        tileGrid.addDataArrivedHandler(new DataArrivedHandler() {
			
			@Override
			public void onDataArrived(DataArrivedEvent event) {
				GlobalData.getStatusBar().stopIndicateProcess(); // loading now complete
			}
		});

        tileGrid.fetchData(criteria,new DSCallback(){

			@Override
			public void execute(DSResponse dsResponse, Object data,
                                DSRequest dsRequest) {
				SC.say("data arived: " + dsResponse.getData().length);
				if (dsResponse.getData().length>0)
					GlobalData.getStatusBar().indicateMessage("Отображается записей: " + String.valueOf(dsResponse.getData().length));
				else
					GlobalData.getStatusBar().indicateMessage("Нет записей" + String.valueOf(dsResponse.getData().length));
				
				for (String s : dsResponse.getData()[0].getAttributes())
					SC.logWarn(s);
			}});
	}
	
	public Record getResource() {
		
		return currentRecord;
	}

	public ResourceType getResourceType() {
		return TabManager.ResourceType.TAG;
	}

	public void updateContent(Boolean all)
	{
		
	}
	
}
