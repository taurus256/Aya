package org.taurus.aya.client;

import com.google.gwt.user.client.Cookies;
import com.smartgwt.client.data.*;
import com.smartgwt.client.util.BooleanCallback;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;
import com.smartgwt.client.widgets.tab.events.CloseClickHandler;
import com.smartgwt.client.widgets.tab.events.TabCloseClickEvent;
import com.smartgwt.client.widgets.tab.events.TabSelectedEvent;
import com.smartgwt.client.widgets.tab.events.TabSelectedHandler;

import java.util.*;

/*Этот класс отвечает за управление вкладками и загрузку содержимого во фреймы внутри них, 
 * если вкладки содержат фреймы.
 * Методы класса - статические, для различения вкладок (и их фреймов, если они есть) 
 * используутся уникальные идентификаторы tabUID
 * 
 * Сейчас реализована разная логика для вкладок, предназначенныхдля редактирования одного ресурса 
 * (документа или скрипта) и вкладки диаграммы задач, которая к конкретному ресурсу не привязана.
 * Вкладки для работы с конкретным ресурсом создаются вызовос openTab, закрываются по removeTab.
 * Если содержат фреймы - используют loadXContent/saveXContent, где X - тип ресурса.
 * */

//TODO Разобраться со статическими методами панелей. Им место в коде классов панелей
// Правда, с экспотртом тогда не совсем понятно. Не несколько же раз их экспортировтаь при создании каждой новой панельки?
public class TabManager {
	
	static TabSet tabset = GlobalData.getTopTabSet();
	
	// Возможные типы документов
	public static enum ResourceType {TASK, LANE, DOCUMENT, SCRIPT, TAG,  CHAT, GRAPH };
	
	// Ссылка (тип документа, Id документа) -> tabUID 
	static Map <ResourceType, Map<Integer, Integer>> resourceMap = new HashMap<ResourceType,Map<Integer, Integer>>();

	// Ссылка tabUID -> Tab
	static Map <Integer, Tab> tabMap = new HashMap<Integer, Tab>();
	
	// Обратная ссылка tabUID -> Record 
	static Map <Integer, Record> backReferenceMap = new HashMap<Integer, Record>();
	
	// Множество вкладок (точнее - tabUID), содержимое которых было изменено
	static Set<Integer> changedTabsSet = new HashSet<Integer>();
	
	private static ArrayList<Integer> tabUIDs = new ArrayList<Integer>();
	
	// Ссылка на текущую выбранную вкладку
	private static ContentPane selectedPane = null;
	
	public static void init()
	{
		for (ResourceType k: ResourceType.values())
		{
			resourceMap.put(k, new HashMap<Integer,Integer>());
		}
	
		GlobalData.getTopTabSet().addCloseClickHandler(new CloseClickHandler()
		{

				@Override
				public void onCloseClick(final TabCloseClickEvent event)
				{
					final ContentPane pane = (ContentPane) event.getTab().getPane();
					Integer tabUID = resourceMap.get(pane.getResourceType()).get(pane.getResource().getAttributeAsInt("id"));

					if (changedTabsSet.contains(tabUID))
					{
						SC.ask("Закрытие вкладки", "Данные не сохранены. Всё равно закрыть?", new BooleanCallback(){

							@Override
							public void execute(Boolean value) {
								if (value)
									removeTab(getTab(pane.getResourceType(),pane.getResource().getAttributeAsInt("id")),pane.getResourceType(), pane.getResource());
							}});
						event.cancel();
					}
					else
					{
						removeTab(getTab(pane.getResourceType(),pane.getResource().getAttributeAsInt("id")),pane.getResourceType(), pane.getResource());
					}
				}
		});
		
		GlobalData.getTopTabSet().addTabSelectedHandler(new TabSelectedHandler()
		{
				@Override
				public void onTabSelected(TabSelectedEvent event)
				{
					selectedPane = (ContentPane) event.getTab().getPane();
					GlobalData.getApplicationMenu().updateMenu(selectedPane.getResourceType(), selectedPane.getResource());
					selectedPane.deferredUpdate();
				}
		});
		exportMethodsToJS();
		
		final  String s5ResourceType = Cookies.getCookie("s5_resource_type");
		final  String s5ResourceId = Cookies.getCookie("s5_resource_id");
		for ( String name: Cookies.getCookieNames())
			SC.logWarn(name);
		
		if (s5ResourceType != null && s5ResourceId != null)
		{
			SC.logWarn("Initialization. TabManager found cookie for opening link");
			SC.logWarn("Initialization. ResourceType = " + s5ResourceType + ", id = " + s5ResourceId);
			try
			{
				openResource(ResourceType.valueOf(s5ResourceType), Integer.valueOf(s5ResourceId));
				Cookies.removeCookie("s5_resource_type");
				Cookies.removeCookie("s5_resource_id");
			}
			catch (Exception ex)
			{
				SC.logWarn("Ошибка при чтении cookie с параметрами ссылки " + ex.getMessage());
			}
		}
		Record r = new Record();
		r.setAttribute("id",0);
		openTab(ResourceType.GRAPH,r);
	}
	
