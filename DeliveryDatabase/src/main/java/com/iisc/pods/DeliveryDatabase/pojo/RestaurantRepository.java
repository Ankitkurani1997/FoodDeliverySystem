package com.iisc.pods.DeliveryDatabase.pojo;

import org.springframework.data.repository.CrudRepository;

public interface RestaurantRepository extends CrudRepository<Restaurant, Integer> {
	
	Restaurant findById(int id);
	
	Restaurant findByRestIdAndItemId(int restId, int itemId);
}
