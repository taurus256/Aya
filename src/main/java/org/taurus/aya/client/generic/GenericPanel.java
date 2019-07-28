package org.taurus.aya.client.generic;

import com.google.gwt.event.shared.HandlerRegistration;
import com.smartgwt.client.data.*;
import com.smartgwt.client.types.DragDataAction;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.util.ValueCallback;
import com.smartgwt.client.widgets.events.DragStartEvent;
import com.smartgwt.client.widgets.events.DragStartHandler;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.events.SelectionChangedHandler;
import com.smartgwt.client.widgets.grid.events.SelectionEvent;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.MenuItemSeparator;
import com.smartgwt.client.widgets.menu.events.ClickHandler;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.events.DataArrivedEvent;
import com.smartgwt.client.widgets.tree.events.DataArrivedHandler;
import org.taurus.aya.client.*;
import org.taurus.aya.client.TabManager.ResourceType;
import org.taurus.aya.shared.Command;
import org.taurus.aya.shared.Command.CommandType;

public class GenericPanel extends VLayout implements SidePanel{

	protected ListGrid treeGrid;
	protected Record selected = null;
	protected MenuItem menuCreateResource, menuCreateFolder, menuRenameResource, menuDeleteResource, menuCreateLink, menuProperties;
	protected HandlerRegistration hrCreateResource, hrCreateFolder, hrRenameResource, hrDeleteResource, hrCreateLink, hrProperties;
	protected Menu menu;
	
	protected ResourceType resourceType = ResourceType.GRAPH;
	protected String messageNew = "Новый ресурс", objectNameWho = "ресурса";
	protected DataSource dataSource = null;
	protected String iconFile =""; // icon name (with file type suffix)
	protected AdvancedCriteria baseCriteria, searchCriteria;

	public GenericPanel(DataSource ds, String iconFile, ResourceType resType, String messageNew, String objectName)
	{
		SC.logWarn("GenericPanel test resType" + resType.toString());

		this.dataSource = ds;
		this.resourceType = resType;
		this.messageNew = messageNew;
		this.objectNameWho = objectName;
		this.iconFile = iconFile;
		baseCriteria = GlobalData.createSearchCriteria();
		searchCriteria = baseCriteria;
		
		treeGrid = new ListGrid();
		treeGrid.setDataSource(ds);
//		treeGrid.setFolderIcon("tree/folder.png");
//		treeGrid.setNodeIcon("tree/" + iconFile);
//		treeGrid.setIconSize(20);

		treeGrid.setSelectionType(SelectionStyle.SINGLE);
		treeGrid.setShowHeader(false);
		treeGrid.setCanEdit(false);
//		treeGrid.setCanReparentNodes(true);
		treeGrid.setCanRemoveRecords(false);
		treeGrid.setCanAcceptDroppedRecords(false);
		treeGrid.setDragDataAction(DragDataAction.MOVE);
		treeGrid.setCanDragRecordsOut(true);//!
		
		treeGrid.setInitialCriteria(baseCriteria);
		treeGrid.setCriteria(baseCriteria);
		treeGrid.setAutoSaveEdits(false);
		treeGrid.setAutoFetchData(true);
		
//		treeGrid.addDataArrivedHandler(new DataArrivedHandler(){
//
//			@Override
//			public void onDataArrived(DataArrivedEvent event) {
//				treeGrid.getData().openAll();
//			}});

		treeGrid.addSelectionChangedHandler(new SelectionChangedHandler() {
			
			@Override
			public void onSelectionChanged(SelectionEvent event) {
				if (event.getState()) //если выделение установлено (не снято) 
				{
					selected = treeGrid.getSelectedRecord();
					
					if (GlobalData.canWrite(selected))
					{
						menuRenameResource.setEnabled(true);
						menuDeleteResource.setEnabled(true);
					}
					else
					{
						menuRenameResource.setEnabled(false);
						menuDeleteResource.setEnabled(false);
					}
					menuCreateLink.setEnabled(true);
					menuProperties.setEnabled(true);
					menu.redraw();
				}
			}
		});
		
		treeGrid.addDragStartHandler(new DragStartHandler(){

			@Override
			public void onDragStart(DragStartEvent event) {
				 GlobalData.setCurrentPanel(getMe());
			}});
		
		treeGrid.setContextMenu(createContextMenu());

		this.addMember(treeGrid);
	}
	
	
	private Menu createContextMenu()
	{
		menu = new Menu();
		
		menuCreateResource = new MenuItem(messageNew);
		menuCreateResource.setIcon("menu/" + iconFile);
		hrCreateResource = menuCreateResource.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler(){

			@Override
			public void onClick(MenuItemClickEvent event)
			{
				Record newRecord = new Record();

				//Setting parent and author for newly created record
				if (selected == null) //ничего не выбрано
					newRecord.setAttribute("parent", 0);
				else
					if (selected.getAttributeAsBoolean("isFolder"))
						newRecord.setAttribute("parent", selected.getAttribute("id")); // выбрана папка
					else
						newRecord.setAttribute("parent", selected.getAttribute("parent")); // выбран элемент в папке

				newRecord.setAttribute("author", GlobalData.getCurrentUser().getAttribute("id"));
				newRecord.setAttribute("rgroup", GlobalData.ACCESS_ALL);
				newRecord.setAttribute("wgroup", GlobalData.ACCESS_ALL);

				GenericPropertiesDialog gpd = new GenericPropertiesDialog(newRecord, iconFile, resourceType, dataSource, objectNameWho);
				gpd.constructInterface();
				gpd.show();
			};
		});

