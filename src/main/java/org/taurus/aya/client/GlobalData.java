package org.taurus.aya.client;

import com.google.gwt.core.client.GWT;
import com.smartgwt.client.data.*;
import com.smartgwt.client.types.DSDataFormat;
import com.smartgwt.client.types.DSOperationType;
import com.smartgwt.client.types.DSProtocol;
import com.smartgwt.client.types.FieldType;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.tab.TabSet;
import org.taurus.aya.client.generic.GenericPanel;

import java.util.LinkedHashMap;

public class GlobalData {

	private static DataSource dataSource_tasks = createRestEventDS();//DataSource.get("tasks");
	private static DataSource dataSource_lanes = createRestLaneDS();//DataSource.get("lanes");
	private static DataSource dataSource_docs = DataSource.get("docs");
	private static DataSource dataSource_content = DataSource.get("content");
	private static DataSource dataSource_scripts = DataSource.get("scripts");
	private static DataSource dataSource_script_content = DataSource.get("script_content");
	private static DataSource dataSource_tags = DataSource.get("tags");
	private static DataSource dataSource_links = DataSource.get("links");
	private static DataSource dataSource_user = createRestUserDS();
	private static DataSource dataSource_group = DataSource.get("group");
	private static DataSource dataSource_relation_user_group = createRestGroupDS();
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
	private static TabSet topTabSet;
	private static ViewManager viewManager;
	private static ApplicationMenu applicationMenu;
	private static StatusBar statusBar;
	private static GenericPanel currentPanel;
//	private static Connector connector;
	
	public static final Integer ACCESS_ALL = null;	// This records can be accessed by all users
	public static final int ACCESS_ONLY_AUTHOR = -1; 	// This records can be shown to author
	
	public static GenericPanel getCurrentPanel() {
		return currentPanel;
	}
	public static void setCurrentPanel(GenericPanel currentPanel) {
		GlobalData.currentPanel = currentPanel;
	}
	
	public static DataSource getDataSource_tasks() {
		return dataSource_tasks;
	}
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

	public static void setViewManager(ViewManager manager)
	{
		viewManager = manager;
	}
	
