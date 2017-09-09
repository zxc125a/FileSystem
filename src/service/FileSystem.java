package service;
import model.*;
import sun.security.jca.GetInstance;
import jdk.internal.org.objectweb.asm.tree.IntInsnNode;

import java.util.*;

import com.sun.media.sound.PortMixerProvider;

import model.*;
import util.*;

public class FileSystem {
	
	private FAT fat; //fat表
	private ArrayList<OpenFile> openFiles;//已打开文件列表
	private Disk[] disks = new Disk[128]; //声明磁盘盘块
	private static FileSystem fileSystem;
	public static final int FOLDER_VALUE = 12;
	public static final int FILE_VALUE = 3;
	public static final int SYSTEM_VALUE = 1;
	
	public FileSystem() {
		
		//初始化 FAT 表
		fat = FAT.getInstance();
		//初始化已打开文件列表
		openFiles = new ArrayList<OpenFile>();
		//默认打开根目录
	    OpenFile openFile = initOpenFile(2, "/");
	    openFiles.add(openFile);
		//为每一个盘块分配空间
		for(int i = 0; i < 128; i++) {
			
			disks[i] = new Disk();
		}
	}
	
	
	/**
	 * 描述： 全局对单一FileSystem实例进行管理和操作
	 * @return
	 */
	public static FileSystem getInstance() {
		
		if(fileSystem == null) {
			
			fileSystem = new FileSystem();
		}
		
		return fileSystem;
	}
	
	public FAT getFat() {
		
		return fat; 
	}

	public void setFat(FAT fat) {
		this.fat = fat;
	}


	public ArrayList<OpenFile> getOpenFiles() {
		return openFiles;
	}

	public void setOpenFiles(ArrayList<OpenFile> openFiles) {
		this.openFiles = openFiles;
	}

	public Disk[] getDisks() {
		return disks;
	}

	public void setDisks(Disk[] disks) {
		this.disks = disks;
	}

	public static void main(String[] args) {
		
		
		FileSystem fileSystem = FileSystem.getInstance();
		//新建文件
		fileSystem.createFile("/1");
		fileSystem.createFile("/1/1.txt");
		fileSystem.openFile("/1/1.txt");
		fileSystem.openFile("/");
		fileSystem.openFile("/2");
		fileSystem.openFile("/2/3");
		fileSystem.openFile("/2.txt");
		for(int i = 0; i < fileSystem.fat.getItem().length; i++) {
			
			System.out.println(fileSystem.fat.getItem()[i]);
		}
	}
	
	/** 
	 * 描述：新建文件/目录基本操作
	 * 返回值：新建的文件或目录的文件目录登记项，保存其信息, 新建失败，则返回null
	 * 参数： pathName 文件路径
	 */
	public FolderNode createFile(String pathName) {
		
		//1. 检查文件目录，确认无重名文件和空文件名
		int folderIndex =  pathSearch(pathName);
		if(folderIndex == -1) {System.out.println("所给路径错误！"); return null;}
		if(folderIndex == 0)  {System.out.println("存在名字相同的文件"); return null;}
		//获取文件名字
		String[] pathArray = pathName.split("/");
		String fileName = new String(pathArray[pathArray.length - 1]);
		//2. 可以新建文件，寻找空闲登记项
		int n = Tool.remaindedCapacity(disks, folderIndex);
		if(n == -1) {
			
			System.out.println("文件目录登记项已达最大值");
			return null;
		}
		//3. 获取所有空闲的磁盘块
		int totalFreeDisk = fat.getFreeDiskNum();
		if(totalFreeDisk == 0) {
			
			System.out.println("创建文件失败，磁盘空间不足");
			return null;
		}
		//4. 找到一个空闲磁盘块,并分配磁盘块个给新建立的文件
		int freeDisk = fat.getFreeDisk();
		
		fat.getItem()[freeDisk] = -1;
		//5. 在工作目录中，登记新建立的文件/目录
		int nodeAttritute = 0;
		if(pathName.endsWith(".txt")) {
			
			nodeAttritute = FILE_VALUE;   //创建一个可读可写的文件
		} else {
		
			nodeAttritute = FOLDER_VALUE;    //创建一个可读可写的目录
		}
		
		disks[folderIndex].getFolderNode()[n].initFolderNode(fileName, "  ", nodeAttritute, freeDisk, 1);
		if(nodeAttritute == FILE_VALUE) {
			
			System.out.println("创建文件成功");
		} else {
			
			System.out.println("创建目录成功");
		}
		
		return disks[folderIndex].getFolderNode()[n];
	}
	
