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
	
	private final String URI_WALLET_DEDUCTBALANCE = "http://localhost:8080/deductBalance";
	private final String URI_WALLET_ADDBALANCE = "http://localhost:8080/addBalance";
	private final String URI_RESTAURANT = "http://localhost:8080/acceptOrder";
	
	HashMap<Integer, List<Item> > restaurants = new HashMap<>(); 
	Map<Integer, String> deliveryAgents = new TreeMap<>();
	List<Integer> listOfCustomers = new ArrayList<>();
	List<Order> orders = new ArrayList<>();
	
	int globalOrderId;
	
	public OrderService(){
		globalOrderId = 1000;
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
			int restId = 0, noOfitems = 0;
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
							noOfitems = Integer.parseInt(str[1]);
							System.out.println(restId + " " + noOfitems);
						}
						else
						{
							int itemId = Integer.parseInt(str[0]), price = Integer.parseInt(str[1]), 
									qty = Integer.parseInt(str[2]);
							addRestaurant(restId, noOfitems, itemId, price, qty);
							System.out.println("item: " + itemId  + " price " + price + " qty " + qty);
						}
					}
					else if(counter == 1)
					{
						int agentId = Integer.parseInt(line);
						deliveryAgents.put(agentId, "available");
						System.out.println("agenetId is " + agentId);
					}
					else if(counter == 2)
					{
						int custId = Integer.parseInt(line);
						listOfCustomers.add(custId);
						System.out.println("custId is " + custId);
					}
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		
	}
	
	public void addRestaurant(int restId, int noOfitems, int itemId, int price, int qty)
	{
		Item item  = new Item();
		item.setItemId(itemId);
		item.setPrice(price);
		item.setQty(qty);
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
		
		int totalBill = computeTotalBill(ord.getRestId(), ord.getItemId(), ord.getQty());
		
		RestTemplate restTemplate = new RestTemplate();
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		JSONObject entityWallet = new JSONObject();
		entityWallet.appendField("custId", ord.getCustId());
		entityWallet.appendField("amount", totalBill);
		
		HttpEntity<String> httpEntityWallet = new HttpEntity<String>(entityWallet.toString(), headers);
		
		ResponseEntity<String> responseWalltet = restTemplate.exchange(URI_WALLET_DEDUCTBALANCE, HttpMethod.POST, httpEntityWallet, String.class);
		
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
			
			HttpEntity<String> httpEntityRestaurant = new HttpEntity<String>(entityWallet.toString(), headers);
			ResponseEntity<String> responseRestaurant = restTemplate.exchange(URI_RESTAURANT, HttpMethod.POST, httpEntityRestaurant, String.class);
			
			if(responseRestaurant.getStatusCode() != HttpStatus.CREATED)
			{
				responseWalltet = restTemplate.exchange(URI_WALLET_ADDBALANCE, HttpMethod.POST, httpEntityWallet, String.class);
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
				orders.add(ord);
				System.out.println("Balance Reduced");
				return id;
			}
		}
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
	
	public void clearData() throws IOException {
		restaurants.clear();
		deliveryAgents.clear();
		listOfCustomers.clear();
		
		initializeData();
		
		System.out.println(restaurants.size());
		System.out.println(deliveryAgents.size());
		System.out.println(listOfCustomers.size());
	}
	
}