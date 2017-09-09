package model;

/**
 * 登记文件/目录用的目录项，其中包括
 * 	1.节点路径：文件或者目录所在的路径
 * 	2.节点类型：如果是文件，则表示文件类型， 如果是目录，则留两格个空
 * 	3.节点属性：用于区分文件和目录
 * 	4.节点起始盘号： 数据存储的第一个盘块号
 * 	5.长度： 占用盘块的数目
 */
public class FolderNode {
	private String nodePathName;  //3 个字节，节点路径
	private String nodeType;   // 2个字节，节点类型
	private int  nodeAttritute; //1个字节， 节点属性
	private int  nodeBeginDisk; //1个字节，节点起始盘号
	private int  nodeLength;  //1个字节，长度
    
	
    public FolderNode() {
    	
    	nodeAttritute = 0;   //表示该登记项未被使用
    	nodeBeginDisk = 0;   //表示该登记项未被使用
    }
    /**
     * 描述：创建一个目录登记项，并初始化一些信息
     * 返回值： 对象实例
     * @param attribute  目录项属性，辨别是文件还是目录
     * @param nodePathName  文件名
     * @param nodeBeginDisk 文件起始磁盘号
     */
    public FolderNode(int attribute,String nodePathName,int  nodeBeginDisk) {
    	
    	this.nodeAttritute = attribute;
    	this.nodePathName = nodePathName;
    	this.nodeBeginDisk = nodeBeginDisk;
    	this.nodeLength = 1;
    }

    /**
     *
     */
    public void initFolderNode(String nodePathName, String nodeType, int  nodeAttritute, 
    		int  nodeBeginDisk, int  nodeLength) {
    	
    	this.nodePathName = nodePathName;
    	this.nodeType = new String(nodeType);
    	this.nodeAttritute = nodeAttritute;
    	this.nodeBeginDisk = nodeBeginDisk;
    	this.nodeLength = nodeLength;
    }
    
	public String getNodePathName() {
		return nodePathName;
	}

	public void setNodePathName(String nodePathName) {
		this.nodePathName = nodePathName;
	}

	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	public int getNodeAttritute() {
		return nodeAttritute;
	}

	public void setNodeAttritute(int nodeAttritute) {
		this.nodeAttritute = nodeAttritute;
	}

	public int getNodeBeginDisk() {
		return nodeBeginDisk;
	}

	public void setNodeBeginDisk(int nodeBeginDisk) {
		this.nodeBeginDisk = nodeBeginDisk;
	}

	public int getNodeLength() {
		return nodeLength;
	}

	public void setNodeLength(int nodeLength) {
		this.nodeLength = nodeLength;
	}
    
    
}
