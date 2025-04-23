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
@ToString(exclude = {"roadList", "siteList"})
@Table(name = "modernrefs")
public class ModernReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    //abbreviation, e.g. Johnson 1980
    @Column(name = "short_ref")
    private String shortRef;

    //full title, e.g. J. Johnson 1980 "The history of Johnsons", in A. Alan and G. George (eds.), Overview of histories of names, Oxford, pp. 12-34
    @Column(name = "full_ref")
    private String fullRef;

    @Column(name = "url")
    private String url;

    //child
    @JsonIgnore
    @ManyToMany(mappedBy = "modernReferenceList", fetch = FetchType.LAZY)
    private List<Road> roadList;

    //child
    @JsonIgnore
    @ManyToMany(mappedBy = "modernReferenceList", fetch = FetchType.LAZY)
    private List<Site> siteList;

    //constructor
    public ModernReference(String shortRef, String fullRef, String url) {
        this.shortRef = shortRef;
        this.fullRef = fullRef;
        this.url = url;
    }

    public List<Road> getRoads(){
        return roadList;
    }

    public void setRoads(List<Road> roadSet){
        this.roadList = roadSet;
    }

    public void addRoad(Road road) {
        this.roadList.add(road);
    }

    public List<Site> getSites(){
        return siteList;
    }

    public void setSites(List<Site> siteSet){
        this.siteList = siteSet;
    }

    public void addSite(Site site) {
        this.siteList.add(site);
    }
}