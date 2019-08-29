package com.atgg.gmall.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.atgg.gmall.cart.mapper")
@ComponentScan(basePackages = "com.atgg.gmall") //扫描redis工具类
public class GmallCartServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallCartServiceApplication.class, args);
	}

}
