package jpabook.jpashop.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long Id;

    private String name;

    @Embedded
    private Address address;

    @JsonIgnore //양방향 연관관계에서는 한쪽은 무조건 JsonIgnore 해줘야 함, 안그러면 Jackson 등 라이브러리들이 Json 만들면서 무한루프에 빠짐
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();
}
