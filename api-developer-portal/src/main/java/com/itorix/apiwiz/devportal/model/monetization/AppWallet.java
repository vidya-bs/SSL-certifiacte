package com.itorix.apiwiz.devportal.model.monetization;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.itorix.apiwiz.common.model.AbstractObject;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Document("App.Wallet.Balance")
public class AppWallet extends AbstractObject {
	private String appId;
	private String appName;
	private String developerEmailId;
	private double balance;

	public void debit(double amount){
		if(amount <= balance){
			balance -= amount;
		}
	}

	public void credit(double amount){
		balance += amount;
	}
}
