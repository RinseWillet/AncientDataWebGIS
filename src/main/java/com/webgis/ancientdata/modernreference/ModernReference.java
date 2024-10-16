package com.webgis.ancientdata.modernreference;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.webgis.ancientdata.road.Road;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    //    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH})
//    @JoinTable(name = "roads_modernref", joinColumns = @JoinColumn(name="modernref_fid"), inverseJoinColumns = @JoinColumn(name="roads_fid"))

    //child
    @ManyToMany(mappedBy = "modernReferenceSet", fetch = FetchType.LAZY)
    private List<Road> roadSet;

    //constructor
    public ModernReference(String shortRef, String fullRef, String URL) {
        this.shortRef = shortRef;
        this.fullRef = fullRef;
        this.URL = URL;
    }

    public List<Road> getRoads(){
        return roadSet;
    }

    public void setRoads(List<Road> roadSet){
        this.roadSet = roadSet;
    }

    public void addRoad(Road road) {
        this.roadSet.add(road);
    }
}


