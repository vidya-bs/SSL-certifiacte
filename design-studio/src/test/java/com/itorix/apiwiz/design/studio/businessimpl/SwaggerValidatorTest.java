package com.itorix.apiwiz.design.studio.businessimpl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.generator.util.SwaggerUtil;
import io.swagger.models.Swagger;
import io.swagger.parser.OpenAPIParser;
import io.swagger.parser.SwaggerParser;
import io.swagger.parser.util.SwaggerDeserializationResult;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.servers.ServerVariables;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertTrue;
@Slf4j
public class SwaggerValidatorTest {

	@Test
	public void checkSwaggerParser() throws MalformedURLException, JsonProcessingException {
		// SwaggerParser parser = new SwaggerParser();
		String swagger3Str = "{\"openapi\":\"3.0.0\",\"info\":{\"title\":\"EDI Benefit Eligibility Inquiry\",\"description\":\"The EDI 270 Health Care Eligibility/Benefit Inquiry transaction set is used to request information from a healthcare insurance plan about a policy’s coverages, typically in relation to a particular plan subscriber. This transaction is typically sent by healthcare service providers, such as hospitals or medical facilities, and sent to insurance companies, government agencies like Medicare or Medicaid, or other organizations that would have information about a given policy. The 270 transaction is used for inquiries about what services are covered for particular patients (policy subscribers or their dependents), including required copay or coinsurance. It may be used to inquire about general information on coverage and benefits. It may also be used for questions about the coverage of specific benefits for a given plan, such as wheelchair rental, diagnostic lab services, physical therapy services, etc. The 270 document typically includes the following:\\n\\n1. Details of the sender of the inquiry (name and contact information of the information receiver)\\n2. Name of the recipient of the inquiry (the information source)\\n3. Details of the plan subscriber about to the inquiry is referring\\n4. Description of eligibility or benefit information requested\",\"version\":\"v1.0.0\",\"termsOfService\":\"https://acme.com/terms-and-conditions\",\"license\":{\"name\":\"Acme EDI License\",\"url\":\"https://acme.com/license\"},\"contact\":{\"name\":\"Thomas Hart\",\"url\":\"https://acme.com/contact-us\",\"email\":\"thomas@acme.com\"}},\"paths\":{\"/benefit-inquiry\":{\"parameters\":[{\"name\":\"Authorization\",\"in\":\"header\",\"required\":true,\"description\":\"\",\"schema\":{\"type\":\"string\"}}],\"post\":{\"summary\":\"Use the Eligibility and Benefit Inquiry (270) transaction to inquire about the health care eligibility and benefits associated with a subscriber or dependent.\",\"description\":\"Use the Eligibility and Benefit Inquiry (270) transaction to inquire about the health care eligibility and benefits associated with a subscriber or dependent.\",\"parameters\":[{\"x-key\":1,\"name\":\"x-senderId\",\"in\":\"header\",\"required\":true,\"description\":\"Sender ID of the client application making the request.\",\"schema\":{\"type\":\"string\"}},{\"x-key\":2,\"name\":\"x-receiverId\",\"in\":\"header\",\"required\":true,\"description\":\"Receiver ID of the client application making the request.\",\"schema\":{\"type\":\"string\"}},{\"x-key\":3,\"name\":\"Content-Type\",\"in\":\"header\",\"required\":true,\"description\":\"Media type of the request payload\",\"example\":\"\",\"schema\":{\"type\":\"string\",\"default\":\"application/json\"}},{\"x-key\":4,\"name\":\"Accept\",\"in\":\"header\",\"required\":false,\"description\":\"Response media type\",\"schema\":{\"type\":\"string\",\"default\":\"application/json\"}}],\"requestBody\":{\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/eligibiltyRequestModel\"}}},\"description\":\"Use the Claim Status Inquiry (276) transaction to inquire about the status of a claim after it has been sent to a payer, whether submitted on paper or electronically.\",\"required\":true,\"x-examples\":{\"application/json\":{\"payloadType\":\"X12_276_Request_005010X279A1\",\"processingMode\":\"RealTime\",\"payloadId\":\"e51d4fae-7dec-11d0-a866-00a0c91e6da2\",\"timestamp\":\"2018-06-15T15:35:45+03:00\",\"payload\":\"ISA*00*          *00*          *ZZ*AVAILITY       *ZZ*BEACON963116116*210122*2010*^*00501*000003100*0*P*:~GS*HR*AVAILITY*BEACON963116116*20210122*2010*1*X*005010X212~ST*276*0001*005010X212~BHT*0010*13*23749464748*20210122*121008~HL*1**20*1~NM1*PR*2*AVAILITY CLEARINGHOUSE*****PI*BHOVO~HL*2*1*21*1~NM1*41*2*REALMED CORPORATION*****46*S00086~HL*3*2*19*1~NM1*1P*2*MONTEFIORE NYACK HOSPITAL*****XX*1104808062~HL*4*3*22*0~DMG*D8*19690411*M~NM1*IL*1*HARTE*JAMES****MI*K9055514802~TRN*1*123456789~REF*1K*499380E0E36B45A7A230516B740BB0FE00000441~REF*D9*23749464748~AMT*T3*323~DTP*472*RD8*20210118-20210118~SE*17*0001~GE*1*1~IEA*1*000003100~\"}}},\"operationId\":\"benefit-inquiry\",\"responses\":{\"200\":{\"description\":\"The Eligibility and Benefit Response (271) transaction is used to respond to a request inquiry about the health care eligibility and benefits associated with a subscriber or dependent.\",\"headers\":{\"Content-Type\":{\"description\":\"\",\"schema\":{\"type\":\"string\",\"default\":\"application/json\"}}},\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/eligibiltyResponseModel\"},\"examples\":{\"response\":{\"value\":{\"payloadType\":\"X12_276_Request_005010X279A1\",\"processingMode\":\"RealTime\",\"payloadId\":\"e51d4fae-7dec-11d0-a866-00a0c91e6da2\",\"timestamp\":\"2018-06-15T15:35:45+03:00\",\"senderId\":\"PRACINSI\",\"receiverId\":\"BHO-541414194\",\"payload\":\"ISA*00*          *00*          *ZZ*AVAILITY       *ZZ*BEACON963116116*210122*2010*^*00501*000003100*0*P*:~GS*HR*AVAILITY*BEACON963116116*20210122*2010*1*X*005010X212~ST*276*0001*005010X212~BHT*0010*13*23749464748*20210122*121008~HL*1**20*1~NM1*PR*2*AVAILITY CLEARINGHOUSE*****PI*BHOVO~HL*2*1*21*1~NM1*41*2*REALMED CORPORATION*****46*S00086~HL*3*2*19*1~NM1*1P*2*MONTEFIORE NYACK HOSPITAL*****XX*1104808062~HL*4*3*22*0~DMG*D8*19690411*M~NM1*IL*1*HARTE*JAMES****MI*K9055514802~TRN*1*123456789~REF*1K*499380E0E36B45A7A230516B740BB0FE00000441~REF*D9*23749464748~AMT*T3*323~DTP*472*RD8*20210118-20210118~SE*17*0001~GE*1*1~IEA*1*000003100~\"}}}},\"application/xml\":{\"schema\":{\"$ref\":\"#/components/schemas/eligibiltyResponseModel\"}},\"text/xml\":{\"schema\":{\"$ref\":\"#/components/schemas/eligibiltyResponseModel\"}}}},\"400\":{\"description\":\"The request could not be understood by the server due to malformed syntax. The client SHOULD NOT repeat the request without modifications.\",\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}},\"application/xml\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}},\"text/xml\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}}}},\"401\":{\"description\":\"The request requires user authentication.\",\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}},\"application/xml\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}},\"text/xml\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}}}},\"403\":{\"description\":\"The server understood the request, but is refusing to fulfill it. Authorization will not help and the request SHOULD NOT be repeated.\",\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}},\"application/xml\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}},\"text/xml\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}}}},\"404\":{\"description\":\"The server has not found anything matching the Request-URI. No indication is given of whether the condition is temporary or permanent.\",\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}},\"application/xml\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}},\"text/xml\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}}}},\"405\":{\"description\":\"The method specified in the Request-Line is not allowed for the resource identified by the Request-URI. The response MUST include an Allow header containing a list of valid methods for the requested resource.\",\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}},\"application/xml\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}},\"text/xml\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}}}},\"406\":{\"description\":\"The resource identified by the request is only capable of generating response entities which have content characteristics not acceptable according to the accept headers sent in the request.\",\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}},\"application/xml\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}},\"text/xml\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}}}},\"429\":{\"description\":\"Request quota settings limit\",\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}},\"application/xml\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}},\"text/xml\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}}}},\"500\":{\"description\":\"The server encountered an unexpected condition which prevented it from fulfilling the request.\",\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}},\"application/xml\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}},\"text/xml\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}}}},\"501\":{\"description\":\"The server does not support the functionality required to fulfill the request. This is the appropriate response when the server does not recognize the request method and is not capable of supporting it for any resource.\",\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}},\"application/xml\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}},\"text/xml\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}}}},\"503\":{\"description\":\"The server is currently unable to handle the request due to a temporary overloading or maintenance of the server.\",\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}},\"application/xml\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}},\"text/xml\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}}}},\"504\":{\"description\":\"The server, while acting as a gateway or proxy, did not receive a timely response from the upstream server specified by the URI (e.g. HTTP, FTP, LDAP) or some other auxiliary server (e.g. DNS) it needed to access in attempting to complete the request.\",\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}},\"application/xml\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}},\"text/xml\":{\"schema\":{\"$ref\":\"#/components/schemas/errorSchema\"}}}}},\"tags\":[\"Eligibility\"],\"security\":[{\"oauth2\":[\"read\",\"write\",\"update\",\"delete\"]}]},\"description\":\"Use the Eligibility and Benefit Inquiry (270) transaction to inquire about the health care eligibility and benefits associated with a subscriber or dependent.\"}},\"tags\":[{\"name\":\"Eligibility\",\"description\":\"EDI 270/271 - Eligibility and Benefit Inquiry and Response.\"}],\"x-mock\":true,\"x-metadata\":{\"metadata\":{\"swaggerName\":\"2d5880294896484fab8f56fa26eafd87\",\"revision\":1,\"documentation\":[{\"title\":\"Oauth 2.0 Client Credentials Flow\",\"summary\":\"The Client Credentials grant is used when applications request an access token to access their own resources, not on behalf of a user.\",\"content\":\"# **Client Credentials Flow**\\n\\nWith machine-to-machine (M2M) applications, such as CLIs, daemons, or services running on your back-end, the system authenticates and authorizes the app rather than a user. For this scenario, typical authentication schemes like username + password or social logins don't make sense. Instead, M2M apps use the Client Credentials Flow (defined in OAuth 2.0 RFC 6749, section 4.4), in which they pass along their Client ID and Client Secret to authenticate themselves and get a token.\\n\\n### **How it works**\\n\\n![](https://release.apiwiz.io:443/artifactory/apiwiz-images/auth-sequence-client-credentials.png)\\n\\n\\n1. Your app authenticates with the Auth0 Authorization Server using its Client ID and Client Secret\\n2. our Auth0 Authorization Server validates the Client ID and Client Secret.\\n3. Your Auth0 Authorization Server responds with an Access Token.\\n4. Your application can use the Access Token to call an API on behalf of itself.\\n5. The API responds with requested data.\\n\\n### **Request Parameters**\\n\\n**grant_type (required)**\\n\\nThe grant_type parameter must be set to client_credentials.\\n\\n**scope (optional)**\\n\\nYour service can support different scopes for the client credentials grant. In practice, not many services actually support this.\\n\\n**Client Authentication (required)**\\n\\nThe client needs to authenticate themselves for this request. Typically the service will allow either additional request parameters client_id and client_secret, or accept the client ID and secret in the HTTP Basic auth header.\\n\\n### **Example**\\n\\nThe following is an example authorization code grant the service would receive.\\n\\n> curl --location --request POST 'https://api.acme.com/v1/auth/token' \\\\\\n> --header 'Authorization: Basic dXNlcm5hbWU6cGFzc3dvcmQ=' \\\\\\n> --header 'grant_type: client_credentials' \\\\\\n> --header 'scope: read'\\n\"},{\"title\":\"Using CORS\",\"summary\":\"APIs are the threads that let you stitch together a rich web experience. But this experience has a hard time translating to the browser, where the options for cross-domain requests are limited to techniques like JSON-P (which has limited use due to security concerns) or setting up a custom proxy (which can be a pain to set up and maintain).\",\"content\":\"# Introduction\\n\\nAPIs are the threads that let you stitch together a rich web experience. But this experience has a hard time translating to the browser, where the options for cross-domain requests are limited to techniques like JSON-P (which has limited use due to security concerns) or setting up a custom proxy (which can be a pain to set up and maintain).\\n\\nCross-Origin Resource Sharing (CORS) is a W3C spec that allows cross-domain communication from the browser. By building on top of the XMLHttpRequest object, CORS allows developers to work with the same idioms as same-domain requests.\\n\\nThe use-case for CORS is simple. Imagine the site alice.com has some data that the site bob.com wants to access. This type of request traditionally wouldn’t be allowed under the browser’s same origin policy. However, by supporting CORS requests, alice.com can add a few special response headers that allows bob.com to access the data.\\n\\nAs you can see from this example, CORS support requires coordination between both the server and client. Luckily, if you are a client-side developer you are shielded from most of these details. The rest of this article shows how clients can make cross-origin requests, and how servers can configure themselves to support CORS.\\n\\n# Adding CORS support to the server\\n\\nMost of the heavy lifting for CORS is handled between the browser and the server. The browser adds some additional headers, and sometimes makes additional requests, during a CORS request on behalf of the client. These additions are hidden from the client (but can be discovered using a packet analyzer such as Wireshark).\\n\\n# CORS Server Flowchart\\n\\n![](https://release.apiwiz.io/artifactory/apiwiz-images/cors_server_flowchart.png)\"}],\"category\":[{\"name\":\"Unsorted\",\"paths\":[\"/benefit-inquiry\"],\"definitions\":[]},{\"name\":\"Eligibilty Response Model\",\"paths\":[],\"definitions\":[\"eligibiltyResponseModel\"]},{\"name\":\"Eligibilty Request Model\",\"paths\":[],\"definitions\":[\"eligibiltyRequestModel\"]},{\"name\":\"Eligibilty Error Model\",\"paths\":[],\"definitions\":[\"errorSchema\"]},{\"name\":\"Eligibilty Resource Paths \",\"paths\":[\"/benefit-inquiry\"],\"definitions\":[]}]}},\"security\":[{\"oauth2\":[\"read\",\"write\",\"update\",\"delete\"]}],\"servers\":[{\"description\":\"Sandbox Server\",\"url\":\"https://sandbox.acme.com/\"},{\"description\":\"Development Server\",\"url\":\"https://dev.acme.com/\"},{\"description\":\"QAT Server\",\"url\":\"https://qat.acme.com/\"},{\"description\":\"Production Server\",\"url\":\"https://api.acme.com/\"}],\"components\":{\"securitySchemes\":{\"oauth2\":{\"type\":\"oauth2\",\"description\":\"The Client Credentials grant is used when applications request an access token to access their own resources, not on behalf of a user.\",\"flows\":{\"clientCredentials\":{\"tokenUrl\":\"https://api.acme.com/v1/access/token\",\"scopes\":{\"read\":\"Allow read scope\",\"write\":\"Allow write scope\",\"update\":\"Allow update scope\",\"delete\":\"Allow delete scope\"}}}}},\"schemas\":{\"errorSchema\":{\"description\":\"Container to hold error data model for X12 transactions\",\"type\":\"object\",\"required\":[\"errors\"],\"properties\":{\"errors\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"code\":{\"type\":\"string\",\"example\":\"Security-1001\"},\"userMessage\":{\"type\":\"string\",\"example\":\"Invalid Access Token\"},\"info\":{\"type\":\"string\",\"example\":\"https://developers.myapi.com/errors/#Security-1001\"}}}}}},\"eligibiltyRequestModel\":{\"description\":\"This API is designed to assist those who request reviews (specialty care, treatment, admission) and those who respond to those requests using the 278 format.\",\"properties\":{\"payloadType\":{\"type\":\"string\",\"description\":\"Paylaod type of EDI X12 request\"},\"processingMode\":{\"type\":\"string\",\"description\":\"Processing mode of the EDI 276/277 request\"},\"payloadID\":{\"type\":\"string\",\"description\":\"ID of the EDI X12 request\"},\"timestamp\":{\"type\":\"string\",\"format\":\"date-time\",\"pattern\":\"[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1]) (2[0-3]|[01][0-9]):[0-5][0-9]\",\"description\":\"Timestamp the request was sent in\"},\"payload\":{\"type\":\"string\",\"description\":\"X12 EDI content or payload used for checking claim status\"}},\"type\":\"object\"},\"eligibiltyResponseModel\":{\"description\":\"This API is designed to assist those who request reviews (specialty care, treatment, admission) and those who respond to those requests using the 278 format.\",\"properties\":{\"payloadType\":{\"type\":\"string\",\"description\":\"Paylaod type of EDI X12 request\"},\"processingMode\":{\"type\":\"string\",\"description\":\"Processing mode of the EDI 276/277 request\"},\"payloadID\":{\"type\":\"string\",\"description\":\"ID of the EDI X12 request\"},\"senderID\":{\"type\":\"string\",\"description\":\"Sender ID of the EDI X12 request\"},\"receiverID\":{\"type\":\"string\",\"description\":\"Receiver ID of the EDI X12 request\"},\"timestamp\":{\"type\":\"string\",\"format\":\"date-time\",\"pattern\":\"[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1]) (2[0-3]|[01][0-9]):[0-5][0-9]\",\"description\":\"Timestamp the request was sent in\"},\"payload\":{\"type\":\"string\",\"description\":\"X12 EDI content or payload used for checking claim status\"}},\"type\":\"object\"}}}}";
		// String swagger2Str =
		// "{\"swagger\":\"2.0\",\"info\":{\"description\":\"This is a sample
		// server product catalog server. For this sample, you can use the api
		// key `special-key` to test the authorization
		// filters.\",\"version\":\"v1\",\"title\":\"ProductCatalog\",\"contact\":{\"name\":\"Support\",\"url\":\"https://itorix.com/contact\",\"email\":\"support@itorix.com\"},\"license\":{\"name\":\"Itoric.Inc\",\"url\":\"https://itorix.com\"}},\"host\":\"coreapiteam-test.apigee.net\",\"basePath\":\"/v1/products\",\"schemes\":[\"https\"],\"consumes\":[\"application/json\"],\"produces\":[\"application/json\"],\"paths\":{\"/\":{\"get\":{\"tags\":[\"Get
		// List of Products\"],\"summary\":\"Get List of
		// products\",\"description\":\"Get list of products available in the
		// catalog\",\"produces\":[\"application/json\"],\"parameters\":[{\"name\":\"Authorization\",\"in\":\"header\",\"description\":\"OAuth
		// 2.0 Bearer
		// Token\",\"required\":true,\"type\":\"string\",\"x-example\":\"Bearer
		// Uuafp57IjZ1RmQKruKVw7MXWEQ8h\"},{\"name\":\"Accept\",\"in\":\"header\",\"description\":\"The
		// Accept request-header field can be used to specify certain media
		// types which are acceptable for the
		// response.\",\"required\":true,\"type\":\"string\",\"x-example\":\"application/json\"}],\"responses\":{\"200\":{\"description\":\"Status
		// 200\",\"schema\":{\"$ref\":\"#/definitions/ProductResponse\"},\"examples\":{\"application/json\":\"{\\n
		// \\\"products\\\": [\\n {\\n \\\"productId\\\": \\\"PMS-425-02\\\",\\n
		// \\\"name\\\": \\\"I Love APIs sticker\\\",\\n \\\"description\\\":
		// \\\"I Love APIs sticker - White\\\",\\n \\\"uuid\\\":
		// \\\"12541a04-6f17-11e4-8810-67f41b6ea52d\\\"\\n },\\n {\\n
		// \\\"productId\\\": \\\"NL-3600-02\\\",\\n \\\"name\\\": \\\"Dev
		// Apigee T-Shirt\\\",\\n \\\"description\\\": \\\"Dev Apigee T-Shirt -
		// Grey\\\",\\n \\\"uuid\\\":
		// \\\"cf1fdbaa-6f17-11e4-9240-b1091e5819c8\\\"\\n },\\n {\\n
		// \\\"productId\\\": \\\"PMS-1655-04\\\",\\n \\\"name\\\": \\\"API
		// Sticker\\\",\\n \\\"description\\\": \\\"API round sticker -
		// White\\\",\\n \\\"uuid\\\":
		// \\\"124ee9da-6f17-11e4-964a-9be08c22a96d\\\"\\n },\\n {\\n
		// \\\"productId\\\": \\\"NL-6010-01\\\",\\n \\\"name\\\": \\\"Apigeek
		// T-Shirt\\\",\\n \\\"description\\\": \\\"Apigeek T-Shirt - Black
		// Vintage\\\",\\n \\\"uuid\\\":
		// \\\"cf17291a-6f17-11e4-b838-43d0822f332b\\\"\\n },\\n {\\n
		// \\\"productId\\\": \\\"PMS-1655-02\\\",\\n \\\"name\\\": \\\"Apigeek
		// Sticker\\\",\\n \\\"description\\\": \\\"Apigee round sticker -
		// White\\\",\\n \\\"uuid\\\":
		// \\\"1249b9ba-6f17-11e4-b1dd-157c1483794d\\\"\\n },\\n {\\n
		// \\\"productId\\\": \\\"PMS-1655-01\\\",\\n \\\"name\\\": \\\"Apigeek
		// Sticker\\\",\\n \\\"description\\\": \\\"Apigee round sticker -
		// Orange\\\",\\n \\\"uuid\\\":
		// \\\"124748ba-6f17-11e4-b1c3-a1b9eae15c4f\\\"\\n },\\n {\\n
		// \\\"productId\\\": \\\"NL-6010-02\\\",\\n \\\"name\\\": \\\"API Ninja
		// T-Shirt\\\",\\n \\\"description\\\": \\\"API Ninja T-Shirt -
		// White\\\",\\n \\\"uuid\\\":
		// \\\"cf1a0f4a-6f17-11e4-9749-ad4a072c08e3\\\"\\n },\\n {\\n
		// \\\"productId\\\": \\\"NL-3600-03\\\",\\n \\\"name\\\": \\\"Shaping
		// Digital T-Shirt\\\",\\n \\\"description\\\": \\\"Shaping Digital
		// T-Shirt - Black\\\",\\n \\\"uuid\\\":
		// \\\"cf22c1da-6f17-11e4-971c-1395e2dc2bfe\\\"\\n },\\n {\\n
		// \\\"productId\\\": \\\"NL-3600-01\\\",\\n \\\"name\\\": \\\"API
		// Slayer T-Shirt\\\",\\n \\\"description\\\": \\\"API Slayer T-Shirt -
		// White\\\",\\n \\\"uuid\\\":
		// \\\"cf1d1c8a-6f17-11e4-a24d-fb2ae314b099\\\"\\n },\\n {\\n
		// \\\"productId\\\": \\\"NL-3600-04\\\",\\n \\\"name\\\": \\\"Shaping
		// Digital T-Shirt\\\",\\n \\\"description\\\": \\\"Shaping Digital
		// T-Shirt - White\\\",\\n \\\"uuid\\\":
		// \\\"cf2580fa-6f17-11e4-80dd-11ffc22bd4e2\\\"\\n }\\n
		// ]\\n}\"}},\"401\":{\"description\":\"Status
		// 401\",\"schema\":{\"$ref\":\"#/definitions/errors\"},\"examples\":{\"application/json\":\"{\\n
		// \\\"errors\\\": [\\n {\\n \\\"code\\\": \\\"Security-1001\\\",\\n
		// \\\"userMessage\\\": \\\"Invalid Access Token\\\",\\n \\\"info\\\":
		// \\\"https://developers.myapi.com/errors/#Security-1001\\\"\\n }\\n
		// ]\\n}\"}}},\"security\":[{\"OAuth2\":[\"catalog:read\",\"catalog:write\"]}]}},\"/productAvailability\":{\"get\":{\"tags\":[\"ProductAvailability\"],\"summary\":\"Product
		// Availability\",\"description\":\"Check for product
		// availability\",\"produces\":[\"application/json\"],\"parameters\":[{\"name\":\"Authorization\",\"in\":\"header\",\"description\":\"OAuth
		// 2.0 Bearer
		// Token\",\"required\":true,\"type\":\"string\",\"x-example\":\"Bearer
		// Uuafp57IjZ1RmQKruKVw7MXWEQ8h\"},{\"name\":\"Accept\",\"in\":\"header\",\"description\":\"The
		// Accept request-header field can be used to specify certain media
		// types which are acceptable for the
		// response.\",\"required\":true,\"type\":\"string\",\"x-example\":\"application/json\"}],\"responses\":{\"200\":{\"description\":\"Status
		// 200\",\"schema\":{\"$ref\":\"#/definitions/GetAvailabilityResponse\"},\"examples\":{\"application/json\":\"{\\n
		// \\\"products\\\": [\\n {\\n \\\"productId\\\": \\\"PMS-425-02\\\",\\n
		// \\\"name\\\": \\\"I Love APIs sticker\\\",\\n \\\"description\\\":
		// \\\"I Love APIs sticker - White\\\",\\n \\\"uuid\\\":
		// \\\"12541a04-6f17-11e4-8810-67f41b6ea52d\\\"\\n }\\n
		// ]\\n}\"}},\"401\":{\"description\":\"Status
		// 401\",\"schema\":{\"$ref\":\"#/definitions/errors\"},\"examples\":{\"application/json\":\"{\\n
		// \\\"errors\\\": [\\n {\\n \\\"code\\\": \\\"Security-1001\\\",\\n
		// \\\"userMessage\\\": \\\"Invalid Access Token\\\",\\n \\\"info\\\":
		// \\\"https://developers.myapi.com/errors/#Security-1001\\\"\\n }\\n
		// ]\\n}\"}}},\"security\":[{\"OAuth2\":[\"catalog:read\",\"catalog:write\"]}]}},\"/{productid}\":{\"get\":{\"tags\":[\"Get
		// Product Details\"],\"summary\":\"Get Product
		// Details\",\"description\":\"Get detailed product
		// details\",\"produces\":[\"application/json\"],\"parameters\":[{\"name\":\"Authorization\",\"in\":\"header\",\"description\":\"OAuth
		// 2.0 Bearer
		// Token\",\"required\":true,\"type\":\"string\",\"x-example\":\"Bearer
		// Uuafp57IjZ1RmQKruKVw7MXWEQ8h\"},{\"name\":\"productid\",\"in\":\"path\",\"required\":true,\"type\":\"string\"},{\"name\":\"Accept\",\"in\":\"header\",\"description\":\"The
		// Accept request-header field can be used to specify certain media
		// types which are acceptable for the
		// response.\",\"required\":true,\"type\":\"string\",\"x-example\":\"application/json\"}],\"responses\":{\"200\":{\"description\":\"Status
		// 200\",\"schema\":{\"$ref\":\"#/definitions/ProductResponse\"},\"examples\":{\"application/json\":\"{\\n
		// \\\"products\\\": [\\n {\\n \\\"productId\\\": \\\"PMS-425-02\\\",\\n
		// \\\"name\\\": \\\"I Love APIs sticker\\\",\\n \\\"description\\\":
		// \\\"I Love APIs sticker - White\\\",\\n \\\"uuid\\\":
		// \\\"12541a04-6f17-11e4-8810-67f41b6ea52d\\\"\\n }\\n
		// ]\\n}\"}},\"401\":{\"description\":\"Status
		// 401\",\"schema\":{\"$ref\":\"#/definitions/errors\"},\"examples\":{\"application/json\":\"{\\n
		// \\\"errors\\\": [\\n {\\n \\\"code\\\": \\\"Security-1001\\\",\\n
		// \\\"userMessage\\\": \\\"Invalid Access Token\\\",\\n \\\"info\\\":
		// \\\"https://developers.myapi.com/errors/#Security-1001\\\"\\n }\\n
		// ]\\n}\"}}},\"security\":[{\"OAuth2\":[\"catalog:read\",\"catalog:write\"]}]}}},\"securityDefinitions\":{\"OAuth2\":{\"type\":\"oauth2\",\"tokenUrl\":\"https://darshan-eval-prod.apigee.net/auth/v1/token\",\"flow\":\"password\",\"scopes\":{\"catalog:read\":\"Read
		// Permission\",\"catalog:write\":\"Write
		// Permission\"}}},\"definitions\":{\"products\":{\"type\":\"object\",\"required\":[\"name\",\"productId\",\"uuid\"],\"properties\":{\"productId\":{\"type\":\"string\",\"description\":\"The
		// unique ID for the product
		// info\"},\"name\":{\"type\":\"string\",\"description\":\"The unique
		// name of the
		// product\"},\"description\":{\"type\":\"string\",\"description\":\"Product
		// description\"},\"uuid\":{\"type\":\"string\",\"description\":\"UUID
		// of product returned\"}},\"description\":\"List if product catalog
		// information\",\"example\":\"{\\n \\\"productId\\\":
		// \\\"PMS-425-02\\\",\\n \\\"name\\\": \\\"I Love APIs sticker\\\",\\n
		// \\\"description\\\": \\\"I Love APIs sticker - White\\\",\\n
		// \\\"uuid\\\": \\\"12541a04-6f17-11e4-8810-67f41b6ea52d\\\"\\n
		// }\"},\"ProductResponse\":{\"type\":\"array\",\"items\":{\"$ref\":\"#/definitions/products\"}},\"errors\":{\"type\":\"object\",\"properties\":{\"errors\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"userMessage\":{\"type\":\"string\"},\"code\":{\"type\":\"string\"},\"info\":{\"type\":\"string\"}}}}},\"example\":\"{\\\"errors\\\":
		// [\\n {\\n \\\"code\\\": \\\"Security-1001\\\",\\n
		// \\\"userMessage\\\": \\\"Invalid Access Token\\\",\\n \\\"info\\\":
		// \\\"https://developers.myapi.com/errors/#Security-1001\\\"\\n }\\n
		// ]}\"},\"GetAvailabilityResponse\":{\"type\":\"object\",\"properties\":{\"Products\":{\"type\":\"object\",\"properties\":{\"product\":{\"type\":\"object\",\"properties\":{\"productid\":{\"type\":\"string\"},\"qty\":{\"type\":\"number\"},\"storeid\":{\"type\":\"number\"}}}}}},\"example\":\"{\\n
		// \\\"Products\\\": {\\n \\\"product\\\": {\\n \\\"productid\\\":
		// \\\"PMS-425-02\\\",\\n \\\"qty\\\": 234,\\n \\\"storeid\\\": 201\\n
		// }\\n }\\n}\"}}}";
		// Swagger swagger = parser.parse(swagger2Str);
		//
		// log.info(swagger.getBasePath());

		String petStore = "{\"openapi\":\"3.0.0\",\"info\":{\"version\":\"1.0.0\",\"title\":\"Petstore Version 2\",\"description\":\"Swagger Petstore\",\"termsOfService\":\"http://www.apache.org/licenses/LICENSE-2.0.html\",\"license\":{\"name\":\"Apache 2.0\",\"url\":\"http://www.apache.org/licenses/LICENSE-2.0.html\"},\"contact\":{\"name\":\"API Support\",\"url\":\"http://www.example.com/support\",\"email\":\"support@example.com\"}},\"servers\":[{\"url\":\"http://petstore.swagger.io/v1\",\"description\":\"Development server\"},{\"url\":\"https://{username}.gigantic-server.com:{port}/{basePath}\",\"description\":\"The production API server\",\"variables\":{\"username\":{\"default\":\"demo\",\"description\":\"this value is assigned by the service provider, in this example `gigantic-server.com`\"},\"port\":{\"enum\":[\"8443\",\"443\"],\"default\":\"8443\"},\"basePath\":{\"default\":\"v2\"}}}],\"paths\":{\"/pets\":{\"summary\":\"An optional, string summary, intended to apply to all operations in this path.\",\"description\":\"An optional, string description, intended to apply to all operations in this path.\",\"servers\":[{\"url\":\"https://development.gigantic-server.com/v1\",\"description\":\"Development server\"},{\"url\":\"https://staging.gigantic-server.com/v1\",\"description\":\"Staging server\"},{\"url\":\"https://api.gigantic-server.com/v1\",\"description\":\"Production server\"},{\"url\":\"https://{username}.gigantic-server.com:{port}/{basePath}\",\"description\":\"The QA API server\",\"variables\":{\"username\":{\"default\":\"demo\",\"description\":\"this value is assigned by the service provider, in this example `gigantic-server.com`\"},\"port\":{\"enum\":[\"8443\",\"443\"],\"default\":\"8443\"},\"basePath\":{\"default\":\"v2\"}}}],\"parameters\":[{\"name\":\"authToken\",\"in\":\"header\",\"description\":\"token to be passed as a header\",\"required\":true,\"schema\":{\"type\":\"array\",\"items\":{\"type\":\"integer\",\"format\":\"int64\"}}},{\"name\":\"OriginHeader\",\"in\":\"header\",\"description\":\"OriginHeader to fetch\",\"required\":true,\"deprecated\":false,\"example\":\"api.itorix.com\",\"schema\":{\"type\":\"string\"}},{\"name\":\"id\",\"in\":\"query\",\"description\":\"ID of the object to fetch\",\"required\":false,\"allowEmptyValue\":false,\"examples\":{\"id-example\":{\"$ref\":\"#/components/examples/zip-example\"}},\"schema\":{\"type\":\"array\",\"items\":{\"type\":\"string\"}},\"style\":\"form\",\"explode\":true},{\"$ref\":\"#/components/parameters/interactionID\"}],\"get\":{\"summary\":\"List all pets\",\"description\":\"List all pets\",\"externalDocs\":{\"url\":\"https://www.google.com\",\"description\":\"External Documentation\"},\"operationId\":\"listPets\",\"security\":[{\"oauth2\":[\"write:pets\",\"read:pets\"]},{\"apiKey\":[]}],\"servers\":[{\"url\":\"https://development.gigantic-server.com/v1\",\"description\":\"Development server\"},{\"url\":\"https://{username}.gigantic-server.com:{port}/{basePath}\",\"description\":\"The QA API server\",\"variables\":{\"username\":{\"default\":\"demo\",\"description\":\"this value is assigned by the service provider, in this example `gigantic-server.com`\"},\"port\":{\"enum\":[\"8443\",\"443\"],\"default\":\"8443\"},\"basePath\":{\"default\":\"v2\"}}}],\"deprecated\":false,\"tags\":[\"pets\"],\"parameters\":[{\"name\":\"limit\",\"in\":\"query\",\"description\":\"How many items to return at one time (max 100)\",\"required\":false,\"schema\":{\"type\":\"integer\",\"format\":\"int32\"}}],\"responses\":{\"200\":{\"description\":\"A paged array of pets\",\"headers\":{\"x-next\":{\"description\":\"A link to the next page of responses\",\"schema\":{\"type\":\"string\"}},\"X-Rate-Limit-Limit\":{\"description\":\"The number of allowed requests in the current period\",\"schema\":{\"type\":\"integer\"}},\"X-Rate-Limit-Remaining\":{\"description\":\"The number of remaining requests in the current period\",\"schema\":{\"type\":\"integer\"}},\"X-Rate-Limit-Reset\":{\"description\":\"The number of seconds left in the current period\",\"schema\":{\"type\":\"integer\"}},\"Authorization\":{\"$ref\":\"#/components/headers/Authorization\"}},\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/Pets\"}}},\"links\":{\"address\":{\"$ref\":\"#/components/links/address\"},\"pinCode\":{\"operationId\":\"pinCode\",\"description\":\"GET User pinCode\",\"parameters\":{\"userId\":\"$request.path.id\"}}}},\"400\":{\"description\":\"A simple string response\",\"content\":{\"text/plain\":{\"schema\":{\"type\":\"string\"}}},\"headers\":{\"X-Rate-Limit-Limit\":{\"description\":\"The number of allowed requests in the current period\",\"schema\":{\"type\":\"integer\"}},\"X-Rate-Limit-Remaining\":{\"description\":\"The number of remaining requests in the current period\",\"schema\":{\"type\":\"integer\"}},\"X-Rate-Limit-Reset\":{\"description\":\"The number of seconds left in the current period\",\"schema\":{\"type\":\"integer\"}}}},\"500\":{\"$ref\":\"#/components/responses/response1\"},\"default\":{\"description\":\"unexpected error\",\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/Error\"}}}}}},\"post\":{\"summary\":\"Create a pet\",\"operationId\":\"createPets\",\"tags\":[\"pets\"],\"requestBody\":{\"description\":\"user to add to the system\",\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/Pet\"},\"examples\":{\"user\":{\"summary\":\"User Example\",\"externalValue\":\"http://foo.bar/examples/user-example.json\"}}},\"application/xml\":{\"schema\":{\"$ref\":\"#/components/schemas/Pet\"},\"examples\":{\"user\":{\"summary\":\"User example in XML\",\"externalValue\":\"http://foo.bar/examples/user-example.xml\"}}},\"*/*\":{\"examples\":{\"user\":{\"summary\":\"User example in other format\",\"externalValue\":\"http://foo.bar/examples/user-example.whatever\"}}}}},\"responses\":{\"201\":{\"description\":\"Null response\"},\"default\":{\"description\":\"unexpected error\",\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/Error\"}}}}}}},\"/pets/{petId}\":{\"parameters\":[{\"name\":\"petId\",\"in\":\"path\",\"required\":true,\"description\":\"The id of the pet to retrieve\",\"schema\":{\"type\":\"string\"}}],\"get\":{\"summary\":\"Info for a specific pet\",\"operationId\":\"showPetById\",\"tags\":[\"pets\"],\"responses\":{\"200\":{\"description\":\"Expected response to a valid request\",\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/Pet\"}}}},\"default\":{\"description\":\"unexpected error\",\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/Error\"}}}}}},\"put\":{\"summary\":\"update a pet\",\"operationId\":\"updatePet\",\"tags\":[\"pets\"],\"requestBody\":{\"$ref\":\"#/components/requestBodies/body1\"},\"responses\":{\"201\":{\"description\":\"Null response\"},\"default\":{\"description\":\"unexpected error\",\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/Error\"}}}}}}}},\"components\":{\"schemas\":{\"Pet\":{\"type\":\"object\",\"nullable\":false,\"deprecated\":false,\"externalDocs\":{\"description\":\"Find more info here\",\"url\":\"https://example.com\"},\"required\":[\"id\",\"name\"],\"properties\":{\"id\":{\"type\":\"integer\",\"readOnly\":false,\"writeOnly\":false,\"format\":\"int64\"},\"name\":{\"type\":\"string\"},\"tag\":{\"type\":\"string\"}}},\"Pets\":{\"type\":\"array\",\"items\":{\"$ref\":\"#/components/schemas/Pet\"}},\"Error\":{\"type\":\"object\",\"required\":[\"code\",\"message\"],\"properties\":{\"code\":{\"type\":\"integer\",\"format\":\"int32\"},\"message\":{\"type\":\"string\"}}},\"Person\":{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"integer\",\"format\":\"int32\",\"xml\":{\"attribute\":true}},\"name\":{\"type\":\"string\",\"xml\":{\"namespace\":\"http://example.com/schema/sample\",\"prefix\":\"sample\"}}}},\"animals\":{\"type\":\"array\",\"items\":{\"type\":\"string\",\"xml\":{\"name\":\"animal\"}}},\"cars\":{\"type\":\"array\",\"items\":{\"type\":\"string\",\"xml\":{\"name\":\"car\"}},\"xml\":{\"wrapped\":true}},\"planes\":{\"type\":\"array\",\"items\":{\"type\":\"string\",\"xml\":{\"name\":\"plane\"}},\"xml\":{\"name\":\"aliens\",\"wrapped\":true}},\"shoes\":{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"integer\",\"format\":\"int64\"},\"name\":{\"type\":\"string\"}},\"required\":[\"name\"],\"example\":\"{\\\"name\\\":\\\"test\\\",\\\"id\\\":123}\"},\"ErrorModel\":{\"type\":\"object\",\"required\":[\"message\",\"code\"],\"properties\":{\"message\":{\"type\":\"string\",\"example\":\"This is a error message example\"},\"code\":{\"type\":\"integer\",\"minimum\":100,\"maximum\":600}}},\"ExtendedErrorModel\":{\"allOf\":[{\"$ref\":\"#/components/schemas/ErrorModel\"},{\"$ref\":\"#/components/schemas/shoes\"},{\"type\":\"object\",\"required\":[\"rootCause\"],\"properties\":{\"rootCause\":{\"type\":\"string\"}}}]},\"simpleModel\":{\"type\":\"object\",\"additionalProperties\":{\"$ref\":\"#/components/schemas/ErrorModel\"},\"properties\":{\"message\":{\"type\":\"string\",\"example\":\"This is a error message example\"},\"code\":{\"type\":\"integer\",\"minimum\":100,\"maximum\":600}}},\"billingAddress\":{\"type\":\"object\",\"required\":[\"name\"],\"properties\":{\"name\":{\"type\":\"string\"},\"address\":{\"allOf\":[{\"$ref\":\"#/components/schemas/ErrorModel\"},{\"type\":\"object\",\"properties\":{\"addressType\":{\"type\":\"string\"}}}]},\"age\":{\"type\":\"integer\",\"format\":\"int32\",\"minimum\":0}}},\"PetOne\":{\"type\":\"object\",\"discriminator\":{\"propertyName\":\"petType\"},\"properties\":{\"name\":{\"type\":\"string\"},\"petType\":{\"type\":\"string\"}},\"required\":[\"name\",\"petType\"]},\"Cat\":{\"description\":\"A representation of a cat. Note that `Cat` will be used as the discriminator value.\",\"allOf\":[{\"$ref\":\"#/components/schemas/PetOne\"},{\"type\":\"object\",\"properties\":{\"huntingSkill\":{\"type\":\"string\",\"description\":\"The measured skill for hunting\",\"default\":\"lazy\",\"enum\":[\"clueless\",\"lazy\",\"adventurous\",\"aggressive\"]}},\"required\":[\"huntingSkill\"]}]},\"Dog\":{\"description\":\"A representation of a dog. Note that `Dog` will be used as the discriminator value.\",\"allOf\":[{\"$ref\":\"#/components/schemas/PetOne\"},{\"type\":\"object\",\"properties\":{\"packSize\":{\"type\":\"integer\",\"format\":\"int32\",\"description\":\"the size of the pack the dog is from\",\"default\":0,\"minimum\":0}},\"required\":[\"packSize\"]}]},\"PetTwo\":{\"type\":\"object\",\"discriminator\":{\"propertyName\":\"petType\",\"mapping\":{\"dog\":\"#/components/schemas/Dog\",\"monster\":\"https://gigantic-server.com/schemas/Monster/schema.json\"}},\"properties\":{\"name\":{\"type\":\"string\"},\"petType\":{\"type\":\"string\"}},\"required\":[\"name\",\"petType\"]}},\"links\":{\"address\":{\"operationId\":\"getUserAddress\",\"description\":\"GET User Address\",\"parameters\":{\"userId\":\"$response.body#/addressID\"}},\"employeeID\":{\"operationId\":\"employeeID\",\"description\":\"EmployeeID\",\"parameters\":{\"userId\":\"$request.path.employeeID\"}},\"departmentID\":{\"operationId\":\"departmentID\",\"description\":\"departmentID\",\"requestBody\":\"$request.body#/user/uuid\",\"parameters\":{\"userId\":\"$request.path.departmentID\"}},\"employeeName\":{\"operationId\":\"employeeName\",\"description\":\"employeeName \",\"parameters\":{\"userId\":\"$request.header.employeeName\"}},\"paymentID\":{\"operationId\":\"paymentID\",\"description\":\"payment ID \",\"parameters\":{\"userId\":\"$request.query.paymentID\"},\"server\":{\"url\":\"http://petstore.swagger.io/v1\",\"description\":\"Development server\"}},\"paymentMethod\":{\"operationRef\":\"#/paths/~12.0~1repositories~1{username}/get\",\"description\":\"payment Method\",\"parameters\":{\"userId\":\"$response.body#/username\"}},\"userUuid\":{\"operationId\":\"getUserAddressByUUID\",\"description\":\"payment ID \",\"parameters\":{\"userUuid\":\"$response.body#/uuid\"}},\"userName\":{\"operationId\":\"userName\",\"description\":\"user Name\",\"parameters\":{\"userUuid\":\"$response.body#/uuid\"},\"server\":{\"url\":\"https://{username}.gigantic-server.com:{port}/{basePath}\",\"description\":\"The production API server\",\"variables\":{\"username\":{\"default\":\"demo\",\"description\":\"this value is assigned by the service provider, in this example `gigantic-server.com`\"},\"port\":{\"enum\":[\"8443\",\"443\"],\"default\":\"8443\"},\"basePath\":{\"default\":\"v2\"}}}}},\"securitySchemes\":{\"basicAuth\":{\"type\":\"http\",\"scheme\":\"basic\"},\"apiKey\":{\"type\":\"apiKey\",\"description\":\"apiKey\",\"name\":\"api_key\",\"in\":\"header\"},\"jwt\":{\"type\":\"http\",\"description\":\"http\",\"scheme\":\"bearer\",\"bearerFormat\":\"JWT\"},\"oauth2\":{\"type\":\"oauth2\",\"description\":\"oauth2\",\"flows\":{\"implicit\":{\"authorizationUrl\":\"https://example.com/api/oauth/dialog\",\"scopes\":{\"write:pets\":\"modify pets in your account\",\"read:pets\":\"read your pets\"}},\"authorizationCode\":{\"authorizationUrl\":\"https://example.com/api/oauth/dialog\",\"tokenUrl\":\"https://example.com/api/oauth/token\",\"scopes\":{\"write:pets\":\"modify pets in your account\",\"read:pets\":\"read your pets\"}},\"clientCredentials\":{\"tokenUrl\":\"https://example.com/api/oauth/token\",\"scopes\":{\"write:pets\":\"modify pets in your account\",\"read:pets\":\"read your pets\"}},\"password\":{\"tokenUrl\":\"https://example.com/api/oauth/token\",\"refreshUrl\":\"https://example.com/api/oauth/refresh\",\"scopes\":{\"write:pets\":\"modify pets in your account\",\"read:pets\":\"read your pets\"}}}}},\"headers\":{\"Authorization\":{\"description\":\"The number of allowed requests in the current period\",\"schema\":{\"type\":\"integer\"}}},\"examples\":{\"examples1\":{\"description\":\"The number of allowed requests in the current period\",\"summary\":\"Example summary\",\"value\":\"{\\\"name\\\":\\\"test\\\",\\\"id\\\":123}\"},\"zip-example\":{\"description\":\"The number of allowed requests in the current period\",\"summary\":\"Example summary\",\"value\":\"[\\\"test\\\",\\\"test1\\\"]\"},\"examples2\":{\"description\":\"The number of allowed requests in the current period\",\"summary\":\"Example summary\",\"externalValue\":\"https://api.itorix.com/examples#example2.json\"},\"frog\":{\"summary\":\"An example of a dog with a cat's name\",\"value\":{\"name\":\"Puma\",\"petType\":\"Dog\",\"color\":\"Black\",\"gender\":\"Female\",\"breed\":\"Mixed\"}}},\"parameters\":{\"parameter1\":{\"name\":\"token\",\"in\":\"header\",\"description\":\"token to be passed as a header\",\"required\":true,\"schema\":{\"type\":\"array\",\"items\":{\"type\":\"integer\",\"format\":\"int64\"}},\"style\":\"simple\"},\"interactionID\":{\"name\":\"interactionID\",\"in\":\"header\",\"description\":\"token to be passed as a header\",\"required\":true,\"schema\":{\"type\":\"array\",\"items\":{\"type\":\"integer\",\"format\":\"int64\"}},\"style\":\"simple\"},\"parameter2\":{\"name\":\"username\",\"in\":\"path\",\"description\":\"username to fetch\",\"required\":true,\"schema\":{\"type\":\"string\"}},\"parameter3\":{\"name\":\"id\",\"in\":\"query\",\"description\":\"ID of the object to fetch\",\"required\":false,\"schema\":{\"type\":\"array\",\"items\":{\"type\":\"string\"}},\"style\":\"form\",\"explode\":true},\"parameter4\":{\"in\":\"query\",\"name\":\"coordinates\",\"content\":{\"application/json\":{\"schema\":{\"type\":\"object\",\"required\":[\"lat\",\"long\"],\"properties\":{\"lat\":{\"type\":\"number\"},\"long\":{\"type\":\"number\"}}}}}},\"parameter5\":{\"in\":\"query\",\"name\":\"freeForm\",\"schema\":{\"type\":\"object\",\"additionalProperties\":{\"type\":\"integer\"}},\"style\":\"form\"}},\"requestBodies\":{\"body1\":{\"description\":\"user to add to the system\",\"required\":true,\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/Pet\"},\"examples\":{\"user\":{\"summary\":\"User Example\",\"externalValue\":\"http://foo.bar/examples/user-example.json\"}}},\"application/xml\":{\"schema\":{\"$ref\":\"#/components/schemas/Pet\"},\"examples\":{\"user\":{\"summary\":\"User example in XML\",\"externalValue\":\"http://foo.bar/examples/user-example.xml\"}}},\"text/plain\":{\"examples\":{\"user\":{\"summary\":\"User example in Plain text\",\"externalValue\":\"http://foo.bar/examples/user-example.txt\"}}},\"*/*\":{\"examples\":{\"user\":{\"summary\":\"User example in other format\",\"externalValue\":\"http://foo.bar/examples/user-example.whatever\"}}}}},\"body2\":{\"description\":\"user to add to the system\",\"required\":false,\"content\":{\"text/xml\":{\"schema\":{\"type\":\"array\",\"items\":{\"type\":\"string\"}}}}},\"body3\":{\"description\":\"user to add to the system\",\"required\":false,\"content\":{\"application/json\":{\"schema\":{\"$ref\":\"#/components/schemas/Pet\"},\"examples\":{\"cat\":{\"summary\":\"An example of a cat\",\"value\":{\"name\":\"Fluffy\",\"petType\":\"Cat\",\"color\":\"White\",\"gender\":\"male\",\"breed\":\"Persian\"}},\"dog\":{\"summary\":\"An example of a dog with a cat's name\",\"value\":{\"name\":\"Puma\",\"petType\":\"Dog\",\"color\":\"Black\",\"gender\":\"Female\",\"breed\":\"Mixed\"}}}}}},\"body4\":{\"description\":\"user to add to the system\",\"required\":false,\"content\":{\"application/octet-stream\":{\"schema\":{\"type\":\"string\",\"format\":\"binary\"}}}},\"body5\":{\"description\":\"user to add to the system\",\"required\":false,\"content\":{\"image/jpeg\":{\"schema\":{\"type\":\"string\",\"format\":\"binary\"}},\"image/png\":{\"schema\":{\"type\":\"string\",\"format\":\"binary\"}}}},\"body6\":{\"description\":\"user to add to the system\",\"required\":false,\"content\":{\"multipart/form-data\":{\"schema\":{\"properties\":{\"file\":{\"type\":\"array\",\"items\":{\"type\":\"string\",\"format\":\"binary\"}}}}}}},\"body7\":{\"description\":\"user to add to the system\",\"required\":false,\"content\":{\"application/x-www-form-urlencoded\":{\"schema\":{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"string\",\"format\":\"uuid\"},\"address\":{\"type\":\"object\",\"properties\":{}}}}}}},\"body8\":{\"description\":\"user to add to the system\",\"required\":false,\"content\":{\"multipart/form-data\":{\"schema\":{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"string\",\"format\":\"uuid\"},\"address\":{\"type\":\"object\",\"properties\":{}},\"profileImage\":{\"type\":\"string\",\"format\":\"binary\"},\"children\":{\"type\":\"array\",\"items\":{\"type\":\"string\"}},\"addresses\":{\"type\":\"array\",\"items\":{\"$ref\":\"#/components/schemas/billingAddress\"}}}}}}},\"requestBody\":{\"description\":\"user to add to the system\",\"required\":false,\"content\":{\"multipart/mixed\":{\"schema\":{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"string\",\"format\":\"uuid\"},\"address\":{\"type\":\"object\",\"properties\":{}},\"historyMetadata\":{\"description\":\"metadata in XML format\",\"type\":\"object\",\"properties\":{}},\"profileImage\":{\"type\":\"string\",\"format\":\"binary\"}}},\"encoding\":{\"historyMetadata\":{\"contentType\":\"application/xml; charset=utf-8\"},\"profileImage\":{\"contentType\":\"image/png, image/jpeg\",\"headers\":{\"X-Rate-Limit-Limit\":{\"description\":\"The number of allowed requests in the current period\",\"schema\":{\"type\":\"integer\"}}}}}}}}},\"responses\":{\"response1\":{\"description\":\"A complex object array response\",\"content\":{\"application/json\":{\"schema\":{\"type\":\"array\",\"items\":{\"$ref\":\"#/components/schemas/Pet\"}}}},\"links\":{\"address\":{\"operationId\":\"getUserAddress\",\"description\":\"GET User Address\",\"parameters\":{\"userId\":\"$request.path.id\"}},\"employeeID\":{\"$ref\":\"#/components/links/employeeID\"}}},\"response2\":{\"description\":\"A simple string response\",\"content\":{\"text/plain\":{\"schema\":{\"type\":\"string\"}}}},\"response3\":{\"description\":\"A simple string response\",\"content\":{\"text/plain\":{\"schema\":{\"type\":\"string\"}}},\"headers\":{\"X-Rate-Limit-Limit\":{\"description\":\"The number of allowed requests in the current period\",\"schema\":{\"type\":\"integer\"}},\"X-Rate-Limit-Remaining\":{\"description\":\"The number of remaining requests in the current period\",\"schema\":{\"type\":\"integer\"}},\"X-Rate-Limit-Reset\":{\"description\":\"The number of seconds left in the current period\",\"schema\":{\"type\":\"integer\"}}}},\"response4\":{\"description\":\"object created\"}}},\"tags\":[{\"name\":\"pet\",\"description\":\"Pets operations\",\"externalDocs\":{\"description\":\"Find more info here\",\"url\":\"https://example.com\"}},{\"name\":\"store\",\"description\":\"store operations\"}],\"security\":[{\"apiKey\":[]},{\"oauth2\":[\"write:pets\",\"read:pets\"]}],\"externalDocs\":{\"url\":\"https://www.itorix.com\",\"description\":\"External Documentation\"}}";

		// log.info(url.getPath() + " " + url.getQuery());

		SwaggerParseResult swaggerParseResult = new OpenAPIParser().readContents(petStore, null, null);

		// log.info(swaggerParseResult.getOpenAPI().getServers());

		OpenAPI openAPI = swaggerParseResult.getOpenAPI();

		ObjectMapper objectMapper = new ObjectMapper();

		log.info(objectMapper.writeValueAsString(openAPI));

		for (Server s : swaggerParseResult.getOpenAPI().getServers()) {
			log.info(s.getUrl());
			ServerVariables variables = s.getVariables();
			String urlStr = s.getUrl();;
			if (variables != null) {
				for (String k : variables.keySet()) {
					// log.info(k + " ->" +
					// variables.get(k).getDefault());
					if (s.getUrl().contains("{" + k + "}")) {
						urlStr = urlStr.replace("{" + k + "}", variables.get(k).getDefault());
						log.info("Replaced String " + urlStr);
					}
				}
			}
			URL url = new URL(urlStr);
			log.info(url.getPath());
		}

	}

