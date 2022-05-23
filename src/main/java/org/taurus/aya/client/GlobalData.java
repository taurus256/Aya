package org.taurus.aya.client;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.data.*;
import com.smartgwt.client.types.*;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.tab.TabSet;
import org.taurus.aya.client.generic.GenericPanel;
import org.taurus.aya.client.widgets.DateControlWidget;

import java.util.LinkedHashMap;

public class GlobalData {

	private static DataSource dataSource_events = createRestEventDS();//DataSource.get("tasks");

	private static DataSource dataSource_tasks = createRestTaskDS();//DataSource.get("tasks");
	private static DataSource dataSource_lanes = createRestLaneDS();//DataSource.get("lanes");
	private static DataSource dataSource_docs = DataSource.get("docs");
	private static DataSource dataSource_content = DataSource.get("content");
	private static DataSource dataSource_scripts = DataSource.get("scripts");
	private static DataSource dataSource_script_content = DataSource.get("script_content");
	private static DataSource dataSource_tags = DataSource.get("tags");
	private static DataSource dataSource_links = DataSource.get("links");
	private static DataSource dataSource_user = createRestUserDS();
	private static DataSource dataSource_group = createRestGroupDS();
	private static DataSource dataSource_relation_user_group = createRestUserGroupDS();
	private static DataSource dataSource_dialogs = DataSource.get("dialogs");
	private static DataSource dataSource_dialog_statistics = DataSource.get("dialog_statistics");
	private static DataSource dataSource_messages = DataSource.get("messages");

	private static Record currentUser;
	private static Record[] users;
	private static Record[] currentUserGroups;
	
	private final static ScriptServiceAsync scriptService = GWT.create( ScriptService.class );
	private final static AnalyticServiceAsync analyticService = GWT.create( AnalyticService.class );

	private final static UserServiceAsync userService = GWT.create( UserService.class );
	
	private static NavigationArea navigationArea;

	private static TaskView taskView;
	private static TabSet topTabSet;
	private static ApplicationMenu applicationMenu;
	private static StatusBar statusBar;
	private static GenericPanel currentPanel;

	private static StatisticsPanel statisticsPanel;

	private static DateControlWidget dateControlWidget;
//	private static Connector connector;
	
	public static final Integer ACCESS_ALL = null;	// This records can be accessed by all users
	public static final int ACCESS_ONLY_AUTHOR = -1; 	// This records can be shown to author

	public static final String LOW_PRIORITY = "Низкий приоритет";
	public static final String NORMAL_PRIORITY = "Нормальный приоритет";
	public static final String HIGH_PRIORITY = "Высокий приоритет";
	
	public static GenericPanel getCurrentPanel() {
		return currentPanel;
	}
	public static void setCurrentPanel(GenericPanel currentPanel) {
		GlobalData.currentPanel = currentPanel;
	}
	
	public static DataSource getDataSource_events() { return dataSource_events;}
	public static DataSource getDataSource_tasks() {return dataSource_tasks;}

	public static DataSource getDataSource_lanes() {
		return dataSource_lanes;
	}
	public static DataSource getDataSource_docs() {
		return dataSource_docs;
	}
	public static DataSource getDataSource_content() {
		return dataSource_content;
	}
	
	public static DataSource getDataSource_scripts() {
		return dataSource_scripts;
	}
	public static DataSource getDataSource_script_content() {
		return dataSource_script_content;
	}
	public static DataSource getDataSource_links() {
		return dataSource_links;
	}

	public static DataSource getDataSource_tags() {
		return dataSource_tags;
	}
	
	public static DataSource getDataSource_relation_user_group() {
		return dataSource_relation_user_group;
	}

	public static ScriptServiceAsync getScriptService()
	{
		return scriptService;
	}

	public static AnalyticServiceAsync getAnalyticService()
	{
		return analyticService;
	}
	
	public static DataSource getDataSource_user() {
		return dataSource_user;
	}
	
	public static DataSource getDataSource_group() {
		return dataSource_group;
	}
	
	public static DataSource getDataSource_dialogs() {
		return dataSource_dialogs;
	}
	
	public static DataSource getDataSource_dialog_statistics() {
		return dataSource_dialog_statistics;
	}
	
	public static DataSource getDataSource_messages() {
		return dataSource_messages;
	}

	public static Record getCurrentUser() {
		return currentUser;
	}
	public static void setCurrentUser(Record currentUser) {
		GlobalData.currentUser = currentUser;
	}

