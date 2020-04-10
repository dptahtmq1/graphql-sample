package io.github.dptahtmq1.springgraphqlsample;

import com.graphql.spring.boot.test.GraphQLResponse;
import com.graphql.spring.boot.test.GraphQLTestTemplate;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpringGraphqlSampleApplicationTests {

    @Autowired
    private GraphQLTestTemplate graphQLTestTemplate;

    @Order(1)
    @Test
    public void testGetPost() throws Exception {
        // Given

        // When
        GraphQLResponse response = graphQLTestTemplate.postForResource("graphql/getPost.graphql");

        // Then
        assertNotNull(response);
        assertTrue(response.isOk());
        assertEquals("Post00", response.get("$.data.recentPosts[0].id"));
    }

    @Order(2)
    @Test
    public void testWritePost() throws Exception {
        // Given

        // When
        GraphQLResponse response = graphQLTestTemplate.postForResource("graphql/writePost.graphql");

        // Then
        assertNotNull(response);
        assertTrue(response.isOk());
        assertNotNull(response.get("$.data.writePost.id"));
    }

}
