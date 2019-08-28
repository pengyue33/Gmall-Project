package com.atgg.gmall.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@MapperScan(basePackages = "com.atgg.gmall.user.mapper")
@SpringBootApplication
@ComponentScan(basePackages = "com.atgg.gmall")
public class GmallUsermanageApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallUsermanageApplication.class, args);
	}

}
