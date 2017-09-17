package view;

/*import DiskBlockGraph;
import DiskPieGraph;
import graph.FATGraph;
import graph.FileManagerGraph;*/
import service.*;

import java.util.ArrayList;

import com.sun.org.apache.xml.internal.security.keys.keyresolver.implementations.PrivateKeyResolver;
import com.sun.swing.internal.plaf.metal.resources.metal_zh_TW;
import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;
import com.sun.xml.internal.bind.v2.util.FatalAdapter;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import jdk.internal.org.objectweb.asm.tree.IntInsnNode;
import model.FAT;


public class MainFrame extends Application {

	private static DiskPieGraph diskPieGraph;	//扇形磁盘分配�?
	private static Stage stage;	//显示窗口
	private static FileSystem fileSystem;  //文件管理系统
	private static DiskBlockGraph diskBlockGraph;  //
	private static FATGraph fatGraph; //FAT 磁盘分配布局
	private static VBox blockBox;
	@Override
	public void start(Stage primaryStage) {
		try {
			stage = primaryStage;
			
			//加入文件操作系统
			fileSystem = FileSystem.getInstance();
			HBox root = new HBox();
			root.setPadding(new Insets(10));
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			//添加文件管理窗口
			FileManagerGraph managerGraph = new FileManagerGraph();
			root.getChildren().add(managerGraph);

			//1.创建磁盘文件块分配表
			blockBox = new VBox();
			diskBlockGraph = new DiskBlockGraph(16, 8);
			paintDiskBlocks();
			//2. 创建扇形磁盘分配图
			HBox diskGraphBox = new HBox();
			diskPieGraph = DiskPieGraph.getInstance();
			diskGraphBox.getChildren().addAll(blockBox, diskPieGraph);
			//3. 将磁盘文件块分配表和扇形磁盘分配图添加到主界面中
			managerGraph.getEditBox().getChildren().add(diskGraphBox);

			//1. 创建FAT分配表布局
			fatGraph = FATGraph.getInstance();
			fatGraph.prefHeightProperty().bind(managerGraph.getEditBox().heightProperty());
			//2. 渲染FAT分配表布局
			paintFat();
			//3. 在主界面中加入FAT表
			root.getChildren().add(fatGraph);

			//添加文件目录�?
			//FileDirectoryTreeGraph fileGraph = new FileDirectoryTreeGraph();
			//root.setTop(fileGraph);


			primaryStage.setTitle("FileSystem");
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 描述： 在fat布局中描绘出fat表内容
	 * 返回值：
	 * @param : FATGraph fatGraph
	 */
	public static void paintFat() {
		
		int[] fat = fileSystem.getFat().getItem();
		for(int i = 0; i < fat.length; i++) {
			
			fatGraph.allocateFATItem(i, fat[i]);
		}
	}
	
	/**
	 * 描述：在VBox布局中渲染磁盘块分配矩阵
	 * 返回值： 
	 * 参数： blockBox  VBox布局，作为主界面中的磁盘块分配矩阵的布局
	 */
	public static void paintDiskBlocks() {
		
		int[] fat = fileSystem.getFat().getItem();
		ArrayList<Integer> list = new ArrayList<Integer>();
		for(int i = 0; i < fat.length; i++) {
			 list.add(Integer.valueOf(fat[i]));
		}
		diskBlockGraph.allocateDiskBlocks(list);
		blockBox.setStyle("-fx-background-color:#FFFFFF;");
		blockBox.setAlignment(Pos.CENTER);
		blockBox.setPadding(new Insets(10, 0, 10, 10));
		blockBox.setSpacing(30);
		Label title = new Label("磁盘分配情况");
		title.setFont(Font.font("宋体", 16));
		title.setTextFill(Color.CORNFLOWERBLUE);
		blockBox.getChildren().clear();
		blockBox.getChildren().addAll(title, diskBlockGraph);
	}
	
	/**
	 * 描述：重新渲染主界面
	 */
	public static void paintMainFrame() {
		
		//主界面中，中描绘出fat表内容
		paintFat();
		//主界面中渲染磁盘块分配矩阵
		paintDiskBlocks();
		//在主界面中渲染
		diskPieGraph.updatePieGraph(fileSystem.getDisksUsedCapacity() - diskPieGraph.getAllocatedValue());
		
	}
	//获取窗口对象
	public static Stage getStage() {
		return stage;
	}

	public static void main(String[] args) {
		launch(args);
	}
}
