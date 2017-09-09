package view;

import java.util.ArrayList;
import java.util.List;
import model.FolderNode;
import model.MyFile;
import service.FileSystem;
import view.FileDirectoryTreeGraph.MyTreeItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * 此类为文件目录树视图
 *
 */

public class FileDirectoryTreeGraph extends TreeView<String> {

	private  static MyTreeItem selectedItem;	//当前选中的结�?
	//图标地址
	private static String emptyFolderImageSrc = "/image/emptyFolder.png";
	private static String folderImageSrc = "/image/folder.png";
	private static String fileImageSrc = "/image/file.png";
	private static String rootImageSrc = "/image/root.png";
	//视图大小
	private static final int VIEW_WIDTH = 200;
	private static final int VIEW_HEIGHT = 200;
	private static FileDirectoryTreeGraph instance;
	//文件结点的右键菜单
	private MyContextMenu rootMenu;
	//文件夹右键菜单
	private MyContextMenu folderMenu;
	//文件管理系统
	private static FileSystem fileSystem = FileSystem.getInstance();

	private FileDirectoryTreeGraph() {
		//初始化目录树
		this.initTreeGraph();
	}

	//初始化文件目录树
	private void initTreeGraph () {
		//添加�?个系统盘作为根结�?
		MyTreeItem rootItem = new MyTreeItem(MyFile.SYSTEM_VALUE);
		//添加此结�?
		this.setRoot(rootItem);
		//展开根结�?
		rootItem.setExpanded(true);

		//文件结点的右键菜�?
		rootMenu = new MyContextMenu(MyFile.SYSTEM_VALUE);
		//文件夹右键菜�?
		folderMenu = new MyContextMenu(MyFile.FOLDER_VALUE);

		//设置大小
		this.setPrefSize(VIEW_WIDTH, VIEW_HEIGHT);
		//设置选中模式为单个�?�中
		this.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		//设置右键菜单
		this.getSelectionModel().selectedItemProperty().addListener(e->{
			//获取选中的结�?
			selectedItem = (MyTreeItem) this.getSelectionModel().getSelectedItem();
			//获取结点类型
			int attribute = selectedItem.getAttribute();
			//根据结点的不同，设置不同的右键菜�?
			if (attribute == MyFile.FOLDER_VALUE) {
				//设置文件夹右菜单
				this.setContextMenu(folderMenu);
			} else {
				//设置系统目录右菜�?
				this.setContextMenu(rootMenu);
			}
		});

		//默认选中根结�?
		this.getSelectionModel().select(rootItem);
	}

	//获取实例
	public static FileDirectoryTreeGraph getInstance() {
		if (instance == null) {
			instance = new FileDirectoryTreeGraph();
		}
		return instance;
	}

	//获取根结点菜单项
	public MyContextMenu getRootMenu() {
		return this.rootMenu;
	}

	//获取文件夹菜单项
	public MyContextMenu getFolderMenu() {
		return this.folderMenu;
	}

	//添加子结点
	public void addChildItem(int attribute) { //FIXME
		//获取要添加子结点的结点
		selectedItem = (MyTreeItem) this.getSelectionModel().getSelectedItem();
		//创建子结点
		System.out.println("test8");
		MyTreeItem childItem = new MyTreeItem(attribute);
		
		//初始化子节点信息并将子节点注册到文件目录登记项
		FolderNode childFolderNode = initChildItem(attribute);
		childItem.setFolderNode(childFolderNode);
		//添加到子结点集合
		selectedItem.getChildList().add(childItem);
		//如果是文件夹，则添加到目录树中，并且设置为中
		if (attribute == MyFile.FOLDER_VALUE || attribute == MyFile.SYSTEM_VALUE) {
			//添加
			selectedItem.getChildren().add(childItem);
			//展开当前结点
			selectedItem.setExpanded(true);
			//将添加的子结点置为�?�中的结�?
			this.getSelectionModel().select(childItem);
		}
	}

	//移除子结点
	public void removeChildItem(MyTreeItem selectedItem) {
		//获取父结点
		MyTreeItem parentItem = (MyTreeItem) selectedItem.getParent();
		//移除当前结点
		parentItem.getChildren().remove(selectedItem);
		//父结点�?�中
		this.getSelectionModel().select(parentItem);
	}
    
	
	/**
	 * 描述： 获取目录树中从根节点到该树节点的路径
	 * 返回值： 返回相应路径
	 * 参数： selectedItem  目录树中的子节点
	 */
	private  String getPathName(MyTreeItem selectedItem) {
		
	   StringBuilder sBuffer = new StringBuilder(); 
	   sBuffer.append(selectedItem.getValue());
	   String str= null;
	   while(selectedItem.getParent() != null){

		   selectedItem = (MyTreeItem)selectedItem.getParent();
		   str = "";
		   str = selectedItem.getValue() + "/";
		   sBuffer.insert(0, str);
	   }
	   return sBuffer.toString();
	}
	
