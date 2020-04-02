package io.github.dptahtmq1.springgraphqlsample;

import graphql.kickstart.tools.GraphQLResolver;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public class PostResolver implements GraphQLResolver<Post> {
    private AuthorDao authorDao;

    public Optional<Author> getAuthor(Post post) {
        return authorDao.getAuthor(post.getAuthorId());
    }
}