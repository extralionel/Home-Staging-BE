package io.home.staging.entity;

import io.home.staging.model.response.JobStatusResponse;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "project")
@EntityListeners(AuditingEntityListener.class)
public class Project {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  private User user;

  @Column(name = "prompt", length = 20000)
  private String prompt;

  @OneToMany(
      mappedBy = "project",
      fetch = FetchType.LAZY,
      cascade = CascadeType.ALL,
      orphanRemoval = true
  )
  private List<Image> images = new ArrayList<>();

  @OneToOne
  private JobStatus job;

  @CreatedDate
  private Instant createdAt;

  public Project(User user, JobStatus job, String prompt) {
    this.user = user;
    this.job = job;
    this.prompt = prompt;
    this.createdAt = Instant.now();
  }

  @Transient
  public void addImage(Image image) {
    images.add(image);
  }
}
