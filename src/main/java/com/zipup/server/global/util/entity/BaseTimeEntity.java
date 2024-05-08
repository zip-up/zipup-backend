package com.zipup.server.global.util.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseTimeEntity {
  @CreatedDate
  @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
  @Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createdDate;

  @LastModifiedDate
  @Column(columnDefinition = "TIMESTAMP")
  @Convert(converter = Jsr310JpaConverters.LocalDateTimeConverter.class)
  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime modifiedDate;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "ENUM('PRIVATE', 'PUBLIC', 'UNLINK') DEFAULT 'PUBLIC'")
  @Setter
  @Builder.Default
  private ColumnStatus status = ColumnStatus.PUBLIC;
}