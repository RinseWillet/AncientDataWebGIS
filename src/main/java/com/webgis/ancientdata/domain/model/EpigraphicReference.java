package com.webgis.ancientdata.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Entity
@NoArgsConstructor
@ToString
@Table(name = "eprefs")
public class EpigraphicReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    //abbreviation, e.g. CIL X.1624
    @Column(name="name")
    private String name;

    //corpus, e.g. CIL, SEG, ILS, etc.
    @Column(name="corpus")
    private String author;

    //book, e.g. X
    @Column(name="book")
    private String work;

    //number, e.g. 1624
    @Column(name="number")
    private Integer number;

    //link, url to online corpora
    @Column(name="link")
    private String link;

    public EpigraphicReference(String name, String author, String work, Integer number, String link) {
        this.name = name;
        this.author = author;
        this.work = work;
        this.number = number;
        this.link = link;
    }
}
