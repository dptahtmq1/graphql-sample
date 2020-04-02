package io.github.dptahtmq1.springgraphqlsample;

import graphql.kickstart.tools.GraphQLQueryResolver;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class Query implements GraphQLQueryResolver {

    private PostDao postDao;

    public List<Post> getRecentPosts(int count, int offset) {
        return postDao.getRecentPosts(count, offset);
    }

}