	@Test
	public void checkSwagger2Linting() {
		String swagger2Str = "{\n" + "  \"swagger\": \"2.0\",\n" + "  \"info\": {\n"
				+ "    \"description\": \"This is a sample server Petstore server.  You can find out more about Swagger at [http://swagger.io](http://swagger.io) or on [irc.freenode.net, #swagger](http://swagger.io/irc/).  For this sample, you can use the api key `special-key` to test the authorization filters.\",\n"
				+ "    \"version\": \"1.0.6\",\n" + "    \"title\": \"Swagger Petstore\",\n"
				+ "    \"termsOfService\": \"http://swagger.io/terms/\",\n" + "    \"contact\": {\n"
				+ "      \"email\": \"apiteam@swagger.io\"\n" + "    },\n" + "    \"license\": {\n"
				+ "      \"name\": \"Apache 2.0\",\n"
				+ "      \"url\": \"http://www.apache.org/licenses/LICENSE-2.0.html\"\n" + "    }\n" + "  },\n"
				+ "  \"host\": \"petstore.swagger.io\",\n" + "  \"basePath\": \"/v2\",\n" + "  \"tags\": [\n"
				+ "    {\n" + "      \"name\": \"pet\",\n" + "      \"description\": \"Everything about your Pets\",\n"
				+ "      \"externalDocs\": {\n" + "        \"description\": \"Find out more\",\n"
				+ "        \"url\": \"http://swagger.io\"\n" + "      }\n" + "    },\n" + "    {\n"
				+ "      \"name\": \"store\",\n" + "      \"description\": \"Access to Petstore orders\"\n" + "    },\n"
				+ "    {\n" + "      \"name\": \"user\",\n" + "      \"description\": \"Operations about user\",\n"
				+ "      \"externalDocs\": {\n" + "        \"description\": \"Find out more about our store\",\n"
				+ "        \"url\": \"http://swagger.io\"\n" + "      }\n" + "    }\n" + "  ],\n" + "  \"schemes\": [\n"
				+ "    \"https\",\n" + "    \"http\"\n" + "  ],\n" + "  \"paths\": {\n"
				+ "    \"/pet/{petId}/uploadImage\": {\n" + "      \"post\": {\n" + "        \"tags\": [\n"
				+ "          \"pet\"\n" + "        ],\n" + "        \"summary\": \"uploads an image\",\n"
				+ "        \"description\": \"\",\n" + "        \"operationId\": \"uploadFile\",\n"
				+ "        \"consumes\": [\n" + "          \"multipart/form-data\"\n" + "        ],\n"
				+ "        \"produces\": [\n" + "          \"application/json\"\n" + "        ],\n"
				+ "        \"parameters\": [\n" + "          {\n" + "            \"name\": \"petId\",\n"
				+ "            \"in\": \"path\",\n" + "            \"description\": \"ID of pet to update\",\n"
				+ "            \"required\": true,\n" + "            \"type\": \"integer\",\n"
				+ "            \"format\": \"int64\"\n" + "          },\n" + "          {\n"
				+ "            \"name\": \"additionalMetadata\",\n" + "            \"in\": \"formData\",\n"
				+ "            \"description\": \"Additional data to pass to server\",\n"
				+ "            \"required\": false,\n" + "            \"type\": \"string\"\n" + "          },\n"
				+ "          {\n" + "            \"name\": \"file\",\n" + "            \"in\": \"formData\",\n"
				+ "            \"description\": \"file to upload\",\n" + "            \"required\": false,\n"
				+ "            \"type\": \"file\"\n" + "          }\n" + "        ],\n" + "        \"responses\": {\n"
				+ "          \"200\": {\n" + "            \"description\": \"successful operation\",\n"
				+ "            \"schema\": {\n" + "              \"$ref\": \"#/definitions/ApiResponse\"\n"
				+ "            }\n" + "          }\n" + "        },\n" + "        \"security\": [\n" + "          {\n"
				+ "            \"petstore_auth\": [\n" + "              \"write:pets\",\n"
				+ "              \"read:pets\"\n" + "            ]\n" + "          }\n" + "        ]\n" + "      }\n"
				+ "    },\n" + "    \"/pet\": {\n" + "      \"post\": {\n" + "        \"tags\": [\n"
				+ "          \"pet\"\n" + "        ],\n" + "        \"summary\": \"Add a new pet to the store\",\n"
				+ "        \"description\": \"\",\n" + "        \"operationId\": \"addPet\",\n"
				+ "        \"consumes\": [\n" + "          \"application/json\",\n" + "          \"application/xml\"\n"
				+ "        ],\n" + "        \"produces\": [\n" + "          \"application/json\",\n"
				+ "          \"application/xml\"\n" + "        ],\n" + "        \"parameters\": [\n" + "          {\n"
				+ "            \"in\": \"body\",\n" + "            \"name\": \"body\",\n"
				+ "            \"description\": \"Pet object that needs to be added to the store\",\n"
				+ "            \"required\": true,\n" + "            \"schema\": {\n"
				+ "              \"$ref\": \"#/definitions/Pet\"\n" + "            }\n" + "          }\n"
				+ "        ],\n" + "        \"responses\": {\n" + "          \"405\": {\n"
				+ "            \"description\": \"Invalid input\"\n" + "          }\n" + "        },\n"
				+ "        \"security\": [\n" + "          {\n" + "            \"petstore_auth\": [\n"
				+ "              \"write:pets\",\n" + "              \"read:pets\"\n" + "            ]\n"
				+ "          }\n" + "        ]\n" + "      },\n" + "      \"put\": {\n" + "        \"tags\": [\n"
				+ "          \"pet\"\n" + "        ],\n" + "        \"summary\": \"Update an existing pet\",\n"
				+ "        \"description\": \"\",\n" + "        \"operationId\": \"updatePet\",\n"
				+ "        \"consumes\": [\n" + "          \"application/json\",\n" + "          \"application/xml\"\n"
				+ "        ],\n" + "        \"produces\": [\n" + "          \"application/json\",\n"
				+ "          \"application/xml\"\n" + "        ],\n" + "        \"parameters\": [\n" + "          {\n"
				+ "            \"in\": \"body\",\n" + "            \"name\": \"body\",\n"
				+ "            \"description\": \"Pet object that needs to be added to the store\",\n"
				+ "            \"required\": true,\n" + "            \"schema\": {\n"
				+ "              \"$ref\": \"#/definitions/Pet\"\n" + "            }\n" + "          }\n"
				+ "        ],\n" + "        \"responses\": {\n" + "          \"400\": {\n"
				+ "            \"description\": \"Invalid ID supplied\"\n" + "          },\n" + "          \"404\": {\n"
				+ "            \"description\": \"Pet not found\"\n" + "          },\n" + "          \"405\": {\n"
				+ "            \"description\": \"Validation exception\"\n" + "          }\n" + "        },\n"
				+ "        \"security\": [\n" + "          {\n" + "            \"petstore_auth\": [\n"
				+ "              \"write:pets\",\n" + "              \"read:pets\"\n" + "            ]\n"
				+ "          }\n" + "        ]\n" + "      }\n" + "    },\n" + "    \"/pet/findByStatus\": {\n"
				+ "      \"get\": {\n" + "        \"tags\": [\n" + "          \"pet\"\n" + "        ],\n"
				+ "        \"summary\": \"Finds Pets by status\",\n"
				+ "        \"description\": \"Multiple status values can be provided with comma separated strings\",\n"
				+ "        \"operationId\": \"findPetsByStatus\",\n" + "        \"produces\": [\n"
				+ "          \"application/json\",\n" + "          \"application/xml\"\n" + "        ],\n"
				+ "        \"parameters\": [\n" + "          {\n" + "            \"name\": \"status\",\n"
				+ "            \"in\": \"query\",\n"
				+ "            \"description\": \"Status values that need to be considered for filter\",\n"
				+ "            \"required\": true,\n" + "            \"type\": \"array\",\n"
				+ "            \"items\": {\n" + "              \"type\": \"string\",\n" + "              \"enum\": [\n"
				+ "                \"available\",\n" + "                \"pending\",\n" + "                \"sold\"\n"
				+ "              ],\n" + "              \"default\": \"available\"\n" + "            },\n"
				+ "            \"collectionFormat\": \"multi\"\n" + "          }\n" + "        ],\n"
				+ "        \"responses\": {\n" + "          \"200\": {\n"
				+ "            \"description\": \"successful operation\",\n" + "            \"schema\": {\n"
				+ "              \"type\": \"array\",\n" + "              \"items\": {\n"
				+ "                \"$ref\": \"#/definitions/Pet\"\n" + "              }\n" + "            }\n"
				+ "          },\n" + "          \"400\": {\n"
				+ "            \"description\": \"Invalid status value\"\n" + "          }\n" + "        },\n"
				+ "        \"security\": [\n" + "          {\n" + "            \"petstore_auth\": [\n"
				+ "              \"write:pets\",\n" + "              \"read:pets\"\n" + "            ]\n"
				+ "          }\n" + "        ]\n" + "      }\n" + "    },\n" + "    \"/pet/findByTags\": {\n"
				+ "      \"get\": {\n" + "        \"tags\": [\n" + "          \"pet\"\n" + "        ],\n"
				+ "        \"summary\": \"Finds Pets by tags\",\n"
				+ "        \"description\": \"Multiple tags can be provided with comma separated strings. Use tag1, tag2, tag3 for testing.\",\n"
				+ "        \"operationId\": \"findPetsByTags\",\n" + "        \"produces\": [\n"
				+ "          \"application/json\",\n" + "          \"application/xml\"\n" + "        ],\n"
				+ "        \"parameters\": [\n" + "          {\n" + "            \"name\": \"tags\",\n"
				+ "            \"in\": \"query\",\n" + "            \"description\": \"Tags to filter by\",\n"
				+ "            \"required\": true,\n" + "            \"type\": \"array\",\n"
				+ "            \"items\": {\n" + "              \"type\": \"string\"\n" + "            },\n"
				+ "            \"collectionFormat\": \"multi\"\n" + "          }\n" + "        ],\n"
				+ "        \"responses\": {\n" + "          \"200\": {\n"
				+ "            \"description\": \"successful operation\",\n" + "            \"schema\": {\n"
				+ "              \"type\": \"array\",\n" + "              \"items\": {\n"
				+ "                \"$ref\": \"#/definitions/Pet\"\n" + "              }\n" + "            }\n"
				+ "          },\n" + "          \"400\": {\n" + "            \"description\": \"Invalid tag value\"\n"
				+ "          }\n" + "        },\n" + "        \"security\": [\n" + "          {\n"
				+ "            \"petstore_auth\": [\n" + "              \"write:pets\",\n"
				+ "              \"read:pets\"\n" + "            ]\n" + "          }\n" + "        ],\n"
				+ "        \"deprecated\": true\n" + "      }\n" + "    },\n" + "    \"/pet/{petId}\": {\n"
				+ "      \"get\": {\n" + "        \"tags\": [\n" + "          \"pet\"\n" + "        ],\n"
				+ "        \"summary\": \"Find pet by ID\",\n" + "        \"description\": \"Returns a single pet\",\n"
				+ "        \"operationId\": \"getPetById\",\n" + "        \"produces\": [\n"
				+ "          \"application/json\",\n" + "          \"application/xml\"\n" + "        ],\n"
				+ "        \"parameters\": [\n" + "          {\n" + "            \"name\": \"petId\",\n"
				+ "            \"in\": \"path\",\n" + "            \"description\": \"ID of pet to return\",\n"
				+ "            \"required\": true,\n" + "            \"type\": \"integer\",\n"
				+ "            \"format\": \"int64\"\n" + "          }\n" + "        ],\n"
				+ "        \"responses\": {\n" + "          \"200\": {\n"
				+ "            \"description\": \"successful operation\",\n" + "            \"schema\": {\n"
				+ "              \"$ref\": \"#/definitions/Pet\"\n" + "            }\n" + "          },\n"
				+ "          \"400\": {\n" + "            \"description\": \"Invalid ID supplied\"\n" + "          },\n"
				+ "          \"404\": {\n" + "            \"description\": \"Pet not found\"\n" + "          }\n"
				+ "        },\n" + "        \"security\": [\n" + "          {\n" + "            \"api_key\": []\n"
				+ "          }\n" + "        ]\n" + "      },\n" + "      \"post\": {\n" + "        \"tags\": [\n"
				+ "          \"pet\"\n" + "        ],\n"
				+ "        \"summary\": \"Updates a pet in the store with form data\",\n"
				+ "        \"description\": \"\",\n" + "        \"operationId\": \"updatePetWithForm\",\n"
				+ "        \"consumes\": [\n" + "          \"application/x-www-form-urlencoded\"\n" + "        ],\n"
				+ "        \"produces\": [\n" + "          \"application/json\",\n" + "          \"application/xml\"\n"
				+ "        ],\n" + "        \"parameters\": [\n" + "          {\n"
				+ "            \"name\": \"petId\",\n" + "            \"in\": \"path\",\n"
				+ "            \"description\": \"ID of pet that needs to be updated\",\n"
				+ "            \"required\": true,\n" + "            \"type\": \"integer\",\n"
				+ "            \"format\": \"int64\"\n" + "          },\n" + "          {\n"
				+ "            \"name\": \"name\",\n" + "            \"in\": \"formData\",\n"
				+ "            \"description\": \"Updated name of the pet\",\n" + "            \"required\": false,\n"
				+ "            \"type\": \"string\"\n" + "          },\n" + "          {\n"
				+ "            \"name\": \"status\",\n" + "            \"in\": \"formData\",\n"
				+ "            \"description\": \"Updated status of the pet\",\n" + "            \"required\": false,\n"
				+ "            \"type\": \"string\"\n" + "          }\n" + "        ],\n" + "        \"responses\": {\n"
				+ "          \"405\": {\n" + "            \"description\": \"Invalid input\"\n" + "          }\n"
				+ "        },\n" + "        \"security\": [\n" + "          {\n" + "            \"petstore_auth\": [\n"
				+ "              \"write:pets\",\n" + "              \"read:pets\"\n" + "            ]\n"
				+ "          }\n" + "        ]\n" + "      },\n" + "      \"delete\": {\n" + "        \"tags\": [\n"
				+ "          \"pet\"\n" + "        ],\n" + "        \"summary\": \"Deletes a pet\",\n"
				+ "        \"description\": \"\",\n" + "        \"operationId\": \"deletePet\",\n"
				+ "        \"produces\": [\n" + "          \"application/json\",\n" + "          \"application/xml\"\n"
				+ "        ],\n" + "        \"parameters\": [\n" + "          {\n"
				+ "            \"name\": \"api_key\",\n" + "            \"in\": \"header\",\n"
				+ "            \"required\": false,\n" + "            \"type\": \"string\"\n" + "          },\n"
				+ "          {\n" + "            \"name\": \"petId\",\n" + "            \"in\": \"path\",\n"
				+ "            \"description\": \"Pet id to delete\",\n" + "            \"required\": true,\n"
				+ "            \"type\": \"integer\",\n" + "            \"format\": \"int64\"\n" + "          }\n"
				+ "        ],\n" + "        \"responses\": {\n" + "          \"400\": {\n"
				+ "            \"description\": \"Invalid ID supplied\"\n" + "          },\n" + "          \"404\": {\n"
				+ "            \"description\": \"Pet not found\"\n" + "          }\n" + "        },\n"
				+ "        \"security\": [\n" + "          {\n" + "            \"petstore_auth\": [\n"
				+ "              \"write:pets\",\n" + "              \"read:pets\"\n" + "            ]\n"
				+ "          }\n" + "        ]\n" + "      }\n" + "    },\n" + "    \"/store/order\": {\n"
				+ "      \"post\": {\n" + "        \"tags\": [\n" + "          \"store\"\n" + "        ],\n"
				+ "        \"summary\": \"Place an order for a pet\",\n" + "        \"description\": \"\",\n"
				+ "        \"operationId\": \"placeOrder\",\n" + "        \"consumes\": [\n"
				+ "          \"application/json\"\n" + "        ],\n" + "        \"produces\": [\n"
				+ "          \"application/json\",\n" + "          \"application/xml\"\n" + "        ],\n"
				+ "        \"parameters\": [\n" + "          {\n" + "            \"in\": \"body\",\n"
				+ "            \"name\": \"body\",\n"
				+ "            \"description\": \"order placed for purchasing the pet\",\n"
				+ "            \"required\": true,\n" + "            \"schema\": {\n"
				+ "              \"$ref\": \"#/definitions/Order\"\n" + "            }\n" + "          }\n"
				+ "        ],\n" + "        \"responses\": {\n" + "          \"200\": {\n"
				+ "            \"description\": \"successful operation\",\n" + "            \"schema\": {\n"
				+ "              \"$ref\": \"#/definitions/Order\"\n" + "            }\n" + "          },\n"
				+ "          \"400\": {\n" + "            \"description\": \"Invalid Order\"\n" + "          }\n"
				+ "        }\n" + "      }\n" + "    },\n" + "    \"/store/order/{orderId}\": {\n"
				+ "      \"get\": {\n" + "        \"tags\": [\n" + "          \"store\"\n" + "        ],\n"
				+ "        \"summary\": \"Find purchase order by ID\",\n"
				+ "        \"description\": \"For valid response try integer IDs with value >= 1 and <= 10. Other values will generated exceptions\",\n"
				+ "        \"operationId\": \"getOrderById\",\n" + "        \"produces\": [\n"
				+ "          \"application/json\",\n" + "          \"application/xml\"\n" + "        ],\n"
				+ "        \"parameters\": [\n" + "          {\n" + "            \"name\": \"orderId\",\n"
				+ "            \"in\": \"path\",\n"
				+ "            \"description\": \"ID of pet that needs to be fetched\",\n"
				+ "            \"required\": true,\n" + "            \"type\": \"integer\",\n"
				+ "            \"maximum\": 10,\n" + "            \"minimum\": 1,\n"
				+ "            \"format\": \"int64\"\n" + "          }\n" + "        ],\n"
				+ "        \"responses\": {\n" + "          \"200\": {\n"
				+ "            \"description\": \"successful operation\",\n" + "            \"schema\": {\n"
				+ "              \"$ref\": \"#/definitions/Order\"\n" + "            }\n" + "          },\n"
				+ "          \"400\": {\n" + "            \"description\": \"Invalid ID supplied\"\n" + "          },\n"
				+ "          \"404\": {\n" + "            \"description\": \"Order not found\"\n" + "          }\n"
				+ "        }\n" + "      },\n" + "      \"delete\": {\n" + "        \"tags\": [\n"
				+ "          \"store\"\n" + "        ],\n" + "        \"summary\": \"Delete purchase order by ID\",\n"
				+ "        \"description\": \"For valid response try integer IDs with positive integer value. Negative or non-integer values will generate API errors\",\n"
				+ "        \"operationId\": \"deleteOrder\",\n" + "        \"produces\": [\n"
				+ "          \"application/json\",\n" + "          \"application/xml\"\n" + "        ],\n"
				+ "        \"parameters\": [\n" + "          {\n" + "            \"name\": \"orderId\",\n"
				+ "            \"in\": \"path\",\n"
				+ "            \"description\": \"ID of the order that needs to be deleted\",\n"
				+ "            \"required\": true,\n" + "            \"type\": \"integer\",\n"
				+ "            \"minimum\": 1,\n" + "            \"format\": \"int64\"\n" + "          }\n"
				+ "        ],\n" + "        \"responses\": {\n" + "          \"400\": {\n"
				+ "            \"description\": \"Invalid ID supplied\"\n" + "          },\n" + "          \"404\": {\n"
				+ "            \"description\": \"Order not found\"\n" + "          }\n" + "        }\n" + "      }\n"
				+ "    },\n" + "    \"/store/inventory\": {\n" + "      \"get\": {\n" + "        \"tags\": [\n"
				+ "          \"store\"\n" + "        ],\n"
				+ "        \"summary\": \"Returns pet inventories by status\",\n"
				+ "        \"description\": \"Returns a map of status codes to quantities\",\n"
				+ "        \"operationId\": \"getInventory\",\n" + "        \"produces\": [\n"
				+ "          \"application/json\"\n" + "        ],\n" + "        \"parameters\": [],\n"
				+ "        \"responses\": {\n" + "          \"200\": {\n"
				+ "            \"description\": \"successful operation\",\n" + "            \"schema\": {\n"
				+ "              \"type\": \"object\",\n" + "              \"additionalProperties\": {\n"
				+ "                \"type\": \"integer\",\n" + "                \"format\": \"int32\"\n"
				+ "              }\n" + "            }\n" + "          }\n" + "        },\n"
				+ "        \"security\": [\n" + "          {\n" + "            \"api_key\": []\n" + "          }\n"
				+ "        ]\n" + "      }\n" + "    },\n" + "    \"/user/createWithArray\": {\n"
				+ "      \"post\": {\n" + "        \"tags\": [\n" + "          \"user\"\n" + "        ],\n"
				+ "        \"summary\": \"Creates list of users with given input array\",\n"
				+ "        \"description\": \"\",\n" + "        \"operationId\": \"createUsersWithArrayInput\",\n"
				+ "        \"consumes\": [\n" + "          \"application/json\"\n" + "        ],\n"
				+ "        \"produces\": [\n" + "          \"application/json\",\n" + "          \"application/xml\"\n"
				+ "        ],\n" + "        \"parameters\": [\n" + "          {\n" + "            \"in\": \"body\",\n"
				+ "            \"name\": \"body\",\n" + "            \"description\": \"List of user object\",\n"
				+ "            \"required\": true,\n" + "            \"schema\": {\n"
				+ "              \"type\": \"array\",\n" + "              \"items\": {\n"
				+ "                \"$ref\": \"#/definitions/User\"\n" + "              }\n" + "            }\n"
				+ "          }\n" + "        ],\n" + "        \"responses\": {\n" + "          \"default\": {\n"
				+ "            \"description\": \"successful operation\"\n" + "          }\n" + "        }\n"
				+ "      }\n" + "    },\n" + "    \"/user/createWithList\": {\n" + "      \"post\": {\n"
				+ "        \"tags\": [\n" + "          \"user\"\n" + "        ],\n"
				+ "        \"summary\": \"Creates list of users with given input array\",\n"
				+ "        \"description\": \"\",\n" + "        \"operationId\": \"createUsersWithListInput\",\n"
				+ "        \"consumes\": [\n" + "          \"application/json\"\n" + "        ],\n"
				+ "        \"produces\": [\n" + "          \"application/json\",\n" + "          \"application/xml\"\n"
				+ "        ],\n" + "        \"parameters\": [\n" + "          {\n" + "            \"in\": \"body\",\n"
				+ "            \"name\": \"body\",\n" + "            \"description\": \"List of user object\",\n"
				+ "            \"required\": true,\n" + "            \"schema\": {\n"
				+ "              \"type\": \"array\",\n" + "              \"items\": {\n"
				+ "                \"$ref\": \"#/definitions/User\"\n" + "              }\n" + "            }\n"
				+ "          }\n" + "        ],\n" + "        \"responses\": {\n" + "          \"default\": {\n"
				+ "            \"description\": \"successful operation\"\n" + "          }\n" + "        }\n"
				+ "      }\n" + "    },\n" + "    \"/user/{username}\": {\n" + "      \"get\": {\n"
				+ "        \"tags\": [\n" + "          \"user\"\n" + "        ],\n"
				+ "        \"summary\": \"Get user by user name\",\n" + "        \"description\": \"\",\n"
				+ "        \"operationId\": \"getUserByName\",\n" + "        \"produces\": [\n"
				+ "          \"application/json\",\n" + "          \"application/xml\"\n" + "        ],\n"
				+ "        \"parameters\": [\n" + "          {\n" + "            \"name\": \"username\",\n"
				+ "            \"in\": \"path\",\n"
				+ "            \"description\": \"The name that needs to be fetched. Use user1 for testing. \",\n"
				+ "            \"required\": true,\n" + "            \"type\": \"string\"\n" + "          }\n"
				+ "        ],\n" + "        \"responses\": {\n" + "          \"200\": {\n"
				+ "            \"description\": \"successful operation\",\n" + "            \"schema\": {\n"
				+ "              \"$ref\": \"#/definitions/User\"\n" + "            }\n" + "          },\n"
				+ "          \"400\": {\n" + "            \"description\": \"Invalid username supplied\"\n"
				+ "          },\n" + "          \"404\": {\n" + "            \"description\": \"User not found\"\n"
				+ "          }\n" + "        }\n" + "      },\n" + "      \"put\": {\n" + "        \"tags\": [\n"
				+ "          \"user\"\n" + "        ],\n" + "        \"summary\": \"Updated user\",\n"
				+ "        \"description\": \"This can only be done by the logged in user.\",\n"
				+ "        \"operationId\": \"updateUser\",\n" + "        \"consumes\": [\n"
				+ "          \"application/json\"\n" + "        ],\n" + "        \"produces\": [\n"
				+ "          \"application/json\",\n" + "          \"application/xml\"\n" + "        ],\n"
				+ "        \"parameters\": [\n" + "          {\n" + "            \"name\": \"username\",\n"
				+ "            \"in\": \"path\",\n" + "            \"description\": \"name that need to be updated\",\n"
				+ "            \"required\": true,\n" + "            \"type\": \"string\"\n" + "          },\n"
				+ "          {\n" + "            \"in\": \"body\",\n" + "            \"name\": \"body\",\n"
				+ "            \"description\": \"Updated user object\",\n" + "            \"required\": true,\n"
				+ "            \"schema\": {\n" + "              \"$ref\": \"#/definitions/User\"\n" + "            }\n"
				+ "          }\n" + "        ],\n" + "        \"responses\": {\n" + "          \"400\": {\n"
				+ "            \"description\": \"Invalid user supplied\"\n" + "          },\n"
				+ "          \"404\": {\n" + "            \"description\": \"User not found\"\n" + "          }\n"
				+ "        }\n" + "      },\n" + "      \"delete\": {\n" + "        \"tags\": [\n"
				+ "          \"user\"\n" + "        ],\n" + "        \"summary\": \"Delete user\",\n"
				+ "        \"description\": \"This can only be done by the logged in user.\",\n"
				+ "        \"operationId\": \"deleteUser\",\n" + "        \"produces\": [\n"
				+ "          \"application/json\",\n" + "          \"application/xml\"\n" + "        ],\n"
				+ "        \"parameters\": [\n" + "          {\n" + "            \"name\": \"username\",\n"
				+ "            \"in\": \"path\",\n"
				+ "            \"description\": \"The name that needs to be deleted\",\n"
				+ "            \"required\": true,\n" + "            \"type\": \"string\"\n" + "          }\n"
				+ "        ],\n" + "        \"responses\": {\n" + "          \"400\": {\n"
				+ "            \"description\": \"Invalid username supplied\"\n" + "          },\n"
				+ "          \"404\": {\n" + "            \"description\": \"User not found\"\n" + "          }\n"
				+ "        }\n" + "      }\n" + "    },\n" + "    \"/user/login\": {\n" + "      \"get\": {\n"
				+ "        \"tags\": [\n" + "          \"user\"\n" + "        ],\n"
				+ "        \"summary\": \"Logs user into the system\",\n" + "        \"description\": \"\",\n"
				+ "        \"operationId\": \"loginUser\",\n" + "        \"produces\": [\n"
				+ "          \"application/json\",\n" + "          \"application/xml\"\n" + "        ],\n"
				+ "        \"parameters\": [\n" + "          {\n" + "            \"name\": \"username\",\n"
				+ "            \"in\": \"query\",\n" + "            \"description\": \"The user name for login\",\n"
				+ "            \"required\": true,\n" + "            \"type\": \"string\"\n" + "          },\n"
				+ "          {\n" + "            \"name\": \"password\",\n" + "            \"in\": \"query\",\n"
				+ "            \"description\": \"The password for login in clear text\",\n"
				+ "            \"required\": true,\n" + "            \"type\": \"string\"\n" + "          }\n"
				+ "        ],\n" + "        \"responses\": {\n" + "          \"200\": {\n"
				+ "            \"description\": \"successful operation\",\n" + "            \"headers\": {\n"
				+ "              \"X-Expires-After\": {\n" + "                \"type\": \"string\",\n"
				+ "                \"format\": \"date-time\",\n"
				+ "                \"description\": \"date in UTC when token expires\"\n" + "              },\n"
				+ "              \"X-Rate-Limit\": {\n" + "                \"type\": \"integer\",\n"
				+ "                \"format\": \"int32\",\n"
				+ "                \"description\": \"calls per hour allowed by the user\"\n" + "              }\n"
				+ "            },\n" + "            \"schema\": {\n" + "              \"type\": \"string\"\n"
				+ "            }\n" + "          },\n" + "          \"400\": {\n"
				+ "            \"description\": \"Invalid username/password supplied\"\n" + "          }\n"
				+ "        }\n" + "      }\n" + "    },\n" + "    \"/user/logout\": {\n" + "      \"get\": {\n"
				+ "        \"tags\": [\n" + "          \"user\"\n" + "        ],\n"
				+ "        \"summary\": \"Logs out current logged in user session\",\n"
				+ "        \"description\": \"\",\n" + "        \"operationId\": \"logoutUser\",\n"
				+ "        \"produces\": [\n" + "          \"application/json\",\n" + "          \"application/xml\"\n"
				+ "        ],\n" + "        \"parameters\": [],\n" + "        \"responses\": {\n"
				+ "          \"default\": {\n" + "            \"description\": \"successful operation\"\n"
				+ "          }\n" + "        }\n" + "      }\n" + "    },\n" + "    \"/user\": {\n"
				+ "      \"post\": {\n" + "        \"tags\": [\n" + "          \"user\"\n" + "        ],\n"
				+ "        \"summary\": \"Create user\",\n"
				+ "        \"description\": \"This can only be done by the logged in user.\",\n"
				+ "        \"operationId\": \"createUser\",\n" + "        \"consumes\": [\n"
				+ "          \"application/json\"\n" + "        ],\n" + "        \"produces\": [\n"
				+ "          \"application/json\",\n" + "          \"application/xml\"\n" + "        ],\n"
				+ "        \"parameters\": [\n" + "          {\n" + "            \"in\": \"body\",\n"
				+ "            \"name\": \"body\",\n" + "            \"description\": \"Created user object\",\n"
				+ "            \"required\": true,\n" + "            \"schema\": {\n"
				+ "              \"$ref\": \"#/definitions/User\"\n" + "            }\n" + "          }\n"
				+ "        ],\n" + "        \"responses\": {\n" + "          \"default\": {\n"
				+ "            \"description\": \"successful operation\"\n" + "          }\n" + "        }\n"
				+ "      }\n" + "    }\n" + "  },\n" + "  \"securityDefinitions\": {\n" + "    \"api_key\": {\n"
				+ "      \"type\": \"apiKey\",\n" + "      \"name\": \"api_key\",\n" + "      \"in\": \"header\"\n"
				+ "    },\n" + "    \"petstore_auth\": {\n" + "      \"type\": \"oauth2\",\n"
				+ "      \"authorizationUrl\": \"https://petstore.swagger.io/oauth/authorize\",\n"
				+ "      \"flow\": \"implicit\",\n" + "      \"scopes\": {\n"
				+ "        \"read:pets\": \"read your pets\",\n"
				+ "        \"write:pets\": \"modify pets in your account\"\n" + "      }\n" + "    }\n" + "  },\n"
				+ "  \"definitions\": {\n" + "    \"ApiResponse\": {\n" + "      \"type\": \"object\",\n"
				+ "      \"properties\": {\n" + "        \"code\": {\n" + "          \"type\": \"integer\",\n"
				+ "          \"format\": \"int32\"\n" + "        },\n" + "        \"type\": {\n"
				+ "          \"type\": \"string\"\n" + "        },\n" + "        \"message\": {\n"
				+ "          \"type\": \"string\"\n" + "        }\n" + "      }\n" + "    },\n"
				+ "    \"Category\": {\n" + "      \"type\": \"object\",\n" + "      \"properties\": {\n"
				+ "        \"id\": {\n" + "          \"type\": \"integer\",\n" + "          \"format\": \"int64\"\n"
				+ "        },\n" + "        \"name\": {\n" + "          \"type\": \"string\"\n" + "        }\n"
				+ "      },\n" + "      \"xml\": {\n" + "        \"name\": \"Category\"\n" + "      }\n" + "    },\n"
				+ "    \"Pet\": {\n" + "      \"type\": \"object\",\n" + "      \"required\": [\n"
				+ "        \"name\",\n" + "        \"photoUrls\"\n" + "      ],\n" + "      \"properties\": {\n"
				+ "        \"id\": {\n" + "          \"type\": \"integer\",\n" + "          \"format\": \"int64\"\n"
				+ "        },\n" + "        \"category\": {\n" + "          \"$ref\": \"#/definitions/Category\"\n"
				+ "        },\n" + "        \"name\": {\n" + "          \"type\": \"string\",\n"
				+ "          \"example\": \"doggie\"\n" + "        },\n" + "        \"photoUrls\": {\n"
				+ "          \"type\": \"array\",\n" + "          \"xml\": {\n" + "            \"wrapped\": true\n"
				+ "          },\n" + "          \"items\": {\n" + "            \"type\": \"string\",\n"
				+ "            \"xml\": {\n" + "              \"name\": \"photoUrl\"\n" + "            }\n"
				+ "          }\n" + "        },\n" + "        \"tags\": {\n" + "          \"type\": \"array\",\n"
				+ "          \"xml\": {\n" + "            \"wrapped\": true\n" + "          },\n"
				+ "          \"items\": {\n" + "            \"xml\": {\n" + "              \"name\": \"tag\"\n"
				+ "            },\n" + "            \"$ref\": \"#/definitions/Tag\"\n" + "          }\n"
				+ "        },\n" + "        \"status\": {\n" + "          \"type\": \"string\",\n"
				+ "          \"description\": \"pet status in the store\",\n" + "          \"enum\": [\n"
				+ "            \"available\",\n" + "            \"pending\",\n" + "            \"sold\"\n"
				+ "          ]\n" + "        }\n" + "      },\n" + "      \"xml\": {\n" + "        \"name\": \"Pet\"\n"
				+ "      }\n" + "    },\n" + "    \"Tag\": {\n" + "      \"type\": \"object\",\n"
				+ "      \"properties\": {\n" + "        \"id\": {\n" + "          \"type\": \"integer\",\n"
				+ "          \"format\": \"int64\"\n" + "        },\n" + "        \"name\": {\n"
				+ "          \"type\": \"string\"\n" + "        }\n" + "      },\n" + "      \"xml\": {\n"
				+ "        \"name\": \"Tag\"\n" + "      }\n" + "    },\n" + "    \"Order\": {\n"
				+ "      \"type\": \"object\",\n" + "      \"properties\": {\n" + "        \"id\": {\n"
				+ "          \"type\": \"integer\",\n" + "          \"format\": \"int64\"\n" + "        },\n"
				+ "        \"petId\": {\n" + "          \"type\": \"integer\",\n" + "          \"format\": \"int64\"\n"
				+ "        },\n" + "        \"quantity\": {\n" + "          \"type\": \"integer\",\n"
				+ "          \"format\": \"int32\"\n" + "        },\n" + "        \"shipDate\": {\n"
				+ "          \"type\": \"string\",\n" + "          \"format\": \"date-time\"\n" + "        },\n"
				+ "        \"status\": {\n" + "          \"type\": \"string\",\n"
				+ "          \"description\": \"Order Status\",\n" + "          \"enum\": [\n"
				+ "            \"placed\",\n" + "            \"approved\",\n" + "            \"delivered\"\n"
				+ "          ]\n" + "        },\n" + "        \"complete\": {\n" + "          \"type\": \"boolean\"\n"
				+ "        }\n" + "      },\n" + "      \"xml\": {\n" + "        \"name\": \"Order\"\n" + "      }\n"
				+ "    },\n" + "    \"User\": {\n" + "      \"type\": \"object\",\n" + "      \"properties\": {\n"
				+ "        \"id\": {\n" + "          \"type\": \"integer\",\n" + "          \"format\": \"int64\"\n"
				+ "        },\n" + "        \"username\": {\n" + "          \"type\": \"string\"\n" + "        },\n"
				+ "        \"firstName\": {\n" + "          \"type\": \"string\"\n" + "        },\n"
				+ "        \"lastName\": {\n" + "          \"type\": \"string\"\n" + "        },\n"
				+ "        \"email\": {\n" + "          \"type\": \"string\"\n" + "        },\n"
				+ "        \"password\": {\n" + "          \"type\": \"string\"\n" + "        },\n"
				+ "        \"phone\": {\n" + "          \"type\": \"string\"\n" + "        },\n"
				+ "        \"userStatus\": {\n" + "          \"type\": \"integer\",\n"
				+ "          \"format\": \"int32\",\n" + "          \"description\": \"User Status\"\n" + "        }\n"
				+ "      },\n" + "      \"xml\": {\n" + "        \"name\": \"User\"\n" + "      }\n" + "    }\n"
				+ "  },\n" + "  \"externalDocs\": {\n" + "    \"description\": \"Find out more about Swagger\",\n"
				+ "    \"url\": \"http://swagger.io\"\n" + "  }\n" + "}";

		SwaggerParser swaggerParser = new SwaggerParser();
		Swagger swagger = swaggerParser.parse(swagger2Str);

		SwaggerDeserializationResult swaggerDeserializationResult = swaggerParser.readWithInfo(swagger2Str);
		log.info(String.valueOf(swaggerDeserializationResult));

	}

