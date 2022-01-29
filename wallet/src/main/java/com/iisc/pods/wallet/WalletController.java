package com.iisc.pods.wallet;


import java.io.IOException;
import java.util.HashMap;

import com.iisc.pods.wallet.pojo.Customer;
import com.iisc.pods.wallet.service.TransactService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WalletController {
	
	@Autowired
	TransactService transactService;
	
	@PostMapping("/addBalance")
	public ResponseEntity<String> addMoney(@RequestBody HashMap<String, Integer> requestData) {
		if(transactService.addBal(requestData.get("custId"), requestData.get("amount")))
			return ResponseEntity.status(HttpStatus.CREATED).body(null);
		else
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	}
	
	@PostMapping("/deductBalance")
	public ResponseEntity<String> deductMoney(@RequestBody HashMap<String, Integer> requestData) {
		int status = transactService.deductBal(requestData.get("custId"), requestData.get("amount"));
		if(status == 1) {
			return ResponseEntity.status(HttpStatus.CREATED).body(null);
		}
		else if(status == 0)
			return ResponseEntity.status(HttpStatus.GONE).body(null);
		else
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
	}
	
	
	@GetMapping("/balance/{num}")
	public ResponseEntity<Customer> getBalance(@PathVariable("num") int num) {
		Customer cust = new Customer();
		cust.setCustId(num);
		cust.setBalance(transactService.getBal(num));
		return new ResponseEntity<Customer>(cust, HttpStatus.OK);
	}
	
	@PostMapping("/reInitialize")
	public ResponseEntity<String> reInit() throws IOException {
		transactService.freshInitWallet();
		return ResponseEntity.status(HttpStatus.CREATED).body(null);
	}
	
	@GetMapping("/initCust")
	public ResponseEntity<String> cust() {
		transactService.addCust();
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}

}