	public static Record[] getUsers() {
		return users;
	}
	public static void setUsers(Record[] users) {
		GlobalData.users = users;
	}
	
	public static Record[] getCurrentUserGroups() {
		return currentUserGroups;
	}
	public static void setCurrentUserGroups(Record[] currentUserGroups) {
		GlobalData.currentUserGroups = currentUserGroups;
	}
	
	public static NavigationArea getNavigationArea() {
		return navigationArea;
	}
	public static void setNavigationArea(NavigationArea navigationArea) {
		GlobalData.navigationArea = navigationArea;
	}
	public static TabSet getTopTabSet() {
		return topTabSet;
	}
	public static void setTopTabSet(TabSet topTabSet) {
		GlobalData.topTabSet = topTabSet;
	}
	public static ApplicationMenu getApplicationMenu() {
		return applicationMenu;
	}
	public static void setApplicationMenu(ApplicationMenu applicationMenu) {
		GlobalData.applicationMenu = applicationMenu;
	}
	
	// Реализует следующее условие для выбора ресурсов:
	// (id != 0) AND ( (rgroup =-1) OR (rgroup in USER_GROUPS) OR (author == current_user_id) )
	public static AdvancedCriteria createSearchCriteria()
	{
		// формируем массив групп
//		ArrayList<Integer> arr = new ArrayList<Integer>();
//		for (Record r: getCurrentUserGroups())
//		{
//			arr.add(r.getAttributeAsInt("id"));
//			SC.logWarn("createSearchCriteria: add group id " + r.getAttributeAsInt("id"));
//		}
//		Integer[] arr1 = {};
//		Integer[] arr2 = (Integer[])(arr.toArray(arr1));
//		AdvancedCriteria crit_index = new AdvancedCriteria("id", OperatorId.NOT_EQUAL,0);
//		AdvancedCriteria crit_group = new AdvancedCriteria(OperatorId.OR, new Criterion[]{new Criterion("rgroup", OperatorId.IS_NULL), new Criterion("rgroup", OperatorId.IN_SET, arr2), new Criterion("author", OperatorId.EQUALS, GlobalData.getCurrentUser().getAttributeAsInt("id"))});
//		AdvancedCriteria advancedCriteria = new AdvancedCriteria(OperatorId.AND, new Criterion[]{crit_index,crit_group});
//
		//return advancedCriteria;
        return new AdvancedCriteria("userId", OperatorId.EQUALS, GlobalData.getCurrentUser().getAttributeAsString("id"));
	}

	//Get all users, belonging to one of groups, with current user belongs to
	public static AdvancedCriteria getUserFilterCriteria()
	{
//		AdvancedCriteria groups_crit = new AdvancedCriteria();
//		ArrayList<Integer> arr = new ArrayList<Integer>();
//		for (Record r:GlobalData.getCurrentUserGroups())
//			arr.add(r.getAttributeAsInt("id"));
//		Integer[] arr1 = {};
//		Integer[] arr2 = (Integer[])(arr.toArray(arr1));
//
//		groups_crit.addCriteria(new Criterion("id", OperatorId.IN_SET, arr2));
		return new AdvancedCriteria();//groups_crit;
	}
	
	public static boolean canWrite(Record r)
	{
		//Если записи нет или она пустая 
		SC.logWarn("GlobalData: canWrite: entering...");
		if (r == null || r.getAttribute("author") == null)
		{
			SC.logWarn("GlobalData: canWrite: INCORRECT DESCRIPTOR!");
			return false;
		}
		SC.logWarn("GlobalData: canWrite: wgroup=" + r.getAttributeAsInt("wgroup"));
		SC.logWarn("GlobalData: canWrite: rgroup=" + r.getAttributeAsInt("rgroup"));

		// Вариант "все"
		if (r.getAttributeAsInt("wgroup") == ACCESS_ALL)	return true;

		// Вариант "текущий пользователь является автором"
		if (r.getAttributeAsInt("author").equals(currentUser.getAttributeAsInt("id")))	return true;

		// Вариант "задана нужная группа"
		for (Record group:currentUserGroups)
		{
			SC.logWarn("canWrite: test group " + r.getAttributeAsInt("wgroup") + " to " + group.getAttributeAsInt("id"));
			if (r.getAttributeAsInt("wgroup").equals(group.getAttributeAsInt("id"))) return true;
		}

		return false;
	}
	
