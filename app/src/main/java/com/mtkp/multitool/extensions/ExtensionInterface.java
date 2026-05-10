package com.mtkp.multitool.extensions;

import android.content.Context;

// Правила для всех расширений (плагинов)
public interface ExtensionInterface {

	int HOST_API_VERSION = 1;

	/**
	 * Стабильный id расширения, который совпадает с id в каталоге/сервере.
	 */
	String getExtensionId();

	/**
	 * Имя для отображения в логах и отладке.
	 */
	String getDisplayName();

	/**
	 * Версия API хоста, которую требует расширение.
	 */
	int getRequiredApiVersion();

	/**
	 * Вызывается после успешной загрузки расширения.
	 */
	void onLoad(Context context, ExtensionHostApi hostApi);

	/**
	 * Вызывается перед выгрузкой расширения.
	 */
	void onUnload();
}
