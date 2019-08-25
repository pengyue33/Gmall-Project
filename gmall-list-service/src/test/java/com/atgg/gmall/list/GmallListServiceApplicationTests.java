package com.atgg.gmall.list;


import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallListServiceApplicationTests {
	@Autowired
	JestClient jestClient;

	@Test
	public void test1() throws Exception {
	     	 String query = "{\n" +
					 "  \"query\": {\n" +
					 "    \"term\": {\n" +
					 "      \"actorList.name\": \"张译\"\n" +
					 "    }\n" +
					 "  }\n" +
					 "}";
			 Search build = new Search.Builder(query).addIndex("movie_chn")
					 .addType("movie_type_chn").build();
		 SearchResult result = jestClient.execute(build);

		List<SearchResult.Hit<Map, Void>> hits = result.getHits(Map.class);
		for (SearchResult.Hit<Map, Void> hit : hits) {
			Map source = hit.source;
			 System.err.println(source);
		}


	}
}
