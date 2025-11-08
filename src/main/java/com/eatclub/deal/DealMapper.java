package com.eatclub.deal;

import com.eatclub.deal.DealRepository.Deal;
import com.eatclub.deal.DealRepository.Restaurant;
import com.eatclub.deal.DealService.ActiveDeal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DealMapper {
    @Mapping(target = "restaurantObjectId", source = "restaurant.objectId")
    @Mapping(target = "restaurantName", source = "restaurant.name")
    @Mapping(target = "restaurantAddress1", source = "restaurant.address1")
    @Mapping(target = "restaurantSuburb", source = "restaurant.suburb")
    @Mapping(target = "restaurantOpen", source = "restaurant.open")
    @Mapping(target = "restaurantClose", source = "restaurant.close")
    @Mapping(target = "dealObjectId", source = "deal.objectId")
    @Mapping(target = "discount", source = "deal.discount")
    @Mapping(target = "dineIn", source = "deal.dineIn")
    @Mapping(target = "lightning", source = "deal.lightning")
    ActiveDeal toActiveDeal(Restaurant restaurant, Deal deal);
}
