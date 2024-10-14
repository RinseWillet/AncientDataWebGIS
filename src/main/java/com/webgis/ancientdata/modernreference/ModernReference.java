package com.webgis.ancientdata.modernreference;

import com.webgis.ancientdata.road.Road;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Data
@Entity
@NoArgsConstructor
@ToString
@Table(name = "modernref")
public class ModernReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    //abbreviation, e.g. Johnson 1980
    @Column(name="short_ref")
    private String shortRef;

    //pagenumber
    @Column(name="full_ref")
    private String fullRef;

    //full title, e.g. J. Johnson 1980 "The history of Johnsons", in A. Alan and G. George (eds.), Overview of histories of names, Oxford, pp. 12-34
    @Column(name="url")
    private String URL;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE,
                    CascadeType.DETACH,
                    CascadeType.REFRESH})
    @JoinTable(name = "roads_modernref", joinColumns = @JoinColumn(name="modernref_fid"),
        inverseJoinColumns = @JoinColumn(name="roads_fid"))
    private Set<Road> roadSet;

    //constructor
    public ModernReference(String shortRef, String fullRef, String URL){
        this.shortRef = shortRef;
        this.fullRef = fullRef;
        this.URL=URL;
    }
}
