package com.hospital.app.entities.account;

import com.hospital.app.entities.EntityLayout;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@Entity
@Table(name = "Tokens")
@AllArgsConstructor
@NoArgsConstructor
public class Token extends EntityLayout {
    //Attributes
    @ColumnDefault("FALSE")
    private Boolean isMobile;
    @Column(columnDefinition = "LONGTEXT")
    private String accessToken;
    @Column(columnDefinition = "LONGTEXT")
    private String refreshToken;
    @Temporal(TemporalType.TIMESTAMP)
    private Date expiredAt;
    //Relationships
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "userId",referencedColumnName = "id",nullable = false)
    private User user;
}