		menuCreateFolder = new MenuItem("Создать папку");
		menuCreateFolder.setIcon("menu/folder.png");
		hrCreateFolder = menuCreateFolder.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler(){

			@Override
			public void onClick(MenuItemClickEvent event) {
				SC.askforValue("Создание папки", "Введите имя", new ValueCallback(){
					public void execute(String value)
					{
						if (value == null) return; // Пользователь нажал Cancel

						//Создание записи в таблице
						Record newRecord = new Record();
						newRecord.setAttribute("name", value);
						newRecord.setAttribute("isFolder", true);

						//Проверяем, какой элемент выбран - узел или папка
						SC.logWarn("ResourcePanel - 1");

						if (selected == null) //ничего не выбрано
							newRecord.setAttribute("parent", 0);
						else
							newRecord.setAttribute("parent", selected.getAttribute("parent")); // выбран элемент в папке

						newRecord.setAttribute("author", GlobalData.getCurrentUser().getAttribute("id"));

						SC.logWarn("ResourcePanel - 2");

						//Сохранить новую запись
						dataSource.addData(newRecord,new DSCallback() {

							@Override
							public void execute(DSResponse dsResponse, Object data, DSRequest dsRequest) {
								//Обновить данные в дереве
								treeGrid.fetchData(GlobalData.createSearchCriteria());
							}
						});

					}
				});

		}});


		menuRenameResource = new MenuItem("Переименовать");
		menuRenameResource.setIcon("menu/rename.png");
		menuRenameResource.setEnabled(false); //при создании деактивирован
		hrRenameResource = menuRenameResource.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler(){

			@Override
			public void onClick(MenuItemClickEvent event)
			{
				//Устанавливаем текст в зависимости от типа ресурса
				String caption = (selected.getAttribute("isFolder") != null && selected.getAttributeAsBoolean("isFolder")) ? "Переименование папки:" : "Переименование " + objectNameWho;

				// Вызываем диалог редактирования
				SC.askforValue(caption, "Переименовать '" + selected.getAttribute("name") + "' в", new ValueCallback(){
					public void execute(String value)
					{
						if (value == null) return; // Пользователь нажал Cancel

						selected.setAttribute("name", value);
						dataSource.updateData(selected);

						ResourceLifeCycleManager.resourceChanged(resourceType, selected);
					}
				});
			};
		});

		menuDeleteResource = new MenuItem("Удалить");
		menuDeleteResource.setIcon("menu/trash.png");
		menuDeleteResource.setEnabled(false); //при создании деактивирован
		hrDeleteResource = menuDeleteResource.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler(){

			@Override
			public void onClick(MenuItemClickEvent event) {

				if (selected == null) return;
				
				String question;
				if (selected.getAttributeAsBoolean("isFolder"))
					question = "Вы подтверждаете удаление папки \"" + selected.getAttributeAsString("name") + "\"?<br> Если в ней содержатся объекты, они тоже будут удалены";
				else
					question = "Вы подтверждаете удаление " + objectNameWho + " \"" + selected.getAttributeAsString("name") + "\"?";

				SC.ask("Вопрос", question, new BooleanCallback(){

					@Override
					public void execute(Boolean value) {
						Record selected = treeGrid.getSelectedRecord();
						if (selected == null) return;
						if (value)
						{
							TabManager.removeTab(TabManager.getTab(resourceType, selected.getAttributeAsInt("id")),resourceType, selected);
							dataSource.removeData(selected, new DSCallback(){

								@Override
								public void execute(DSResponse dsResponse,
                                                    Object data, DSRequest dsRequest) {
									ResourceLifeCycleManager.resourceDeleted(resourceType, dsResponse.getData()[0]);
								}});

							selected = null;
						}
					}});
			};
		});

		MenuItemSeparator separator = new MenuItemSeparator();
		
		menuCreateLink = new MenuItem("Получить ссылку");
		menuCreateLink.setIcon("menu/link.png");
		menuCreateLink.setEnabled(false);
		
		menuCreateLink.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(MenuItemClickEvent event) {
				CommandExecutor.exec(new Command(CommandType.CREATE_LINK,resourceType,selected.getAttributeAsString("name"), selected.getAttributeAsInt("id")));
			}});
		
		menuProperties = new MenuItem("Свойства");
		menuProperties.setEnabled(false);
		hrProperties = menuProperties.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(MenuItemClickEvent event) {
				GenericPropertiesDialog gpd = new GenericPropertiesDialog(treeGrid.getSelectedRecord(), iconFile, resourceType, dataSource, objectNameWho);
				gpd.constructInterface();
				gpd.show();
			}});
		 
		menu.setItems(menuCreateResource,menuCreateFolder,separator,menuRenameResource,menuDeleteResource,separator,menuCreateLink,menuProperties);
		SC.logWarn("GenericPanel: menuItemLength=" +menu.getItems().length);
		
		menu.setHeight(menu.getItems().length*ApplicationMenu.ITEM_MENU_HEIGHT + 3);
		
		return menu;
	}

	public ResourceType getResourceType()
	{
		return resourceType;
	}
	
	public Record getTreeSelectedRecord()
	{
		return treeGrid.getSelectedRecord();
	}
	
	protected GenericPanel getMe()
	{
		return this;
	}
	
	public ListGrid getTreeGrid()
	{
		return treeGrid;
	}
	
	public void deleteCreateFolderItem()
	{
		menu.removeItem(menuCreateFolder);
		menu.setHeight(menu.getItems().length*ApplicationMenu.ITEM_MENU_HEIGHT + 3);
	}
	
	//Установить базовый критерий поиска (при конфигурировании панели)
	public void setBaseCriteria(AdvancedCriteria criteria)
	{
		baseCriteria = criteria;
	}

	public AdvancedCriteria getBaseCriteria()
	{
		return baseCriteria;
	}

	public void setSearchCriteria(AdvancedCriteria criteria)
	{
		searchCriteria = criteria;
	}

	public void update()
	{
		SC.logWarn("GenericPanel: performing update (tg)");
		treeGrid.invalidateCache();
		treeGrid.fetchData(searchCriteria);
		SC.logWarn("GenericPanel: end of update (tg)");
	}
}