	@Test
	public void checkSwaggerResponseSchema() throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		String swagger2Str = "{\"swagger\":\"2.0\",\"info\":{\"title\":\"EDI Claim Status Inquiry\",\"description\":\"The EDI 276 transaction set is a Health Care Claim Status Inquiry. It is used by healthcare providers to verify the status of a claim submitted previously to a payer, such as an insurance company, HMO, government agency like Medicare or Medicaid, etc. The 276 transaction is specified by HIPAA for the electronic submission of claim status requests. The transaction typically includes:\\n\\n1. Provider identification\\n2. Patient identification\\n3. Subscriber information\\n4. Date(s) of service(s)\\n5. Charges\\n\\nThe EDI 277 Health Care Claim Status Response transaction set is used by healthcare payers (insurance companies, Medicare, etc.) to report on the status of claims (837 transactions) previously submitted by providers.\",\"version\":\"v1.0.1\",\"termsOfService\":\"https://acme.com/terms-and-conditions\",\"license\":{\"name\":\"Acme EDI License\",\"url\":\"https://acme.com/license\"},\"contact\":{\"name\":\"Thomas Hart\",\"url\":\"https://acme.com/contact-us\",\"email\":\"thomas@acme.com\"}},\"paths\":{\"/status-inquiry\":{\"parameters\":[{\"name\":\"Authorization\",\"in\":\"header\",\"required\":true,\"description\":\"\",\"type\":\"string\"}],\"x-description\":\"This API is designed to assist those who request reviews (specialty care, treatment, admission) and those who respond to those requests using the 278 format.\",\"post\":{\"summary\":\"This API is designed to assist those who request reviews (specialty care, treatment, admission) and those who respond to those requests using the 278 format.\",\"description\":\"Use the Claim Status Inquiry (276) transaction to inquire about the status of a claim after it has been sent to a payer, whether submitted on paper or electronically.\\n\\nThe Claim Status Response (277) transaction is used to respond to a request inquiry about the status of a claim after it has been sent to a payer, whether submitted on paper or electronically.\",\"parameters\":[{\"x-key\":1,\"name\":\"x-senderId\",\"in\":\"header\",\"required\":true,\"description\":\"Sender ID of the client application making the request.\",\"type\":\"string\"},{\"x-key\":2,\"name\":\"x-receiverId\",\"in\":\"header\",\"required\":true,\"description\":\"Receiver ID of the client application making the request.\",\"type\":\"string\"},{\"x-key\":3,\"name\":\"Content-Type\",\"in\":\"header\",\"required\":true,\"description\":\"Media type of the request payload\",\"x-example\":\"\",\"default\":\"application/json\",\"type\":\"string\"},{\"x-key\":4,\"name\":\"Accept\",\"in\":\"header\",\"required\":false,\"description\":\"Response media type\",\"default\":\"application/json\",\"type\":\"string\"},{\"name\":\"claimsInquiryRequest\",\"in\":\"body\",\"description\":\"Use the Claim Status Inquiry (276) transaction to inquire about the status of a claim after it has been sent to a payer, whether submitted on paper or electronically.\",\"required\":true,\"schema\":{\"description\":\"\",\"$ref\":\"#/definitions/claimsRequestModel\"},\"x-examples\":{\"application/json\":{\"payloadType\":\"X12_276_Request_005010X279A1\",\"processingMode\":\"RealTime\",\"payloadId\":\"e51d4fae-7dec-11d0-a866-00a0c91e6da2\",\"timestamp\":\"2018-06-15T15:35:45+03:00\",\"payload\":\"ISA*00*          *00*          *ZZ*AVAILITY       *ZZ*BEACON963116116*210122*2010*^*00501*000003100*0*P*:~GS*HR*AVAILITY*BEACON963116116*20210122*2010*1*X*005010X212~ST*276*0001*005010X212~BHT*0010*13*23749464748*20210122*121008~HL*1**20*1~NM1*PR*2*AVAILITY CLEARINGHOUSE*****PI*BHOVO~HL*2*1*21*1~NM1*41*2*REALMED CORPORATION*****46*S00086~HL*3*2*19*1~NM1*1P*2*MONTEFIORE NYACK HOSPITAL*****XX*1104808062~HL*4*3*22*0~DMG*D8*19690411*M~NM1*IL*1*HARTE*JAMES****MI*K9055514802~TRN*1*123456789~REF*1K*499380E0E36B45A7A230516B740BB0FE00000441~REF*D9*23749464748~AMT*T3*323~DTP*472*RD8*20210118-20210118~SE*17*0001~GE*1*1~IEA*1*000003100~\"}}}],\"operationId\":\"status-inquiry\",\"responses\":{\"200\":{\"description\":\"The Claim Status Response (277) transaction is used to respond to a request inquiry about the status of a claim after it has been sent to a payer, whether submitted on paper or electronically.\",\"schema\":{\"$ref\":\"#/definitions/claimsResponseModel\"},\"headers\":{\"Content-Type\":{\"description\":\"\",\"default\":\"application/json\",\"type\":\"string\"}},\"examples\":{\"application/json\":{\"payloadType\":\"X12_276_Request_005010X279A1\",\"processingMode\":\"RealTime\",\"payloadId\":\"e51d4fae-7dec-11d0-a866-00a0c91e6da2\",\"timestamp\":\"2018-06-15T15:35:45+03:00\",\"senderId\":\"PRACINSI\",\"receiverId\":\"BHO-541414194\",\"payload\":\"ISA*00*          *00*          *ZZ*AVAILITY       *ZZ*BEACON963116116*210122*2010*^*00501*000003100*0*P*:~GS*HR*AVAILITY*BEACON963116116*20210122*2010*1*X*005010X212~ST*276*0001*005010X212~BHT*0010*13*23749464748*20210122*121008~HL*1**20*1~NM1*PR*2*AVAILITY CLEARINGHOUSE*****PI*BHOVO~HL*2*1*21*1~NM1*41*2*REALMED CORPORATION*****46*S00086~HL*3*2*19*1~NM1*1P*2*MONTEFIORE NYACK HOSPITAL*****XX*1104808062~HL*4*3*22*0~DMG*D8*19690411*M~NM1*IL*1*HARTE*JAMES****MI*K9055514802~TRN*1*123456789~REF*1K*499380E0E36B45A7A230516B740BB0FE00000441~REF*D9*23749464748~AMT*T3*323~DTP*472*RD8*20210118-20210118~SE*17*0001~GE*1*1~IEA*1*000003100~\"}}},\"400\":{\"description\":\"The request could not be understood by the server due to malformed syntax. The client SHOULD NOT repeat the request without modifications.\",\"schema\":{\"$ref\":\"#/definitions/errorSchema\"}},\"401\":{\"description\":\"The request requires user authentication.\",\"schema\":{\"$ref\":\"#/definitions/errorSchema\"}},\"403\":{\"description\":\"The server understood the request, but is refusing to fulfill it. Authorization will not help and the request SHOULD NOT be repeated.\",\"schema\":{\"$ref\":\"#/definitions/errorSchema\"}},\"404\":{\"description\":\"The server has not found anything matching the Request-URI. No indication is given of whether the condition is temporary or permanent.\",\"schema\":{\"$ref\":\"#/definitions/errorSchema\"}},\"405\":{\"description\":\"The method specified in the Request-Line is not allowed for the resource identified by the Request-URI. The response MUST include an Allow header containing a list of valid methods for the requested resource.\",\"schema\":{\"$ref\":\"#/definitions/errorSchema\"}},\"406\":{\"description\":\"The resource identified by the request is only capable of generating response entities which have content characteristics not acceptable according to the accept headers sent in the request.\",\"schema\":{\"$ref\":\"#/definitions/errorSchema\"}},\"429\":{\"description\":\"Request quota settings limit\",\"schema\":{\"$ref\":\"#/definitions/errorSchema\"}},\"500\":{\"description\":\"The server encountered an unexpected condition which prevented it from fulfilling the request.\",\"headers\":{},\"schema\":{\"$ref\":\"#/definitions/errorSchema\"}},\"501\":{\"description\":\"The server does not support the functionality required to fulfill the request. This is the appropriate response when the server does not recognize the request method and is not capable of supporting it for any resource.\",\"schema\":{\"$ref\":\"#/definitions/errorSchema\"}},\"503\":{\"description\":\"The server is currently unable to handle the request due to a temporary overloading or maintenance of the server.\",\"schema\":{\"$ref\":\"#/definitions/errorSchema\"}},\"504\":{\"description\":\"The server, while acting as a gateway or proxy, did not receive a timely response from the upstream server specified by the URI (e.g. HTTP, FTP, LDAP) or some other auxiliary server (e.g. DNS) it needed to access in attempting to complete the request.\",\"schema\":{\"$ref\":\"#/definitions/errorSchema\"}}},\"tags\":[\"Claims\"],\"security\":[{\"oauth2\":[\"read\",\"write\",\"update\",\"delete\"]}]}}},\"schemes\":[\"https\"],\"x-servers\":[{\"description\":\"Sandbox Server\",\"url\":\"https://sandbox.acme.com/\"},{\"description\":\"Development Server\",\"url\":\"https://dev.acme.com/\"},{\"description\":\"QAT Server\",\"url\":\"https://qat.acme.com/\"},{\"description\":\"Production Server\",\"url\":\"https://api.acme.com/\"}],\"consumes\":[\"application/json\"],\"produces\":[\"application/json\",\"application/xml\",\"text/xml\"],\"tags\":[{\"name\":\"Claims\",\"description\":\"This API is designed to assist those who request reviews (specialty care, treatment, admission) and those who respond to those requests using the 278 format.\"}],\"basePath\":\"/v1/claims\",\"host\":\"acme.com\",\"definitions\":{\"claimsRequestModel\":{\"description\":\"This API is designed to assist those who request reviews (specialty care, treatment, admission) and those who respond to those requests using the 278 format.\",\"properties\":{\"payloadType\":{\"type\":\"string\",\"description\":\"Paylaod type of EDI X12 request\"},\"processingMode\":{\"type\":\"string\",\"description\":\"Processing mode of the EDI 276/277 request\"},\"payloadID\":{\"type\":\"string\",\"description\":\"ID of the EDI X12 request\"},\"timestamp\":{\"type\":\"string\",\"format\":\"date-time\",\"pattern\":\"[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1]) (2[0-3]|[01][0-9]):[0-5][0-9]\",\"description\":\"Timestamp the request was sent in\"},\"payload\":{\"type\":\"string\",\"description\":\"X12 EDI content or payload used for checking claim status\"}},\"type\":\"object\"},\"claimsResponseModel\":{\"description\":\"This API is designed to assist those who request reviews (specialty care, treatment, admission) and those who respond to those requests using the 278 format.\",\"properties\":{\"payloadType\":{\"type\":\"string\",\"description\":\"Paylaod type of EDI X12 request\"},\"processingMode\":{\"type\":\"string\",\"description\":\"Processing mode of the EDI 276/277 request\"},\"payloadID\":{\"type\":\"string\",\"description\":\"ID of the EDI X12 request\"},\"senderID\":{\"type\":\"string\",\"description\":\"Sender ID of the EDI X12 request\"},\"receiverID\":{\"type\":\"string\",\"description\":\"Receiver ID of the EDI X12 request\"},\"timestamp\":{\"type\":\"string\",\"format\":\"date-time\",\"pattern\":\"[0-9]{4}-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1]) (2[0-3]|[01][0-9]):[0-5][0-9]\",\"description\":\"Timestamp the request was sent in\"},\"payload\":{\"type\":\"string\",\"description\":\"X12 EDI content or payload used for checking claim status\"}},\"type\":\"object\"},\"errorSchema\":{\"description\":\"Container to hold error data model for X12 transactions\",\"type\":\"object\",\"required\":[\"errors\"],\"properties\":{\"errors\":{\"type\":\"array\",\"items\":{\"type\":\"object\",\"properties\":{\"code\":{\"type\":\"string\",\"example\":\"Security-1001\"},\"userMessage\":{\"type\":\"string\",\"example\":\"Invalid Access Token\"},\"info\":{\"type\":\"string\",\"example\":\"https://developers.myapi.com/errors/#Security-1001\"}}}}}}},\"securityDefinitions\":{\"oauth2\":{\"type\":\"oauth2\",\"description\":\"The Client Credentials grant is used when applications request an access token to access their own resources, not on behalf of a user.\",\"scopes\":{\"read\":\"Allow read scope\",\"write\":\"Allow write scope\",\"update\":\"Allow update scope\",\"delete\":\"Allow delete scope\"},\"flow\":\"application\",\"tokenUrl\":\"https://api.acme.com/v1/access/token\"}},\"parameters\":{},\"x-mock\":true,\"x-metadata\":{\"metadata\":{\"swaggerName\":\"6bb7467b53e248c8a62ce85c35b2eee9\",\"revision\":1,\"documentation\":[{\"title\":\"Oauth 2.0 Client Credentials Flow\",\"summary\":\"The Client Credentials grant is used when applications request an access token to access their own resources, not on behalf of a user.\",\"content\":\"# **Client Credentials Flow**\\n\\nWith machine-to-machine (M2M) applications, such as CLIs, daemons, or services running on your back-end, the system authenticates and authorizes the app rather than a user. For this scenario, typical authentication schemes like username + password or social logins don't make sense. Instead, M2M apps use the Client Credentials Flow (defined in OAuth 2.0 RFC 6749, section 4.4), in which they pass along their Client ID and Client Secret to authenticate themselves and get a token.\\n\\n### **How it works**\\n\\n![](https://release.apiwiz.io:443/artifactory/apiwiz-images/auth-sequence-client-credentials.png)\\n\\n\\n1. Your app authenticates with the Auth0 Authorization Server using its Client ID and Client Secret\\n2. our Auth0 Authorization Server validates the Client ID and Client Secret.\\n3. Your Auth0 Authorization Server responds with an Access Token.\\n4. Your application can use the Access Token to call an API on behalf of itself.\\n5. The API responds with requested data.\\n\\n### **Request Parameters**\\n\\n**grant_type (required)**\\n\\nThe grant_type parameter must be set to client_credentials.\\n\\n**scope (optional)**\\n\\nYour service can support different scopes for the client credentials grant. In practice, not many services actually support this.\\n\\n**Client Authentication (required)**\\n\\nThe client needs to authenticate themselves for this request. Typically the service will allow either additional request parameters client_id and client_secret, or accept the client ID and secret in the HTTP Basic auth header.\\n\\n### **Example**\\n\\nThe following is an example authorization code grant the service would receive.\\n\\n> curl --location --request POST 'https://api.acme.com/v1/auth/token' \\\\\\n> --header 'Authorization: Basic dXNlcm5hbWU6cGFzc3dvcmQ=' \\\\\\n> --header 'grant_type: client_credentials' \\\\\\n> --header 'scope: read'\\n\"},{\"title\":\"Using CORS\",\"summary\":\"APIs are the threads that let you stitch together a rich web experience. But this experience has a hard time translating to the browser, where the options for cross-domain requests are limited to techniques like JSON-P (which has limited use due to security concerns) or setting up a custom proxy (which can be a pain to set up and maintain).\",\"content\":\"# Introduction\\n\\nAPIs are the threads that let you stitch together a rich web experience. But this experience has a hard time translating to the browser, where the options for cross-domain requests are limited to techniques like JSON-P (which has limited use due to security concerns) or setting up a custom proxy (which can be a pain to set up and maintain).\\n\\nCross-Origin Resource Sharing (CORS) is a W3C spec that allows cross-domain communication from the browser. By building on top of the XMLHttpRequest object, CORS allows developers to work with the same idioms as same-domain requests.\\n\\nThe use-case for CORS is simple. Imagine the site alice.com has some data that the site bob.com wants to access. This type of request traditionally wouldn’t be allowed under the browser’s same origin policy. However, by supporting CORS requests, alice.com can add a few special response headers that allows bob.com to access the data.\\n\\nAs you can see from this example, CORS support requires coordination between both the server and client. Luckily, if you are a client-side developer you are shielded from most of these details. The rest of this article shows how clients can make cross-origin requests, and how servers can configure themselves to support CORS.\\n\\n# Adding CORS support to the server\\n\\nMost of the heavy lifting for CORS is handled between the browser and the server. The browser adds some additional headers, and sometimes makes additional requests, during a CORS request on behalf of the client. These additions are hidden from the client (but can be discovered using a packet analyzer such as Wireshark).\\n\\n# CORS Server Flowchart\\n\\n![](https://release.apiwiz.io/artifactory/apiwiz-images/cors_server_flowchart.png)\"}],\"category\":[{\"name\":\"Unsorted\",\"paths\":[],\"definitions\":[]},{\"name\":\"Claims Response Model\",\"paths\":[],\"definitions\":[\"claimsResponseModel\"]},{\"name\":\"Claims Request Model\",\"paths\":[],\"definitions\":[\"claimsRequestModel\"]},{\"name\":\"Claims Error Model\",\"paths\":[],\"definitions\":[\"errorSchema\"]},{\"name\":\"Claims Resource Paths \",\"paths\":[\"/status-inquiry\"],\"definitions\":[]}]}},\"security\":[{\"oauth2\":[\"read\",\"write\",\"update\",\"delete\"]}]}";

