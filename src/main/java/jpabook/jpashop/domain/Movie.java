package jpabook.jpashop.domain;

import javax.persistence.Entity;

@Entity
public class Movie extends Item {
    private String artist;
    private String etc;
}