package com.webgis.ancientdata.modernreference;

import jakarta.persistence.*;
import lombok.*;

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
    @Column(name="URL")
    private String URL;

    //constructor
    public ModernReference(String shortRef, String fullRef, String URL){
        this.shortRef = shortRef;
        this.fullRef = fullRef;
        this.URL=URL;
    }
}
