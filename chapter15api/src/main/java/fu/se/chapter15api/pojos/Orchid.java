package fu.se.chapter15api.pojos;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "Orchid", schema = "dbo")
public class Orchid {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "OrchidID")
    private Integer orchidId;

    @NotBlank
    @Size(max = 150)
    @Column(name = "OrchidName", nullable = false, length = 150)
    private String orchidName;

    @Column(name = "IsNatural", nullable = false)
    private boolean isNatural;

    @Size(max = 1000)
    @Column(name = "OrchidDescription", length = 1000)
    private String orchidDescription;

    @Size(max = 100)
    @Column(name = "OrchidCategory", length = 100)
    private String orchidCategory;

    @Column(name = "IsAttractive", nullable = false)
    private boolean isAttractive;

    @Size(max = 1000)
    @Column(name = "OrchidURL", length = 1000)
    private String orchidURL;

    public Orchid() {
    }

    public Integer getOrchidId() {
        return orchidId;
    }

    public void setOrchidId(Integer orchidId) {
        this.orchidId = orchidId;
    }

    public String getOrchidName() {
        return orchidName;
    }

    public void setOrchidName(String orchidName) {
        this.orchidName = orchidName;
    }

    public boolean isNatural() {
        return isNatural;
    }

    public void setNatural(boolean natural) {
        isNatural = natural;
    }

    public String getOrchidDescription() {
        return orchidDescription;
    }

    public void setOrchidDescription(String orchidDescription) {
        this.orchidDescription = orchidDescription;
    }

    public String getOrchidCategory() {
        return orchidCategory;
    }

    public void setOrchidCategory(String orchidCategory) {
        this.orchidCategory = orchidCategory;
    }

    public boolean isAttractive() {
        return isAttractive;
    }

    public void setAttractive(boolean attractive) {
        isAttractive = attractive;
    }

    public String getOrchidURL() {
        return orchidURL;
    }

    public void setOrchidURL(String orchidURL) {
        this.orchidURL = orchidURL;
    }
}
