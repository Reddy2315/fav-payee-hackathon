package com.hackathon.favoritepayee.mapper;

import com.hackathon.favoritepayee.dto.FavoriteAccountRequest;
import com.hackathon.favoritepayee.dto.FavoriteAccountResponse;
import com.hackathon.favoritepayee.entity.FavoriteAccount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FavoriteAccountMapper {

    FavoriteAccount toEntity(FavoriteAccountRequest request);

    FavoriteAccountResponse toResponse(FavoriteAccount entity);

    List<FavoriteAccountResponse> toResponseList(List<FavoriteAccount> entities);
}
