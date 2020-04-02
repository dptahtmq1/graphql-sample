package io.github.dptahtmq1.springgraphqlsample;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    private String id;
    private String title;
    private String text;
    private String category;
    private String authorId;
}
