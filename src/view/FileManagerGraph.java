package view;

import java.util.ArrayList;
import java.util.List;

//import graph.FileDirectoryEditGraph.FileDirectoryItem;
//import graph.FileDirectoryTreeGraph.MyTreeItem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.MyFile;
import service.FileSystem;
//import sun.applet.Main;
import util.StringMethod;
import view.FileDirectoryEditGraph.FileDirectoryItem;
import view.FileDirectoryTreeGraph.MyTreeItem;

/**
 * 此类为文件管理视图，包含了路径显示，文件目录树和文件视图
 *
 */

public class FileManagerGraph extends BorderPane {

	private PathBarGraph pathBarGraph;	//路径�?
	private FileDirectoryTreeGraph treeGraph;	//文件目录�?
	private FileDirectoryEditGraph editGraph;	//文件编辑窗口集合
	private VBox editBox;	//为了扩展，增加编辑布�?
	public static MyTreeItem rootItem;	//根结�?
	private MyTreeItem currentItem;	//当前选中进入的子�?
	private FileSystem fileSystem = FileSystem.getInstance();	
	public static String currentPath = "Root:\\";	//获取当前路径
	
	//构�?�函�?
	public FileManagerGraph() {
		//初始�?
		this.initGraph();
	}

	//初始化文件管理视图
	private void initGraph() {
		//创建文件目录�?
		treeGraph = FileDirectoryTreeGraph.getInstance();
		//为文件目录树设置事件
		this.addActionForTreeGraph();
		//获取根结�?
		rootItem = (MyTreeItem) treeGraph.getRoot();
		currentItem = rootItem;
		//创建文件编辑窗口
		editGraph = new FileDirectoryEditGraph();
		//创建编辑布局
		editBox = new VBox();
		editBox.getChildren().add(editGraph);
		//为编辑窗口设置事�?
		this.addActionForEditGraph();
		//创建路径�?
		pathBarGraph = new PathBarGraph(currentItem);

		//添加
		this.setTop(pathBarGraph);
		this.setLeft(treeGraph);
		this.setCenter(editBox);
	}

	//获取编辑扩展布局
	public VBox getEditBox() {
		return this.editBox;
	}

	//转换路径时，更新路径和编辑窗口
	private void transformPath(MyTreeItem selectedItem) {
		//更新编辑窗口
		this.updateEditGraph(selectedItem);
		//更新路径
		this.pathBarGraph.updatePath(selectedItem);
	}

	//为文件目录树的各个部分设置事件
	private void addActionForTreeGraph() {
		//路径更新，编辑窗口更新
		this.treeGraph.setOnMouseClicked(e->{
			if (e.getButton() == MouseButton.PRIMARY) {
				//鼠标左键点击事件
				//获取选中的子�?
				MyTreeItem selectedItem = (MyTreeItem) this.treeGraph.getSelectionModel().getSelectedItem();
				//更新  
				if (selectedItem != this.currentItem) {
					//转换路径
					this.transformPath(selectedItem);
				}
			}
  
		});
		//目录树中右键菜单项中的新建文件夹菜单项
		this.treeGraph.getRootMenu().getAddFolder().setOnAction(e->{ //FIXME
			//添加文件夹到编辑窗口-----1
			this.addFolderToEditGraph();
			//重新渲染主界面
			MainFrame.paintMainFrame();
			
		});
		this.treeGraph.getFolderMenu().getAddFolder().setOnAction(e->{
			//添加文件夹到编辑窗口
			this.addFolderToEditGraph();
			//重新渲染主界面
			MainFrame.paintMainFrame(); 
		});
		this.treeGraph.getRootMenu().getDelete().setOnAction(e->{
			//从编辑窗口删除指定文�?
			this.removeItemFromEditGraph(); 
			MainFrame.paintMainFrame(); 
		});
		this.treeGraph.getFolderMenu().getDelete().setOnAction(e->{
			//从编辑窗口删除指定文�?
			this.removeItemFromEditGraph();
			MainFrame.paintMainFrame(); 
		});
	}

	//往编辑窗口添加文件夹
	private void addFolderToEditGraph() { 
		//获取选中的子项
		MyTreeItem selectedItem = (MyTreeItem) this.treeGraph.getSelectionModel().getSelectedItem();
		//目录树添加子结点
		this.treeGraph.addChildItem(MyFile.FOLDER_VALUE);
		//如果添加子项结点路径与当前一致，则编辑窗口也要添加子项
		if (selectedItem == this.currentItem) {
			//获取选中的子项
			selectedItem = (MyTreeItem) this.treeGraph.getSelectionModel().getSelectedItem();
			//编辑窗口添加文件
			this.addItemToEditGraph(selectedItem);  
		}
	}