	/**
	 *描述：实现打开文件操作，并从磁盘中读取文件内容
	 *返回值： 无
	 *参数： pathName : 文件路径名
	 */
	public void openFile(String pathName) {
		
		//1. 首先要检查该文件是否存在
		int folderIndex  = pathSearch(pathName);
		if(folderIndex != 0 && folderIndex != 2)  {System.out.println("打开文件失败，文件路径有误!!!"); return;}
		//2. 如果文件存在，还要检查打开方式，确保不能以写方式打开只读文件
		//
		//3. 最后填写已打开文件表，若文件已经打开或者超过打开文件的数量，则不需要填写已打开文件表
	    if(openFiles.size() > 5) {System.out.println("打开文件失败，文件打开数量过多"); return;} //文件数量不能超过5
	  
	    //4.文件是否已经打开,未打开过则，则需要添加到已打开文件列表中
	    int beginDiskIndex = getBeginDiskNum(pathName);//获取文件在起始磁盘块号
	    //如果是文件，则需要判断该文件是否已经打开 
	    if(beginDiskIndex == 0) {System.out.println("打开文件失败，文件路径有误!!!"); return; }
	    if(pathName.endsWith(".txt")) {
	    	
		    if(!judgeOpenFile(pathName)) {
		    	
			    // 在已打开文件表中添加新的文件
			    OpenFile openFile = initOpenFile(beginDiskIndex, pathName);
			    openFiles.add(openFile);
		    }
	    }

	    //5. 获取文件内容，并输出
	    if(pathName.endsWith(".txt")) {
		     //显示文件内容
	    	StringBuilder sbBuilder  = new StringBuilder(); //根据起始磁盘盘块号，读取文件内容到缓冲区中
		    sbBuilder.append(getFileContent(beginDiskIndex));
		    //输出文件内容
		    System.out.println(sbBuilder.toString());
		    System.out.println("打开文件成功！！！");
		    return;
	    } else {
	    	//显示目录内容
	    	System.out.println("打开目录成功！！！");
	    }

	}

	/**
	 * 描述： 关闭文件
	 * 返回值：无
	 * 参数：pathName 文件路径
	 */
	public void closeFile(String pathName, String content) {
		
		//1. 首先要看该文件是否打开，如果没有打开，就不用关闭
	    int beginDiskIndex = getBeginDiskNum(pathName);  //获取文件在起始磁盘块号
	    if(!judgeOpenFile(pathName)) {System.out.println("关闭失败，文件未打开"); return;}
	    //2. 检查打开方式，如果是写方式打开的，要追加文件数据和文件结束符。如果是读方式打开的，则不用保存数据;
	    if(checkFileAttribute(pathName) == 0) {return;} //读打开方式，不用存储数据
	    //3. 写打开方式,存储数据
	    int len = storeIntoDisk(pathName, content);
	    //4.修改目录项长度
	    FolderNode folderNode = getFolderNode(pathName);
	    if(folderNode == null) {System.out.println("获取目录登记项失败！！！");  return;}
	    folderNode.setNodeLength((int)len);
	    //5.从已打开文件表中删除对应项
	    boolean flag = removeOpenFile(beginDiskIndex);
	    if(!flag) {
	    
	    	System.out.println("已打文件表操作失败");
	    } else {
	    	
	    	System.out.println("文件关闭成功");
	    }
		return;
	}
	
	/**
	 * 描述： 删除文件
	 * 返回值：删除成功，则返回true, 否则返回false
	 * 参数： pathName  文件路径 
	 */
	public void deleFile(String pathName) {
		
		//1. 验证要删除的文件是否存在
		int beginDisk = getBeginDiskNum(pathName);
		if(beginDisk == -1) {System.out.println("路径不能为空"); return;}
		if(beginDisk == 0) {System.out.println("文件不存在"); return;}
		//2. 验证文件是否打开
		if(judgeOpenFile(pathName)) {System.out.println("文件已经打开，不能删除"); return;}
		//3. 删除目录中的目录项
		if(!deleFolderNode(pathName)) {System.out.println("文件在目录中登记项删除失败"); return;}
		//4. 释放文件磁盘空间
		if(!freeDiskCapacity(beginDisk)) {System.out.println("文件磁盘空间释放失败"); return;}
		System.out.println("文件删除成功！！！");
		return;
	}

	
//--------------------------------------------------------------------------------------//
	
