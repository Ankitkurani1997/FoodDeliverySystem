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
import org.springframework.web.client.RestTemplate;

import com.iisc.pods.pojo.Item;
import com.iisc.pods.pojo.Order;

import net.minidev.json.JSONObject;

@Service
public class OrderService {
	
	private final String URI_WALLET_DEDUCTBALANCE = "http://10.217.64.43:8080/deductBalance";
	private final String URI_WALLET_ADDBALANCE = "http://10.217.64.43:8080/addBalance";
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
						deliveryAgents.put(agentId, "available");
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
	
	public int requestOrder(Order ord) {
		if(!restaurants.containsKey(ord.getRestId())) {
			return -1;
		}
		int totalBill = computeTotalBill(ord.getRestId(), ord.getItemId(), ord.getQty());
		RestTemplate restTemplate = new RestTemplate();
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		JSONObject entityWallet = new JSONObject();
		entityWallet.appendField("custId", ord.getCustId());
		entityWallet.appendField("amount", totalBill);
		
		HttpEntity<Object> httpEntityWallet = new HttpEntity<Object>(entityWallet.toString(), headers);
		
		ResponseEntity<Object> responseWalltet = restTemplate.exchange(URI_WALLET_DEDUCTBALANCE, HttpMethod.POST, httpEntityWallet, Object.class);
		
		if(responseWalltet.getStatusCode() == HttpStatus.GONE)
		{
			return -1;
		}
		else
		{
			JSONObject entityRestaurant = new JSONObject();
			entityRestaurant.appendField("restId", ord.getRestId());
			entityRestaurant.appendField("itemId", ord.getItemId());
			entityRestaurant.appendField("qty", ord.getQty());
			
			HttpEntity<Object> httpEntityRestaurant = new HttpEntity<Object>(entityWallet.toString(), headers);
			ResponseEntity<Object> responseRestaurant = restTemplate.exchange(URI_RESTAURANT, HttpMethod.POST, httpEntityRestaurant, Object.class);
			
			if(responseRestaurant.getStatusCode() != HttpStatus.CREATED)
			{
				responseWalltet = restTemplate.exchange(URI_WALLET_ADDBALANCE, HttpMethod.POST, httpEntityWallet, Object.class);
				if(responseWalltet.getStatusCode() == HttpStatus.GONE)
				{
					return -1;
				}
				else
				{
					System.out.println("Restored Balance");
					return -1;
				}
			}
			else
			{
				int id = globalOrderId;
				ord.setOrderId(id);
				globalOrderId++;
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
				System.out.println("Balance Reduced");
				return id;
			}
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
		for(Map.Entry<Integer, String> agent : deliveryAgents.entrySet() ) {
			agent.setValue("signed-out");
		}
	}
	
}
