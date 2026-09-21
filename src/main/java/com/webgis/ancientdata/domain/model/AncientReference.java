package com.webgis.ancientdata.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@Entity
@NoArgsConstructor
@ToString
@Table(name = "ancientrefs")
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

    //child
    @JsonIgnore
    @ManyToMany(mappedBy = "ancientReferenceList", fetch = FetchType.LAZY)
    private List<Site> siteList;

    public List<Site> getSites(){
        return siteList;
    }

    public void setSites(List<Site> siteSet) {
        this.siteList = siteSet;
    }

    public void addSite(Site site) {
        this.siteList.add(site);
    }
}