	public static boolean canRead(Record r)
	{
		//Если записи нет или она пустая 

		if (r == null || r.getAttribute("author") == null)
		{
			SC.logWarn("GlobalData: canWrite: INCORRECT DESCRIPTOR!");
			return false;
		}
		
		// Вариант "все"
		if (r.getAttributeAsInt("rgroup") == ACCESS_ALL)	return true;

		// Вариант "текущий пользователь является автором"
		if (r.getAttributeAsInt("author").equals(currentUser.getAttributeAsInt("id")))	return true;

		// Вариант "задана нужная группа"
		for (Record group:currentUserGroups)
		{
			SC.logWarn("canWrite: test group " + r.getAttributeAsInt("rgroup") + " to " + group.getAttributeAsInt("id"));
			if (r.getAttributeAsInt("rgroup").equals(group.getAttributeAsInt("id"))) return true;
		}

		return false;
	}	
	public static StatusBar getStatusBar() {
		return statusBar;
	}

	public static void setStatusBar(StatusBar statusBar) {
		GlobalData.statusBar = statusBar;
	}

	public static StatisticsPanel getStatisticsPanel(){
		if (statisticsPanel == null)
		{
			SC.logWarn("StatisticsPanel IS NULL");
			return null;
		}
		else
			return statisticsPanel;
	}

	public static void setStatisticsPanel(StatisticsPanel statisticsPanel) {
		GlobalData.statisticsPanel = statisticsPanel;
	}

	public static TaskView getTaskView() {
		return taskView;
	}

	public static void setTaskView(TaskView taskView) {
		GlobalData.taskView = taskView;
	}

	public static UserServiceAsync getUserService() {
		return userService;
	}

	public static DateControlWidget getDateControlWidget() {
		return dateControlWidget;
	}

	public static void setDateControlWidget(DateControlWidget dateControlWidget) {
		GlobalData.dateControlWidget = dateControlWidget;
	}

	private static DataSource createRestUserDS() {

		/* Request url*/
		String url = "/users";

		/* Request fields */
		DataSourceField id = new DataSourceField("id", FieldType.INTEGER);
		id.setPrimaryKey(true);
		id.setHidden(true);
		DataSourceField usid = new DataSourceField("usid", FieldType.TEXT);
		usid.setHidden(true);
		DataSourceField password = new DataSourceField("password", FieldType.TEXT,"Новый пароль");
		password.setCanEdit(false); // используется для блокировки смены пароля на форме

		DataSourceField showedName = new DataSourceField("showedName", FieldType.TEXT,"Отображаемое имя");
		showedName.setHidden(true);

		DataSourceField firstname = new DataSourceField("firstname", FieldType.TEXT, "Имя");
		firstname.setRequired(true);

		DataSourceField surname = new DataSourceField("surname", FieldType.TEXT, "Фамилия");
		surname.setRequired(true);

		DataSourceField patronymic = new DataSourceField("patronymic", FieldType.TEXT, "Отчество");
		patronymic.setRequired(true);

		DataSourceField nickname = new DataSourceField("nickname", FieldType.TEXT, "Псевдоним (ник)");
		nickname.setRequired(true);

		DataSourceField useJira = new DataSourceField("useJira", FieldType.BOOLEAN, "Использовать JIRA");
		useJira.setRequired(false);

		DataSourceField jiraLogin = new DataSourceField("jiraLogin", FieldType.TEXT, "Логин JIRA");
		jiraLogin.setRequired(false);

		DataSourceField jiraPass = new DataSourceField("jiraPass", FieldType.TEXT, "Пароль JIRA");
		jiraPass.setRequired(false);

		DataSourceField[] fields = {
									id,
									firstname,
									surname,
									patronymic,
									nickname,
									new DataSourceField("workphone", FieldType.TEXT,"Рабочий телефон"),
									new DataSourceField("mobphone", FieldType.TEXT,"Мобильный телефон"),
									usid,
									password,
									showedName,
									useJira,
									jiraLogin,
									jiraPass
									};

		//return dataSource;
		return createUserDS("/users",fields);
	}

	private static DataSource createRestGroupDS() {

		/* Request url*/
		String dsName = "/groups";

		/* Request fields */
		DataSourceField id = new DataSourceField("id", FieldType.INTEGER);
		id.setPrimaryKey(true);

		DataSourceField[] fields = {
				id,
				new DataSourceField("name", FieldType.TEXT, "Название"),
				new DataSourceField("description", FieldType.TEXT, "Описание"),
		};

		return createDS(dsName,fields);
	}

