package com.webgis.ancientdata.domain.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@Entity
@NoArgsConstructor
@ToString
@Table(name = "modernrefs")
public class ModernReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    //abbreviation, e.g. Johnson 1980
    @Column(name = "short_ref")
    private String shortRef;

    //pagenumber
    @Column(name = "full_ref")
    private String fullRef;

    //full title, e.g. J. Johnson 1980 "The history of Johnsons", in A. Alan and G. George (eds.), Overview of histories of names, Oxford, pp. 12-34
    @Column(name = "url")
    private String URL;

    //child
    @ManyToMany(mappedBy = "modernReferenceList", fetch = FetchType.LAZY)
    private List<Road> roadList;

    //child
    @ManyToMany(mappedBy = "modernReferenceList", fetch = FetchType.LAZY)
    private List<Site> siteList;

    //constructor
    public ModernReference(String shortRef, String fullRef, String URL) {
        this.shortRef = shortRef;
        this.fullRef = fullRef;
        this.URL = URL;
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


