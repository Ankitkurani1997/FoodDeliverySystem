package com.iisc.pods.DeliveryDatabase.pojo;

import org.springframework.data.repository.CrudRepository;

public interface AgentRepository extends CrudRepository<Agent, Integer> {
	
	Agent findById(int id);
}
