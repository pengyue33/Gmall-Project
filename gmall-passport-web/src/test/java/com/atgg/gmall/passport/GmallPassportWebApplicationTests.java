package com.atgg.gmall.passport;

import com.atgg.gmall.passport.util.JwtUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPassportWebApplicationTests {

	@Test
	public void contextLoads() {
		String key = "atguigu";
		String ip="192.168.2.132";
		Map map = new HashMap();
		map.put("userId","1001");
		map.put("nickName","marry");

		String encode = JwtUtil.encode(key, map, ip);
		System.out.println(encode);
		//eyJhbGciOiJIUzI1NiJ9.
		// eyJuaWNrTmFtZSI6Im1hcnJ5IiwidXNlcklkIjoiMTAwMSJ9.
		// kvNHRXPQh-6SfNGLD6t_dYWtQX0TXzx2WWi_O1KukJk
	}

}