	private static DataSource createRestUserGroupDS() {

		/* Request url*/
		String dsName = "/user_groups";

		/* Request fields */
		DataSourceField id = new DataSourceField("id", FieldType.INTEGER);
		id.setPrimaryKey(true);

		DataSourceField[] fields = {
				id,
				new DataSourceField("userId", FieldType.INTEGER, "ID пользователя"),
				new DataSourceField("name", FieldType.TEXT, "Название"),
				new DataSourceField("description", FieldType.TEXT, "Описание"),
		};

		return createDS(dsName,fields);
	}

	private static DataSource createRestLaneDS() {

		/* Request url*/
		String dsName = "/lanes";

		/* Request fields */
		DataSourceField id = new DataSourceField("id", FieldType.INTEGER);
		id.setPrimaryKey(true);
		id.setHidden(true);

		DataSourceField wuser = new DataSourceField("wuser", FieldType.INTEGER);
		wuser.setHidden(true);
		DataSourceField wgroup = new DataSourceField("wgroup", FieldType.INTEGER);
		wgroup.setHidden(true);

		DataSourceField ruser = new DataSourceField("ruser", FieldType.INTEGER);
		ruser.setHidden(true);
		DataSourceField rgroup = new DataSourceField("rgroup", FieldType.INTEGER);
		rgroup.setHidden(true);
		DataSourceField is_folder = new DataSourceField("is_folder", FieldType.BOOLEAN);
		is_folder.setHidden(true);

		DataSourceField author = new DataSourceField("author", FieldType.TEXT);
		author.setHidden(true);

		DataSourceField laneOrder = new DataSourceField("laneOrder", FieldType.INTEGER);
		laneOrder.setHidden(true);

		DataSourceField description = new DataSourceField("description", FieldType.TEXT, "Описание");
		DataSourceField[] fields = {
				id,
				new DataSourceField("name", FieldType.TEXT, "Название"),
				description,
				laneOrder,
				new DataSourceField("visible", FieldType.BOOLEAN, "Отображать"),
				new DataSourceField("analysed", FieldType.BOOLEAN, "Анализировать"),
				author,
				is_folder,
				wuser,
				wgroup,
				ruser,
				rgroup
		};

		return createDS(dsName,fields);
	}

	private static DataSource createRestTaskDS() {

		/* Datasource name*/
		String dsName = "/tasks";

		DataSourceField id= new DataSourceField("id", FieldType.INTEGER);
		id.setPrimaryKey(true);

		//eventWindowStyle.setCanView(false);
		DataSourceField icon = new DataSourceField("icon", FieldType.TEXT);
		//icon.setCanView(false);

		DataSourceField state = new DataSourceField("state", FieldType.INTEGER);
		//state.setCanView(false);
		//start.setCanView(false);
		DataSourceField processTime = new DataSourceField("spentTime", FieldType.FLOAT, "Затрачено времени");
		processTime.setDecimalPrecision(1);

		DataSourceField lane = new DataSourceField("lane", FieldType.TEXT, "Поток");
		lane.setForeignKey("lanes.name");
		lane.setForeignDisplayField("name");
		lane.setUseLocalDisplayFieldValue(false);
		lane.setRequired(true);

		DataSourceField priority = new DataSourceField("priority", FieldType.INTEGER, "Приоритет");
		LinkedHashMap<Integer,String> valueMapPriority = new LinkedHashMap<>();

		valueMapPriority.put(0,LOW_PRIORITY);
		valueMapPriority.put(1,NORMAL_PRIORITY);
		valueMapPriority.put(2,HIGH_PRIORITY);
		priority.setValueMap(valueMapPriority);

		DataSourceField plannedDuration = new DataSourceField("plannedDuration", FieldType.FLOAT, "Планируемое время");
		plannedDuration.setDecimalPrecision(1);

		DataSourceField[] fields = {
				id,
				new DataSourceField("taskId", FieldType.INTEGER,"Предыдущая задача"),
				lane,
				new DataSourceField("name", FieldType.TEXT, "Название"),
				new DataSourceField("description", FieldType.TEXT,"Описание"),
				new DataSourceField("startDate", FieldType.DATETIME, "Дата начала"),
				new DataSourceField("endDate", FieldType.DATETIME, "Дата завершения"),
				new DataSourceField("executor", FieldType.INTEGER, "Исполнитель",300, true),
				priority,
				plannedDuration,
				icon,
				state,
				new DataSourceField("executorName", FieldType.TEXT, "Имя исполнителя"),
				new DataSourceField("author", FieldType.TEXT, "Задача создана"),
				processTime,
				new DataSourceField("userCorrectSpentTime",FieldType.BOOLEAN),
				new DataSourceField("fragmented",FieldType.BOOLEAN),
				new DataSourceField("wuser", FieldType.INTEGER),
				new DataSourceField("wgroup", FieldType.INTEGER),
				new DataSourceField("ruser", FieldType.INTEGER),
				new DataSourceField("rgroup", FieldType.INTEGER),
				new DataSourceField("externalJiraTaskId",FieldType.TEXT, "ID задачи Jira")
		};

		return createDS(dsName,fields);
	}

