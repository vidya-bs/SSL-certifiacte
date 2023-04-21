package com.itorix.apiwiz.common.model.monetization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.itorix.apiwiz.common.model.AbstractObject;
import java.util.Date;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@JsonInclude(Include.NON_NULL)
@Data
@Document("Connectors.Apigee.RatePlan")
public class RatePlan extends AbstractObject {

  public enum Type {
    FEESONLY,RATECARD,REVSHARE,REVSHARE_RATECARD,ADJUSTABLE_NOTIFICATION
  }

  public enum Status {
    DRAFT,PUBLISHED
  }

  public enum Audience {

    EVERYONE("Everyone"),
    DEVELOPERCATEGORY("Developer Category"),
    DEVELOPER("Developer");
    private String audience;

    Audience(String audience) {
      this.audience = audience;
    }

    public String getAudience() {
      return audience;
    }

  }

  private String name;
  private String productBundleId;
  private String productBundleName;
  private Type type;
  private String apigeeId;
  private String organization;
  private Audience audience;
  private String developerCategory;
  private String developerEmailId;
  private String startDate;
  private String endDate;
  private Boolean hasEndDate;
  private Boolean visibleToPortals;
  private ContractDetail contractDetail;
  private CostModel costModel;
  private Boolean isFreemium;
  private FreemiumDetail freemiumDetail;
  private RateCard rateCard;
  private RevenueShare revenueShare;
  private int adjNotificationCalcFreqInMonths;
  private Status status;
  private String configStatus;
  private Boolean activeFlag;
  private Date createdDate;
  private Date modifiedDate;

}
