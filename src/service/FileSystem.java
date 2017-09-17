package service;
import model.*;
import sun.security.jca.GetInstance;
import jdk.internal.org.objectweb.asm.tree.IntInsnNode;

import java.util.*;

import com.sun.media.sound.PortMixerProvider;

import model.*;
import util.*;

public class FileSystem {
	
	private FAT fat; //fat��
	private ArrayList<OpenFile> openFiles;//�Ѵ��ļ��б�
	private Disk[] disks = new Disk[128]; //���������̿�
	private static FileSystem fileSystem;
	public static final int FOLDER_VALUE = 12;
	public static final int FILE_VALUE = 3;
	public static final int SYSTEM_VALUE = 1;
	
	public FileSystem() {
		
		//��ʼ�� FAT ��
		fat = FAT.getInstance();
		//��ʼ���Ѵ��ļ��б�
		openFiles = new ArrayList<OpenFile>();
		//Ĭ�ϴ򿪸�Ŀ¼
	    OpenFile openFile = initOpenFile(2, "/");
	    openFiles.add(openFile);
		//Ϊÿһ���̿����ռ�
		for(int i = 0; i < 128; i++) {
			
			disks[i] = new Disk();
		}
	}
	
	
	/**
	 * ������ ȫ�ֶԵ�һFileSystemʵ�����й���Ͳ���
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
		//�½��ļ�
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
	 * �������½��ļ�/Ŀ¼��������
	 * ����ֵ���½����ļ���Ŀ¼���ļ�Ŀ¼�Ǽ����������Ϣ, �½�ʧ�ܣ��򷵻�null
	 * ������ pathName �ļ�·��
	 */
	public FolderNode createFile(String pathName) {
		
		//1. ����ļ�Ŀ¼��ȷ���������ļ��Ϳ��ļ���
		int folderIndex =  pathSearch(pathName);
		if(folderIndex == -1) {System.out.println("����·������"); return null;}
		if(folderIndex == 0)  {System.out.println("����������ͬ���ļ�"); return null;}
		//��ȡ�ļ�����
		String[] pathArray = pathName.split("/");
		String fileName = new String(pathArray[pathArray.length - 1]);
		//2. �����½��ļ���Ѱ�ҿ��еǼ���
		int n = Tool.remaindedCapacity(disks, folderIndex);
		if(n == -1) {
			
			System.out.println("�ļ�Ŀ¼�Ǽ����Ѵ����ֵ");
			return null;
		}
		//3. ��ȡ���п��еĴ��̿�
		int totalFreeDisk = fat.getFreeDiskNum();
		if(totalFreeDisk == 0) {
			
			System.out.println("�����ļ�ʧ�ܣ����̿ռ䲻��");
			return null;
		}
		//4. �ҵ�һ�����д��̿�,��������̿�����½������ļ�
		int freeDisk = fat.getFreeDisk();
		
		fat.getItem()[freeDisk] = -1;
		//5. �ڹ���Ŀ¼�У��Ǽ��½������ļ�/Ŀ¼
		int nodeAttritute = 0;
		if(pathName.endsWith(".txt")) {
			
			nodeAttritute = FILE_VALUE;   //����һ���ɶ���д���ļ�
		} else {
		
			nodeAttritute = FOLDER_VALUE;    //����һ���ɶ���д��Ŀ¼
		}
		
		disks[folderIndex].getFolderNode()[n].initFolderNode(fileName, "  ", nodeAttritute, freeDisk, 1);
		if(nodeAttritute == FILE_VALUE) {
			
			System.out.println("�����ļ��ɹ�");
		} else {
			
			System.out.println("����Ŀ¼�ɹ�");
		}
		
		return disks[folderIndex].getFolderNode()[n];
	}
	