	private static DataSource createRestEventDS() {

		/* Datasource name*/
		String dsName = "/events";

		DataSourceField id= new DataSourceField("id", FieldType.INTEGER);
		id.setPrimaryKey(true);

		DataSourceField parent = new DataSourceField("parent", FieldType.INTEGER);
		parent.setRootValue(0);
		parent.setForeignKey("id");
		/* Request fields */
		DataSourceField eventWindowStyle = new DataSourceField("eventWindowStyle", FieldType.TEXT);
		//eventWindowStyle.setCanView(false);
		DataSourceField icon = new DataSourceField("icon", FieldType.TEXT);
		//icon.setCanView(false);

		DataSourceField state = new DataSourceField("state", FieldType.INTEGER);
		//state.setCanView(false);
		DataSourceField is_backlog = new DataSourceField("isBacklog", FieldType.BOOLEAN);
		//is_backlog.setCanView(false);
		//start.setCanView(false);
		DataSourceField is_graph = new DataSourceField("isGraph", FieldType.BOOLEAN);
		//is_graph.setCanView(false);
		DataSourceField spentTime = new DataSourceField("spentTime", FieldType.FLOAT, "Затрачено на фрагмент (часов)");
		spentTime.setDecimalPrecision(1);

		DataSourceField lane = new DataSourceField("lane", FieldType.TEXT, "Поток");
		lane.setForeignKey("lanes.name");
		lane.setForeignDisplayField("name");
        lane.setUseLocalDisplayFieldValue(false);

		DataSourceField priority = new DataSourceField("priority", FieldType.INTEGER, "Приоритет");
		LinkedHashMap<Integer,String> valueMapPriority = new LinkedHashMap<>();

		valueMapPriority.put(0,LOW_PRIORITY);
		valueMapPriority.put(1,NORMAL_PRIORITY);
		valueMapPriority.put(2,HIGH_PRIORITY);
		priority.setValueMap(valueMapPriority);

		DataSourceField duration_h = new DataSourceField("duration_h", FieldType.FLOAT, "Планируемое время (часов)");
		duration_h.setDecimalPrecision(1);

		DataSourceField[] fields = {
				id,
				parent,
				new DataSourceField("taskId", FieldType.INTEGER,"Задача"),
				new DataSourceField("index", FieldType.INTEGER,"Фрагмент"),
				lane,
				new DataSourceField("name", FieldType.TEXT, "Название"),
				new DataSourceField("description", FieldType.TEXT,"Описание"),
				new DataSourceField("startDate", FieldType.DATETIME, "Дата начала"),
				new DataSourceField("endDate", FieldType.DATETIME, "Дата завершения"),
				new DataSourceField("externalUrl", FieldType.TEXT, "Ссылка"),
				eventWindowStyle,
				new DataSourceField("executor", FieldType.INTEGER, "Исполнитель"),
				priority,
				new DataSourceField("duration_d", FieldType.INTEGER, "Время исполнения (дней)"),
				duration_h,
				icon,
				state,
				new DataSourceField("executorName", FieldType.TEXT, "Имя исполнителя"),
				new DataSourceField("author", FieldType.TEXT, "Задача создана"),
				is_backlog,
				spentTime,
    			is_graph,
				new DataSourceField("userCorrectSpentTime",FieldType.BOOLEAN),
				new DataSourceField("fragmented",FieldType.BOOLEAN),
				new DataSourceField("wuser", FieldType.INTEGER),
				new DataSourceField("wgroup", FieldType.INTEGER),
				new DataSourceField("ruser", FieldType.INTEGER),
				new DataSourceField("rgroup", FieldType.INTEGER),
				new DataSourceField("externalJiraTaskId", FieldType.TEXT, "ID задачи Jira")
		};

		return createDS(dsName,fields);
	}