	//从编辑窗口删除文件或文件�?
	private void removeItemFromEditGraph() {
		//获取选中的子�?
		MyTreeItem selectedItem = (MyTreeItem) this.treeGraph.getSelectionModel().getSelectedItem();
		//规范化文件路径 
		String path = StringMethod.deleRootStr(selectedItem.getPath());
		//删除文件系统中的对应文件的数据  
		fileSystem.deleFile(path); 
		//从子结点集合中删除当前结�?
		MyTreeItem parentItem = (MyTreeItem) selectedItem.getParent();
		parentItem.removeChild(selectedItem);
		if (selectedItem == this.currentItem) {
			//如果删除的是当前的文件夹，更新编辑窗口和路径，路径为当前路径的上�?层路�?
			//更新编辑窗口
			this.updateEditGraph(parentItem);
			//更新路径
			this.pathBarGraph.updatePath(parentItem);
		} else if (selectedItem.getParent() == this.currentItem) {
			//如果删除子项结点父路径与当前�?致，则编辑窗口也要删除子�?
			this.editGraph.removeFileDirectory(selectedItem);
		}
		//从目录树中移除结�?
		this.treeGraph.removeChildItem(selectedItem);
	}

	//更新文件编辑窗口 -- 文件路径改变
	private void updateEditGraph(MyTreeItem selectedItem) {
		//更新当前子项
		this.currentItem = selectedItem;
		//清空
		this.editGraph.getChildren().clear();
		//获取当前目录的子项集�?
		List<MyTreeItem> childList = this.currentItem.getChildList();
		//添加
		if (childList != null && childList.size() > 0) {
			for(MyTreeItem child : childList) {
				//�?编辑窗口添加子项
				this.addItemToEditGraph(child);
			}
		}
	}

	//为文件编辑窗口的各个部分设置事件
	private void addActionForEditGraph() {
		//设置新建文件和新建文件夹的事件
		this.editGraph.getAddMenu().getAddFile().setOnAction(e->{
			//编辑窗口添加文件
			FileDirectoryItem item = this.addItemToEditGraph(MyFile.FILE_VALUE);
			MyTreeItem selectedItem = (MyTreeItem) this.treeGraph.getSelectionModel().getSelectedItem();
			selectedItem.getPath();  
			//将结点加入子结点集合  
			this.currentItem.addChild(item.getTreeItem());
			//重新渲染主界面
			MainFrame.paintMainFrame();
		});
		this.editGraph.getAddMenu().getAddFolder().setOnAction(e->{
			//编辑窗口添加文件�?
			FileDirectoryItem item = this.addItemToEditGraph(MyFile.FOLDER_VALUE);
			//将结点加入子结点集合
			this.currentItem.addChild(item.getTreeItem());
			//目录树添加文件夹结点
			this.currentItem.getChildren().add(item.getTreeItem());
			//重新渲染主界面
			MainFrame.paintMainFrame();  
		});
	}

	//给定�?个目录树结点在编辑窗口添加一个子�?
	private FileDirectoryItem addItemToEditGraph(MyTreeItem treeItem) {
		//�?编辑窗口添加子项
		FileDirectoryItem item = this.editGraph.addFileDirectory(treeItem);
		//子项添加事件
		this.addActionToEditGraphItem(item);
		//返回
		return item;
	}

	//给定文件类型参数在编辑窗口添加一个子�?
	private FileDirectoryItem addItemToEditGraph(int attribute) {
		//�?编辑窗口添加子项
		FileDirectoryItem item = this.editGraph.addFileDirectory(attribute, currentItem);
		//子项添加事件
		this.addActionToEditGraphItem(item);
		//返回
		return item;
	}