	public static Tab getTab(ResourceType resType, int id)
	{
		Integer tabUID = resourceMap.get(resType).get(id);
		if (tabUID != null)
		{
			SC.logWarn("TabManager: found tab for resType " + resType.ordinal() + " and id=" + id);
			return tabMap.get(tabUID);
		}
		else
		{
			SC.logWarn("TabManager: cannot find tab with resType " + resType.ordinal() + " and id=" + id);
			return null;
		}
	}
	
	/**
	 * Обновить все вкладки с ресурсами данного типа. 
	 * Для активной вкладки обновление будет выполнено немедленно, для остальных - установлен флаг отложенного обновления (DeferredUpdateFlag)
	 * @param @see ResourceType resType тип ресурса, для которого нужно обновить все вкладки (например, TASK  - для всех задач)
	 * */
	public static void updateAllOpen(ResourceType resType, Boolean updateAllContent)
	{
		for (Tab tab:GlobalData.getTopTabSet().getTabs())
		{
			ContentPane cp = ((ContentPane)tab.getPane());
			if ( cp.getResourceType() == resType)
				if (tabset.getSelectedTab() == tab)
					cp.updateContent(updateAllContent);
				else
					cp.setDeferredUpdateFlag();
		}
	}
	

	private static int generateTabUID()
	{
        Integer id;
        boolean found = true;
        
        do
        {
            id = (int) (Math.round(Math.random() * 1000));
            found = false;
            for (Integer k: tabUIDs)
                if (k.equals(id))
                {
                    found = true;
                    break;
                }
        } while (found);
        
        tabUIDs.add(id);
        SC.logWarn("TabManager. Generated Id = " + id);
        return id;
    }
	
	public native static int getFrameNumber(int tabUID)
	/*-{
		console.log("TabManager. Search for frame... ");
        var k = window.parent.frames.length;
        console.log("TabManager. Search for frame... k=" + k);
        
        for (var i=0; i<k; i++)
        {
            try{
                console.log("TabManager. Test " + window.parent.frames[i].location.href);
                if (window.parent.frames[i].location.search != null && window.parent.frames[i].location.search.search("tabuid="+tabUID)>0)
                {
                    console.log("TabManager. Found frame number is: " + i);
                    return i;
                }
            } catch (e) {
                console.log("TabManager. Cannot read property 'search' for frame " + i);
            }
        }
        console.log("TabManager. Frame with id " + tabUID + " is not found.");
        return -1;
	}-*/;
	
