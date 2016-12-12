package com.xdja.android.util;

/**
 * 用于生成各种对话框的工具类
 * @author zlw
 *
 */
public class DialogUtil {
	private volatile static DialogUtil mInstance;
	
	private DialogUtil(){
		
	}
	
	public DialogUtil getInstance() {
		if (mInstance == null) {
			synchronized (DialogUtil.class) {
				if (mInstance == null) {
					mInstance = new DialogUtil();
				}
			}
		}
		
		return mInstance;
	}
	
	public void show() {
		
	}
}
