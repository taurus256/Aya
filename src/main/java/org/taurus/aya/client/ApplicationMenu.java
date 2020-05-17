package org.taurus.aya.client;

import com.google.gwt.user.client.Cookies;
import com.smartgwt.client.data.*;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuItem;
import com.smartgwt.client.widgets.menu.MenuItemSeparator;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;
import com.smartgwt.client.widgets.toolbar.ToolStrip;
import com.smartgwt.client.widgets.toolbar.ToolStripButton;
import com.smartgwt.client.widgets.toolbar.ToolStripMenuButton;
import org.taurus.aya.client.TabManager.ResourceType;
import org.taurus.aya.client.dialogs.*;
import org.taurus.aya.client.generic.GenericPropertiesDialog;
import org.taurus.aya.shared.Command;
import org.taurus.aya.shared.Command.CommandType;

import java.util.Date;

public class ApplicationMenu extends HLayout {
	
	
    private static final int APPLICATION_MENU_HEIGHT = 27;
    public static  final int ITEM_MENU_HEIGHT = 23;
    public static final int CONTEXT_MENU_HEIGHT = 24;
    
    Menu createResourceMenu = null;
    Menu userMenu = null;
    Menu viewMenu = null;
    MenuItem createLink;
 
    static TabManager.ResourceType resourceEnumValues[] = TabManager.ResourceType.values();
    
    private ResourceType resourceType;
    private Record resourceRecord;
   
    public ApplicationMenu() {
         
        super();
        this.setHeight(APPLICATION_MENU_HEIGHT);

        ToolStrip toolStrip = new ToolStrip();
	    toolStrip.setWidth100();
        
//        ToolStripButton iconButton = new ToolStripButton();
//	    iconButton.setIcon("S5icon.png");
//	    toolStrip.addButton(iconButton);
//	    iconButton.addClickHandler(new ClickHandler(){
//
//			@Override
//			public void onClick(ClickEvent event) {
//				GlobalData.getNavigationArea().toggle();
//			}});
//	    
//	    /*										NEW Menu										*/
	    /****************************************************************************************/
	    
	    createResourceMenu = new Menu();
	    
	    MenuItem createTask = new MenuItem("Задачу");
	    createTask.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {

			@Override
			public void onClick(MenuItemClickEvent event) {
				CommandExecutor.exec(new Command(CommandType.CREATE_BACKLOG_TASK));
			}
		});
	    
