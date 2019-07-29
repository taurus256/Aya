package org.taurus.aya.client.dialogs;

import com.smartgwt.client.data.*;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.AnimationEffect;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.ImgButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.FileItem;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.form.fields.events.ChangedEvent;
import com.smartgwt.client.widgets.form.fields.events.ChangedHandler;
import com.smartgwt.client.widgets.grid.events.RecordDropEvent;
import com.smartgwt.client.widgets.grid.events.RecordDropHandler;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.layout.VLayout;
import com.smartgwt.client.widgets.tree.TreeGrid;
import org.taurus.aya.client.GlobalData;
import org.taurus.aya.client.TabManager.ResourceType;
import org.taurus.aya.client.generic.GenericPropertiesDialog;

import java.util.HashSet;

public class AddFileDialog extends GenericPropertiesDialog{
	
	Integer folderId = 0;
	Label label;
	DynamicForm df;
//	final String message = "Для загрузки нескольких файлов последовательно используйте кнопки \"Выбрать файл\" и \"Загрузить файл\".<br>Загрузка больших файлов (>5Мб) может занимать некоторое время.";
	final String message1 = "Загрузка больших файлов (>5Мб) может занимать некоторое время";	
	final String message2 = "Файл уже загружен. Вы можете изменять только теги и параметры доступа";
	Record[] cloneOfDroppedRecords;
	
	private HashSet<String> filetypes;
	
	public AddFileDialog(Record selected)
	{
		super(selected,"file.png",ResourceType.DOCUMENT,GlobalData.getDataSource_docs()," файла");
		
		
		setCanDragReposition(true);  
		setCanDragResize(false);
		
		if (record.getAttributeAsInt("id") == null)
			setTitle("Загрузка файла");
		else
			setTitle("Свойства файла");
		
		setBodyColor("rgb(253, 253, 253)");
//		setBodyStyle("s3_windowBody");
		setHoverMoveWithMouse(true);
		setAutoSize(true);
		setMinHeight(0);
		setAutoCenter(true);
		setAnimateShowEffect(AnimationEffect.FADE);
		

//		 Redetermine the tagListGrid behavior
//		 It have to save dropped data into array instead of saving it to database  
		hrRecordDrop.removeHandler();
		tagListGrid.addRecordDropHandler( new RecordDropHandler(){

					@Override
					public void onRecordDrop(RecordDropEvent event) {
						SC.logWarn("AddFileDialog: enter to addRecordDropHandler");
						DataSource ds = ((TreeGrid)(event.getSourceWidget())).getDataSource();
						
						Record[] cloneOfDroppedRecords = ds.copyRecords(event.getDropRecords());
						for (Record r:cloneOfDroppedRecords)
							{
								r.setAttribute("tag_id", r.getAttribute("id"));
								r.setAttribute("doc_id", record.getAttribute("id"));
								r.setAttribute("name", record.getAttributeAsString("name"));
								r.setAttribute("resource_type",getRecourceType().ordinal());
								r.setAttribute("image", getImageName());
								tagListGrid.addData(r);
							}
						SC.logWarn("AddFileDialog: data added from recordDrophandler");
						event.cancel();
					}}
				);

		//Update the Tag label
		if (record.getAttributeAsInt("id") != null) tagListGrid.fetchData(new Criteria(getColumnName(), record.getAttributeAsString("id")));
		
		// Function to adding available extensions to the set 
		addFileTypes();
	}
	
	@Override
	public void constructInterface()
	{
		this.addItem(createLabel());
		if (record.getAttributeAsInt("id") == null) this.addItem(createLoadForm()); // Load form is shown only for newly created record
		this.addItem(createTagsLayout());
		this.addItem(createSecurityLayout());
		this.addItem(createButtonsLayout());
	}
	
	@Override
	protected String getImageName()
	{
		if (filetypes==null) addFileTypes() ;
		String fname = record.getAttributeAsString("name");
		if (fname == null )
		{
			return "file.png";
		}

		// If file name present, try to extract extension from it
		int indexOfDotSign = record.getAttributeAsString("name").lastIndexOf(".");

		// If extension is present
		if ((indexOfDotSign != -1) && (record.getAttributeAsString("name").length() - indexOfDotSign < 5))
		{
			String extension = record.getAttributeAsString("name").substring(record.getAttributeAsString("name").lastIndexOf(".") + 1,record.getAttributeAsString("name").length()).toLowerCase();

			if (filetypes.contains(extension)) // if extension is present in the set of availible extensions
				return extension + ".png";
			else
				return "file.png";
		}
		else
		{
			return "file.png";
		}
	}
	
