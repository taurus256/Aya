package org.taurus.aya.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.ContentsType;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.HTMLPane;
import org.taurus.aya.client.TabManager.ResourceType;
import org.taurus.aya.client.widgets.CustomHTMLPane;

public class DocView extends ContentPane {
	
	HTMLPane htmlPane = null;
	int docId;
	/*TODO Статическпий ИД документа - плохо. Нужно "опускать" его во фрейм. Проверить логику работы loadDocument*/
	int tabUID;
	Record rec;
	
	// На вход конструктора поступает Record типа документ
	public DocView(Record rec, int tabUID)
	{
		SC.logWarn("DocView\t" + String.valueOf(tabUID));
		this.rec = rec;
		this.tabUID = tabUID;
		docId = rec.getAttributeAsInt("id");

		this.addMember(createEditor());
		this.setPadding(0);
		this.setHeight("100%");


	}

	HTMLPane createEditor()
	{
		htmlPane  = new CustomHTMLPane();
		htmlPane.setContentsType(ContentsType.PAGE);
		htmlPane.setContentsURL("R4/r4.html?tabuid=" + String.valueOf(tabUID));
		htmlPane.setWidth("100%");
		htmlPane.setHeight("100%");

		return htmlPane;
	}
	
	public static void loadContent()
	{
//		SC.logWarn("loadContent called");
//		
//		Criteria criteria = new Criteria();
//		criteria.addCriteria("document_id", docId);
//		GlobalData.getDataSource_content().fetchData(criteria,new DSCallback(){
//
//			@Override
//			public void execute(DSResponse dsResponse, Object data,
//					DSRequest dsRequest) {
//				/*SC.logWarn("loadContent: data loaded");
//				loadedData = new Record(convertData((JavaScriptObject)data));
//				SC.logWarn("loadContent: data type is " + data.getClass().toString());
//								
//				loadContentToEditor((JavaScriptObject)data);*/
//				if (dsResponse.getData().length == 0)
//				{ 
//					// Записи для данного документа ещё нет, задаем её id = -1
//					loadContentToEditor(docId, "Этот документ создан " + (new Date()).toString(), -1);
//				}
//				else
//				{
//					// Запись найдена, извлекаем значения полей
//					String content = dsResponse.getData()[0].getAttribute("content");
//					Integer contentId =  dsResponse.getData()[0].getAttributeAsInt("id");
//					loadContentToEditor(docId, content, contentId);
//				}
//			}});
		//TabManager.loadContent(TabManager.ResourceType.DOCUMENT, tabUID);
	}
	
//	public static void saveContent(int docId, String content, int contentId)
//	{
//		Record r = new Record();
//		r.setAttribute("document_id",docId);
//		r.setAttribute("content",content);
//		
//		if (contentId < 0)
//		{
//			//Создаем запись в таблице content заново
//			GlobalData.getDataSource_content().addData(r, new DSCallback(){
//
//				@Override
//				public void execute(DSResponse dsResponse, Object data,
//						DSRequest dsRequest) {
//					GlobalData.getStatusBar().indicateMessage("Документ сохранен");
//				}});
//		}
//		else
//		{	
//			//Изменяем существующую запись
//			
//			SC.logWarn("DocView:saveContent contentId " + contentId);
//			r.setAttribute("id",contentId);
//			SC.logWarn("DocView:saveContent r.id = " + r.getAttribute("id"));
//			
//			GlobalData.getDataSource_content().updateData(r,new DSCallback(){
//	
//				@Override
//				public void execute(DSResponse dsResponse, Object data,
//						DSRequest dsRequest) {
//					GlobalData.getStatusBar().indicateMessage("Документ обновлен");
//				}
//			});
//		}
//	}

	public static native JavaScriptObject convertData(JavaScriptObject data)
	/*-{
		return data[0];
	}-*/;

	
	public native static void loadContentToEditor(JavaScriptObject data)
	/*-{
		//загрузка данных, приехавших с сервера
		window.top.frames[window.top.frames.length -1].CKEDITOR.instances.editor1.setData((data[0]).content);
		//сохранение в переменной ID документа
		window.top.frames[window.top.frames.length -1].DOC_ID = (data[0]).id;
		console.log("loading data OK");
	}-*/;

	public native static void loadContentToEditor(int id, String content, int contentId)
	/*-{
		//загрузка данных, приехавших с сервера
		window.top.frames[window.top.frames.length -1].CKEDITOR.instances.editor1.setData(content);
		//сохранение в переменной ID документа
		window.top.frames[window.top.frames.length -1].DOC_ID = id;
		window.top.frames[window.top.frames.length -1].CONTENT_ID = contentId;
		console.log("DOC_ID=" + window.top.frames[window.top.frames.length -1].DOC_ID + ", CONTENT_ID=" + window.top.frames[window.top.frames.length -1].CONTENT_ID);
		console.log("loading data OK");
	}-*/;

	public Record getResource()
	{
		return rec;
	}


	public ResourceType getResourceType() 
	{
		return TabManager.ResourceType.DOCUMENT;
	}
	
	public void updateContent(Boolean all)
	{
		TabManager.loadContent("DOCUMENT", tabUID);
	}
}

