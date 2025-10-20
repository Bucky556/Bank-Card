package code.uz.bankcard.entity;

import code.uz.bankcard.enums.GeneralStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "profile")
@Getter
@Setter
public class ProfileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;
    @Column(name = "username")
    private String username;
    @Column(name = "password")
    private String password;
    @Column(name = "created_date")
    private LocalDateTime createdDate;
    @Column(name = "visible")
    private Boolean visible = true;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private GeneralStatus status;

    @OneToMany(mappedBy = "profile", fetch = FetchType.LAZY)
    private List<RoleEntity> role;
}