	public static void openTab(ResourceType type, Record r)
	{
		SC.logWarn("TabManager.\t OpenTab called ");
		
		int tabUID = generateTabUID();
		
		if ( r == null || resourceMap.get(type).get(r.getAttributeAsInt("id")) == null)
		{
			// добавляем вкладку
			Tab tab = null;
			if (type== ResourceType.TASK)
			{
				tab = createTab(r.getAttribute("name"), "task.svg", new TaskView(r,tabUID), true);
				SC.logWarn("TabManager.\t Adding tab for task " + r.getAttribute("name"));
			}
			
			if (type== ResourceType.GRAPH)
			{
				tab = createTab("График задач", "task.svg", new TaskView(r,tabUID), true);
				SC.logWarn("TabManager.\t Adding graph tab for task graph");
			}
			
			if (type== ResourceType.DOCUMENT)
			{
				if (r.getAttributeAsInt("type") == 1)
				{
					tab = createTab(r.getAttribute("name"), "document.svg", new DocView(r,tabUID), true);
					SC.logWarn("TabManager.\t Adding tab for document " + r.getAttribute("name"));
				}
				else if (r.getAttributeAsInt("type") == 2)
				{
					Criteria crit = new Criteria();
					crit.addCriteria("document_id", r.getAttributeAsInt("id"));
					GlobalData.getDataSource_content().fetchData(crit, new DSCallback(){

						@Override
						public void execute(DSResponse dsResponse, Object data, DSRequest dsRequest)
						{
							if (dsResponse.getData().length == 1)	
								GlobalData.getDataSource_content().downloadFile(dsResponse.getData()[0]);
							else
								SC.warn("При попытке скачать файл обнаружено записей:" + dsResponse.getData().length +". Нарушение ссылочной целостности БД!");
						}
					});
				}
				
			}

			if (type== ResourceType.SCRIPT)
			{
				tab = createTab(r.getAttribute("name"), "script.svg",  new ScriptView(r,tabUID), true);
				SC.logWarn("TabManager.\t Adding tab for script " + r.getAttribute("name"));
			}
			
			if (type== ResourceType.TAG)
			{
				tab = createTab(r.getAttribute("name"), "tag.svg",  new TagView(r), true);
				SC.logWarn("TabManager.\t Adding tab for tag " + r.getAttribute("name"));
			}

			if (type== ResourceType.CHAT)
			{
				tab = createTab("[ " + r.getAttributeAsString("showedName") + " ]", "chat.svg",  new ChatView(r), true);
				SC.logWarn("TabManager.\t Adding tab for dialog " + r.getAttribute("id"));
			}
			
		    tabset.addTab(tab);
		    tabset.selectTab(tab);
		    
		    resourceMap.get(type).put(r.getAttributeAsInt("id"), tabUID);
		    tabMap.put(tabUID, tab);
		    backReferenceMap.put(tabUID, r);
		}
		else
		{
			tabUID = resourceMap.get(type).get(r.getAttributeAsInt("id"));
			tabset.selectTab(tabMap.get(tabUID));
			SC.logWarn("TabManager.\t Selected tab for document " + r.getAttributeAsInt("name"));
		}
	}
	
	public static void removeTab(Tab tab, ResourceType type, Record r)
	{
		SC.logWarn("TabManager.\t Test: can i remove tab for " + r.getAttributeAsInt("name") + "?");
		if (resourceMap.get(type).get(r.getAttributeAsInt("id")) != null)
		{
			SC.logWarn("TabManager.\t Yes, this tab is open. Removing...");
			
			int tabUID = resourceMap.get(type).get(r.getAttributeAsInt("id")); 
			
//			// remove tab
//			tabset.removeTab(
//						tabMap.get(tabUID)
//						);
			
			// remove data in associative arrays
			tabMap.remove(tabUID);
			resourceMap.get(type).remove(r.getAttributeAsInt("id"));
			backReferenceMap.remove(tabUID);
			changedTabsSet.remove(tabUID);
			tabset.removeTab(tab);
		}
	}
	
	public static void loadContent(String typeStr, int tabUID_)
	{
		final int tabUID = tabUID_;
		SC.logWarn("TabManager.\t loadContent called, docId=" + backReferenceMap.get(tabUID).getAttributeAsString("id"));
		final int docId = backReferenceMap.get(tabUID).getAttributeAsInt("id");

		switch (ResourceType.valueOf(typeStr))
		{
		case DOCUMENT:
			{
				
				Criteria criteria = new Criteria();
				criteria.addCriteria("document_id", docId);
				GlobalData.getDataSource_content().fetchData(criteria,new DSCallback(){

					@Override
					public void execute(DSResponse dsResponse, Object data, DSRequest dsRequest) {
						if (dsResponse.getData().length == 0)
						{ 
							SC.logWarn("TabManager.\t loading new-generated content document, docId=" + docId + " tabUID=" + tabUID);
							// Записи для данного документа ещё нет, задаем её id = -1
							loadDocContentToEditor(docId, "Этот документ создан " + (new Date()).toString(), -1, getFrameNumber(tabUID));
						}
						else
						{
							SC.logWarn("TabManager.\t loading existing content into document, docId=" + docId + " tabUID=" + tabUID);
							// Запись найдена, извлекаем значения полей
							String content = dsResponse.getData()[0].getAttribute("content");
							Integer contentId =  dsResponse.getData()[0].getAttributeAsInt("id");
							SC.logWarn("TabManager.\t contentId=" + contentId);
							loadDocContentToEditor(docId, content, contentId, getFrameNumber(tabUID));
						}
						GlobalData.getStatusBar().stopIndicateProcess();
					}});
			};break;
		default : SC.warn("TabManager.\t Cannot recognize type " + typeStr);
		}
	}
	
