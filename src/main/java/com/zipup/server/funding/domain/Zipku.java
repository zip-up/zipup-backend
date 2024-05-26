package com.zipup.server.funding.domain;

import com.zipup.server.funding.dto.ZipkuResponse;
import com.zipup.server.global.util.converter.StringToUuidConverter;
import com.zipup.server.global.util.entity.BaseTimeEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "zipku")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Zipku extends BaseTimeEntity {
  @Id
  @GeneratedValue(generator = "zipku")
  @GenericGenerator(name = "zipku", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(name = "zipku_id", columnDefinition = "BINARY(16) DEFAULT (UNHEX(REPLACE(UUID(), \"-\", \"\")))")
  @Convert(converter = StringToUuidConverter.class)
  private UUID id;

  @Column
  private String title;

  @Column
  private int goalPrice;

  @Column
  private String productUrl;

  @Column
  private String imageUrl;

  public ZipkuResponse toSummaryResponse() {
    return ZipkuResponse.builder()
            .id(id)
            .title(title)
            .imageUrl(imageUrl)
            .productUrl(productUrl)
            .goalPrice(goalPrice)
            .build();
  }
}
