package com.iisc.pods.restaurant.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.iisc.pods.restaurant.pojo.Restaurant;

@Service
public class InventoryService {
	
	HashMap<Integer, HashMap<Integer, Integer> > restaurants = new HashMap<>();
	
	public int acceptOrder(int restId, int itemId, int qty) {
		if(restaurants.containsKey(restId) && restaurants.get(restId).containsKey(itemId)) {
			if(qty <= restaurants.get(restId).get(itemId))
			{
				restaurants.get(restId).put(itemId, restaurants.get(restId).get(itemId) - qty);
				return 1;
			}
			else
				return 0;
		}
		return -1;
	}
	
	
	
	@EventListener(ApplicationReadyEvent.class)
	public void initRestaurants() throws IOException {
		
		try {
			File file = new File("initialData.txt");
		
			Scanner sc = new Scanner(file);
			
			int count = -1, restId = -1;
			HashMap<Integer, Integer> items = new HashMap<>();
			while(sc.hasNextLine())
			{
				String line = sc.nextLine();
				if(line.contains("****"))
					break;
				if(count==-1) {
					restId = Integer.parseInt(line.split(" ")[0]);
					count = Integer.parseInt(line.split(" ")[1]);
				}
				else if(count>0) {
					items.put(Integer.parseInt(line.split(" ")[0]), Integer.parseInt(line.split(" ")[2]));
					count--;
					if(count==0) {
						restaurants.put(restId, items);
						restId = -1;
						items = new HashMap<>();
						count = -1;
					}		
				}
			}
			sc.close();
		}
		catch(FileNotFoundException e) {
			System.out.println("Initialialization file not found!");
		}
		catch(NumberFormatException e) {
			e.printStackTrace();
		}
	}
	
	public void freshInitRestaurants() throws IOException {
		restaurants = new HashMap<>();
		this.initRestaurants();
	}
	
	public void addRest() {
		HashMap<Integer, Integer> t = new HashMap<>();
		t.put(1, 20);
		t.put(2, 13);
		restaurants.put(101, t);
	}
	
	public int refill(int restId, int itemId, int qty) {
		if(restaurants.containsKey(restId) && restaurants.get(restId).containsKey(itemId)) {
			restaurants.get(restId).put(itemId, restaurants.get(restId).get(itemId) + qty);
			return 1;
		}
		return -1;
	}
	
	public Restaurant getRest(int restId) {
		Restaurant res = new Restaurant();
		res.setRestId(restId);
		res.setItems(restaurants.get(restId));
		return res;
	}
}
