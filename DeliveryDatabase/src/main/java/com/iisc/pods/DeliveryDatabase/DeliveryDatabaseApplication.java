package com.iisc.pods.DeliveryDatabase;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.iisc.pods.DeliveryDatabase.pojo.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Optional;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DeliveryDatabaseApplication {
	
	private static final Logger log = LoggerFactory.getLogger(DeliveryDatabaseApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(DeliveryDatabaseApplication.class, args);
	}
	
	/*@Bean
	  public CommandLineRunner demo(AgentRepository ar, RestaurantRepository rr, OrderRespository or) {
	    return (args) -> {
	    		
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
	    					Key key = new Key(restId, Integer.parseInt(line.split(" ")[0]));
	    					Restaurant res = new Restaurant(key, Integer.parseInt(line.split(" ")[1]));
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
	    };
	  }*/
}
