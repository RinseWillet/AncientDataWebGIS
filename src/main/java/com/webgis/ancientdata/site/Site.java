package com.webgis.ancientdata.site;

import com.webgis.ancientdata.modernreference.ModernReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;

import org.locationtech.jts.geom.Point;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "arch_sites")
public class Site implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name="pleiadesid")
    private Integer pleiadesId;

    @Column(name="name")
    private String name;

    @Column(name = "geom")
    private Point geom;

    // province during 2nd c. CE
    @Column(name = "province")
    private String province;

    //type of site
    @Column(name="sitetype")
    private String siteType;

    //juridical status of settlement in 2nd c. CE
    @Column(name="status")
    private String status;

    //reference and commentary for juridical status
    @Column(name = "statusref", length = 800)
    private String statusReference;

    //comment
    @Column(name = "comment", length = 800)
    private String comment;

//    //ancientreference(s)
//    @Column(name = "ancrefs")
//    private ArrayList<AncientReference> ancientReferences;
//
//    //epigraphicreference(s)
//    @Column(name = "eprefs")
//    private ArrayList<EpigraphicReference> epigraphicReferences;

    //parent
    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinTable(name = "modernrefs_sites_mapping",
            joinColumns = @JoinColumn(name="site_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "modernref_id", referencedColumnName = "id"))
    private List<ModernReference> modernReferenceList;

    public Site(Integer pleiadesId,
                String name,
                Point geom,
                String province,
                String siteType,
                String status,
                String statusReference,
                String comment
                ) {
        this.pleiadesId = pleiadesId;
        this.name = name;
        this.geom = geom;
        this.province = province;
        this.siteType = siteType;
        this.status = status;
        this.statusReference = statusReference;
        this.comment = comment;
    }

    public List<ModernReference> getModernReferences(){
        return modernReferenceList;
    }

    public void setModernReferences(List<ModernReference> modernReferenceSet) {
        this.modernReferenceList = modernReferenceSet;
    }

    public void addModernReference(ModernReference modernReference) {
        this.modernReferenceList.add(modernReference);
    }
}