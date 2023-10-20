package com.itorix.apiwiz.common.model.apigee;

public class StaticFields {
  public static final String ERR_CODE_APIGEE_1008 ="Apigee-1008";
  public static final String ERR_CODE_APIGEE_1009 ="Apigee-1009";

  public static final String ERR_MSG_APIGEE_1008 ="Service Request already Approved, Editing not possible.";
  public static final String ERR_MSG_APIGEE_1009 ="Service Request already present and Approved.";

  public static final String ORG_NAME ="org";

  public static final String TYPE_NAME ="type";

  public static final String ENV_NAME ="env";
  public static final String NAME="name";
  public static final String ACTIVE_FLAG="activeFlag";
  public static final String IS_SAAS="isSaaS";
  public static final String STATUS_REVIEW="Review";
  public static final String STATUS_APPROVED="Approved";

  public static final String APIGEE_CONFIG_COLLECTION="Connectors.Apigee.Configuration";
  public static final String APIGEEX_CONFIG_COLLECTION="Connectors.ApigeeX.Configuration";
  public static final String KONG_RUNTIME_COLLECTION="Connectors.Kong.Runtime.List";
  public static final String AZURE_CONFIG_COLLECTION="Connectors.Azure.Configuration";

  public static final String AZURE="Azure";
  public static final String KONG="Kong";
  public static final String APIGEE="Apigee";
  public static final String APIGEEX="Apigeex";

  public static final String EMAIL_SUBJECT_CONTACT_SALES="contact-sales";
  public static final String EMAIL_SUBJECT_REQUEST_A_DEMO="request-a-demo";

  public static final String REQUEST_AUDIT_REPORT_TAG="request-audit-report";
  public static final String AZURE_SUBSCRIPTIONS_URL="https://%s/subscriptions/%s/resourceGroups/%s/providers/Microsoft.ApiManagement/service/%s/subscriptions?api-version=%s";

  public static final String V1_ORGANIZATIONS_PATH="/v1/organizations/";
  public static final String PRODUCTS_PATH="/apiproducts?expand=true";

  public static final String EMAIL_SUBJECT_JOB_APPLICATION ="New Job Application | %s";

  public static final String BUILD_TEMPLATE_ADD_FIELDS="{ $addFields: { connectorObjectId: { $toObjectId: '$connectorId' } } }";
  public static final String BUILD_TEMPLATE_LOOK_UP_APIGEE="{ $lookup: { from: 'Connectors.Apigee.Configuration', localField: 'connectorObjectId', foreignField: '_id', as: 'apigeeConfig' } }";
  public static final String BUILD_TEMPLATE_LOOK_UP_APIGEEX="{ $lookup: { from: 'Connectors.ApigeeX.Configuration', localField: 'connectorObjectId', foreignField: '_id', as: 'apigeeXConfig' } }";
  public static final String BUILD_TEMPLATE_PROJECT="{ $project: { connectorId: 1, orgName: { $cond: { if: { $gt: [{ $size: '$apigeeConfig' }, 0] }, then: { $arrayElemAt: ['$apigeeConfig.orgname', 0] }, else: { $arrayElemAt: ['$apigeeXConfig.orgName', 0] } } }, type: { $cond: { if: { $gt: [{ $size: '$apigeeConfig' }, 0] }, then: 'apigee', else: 'apigeex' } } } }";
  public static final String BUILD_TEMPLATE_MATCH_CONNECTOR_ID_NON_NULL="{$match: {connectorId: {$ne: null}}}";
}