	public static ViewManager getViewManager()
	{
		return viewManager;
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
        return new AdvancedCriteria();
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
		if (r.getAttributeAsInt("wgroup").equals(ACCESS_ALL))	return true;

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

	public static UserServiceAsync getUserService() {
		return userService;
	}


//	public static Connector getConnector() {
//		return connector;
//	}
//
//	public static void setConnector(Connector connector_) {
//		connector = connector_;
//	}

	private static DataSource createRestUserDS() {

		/* Request url*/
		String url = "/users";

		/* Request fields */
		DataSourceField[] fields = {
									new DataSourceField("id", FieldType.INTEGER),
									new DataSourceField("firstname", FieldType.TEXT),
									new DataSourceField("surname", FieldType.TEXT),
									new DataSourceField("patronymic", FieldType.TEXT),
									new DataSourceField("nickname", FieldType.TEXT),
									new DataSourceField("workphone", FieldType.TEXT),
									new DataSourceField("mobphone", FieldType.TEXT),
									new DataSourceField("usid", FieldType.TEXT),
									new DataSourceField("passwordHash", FieldType.TEXT),
									new DataSourceField("showed_name", FieldType.TEXT)
									};

		DataSource dataSource =
				new RestDataSource() {

					protected Object transformRequest(DSRequest dsRequest) {
						//dsRequest.setParams(getRequestParams());
						return super.transformRequest(dsRequest);
					}

					protected void transformResponse(DSResponse response, DSRequest request, Object data) {
						super.transformResponse(response, request, data);
					}
				};

		dataSource.setDataFormat(DSDataFormat.JSON);
		dataSource.setDataProtocol(DSProtocol.GETPARAMS);
		dataSource.setJsonPrefix("");
		dataSource.setJsonSuffix("");


		dataSource.setDataURL(url);
		dataSource.setFields(fields);

		/* finally set data source */
		return dataSource;
	}

	private static DataSource createRestGroupDS() {

		/* Request url*/
		String url = "/groups";

		/* Request fields */
		DataSourceField[] fields = {
				new DataSourceField("id", FieldType.INTEGER),
				new DataSourceField("name", FieldType.TEXT),
				new DataSourceField("description", FieldType.TEXT),
		};

		DataSource dataSource =
				new RestDataSource() {

					protected Object transformRequest(DSRequest dsRequest) {
						//dsRequest.setParams(getRequestParams());
						return super.transformRequest(dsRequest);
					}

					protected void transformResponse(DSResponse response, DSRequest request, Object data) {
						super.transformResponse(response, request, data);
					}
				};

		dataSource.setDataFormat(DSDataFormat.JSON);
		dataSource.setDataProtocol(DSProtocol.GETPARAMS);
		dataSource.setJsonPrefix("");
		dataSource.setJsonSuffix("");

		//set up FETCH to use POST requests
		OperationBinding fetch = new OperationBinding();
		fetch.setDataURL("/groups/fetch");
		fetch.setOperationType(DSOperationType.FETCH);
		fetch.setDataProtocol(DSProtocol.POSTPARAMS);
		dataSource.setOperationBindings(fetch);

		dataSource.setDataURL(url);
		dataSource.setFields(fields);

		/* finally set data source */
		return dataSource;
	}

	private static DataSource createRestLaneDS() {

		/* Request url*/
		String url = "/lanes";

		/* Request fields */
		DataSourceField id = new DataSourceField("id", FieldType.INTEGER);
		id.setPrimaryKey(true);
		DataSourceField parent = new DataSourceField("parent", FieldType.INTEGER);
		parent.setForeignKey("id");
		parent.setRootValue(0);


		DataSourceField[] fields = {
				id,
				parent,
				new DataSourceField("name", FieldType.TEXT),
				new DataSourceField("description", FieldType.TEXT),
				new DataSourceField("lane_order", FieldType.INTEGER),
				new DataSourceField("visible", FieldType.BOOLEAN),
				new DataSourceField("author", FieldType.TEXT),
				new DataSourceField("is_folder", FieldType.BOOLEAN),
				new DataSourceField("wuser", FieldType.INTEGER),
				new DataSourceField("wgroup", FieldType.INTEGER),
				new DataSourceField("ruser", FieldType.INTEGER),
				new DataSourceField("rgroup", FieldType.INTEGER)
		};

		DataSource dataSource =
				new RestDataSource() {

					protected Object transformRequest(DSRequest dsRequest) {
						//dsRequest.setParams(getRequestParams());
						return super.transformRequest(dsRequest);
					}

					protected void transformResponse(DSResponse response, DSRequest request, Object data) {
						super.transformResponse(response, request, data);
					}
				};
		dataSource.setID("lanes");
		dataSource.setDataFormat(DSDataFormat.JSON);
		dataSource.setDataProtocol(DSProtocol.GETPARAMS);
		dataSource.setJsonPrefix("");
		dataSource.setJsonSuffix("");


		dataSource.setDataURL(url);
		dataSource.setFields(fields);

		/* finally set data source */
		return dataSource;
	}

	private static DataSource createRestEventDS() {

		/* Request url*/
		String url = "/events";
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

		DataSourceField lane = new DataSourceField("lane", FieldType.TEXT, "Поток");
		lane.setForeignKey("lanes.name");
		lane.setForeignDisplayField("name");
        lane.setUseLocalDisplayFieldValue(false);

		DataSourceField priority = new DataSourceField("priority", FieldType.INTEGER, "Приоритет");
		LinkedHashMap valueMapPriority = new LinkedHashMap<Integer,String>();

		valueMapPriority.put(0,"Низкий приоритет");
		valueMapPriority.put(1,"Нормальный приоритет");
		valueMapPriority.put(2,"Высокий приоритет");
		priority.setValueMap(valueMapPriority);

		DataSourceField[] fields = {
				id,
				parent,
				new DataSourceField("prev", FieldType.INTEGER,"Предыдущая задача"),
				lane,
				new DataSourceField("name", FieldType.TEXT, "Название"),
				new DataSourceField("description", FieldType.TEXT,"Описание"),
				new DataSourceField("startDate", FieldType.DATETIME, "Дата начала"),
				new DataSourceField("endDate", FieldType.DATETIME, "Дата завершения"),
				eventWindowStyle,
				new DataSourceField("executor", FieldType.INTEGER, "Исполнитель"),
				priority,
				new DataSourceField("duration_d", FieldType.INTEGER, "Время исполнения (дней)"),
				new DataSourceField("duration_h", FieldType.INTEGER,"Время исполнения (часов)"),
				icon,
				state,
				new DataSourceField("executor_name", FieldType.TEXT, "Имя исполнителя"),
				new DataSourceField("author", FieldType.TEXT, "Задача создана"),
				is_backlog,
				new DataSourceField("spent_time", FieldType.INTEGER, "Время выполнения"),
    			is_graph,
				new DataSourceField("wuser", FieldType.INTEGER),
				new DataSourceField("wgroup", FieldType.INTEGER),
				new DataSourceField("ruser", FieldType.INTEGER),
				new DataSourceField("rgroup", FieldType.INTEGER)
		};

		DataSource dataSource =
				new RestDataSource() {
					protected Object transformRequest(DSRequest dsRequest) {
						//dsRequest.setParams(getRequestParams());
						return super.transformRequest(dsRequest);
					}

					protected void transformResponse(DSResponse response, DSRequest request, Object data) {
						super.transformResponse(response, request, data);
					}
				};
		dataSource.setID("events");
		dataSource.setDataFormat(DSDataFormat.JSON);
		dataSource.setDataProtocol(DSProtocol.GETPARAMS);
		dataSource.setJsonPrefix("");
		dataSource.setJsonSuffix("");

		//set up FETCH to use POST requests
		OperationBinding fetch = new OperationBinding();
		fetch.setDataURL("/events/fetch");
		fetch.setOperationType(DSOperationType.FETCH);
		fetch.setDataProtocol(DSProtocol.POSTPARAMS);

		//set up ADD to use POST requests
		OperationBinding add = new OperationBinding();
		add.setDataURL("/events/modify");
		add.setOperationType(DSOperationType.ADD);
		add.setDataProtocol(DSProtocol.POSTPARAMS);

		//set up UPDATE to use POST
		OperationBinding update = new OperationBinding();
		update.setOperationType(DSOperationType.UPDATE);
		update.setDataURL("/events/modify");
		update.setDataProtocol(DSProtocol.POSTPARAMS);
//		DSRequest updateProps = new DSRequest();
//		updateProps.setHttpMethod("PUT");
//		update.setRequestProperties(updateProps);

		//set up REMOVE to use DELETE
		OperationBinding remove = new OperationBinding();
		remove.setOperationType(DSOperationType.REMOVE);
		remove.setDataURL("/events/modify");
		remove.setDataProtocol(DSProtocol.POSTPARAMS);
//		DSRequest removeProps = new DSRequest();
//		removeProps.setHttpMethod("DELETE");
//		remove.setRequestProperties(removeProps);

		dataSource.setOperationBindings(fetch, add, update, remove);

		dataSource.setDataURL(url);
		dataSource.setFields(fields);

		/* finally set data source */
		return dataSource;
	}
}