	    MenuItem createDocument = new MenuItem("Документ");
	    createDocument.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {

			@Override
			public void onClick(MenuItemClickEvent event) {
				Record r = new Record();
				r.setAttribute("parent",-1);
				r.setAttribute("author",GlobalData.getCurrentUser().getAttribute("id"));
				r.setAttribute("rgroup",GlobalData.ACCESS_ALL);
				r.setAttribute("wgroup",GlobalData.ACCESS_ALL);
			}
		});
	    
	    MenuItem createScript = new MenuItem("Скрипт");
	    createScript.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {

			@Override
			public void onClick(MenuItemClickEvent event) {
				Record r = new Record();
				r.setAttribute("parent",0);
				r.setAttribute("author",GlobalData.getCurrentUser().getAttribute("id"));
				r.setAttribute("rgroup",GlobalData.ACCESS_ALL);
				r.setAttribute("wgroup",GlobalData.ACCESS_ALL);
			}
		});
	    
	    MenuItem createTag = new MenuItem("Тэг");
	    createTag.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {

			@Override
			public void onClick(MenuItemClickEvent event) {
				Record r = new Record();
				r.setAttribute("parent",0);
				r.setAttribute("author",GlobalData.getCurrentUser().getAttribute("id"));
				r.setAttribute("rgroup",GlobalData.ACCESS_ALL);
				r.setAttribute("wgroup",GlobalData.ACCESS_ALL);
				GenericPropertiesDialog gpd = new GenericPropertiesDialog(r, "tag.png", ResourceType.TAG, GlobalData.getDataSource_tags(), "тега");
			}
		});
	    
	    createLink = new MenuItem("Ссылку");
	    createLink.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler(){

			@Override
			public void onClick(MenuItemClickEvent event) {
				CommandExecutor.exec(new Command(CommandType.CREATE_LINK,resourceType,resourceRecord.getAttributeAsString("name") + "\"", resourceRecord.getAttributeAsInt("id")));
			}});
	    createLink.setEnabled(false);
	    
	    createResourceMenu.setData(createTask,createDocument,createScript,createTag,new MenuItemSeparator(),createLink);
	    createResourceMenu.setHeight(6 *ITEM_MENU_HEIGHT - 2);
	    
	    ToolStripMenuButton menuCreateButton = new ToolStripMenuButton("Создать", createResourceMenu);
	    toolStrip.addMenuButton(menuCreateButton);
	    
	    /* 										View menu										*/
	    /********************************************************************************************/
	    
	    viewMenu = new Menu();

		final MenuItem setWeekMode = new MenuItem("Недельный график");
		final MenuItem setMonthMode = new MenuItem("Месячный график");
		final MenuItem setViewMyTasks = new MenuItem("Показывать только мои задачи");
		final MenuItem setViewAllTasks = new MenuItem("Показывать задачи всех пользователей");
		setWeekMode.setChecked(true);
		setViewMyTasks.setChecked(true);

		setWeekMode.addClickHandler(event -> { GlobalData.getTaskView().setWeekMode(true); setWeekMode.setChecked(true); setMonthMode.setChecked(false); viewMenu.refreshRow(0); viewMenu.refreshRow(1);});
		setMonthMode.addClickHandler(event -> { GlobalData.getTaskView().setWeekMode(false); setWeekMode.setChecked(false); setMonthMode.setChecked(true); viewMenu.refreshRow(0); viewMenu.refreshRow(1);});
		setViewMyTasks.addClickHandler(event -> { GlobalData.getTaskView().setViewMyTasksMode(true); setViewMyTasks.setChecked(true); setViewAllTasks.setChecked(false); viewMenu.refreshRow(3); viewMenu.refreshRow(4);});
		setViewAllTasks.addClickHandler(event -> { GlobalData.getTaskView().setViewMyTasksMode(false); setViewMyTasks.setChecked(false); setViewAllTasks.setChecked(true); viewMenu.refreshRow(3); viewMenu.refreshRow(4);});

		viewMenu.setData(setWeekMode,setMonthMode,new MenuItemSeparator(), setViewMyTasks, setViewAllTasks);
		viewMenu.setHeight(5 *ITEM_MENU_HEIGHT - 2);

		ToolStripMenuButton viewMenuButton = new ToolStripMenuButton("Вид", viewMenu);
		toolStrip.addMenuButton(viewMenuButton);

		/* 										Administration menu									*/
	    /********************************************************************************************/
	    
	    Menu editMenu = new Menu();
	    editMenu.setCanSelectParentItems(true); 

	    
	    MenuItem lanesManager = new MenuItem("Управление потоками");
	    lanesManager.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {

			@Override
			public void onClick(MenuItemClickEvent event) {
				LaneCreationDialog ld = new LaneCreationDialog(GlobalData.getDataSource_lanes());
				ld.show();
			}
		});


	    MenuItem usersManager = new MenuItem("Управление пользователями");
	    usersManager.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {

			@Override
			public void onClick(MenuItemClickEvent event) {
				UserManagementDialog umd = new UserManagementDialog();
				umd.show();
			}
		});
	    
	    MenuItem groupsManager = new MenuItem("Управление группами");
	    groupsManager.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler() {

			@Override
			public void onClick(MenuItemClickEvent event) {
				GroupManagementDialog gmd = new GroupManagementDialog();
				gmd.show();
			}
		});


	    ToolStrip toolStripRight = new ToolStrip();
	    toolStripRight.setAlign(Alignment.RIGHT);
	    editMenu.setData(lanesManager,usersManager,groupsManager);
	    editMenu.setHeight(2 *ITEM_MENU_HEIGHT - 1);

	    ToolStripMenuButton menuButton = new ToolStripMenuButton("Настройки", editMenu);
	    toolStrip.addMenuButton(menuButton);
	    
	    /*				 							Help menu										*/
	    /********************************************************************************************/
	    
	    Menu helpMenu = new Menu();

	    MenuItem hotKeysItem = new MenuItem("Горячие клавиши");
		hotKeysItem.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler(){

			@Override
			public void onClick(MenuItemClickEvent event) {
				SC.say("Горячие клавиши","<table width=300" +
						"<tr><td><b>Действие</b></td><td><b>Linux</b></td><td><b>Windows</b></td></tr>" +
						"<tr><td>Показать/скрыть панель задач</td><td>Alt+1</td><td>Alt+1</td></tr>" +
						"<tr><td>Показать/скрыть панель статистики</td><td>Alt+2</td><td>Alt+2</td></tr>" +
						"<tr><td>Показать свойства задачи в графике</td><td>Alt+Shift+клик по задаче</td><td>Alt+клик по задаче</td></tr>" +
						"</table>");
			}

		});

	    MenuItem showConsoleItem = new MenuItem("Консоль SmartGWT");
	    com.smartgwt.client.widgets.menu.events.ClickHandler handler = new com.smartgwt.client.widgets.menu.events.ClickHandler(){
			@Override
			public void onClick(MenuItemClickEvent event) {
				SC.showConsole();
			}
		};
	    showConsoleItem.addClickHandler(handler);
	    helpMenu.addItem(showConsoleItem);

	    MenuItem aboutItem = new MenuItem("О программе");
	    aboutItem.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler(){

			@Override
			public void onClick(MenuItemClickEvent event) {
				new AboutDialog();
			}
			
		});
	    
	    helpMenu.setData(hotKeysItem, new MenuItemSeparator(), showConsoleItem, aboutItem);

	    helpMenu.setHeight(2 *ITEM_MENU_HEIGHT);
	    
	    ToolStripMenuButton menuHelpButton = new ToolStripMenuButton("Помощь", helpMenu);
		    toolStrip.addMenuButton(menuHelpButton);

		
		/* 								Current user menu (profile etc...)							*/
	    /********************************************************************************************/

		MenuItem[] userMenuArray = new MenuItem[3];

		// Профиль пользователя
	    MenuItem showProfile = new MenuItem("Профиль");
	    showProfile.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler(){

			@Override
			public void onClick(MenuItemClickEvent event) {
				UserProfileDialog upd = new UserProfileDialog(GlobalData.getCurrentUser()); 
				upd.show();
			}});
	    userMenuArray[0] = showProfile;
	    
	    userMenuArray[1] = new MenuItemSeparator();
	    
	    //Кнопка смены пользователя