	/**
	 *������ʵ�ִ��ļ����������Ӵ����ж�ȡ�ļ�����
	 *����ֵ�� ��
	 *������ pathName : �ļ�·����
	 */
	public void openFile(String pathName) {
		
		//1. ����Ҫ�����ļ��Ƿ����
		int folderIndex  = pathSearch(pathName);
		if(folderIndex != 0 && folderIndex != 2)  {System.out.println("���ļ�ʧ�ܣ��ļ�·������!!!"); return;}
		//2. ����ļ����ڣ���Ҫ���򿪷�ʽ��ȷ��������д��ʽ��ֻ���ļ�
		//
		//3. �����д�Ѵ��ļ������ļ��Ѿ��򿪻��߳������ļ�������������Ҫ��д�Ѵ��ļ���
	    if(openFiles.size() > 5) {System.out.println("���ļ�ʧ�ܣ��ļ�����������"); return;} //�ļ��������ܳ���5
	  
	    //4.�ļ��Ƿ��Ѿ���,δ�򿪹�������Ҫ��ӵ��Ѵ��ļ��б���
	    int beginDiskIndex = getBeginDiskNum(pathName);//��ȡ�ļ�����ʼ���̿��
	    //������ļ�������Ҫ�жϸ��ļ��Ƿ��Ѿ��� 
	    if(beginDiskIndex == 0) {System.out.println("���ļ�ʧ�ܣ��ļ�·������!!!"); return; }
	    if(pathName.endsWith(".txt")) {
	    	
		    if(!judgeOpenFile(pathName)) {
		    	
			    // ���Ѵ��ļ���������µ��ļ�
			    OpenFile openFile = initOpenFile(beginDiskIndex, pathName);
			    openFiles.add(openFile);
		    }
	    }

	    //5. ��ȡ�ļ����ݣ������
	    if(pathName.endsWith(".txt")) {
		     //��ʾ�ļ�����
	    	StringBuilder sbBuilder  = new StringBuilder(); //������ʼ�����̿�ţ���ȡ�ļ����ݵ���������
		    sbBuilder.append(getFileContent(beginDiskIndex));
		    //����ļ�����
		    System.out.println(sbBuilder.toString());
		    System.out.println("���ļ��ɹ�������");
		    return;
	    } else {
	    	//��ʾĿ¼����
	    	System.out.println("��Ŀ¼�ɹ�������");
	    }

	}

	/**
	 * ������ �ر��ļ�
	 * ����ֵ����
	 * ������pathName �ļ�·��
	 */
	public void closeFile(String pathName, String content) {
		
		//1. ����Ҫ�����ļ��Ƿ�򿪣����û�д򿪣��Ͳ��ùر�
	    int beginDiskIndex = getBeginDiskNum(pathName);  //��ȡ�ļ�����ʼ���̿��
	    if(!judgeOpenFile(pathName)) {System.out.println("�ر�ʧ�ܣ��ļ�δ��"); return;}
	    //2. ���򿪷�ʽ�������д��ʽ�򿪵ģ�Ҫ׷���ļ����ݺ��ļ�������������Ƕ���ʽ�򿪵ģ����ñ�������;
	    if(checkFileAttribute(pathName) == 0) {return;} //���򿪷�ʽ�����ô洢����
	    //3. д�򿪷�ʽ,�洢����
	    int len = storeIntoDisk(pathName, content);
	    //4.�޸�Ŀ¼���
	    FolderNode folderNode = getFolderNode(pathName);
	    if(folderNode == null) {System.out.println("��ȡĿ¼�Ǽ���ʧ�ܣ�����");  return;}
	    folderNode.setNodeLength((int)len);
	    //5.���Ѵ��ļ�����ɾ����Ӧ��
	    boolean flag = removeOpenFile(beginDiskIndex);
	    if(!flag) {
	    
	    	System.out.println("�Ѵ��ļ������ʧ��");
	    } else {
	    	
	    	System.out.println("�ļ��رճɹ�");
	    }
		return;
	}
	
	/**
	 * ������ ɾ���ļ�
	 * ����ֵ��ɾ���ɹ����򷵻�true, ���򷵻�false
	 * ������ pathName  �ļ�·�� 
	 */
	public void deleFile(String pathName) {
		
		//1. ��֤Ҫɾ�����ļ��Ƿ����
		int beginDisk = getBeginDiskNum(pathName);
		if(beginDisk == -1) {System.out.println("·������Ϊ��"); return;}
		if(beginDisk == 0) {System.out.println("�ļ�������"); return;}
		//2. ��֤�ļ��Ƿ��
		if(judgeOpenFile(pathName)) {System.out.println("�ļ��Ѿ��򿪣�����ɾ��"); return;}
		//3. ɾ��Ŀ¼�е�Ŀ¼��
		if(!deleFolderNode(pathName)) {System.out.println("�ļ���Ŀ¼�еǼ���ɾ��ʧ��"); return;}
		//4. �ͷ��ļ����̿ռ�
		if(!freeDiskCapacity(beginDisk)) {System.out.println("�ļ����̿ռ��ͷ�ʧ��"); return;}
		System.out.println("�ļ�ɾ���ɹ�������");
		return;
	}

	
//--------------------------------------------------------------------------------------//
	
