package com.webgis.ancientdata.ancientreference;

import jakarta.persistence.*;
import lombok.*;

@Data
@Entity
@NoArgsConstructor
@ToString
@Table(name = "ancientref")
public class AncientReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    //abbreviation, e.g. Strabo XII.1.2
    @Column(name="name")
    private String name;

    //author
    @Column(name="author")
    private String author;

    //work, e.g. Geographika
    @Column(name="work")
    private String work;

    //booknumber, e.g. XII
    @Column(name="book")
    private String book;

    //paragraphnumber
    @Column(name="page")
    private Integer page;

    //constructor
    public AncientReference(String name, String author, String work, String book, Integer page) {
        this.name = name;
        this.author = author;
        this.work = work;
        this.book = book;
        this.page = page;
    }
}