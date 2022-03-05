package com.iisc.pods.DeliveryDatabase.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Optional;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.iisc.pods.DeliveryDatabase.DeliveryDatabaseApplication;
import com.iisc.pods.DeliveryDatabase.pojo.Agent;
import com.iisc.pods.DeliveryDatabase.pojo.AgentRepository;
import com.iisc.pods.DeliveryDatabase.pojo.Key;
import com.iisc.pods.DeliveryDatabase.pojo.Orders;
import com.iisc.pods.DeliveryDatabase.pojo.OrderRespository;
import com.iisc.pods.DeliveryDatabase.pojo.Restaurant;
import com.iisc.pods.DeliveryDatabase.pojo.RestaurantRepository;

@Service
public class DatabaseService {
	
	@Autowired
	AgentRepository ar;

	@Autowired
	RestaurantRepository rr;
	
	@Autowired
	OrderRespository or;
	
	int db_orderId;
	
	public DatabaseService() {
		// TODO Auto-generated constructor stub
		db_orderId = 1000; // Set the order ID to 1000 in the initial state
	}
	
	private static final Logger log = LoggerFactory.getLogger(DeliveryDatabaseApplication.class);
	
	/**
	 * Initialize the H2 database with contents from initialData.txt
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void initialize() {
		try {
			File file = new File("initialData.txt");
		
			Scanner sc = new Scanner(file);
			
			int count = -1, restId = -1;
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				line = line.strip();
				if(line.contains("****"))
					break;
				if(count==-1) {
					restId = Integer.parseInt(line.split(" ")[0]);
					System.out.println(restId);
					count = Integer.parseInt(line.split(" ")[1]);
					if(count==0)
						count=-1;
				}
				else if(count>0) {
					Restaurant res = new Restaurant(restId, Integer.parseInt(line.split(" ")[0]), Integer.parseInt(line.split(" ")[1]));
					log.info(res.getPrice() + "");
					count--;
					res.toString();
					rr.save(res);
					if(count == 0)
						count=-1;
				}
			}
			
			while(sc.hasNextLine()) {
				String line = sc.nextLine();
				line = line.strip();
				if(line.contains("****"))
					break;
				else {
					ar.save(new Agent(Integer.parseInt(line)));
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
		
		log.info("All agents : ");
		for(Agent agent : ar.findAll()) {
			log.info(agent.toString());
		}
		
		log.info("");
		
		log.info("All restaurants : ");
		for(Restaurant res : rr.findAll()) {
			log.info(res.toString());
		}
		
	}
	
	
	/**
	 * Change the status of the agent
	 * @param agent
	 * @return
	 */
	public int changeAgentStatus(Agent agent) {
		String status = agent.getStatus();
		agent = ar.findById(agent.getAgentId());
		if(status.equals("signed-out") && agent.getStatus().equals("unavailable"))
			return 1;
		agent.setStatus(status);
		ar.save(agent);
		log.info(agent.toString());
		return 1;
	}
	
	/**
	 * Find the agent with the given agent ID
	 * @param agentId
	 * @return
	 */
	public Agent getAgent(int agentId) {
		Agent agent = ar.findById(agentId);
		return agent;
	}
	
	/**
	 * Create a new order with the given paramters
	 * @param restId
	 * @param itemId
	 * @param qty
	 * @param custId
	 * @return
	 */
	public Orders createOrder(int restId, int itemId, int qty, int custId) {
		
		log.info(restId + " " + itemId);
		Restaurant res = rr.findByRestIdAndItemId(restId, itemId);
		
		Orders ord = new Orders(db_orderId++, res, qty, custId);
		System.out.println(db_orderId);
		
		log.info("phew");
		int agid = Integer.MAX_VALUE;
		for(Agent agent : ar.findAll()) {
			log.info(agent.getAgentId() + "");
			if(agent.getStatus().equals("available") && agent.getAgentId() < agid) {
				agid = agent.getAgentId();
				log.info("aaya");
			}
				
		}
		log.info(agid + "");
		
		if(agid < Integer.MAX_VALUE)
		{
			ord.setStatus("assigned");
			Agent ag = ar.findById(agid);
			ord.setAgent(ag);
			ag.setStatus("unavailable");
			ar.save(ag);
		}
		
		or.save(ord);
		
		log.info("Saved");
		
		for(Orders order : or.findAll()) {
			log.info(order.toString());
		}
		
		return ord;
	}
	
	/**
	 * Find the order with the given order ID
	 * @param orderId
	 * @return
	 */
	public Orders getOrder(int orderId) {
		Orders order = or.findById(orderId);
		return order;
	}
	
	/**
	 * Change the status of the order from assigned to delivered
	 * @param orderId
	 * @return
	 */
	public int changeOrderStatus(int orderId) {
		Orders order = or.findById(orderId);
		order.setStatus("delivered");
		Agent agent = order.getAgent();
		agent.setStatus("available");
		or.save(order);
		ar.save(agent);
		return 1;
	}
	
	/**
	 * Return the list of orders that are in unassigned state.
	 * @return
	 */
	public Orders[] getUnassignedOrders() {
		return or.findAllByStatus("unassigned");
	}
	
	/**
	 * Assign the order to an available agent
	 * @param orderId
	 * @param agentId
	 * @return
	 */
	public int assignOrder(int orderId, int agentId) {
		Orders order = or.findById(orderId);
		Agent agent = ar.findById(agentId);
		agent.setStatus("unavailable");
		order.setAgent(ar.findById(agentId));
		order.setStatus("assigned");
		or.save(order);
		ar.save(agent);
		return 1;
	}
	
	/**
	 * Return the object in the Restaurant table with the given restaurant ID and item ID
	 * @param restId
	 * @param itemId
	 * @return
	 */
	public Restaurant getRestaurant(int restId, int itemId) {
		Restaurant res = rr.findByRestIdAndItemId(restId, itemId);
		return res;
	}
	
	/**
	 * Delete the data in all the tables, set the orderID variable to 1000 and reinitialize the data from initialData.txt
	 * @return
	 */
	public int freshInit() {
		
		or.deleteAll();
		ar.deleteAll();
		rr.deleteAll();
		db_orderId = 1000;
		System.out.println(db_orderId);
		initialize();
		return 1;
	}
}