	/**
	 * 描述： 初始化当前路径中新建立的一个子节点
	 * 返回值：新建立字节中的文件目录项实例，其中包含该子节点的相关属性和信息
	 * @param childItem  新建立的子节点
	 */ 
	public  FolderNode initChildItem(int attribute) {
		
		//获取被选择节点的路径
		String selectedItemPath = getPathName(selectedItem);
		//获取子节点路径
		StringBuilder childItemPath = new StringBuilder(selectedItemPath);
		if(attribute == MyFile.FILE_VALUE) {
			childItemPath.append("/" + "新建文件.txt") ;
		} else {
			childItemPath.append( "/" + "新建文件夹");
		}
		//子节点中的文件目录项信息
		childItemPath.delete(0, 4);
		FolderNode childFolderNode = fileSystem.createFile(childItemPath.toString());
		return childFolderNode;
	} 
	
	
	//根据传入的参数创建文件夹结点或新建节点
	static class MyTreeItem extends TreeItem<String> {

		private int attribute;	//文件属属性
		private FolderNode folderNode;  //目录登记项，记录该文件的信息
		private List<MyTreeItem> childList;	 //子结点集合

		public MyTreeItem(int attribute) {
			
			System.out.println("test9");
			//初始化文件目录登记项
			folderNode = new FolderNode();
			//接收属性
			this.attribute = attribute;
			//初始化集合
			this.childList = new ArrayList<MyTreeItem>();
			//设置图标和名字
			if (attribute == MyFile.FOLDER_VALUE) {
				//文件�?
				ImageView emptyIcon = new ImageView(new Image(getClass().getResourceAsStream(emptyFolderImageSrc)));
				this.setGraphic(emptyIcon);
				this.setValue("新建文件夹");
				ImageView normalIcon = new ImageView(new Image(getClass().getResourceAsStream(folderImageSrc)));
				//监测文件夹变�?
				this.leafProperty().addListener(e->{
					if(this.isLeaf()) {
						//如果是叶子，则图标设为空文件�?
						this.setGraphic(emptyIcon);
					} else {
						//如果包含有子项，则设置为标准图标
						this.setGraphic(normalIcon);
					}
				});
			} else if (attribute == MyFile.FILE_VALUE) {
				//文件
				ImageView icon = new ImageView(new Image(getClass().getResourceAsStream(fileImageSrc)));
				this.setGraphic(icon);
				this.setValue("新建文件.txt");
			} else {
				//系统文件目录
				ImageView icon = new ImageView(new Image(getClass().getResourceAsStream(rootImageSrc)));
				this.setGraphic(icon);
				this.setValue("系统文件");
			}
			
		}

		//获取文件属性
		public int getAttribute() {
			return this.attribute;
		}

		//获取子结点集合
		public List<MyTreeItem> getChildList() {
			return this.childList;
		}
		
		//获取该节点的文件目录项
		public FolderNode getFolderNode() {
			return folderNode;
		}
		//设置该节点的文件目录项
		public void setFolderNode(FolderNode folderNode) {
			this.folderNode = folderNode;
		}
		

		
		
	}

	//根据传入的文件类型创建不同的右键菜单
	class MyContextMenu extends ContextMenu {

		//打开菜单项
		private MenuItem open = new MenuItem("打开");
		//删除菜单项
		private MenuItem delete = new MenuItem("删除");
		//重命名菜单项
		private MenuItem rename = new MenuItem("重命名");
		//新建文件夹菜单项
		private MenuItem addFolder = new MenuItem("新建文件夹");
		//属性菜单项
		private MenuItem attribute = new MenuItem("属性");

		//构造函数
		public MyContextMenu(int attribute) {
			if (attribute == MyFile.FOLDER_VALUE) {
				//文件夹
				this.createFolderMenu();
			} else {
				//系统目录
				this.createRootMenu();
			}
		}

		//创建系统盘菜单
		public void createRootMenu() {
			//新建文件夹菜单项
			addFolder = new MenuItem("新建文件夹");
			//添加菜单项到菜单项
			this.getItems().addAll(addFolder);
		}

		//创建文件夹菜单
		public void createFolderMenu() {
			//打开菜单项
			open = new MenuItem("打开");
			//删除菜单项
			delete = new MenuItem("删除");
			//重命名菜单项
			rename = new MenuItem("重命名");
			//新建文件夹菜单项
			addFolder = new MenuItem("新建文件夹");
			//属性菜单项
			attribute = new MenuItem("属性");
			//添加菜单项到菜单项
			this.getItems().addAll(open, delete, rename, addFolder, attribute);
		}

		//获取菜单�?
		public MenuItem getAddFolder() {
			return addFolder;
		}

		public MenuItem getOpen() {
			return open;
		}

		public MenuItem getDelete() {
			return delete;
		}

		public MenuItem getRename() {
			return rename;
		}

		public MenuItem getAttribute() {
			return attribute;
		}

	}


	

}