	// Add available extensions to the set 
	private void addFileTypes()
	{
		filetypes = new HashSet<String>();
		filetypes.add("3gp");
		filetypes.add("ai");
		filetypes.add("asp");
		filetypes.add("avi");
		filetypes.add("bmp");
		filetypes.add("c");
		filetypes.add("cpp");
		filetypes.add("css");
		filetypes.add("dat");
		filetypes.add("doc");
		filetypes.add("document");
		filetypes.add("docx");
		filetypes.add("dot");
		filetypes.add("dotx");
		filetypes.add("dwg");
		filetypes.add("dxf");
		filetypes.add("eps");
		filetypes.add("exe");
		filetypes.add("file");
		filetypes.add("gif");
		filetypes.add("h");
		filetypes.add("html");
		filetypes.add("iso");
		filetypes.add("java");
		filetypes.add("jpg");
		filetypes.add("mid");
		filetypes.add("mov");
		filetypes.add("mp3");
		filetypes.add("mp4");
		filetypes.add("mpg");
		filetypes.add("odp");
		filetypes.add("ods");
		filetypes.add("odt@2x");
		filetypes.add("pdf");
		filetypes.add("php");
		filetypes.add("pnd");
		filetypes.add("pps");
		filetypes.add("ppt");
		filetypes.add("psd");
		filetypes.add("py");
		filetypes.add("qt");
		filetypes.add("rar");
		filetypes.add("rtf");
		filetypes.add("script");
		filetypes.add("sql");
		filetypes.add("tag");
		filetypes.add("task");
		filetypes.add("tga");
		filetypes.add("tgz");
		filetypes.add("tiff");
		filetypes.add("txt");
		filetypes.add("wav");
		filetypes.add("xls");
		filetypes.add("xlsx");
		filetypes.add("xml");
		filetypes.add("zip");
	}
	
	Label createLabel()
	{
		label = new Label();
		label.setWidth100();
		label.setHeight(50);
		label.setMargin(10);
		if (record.getAttributeAsInt("id") == null)
			label.setContents(message1);
		else
			label.setContents(message2);
		return label;
	}
	
	//Форма используется для записи в таблицу document_content
	DynamicForm createLoadForm()
	{
		df = new DynamicForm();
		df.setHeight(50);
		df.setDataSource(GlobalData.getDataSource_content());
		
		TextItem documentId = new TextItem("document_id");
		documentId.setVisible(false);
		documentId.setValue("empty");

		FileItem fileItem = new FileItem("filecontent");
		fileItem.setMultiple(false);
		fileItem.addChangedHandler(new ChangedHandler(){

			@Override
			public void onChanged(ChangedEvent event) {
				
				//Adding necessary fields to record to get ability for writing it into documents table
				
				SC.logWarn("AddFileDialog: submitButton: onclick: " + df.getField("filecontent").getDisplayValue());
				String path = df.getField("filecontent").getDisplayValue();
				String fname = path;
				if (path.contains("\\"))
				{
					String [] fname_parts = df.getField("filecontent").getDisplayValue().split("\\\\");
					fname = fname_parts[fname_parts.length-1];
				}
				if (path.contains("/"))
				{
					String [] fname_parts = df.getField("filecontent").getDisplayValue().split("/");
					fname = fname_parts[fname_parts.length-1];
				}
				record.setAttribute("name",fname);
				record.setAttribute("type",2);
				record.setAttribute("icon", "tree/file.png"); // it is image for "document" table
			}});
		
		df.setFields(documentId, fileItem);
		df.setMargin(10);
		
		return df;
	}
	
