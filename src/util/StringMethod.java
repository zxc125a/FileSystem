package util;

public class StringMethod {
	/**
	 * �ж��ַ�������str�Ƿ�Ϊ�ն������������Ϊ0
	 * ���ǣ��򷵻�true, ���򷵻�false;
	 */
	public static boolean isEmpty(String str) {
		
		if(str == "" || str == null) {
			
			return true;
		}
		return false;
	}
}
