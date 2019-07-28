package org.taurus.aya.client;
/**
 * Статический класс для управления вкладками главного окна 
 */

import com.smartgwt.client.widgets.tab.Tab;
import com.smartgwt.client.widgets.tab.TabSet;

import java.util.HashMap;

public class ViewManager {
	
	static HashMap<Integer, Tab> openDocuments = new HashMap<Integer, Tab>();
	static TabSet tabset = GlobalData.getTopTabSet();

	/* Добавление обработчика закрытия вкладки. Необходимо вызвать перед началом работы с обьъектом*/
	/*TODO Вообще, тут надо доработать...Существуют ещё скрипты, открытие и закрытие их вкладок вообще никак не отслеживается.
	 * Надо как-то расширить мапину openDocuments, чтобы могла и документы, и скрипты, и задачи...
	 * */
//	public static void init()
//	{
//		tabset.addCloseClickHandler(new CloseClickHandler(){
//
//			@Override
//			public void onCloseClick(TabCloseClickEvent event) {
//				for ( Integer id: openDocuments.keySet())
//				{
//					if (openDocuments.get(id) == event.getTab())
//						openDocuments.remove(id);
//					SC.logWarn("removed tab for docId " + id + ", remains " + openDocuments.size() + " elements");
//				}
//			}});
//	}
//	
//	public static void openDocumentView(Record docRecord)
//	{
//		Integer docId = docRecord.getAttributeAsInt("id");
//		
//		//Проверка - вдруг такой документ уже открыт?
//		if (openDocuments.get(docId) != null)
//		{
//			SC.logWarn("openDocumentView: open existing docunent view");
//			tabset.selectTab(openDocuments.get(docId));
//		}
//		else
//		{
//			SC.logWarn("openDocumentView: open new docunent view");
//			Tab tab = createTab(docRecord.getAttribute("name"), new DocView(docRecord,1), true);
//		    	    
//		    openDocuments.put(docId,tab);
//		    	    
//		    tabset.addTab(tab);
//		    tabset.selectTab(tab);
//
//		}
//	}
//	
//	public static void closeDocumentVIew(Record docRecord)
//	{
//		Integer docId = docRecord.getAttributeAsInt("id");
//		
//		if (openDocuments.get(docId) != null)
//		{
//			tabset.removeTab(openDocuments.get(docId));
//			openDocuments.remove(docId);
//			SC.logWarn("closeDocumentView: close view for id " + docId + " remains " + openDocuments.size() + " elements");
//		}
//	}
//	
//	public static void openScriptView(Record scriptRecord)
//	{
//		SC.logWarn("openScriptView - 1");
//		Tab tab = createTab("Script Editor", new ScriptView(scriptRecord), true);
//		SC.logWarn("openScriptView - 2");
//	    TabSet tabset = GlobalData.getTopTabSet();
//	    tabset.addTab(tab);
//	    tabset.selectTab(tab);
//	}
//	
//	private static Tab createTab(String title, Canvas pane, boolean closable) {
//	    Tab tab = new Tab(title);
//	    tab.setCanClose(closable);
//	    tab.setPane(pane);
//	    return tab;
//	}
}
