package io.github.dptahtmq1.springgraphqlsample;

import graphql.kickstart.tools.GraphQLResolver;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class AuthorResolver implements GraphQLResolver<Author> {
    private PostDao postDao;
    
    public List<Post> getPosts(Author author) {
        return postDao.getAuthorPosts(author.getId());
    }
}