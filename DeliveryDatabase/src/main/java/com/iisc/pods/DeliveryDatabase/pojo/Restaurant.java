package com.iisc.pods.DeliveryDatabase.pojo;


import java.util.ArrayList;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

/**
 * 
 * @author sawrush
 * This class corresponds to the "Restaurant" table in the H2 database. It has the following fields:
 * 		1. id : an auto generated field that is used as the primary key.
 * 		2. restId : Restaurant ID
 * 		3. itemId : Item ID
 * 		4. price : the price of the item.
 */

@Entity
public class Restaurant {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	int id;
	
	int restId;
	
	int itemId;
	
	int price;
	
	@OneToMany(mappedBy = "restaurant")
	Set<Orders> orders;
	
	public Restaurant() {
		// TODO Auto-generated constructor stub
	}
	
	
	public Restaurant(int restId, int itemId, int price) {
		super();
		this.restId = restId;
		this.itemId = itemId;
		this.price = price;
	}
	
	
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public int getRestId() {
		return restId;
	}


	public void setRestId(int restId) {
		this.restId = restId;
	}


	public int getItemId() {
		return itemId;
	}


	public void setItemId(int itemId) {
		this.itemId = itemId;
	}


	@Override
	public String toString() {
		return "Restaurant [id=" + id + ", restId=" + restId + ", itemId=" + itemId + ", price=" + price + "]";
	}


	
	
}
