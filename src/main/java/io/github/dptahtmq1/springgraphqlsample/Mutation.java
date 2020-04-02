package io.github.dptahtmq1.springgraphqlsample;

import graphql.kickstart.tools.GraphQLMutationResolver;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class Mutation implements GraphQLMutationResolver {
    private PostDao postDao;

    public Post writePost(String title, String text, String category, String author) {
        Post post = new Post();
        post.setId(UUID.randomUUID().toString());
        post.setTitle(title);
        post.setText(text);
        post.setCategory(category);
        post.setAuthorId(author);
        postDao.savePost(post);

        return post;
    }
}