	/**
	 * 描述：给出文件路径名来获取文件的起始磁盘块号
	 * 返回值：根路径，则返回2,文件不存在则返回0,否则返回文件的磁盘盘块号
	 * 参数：pathName：文件路径名
	 */
	private int getBeginDiskNum(String pathName) {
		
		//返回根目录所在的磁盘块
		if("/".equals(pathName)) {
			
			return 2;
		}
		int diskNum = pathSearch(pathName);
		if(diskNum != 0) {return 0;} //文件路径有误
		//1. 获取文件工作目录对应
		int length = pathName.split("/").length;
		String fileName = pathName.split("/")[length - 1];
		pathName += ".";
		diskNum = pathSearch(pathName);  //文件名不存在，返回该文件的工作目录所在的磁盘块号
		//2. 获取文件名
		int i = 0;
		for(; i < 8; i++) {
			
			
			if(fileName.equals(disks[diskNum].getFolderNode()[i].getNodePathName())) {
				
				return disks[diskNum].getFolderNode()[i].getNodeBeginDisk();
			}
		}
		
		return 0;
	} 
	
	
	/**
	 * 描述：给定文件路径名，返回该文件所在工作目录的磁盘块号
	 * 返回值：不存在文件对应路径，则返回 -1，若找到同路径名文件，则返回0，返回2，表示返回根目录的磁盘块号，否则返回文件工作目录所在的磁盘盘块号
	 * 
	 * 参数：pathName：文件路径名
	 */
	private int pathSearch(String pathName) {
		
		//返回根目录所在的磁盘块
		if("/".equals(pathName)) {
			
			return 2;
		}
		//1. 获取文件名
		String[] pathArray = pathName.split("/");
		String fileName = new String(pathArray[pathArray.length - 1]);
		//若文件名为空，则返回-1；
		if(StringMethod.isEmpty(pathName)) {
			
			return -1;
		}
		//2. 从根目录开始遍历
		int folderIndex = 2;	 //当前目录所在磁盘盘块的编号， 2表示根目录所在的磁盘号
		boolean flag = false;    //true 表示存在重名文件，false表示不存在重名文件
		//3. 设置文件类型
		int type = 1;            //type == 1 表示目录, type == 0 表示文件
		//4. 遍历所给路径的子目录
		for(int i = 1; i < pathArray.length; i++) {
			
			//判断最后一个子目录是文件还是目录
			if(i == pathArray.length - 1) {
				
				if(fileName.endsWith(".txt")) 
					
					type = 0;   //文件
			} else {
				
					type = 1;     //目录
			}
			
			//1. 从磁盘块中取出目录中的目录项
			FolderNode[] folderNodes = Arrays.copyOf(disks[folderIndex].getFolderNode(), disks[folderIndex].getFolderNode().length);
				//2. 遍历当前目录下的所有目录项
			int j = 0;
			for(; j < 8; j++) {
				
				int k = folderNodes[j].getNodeBeginDisk();
				//k != 0 表示当前磁盘盘块已被使用,磁盘内容为文件内容或者是目录
				if(k != 0) {
					//3.取出该磁盘盘块内容，并根据目录登记项属性判断是否为文件类型：
					if( (folderNodes[j].getNodeAttritute() & 8) == type * 8) { //字节为 8（00001000），表示该目录是一个目录的登记项
						//4. 比较子目录名
						if(pathArray[i].equals(folderNodes[j].getNodePathName())) {
								
							if(i == pathArray.length - 1) {
								
								flag = true;    
								break;
							}
							//记下当前子目录下的起始盘号
							folderIndex = k;
							break;
						}
					}
				}
			}
			if(j >= 8 && i < pathArray.length - 1) {
				
				return -1;    //所给路径的深度大于目录实际深度
			}
		}

		if(!flag) {
			return folderIndex;
		} else {
			return 0;
		}
	} 
	
	
	/**
	 * 描述：给出文件起始盘块号，返回文件的内容
	 * 返回值：返回文件内容
	 * 参数：diskNum 文件起始盘块号
	 */
	public String getFileContent(int diskNum) {
		
		//1. 设置缓冲区
		String sBuffer = new String();
		String str = null;
		//2. 根据起始盘块号，读取磁盘中文件的内容
		while(diskNum != -1) {
			
			str  = new String(disks[diskNum].getContent());
			if(StringMethod.isEmpty(str)) {
			
				str = "";
			}
			sBuffer += str;
			try {
				diskNum = fat.nextDiskIndex(diskNum);
			}catch (Exception e) {
				// TODO: handle exception
				System.out.println("FAT表访问越界！！！");
				System.exit(0);
			}
			
		}
		
		return sBuffer;
	} 
	
	
	/**
	 * 描述：验证给定路径的文件是否已经被打开；
	 *返回值：打开被返回 true, 否则返回 false;
	 *参数：beginDiskIndex：文件起始盘块号，pathName：文件所在的路径
	 */
	private boolean judgeOpenFile(String pathName) {
		
		//1. 获取文件起始盘块号
	    int beginDiskIndex = getBeginDiskNum(pathName);
	    //2. 获取文件名
	    String[] strArray = pathName.split("/");
	    String fileName = strArray[strArray.length - 1];
	    Iterator<OpenFile> it = openFiles.iterator();
	    while(it.hasNext()) {
	    	
	    	OpenFile openFile = it.next();
	    	if(openFile.getDiskNumber() == beginDiskIndex && 
	    			openFile.getFileName().equals(fileName)) {
	    		
	    		System.out.println("文件已经被打开");
	    		return true;
	    	}
	    }
	    return false;
	    
	}
	
	
	/**
	 * 描述： 为已打开文件新建一个OpenFile对象，并为之初始化
	 * 返回值： 初始化后的OpenFile实例
	 * 参数：
	 */
	private OpenFile initOpenFile(int diskNum, String pathName) {
		
		//1. 根据文件路径，获取文件名
	    String[] strArray = pathName.split("/");
	    String fileName = null;
	    if(strArray.length > 1)
	    	fileName = strArray[strArray.length - 1];
	    //2. 获取文件属性
		int nodeAttritute = 0;
		if(pathName.endsWith(".txt")) {
			
			nodeAttritute = FOLDER_VALUE;   //创建一个可读可写的目录
		} else {
			nodeAttritute = FILE_VALUE;    //创建一个可读可写的文件
		}
		//3. 获取起始磁盘号
		int beginDiskNum = diskNum;
		//4. 获取文件长度
		int length = fat.getFileLength(beginDiskNum);
		//5. 设设置文件默认类型，'1'为可写
		int flag = 1;
		//6. 设置读指针
		Pointer read = new Pointer();
		read.setBnum(beginDiskNum);
		read.setDnum(beginDiskNum + length - 1);
		//7. 设置写指针
		Pointer write = new Pointer();
		write.setBnum(beginDiskNum + length - 1);
		write.setDnum(beginDiskNum + length - 1);
		//8. 初始化OpenFile实例对象
		OpenFile openFile = new OpenFile(fileName, nodeAttritute, beginDiskNum, length, flag, read, write);
		
		return openFile;
		
	}


