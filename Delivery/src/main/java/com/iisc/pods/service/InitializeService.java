package com.iisc.pods.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import com.iisc.pods.pojo.Item;

@Service
public class InitializeService {//implements ApplicationRunner{
	
	HashMap<Integer, Pair<Integer, List<Item>>> restaurants = new HashMap<>(); 
	
	@EventListener(ApplicationReadyEvent.class)
	public void initializeData() throws IOException
	{
		System.out.println("***************************************Hello");
		File file = new File("initialData.txt");
		FileReader fileReader = new FileReader(file);
		try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
			String pattern = "****";
			String line;
			int counter = 0;
			int restId = 0, items = 0;
			while((line = bufferedReader.readLine())!=null) {
				//System.out.println(line);
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
							items = Integer.parseInt(str[1]);
							System.out.println(restId + " " + items);
						}
						else
						{
							int itemId = Integer.parseInt(str[0]), price = Integer.parseInt(str[1]), 
									qty = Integer.parseInt(str[2]);
							if(!addRestaurant(restId, items, itemId, price, qty))
							{
								System.out.println("Not able to add items for restaurant id: " + restId);
							}
							System.out.println("item: " + itemId  + " price " + price + " qty " + qty);
						}
					}
					else if(counter == 1)
					{
						int agentId = Integer.parseInt(line);
						System.out.println("agenetId is " + agentId);
					}
					else if(counter == 2)
					{
						int custId = Integer.parseInt(line);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public boolean addRestaurant(int restId, int items, int itemId, int price, int qty)
	{
		Pair<Integer, List<Item> > p;
		//restaurants.put(restId, pai)
		return true;
	}
	
	/*@Override
	public void run(ApplicationArguments args) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("ok dokie");
	}*/
	
}