		String test = "{\n" + "    \"swagger\": \"2.0\",\n" + "    \"info\": {\n"
				+ "        \"description\": \"This is a sample server product catalog server.  For this sample, you can use the api key `special-key` to test the authorization filters.\",\n"
				+ "        \"version\": \"v1\",\n" + "        \"title\": \"ProductCatalog\",\n"
				+ "        \"contact\": {\n" + "            \"name\": \"Support\",\n"
				+ "            \"url\": \"https://itorix.com/contact\",\n"
				+ "            \"email\": \"support@itorix.com\"\n" + "        },\n" + "        \"license\": {\n"
				+ "            \"name\": \"Itoric.Inc\",\n" + "            \"url\": \"https://itorix.com\"\n"
				+ "        }\n" + "    },\n" + "    \"host\": \"coreapiteam-test.apigee.net\",\n"
				+ "    \"basePath\": \"/v1/productsss\",\n" + "    \"schemes\": [\n" + "        \"https\"\n"
				+ "    ],\n" + "    \"consumes\": [\n" + "        \"application/json\"\n" + "    ],\n"
				+ "    \"produces\": [\n" + "        \"application/json\"\n" + "    ],\n" + "    \"paths\": {\n"
				+ "        \"/productAvailability\": {\n" + "            \"get\": {\n" + "                \"tags\": [\n"
				+ "                    \"ProductAvailability\"\n" + "                ],\n"
				+ "                \"summary\": \"Product Availability\",\n"
				+ "                \"description\": \"Check for product availability\",\n"
				+ "                \"produces\": [\n" + "                    \"application/json\"\n"
				+ "                ],\n" + "                \"parameters\": [\n" + "                    {\n"
				+ "                        \"name\": \"Authorization\",\n"
				+ "                        \"in\": \"header\",\n"
				+ "                        \"description\": \"OAuth 2.0 Bearer Token\",\n"
				+ "                        \"required\": true,\n" + "                        \"type\": \"string\",\n"
				+ "                        \"x-example\": \"Bearer Uuafp57IjZ1RmQKruKVw7MXWEQ8h\"\n"
				+ "                    },\n" + "                    {\n"
				+ "                        \"name\": \"Accept\",\n" + "                        \"in\": \"header\",\n"
				+ "                        \"description\": \"The Accept request-header field can be used to specify certain media types which are acceptable for the response.\",\n"
				+ "                        \"required\": true,\n" + "                        \"type\": \"string\",\n"
				+ "                        \"x-example\": \"application/json\"\n" + "                    }\n"
				+ "                ],\n" + "                \"responses\": {\n" + "                    \"200\": {\n"
				+ "                        \"description\": \"Status 200\",\n"
				+ "                        \"schema\": {\n"
				+ "                            \"$ref\": \"#/definitions/GetAvailabilityResponse\"\n"
				+ "                        },\n" + "                        \"examples\": {\n"
				+ "                            \"application/json\": \"{\\n    \\\"products\\\": [\\n        {\\n            \\\"productId\\\": \\\"PMS-425-02\\\",\\n            \\\"name\\\": \\\"I Love APIs sticker\\\",\\n            \\\"description\\\": \\\"I Love APIs sticker - White\\\",\\n            \\\"uuid\\\": \\\"12541a04-6f17-11e4-8810-67f41b6ea52d\\\"\\n        }\\n    ]\\n}\"\n"
				+ "                        }\n" + "                    },\n" + "                    \"401\": {\n"
				+ "                        \"description\": \"Status 401\",\n"
				+ "                        \"schema\": {\n"
				+ "                            \"$ref\": \"#/definitions/errors\"\n" + "                        },\n"
				+ "                        \"examples\": {\n"
				+ "                            \"application/json\": \"{\\n    \\\"errors\\\": [\\n        {\\n            \\\"code\\\": \\\"Security-1001\\\",\\n            \\\"userMessage\\\": \\\"Invalid Access Token\\\",\\n            \\\"info\\\": \\\"https://developers.myapi.com/errors/#Security-1001\\\"\\n        }\\n    ]\\n}\"\n"
				+ "                        }\n" + "                    }\n" + "                },\n"
				+ "                \"security\": [\n" + "                    {\n"
				+ "                        \"OAuth2\": [\n" + "                            \"catalog:read\",\n"
				+ "                            \"catalog:write\"\n" + "                        ]\n"
				+ "                    }\n" + "                ]\n" + "            }\n" + "        },\n"
				+ "        \"/{productid}\": {\n" + "            \"get\": {\n" + "                \"tags\": [\n"
				+ "                    \"Get Product Details\"\n" + "                ],\n"
				+ "                \"summary\": \"Get Product Details\",\n"
				+ "                \"description\": \"Get detailed product details\",\n"
				+ "                \"produces\": [\n" + "                    \"application/json\"\n"
				+ "                ],\n" + "                \"parameters\": [\n" + "                    {\n"
				+ "                        \"name\": \"Authorization\",\n"
				+ "                        \"in\": \"header\",\n"
				+ "                        \"description\": \"OAuth 2.0 Bearer Token\",\n"
				+ "                        \"required\": true,\n" + "                        \"type\": \"string\",\n"
				+ "                        \"x-example\": \"Bearer Uuafp57IjZ1RmQKruKVw7MXWEQ8h\"\n"
				+ "                    },\n" + "                    {\n"
				+ "                        \"name\": \"productid\",\n" + "                        \"in\": \"path\",\n"
				+ "                        \"required\": true,\n" + "                        \"type\": \"string\"\n"
				+ "                    },\n" + "                    {\n"
				+ "                        \"name\": \"Accept\",\n" + "                        \"in\": \"header\",\n"
				+ "                        \"description\": \"The Accept request-header field can be used to specify certain media types which are acceptable for the response.\",\n"
				+ "                        \"required\": true,\n" + "                        \"type\": \"string\",\n"
				+ "                        \"x-example\": \"application/json\"\n" + "                    }\n"
				+ "                ],\n" + "                \"responses\": {\n" + "                    \"200\": {\n"
				+ "                        \"description\": \"Status 200\",\n"
				+ "                        \"schema\": {\n"
				+ "                            \"$ref\": \"#/definitions/ProductResponse\"\n"
				+ "                        },\n" + "                        \"examples\": {\n"
				+ "                            \"application/json\": \"{\\n    \\\"products\\\": [\\n        {\\n            \\\"productId\\\": \\\"PMS-425-02\\\",\\n            \\\"name\\\": \\\"I Love APIs sticker\\\",\\n            \\\"description\\\": \\\"I Love APIs sticker - White\\\",\\n            \\\"uuid\\\": \\\"12541a04-6f17-11e4-8810-67f41b6ea52d\\\"\\n        }\\n    ]\\n}\"\n"
				+ "                        }\n" + "                    },\n" + "                    \"401\": {\n"
				+ "                        \"description\": \"Status 401\",\n"
				+ "                        \"schema\": {\n"
				+ "                            \"$ref\": \"#/definitions/errors\"\n" + "                        },\n"
				+ "                        \"examples\": {\n"
				+ "                            \"application/json\": \"{\\n    \\\"errors\\\": [\\n        {\\n            \\\"code\\\": \\\"Security-1001\\\",\\n            \\\"userMessage\\\": \\\"Invalid Access Token\\\",\\n            \\\"info\\\": \\\"https://developers.myapi.com/errors/#Security-1001\\\"\\n        }\\n    ]\\n}\"\n"
				+ "                        }\n" + "                    }\n" + "                },\n"
				+ "                \"security\": [\n" + "                    {\n"
				+ "                        \"OAuth2\": [\n" + "                            \"catalog:read\",\n"
				+ "                            \"catalog:write\"\n" + "                        ]\n"
				+ "                    }\n" + "                ]\n" + "            }\n" + "        },\n"
				+ "        \"/list\": {\n" + "            \"get\": {\n" + "                \"tags\": [\n"
				+ "                    \"Get List of Products\"\n" + "                ],\n"
				+ "                \"summary\": \"Get List of products\",\n"
				+ "                \"description\": \"Get list of products available in the catalog\",\n"
				+ "                \"produces\": [\n" + "                    \"application/json\"\n"
				+ "                ],\n" + "                \"parameters\": [\n" + "                    {\n"
				+ "                        \"name\": \"Authorization\",\n"
				+ "                        \"in\": \"header\",\n"
				+ "                        \"description\": \"OAuth 2.0 Bearer Token\",\n"
				+ "                        \"required\": true,\n" + "                        \"type\": \"string\",\n"
				+ "                        \"x-example\": \"Bearer Uuafp57IjZ1RmQKruKVw7MXWEQ8h\"\n"
				+ "                    },\n" + "                    {\n"
				+ "                        \"name\": \"Accept\",\n" + "                        \"in\": \"header\",\n"
				+ "                        \"description\": \"The Accept request-header field can be used to specify certain media types which are acceptable for the response.\",\n"
				+ "                        \"required\": true,\n" + "                        \"type\": \"string\",\n"
				+ "                        \"x-example\": \"application/json\"\n" + "                    }\n"
				+ "                ],\n" + "                \"responses\": {\n" + "                    \"200\": {\n"
				+ "                        \"description\": \"Status 200\",\n"
				+ "                        \"schema\": {\n"
				+ "                            \"$ref\": \"#/definitions/ProductResponse\"\n"
				+ "                        },\n" + "                        \"examples\": {\n"
				+ "                            \"application/json\": \"{\\n    \\\"products\\\": [\\n        {\\n            \\\"productId\\\": \\\"PMS-425-02\\\",\\n            \\\"name\\\": \\\"I Love APIs sticker\\\",\\n            \\\"description\\\": \\\"I Love APIs sticker - White\\\",\\n            \\\"uuid\\\": \\\"12541a04-6f17-11e4-8810-67f41b6ea52d\\\"\\n        },\\n        {\\n            \\\"productId\\\": \\\"NL-3600-02\\\",\\n            \\\"name\\\": \\\"Dev Apigee T-Shirt\\\",\\n            \\\"description\\\": \\\"Dev Apigee T-Shirt - Grey\\\",\\n            \\\"uuid\\\": \\\"cf1fdbaa-6f17-11e4-9240-b1091e5819c8\\\"\\n        },\\n        {\\n            \\\"productId\\\": \\\"PMS-1655-04\\\",\\n            \\\"name\\\": \\\"API Sticker\\\",\\n            \\\"description\\\": \\\"API round sticker - White\\\",\\n            \\\"uuid\\\": \\\"124ee9da-6f17-11e4-964a-9be08c22a96d\\\"\\n        },\\n        {\\n            \\\"productId\\\": \\\"NL-6010-01\\\",\\n            \\\"name\\\": \\\"Apigeek T-Shirt\\\",\\n            \\\"description\\\": \\\"Apigeek T-Shirt - Black Vintage\\\",\\n            \\\"uuid\\\": \\\"cf17291a-6f17-11e4-b838-43d0822f332b\\\"\\n        },\\n        {\\n            \\\"productId\\\": \\\"PMS-1655-02\\\",\\n            \\\"name\\\": \\\"Apigeek Sticker\\\",\\n            \\\"description\\\": \\\"Apigee round sticker - White\\\",\\n            \\\"uuid\\\": \\\"1249b9ba-6f17-11e4-b1dd-157c1483794d\\\"\\n        },\\n        {\\n            \\\"productId\\\": \\\"PMS-1655-01\\\",\\n            \\\"name\\\": \\\"Apigeek Sticker\\\",\\n            \\\"description\\\": \\\"Apigee round sticker - Orange\\\",\\n            \\\"uuid\\\": \\\"124748ba-6f17-11e4-b1c3-a1b9eae15c4f\\\"\\n        },\\n        {\\n            \\\"productId\\\": \\\"NL-6010-02\\\",\\n            \\\"name\\\": \\\"API Ninja T-Shirt\\\",\\n            \\\"description\\\": \\\"API Ninja T-Shirt - White\\\",\\n            \\\"uuid\\\": \\\"cf1a0f4a-6f17-11e4-9749-ad4a072c08e3\\\"\\n        },\\n        {\\n            \\\"productId\\\": \\\"NL-3600-03\\\",\\n            \\\"name\\\": \\\"Shaping Digital T-Shirt\\\",\\n            \\\"description\\\": \\\"Shaping Digital T-Shirt - Black\\\",\\n            \\\"uuid\\\": \\\"cf22c1da-6f17-11e4-971c-1395e2dc2bfe\\\"\\n        },\\n        {\\n            \\\"productId\\\": \\\"NL-3600-01\\\",\\n            \\\"name\\\": \\\"API Slayer T-Shirt\\\",\\n            \\\"description\\\": \\\"API Slayer T-Shirt - White\\\",\\n            \\\"uuid\\\": \\\"cf1d1c8a-6f17-11e4-a24d-fb2ae314b099\\\"\\n        },\\n        {\\n            \\\"productId\\\": \\\"NL-3600-04\\\",\\n            \\\"name\\\": \\\"Shaping Digital T-Shirt\\\",\\n            \\\"description\\\": \\\"Shaping Digital T-Shirt - White\\\",\\n            \\\"uuid\\\": \\\"cf2580fa-6f17-11e4-80dd-11ffc22bd4e2\\\"\\n        }\\n    ]\\n}\"\n"
				+ "                        }\n" + "                    },\n" + "                    \"401\": {\n"
				+ "                        \"description\": \"Status 401\",\n"
				+ "                        \"schema\": {\n"
				+ "                            \"$ref\": \"#/definitions/errors\"\n" + "                        },\n"
				+ "                        \"examples\": {\n"
				+ "                            \"application/json\": \"{\\n    \\\"errors\\\": [\\n        {\\n            \\\"code\\\": \\\"Security-1001\\\",\\n            \\\"userMessage\\\": \\\"Invalid Access Token\\\",\\n            \\\"info\\\": \\\"https://developers.myapi.com/errors/#Security-1001\\\"\\n        }\\n    ]\\n}\"\n"
				+ "                        }\n" + "                    }\n" + "                },\n"
				+ "                \"security\": [\n" + "                    {\n"
				+ "                        \"OAuth2\": [\n" + "                            \"catalog:read\",\n"
				+ "                            \"catalog:write\"\n" + "                        ]\n"
				+ "                    }\n" + "                ]\n" + "            },\n"
				+ "            \"x-description\": \"List products\"\n" + "        }\n" + "    },\n"
				+ "    \"securityDefinitions\": {\n" + "        \"OAuth2\": {\n" + "            \"type\": \"oauth2\",\n"
				+ "            \"tokenUrl\": \"https://darshan-eval-prod.apigee.net/auth/v1/token\",\n"
				+ "            \"flow\": \"password\",\n" + "            \"scopes\": {\n"
				+ "                \"catalog:read\": \"Read Permission\",\n"
				+ "                \"catalog:write\": \"Write Permission\"\n" + "            }\n" + "        }\n"
				+ "    },\n" + "    \"definitions\": {\n" + "        \"products\": {\n"
				+ "            \"type\": \"object\",\n" + "            \"required\": [\n"
				+ "                \"name\",\n" + "                \"productId\",\n" + "                \"uuid\"\n"
				+ "            ],\n" + "            \"properties\": {\n" + "                \"productId\": {\n"
				+ "                    \"type\": \"string\",\n"
				+ "                    \"description\": \"The unique ID for the product info\"\n"
				+ "                },\n" + "                \"name\": {\n"
				+ "                    \"type\": \"string\",\n"
				+ "                    \"description\": \"The unique name of the product\"\n" + "                },\n"
				+ "                \"description\": {\n" + "                    \"type\": \"string\",\n"
				+ "                    \"description\": \"Product description\"\n" + "                },\n"
				+ "                \"uuid\": {\n" + "                    \"type\": \"string\",\n"
				+ "                    \"description\": \"UUID of product returned\"\n" + "                }\n"
				+ "            },\n" + "            \"description\": \"List if product catalog information\",\n"
				+ "            \"example\": \"{\\n            \\\"productId\\\": \\\"PMS-425-02\\\",\\n            \\\"name\\\": \\\"I Love APIs sticker\\\",\\n            \\\"description\\\": \\\"I Love APIs sticker - White\\\",\\n            \\\"uuid\\\": \\\"12541a04-6f17-11e4-8810-67f41b6ea52d\\\"\\n        }\"\n"
				+ "        },\n" + "        \"ProductResponse\": {\n" + "            \"type\": \"array\",\n"
				+ "            \"items\": {\n" + "                \"$ref\": \"#/definitions/products\"\n"
				+ "            }\n" + "        },\n" + "        \"errors\": {\n" + "            \"type\": \"object\",\n"
				+ "            \"properties\": {\n" + "                \"errors\": {\n"
				+ "                    \"type\": \"array\",\n" + "                    \"items\": {\n"
				+ "                        \"type\": \"object\",\n" + "                        \"properties\": {\n"
				+ "                            \"userMessage\": {\n"
				+ "                                \"type\": \"string\"\n" + "                            },\n"
				+ "                            \"code\": {\n" + "                                \"type\": \"string\"\n"
				+ "                            },\n" + "                            \"info\": {\n"
				+ "                                \"type\": \"string\"\n" + "                            }\n"
				+ "                        }\n" + "                    }\n" + "                }\n" + "            },\n"
				+ "            \"example\": \"{\\\"errors\\\": [\\n        {\\n            \\\"code\\\": \\\"Security-1001\\\",\\n            \\\"userMessage\\\": \\\"Invalid Access Token\\\",\\n            \\\"info\\\": \\\"https://developers.myapi.com/errors/#Security-1001\\\"\\n        }\\n    ]}\"\n"
				+ "        },\n" + "        \"GetAvailabilityResponse\": {\n" + "            \"type\": \"object\",\n"
				+ "            \"properties\": {\n" + "                \"Products\": {\n"
				+ "                    \"type\": \"object\",\n" + "                    \"properties\": {\n"
				+ "                        \"product\": {\n" + "                            \"type\": \"object\",\n"
				+ "                            \"properties\": {\n"
				+ "                                \"productid\": {\n"
				+ "                                    \"type\": \"string\"\n" + "                                },\n"
				+ "                                \"qty\": {\n"
				+ "                                    \"type\": \"number\"\n" + "                                },\n"
				+ "                                \"storeid\": {\n"
				+ "                                    \"type\": \"number\"\n" + "                                }\n"
				+ "                            }\n" + "                        }\n" + "                    }\n"
				+ "                }\n" + "            },\n"
				+ "            \"example\": \"{\\n    \\\"Products\\\": {\\n        \\\"product\\\": {\\n            \\\"productid\\\": \\\"PMS-425-02\\\",\\n            \\\"qty\\\": 234,\\n            \\\"storeid\\\": 201\\n        }\\n    }\\n}\"\n"
				+ "        }\n" + "    },\n" + "    \"x-servers\": [],\n" + "    \"tags\": [],\n"
				+ "    \"parameters\": {},\n" + "    \"x-mock\": true,\n" + "    \"x-metadata\": {\n"
				+ "        \"metadata\": {\n" + "            \"swaggerName\": \"0af476b3d44447bcbcfb9f0f0459963d\",\n"
				+ "            \"revision\": 1,\n" + "            \"documentation\": [],\n"
				+ "            \"category\": [\n" + "                {\n"
				+ "                    \"name\": \"Unsorted\",\n" + "                    \"paths\": [\n"
				+ "                        \"/productAvailability\",\n" + "                        \"/{productid}\",\n"
				+ "                        \"/list\"\n" + "                    ],\n"
				+ "                    \"definitions\": [\n" + "                        \"products\",\n"
				+ "                        \"ProductResponse\",\n" + "                        \"errors\",\n"
				+ "                        \"GetAvailabilityResponse\"\n" + "                    ]\n"
				+ "                }\n" + "            ]\n" + "        }\n" + "    }\n" + "}";

		SwaggerParser swaggerParser = new SwaggerParser();
		Swagger swagger = swaggerParser.parse(test);

		String swaggerJson = objectMapper.writeValueAsString(swagger);

		log.info("Before deletion " + swaggerJson);
		assertTrue(swaggerJson.contains("responseSchema"));
		String s = SwaggerUtil.removeResponseSchemaTag(swaggerJson);
		log.info("After Deletion " + s);

		assertTrue(!s.contains("responseSchema"));

		// DocumentContext documentContext = JsonPath.parse(swaggerJson);
		//
		// String responseSchemaPath =
		// "$.paths.[*].[*].responses.*.responseSchema";
		// Object responseSchema = documentContext.read(responseSchemaPath);
		//
		// if(responseSchema != null ) {
		// log.info(documentContext.delete(responseSchemaPath).jsonString());
		// }

	}

}
