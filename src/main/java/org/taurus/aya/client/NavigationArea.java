package org.taurus.aya.client;
/**
 * Боковая панель
 */
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.VisibilityMode;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.layout.SectionStack;
import com.smartgwt.client.widgets.layout.SectionStackSection;
import com.smartgwt.client.widgets.tree.TreeGrid;

public class NavigationArea extends com.smartgwt.client.widgets.layout.HLayout {
	
	private TaskPanel taskPanel;
	private DocPanel docPanel;
	private ScriptPanel scriptPanel;
	private TagPanel tagPanel;
	private ChatPanel chatPanel;

	TreeGrid taskTree;
	private int myWidth;
	private final SectionStack sectionStack;
	
    public NavigationArea() {
    	
		super();
	
	    this.setMembersMargin(20); 
	    this.setOverflow(Overflow.HIDDEN);
	    this.setShowResizeBar(true);
	    this.setWidth(250);
	    this.setMinWidth(250);
	    this.setAnimateMembers(false);
	    taskPanel = new TaskPanel();
	    docPanel = new DocPanel();
	    scriptPanel = new ScriptPanel();
	    tagPanel = new TagPanel();
	    chatPanel = new ChatPanel();

	     
	    sectionStack = new SectionStack();
	    sectionStack.setShowExpandControls(true);
	    sectionStack.setAnimateSections(false);
	    sectionStack.setVisibilityMode(VisibilityMode.MUTEX);
	    sectionStack.setOverflow(Overflow.HIDDEN);
	    sectionStack.setHeaderHeight(32);
	    sectionStack.setMargin(0);
	    sectionStack.setPadding(0);
	
		SectionStackSection section1 = new SectionStackSection("Задачи");
		//section1.setIcon("tree/note_open.png");
	    section1.setExpanded(true);
	    section1.addItem(taskPanel);
	
//		SectionStackSection section2 = new SectionStackSection("Документы");
//		//section2.setIcon("tree/document_open.png");
//	    section2.setExpanded(false);
//	    section2.addItem(docPanel);
//
//	    SectionStackSection section3 = new SectionStackSection("Скрипты");
//	    //section3.setIcon("tree/note_open.png");
//	    section3.setExpanded(false);
//	    section3.addItem(scriptPanel);
//
//	    SectionStackSection section4 = new SectionStackSection("Теги");
//	    //section3.setIcon("tree/note_open.png");
//	    section4.setExpanded(false);
//	    section4.addItem(tagPanel);
//
//	    SectionStackSection section5 = new SectionStackSection("Сообщения");
//	    //section3.setIcon("tree/note_open.png");
//	    section5.setName("messages");
//	    section5.setExpanded(false);
//	    section5.addItem(chatPanel);
	    
	    sectionStack.addSection(section1);
//	    sectionStack.addSection(section2);
//	    sectionStack.addSection(section3);
//	    sectionStack.addSection(section4);
//	    sectionStack.addSection(section5);
	
	    this.addMember(sectionStack);
	    
	    GlobalData.setNavigationArea(this);
	}

	public TaskPanel getTaskPanel() {
		return taskPanel;
	}

	public DocPanel getDocPanel() {
		return docPanel;
	}

	public ScriptPanel getScriptPanel() {
		return scriptPanel;
	}

	public TagPanel getTagPanel() {
		return tagPanel;
	}

	public ChatPanel getChatPanel(){
		return chatPanel;
	}

	public SectionStackSection getSection(String name)
	{
		int index = sectionStack.getSectionNumber(name);
		if (index >= 0)
			return sectionStack.getSection(index);
		else 
		{
			SC.logWarn("Cannot find section named " + name + "!");
			return null;
		}
	}
	
	public void toggle()
	{
		if (this.getWidth() != 0)
		{
			myWidth = this.getWidth();
			this.setWidth(0);
		}
		else
			this.setWidth(myWidth);
	}

}
