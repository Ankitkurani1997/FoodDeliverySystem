package com.iisc.pods.DeliveryDatabase.pojo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface OrderRespository extends CrudRepository<Orders, Integer> {
	Orders findById(int orderId);
	
	Orders[] findAllByStatus(String status);
	
}
