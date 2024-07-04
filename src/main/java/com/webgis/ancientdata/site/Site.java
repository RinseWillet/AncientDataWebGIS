package com.webgis.ancientdata.site;

import com.webgis.ancientdata.ancientreference.AncientReference;
import com.webgis.ancientdata.epigraphicreference.EpigraphicReference;
import com.webgis.ancientdata.modernreference.ModernReference;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import java.io.Serializable;
import java.util.ArrayList;

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

    //type of site
    @Column(name="type")
    private SiteType type;

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

    //modernreference(s)
    @Column(name = "modrefs")
    private ArrayList<ModernReference> modernReferences;

    //ancientreference(s)
    @Column(name = "ancrefs")
    private ArrayList<AncientReference> ancientReferences;

    //epigraphicreference(s)
    @Column(name = "eprefs")
    private ArrayList<EpigraphicReference> epigraphicReferences;

    public Site(Integer pleiadesId,
                String name,
                Point geom,
                String province,
                SiteType type,
                String conventus,
                String status,
                String statusReference,
                String comment,
                ArrayList<ModernReference> modernReferences,
                ArrayList<AncientReference> ancientReferences,
                ArrayList<EpigraphicReference> epigraphicReferences) {
        this.pleiadesId = pleiadesId;
        this.name = name;
        this.geom = geom;
        this.province = province;
        this.type = type;
        this.conventus = conventus;
        this.status = status;
        this.statusReference = statusReference;
        this.comment = comment;
        this.modernReferences = modernReferences;
        this.ancientReferences = ancientReferences;
        this.epigraphicReferences = epigraphicReferences;
    }
}