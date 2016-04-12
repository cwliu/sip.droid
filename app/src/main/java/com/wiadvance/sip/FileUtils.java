package com.wiadvance.sip;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {
	public static void copyIfNotExist(Context context, int ressourceId, String target) throws IOException {
		File lFileToCopy = new File(target);
		if (!lFileToCopy.exists()) {
			copyFromPackage(context, ressourceId, lFileToCopy.getName()); 
		}
	}

	public static void copyFromPackage(Context context, int ressourceId, String target) throws IOException {
		FileOutputStream lOutputStream = context.openFileOutput (target, 0);
		InputStream lInputStream = context.getResources().openRawResource(ressourceId);
		int readByte;
		byte[] buff = new byte[8048];
		while (( readByte = lInputStream.read(buff)) != -1) {
			lOutputStream.write(buff,0, readByte);
		}
		lOutputStream.flush();
		lOutputStream.close();
		lInputStream.close();
	}
}
