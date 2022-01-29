package com.iisc.pods.wallet.service;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import com.iisc.pods.wallet.pojo.Customer;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class TransactService {
	
	HashMap<Integer, Integer> customers = new HashMap<>();
	
	public boolean addBal(int custId, int amount) {
		if(customers.containsKey(custId)) {
			customers.put(custId, customers.get(custId)+amount);
			return true;
		}
		return false;
	}
	
	public int deductBal(int custId, int amount) {
		if(customers.containsKey(custId) && customers.get(custId) >= amount) {
			customers.put(custId, customers.get(custId)-amount);
			return 1;
		}
		else if(customers.get(custId) < amount)
			return 0;
		return -1;
	}
	
	
	public int getBal(int custId) {
		if(customers.containsKey(custId))
			return customers.get(custId);
		return -1;
	}
	
	public void addCust() {
		customers.put(301, 200000);
		customers.put(302, 200000);
		customers.put(303, 200000);
	}
	
	
	@EventListener(ApplicationReadyEvent.class)
	public void initWallet() throws IOException {
		
		try {
			File file = new File("initialData.txt");
		
			Scanner sc = new Scanner(file);
			
			int skip_count = 3, wallet_bal = 0;
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				if(line.contains("****") && skip_count>0)
					skip_count--;
				else if(skip_count==0)
					wallet_bal = Integer.parseInt(line);
			}
			System.out.println(wallet_bal);
			
			sc.close();
			sc = new Scanner(file);
			skip_count = 2;
			
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				if(line.contains("****") && skip_count>0)
					skip_count--;
				else if(skip_count==0 && line.contains("****"))
					break;
				else if(skip_count==0) {
					int custId = Integer.parseInt(line);
					customers.put(custId, wallet_bal);
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
	
	public void freshInitWallet() throws IOException {
		customers = new HashMap<>();
		this.initWallet();
	}
	
}
