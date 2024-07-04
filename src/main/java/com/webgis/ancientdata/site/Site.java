package com.webgis.ancientdata.site;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import java.io.Serializable;

@Data
@Entity
@NoArgsConstructor
@ToString
@Table(name = "arch_sites")
public class Site implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name="pleiades_i")
    private Integer pleiadesId;

    @Column(name="name")
    private String name;

    @Column(name = "geom")
    private Point geom;

    // province during 2nd c. CE
    @Column(name = "province")
    private String province;

    //type of site (settlement, settlement with stone buildings, juridical city, fort, legionary fortress)
    @Column(name="type")
    private String type;

    //to which Assize district a settlement (only cities) belonged
    @Column(name = "conventu_1")
    private String conventus;

    //juridical status of settlement in 2nd c. CE
    @Column(name="status")
    private String status;

    //reference and commentary for juridical status
    @Column(name = "status_ref")
    private String statusReference;

    //comment
    @Column(name = "comment")
    private String comment;

    //references, historical or secondary
//    @Column(name = "refs")
//    private ArrayList<>

    //constructor
    public Site (Integer pleiadesId, String name, Point geom, String province, String conventus, String type, String status, String statusReference, String comment){
        this.pleiadesId = pleiadesId;
        this.name = name;
        this.geom = geom;
        this.province = province;
        this.conventus = conventus;
        this.type = type;
        this.status = status;
        this.statusReference = statusReference;
        this.comment = comment;
    }
}