	public static void saveDocContent(int docId, String content, int contentId)
	{
		Record r = new Record();
		r.setAttribute("document_id",docId);
		r.setAttribute("content",content);
		
		if (contentId < 0)
		{
			//Создаем запись в таблице content заново
			GlobalData.getDataSource_content().addData(r, new DSCallback(){

				@Override
				public void execute(DSResponse dsResponse, Object data,
                                    DSRequest dsRequest) {
					GlobalData.getStatusBar().indicateMessage("Документ создан");
				}});
		}
		else
		{	
			//Изменяем существующую запись
			
			SC.logWarn("DocView:saveContent contentId " + contentId);
			r.setAttribute("id",contentId);
			SC.logWarn("DocView:saveContent r.id = " + r.getAttribute("id"));
			
			GlobalData.getDataSource_content().updateData(r,new DSCallback(){
	
				@Override
				public void execute(DSResponse dsResponse, Object data,
                                    DSRequest dsRequest) {
					GlobalData.getStatusBar().indicateMessage("Документ обновлен");
				}
			});
		}
	}

	public static void loadScriptContent(int tabUID_)
	{
		final int tabUID = tabUID_;
		final Integer scriptId = ((ContentPane)(tabMap.get(tabUID).getPane())).getResource().getAttributeAsInt("id");
		if (scriptId == null) {
            SC.logWarn("TabManager\n Cannot get scriptId"); return;}
		
		Criteria criteria = new Criteria();
		criteria.addCriteria("script_id", scriptId);

		GlobalData.getDataSource_script_content().fetchData(criteria,new DSCallback(){

			@Override
			public void execute(DSResponse dsResponse, Object data,
                                DSRequest dsRequest)
			{
				if (dsResponse.getData().length == 0)
				{ 
					SC.logWarn("ScriptView. Set text for empty script");
					// Записи для данного документа ещё нет, задаем её id = -1
					String content = "//Скрипт создан " + (new Date()).toString() + ", пользователем " + GlobalData.getCurrentUser().getAttributeAsString("firstname") + " " + GlobalData.getCurrentUser().getAttributeAsString("surname");
					loadScriptContentToEditor(scriptId, content, -1, getFrameNumber(tabUID));
				}
				else
				{
					SC.logWarn("ScriptView. Set text for script with id " + scriptId);
					// Запись найдена, извлекаем значения полей
					String content = dsResponse.getData()[0].getAttribute("content");
					final Integer contentId =  dsResponse.getData()[0].getAttributeAsInt("id");
					loadScriptContentToEditor(scriptId, content, contentId, getFrameNumber(tabUID));
				}
				GlobalData.getStatusBar().stopIndicateProcess();
			}
		});
	}

	public static void saveScriptContent(int scriptId, String content, int contentId)
	{
		Record r = new Record();
		r.setAttribute("script_id",scriptId);
		r.setAttribute("content",content);
		
		if (contentId < 0)
		{
			//Создаем запись в таблице content заново
			GlobalData.getDataSource_script_content().addData(r, new DSCallback(){

				@Override
				public void execute(DSResponse dsResponse, Object data,
                                    DSRequest dsRequest) {
					GlobalData.getStatusBar().indicateMessage("Скрипт сохранен");
				}});
		}
		else
		{	
			//Изменяем существующую запись
			
			SC.logWarn("TabManager\t saveScriptContent contentId " + contentId);
			r.setAttribute("id",contentId);
			SC.logWarn("TabManager\t saveScriptContent r.id = " + r.getAttribute("id"));
			
			GlobalData.getDataSource_script_content().updateData(r,new DSCallback(){
				@Override
				public void execute(DSResponse dsResponse, Object data,
                                    DSRequest dsRequest) {
					GlobalData.getStatusBar().indicateMessage("Скрипт обновлен");
				}
			});
		}
	}
	
	private static Tab createTab(String title, Canvas pane, boolean closable) {
	    Tab tab = new Tab(title);
	    tab.setCanClose(closable);
	    tab.setPane(pane);
	    tab.setPaneMargin(0);
	    return tab;
	}
	
	private static Tab createTab(final String title, final String icon, Canvas pane, boolean closable) {
	    final Tab tab = new Tab();
	    tab.setTitle("<span>" + Canvas.imgHTML("header/" + icon, 20, 20) + " " + title + "</span>");
	    tab.setCanClose(closable);
	    tab.setPane(pane);
	    tab.setAttribute("caption", title);
	    tab.addTabSelectedHandler(new TabSelectedHandler(){

			@Override
			public void onTabSelected(TabSelectedEvent event) {
				tab.setTitle("<span>" + Canvas.imgHTML("header/" + icon, 20, 20) + " " + title + "</span>");
			}});
	    
	    return tab;
	}
	
