package com.atgg.gmall.order;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.LinkedList;

//@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallOrderWebApplicationTests {

	@Test
	public void contextLoads() {
		LinkedList<Integer> ints = new LinkedList<Integer>();
		ints.add(1);
		ints.add(2);
		System.out.println(ints.get(1));

	}

}
