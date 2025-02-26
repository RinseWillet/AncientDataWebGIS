package com.webgis.ancientdata.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.locationtech.jts.geom.MultiLineString;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

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

    @Min(value = 1, message = "Catalog number must be greater than 0")
    @Column(name="cat_nr")
    private int cat_nr;

    @NotBlank(message= "Name cannot be empty")
    @Size(max=255, message = " Name must be less than 255 characters")
    @Column(name="cat_name")
    private String name;

    @NotNull(message = "Geometry data (geom) is required")
    @Column(name = "geom")
    private MultiLineString geom;

    @NotBlank(message = "Type cannot be empty")
    @Size(max = 255, message = "Type must be less than 255 characters")
    @Column(name = "type")
    private String type;

    @Size(max = 1000, message = "Type description must be less than 1000 characters")
    @Column(name = "cat_type_descr")
    private String typeDescription;

    @Size(max = 1500, message = "Location details must be less than 1500 characters")
    @Column(name = "cat_location", length=1500)
    private String location;

    @Size(max = 5000, message = "Description must be less than 5000 characters")
    @Column(name = "cat_description", length=5000)
    private String description;

    @Column(name = "cat_date")
    private String date;

    @Size(max = 800, message = "References must be less than 800 characters")
    @Column(name = "cat_ref", length=800)
    private String references;

    @Size(max = 800, message = "Historical references must be less than 800 characters")
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
