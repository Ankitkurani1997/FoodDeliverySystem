package com.iisc.pods.controller;

import java.io.IOException;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.iisc.pods.pojo.Order;
import com.iisc.pods.service.OrderService;

import net.minidev.json.JSONObject;

@RestController
public class DeliveryController {
	
	@Autowired
	OrderService orderService;
	
	private final Object lock = new Object();
	/**
	 * This is the end Point for reinitializing the Delivery application
	 * Delete all orders from the records (no matter what their status is), and mark status of
	 * each agent as signed-out
	 * @throws IOException
	 */
	@PostMapping("/reInitialize")
	@ResponseBody
	public ResponseEntity<String> initializeRecords() throws IOException {
		synchronized (lock) {
			orderService.clearData();
			return ResponseEntity.status(HttpStatus.CREATED).body(null);
		}
	}
	
	
	/**
	 * @param ord
	 * This is the main end-point used by the customer to place an order
	 * It calls the requestOrder function under the service OrderService 
	 * @return If order is placed successfully, return the order Id and also status as 201
	 * else returns code 410
	 */
	@PostMapping("/requestOrder")
	@ResponseBody
	public ResponseEntity<Object> placeNewOrder(@RequestBody Order ord){
		synchronized (lock) {
			System.out.println("Entering");
			int orderId = orderService.requestOrder(ord); 
			if(orderId != -1)
			{
				JSONObject entity = new JSONObject();
				entity.appendField("orderId", orderId);
				System.out.println("Leaving");
				return new ResponseEntity<Object>(entity, HttpStatus.CREATED);
				
			}
			else
			{
				System.out.println("Leaving");
				return ResponseEntity.status(HttpStatus.GONE).body(null);
			}	
		}
	}
	
	/**
	 * @param requestData
	 * This end Point implements Sign In feature of the Delivery Service application
	 * If agentId is already in available or unavailable state, do nothing. Otherwise, If any
	 * orderIds are currently in unassigned state, find the least numbered orderId y that is
	 * unassigned, mark agentId as unavailable, mark the status of y as assigned, and record
	 * that y is assigned to agentId. Otherwise, mark the status of agentId as available
	 * @return In all cases return HTTP status code 201
	 */
	@PostMapping("/agentSignIn")
	@ResponseBody
	public ResponseEntity<Object> agentSignIn(@RequestBody HashMap<String,Integer> requestData) {
		synchronized (lock) {
			System.out.println("Entering Login for Agent");
			if(!orderService.getDeliveryAgents().containsKey(requestData.get("agentId"))) {
				System.out.println("Leaving error agent");
				return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
			}
			orderService.agentSignIn(requestData.get("agentId"));	
			System.out.println("Leaving Agent");
			return ResponseEntity.status(HttpStatus.CREATED).body(null);
		}
	}
	
	
	/**
	 * @param requestData
	 * This end Point implements Sign Out feature of the Delivery Service application
	 * If agentId is already signed-out or is unavailable, do nothing, else mark agentId as being in
     * signed-out state. 
	 * @return In both cases, return HTTP status code 201.
	 */
	@PostMapping("/agentSignOut")
	@ResponseBody
	public ResponseEntity<String> agentSignOut(@RequestBody HashMap<String,Integer> requestData) {
		synchronized (lock) {
			orderService.agentSignOut(requestData.get("agentId"));	
			return ResponseEntity.status(HttpStatus.CREATED).body(null);
		}
	}
	
	/**
	 * @param requestData
	 * This end Point marks the status of the order as delivered if it is in assigned state
	 * and makes the agent of the agent assigned to it as available and also checks if any other
	 * order is unassigned and assigns this agent with the lowest order id unassigned.
	 * @return In all cases return 201. 
	 * Additional checks are provided such as orderId is not available
	 */
	@PostMapping("/orderDelivered")
	@ResponseBody
	public ResponseEntity<String> orderDelivered(@RequestBody HashMap<String, Integer> requestData ) {
		synchronized (lock) {
			Integer orderId = requestData.get("orderId");
			if(!orderService.getOrders().containsKey(orderId)) {
				return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
			}
			orderService.orderDelivered(orderId);
			return ResponseEntity.status(HttpStatus.CREATED).body(null);
		}
	}
	
	/**
	 * This endPoint returns the order details for the orderId provided
	 * @return If orderId is a non-existent orderId return HTTP status code 404. Otherwise return
     * status code 200 along with response JSON of the form {"orderId": orderId, "status": x,
     * “agentId”: y}, where x is unassigned, or assigned, or delivered. y will be the agentId
       that is assigned the order orderId in case orderId is in assigned or delivered state, else y will be -1.
	 */
	@GetMapping("/order/{num}")
	@ResponseBody
	public ResponseEntity<Object> getOrderInfo(@PathVariable("num") int num) {
		synchronized (lock) {	
			if(num >= orderService.getGlobalOrderId() || num <1000) {
				return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
			}
			JSONObject entity = orderService.getOrderDetails(num);
			return new ResponseEntity<Object>(entity, HttpStatus.OK);
		}
	}
	
	/**
	 * @param num
	 * @return This endPoint returns the agent details for the agentId provided
	 * and return status code 200 and response JSON of the form {"agendId": num, "status": y},
	 * where y is signed-out, available, or unavailable
	 */
	@GetMapping("/agent/{num}")
	@ResponseBody
	public ResponseEntity<Object> getAgentInfo(@PathVariable("num") int num) {
		synchronized (lock) {
			if(!orderService.getDeliveryAgents().containsKey(num)) {
				return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
			}
			JSONObject entity = orderService.getAgentDetails(num);
			return new ResponseEntity<Object>(entity, HttpStatus.OK);
		}
	}
	
}
