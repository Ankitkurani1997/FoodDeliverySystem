package com.iisc.pods.DeliveryDatabase.Controller;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.iisc.pods.DeliveryDatabase.pojo.Agent;
import com.iisc.pods.DeliveryDatabase.pojo.Orders;
import com.iisc.pods.DeliveryDatabase.pojo.Restaurant;
import com.iisc.pods.DeliveryDatabase.service.DatabaseService;

@RestController
public class DeliveryDatabaseController {
	
	@Autowired
	DatabaseService dbs;

	/**
	 * This API takes an agent ID and a status as parameter and updates the agent's status. Used against agentSignIn and agentSignout.
	 * @param agent
	 * @return Http Response code
	 */
	@PutMapping("/agentStatusChange")
	@ResponseBody
	public ResponseEntity<String> changeAgentStatus(@RequestBody Agent agent) {
		
		if(dbs.changeAgentStatus(agent) == 1)
			return ResponseEntity.status(HttpStatus.CREATED).body(null);
		else
			return ResponseEntity.status(HttpStatus.GONE).body(null);
	}
	
	
	/**
	 * Accept an agent ID as a URL path variable and return the Agent with that particular ID.
	 * @param num
	 * @return Agent object
	 */
	@GetMapping("/agent/{num}")
	@ResponseBody
	public Agent getAgent(@PathVariable("num") int num) {
		Agent agent = dbs.getAgent(num);
		return agent;
	}
	
	/**
	 * Reinitialize the data in the H2 instance
	 * @return Http status code
	 */
	@PostMapping("/reInitialize")
	@ResponseBody
	public ResponseEntity<String> clean() {
		if(dbs.freshInit() == 1)
			return ResponseEntity.status(HttpStatus.CREATED).body(null);
		else
			return ResponseEntity.status(HttpStatus.GONE).body(null);
	}
	
	/**
	 * Create a new order in the Orders table. Input required is a customer ID, a restaurant ID, an item ID and quantity. 
	 * @param requestData
	 * @return The order that was created
	 */
	@PostMapping("/createOrder")
	@ResponseBody
	public ResponseEntity<Orders> createOrder(@RequestBody HashMap<String,Integer> requestData) {
		System.out.println(requestData.toString());
		Orders order = dbs.createOrder(requestData.get("restId"), requestData.get("itemId"), requestData.get("qty"), requestData.get("custId"));
		System.out.println(order.toString());
		return new ResponseEntity<Orders>(order, HttpStatus.OK);
	}
	
	
	/**
	 * Accept an order ID as a path variable and returns the corresponding Orders object.
	 * @param num
	 * @return Orders object
	 */
	@GetMapping("/order/{num}")
	@ResponseBody
	public Orders getOrder(@PathVariable("num") int num) {
		Orders order = dbs.getOrder(num);
		return order;
	}
	
	
	/**
	 * Accept a restaurant ID and an item ID as input from path variables and return the price of the item in that particular restaurant.
	 * @param restId
	 * @param itemId
	 * @return Restaurant object
	 */
	@GetMapping("/restaurant")
	@ResponseBody
	public Restaurant getOrder(@RequestParam int restId, @RequestParam int itemId) {
		Restaurant res = dbs.getRestaurant(restId, itemId);
		System.out.println(res.toString());
		return res;
	}
	
	/**
	 * Get the list of orders that are unassigned to any agent currently. This list is used to assign an order to an agent who just got available.
	 * @return A list of orders
	 */
	@GetMapping("/unassignedOrders")
	@ResponseBody
	public Orders[] getUnassignedOrders() {
		Orders[] order = dbs.getUnassignedOrders();
		return order;
	}
	
	/**
	 * Assign the agent with the particular agentID to the order with the given orderID.
	 * @param requestData
	 * @return Http Status code
	 */
	@PutMapping("/assignOrder")
	@ResponseBody
	public ResponseEntity<String> assignOrder(@RequestBody HashMap<String,Integer> requestData) {
		dbs.assignOrder(requestData.get("orderId"), requestData.get("agentId"));
		return ResponseEntity.status(HttpStatus.CREATED).body(null);
	}
	
	/**
	 * Marks an order as delivered. Also free the agent that was assigned to that order. The Order must reflect a delivered status and the agent must now be available.
	 * @param num
	 * @return Http Status code
	 */
	@PutMapping("/deliverOrder/{num}")
	@ResponseBody
	public ResponseEntity<String> changeOrderStatus(@PathVariable("num") int num) {
		
		if(dbs.changeOrderStatus(num) == 1)
			return ResponseEntity.status(HttpStatus.CREATED).body(null);
		else
			return ResponseEntity.status(HttpStatus.GONE).body(null);
	}
}
