package com.mtkp.multitool.data.mapper;

import com.mtkp.multitool.data.local.CachedCategoryEntity;
import com.mtkp.multitool.data.local.CachedExtensionEntity;
import com.mtkp.multitool.data.remote.dto.CategoryDto;
import com.mtkp.multitool.data.remote.dto.ExtensionDto;

import java.util.ArrayList;
import java.util.List;

/**
 * Маппер для преобразования DTO (из сети) в Entity (локальное хранилище).
 * Отделяет логику трансформации от бизнес-логики.
 */
public class DtoToEntityMapper {

    /**
     * Преобразовать ExtensionDto в CachedExtensionEntity.
     */
    public static CachedExtensionEntity mapExtensionDtoToEntity(ExtensionDto dto) {
        if (dto == null) {
            return null;
        }
        CachedExtensionEntity entity = new CachedExtensionEntity();
        entity.id = dto.id;
        entity.name = dto.name != null ? dto.name : "";
        entity.shortDescription = dto.shortDescription;
        entity.extensionPath = dto.extensionPath;
        entity.categoriesCsv = dto.categories == null ? "" : String.join(",", dto.categories);
        entity.version = dto.version;
        entity.downloads = dto.downloads;
        entity.rating = dto.rating;
        entity.badge = dto.badge;
        entity.updatedAt = dto.updatedAt;
        return entity;
    }

    /**
     * Преобразовать список ExtensionDto в список CachedExtensionEntity.
     */
    public static List<CachedExtensionEntity> mapExtensionListDtoToEntity(List<ExtensionDto> dtoList) {
        List<CachedExtensionEntity> entityList = new ArrayList<>();
        if (dtoList != null) {
            for (ExtensionDto dto : dtoList) {
                CachedExtensionEntity entity = mapExtensionDtoToEntity(dto);
                if (entity != null) {
                    entityList.add(entity);
                }
            }
        }
        return entityList;
    }

    /**
     * Преобразовать CategoryDto в CachedCategoryEntity.
     */
    public static CachedCategoryEntity mapCategoryDtoToEntity(CategoryDto dto) {
        if (dto == null) {
            return null;
        }
        CachedCategoryEntity entity = new CachedCategoryEntity();
        entity.id = dto.id;
        entity.name = dto.name;
        entity.displayName = dto.displayName;
        entity.description = dto.description;
        return entity;
    }

    /**
     * Преобразовать список CategoryDto в список CachedCategoryEntity.
     */
    public static List<CachedCategoryEntity> mapCategoryListDtoToEntity(List<CategoryDto> dtoList) {
        List<CachedCategoryEntity> entityList = new ArrayList<>();
        if (dtoList != null) {
            for (CategoryDto dto : dtoList) {
                CachedCategoryEntity entity = mapCategoryDtoToEntity(dto);
                if (entity != null) {
                    entityList.add(entity);
                }
            }
        }
        return entityList;
    }
}

