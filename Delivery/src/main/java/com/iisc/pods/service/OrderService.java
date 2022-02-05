package com.iisc.pods.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.iisc.pods.pojo.Item;
import com.iisc.pods.pojo.Order;

import net.minidev.json.JSONObject;

@Service
public class OrderService {
	
	private final String URI_WALLET_DEDUCTBALANCE = "http://localhost:8082/deductBalance";
	private final String URI_WALLET_ADDBALANCE = "http://localhost:8082/addBalance";
	private final String URI_RESTAURANT = "http://localhost:8080/acceptOrder";
	
	HashMap<Integer, List<Item> > restaurants = new HashMap<>(); 
	Map<Integer, String> deliveryAgents = new TreeMap<>();
	Map<Integer, Order> orders = new TreeMap<>();
	
	int globalOrderId;
	
	public OrderService(){
		globalOrderId = 1000;
	}
	
	public int getGlobalOrderId() {
		return globalOrderId;
	}


	public void setGlobalOrderId(int globalOrderId) {
		this.globalOrderId = globalOrderId;
	}

	public Map<Integer, Order> getOrders() {
		return orders;
	}

	public void setOrders(Map<Integer, Order> orders) {
		this.orders = orders;
	}

	public Map<Integer, String> getDeliveryAgents() {
		return deliveryAgents;
	}

