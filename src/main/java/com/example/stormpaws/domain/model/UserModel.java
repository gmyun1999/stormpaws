package com.example.stormpaws.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "UserModel",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"oauth_type", "oauth_id"})},
    indexes = {
      @Index(name = "idx_name", columnList = "name"),
      @Index(name = "idx_created_at", columnList = "created_at"),
      @Index(name = "idx_updated_at", columnList = "updated_at")
    })
public class UserModel {

  @Id
  @Column(length = 36)
  private String id;

  @Column(length = 64, nullable = true)
  private String name;

  @Column(length = 64, nullable = true)
  private String email;

  @Column(name = "mobile_no", length = 16, nullable = true)
  private String mobileNo;

  @Column(name = "oauth_type", length = 16, nullable = false)
  private String oauthType;

  @Column(name = "oauth_id", length = 64, nullable = false)
  private String oauthId;

  @Column(name = "tos_agreed", nullable = false)
  private boolean tosAgreed;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}
