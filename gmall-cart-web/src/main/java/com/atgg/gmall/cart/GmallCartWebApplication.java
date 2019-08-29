package com.atgg.gmall.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.atgg.gmall")//拦截器扫描
public class GmallCartWebApplication {

	public static void main(String[] args) {

		SpringApplication.run(GmallCartWebApplication.class, args);
	}

}
