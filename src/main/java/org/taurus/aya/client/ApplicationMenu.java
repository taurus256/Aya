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
import java.util.function.Consumer;

public class ApplicationMenu extends HLayout {
	
	
    private static final int APPLICATION_MENU_HEIGHT = 27;
    public static  final int ITEM_MENU_HEIGHT = 23;

    Menu layoutMenu = null;
    Menu userMenu = null;
    Menu viewMenu = null;
    MenuItem createLink;
 
    static TabManager.ResourceType resourceEnumValues[] = TabManager.ResourceType.values();
    
    private ResourceType resourceType;
    private Record resourceRecord;
	private final MenuItem switchTasksView;
	private final MenuItem switchStatisticsView;
	private final MenuItem setWeekMode;
	private final MenuItem setMonthMode;
	private final MenuItem setViewMyTasks;
	private final MenuItem setViewAllTasks;

	public ApplicationMenu() {
         
        super();
        this.setHeight(APPLICATION_MENU_HEIGHT);

        ToolStrip toolStrip = new ToolStrip();
	    toolStrip.setWidth100();
        
//	    /*										NEW Menu										*/
	    /****************************************************************************************/
	    
	    layoutMenu = new Menu();

		switchTasksView = new MenuItem("Панель ожидающих задач");
		switchTasksView.setKeyTitle("<sup>Alt+1</sup>");
		switchTasksView.setChecked(true);
	    switchTasksView.addClickHandler(event -> Aya.switchTaskPanel());

		switchStatisticsView = new MenuItem("Панель статистики");
		switchStatisticsView.setKeyTitle("<sup>Alt+2</sup>");
	    switchStatisticsView.addClickHandler(event -> Aya.switchStatisticsPanel());
	    
	    layoutMenu.setData(switchTasksView, switchStatisticsView);
	    layoutMenu.setHeight(2 *ITEM_MENU_HEIGHT - 2);
	    
	    ToolStripMenuButton layoutControlButton = new ToolStripMenuButton("Панели", layoutMenu);
	    toolStrip.addMenuButton(layoutControlButton);
	    
	    /* 										View menu										*/
	    /********************************************************************************************/
	    
	    viewMenu = new Menu();

		setWeekMode = new MenuItem("На неделю");
		setWeekMode.setKeyTitle("<sup>Alt+W</sup>");
		setMonthMode = new MenuItem("На месяц");
		setMonthMode.setKeyTitle("<sup>Alt+Q</sup>");
		setViewMyTasks = new MenuItem("Показывать только мои задачи");
		setViewMyTasks.setKeyTitle("<sup>Alt+M</sup>");
		setViewAllTasks = new MenuItem("Показывать задачи всех пользователей");
		setViewAllTasks.setKeyTitle("<sup>Alt+C</sup>");

		setWeekMode.setChecked(true);
		setViewMyTasks.setChecked(true);

		setWeekMode.addClickHandler(event -> GlobalData.getTaskView().setWeekMode(true));
		setMonthMode.addClickHandler(event -> GlobalData.getTaskView().setWeekMode(false));
		setViewMyTasks.addClickHandler(event -> GlobalData.getTaskView().setViewMyTasksMode(true));
		setViewAllTasks.addClickHandler(event -> GlobalData.getTaskView().setViewMyTasksMode(false));

		viewMenu.setData(setWeekMode, setMonthMode,new MenuItemSeparator(), setViewMyTasks, setViewAllTasks);
		viewMenu.setHeight(5 *ITEM_MENU_HEIGHT - 2);

		ToolStripMenuButton viewMenuButton = new ToolStripMenuButton("График", viewMenu);
		toolStrip.addMenuButton(viewMenuButton);

		/* 										Administration menu									*/
	    /********************************************************************************************/
	    
	    Menu editMenu = new Menu();
	    editMenu.setCanSelectParentItems(true); 

	    
	    MenuItem lanesManager = new MenuItem("Управление потоками");
	    lanesManager.addClickHandler(event -> {
			LaneCreationDialog ld = new LaneCreationDialog(GlobalData.getDataSource_lanes(), new Consumer(){
				@Override
				public void accept(Object o) {
					GlobalData.getTaskView().updateTimeline();
				}
			});
			ld.show();
		});


	    MenuItem usersManager = new MenuItem("Управление пользователями");
	    usersManager.addClickHandler(event -> {
			UserManagementDialog umd = new UserManagementDialog();
			umd.show();
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
		hotKeysItem.addClickHandler(event -> SC.say("Горячие клавиши","" +
				"<table width=500>" +
				"<tr><td><b>Действия с панелями</b></td><td><b>Сочетание<sup>*</sup></b></td></tr>" +
				"<tr><td>Показать/скрыть панель задач</td><td>Alt+1</td></tr>" +
				"<tr><td>Показать/скрыть панель статистики</td><td>Alt+2</td></tr>" +
				"</table>" +
				"<table width=500>" +
				"<tr><td><b>Переключение режимов графика</b></td><td><b>Сочетание<sup>*</sup></b></td></tr>" +
				"<tr><td>График на месяц</td><td>Alt+Q</td></tr>" +
				"<tr><td>График на неделю</td><td>Alt+W</td></tr>" +
				"<tr><td>Показывать только мои задачи</td><td>Alt+M</td></tr>" +
				"<tr><td>Показывать задачи всей команды</td><td>Alt+C</td></tr>" +
				"</table>" +
				"<table width=500>" +
				"<tr><td><b>Действия с задачами</b></td><td><b>Сочетание<sup>*</sup></b></td></tr>" +
				"<tr><td>Показать/скрыть панель задач</td><td>Alt+1</td></tr>" +
				"<tr><td>Показать/скрыть панель статистики</td><td>Alt+2</td></tr>" +
				"<tr><td>Открыть свойства задачи</td><td>Alt+клик по задаче</td></tr>" +
				"<tr><td>Начать выполнение текущей задачи (Activate)</td><td>Alt+A</td></tr>" +
				"<tr><td>Приостановить выполнение текущей задачи (Suspend)</td><td>Alt+S</td></tr>" +
				"<tr><td>Завершить выполнение текущей задачи (Done)</td><td>Alt+D</td></tr>" +
				"<tr><td>Установить статус \"внимание\" текущей задаче (Fail)</td><td>Alt+F</td></tr>" +
				"<tr><td>Сбросить статус текщей задачи (General)</td><td>Alt+G</td></tr>" +
				"</table>" +
				"<br><sup>*</sup> Вместо Alt можно использовать сочетание Alt+Shift, если сочетание с Alt занято браузером или операционной системой"));

	    MenuItem showConsoleItem = new MenuItem("Консоль SmartGWT");
	    com.smartgwt.client.widgets.menu.events.ClickHandler handler = event -> SC.showConsole();
	    showConsoleItem.addClickHandler(handler);
	    helpMenu.addItem(showConsoleItem);

	    MenuItem aboutItem = new MenuItem("О программе");
	    aboutItem.addClickHandler(event -> new AboutDialog());
	    
	    helpMenu.setData(hotKeysItem, new MenuItemSeparator(), showConsoleItem, aboutItem);

	    helpMenu.setHeight(2 *ITEM_MENU_HEIGHT);
	    
	    ToolStripMenuButton menuHelpButton = new ToolStripMenuButton("Помощь", helpMenu);
		    toolStrip.addMenuButton(menuHelpButton);

		
		/* 								Current user menu (profile etc...)							*/
	    /********************************************************************************************/

		MenuItem[] userMenuArray = new MenuItem[3];

		// Профиль пользователя
	    MenuItem showProfile = new MenuItem("Профиль");
	    showProfile.addClickHandler(event -> {
			UserProfileDialog upd = new UserProfileDialog(GlobalData.getCurrentUser());
			upd.show();
		});
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
	    forgetUser.addClickHandler(event -> {
			//обнулить cookie и обновить страницу
			Cookies.setCookie("usid", "", new Date(System.currentTimeMillis()+30L*24*3600*1000),"","/",false);
			com.google.gwt.user.client.Window.Location.reload();
		});

	    userMenuArray[2] = forgetUser;
	     
	    final Menu userMenu = new Menu();
	    
	    userMenu.setData(userMenuArray);
	    userMenu.setHeight(2 *ITEM_MENU_HEIGHT);
	    
	    final ToolStripButton userButton = new ToolStripButton();
	    userButton.setTitle(GlobalData.getCurrentUser().getAttributeAsString("firstname") + " " + GlobalData.getCurrentUser().getAttributeAsString("surname"));

	    userButton.addClickHandler(event -> userMenu.showNextTo(userButton,"bottom"));
	    
	    toolStripRight.addButton(userButton);
		
	    this.addMember(toolStrip);
	    this.addMember(toolStripRight);
	    GlobalData.setApplicationMenu(this);
    }
    
    //Обновить меню
    public void setCheckedSwitchTasksMenu(boolean state){
    	switchTasksView.setChecked(state);
		layoutMenu.refreshFields();
	}

	public void setCheckedSwitchStatisticsMenu(boolean state){
		switchStatisticsView.setChecked(state);
		layoutMenu.refreshFields();
	}

	public void setWeekModeMenu(boolean state){
		if (state) {
			setWeekMode.setChecked(true); setMonthMode.setChecked(false);
		}
		else{
			setWeekMode.setChecked(false); setMonthMode.setChecked(true);
		}
		viewMenu.refreshRow(0);
		viewMenu.refreshRow(1);
	}

	public void setViewMyTasksMenu(boolean state){
		if (state){
			setViewMyTasks.setChecked(true); setViewAllTasks.setChecked(false);
		}else{
			setViewMyTasks.setChecked(false); setViewAllTasks.setChecked(true);
		}
		viewMenu.refreshRow(3);
		viewMenu.refreshRow(4);
	}
}
