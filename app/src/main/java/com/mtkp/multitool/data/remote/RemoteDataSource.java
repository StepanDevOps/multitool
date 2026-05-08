package com.mtkp.multitool.data.remote;

import com.mtkp.multitool.data.remote.dto.ExtensionDto;

import java.util.ArrayList;
import java.util.List;

/**
 * Заглушка для удалённого источника данных.
 * Пока реализована простая синхронная заглушка — позже сюда можно подключить Retrofit.
 */
public class RemoteDataSource implements ExtensionsApi {

    public RemoteDataSource() {
        // TODO: инициализация сети (Retrofit/OkHttp) — пока заглушка
    }

    @Override
    public List<ExtensionDto> fetchExtensions(int page, int perPage) throws Exception {
        // TODO: сделать реальный сетевой запрос. Сейчас возвращаем пустой список для разработки.
        return new ArrayList<>();
    }

    @Override
    public ExtensionDto fetchExtensionById(int id) throws Exception {
        // TODO: реальная загрузка расширения
        return null;
    }
}

