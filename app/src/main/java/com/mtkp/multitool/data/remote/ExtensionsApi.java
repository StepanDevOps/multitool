package com.mtkp.multitool.data.remote;

import com.mtkp.multitool.data.remote.dto.ExtensionDto;

import java.util.List;

/**
 * Простой интерфейс API для получения данных о расширениях.
 *
 * Примечание: это не привязано к конкретной реализации сети — позже можно
 * подключить Retrofit/OkHttp и реализовать методы в RemoteDataSource.
 */
public interface ExtensionsApi {

    /**
     * Получить список расширений (страница/пагинация при необходимости).
     * Реализация должна быть асинхронной (в репозитории мы её вызовем в фоне).
     */
    List<ExtensionDto> fetchExtensions(int page, int perPage) throws Exception;

    /**
     * Получить одну запись расширения по id.
     */
    ExtensionDto fetchExtensionById(int id) throws Exception;
}