	private static DataSource createUserDS(String name, DataSourceField[] fields)
	{
		DataSource dataSource =
				new RestDataSource() {
					protected Object transformRequest(DSRequest dsRequest) {
						return super.transformRequest(dsRequest);
					}

					protected void transformResponse(DSResponse response, DSRequest request, Object data) {
						super.transformResponse(response, request, data);
					}
				};

		dataSource.setID(name);
		dataSource.setDataFormat(DSDataFormat.JSON);
		dataSource.setDataProtocol(DSProtocol.GETPARAMS);
		dataSource.setJsonPrefix("");
		dataSource.setJsonSuffix("");

		//set up FETCH to use POST requests
		OperationBinding fetch = new OperationBinding();
		fetch.setDataURL(name + "/fetch");
		fetch.setOperationType(DSOperationType.FETCH);
		fetch.setDataProtocol(DSProtocol.POSTPARAMS);

		//set up ADD to use POST requests~`
		OperationBinding add = new OperationBinding();
		add.setDataURL(name + "/modify");
		add.setOperationType(DSOperationType.ADD);
		add.setDataProtocol(DSProtocol.POSTPARAMS);

		//set up UPDATE to use POST
		OperationBinding update = new OperationBinding();
		update.setOperationType(DSOperationType.UPDATE);
		update.setDataURL(name + "/modify");
		update.setDataProtocol(DSProtocol.POSTPARAMS);

		//set up REMOVE to use DELETE
		OperationBinding remove = new OperationBinding();
		remove.setOperationType(DSOperationType.REMOVE);
		remove.setDataURL(name + "/modify");
		remove.setDataProtocol(DSProtocol.POSTPARAMS);

		//set up custom operation
		OperationBinding custom = new OperationBinding();
		custom.setOperationType(DSOperationType.CUSTOM);
		custom.setDataURL(name + "/");
		custom.setDataProtocol(DSProtocol.GETPARAMS);

		dataSource.setOperationBindings(fetch, add, update, remove, custom);

		dataSource.setDataURL(name);
		dataSource.setFields(fields);

		/* finally set data source */
		return dataSource;
	}

	private static DataSource createDS(String name, DataSourceField[] fields)
	{
		DataSource dataSource =
				new RestDataSource() {
					protected Object transformRequest(DSRequest dsRequest) {
						return super.transformRequest(dsRequest);
					}

					protected void transformResponse(DSResponse response, DSRequest request, Object data) {
						super.transformResponse(response, request, data);
					}
				};

		dataSource.setID(name);
		dataSource.setDataFormat(DSDataFormat.JSON);
		dataSource.setDataProtocol(DSProtocol.GETPARAMS);
		dataSource.setJsonPrefix("");
		dataSource.setJsonSuffix("");

		//set up FETCH to use POST requests
		OperationBinding fetch = new OperationBinding();
		fetch.setDataURL(name + "/fetch");
		fetch.setOperationType(DSOperationType.FETCH);
		fetch.setDataProtocol(DSProtocol.POSTPARAMS);

		//set up ADD to use POST requests~`
		OperationBinding add = new OperationBinding();
		add.setDataURL(name + "/modify");
		add.setOperationType(DSOperationType.ADD);
		add.setDataProtocol(DSProtocol.POSTPARAMS);

		//set up UPDATE to use POST
		OperationBinding update = new OperationBinding();
		update.setOperationType(DSOperationType.UPDATE);
		update.setDataURL(name + "/modify");
		update.setDataProtocol(DSProtocol.POSTPARAMS);

		//set up REMOVE to use POST
		OperationBinding remove = new OperationBinding();
		remove.setOperationType(DSOperationType.REMOVE);
		remove.setDataURL(name + "/modify");
		remove.setDataProtocol(DSProtocol.POSTPARAMS);

		//set up CUSTOM to use POST
		OperationBinding custom = new OperationBinding();
		custom.setOperationType(DSOperationType.CUSTOM);
		custom.setDataURL(name + "/modify");
		custom.setDataProtocol(DSProtocol.POSTPARAMS);

		dataSource.setOperationBindings(fetch, add, update, remove, custom);

		dataSource.setDataURL(name);
		dataSource.setFields(fields);
		dataSource.setPreventHTTPCaching(true);
		dataSource.setCacheAllData(false);
		dataSource.setCacheAcrossOperationIds(false);

		/* finally set data source */
		return dataSource;
	}
}