	protected HLayout createTagsLayout()
	{
		final HLayout hLayoutMain = new HLayout();
		hLayoutMain.setWidth100();
		hLayoutMain.setHeight(50);
		hLayoutMain.setMembersMargin(5);
		
		VLayout vlayoutContent =  new VLayout();
		vlayoutContent.setHeight100();
		
		final HLayout hlayoutLabel = new HLayout();
		hlayoutLabel.setMembersMargin(5);
		labelTags = new Label();
		labelTags.setIcon("forms/tag.png");
		labelTags.setIconSize(32);
		labelTags.setHeight(50);
		labelTags.setWidth100();
		labelTags.setMargin(10);
		labelTags.setContents("Тегов нет");
		hlayoutLabel.addMember(labelTags);
		
		vlayoutContent.setWidth100();
		vlayoutContent.addMember(hlayoutLabel);
		final VLayout vLayoutControls = createTagControls();
		vLayoutControls.setVisible(false);
		vlayoutContent.addMember(vLayoutControls);
		
		hLayoutMain.addMember(vlayoutContent);
		
		HLayout hLayoutButton = new HLayout();
		hLayoutButton.setWidth(50);
		hLayoutButton.setHeight100();
		hLayoutButton.setAlign(Alignment.CENTER);
		
		VLayout vLayoutButton =  new VLayout();
		vLayoutButton.setWidth("25px");
		vLayoutButton.setHeight100();
		vLayoutButton.setAlign(Alignment.CENTER);
				
		final ImgButton button = new ImgButton(){{
			setAlign(Alignment.CENTER);
			setSize("25px", "25px");
			setSrc("forms/down.png");
			setShowRollOver(false);
			setShowDown(false);
		}};
		button.addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				if (hlayoutLabel.isVisible())
				{
					// Now we create new record or update existing 
					if (record.getAttributeAsInt("id") == null ) // Here we need to create record and obtain its ID from database
					{
						
						// Test for selecting file
						if (df.getField("filecontent").getValue()==null) {
							SC.warn("Файл должен быть выбран");
							return;	
						}
						
						SC.logWarn("AddFileDialog: record id IS NULL... Create new record");
						
						for (FormItem f : df.getFields())
						{
							record.setAttribute(f.getName(), f.getValue());
							SC.logWarn("AddFileDialog. Save data " + f.getName() + "=" + f.getValue());
						}
						
						dataSource.addData(record, new DSCallback(){

							@Override
							public void execute(DSResponse dsResponse, Object data,
                                                DSRequest dsRequest) {
								if (dsResponse.getDataAsRecordList().getLength()>0)
								{
									implicityCreated = true;
									record = dsResponse.getDataAsRecordList().get(0);
									SC.logWarn("AddFileDialog: new record implicitly added to DB, id = " + record.getAttributeAsString("id"));
									
									tagListGrid.setCriteria(new Criteria(getColumnName(), record.getAttributeAsString("id")));
									hlayoutLabel.hide();
									vLayoutControls.show();
									button.setSrc("forms/up.png");
									hLayoutMain.setHeight(130);
								}
								else
									SC.logWarn("AddFileDialog: Could not implicitly create record");
							}});
					}
					else
					{
						SC.logWarn("AddFileDialog: Record is already created");
						tagListGrid.setCriteria(new Criteria(getColumnName(), record.getAttributeAsString("id")));
						
						hlayoutLabel.hide();
						vLayoutControls.show();
						button.setSrc("forms/up.png");
						hLayoutMain.setHeight(130);
					}
				}
				else
				{
					hlayoutLabel.show();
					vLayoutControls.hide();
					button.setSrc("forms/down.png");
					hLayoutMain.setHeight(50);
					updateTagLabel();
				}
			}});
		vLayoutButton.addMember(button);
		hLayoutButton.addMember(vLayoutButton);
		hLayoutMain.addMember(hLayoutButton);
		
		return hLayoutMain;
	}

	
	protected VLayout createButtonsLayout()
	{
		VLayout vlayout = new VLayout();
		vlayout.setAlign(VerticalAlignment.CENTER);
		vlayout.setWidth100();
		vlayout.setHeight(50);
		vlayout.setMargin(10);

		HLayout hlayout = new HLayout();
		hlayout.setAlign(Alignment.RIGHT);
		hlayout.setWidth(400);
		hlayout.setMembersMargin(10);
		hlayout.setHeight(25);
		
		final IButton submitButton = new IButton("Сохранить");
		submitButton.addClickHandler(new ClickHandler()
		{
			@Override
			public void onClick(ClickEvent event)
			{
				// Создание записи в таблице document
				if (record.getAttribute("id") == null)
				{
					label.setContents("Загрузка...");
					GlobalData.getStatusBar().indicateProcess("Идет загрузка файла...");
					submitButton.disable();

					GlobalData.getDataSource_docs().addData(record,new DSCallback()
					{
	
						@Override
						public void execute(DSResponse dsResponse, Object data, DSRequest dsRequest)
						{
							// Create record in "content" table (this table is associated with DynamicForm df)
							SC.logWarn("AddFileDialog file saved in hierarchy. id is " + dsResponse.getDataAsRecordList().get(0).getAttributeAsString("id"));
							df.getField("document_id").setValue(dsResponse.getDataAsRecordList().get(0).getAttributeAsInt("id"));
							df.saveData(new DSCallback()
							{
	
								@Override
								public void execute(DSResponse dsResponse, Object data, DSRequest dsRequest)
								{
									label.setContents(message1);
									GlobalData.getStatusBar().indicateMessage("Файл загружен на сервер");
									hide();
								}
							});
						}
					});
				}
				else
					GlobalData.getDataSource_docs().updateData(record,new DSCallback()
					{
						@Override
						public void execute(DSResponse dsResponse, Object data, DSRequest dsRequest)
						{
							// Now we has updated document data. Should we save file content?
							if (implicityCreated)
							{
								// Yes. Document content saved
								df.getField("document_id").setValue(dsResponse.getDataAsRecordList().get(0).getAttributeAsInt("id"));
								df.saveData(new DSCallback()
								{
		
									@Override
									public void execute(DSResponse dsResponse, Object data, DSRequest dsRequest)
									{
										label.setContents(message1);
										GlobalData.getStatusBar().indicateMessage("Файл загружен на сервер");
										hide();
									}
								});
							}
							else
							{
								// Simply show success message
								SC.logWarn("AddFileDialog: file data updated");
								GlobalData.getStatusBar().indicateMessage("Данные обновлены");
								hide();
							}
						}
					});
			}
		});
		submitButton.setWidth("150px");
		
		final IButton cancelButton = new IButton("Отменить");
		cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {

				if (implicityCreated)
				{
					dataSource.removeData(record, new DSCallback(){

						@Override
						public void execute(DSResponse dsResponse, Object data,
                                            DSRequest dsRequest) {
							SC.logWarn("GenericPropertiesDialog: remove implicitly created record");
							
						}});
				}
				hide();
			}
		});
		
		hlayout.addMember(submitButton);
		hlayout.addMember(cancelButton);
		
		vlayout.addMember(hlayout);
		return vlayout;
	}
}
