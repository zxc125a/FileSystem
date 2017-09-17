package model;

/**
 * �Ǽ��ļ�/Ŀ¼�õ�Ŀ¼����а���
 * 	1.�ڵ�·�����ļ�����Ŀ¼���ڵ�·��
 * 	2.�ڵ����ͣ�������ļ������ʾ�ļ����ͣ� �����Ŀ¼�������������
 * 	3.�ڵ����ԣ����������ļ���Ŀ¼
 * 	4.�ڵ���ʼ�̺ţ� ���ݴ洢�ĵ�һ���̿��
 * 	5.���ȣ� ռ���̿����Ŀ
 */
public class FolderNode {
	private String nodePathName;  //3 ���ֽڣ��ڵ�·��
	private String nodeType;   // 2���ֽڣ��ڵ�����
	private int  nodeAttritute; //1���ֽڣ� �ڵ�����
	private int  nodeBeginDisk; //1���ֽڣ��ڵ���ʼ�̺�
	private int  nodeLength;  //1���ֽڣ�����
    
	
    public FolderNode() {
    	
    	nodeAttritute = 0;   //��ʾ�õǼ���δ��ʹ��
    	nodeBeginDisk = 0;   //��ʾ�õǼ���δ��ʹ��
    }
    /**
     * ����������һ��Ŀ¼�Ǽ������ʼ��һЩ��Ϣ
     * ����ֵ�� ����ʵ��
     * @param attribute  Ŀ¼�����ԣ�������ļ�����Ŀ¼
     * @param nodePathName  �ļ���
     * @param nodeBeginDisk �ļ���ʼ���̺�
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
