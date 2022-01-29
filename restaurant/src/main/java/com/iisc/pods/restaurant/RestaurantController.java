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
	
	@PostMapping("/acceptOrder")
	public ResponseEntity<String> newOrder(@RequestBody HashMap<String, Integer> requestData) {
		
		int status = inventoryService.acceptOrder(requestData.get("restId"), requestData.get("itemId"), requestData.get("qty"));
		if(status == 1)
			return ResponseEntity.status(HttpStatus.CREATED).body(null);
			
		else if(status == 0)
			return ResponseEntity.status(HttpStatus.GONE).body(null);
		else
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	}
	
	@PostMapping("/refillItem")
	public ResponseEntity<String> addToInventory(@RequestBody HashMap<String, Integer> requestData) {
		
		int status = inventoryService.refill(requestData.get("restId"), requestData.get("itemId"), requestData.get("qty"));
		if(status == 1)
			return ResponseEntity.status(HttpStatus.CREATED).body(null);
		else
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	}
	
	
	@PostMapping("/reInitialize")
	public ResponseEntity<String> reInit() throws IOException {
		inventoryService.freshInitRestaurants();
		return ResponseEntity.status(HttpStatus.CREATED).body(null);
	}
	
	
	
	@GetMapping("/restInit")
	public ResponseEntity<String> rest() {
		inventoryService.addRest();
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}
	
	
	@GetMapping("/inv/{num}")
	public ResponseEntity<Restaurant> getInv(@PathVariable("num") int num) {
		Restaurant res = inventoryService.getRest(num);
		return new ResponseEntity<Restaurant>(res, HttpStatus.OK);
	}
	
	
}
