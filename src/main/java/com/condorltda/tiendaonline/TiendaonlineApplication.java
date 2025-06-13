package com.condorltda.tiendaonline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class TiendaonlineApplication {

	public static void main(String[] args) {
		SpringApplication.run(TiendaonlineApplication.class, args);
	}

}
