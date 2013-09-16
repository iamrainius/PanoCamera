package com.jingz.app.pano.storage;

public class StorageManagerFactory {
	public static StorageManager getStorageManager() {
		return new LocalFileStorageManager();
	}
}
