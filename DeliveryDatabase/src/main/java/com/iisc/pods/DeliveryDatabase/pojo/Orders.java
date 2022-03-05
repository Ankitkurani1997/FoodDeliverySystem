package com.iisc.pods.DeliveryDatabase.pojo;

import java.util.Optional;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;

/**
 * 
 * @author sawrush
 * This class corresponds to a table "Orders" in the H2 database. It has the following fields : 
 * 		1. orderId : primary key of the table
 * 		2. restaurant : Foreign key to the Restaurant table. 
 * 		3. qty : The quantity of the item requested by the customer
 * 		4. custId : The ID of the customer who requested the order.
 * 		5. status : the status of the order.
 * 		6. agent : Foreign key to the Agent table.
 */


@Entity
public class Orders {
	
	@Id
	int orderId;
	
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "id")
	Restaurant restaurant;
	
	int qty;
	int custId;
	String status;
	
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "agentid")
	Agent agent;

	public Orders() {
		// TODO Auto-generated constructor stub
	}
	
	public Orders(int orderId, Restaurant restaurant, int qty, int custId) {
		super();
		this.orderId = orderId;
		this.restaurant = restaurant;
		this.qty = qty;
		this.custId = custId;
		this.status = "unassigned";
	}


	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public Restaurant getRestaurant() {
		return restaurant;
	}

	public void setRestaurant(Restaurant restaurant) {
		this.restaurant = restaurant;
	}

	public int getQty() {
		return qty;
	}

	public void setQty(int qty) {
		this.qty = qty;
	}

	public int getCustId() {
		return custId;
	}

	public void setCustId(int custId) {
		this.custId = custId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Agent getAgent() {
		return agent;
	}

	public void setAgent(Agent agent) {
		this.agent = agent;
	}


	@Override
	public String toString() {
		if(agent != null)
			return "Order [orderId=" + orderId + ", restaurant=" + restaurant.toString() + ", qty=" + qty + ", custId=" + custId
				+ ", status=" + status + ", agent=" + agent.toString() + "]";
		else
			return "Order [orderId=" + orderId + ", restaurant=" + restaurant.toString() + ", qty=" + qty + ", custId=" + custId
					+ ", status=" + status + ", agent=Agent [agentId=-1, status=-]]";
	}
	
	
}
