package com.webgis.ancientdata.road;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.webgis.ancientdata.modernreference.ModernReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import org.locationtech.jts.geom.MultiLineString;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "roads")
public class Road {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name="cat_nr")
    private int cat_nr;

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

    //parent
    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.LAZY)
    @JoinTable(name = "modernrefs_roads_mapping",
    joinColumns = @JoinColumn(name="road_id", referencedColumnName = "id"),
    inverseJoinColumns = @JoinColumn(name = "modernref_id", referencedColumnName = "id"))
    private List<ModernReference> modernReferenceList;

    public Road(int cat_nr,
                String name,
                MultiLineString geom,
                String type,
                String typeDescription,
                String location,
                String description,
                String date,
                String references,
                String historicalReferences) {
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