	public void setDeliveryAgents(Map<Integer, String> deliveryAgents) {
		this.deliveryAgents = deliveryAgents;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void initializeData() throws IOException
	{
		File file = new File("initialData.txt");
		FileReader fileReader = new FileReader(file);
		try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
			String pattern = "****";
			String line;
			int counter = 0;
			int restId = 0;
			while((line = bufferedReader.readLine())!=null) {
				line = line.strip();
				if(line.equalsIgnoreCase(pattern)) {
					counter++;
				}
				else
				{
					if(counter == 0)
					{
						String[] str = line.split(" ");
						if(str.length == 2)
						{
							restId = Integer.parseInt(str[0]);
						}
						else
						{
							int itemId = Integer.parseInt(str[0]), price = Integer.parseInt(str[1]);
							addRestaurant(restId, itemId, price);
						}
					}
					else if(counter == 1)
					{
						int agentId = Integer.parseInt(line);
						deliveryAgents.put(agentId, "signed-out");
					}
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
	}
	
	public void addRestaurant(int restId, int itemId, int price)
	{
		Item item  = new Item();
		item.setItemId(itemId);
		item.setPrice(price);
		if(restaurants.containsKey(restId))
		{
			List<Item> lst = restaurants.get(restId);
			lst.add(item);
			restaurants.put(restId, lst);
		}
		else
		{
			List<Item> lst = new ArrayList<>();
			lst.add(item);
			restaurants.put(restId, lst);
		}
	}
	
	public int computeTotalBill(int restId, int itemId, int qty)
	{
		int total = 0;
		List<Item> items = restaurants.get(restId);
		for(int i=0;i<items.size();i++)
		{
			if(items.get(i).getItemId() == itemId)
			{
				total = qty * items.get(i).getPrice();
				break;
			}
		}
		return total;
	}
	
	public int isDatavalid(int restId, int itemId) {
		if(!restaurants.containsKey(restId)) {
			System.out.println("restId not present");
			return 0;
		}
		List<Item> items = restaurants.get(restId);
		for(int i=0;i<items.size();i++)
		{
			if(items.get(i).getItemId() == itemId)
			{
				return 1;
			}
		}
		System.out.println("itemId is not present");
		return 0;
	}
	
	public int requestOrder(Order ord) {
		if(isDatavalid(ord.getRestId(), ord.getItemId()) == 0) {
			return -1;
		}
		int totalBill = computeTotalBill(ord.getRestId(), ord.getItemId(), ord.getQty());
		
		int flag = 0;
		JSONObject entityWallet = null;
		RestTemplate restTemplate = null;
		ResponseEntity<Object> responseWallet = null;
		HttpEntity<Object> httpEntityWallet = null;
		HttpEntity<String> httpEntityRestaurant = null;
		HttpStatus responseRestaurant = null;
		
		try {
		
			restTemplate = new RestTemplate();
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			
			entityWallet = new JSONObject();
			entityWallet.appendField("custId", ord.getCustId());
			entityWallet.appendField("amount", totalBill);
			
			httpEntityWallet = new HttpEntity<Object>(entityWallet.toString(), headers);
			responseWallet = restTemplate.exchange(URI_WALLET_DEDUCTBALANCE, HttpMethod.POST, httpEntityWallet, Object.class);
			flag = 1;
			
			JSONObject entityRestaurant = new JSONObject();
			entityRestaurant.appendField("restId", ord.getRestId());
			entityRestaurant.appendField("itemId", ord.getItemId());
			entityRestaurant.appendField("qty", ord.getQty());
			
			httpEntityRestaurant = new HttpEntity<String>(entityRestaurant.toString(), headers);
			responseRestaurant = restTemplate.exchange(URI_RESTAURANT, HttpMethod.POST, httpEntityRestaurant, String.class).getStatusCode();
			
			int id = globalOrderId;
			ord.setOrderId(id);
			globalOrderId++;
			
				if(responseRestaurant == HttpStatus.CREATED) {
					int agentAssigned = isAgentAvailable();
			
					if(agentAssigned!=-1)
					{
						ord.setStatus("assigned");
						ord.setAgentId(agentAssigned);
					}
					else
					{
						ord.setStatus("unassigned");
					}
					orders.put(id ,ord);
					return id;
				}
				else
				{
					return -1;
				}
		}
		catch (HttpClientErrorException e) {
			if(flag == 0) {
				return -1;
			}
			else {
				try {
					responseWallet = restTemplate.exchange(URI_WALLET_ADDBALANCE, HttpMethod.POST, httpEntityWallet, Object.class);
					return -1;
				}
				catch(HttpClientErrorException exception) {
					return -1;
				}
			}
		}
		catch (Exception e) {
			if(flag==0)
				System.out.println("Issue in deducting balance from wallet");
			else
				System.out.println("Issue in placing order");
			return -1;
		}
	}
	
	public void agentSignIn(int agentId) {
		
		String status = deliveryAgents.get(agentId);
		if(!status.equalsIgnoreCase("available"))
		{
			int orderId = isAnyOrderUnAssigned();
			if(orderId!=-1) {
				deliveryAgents.put(agentId, "unavailable");
				Order ord = orders.get(orderId);
				ord.setStatus("assigned");
				ord.setAgentId(agentId);
				orders.put(orderId, ord);
			}
			else {
				deliveryAgents.put(agentId, "available");
			}
		}
			
	}
	
	public void agentSignOut(int agentId) {
		
		String status = deliveryAgents.get(agentId);
		if(!status.equalsIgnoreCase("signed-out") && !status.equalsIgnoreCase("unavailable"))
		{
			deliveryAgents.put(agentId, "signed-out");
		}
			
	}
	
	public void orderDelivered(int orderId) {
		Order ord = orders.get(orderId);
		if(ord.getStatus().equalsIgnoreCase("assigned")) {
			ord.setStatus("delivered");
			orders.put(orderId, ord);
			int newOrderId = isAnyOrderUnAssigned();
			if(newOrderId!=-1)
			{
				deliveryAgents.put(ord.getAgentId(), "unavailable");
				Order newOrd = orders.get(newOrderId);
				newOrd.setStatus("assigned");
				newOrd.setAgentId(ord.getAgentId());
				orders.put(newOrderId, newOrd);
			}
			else
			{
				deliveryAgents.put(ord.getAgentId(), "available");
			}
		}
	}
	
	public JSONObject getOrderDetails(int orderId) {	
		JSONObject entity = new JSONObject(); 
		entity.appendField("orderId", orderId);
		entity.appendField("status", orders.get(orderId).getStatus());
		entity.appendField("agentId", orders.get(orderId).getAgentId());		
		return entity;
	}
	
	public JSONObject getAgentDetails(int agentId) {	
		JSONObject entity = new JSONObject(); 
		entity.appendField("agentId", agentId);
		entity.appendField("status", deliveryAgents.get(agentId));		
		return entity;
	}
	
	public int isAgentAvailable() {
		for(Map.Entry<Integer, String> agent : deliveryAgents.entrySet() ) {
			if(agent.getValue().equalsIgnoreCase("available")) {
				agent.setValue("unavailable");
				return agent.getKey();
			}
		}
		return -1;
	}
	
	public int isAnyOrderUnAssigned() {
		for(Map.Entry<Integer, Order> order : orders.entrySet() ) {
			if(order.getValue().getStatus().equalsIgnoreCase("unassigned")) {
				order.getValue().setStatus("assigned");
				return order.getKey();
			}
		}
		return -1;
	}
	
	public void clearData() throws IOException {
		orders.clear();
		globalOrderId = 1000;
		for(Map.Entry<Integer, String> agent : deliveryAgents.entrySet() ) {
			agent.setValue("signed-out");
		}
	}
	
}
