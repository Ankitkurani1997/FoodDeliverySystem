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
	
	@PostMapping("/reInitialize")
	@ResponseBody
	public ResponseEntity<String> initializeRecords() throws IOException {
		
		orderService.clearData();
		return ResponseEntity.status(HttpStatus.CREATED).body(null);
	}
	
	@PostMapping("/requestOrder")
	@ResponseBody
	public ResponseEntity<Object> placeNewOrder(@RequestBody Order ord){
		
		int orderId = orderService.requestOrder(ord); 
		if(orderId != -1)
		{
			JSONObject entity = new JSONObject();
			entity.appendField("orderId", orderId);
			return new ResponseEntity<Object>(entity, HttpStatus.CREATED);
		}
		else
		{
			return ResponseEntity.status(HttpStatus.GONE).body(null);
		}
		
	}
	
	@PostMapping("/agentSignIn")
	@ResponseBody
	public ResponseEntity<Object> agentSignIn(@RequestBody HashMap<String,Integer> requestData) {
		if(!orderService.getDeliveryAgents().containsKey(requestData.get("agentId"))) {
			return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		orderService.agentSignIn(requestData.get("agentId"));	
		return ResponseEntity.status(HttpStatus.CREATED).body(null);
	}
	
	@PostMapping("/agentSignOut")
	@ResponseBody
	public ResponseEntity<String> agentSignOut(@RequestBody HashMap<String,Integer> requestData) {
		
		orderService.agentSignOut(requestData.get("agentId"));	
		return ResponseEntity.status(HttpStatus.CREATED).body(null);
	}
	
	@PostMapping("/orderDelivered")
	@ResponseBody
	public ResponseEntity<String> orderDelivered(@RequestBody HashMap<String, Integer> requestData ) {
		
		Integer orderId = requestData.get("orderId");
		if(!orderService.getOrders().containsKey(orderId)) {
			return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		
		orderService.orderDelivered(orderId);
		return ResponseEntity.status(HttpStatus.CREATED).body(null);
	}
	
	@GetMapping("/order/{num}")
	@ResponseBody
	public ResponseEntity<Object> getOrderInfo(@PathVariable("num") int num) {
		
		if(num >= orderService.getGlobalOrderId() || num <1000) {
			return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		JSONObject entity = orderService.getOrderDetails(num);
		return new ResponseEntity<Object>(entity, HttpStatus.OK);
	}
	
	@GetMapping("/agent/{num}")
	@ResponseBody
	public ResponseEntity<Object> getAgentInfo(@PathVariable("num") int num) {
		if(!orderService.getDeliveryAgents().containsKey(num)) {
			return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		JSONObject entity = orderService.getAgentDetails(num);
		return new ResponseEntity<Object>(entity, HttpStatus.OK);
	}
	
}