	/* Создание ссылок на методы Java для доступа из Javascript-кода */
	public native static void exportMethodsToJS() 
	/*-{
    	$wnd.loadContent = $entry(@org.taurus.aya.client.TabManager::loadContent(Ljava/lang/String;I));
    	$wnd.saveContent = $entry(@org.taurus.aya.client.TabManager::saveDocContent(ILjava/lang/String;I));
    	$wnd.loadScriptContent = $entry(@org.taurus.aya.client.TabManager::loadScriptContent(I));
    	$wnd.saveScriptContent = $entry(@org.taurus.aya.client.TabManager::saveScriptContent(ILjava/lang/String;I));
    	$wnd.execCommand = $entry(@org.taurus.aya.client.CommandExecutor::jsExec(Ljava/lang/String;Ljava/lang/String;I));
    	$wnd.contentChanged = $entry(@org.taurus.aya.client.TabManager::contentChanged(I));
    	$wnd.contentSaved = $entry(@org.taurus.aya.client.TabManager::contentSaved(I));
	}-*/;
	
	public native static void loadDocContentToEditor(int id, String content, int contentId, int frameNumber)
	/*-{
		console.log("loading data to frame number" + frameNumber);
		//загрузка данных, приехавших с сервера
		window.top.frames[frameNumber].CKEDITOR.instances.editor1.setData(content);
		//сохранение в переменной ID документа
		window.top.frames[frameNumber].DOC_ID = id;
		window.top.frames[frameNumber].CONTENT_ID = contentId;
		console.log("DOC_ID=" + window.top.frames[frameNumber].DOC_ID + ", CONTENT_ID=" + window.top.frames[frameNumber].CONTENT_ID);
		console.log("loading data OK");
	}-*/;
	
	public native static void loadScriptContentToEditor(int id, String content, int contentId, int frameNumber)
	/*-{
		console.log("loading script content to frame number" + frameNumber);
		//загрузка данных, приехавших с сервера
		window.top.frames[frameNumber].editor.setValue(content);
		//сохранение в переменной ID документа
		window.top.frames[frameNumber].SCRIPT_ID = id;
		window.top.frames[frameNumber].CONTENT_ID = contentId;
		console.log("SCRIPT_ID=" + window.top.frames[frameNumber].SCRIPT_ID + ", CONTENT_ID=" + window.top.frames[frameNumber].CONTENT_ID);
		console.log("loading data OK");
	}-*/;

	/* Загружает указанный ресурс. Для этого сначала читает описание ресурса из БД и проверяет налилие у пользователя прав доступа к нему*/
	/* Допустимые типы ресурсов - задача, документ, скрипт, тег.*/
	public static void openResource(final ResourceType type, int id)
	{
		DataSource ds;
		final String absentDescription;
		switch (type)
		{
			case TASK: { ds = GlobalData.getDataSource_events(); absentDescription ="Система не может найти требуемую задачу. Возможно, она была удалена";  break;}
			case DOCUMENT: { ds = GlobalData.getDataSource_docs(); absentDescription ="Система не может найти требуемый документ. Возможно, он был удален";  break;}
			case SCRIPT: { ds = GlobalData.getDataSource_scripts(); absentDescription ="Система н может найти требуемый скрипт Возможно, он был удален";  break;}
			case TAG: { ds = GlobalData.getDataSource_tags(); absentDescription ="Система не может открыть требуемый тег. Возможно, он был удален";  break;}
			default: {
                SC.warn("Попытка открыть ресурс недопустимого типа"); return;}
		}
		
		Criteria crit = new Criteria();
		crit.addCriteria("id", id);
		ds.fetchData(crit, new DSCallback(){

			@Override
			public void execute(DSResponse dsResponse, Object data,
                                DSRequest dsRequest) {
				if (dsResponse.getData().length == 0)
				{
					SC.warn("Загрузка ресурса",absentDescription);
				}
				else 
					if (dsResponse.getData().length > 1)
					{
						SC.warn("Загрузка ресурса","Обнаружено более одного ресурса с одинаковым ID. Нарушение целостности БД!");
					}
					else
					{
						Record r = dsResponse.getData()[0];
						if (GlobalData.canRead(r))
							TabManager.openTab(type, r);
						else
							SC.warn("Загрузка ресурса","У вас недостаточно прав для открытия данного ресурса");
					}
			}});
	}

	public static void contentChanged(int tabUID)
	{
		SC.logWarn("TabManager: contentChanged for tab " + tabUID);
		changedTabsSet.add(tabUID);
	}
	
	public static void contentSaved(int tabUID)
	{
		SC.logWarn("TabManager: contentSaved for tab " + tabUID);
		changedTabsSet.remove(tabUID);
	}
	
	public static ContentPane getSelectedPane() {
		return selectedPane;
	}

}