//	    MenuItem changeUser = new MenuItem("Сменить пользователя");
//	    changeUser.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler(){
//
//			@Override
//			public void onClick(MenuItemClickEvent event) {
//				//показать диалог смены пользователя
//				UserChangeDialog d = new UserChangeDialog();
//				d.show();
//
//			}});
//	    userMenuArray[2] = changeUser;
	   
	    MenuItem forgetUser= new MenuItem("Выйти");
	    forgetUser.addClickHandler(new com.smartgwt.client.widgets.menu.events.ClickHandler(){

				@Override
				public void onClick(MenuItemClickEvent event) {
					//обнулить cookie и обновить страницу
					Cookies.setCookie("usid", "", new Date(System.currentTimeMillis()+30L*24*3600*1000),"","/",false);
	    			com.google.gwt.user.client.Window.Location.reload();
				}});

	    userMenuArray[2] = forgetUser;
	     
	    final Menu userMenu = new Menu();
	    
	    userMenu.setData(userMenuArray);
	    userMenu.setHeight(2 *ITEM_MENU_HEIGHT);
	    
	    final ToolStripButton userButton = new ToolStripButton();
	    userButton.setTitle(GlobalData.getCurrentUser().getAttributeAsString("firstname") + " " + GlobalData.getCurrentUser().getAttributeAsString("surname"));

	    userButton.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				userMenu.showNextTo(userButton,"bottom");
			}});
	    
	    toolStripRight.addButton(userButton);
		
	    this.addMember(toolStrip);
	    this.addMember(toolStripRight);
	    GlobalData.setApplicationMenu(this);
    }
    
    /* Methods for dynamically-created Operations menu*/
    
	com.smartgwt.client.widgets.menu.events.ClickHandler scriptHandler = new com.smartgwt.client.widgets.menu.events.ClickHandler(){


		@Override
		public void onClick(MenuItemClickEvent event) {
			SC.say(event.getItem().getAttributeAsString("id"));
			
		}
	};
    
    //Обновить меню операций и запомнить данные для генерации ссылки, если потребуется
    public void updateMenu(ResourceType type, Record r)
    {
    	resourceType = type;
    	resourceRecord = r;
    	if (type.ordinal()<5)
    		createLink.setEnabled(true);
    	else
    		createLink.setEnabled(false);
    	createResourceMenu.refreshFields();
    }
    
}
