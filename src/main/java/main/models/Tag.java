package main.models;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "tags")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)

    @Column(name = "tag_id")
    Long tagId;
    @Column(name = "q_id")
    Long q_id;
    @Column(name = "tag")
    String tag;
}
