package com.example.stormpaws.domain.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
public class DeckModel {
  @Id
  @Column(length = 36)
  private String id;

  @Column(name = "Deck_name", length = 36, nullable = false)
  private String name;

  @ManyToOne
  @JoinColumn(name = "user_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
  @JsonManagedReference
  private UserModel user;

  @OneToMany(mappedBy = "deck")
  @JsonManagedReference
  private List<DeckCardModel> decklist;

  @Column(name = "is_default", nullable = false)
  private boolean isDefaultDeck; // 기본 덱 여부

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
}