	/**
	 * �����������ļ�·��������ȡ�ļ�����ʼ���̿��
	 * ����ֵ����·�����򷵻�2,�ļ��������򷵻�0,���򷵻��ļ��Ĵ����̿��
	 * ������pathName���ļ�·����
	 */
	private int getBeginDiskNum(String pathName) {
		
		//���ظ�Ŀ¼���ڵĴ��̿�
		if("/".equals(pathName)) {
			
			return 2;
		}
		int diskNum = pathSearch(pathName);
		if(diskNum != 0) {return 0;} //�ļ�·������
		//1. ��ȡ�ļ�����Ŀ¼��Ӧ
		int length = pathName.split("/").length;
		String fileName = pathName.split("/")[length - 1];
		pathName += ".";
		diskNum = pathSearch(pathName);  //�ļ��������ڣ����ظ��ļ��Ĺ���Ŀ¼���ڵĴ��̿��
		//2. ��ȡ�ļ���
		int i = 0;
		for(; i < 8; i++) {
			
			
			if(fileName.equals(disks[diskNum].getFolderNode()[i].getNodePathName())) {
				
				return disks[diskNum].getFolderNode()[i].getNodeBeginDisk();
			}
		}
		
		return 0;
	} 
	
	
	/**
	 * �����������ļ�·���������ظ��ļ����ڹ���Ŀ¼�Ĵ��̿��
	 * ����ֵ���������ļ���Ӧ·�����򷵻� -1�����ҵ�ͬ·�����ļ����򷵻�0������2����ʾ���ظ�Ŀ¼�Ĵ��̿�ţ����򷵻��ļ�����Ŀ¼���ڵĴ����̿��
	 * 
	 * ������pathName���ļ�·����
	 */
	private int pathSearch(String pathName) {
		
		//���ظ�Ŀ¼���ڵĴ��̿�
		if("/".equals(pathName)) {
			
			return 2;
		}
		//1. ��ȡ�ļ���
		String[] pathArray = pathName.split("/");
		String fileName = new String(pathArray[pathArray.length - 1]);
		//���ļ���Ϊ�գ��򷵻�-1��
		if(StringMethod.isEmpty(pathName)) {
			
			return -1;
		}
		//2. �Ӹ�Ŀ¼��ʼ����
		int folderIndex = 2;	 //��ǰĿ¼���ڴ����̿�ı�ţ� 2��ʾ��Ŀ¼���ڵĴ��̺�
		boolean flag = false;    //true ��ʾ���������ļ���false��ʾ�����������ļ�
		//3. �����ļ�����
		int type = 1;            //type == 1 ��ʾĿ¼, type == 0 ��ʾ�ļ�
		//4. ��������·������Ŀ¼
		for(int i = 1; i < pathArray.length; i++) {
			
			//�ж����һ����Ŀ¼���ļ�����Ŀ¼
			if(i == pathArray.length - 1) {
				
				if(fileName.endsWith(".txt")) 
					
					type = 0;   //�ļ�
			} else {
				
					type = 1;     //Ŀ¼
			}
			
			//1. �Ӵ��̿���ȡ��Ŀ¼�е�Ŀ¼��
			FolderNode[] folderNodes = Arrays.copyOf(disks[folderIndex].getFolderNode(), disks[folderIndex].getFolderNode().length);
				//2. ������ǰĿ¼�µ�����Ŀ¼��
			int j = 0;
			for(; j < 8; j++) {
				
				int k = folderNodes[j].getNodeBeginDisk();
				//k != 0 ��ʾ��ǰ�����̿��ѱ�ʹ��,��������Ϊ�ļ����ݻ�����Ŀ¼
				if(k != 0) {
					//3.ȡ���ô����̿����ݣ�������Ŀ¼�Ǽ��������ж��Ƿ�Ϊ�ļ����ͣ�
					if( (folderNodes[j].getNodeAttritute() & 8) == type * 8) { //�ֽ�Ϊ 8��00001000������ʾ��Ŀ¼��һ��Ŀ¼�ĵǼ���
						//4. �Ƚ���Ŀ¼��
						if(pathArray[i].equals(folderNodes[j].getNodePathName())) {
								
							if(i == pathArray.length - 1) {
								
								flag = true;    
								break;
							}
							//���µ�ǰ��Ŀ¼�µ���ʼ�̺�
							folderIndex = k;
							break;
						}
					}
				}
			}
			if(j >= 8 && i < pathArray.length - 1) {
				
				return -1;    //����·������ȴ���Ŀ¼ʵ�����
			}
		}

		if(!flag) {
			return folderIndex;
		} else {
			return 0;
		}
	} 
	
	
	/**
	 * �����������ļ���ʼ�̿�ţ������ļ�������
	 * ����ֵ�������ļ�����
	 * ������diskNum �ļ���ʼ�̿��
	 */
	public String getFileContent(int diskNum) {
		
		//1. ���û�����
		String sBuffer = new String();
		String str = null;
		//2. ������ʼ�̿�ţ���ȡ�������ļ�������
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
				System.out.println("FAT�����Խ�磡����");
				System.exit(0);
			}
			
		}
		
		return sBuffer;
	} 
	
	
	/**
	 * ��������֤����·�����ļ��Ƿ��Ѿ����򿪣�
	 *����ֵ���򿪱����� true, ���򷵻� false;
	 *������beginDiskIndex���ļ���ʼ�̿�ţ�pathName���ļ����ڵ�·��
	 */
	private boolean judgeOpenFile(String pathName) {
		
		//1. ��ȡ�ļ���ʼ�̿��
	    int beginDiskIndex = getBeginDiskNum(pathName);
	    //2. ��ȡ�ļ���
	    String[] strArray = pathName.split("/");
	    String fileName = strArray[strArray.length - 1];
	    Iterator<OpenFile> it = openFiles.iterator();
	    while(it.hasNext()) {
	    	
	    	OpenFile openFile = it.next();
	    	if(openFile.getDiskNumber() == beginDiskIndex && 
	    			openFile.getFileName().equals(fileName)) {
	    		
	    		System.out.println("�ļ��Ѿ�����");
	    		return true;
	    	}
	    }
	    return false;
	    
	}
	
	
	/**
	 * ������ Ϊ�Ѵ��ļ��½�һ��OpenFile���󣬲�Ϊ֮��ʼ��
	 * ����ֵ�� ��ʼ�����OpenFileʵ��
	 * ������
	 */
	private OpenFile initOpenFile(int diskNum, String pathName) {
		
		//1. �����ļ�·������ȡ�ļ���
	    String[] strArray = pathName.split("/");
	    String fileName = null;
	    if(strArray.length > 1)
	    	fileName = strArray[strArray.length - 1];
	    //2. ��ȡ�ļ�����
		int nodeAttritute = 0;
		if(pathName.endsWith(".txt")) {
			
			nodeAttritute = FOLDER_VALUE;   //����һ���ɶ���д��Ŀ¼
		} else {
			nodeAttritute = FILE_VALUE;    //����һ���ɶ���д���ļ�
		}
		//3. ��ȡ��ʼ���̺�
		int beginDiskNum = diskNum;
		//4. ��ȡ�ļ�����
		int length = fat.getFileLength(beginDiskNum);
		//5. �������ļ�Ĭ�����ͣ�'1'Ϊ��д
		int flag = 1;
		//6. ���ö�ָ��
		Pointer read = new Pointer();
		read.setBnum(beginDiskNum);
		read.setDnum(beginDiskNum + length - 1);
		//7. ����дָ��
		Pointer write = new Pointer();
		write.setBnum(beginDiskNum + length - 1);
		write.setDnum(beginDiskNum + length - 1);
		//8. ��ʼ��OpenFileʵ������
		OpenFile openFile = new OpenFile(fileName, nodeAttritute, beginDiskNum, length, flag, read, write);
		
		return openFile;
		
	}


	/**
	 * ����������ļ��Ĵ򿪸�ʽ
	 * ����ֵ�� 1��ʾ�ɶ���д��ʽ�� 0��ʾ����ʽ, -1��ʾ�ļ�������
	 * ������pathName �ļ�·����
	 */
	private int checkFileAttribute(String pathName) {
		
		//1. ��ȡ�ļ�����Ŀ¼��Ӧ
		int diskNum = pathSearch(pathName);
		if(diskNum == -1) {System.out.println("�ļ�·��Ϊ��"); return -1;}
		if(diskNum != 0) {System.out.println("�ļ�������"); return -1; }
		//2. ��ȡ�ļ���
		int length = pathName.split("/").length;
		String fileName = pathName.split("/")[length - 1];
		int i = 0;
		for(; i < 8; i++) {
				//3.��ȡ�ļ����ԣ���֤�ļ��򿪷�ʽ	
			if(fileName.equals(disks[diskNum].getFolderNode()[i].getNodePathName())) {
				
				int attribute = disks[diskNum].getFolderNode()[i].getNodeAttritute();
				if((attribute & 8) == 0 &&  (attribute & 1) == 1) {
					//���ض��򿪷�ʽ
					return 0; 
				}
			}
		}
		//����д�򿪷�ʽ
		return 1;
	}
	
	
	/**
	 * ���������ļ����ݣ��洢�������У�������ռ�ô����̿������
	 * ����ֵ���ļ�ռ�ô����̿������,�洢ʧ�ܣ�����0
	 * ������pathName ·����
	 *     content �ļ�����
	 */
	private int storeIntoDisk(String pathName, String content) {
		
		int beginDiskNum = getBeginDiskNum(pathName);
		if(beginDiskNum == 0 || beginDiskNum == -1) {
			
			System.out.println("�ļ�·�����󣬴洢ʧ��");
			return 0;
		}
		
		//1. ���д��̿����Ƿ�����
		int length = (int)(Math.ceil((content.length() % 64))); //�ļ���������Ҫ�洢�Ĵ����̿���
		int size = fat.getFreeDiskNum();
		if(length > size) {System.out.println("���̿ռ䲻�㣡����"); return 0;}
		//2. �ͷ��ļ�ԭ���̿ռ�
	    boolean flag = freeDiskCapacity(beginDiskNum); //�ͷ��ļ�ԭ�д��̿ռ�
	    if(!flag) {System.out.println("���̿ռ��ͷ�ʧ��"); return 0;}
		//3. ���ļ������µĴ��̿ռ�
	    int i = 0;
		for(; i < length - 1; i++) {
			
			int nextDiskNum = fat.getFreeDisk();
			disks[nextDiskNum].setContent(new String(content.substring(64 * i, 64 * ( i + 1) - 1)));
			fat.getItem()[beginDiskNum] = nextDiskNum;
			beginDiskNum = nextDiskNum;
		}
		//Ϊ���һ������̿����ռ�
		int nextDiskNum = fat.getFreeDisk();
		disks[nextDiskNum].setContent(new String(content.substring(64 * i, content.length() - 1)));
		fat.getItem()[beginDiskNum] = nextDiskNum;
		fat.getItem()[nextDiskNum] = -1;
		return length;
	}


	/**
	 * ������ �ͷ��ļ���ռ�õĴ��̿ռ�
	 * ����ֵ�� �ͷųɹ��򷵻�true, ���򷵻�false;
	 * ������ beginDiskNum�ļ���ʼ�̿���
	 */
	private boolean freeDiskCapacity(int beginDiskNum) {
		
		if(beginDiskNum < 3 || beginDiskNum > 127) {
		
			System.out.println("�̿��Խ�磬���̿ռ��ͷ�ʧ��");
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
	 * ��������ȡ�ļ����ڵ�Ŀ¼��ڵ�
	 * ����ֵ�� FolderNodeʵ��
	 * ������ pathName �ļ�����·���ڵ�
	 */
	private FolderNode getFolderNode(String pathName) {
		
		//1. ��ȡ�ļ�����Ŀ¼��Ӧ
		int diskNum = pathSearch(pathName);
		if(diskNum == -1) {System.out.println("�ļ�·��Ϊ��"); return null;}
		if(diskNum != 0) {System.out.println("�ļ�������"); return null; }
		//2. ��ȡ�ļ���
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
	 * ��������ָ���Ѵ��ļ���Ӵ������Ƴ�
	 * ����ֵ�� �Ƴ��ɹ����򷵻�true, ���򷵻�false
	 * ������ beginFileDisk �ļ���ʼ���̺�
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
	 * ������ ���ļ��ĵǼ�Ŀ¼���Ŀ¼��ɾ��
	 * ����ֵ��ɾ���ɹ����򷵻�true, ���򷵻�false
	 * ������ pathName �ļ�·����
	 */
	private boolean deleFolderNode(String pathName) {
		
		//1. ��ȡ�ļ�����Ŀ¼��Ӧ
				int diskNum = pathSearch(pathName);
				if(diskNum == -1) {System.out.println("�ļ�·��Ϊ��"); return false;}
				if(diskNum != 0) {System.out.println("�ļ�������"); return false; }
				//2. ��ȡ�ļ���
				int length = pathName.split("/").length;
				String fileName = pathName.split("/")[length - 1];
				int i = 0;
				for(; i < 8; i++) {
						//3.��ȡ�ļ����ԣ���֤�ļ��򿪷�ʽ	
					if(fileName.equals(disks[diskNum].getFolderNode()[i].getNodePathName())) {
						
							//ɾ���ļ���Ŀ¼�еĵǼ���
						disks[diskNum].getFolderNode()[i] = new FolderNode();
						return true; 
						
					}
				}
				//ɾ��ʧ��
				return false;
	}
}
