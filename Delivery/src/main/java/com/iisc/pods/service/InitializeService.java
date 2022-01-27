package com.iisc.pods.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.iisc.pods.pojo.Item;

@Service
public class InitializeService {
	
	HashMap<Integer, List<Item> > restaurants = new HashMap<>(); 
	HashMap<Integer, String> deliveryAgents = new HashMap<>();
	List<Integer> listOfCustomers = new ArrayList<>();
	
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
						addAgents(agentId, "available");
						System.out.println("agenetId is " + agentId);
					}
					else if(counter == 2)
					{
						int custId = Integer.parseInt(line);
						listOfCustomers.add(custId);
						System.out.println("custId is " + custId);
					}
					else if(counter == 3)
					{
						int walletMoney = Integer.parseInt(line);
						System.out.println("Initial Money is " + walletMoney);
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
	
	public void addAgents(int agenId, String status)
	{
		deliveryAgents.put(agenId, status);
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