	//为编辑窗口的子项设置事件
	private void addActionToEditGraphItem(FileDirectoryItem item) {
		//获取相关联的结点
		MyTreeItem treeItem = item.getTreeItem();
		//删除菜单项事�?
		item.getMenu().getDelete().setOnAction(e->{
			
			//规范化文件路径 
			String path = StringMethod.deleRootStr(treeItem.getPath());
			//删除文件系统中的对应文件的数据
			fileSystem.deleFile(path); 
			//从子结点集合中删除此结点
			this.currentItem.removeChild(treeItem);
			//如果是文件夹，从目录树种删除相关联的结点
			if (treeItem.getAttribute() == MyFile.FOLDER_VALUE) {
				this.currentItem.getChildren().remove(item.getTreeItem());
			}
			//从编辑窗口删除当前子�?
			this.editGraph.getChildren().remove(item);
			//重新渲染整个界面
			MainFrame.paintMainFrame();
		});
		//打开菜单项事�?
//FIXME			
		item.getMenu().getOpen().setOnAction(e->{ 
			//目录编辑框中，右键菜单打开项，打开文本编辑器
			openMenuDeal(item, treeItem);

		});
		//双击事件
		item.setOnMouseClicked(e->{
			if (e.getClickCount() == 2) {
				//打开文本编辑器
				openMenuDeal(item, treeItem); 
			}
		});
	}

	/**
	 * 描述： 编辑框中对文件或目录右键点击打开菜单项或双击文件时，处理器所执行的方法
	 * 返回值：
	 * 参数：
	 * @param item  编辑框对象
	 * @param treeItem 当前文件/目录节点
	 * 
	 */
	private void openMenuDeal(FileDirectoryItem item, MyTreeItem treeItem) {
		
		if (treeItem.getAttribute() == MyFile.FILE_VALUE) {
			//如果是文件对象，则打文本编辑窗口
			FileContentEditGraph  editObject = new FileContentEditGraph(item.getName().getText(), treeItem.getPath());
			//规范化文件路径，去除路径中的'ROOT:'子串
			String pathName = StringMethod.deleRootStr(treeItem.getPath());
			//从后台获取文件内容
			String strBuffer = fileSystem.openFile(pathName);
			editObject.getContentArea().setText(strBuffer); 
			Stage editStage = editObject;
			editStage.show();
		} else if (treeItem.getAttribute() == MyFile.FOLDER_VALUE) {
			//如果是文件夹，则转换路径到下�?�?
			this.transformPath(treeItem); 
		}
	}
	
	//路径�?
	class PathBarGraph extends HBox {

		private Button backButton;	//返回按钮
		private TextField pathField;	//路径�?
		private MyTreeItem currentItem;	//当前路径的文件目录树子项
		private String backImageSrc = "/image/back16.png";	//返回按钮图片地址

		public PathBarGraph(MyTreeItem currentItem) {
			//接收参数
			this.currentItem = currentItem;
			//初始�?
			initGraph();
		}

		private void initGraph() {
			//创建返回按钮
			backButton = new Button();
			//图标
			backButton.setGraphic(new ImageView(new Image(getClass().getResourceAsStream(backImageSrc))));
			//提示
			backButton.setTooltip(new Tooltip("返回上一�?"));
			//设置为�?�明
			backButton.setStyle("-fx-background-color:#FFFFFF;");
			//设置鼠标事件
			backButton.setOnMouseEntered(e->{
				backButton.setStyle("-fx-background-color:#E0FFFF;");
			});
			backButton.setOnMouseExited(e->{
				backButton.setStyle("-fx-background-color:#FFFFFF;");
			});

			//创建路径�?
			FileManagerGraph.currentPath = getPath();
			pathField = new TextField(FileManagerGraph.currentPath);
			pathField.setMinWidth(600);
			//路径提示
			Label pathLabel = new Label("当前路径�?");

			//添加返回按钮和路径栏
			this.getChildren().addAll(backButton, pathLabel, pathField);
			//背景设置为白�?
			this.setStyle("-fx-background-color:#FFFFFF");
			//设置间距
			this.setSpacing(5);
			this.setAlignment(Pos.CENTER_LEFT);
			this.setPadding(new Insets(2));
		}

		//获取路径
		private String getPath() {
			//路径集合
			List<String> pathList = new ArrayList<String>();
			//当前子项
			MyTreeItem temp = currentItem;
			//�?上获取路�?
			while (temp != treeGraph.getRoot()) {
				pathList.add(temp.getValue());
				temp = (MyTreeItem) temp.getParent();
			} 
			//获取完整路径
			StringBuilder path = new StringBuilder("Root:\\");
			if (pathList != null && pathList.size() > 0) {
				for(int i = pathList.size()-1; i >= 0; i--) {
					path.append(pathList.get(i));
					if (i != 0) {
						path.append("\\");
					}
				}
			}
			//返回路径
			return path.toString();
		}

		//更新路径
		public void updatePath(MyTreeItem currentItem) {
			//更新当前子项
			this.currentItem = currentItem;
			//更新路径
			FileManagerGraph.currentPath = getPath();
			this.pathField.setText(FileManagerGraph.currentPath);
		}

	}

}
