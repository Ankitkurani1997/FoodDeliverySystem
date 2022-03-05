package com.iisc.pods.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
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

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

@Service
public class OrderService {
	
	private final String URI_WALLET_DEDUCTBALANCE = "http://walletservice:8082/deductBalance";
	private final String URI_WALLET_ADDBALANCE = "http://walletservice:8082/addBalance";
	private final String URI_RESTAURANT = "http://restaurantservice:8080/acceptOrder";
	private String URI_DATABASE = "http://databaseservice:8083/";
	
	HashMap<Integer, List<Item> > restaurants = new HashMap<>(); 
	Map<Integer, String> deliveryAgents = new TreeMap<>();
	Map<Integer, Order> orders = new TreeMap<>();
	
	int globalOrderId;
	
	RestTemplate restTemplate = new RestTemplate();
	HttpHeaders headers = new HttpHeaders();
	
	public OrderService(){
		globalOrderId = 1000;
		headers.setContentType(MediaType.APPLICATION_JSON);
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
	
	/**
	 * 
	 * initializeData function is invoked by the event-listner as soon as the delivery
	 * application is launched. The Delivery application will initialize all its data also
	 * from the initialData.txt file provided.
	 * @throws IOException
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void initializeData() throws IOException
	{
		File file;
		try {
			file = new File("initialData.txt");
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
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
		bufferedReader.close();
		fileReader.close();
		}
		catch(FileNotFoundException e) {
			System.out.println("Initialialization file not found!");
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * @param restId
	 * @param itemId
	 * @param price
	 * addRestaurant function is called while initializing data at the start.
	 * HashMap restaurants is populated in this function which keeps account of the
	 * restaurants present, item id's in that particular restaurant and what are there
	 * respective prices
	 */
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
	
	
	/**
	 * @param restId
	 * @param itemId
	 * @param qty
	 * 
	 * computeTotalBill calculates the total bill of an order and returns it to the requestOrder function.
	 * 
	 * @return total
	 */
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
	
	
	/**
	 * @param restId
	 * @param itemId
	 * 
	 * isDatavalid checks if the given restaurant id is valid or not, and if valid
	 * then is the item if given to it is valid or not.
	 * 
	 * @return 1 if valid else 0
	 */
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
	
	
	/**
	 * @param ord
	 * 
	 * requestOrder function is called when the post request to end point requestOrder is done and in turn the
	 * placeNewOerder function calls this method to compute the bill of the order and 
	 * contact the wallet and restaurant services to successfully place the order.
	 * 
	 * Various checks are performed in this function like to check the validity of the order
	 * Functionality includes :- 
	 * 1. Computing Bill
	 * 2. Deducting balance from the wallet if amount of bill is present in the account of that
	 * 	  particular customer
	 * 3. Contacting restaurant to check if particular quantity of specific item is present with them
	 *    if yes then place the order successfully, else restoring the balance deducted from the
	 *    particular customer's account.
	 * 
	 * @return It return 1 if order is placed or booked successfully else returns -1 to the function
	 * placeNewOrder
	 * @throws net.minidev.json.parser.ParseException 
	 */
	public int requestOrder(int custId, int restId, int itemId, int qty) throws net.minidev.json.parser.ParseException {
		/*if(isDatavalid(ord.getRestId(), ord.getItemId()) == 0) {
			return -1;
		}*/
		
		String url = "http://ankitdatabase:8083/restaurant?restId=" + restId + "&itemId=" + itemId;
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
		JSONParser parser = new JSONParser();
		JSONObject restaurant = (JSONObject) parser.parse(response.getBody());
		
		System.out.println(restaurant.toString());
		int totalBill = qty * (Integer) restaurant.get("price");
		
		int flag = 0;
		JSONObject entityWallet = null;
		JSONObject entityDatabase = null;
		RestTemplate restTemplate = null;
		HttpEntity<Object> httpEntityWallet = null;
		HttpEntity<String> httpEntityRestaurant = null;
		HttpEntity<String> httpEntityDatabase = null;
		HttpStatus responseRestaurant = null;
		
		try {
		
			restTemplate = new RestTemplate();
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			
			entityWallet = new JSONObject();
			entityWallet.appendField("custId", custId);
			entityWallet.appendField("amount", totalBill);
			
			httpEntityWallet = new HttpEntity<Object>(entityWallet.toString(), headers);
			restTemplate.exchange("http://ankitwallet:8082/deductBalance", HttpMethod.POST, httpEntityWallet, Object.class);
			flag = 1;
			
			JSONObject entityRestaurant = new JSONObject();
			entityRestaurant.appendField("restId", restId);
			entityRestaurant.appendField("itemId", itemId);
			entityRestaurant.appendField("qty", qty);
			
			httpEntityRestaurant = new HttpEntity<String>(entityRestaurant.toString(), headers);
			responseRestaurant = restTemplate.exchange("http://ankitrestaurant:8080/acceptOrder", HttpMethod.POST, httpEntityRestaurant, String.class).getStatusCode();
			
			if(responseRestaurant == HttpStatus.CREATED) {
				entityDatabase = new JSONObject();
				entityDatabase.appendField("restId", restId);
				entityDatabase.appendField("itemId", itemId);
				entityDatabase.appendField("qty", qty);
				entityDatabase.appendField("custId", custId);
				
				url = "http://ankitdatabase:8083/createOrder";
				httpEntityDatabase = new HttpEntity<String>(entityDatabase.toString(), headers);
				ResponseEntity<String> res = restTemplate.exchange(url, HttpMethod.POST, httpEntityDatabase, String.class);
				parser = new JSONParser();
				JSONObject order = (JSONObject) parser.parse(res.getBody());
				
				if(res.getStatusCode() != HttpStatus.OK)
					return -1;
				System.out.println("Test1 : " + order.toString());
				return Integer.parseInt(order.get("orderId").toString());
			}
			return -1;
			
//			int id = globalOrderId;
//			ord.setOrderId(id);
//			globalOrderId++;
//			
//				if(responseRestaurant == HttpStatus.CREATED) {
//					int agentAssigned = isAgentAvailable();
//			
//					if(agentAssigned!=-1)
//					{
//						ord.setStatus("assigned");
//						ord.setAgentId(agentAssigned);
//					}
//					else
//					{
//						ord.setStatus("unassigned");
//					}
//					/*
//					changes
//					 */
//					entityDatabase = new JSONObject();
//					entityDatabase.appendField("orderId", ord.getOrderId());
//					entityDatabase.appendField("restId", ord.getRestId());
//					entityDatabase.appendField("agentId", ord.getAgentId());
//					entityDatabase.appendField("itemId", ord.getItemId());
//					entityDatabase.appendField("qty", ord.getQty());
//					entityDatabase.appendField("status", ord.getStatus());
//					entityDatabase.appendField("custId", ord.getCustId());
//
//					//httpEntityDatabase = new HttpEntity<String>(entityRestaurant.toString(), headers);
//					//restTemplate.exchange(URI_DATABASE + "addOrder", HttpMethod.POST, httpEntityDatabase, Object.class);
//					
//					orders.put(id ,ord);
//					return id;
//				}
//				else
//				{
//					return -1;
//				}
		}
		catch (HttpClientErrorException e) {
			if(flag == 0) {
				return -1;
			}
			else {
				try {
					restTemplate.exchange("http://ankitwallet:8082/addBalance", HttpMethod.POST, httpEntityWallet, Object.class);
					return -1;
				}
				catch(HttpClientErrorException exception) {
					return -1;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			if(flag==0)
				System.out.println("Issue in deducting balance from wallet");
			else
				System.out.println("Issue in placing order");
			return -1;
		}
	}
	
	/**
	 * @param agentId
	 * agentSignIn handles the sign in functionality of the delivery application
	 * It checks the status of the agent and changes the status to available 
	 * if agent is signed-out
	 * If agent is unavailable, then it does nothing
	 * If agent is signed out and if some order is yet to be assigned
	 * it allocates the lowest order id order to that agent and 
	 * makes status of agent to unavailable.
	 * @throws net.minidev.json.parser.ParseException 
	 */
	public int agentSignIn(int agentId) throws net.minidev.json.parser.ParseException {
		
		String url = "http://ankitdatabase:8083/unassignedOrders";
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
		
		System.out.println("TT : " + response.getBody());
		JSONParser parser = new JSONParser();
		JSONArray arr = (JSONArray) parser.parse(response.getBody());
		
		url = "http://ankitdatabase:8083/agentStatusChange";
		JSONObject postData = new JSONObject();
		postData.appendField("agentId", agentId);
		postData.appendField("status", "available");
		
		HttpEntity<Object> httpData = new HttpEntity<Object>(postData.toString(), headers);
		HttpStatus res = restTemplate.exchange(url, HttpMethod.PUT, httpData, String.class).getStatusCode();
		
		if(res != HttpStatus.CREATED)
			return -1;
		
		if(arr.size() != 0) {
			int orderId = Integer.MAX_VALUE;
			for(int i=0;i<arr.size();i++) {
				JSONObject obj = (JSONObject) arr.get(i);
				if((Integer) obj.get("orderId") < orderId)
					orderId = (Integer) obj.get("orderId");
			}
			
			
			
			
			url = "http://ankitdatabase:8083/assignOrder/";
			postData = new JSONObject();
			postData.appendField("orderId", orderId);
			postData.appendField("agentId", agentId);
			httpData = new HttpEntity<Object>(postData.toString(), headers);
			res = restTemplate.exchange(url, HttpMethod.PUT, httpData, String.class).getStatusCode();
			
			if(res != HttpStatus.CREATED)
				return -1;
		}
		
		
		
		return 1;
		
		/*String status = deliveryAgents.get(agentId);
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
		}*/
			
	}
	
	
	/**
	 * @param agentId
	 * agentSignOut handles the sign out functionality of the delivery application
	 * It checks the status of the agent and changes the status to signed-out if 
	 * status of agent is available
	 */
	public int agentSignOut(int agentId) {
		
		String url = "http://ankitdatabase:8083/agentStatusChange";
		JSONObject postData = new JSONObject();
		postData.appendField("agentId", agentId);
		postData.appendField("status", "signed-out");
		
		HttpEntity<Object> httpData = new HttpEntity<Object>(postData.toString(), headers);
		HttpStatus res = restTemplate.exchange(url, HttpMethod.PUT, httpData, String.class).getStatusCode();
		
		if(res != HttpStatus.CREATED)
			return -1;
		return 1;
	}
	
	
	/**
	 * @param orderId
	 * orderDelivered checks the status of the particular order and if the status of the 
	 * order is assigned then it changes the status of the order to assigned
	 * Also it changes the status of the agent to available
	 * If any other orders are pending to be assigned then it assigns the 
	 * lowest orderId order to the agent and makes the status of the order as assigned and also changes
	 * the status of agent to unavailable 
	 */
	public int orderDelivered(int orderId) {
		
		String url = "http://ankitdatabase:8083/deliverOrder/" + orderId;
		HttpStatus res = restTemplate.exchange(url, HttpMethod.PUT, null, String.class).getStatusCode();
		
		if(res != HttpStatus.CREATED)
			return -1;
		return 1;
		/*Order ord = orders.get(orderId);
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
		}*/
	}
	
	
	/**
	 * @param orderId
	 * getOrderDetails returns the JSONObject having 3 key value pairs
	 * 1. orderId : x
	 * 2. status : y (status of that particular order)
	 * 3. agantId : z (agent assigned to that order)
	 * @throws net.minidev.json.parser.ParseException 
	 */
	public JSONObject getOrderDetails(int orderId) throws net.minidev.json.parser.ParseException {
		
		String url = "http://ankitdatabase:8083/order/" + orderId;
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
		
		JSONParser parser = new JSONParser();
		JSONObject res = (JSONObject) parser.parse(response.getBody());
		
		JSONObject entity = new JSONObject();
		entity.appendField("orderId", res.get("orderId"));
		entity.appendField("custId", res.get("custId"));
		entity.appendField("status", res.get("status"));
		
		JSONObject restaurant = (JSONObject) res.get("restaurant");
		entity.appendField("restId", restaurant.get("restId"));
		entity.appendField("itemId", restaurant.get("itemId"));
		
		JSONObject agent = (JSONObject) res.get("agent");
		if(agent != null)
			entity.appendField("agentId", agent.get("agentId"));
		else
			entity.appendField("agentId", -1);
		return entity;
	}
	
	/**
	 * @param agentId
	 * getAgentDetails returns the JSONObject having 2 key value pairs
	 * 1. agentId: x
	 * 2. status : y ( y is having one of the three values (available, unavailable, signed-out))
	 * @throws net.minidev.json.parser.ParseException 
	 */
	public JSONObject getAgentDetails(int agentId) throws net.minidev.json.parser.ParseException {	
		String url = "http://ankitdatabase:8083/agent/" + agentId;
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
		
		JSONParser parser = new JSONParser();
		JSONObject entity = (JSONObject) parser.parse(response.getBody());
		return entity;
	}
	
	/**
	 * isAgentAvailable function checks if any status of any agent(in ascending order of agentId) is available or not
	 * If any agent is available then it makes status of that agent unavailable and returns the agentId
	 * of the agent available
	 * else returns -1
	 * @return
	 */
	public int isAgentAvailable() {
		for(Map.Entry<Integer, String> agent : deliveryAgents.entrySet() ) {
			if(agent.getValue().equalsIgnoreCase("available")) {
				agent.setValue("unavailable");
				return agent.getKey();
			}
		}
		return -1;
	}
	
	/**
	 * isAnyOrderUnAssigned checks if there are any orders(in ascending order of orderId) which are
	 * unassigned. If any order is unassigned then it changes its status to assigned and returns that 
	 * particular order id
	 * else returns -1;
	 * @return
	 */
	public int isAnyOrderUnAssigned() {
		for(Map.Entry<Integer, Order> order : orders.entrySet() ) {
			if(order.getValue().getStatus().equalsIgnoreCase("unassigned")) {
				order.getValue().setStatus("assigned");
				return order.getKey();
			}
		}
		return -1;
	}
	
	/**
	 * clearData is called when the reInitialize end point is called up
	 * All the orders are cleared irrespective of their status
	 * All the agents are signed out, that is their status is changed to signed-out 
	 * @throws IOException
	 */
	public void clearData() throws IOException {
		String url = "http://ankitdatabase:8083/reInitialize";
		restTemplate.exchange(url, HttpMethod.POST, null, String.class);
	}
	
}