	/**
	 * 描述：检查文件的打开格式
	 * 返回值： 1表示可读可写方式， 0表示读方式, -1表示文件不存在
	 * 参数：pathName 文件路径名
	 */
	private int checkFileAttribute(String pathName) {
		
		//1. 获取文件工作目录对应
		int diskNum = pathSearch(pathName);
		if(diskNum == -1) {System.out.println("文件路径为空"); return -1;}
		if(diskNum != 0) {System.out.println("文件不存在"); return -1; }
		//2. 获取文件名
		int length = pathName.split("/").length;
		String fileName = pathName.split("/")[length - 1];
		int i = 0;
		for(; i < 8; i++) {
				//3.获取文件属性，验证文件打开方式	
			if(fileName.equals(disks[diskNum].getFolderNode()[i].getNodePathName())) {
				
				int attribute = disks[diskNum].getFolderNode()[i].getNodeAttritute();
				if((attribute & 8) == 0 &&  (attribute & 1) == 1) {
					//返回读打开方式
					return 0; 
				}
			}
		}
		//返回写打开方式
		return 1;
	}
	
	
	/**
	 * 描述：将文件内容，存储到磁盘中，并返回占用磁盘盘块的数量
	 * 返回值：文件占用磁盘盘块的数量,存储失败，返回0
	 * 参数：pathName 路径名
	 *     content 文件内容
	 */
	private int storeIntoDisk(String pathName, String content) {
		
		int beginDiskNum = getBeginDiskNum(pathName);
		if(beginDiskNum == 0 || beginDiskNum == -1) {
			
			System.out.println("文件路径有误，存储失败");
			return 0;
		}
		
		//1. 空闲磁盘块数是否满足
		int length = (int)(Math.ceil((content.length() % 64))); //文件内容所需要存储的磁盘盘块数
		int size = fat.getFreeDiskNum();
		if(length > size) {System.out.println("磁盘空间不足！！！"); return 0;}
		//2. 释放文件原磁盘空间
	    boolean flag = freeDiskCapacity(beginDiskNum); //释放文件原有磁盘空间
	    if(!flag) {System.out.println("磁盘空间释放失败"); return 0;}
		//3. 给文件分配新的磁盘空间
	    int i = 0;
		for(; i < length - 1; i++) {
			
			int nextDiskNum = fat.getFreeDisk();
			disks[nextDiskNum].setContent(new String(content.substring(64 * i, 64 * ( i + 1) - 1)));
			fat.getItem()[beginDiskNum] = nextDiskNum;
			beginDiskNum = nextDiskNum;
		}
		//为最后一块磁盘盘块分配空间
		int nextDiskNum = fat.getFreeDisk();
		disks[nextDiskNum].setContent(new String(content.substring(64 * i, content.length() - 1)));
		fat.getItem()[beginDiskNum] = nextDiskNum;
		fat.getItem()[nextDiskNum] = -1;
		return length;
	}


