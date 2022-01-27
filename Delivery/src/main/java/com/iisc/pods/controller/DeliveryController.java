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

import com.iisc.pods.pojo.DeliveryAgent;
import com.iisc.pods.pojo.Order;
import com.iisc.pods.service.OrderService;
import com.iisc.pods.service.Inventory;

import net.minidev.json.JSONObject;

@RestController
public class DeliveryController {
	
	
	
	@Autowired
	Inventory invObject;
	
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
	public ResponseEntity<Object> placeNewOrder(@RequestBody Order ord) {
		
		int orderId = orderService.requestOrder(ord); 
		if(orderId != -1)
		{
			JSONObject entity = new JSONObject();
			entity.appendField("orderId", orderId);
			return new ResponseEntity<Object>(entity, HttpStatus.CREATED);
		}
		else
		{
			JSONObject entity = new JSONObject();
			return new ResponseEntity<Object>(entity, HttpStatus.GONE);
		}
		
	}
	
	@PostMapping("/agentSignIn")
	@ResponseBody
	public ResponseEntity<String> agentSignIn(@RequestBody DeliveryAgent agentId) {
		
		System.out.println(agentId.getAgentId());	
		return ResponseEntity.status(HttpStatus.CREATED).body("Code 201");
	}
	
	@PostMapping("/agentSignOut")
	@ResponseBody
	public ResponseEntity<String> agentSignOut(@RequestBody DeliveryAgent agentId) {
		
		System.out.println(agentId.getAgentId());
		return ResponseEntity.status(HttpStatus.CREATED).body("Sign out Code 201");
	}
	
	@PostMapping("/orderDelivered")
	@ResponseBody
	public ResponseEntity<String> orderDelivered(@RequestBody HashMap<String, Integer> requestData ) {
		
		Integer agentId = requestData.get("orderId");
		System.out.println(agentId);
		return ResponseEntity.status(HttpStatus.CREATED).body("Order Delivered Code 201");
	}
	
	@GetMapping("/order/{num}")
	@ResponseBody
	public ResponseEntity<Object> getOrderInfo(@PathVariable("num") int num) {
		
		System.out.println(num);
		return ResponseEntity.status(HttpStatus.OK).body("Order Info Code 200");
	}
	
	@GetMapping("/agent/{num}")
	@ResponseBody
	public ResponseEntity<Object> getAgentInfo(@PathVariable("num") int num) {
		
		System.out.println(num);
		return ResponseEntity.status(HttpStatus.OK).body("Order Info Code 200");
	}
	
}
