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
    @Column(name="name")
    private String name;

    //pagenumber
    @Column(name="page")
    private Integer page;

    //full title, e.g. J. Johnson 1980 "The history of Johnsons", in A. Alan and G. George (eds.), Overview of histories of names, Oxford, pp. 12-34
    @Column(name="title")
    private String title;

    //constructor
    public ModernReference(String name, Integer page, String title){
        this.name = name;
        this.page = page;
        this.title=title;
    }
}