	/**
	 * 描述： 释放文件所占用的磁盘空间
	 * 返回值： 释放成功则返回true, 否则返回false;
	 * 参数： beginDiskNum文件起始盘块编号
	 */
	private boolean freeDiskCapacity(int beginDiskNum) {
		
		if(beginDiskNum < 3 || beginDiskNum > 127) {
		
			System.out.println("盘块号越界，磁盘空间释放失败");
			return false;
		}
		
		int nextDisk = 0;
		do{
			
			nextDisk = fat.getItem()[beginDiskNum];
			fat.getItem()[beginDiskNum] = 0;
			disks[beginDiskNum].setContent("");
			beginDiskNum = nextDisk;
			
		} while(nextDisk != -1);
		
		return true;
	}
	
	
	/**
	 * 描述：获取文件所在的目录项节点
	 * 返回值： FolderNode实例
	 * 参数： pathName 文件所在路径节点
	 */
	private FolderNode getFolderNode(String pathName) {
		
		//1. 获取文件工作目录对应
		int diskNum = pathSearch(pathName);
		if(diskNum == -1) {System.out.println("文件路径为空"); return null;}
		if(diskNum != 0) {System.out.println("文件不存在"); return null; }
		//2. 获取文件名
		int length = pathName.split("/").length;
		String fileName = pathName.split("/")[length - 1];
		int i = 0;
		for(; i < 8; i++) {
					
			if(fileName.equals(disks[diskNum].getFolderNode()[i].getNodePathName())) {
				return disks[diskNum].getFolderNode()[i];
			}
		}
				
		return null; 
	}

	
	/**
	 * 描述：将指定已打开文件项从磁盘中移出
	 * 返回值： 移出成功，则返回true, 否则返回false
	 * 参数： beginFileDisk 文件起始磁盘号
	 */
	private boolean removeOpenFile(int beginFileDisk) {
		
		Iterator<OpenFile> it = openFiles.iterator();
		while(it.hasNext()) {
			
			OpenFile openFile = (OpenFile) it.next();
			if((int)openFile.getDiskNumber() == beginFileDisk) {
				
				return true;
			}
		}
		
		return false;
	}

	/**
	 * 描述： 将文件的登记目录项从目录中删除
	 * 返回值：删除成功，则返回true, 否则返回false
	 * 参数： pathName 文件路径名
	 */
	private boolean deleFolderNode(String pathName) {
		
		//1. 获取文件工作目录对应
				int diskNum = pathSearch(pathName);
				if(diskNum == -1) {System.out.println("文件路径为空"); return false;}
				if(diskNum != 0) {System.out.println("文件不存在"); return false; }
				//2. 获取文件名
				int length = pathName.split("/").length;
				String fileName = pathName.split("/")[length - 1];
				int i = 0;
				for(; i < 8; i++) {
						//3.获取文件属性，验证文件打开方式	
					if(fileName.equals(disks[diskNum].getFolderNode()[i].getNodePathName())) {
						
							//删除文件在目录中的登记项
						disks[diskNum].getFolderNode()[i] = new FolderNode();
						return true; 
						
					}
				}
				//删除失败
				return false;
	}
}
