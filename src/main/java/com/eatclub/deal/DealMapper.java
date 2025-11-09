package com.eatclub.deal;

import com.eatclub.deal.DealRepository.Deal;
import com.eatclub.deal.DealRepository.Restaurant;
import com.eatclub.deal.DealService.ActiveDeal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface DealMapper {
    @Mapping(target = "restaurantObjectId", source = "restaurant.objectId")
    @Mapping(target = "restaurantName", source = "restaurant.name")
    @Mapping(target = "restaurantAddress1", source = "restaurant.address1")
    @Mapping(target = "restaurantSuburb", source = "restaurant.suburb")
    @Mapping(target = "restaurantOpen", source = "dealWrapper", qualifiedByName = "openTime")
    @Mapping(target = "restaurantClose", source = "dealWrapper", qualifiedByName = "closeTime")
    @Mapping(target = "dealObjectId", source = "deal.objectId")
    @Mapping(target = "discount", source = "deal.discount")
    @Mapping(target = "dineIn", source = "deal.dineIn")
    @Mapping(target = "lightning", source = "deal.lightning")
    @Mapping(target = "qtyLeft", source = "deal.qtyLeft")
    ActiveDeal toActiveDeal(RestaurantDeal dealWrapper);

    @Named("openTime")
    default Time mapOpenTime(RestaurantDeal dealWrapper) {
        if (dealWrapper.deal().lightning()) {
            return dealWrapper.deal().open();
        } else {
            return dealWrapper.restaurant().open();
        }
    }

    @Named("closeTime")
    default Time mapCloseTime(RestaurantDeal dealWrapper) {
        if (dealWrapper.deal().lightning()) {
            return dealWrapper.deal().close();
        } else {
            return dealWrapper.restaurant().close();
        }
    }

    record RestaurantDeal(Restaurant restaurant, Deal deal) {
    }
}
