package com.webgis.ancientdata.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.locationtech.jts.geom.Point;

import java.io.Serializable;
import java.util.List;

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

    //modern reference
    @Column(name = "ref", length = 800)
    private String references;

    //description
    @Column(name = "description", length=5000)
    private String description;

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
                String references,
                String description
                ) {
        this.pleiadesId = pleiadesId;
        this.name = name;
        this.geom = geom;
        this.province = province;
        this.siteType = siteType;
        this.status = status;
        this.references = references;
        this.description = description;
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