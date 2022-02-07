package com.iisc.pods.restaurant;

import java.io.IOException;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.iisc.pods.restaurant.pojo.Restaurant;
import com.iisc.pods.restaurant.service.InventoryService;

@RestController
public class RestaurantController {
	
	@Autowired
	InventoryService inventoryService;
	
	/**
	 * @param requestData (JSON payload of the form {“restId”: num, “itemId”: x, “qty”: y})
	 * This end-point will be invoked by the Delivery service.
	 * If the restaurant with ID num has at least y quantities of itemId x in its current inventory
	 * then
	 * 		reduce the inventory of item x in restaurant num by yreturn HTTP status code 201 (created)
	 * else
	 * 		return HTTP status code 410 (gone)
	 * @return HTTP status code 201 or 410
	 */
	@PostMapping("/acceptOrder")
	public ResponseEntity<Object> newOrder(@RequestBody HashMap<String, Integer> requestData) {
		
		int status = inventoryService.acceptOrder(requestData.get("restId"), requestData.get("itemId"), requestData.get("qty"));
		if(status == 1)
			return ResponseEntity.status(HttpStatus.CREATED).body(null);
			
		else if(status == 0)
			return ResponseEntity.status(HttpStatus.GONE).body(null);
		else
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	}
	
	/**
	 * @param requestData (JSON payload of the form {“restId”: num, “itemId”: x, “qty”: y})
	 * Increases the inventory of itemId x in restaurant num by y (provided num supplies x
	 * as per /initialData.txt)
	 * @return HTTP status code 201
	 */
	@PostMapping("/refillItem")
	public ResponseEntity<Object> addToInventory(@RequestBody HashMap<String, Integer> requestData) {
		
		int status = inventoryService.refill(requestData.get("restId"), requestData.get("itemId"), requestData.get("qty"));
		if(status == 1)
			return ResponseEntity.status(HttpStatus.CREATED).body(null);
		else
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	}
	
	/**
	 * Set inventory of all items in all restaurants as given in the /initialData.txt file.
	 * @return HTTP status code 201
	 * @throws IOException
	 */
	@PostMapping("/reInitialize")
	public ResponseEntity<Object> reInit() throws IOException {
		inventoryService.freshInitRestaurants();
		return ResponseEntity.status(HttpStatus.CREATED).body(null);
	}
	
}
