package io.github.dptahtmq1.springgraphqlsample;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Author {
    private String id;
    private String name;
    private String thumbnail;
}