package com.webgis.ancientdata.road;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.locationtech.jts.geom.MultiLineString;

@Data
@Entity
@NoArgsConstructor
@ToString
@Table(name = "roads")
public class Road {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name="cat_nr")
    private long cat_nr;

    @Column(name="cat_name")
    private String name;

    @Column(name = "geom")
    private MultiLineString geom;

    @Column(name = "type")
    private String type;

    @Column(name = "cat_type_descr")
    private String typeDescription;

    @Column(name = "cat_location", length=1500)
    private String location;

    @Column(name = "cat_description", length=5000)
    private String description;

    @Column(name = "cat_date")
    private String date;

    @Column(name = "cat_ref", length=800)
    private String references;

    @Column(name = "cat_hist_ref", length=800)
    private String historicalReferences;

    public Road(long id,
                long cat_nr,
                String name,
                MultiLineString geom,
                String type,
                String typeDescription,
                String location,
                String description,
                String date,
                String references,
                String historicalReferences) {
        this.id = id;
        this.cat_nr = cat_nr;
        this.name = name;
        this.geom = geom;
        this.type = type;
        this.typeDescription = typeDescription;
        this.location = location;
        this.description = description;
        this.date = date;
        this.references = references;
        this.historicalReferences = historicalReferences;
    }
}
