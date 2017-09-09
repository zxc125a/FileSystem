package model;
/**
 * 文件操作系统： 
 * 	初始化FAT表，共128项，占128个字节，2个磁盘物理块
 * 		1）将前两个物理块的容量用于存储FAT表,则有fat[0] = -1， fat[1] = -1
 * 		2）第三个物理块用于存储系统数据，即fat[2] = -1
 * 		3）其余初始化为空闲快，即fat[n] = 0 (2 < n < 128)
 */
public class FAT {

	public  static FAT fat;
	private int[] item;
	
	public FAT() {
		
		item = new int[128];
		item[0] = -1;
		item[1] = -1;
		item[2] = -1;
		item[3] = -1;
		for(int i = 4; i < 128; i++) {
			
			item[i] = 0;
		}
	}
	
	
	/**
	 * 
	 */
	public static FAT getInstance() {
		
		if(fat == null) {
			fat = new FAT();
		}
		
		return fat;
	}
	
	/**
	 * 遍历fat表，查找空闲的磁盘盘块，如果查找成功，返回其编号，否则返回-1；
	 */
	public int getFreeDisk() {
		
		for(int i = 3; i < 128; i++) {
			
			if(item[i] == 0) {
				return i;
			}
		}
		return 0;
	}
	
	/**
	 * 根据给定文件磁盘盘块索引，返回下一块磁盘盘块索引;
	 * @param diskIndex 文件起始盘块编号
	 * @return   文件下一块盘块编号
	 */
	public int nextDiskIndex(int diskIndex) throws ArrayIndexOutOfBoundsException{
		
		if(diskIndex < 3 || diskIndex > 127) {
			throw new ArrayIndexOutOfBoundsException(diskIndex);
		}
		return item[diskIndex];
	}
	
	/**
	 * 描述：FAT表根据文件起始磁盘块号，获取文件占据磁块长度
	 * 返回值： 文件存在，则返回文件长度，否则返回-1
	 * 参数： beginDiskNum 文件起始磁盘块号 
	 */
	public int getFileLength(int beginDiskNum) {
		
		if(item[beginDiskNum] == 0) {
			
			System.out.println("文件起始磁盘块号错误！！！");
			return -1;
		}
		int len = 0;
		while(beginDiskNum != -1) {
			
			len++;
			beginDiskNum = item[beginDiskNum];
		}
		
		return len;
	}
	
	/**
	 * 描述： 获取所有空闲磁盘块块数
	 * 返回值： 返回空闲磁盘块块数
	 * 参数： 无
	 */
	public int getFreeDiskNum() {
		
		int n = 0;
		for(int i = 3; i < 128; i++) {
			
			if(item[i] == 0) n++;
		}
		
		return n;
	}
	
	public int[] getItem() {
		return item;
	}

	
	
//---------------------------------------------------------------------------	



}
