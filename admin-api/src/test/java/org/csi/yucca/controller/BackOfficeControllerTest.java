package org.csi.yucca.controller;

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.util.Iterator;

import org.hamcrest.Matchers;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;


/**
 * https://github.com/csipiemonte/yucca-dataservice/blob/oData-multiapi/insertdata-api/src/test/java/org/csi/yucca/dataservice/insertdataapi/test/unit/HttpDatasetInsertTest.java

 * @author gianfranco.stolfa
 *
 */
public class BackOfficeControllerTest extends TestBase{
	
	@BeforeClass
	public void setUpSecretObject() throws IOException {
		super.setUpSecretObject("/testSecret.json");
	}

	@DataProvider(name="json")
	public Iterator<Object[]> getFromJson(){  
		return super.getFromJson(
			     "/BackOfficeController_licenses_dataIn.json",
			     "/BackOfficeController_ecosystem_dataIn.json",
			     "/BackOfficeController_domain_dataIn.json",
			     "/BackOfficeController_organization_dataIn.json",
			     "/BackOfficeController_tag_dataIn.json"
			     );
	}	
	
	/**
	 * POST
	 * http://localhost:8080/adminapi/1/backoffice/domains
	 * 
	 * @param dato
	 * @throws JSONException
	 * @throws InterruptedException
	 */
	@Test(dataProvider = "json")
	public void backOfficeTestCrud(JSONObject dato) throws JSONException, InterruptedException {
		
		// define ecosystem url:
		StringBuilder ecosystemUrlBuilder = new StringBuilder();
		ecosystemUrlBuilder.append(dato.get("adminapi.url")).append("/")
		.append(dato.get("adminapi.version")).append("/").append("backoffice").append("/").append("ecosystems");
		Integer idEcosystem = postEchosystem(ecosystemUrlBuilder.toString(), dato); 
		
		// define url
		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(dato.get("adminapi.url")).append("/")
		.append(dato.get("adminapi.version")).append("/").append(dato.get("adminapi.apicode")).append("/").append(dato.getString("adminapi.entityset"));
		
		// test post
		Integer idDomain = testPost(urlBuilder.toString(), dato, idEcosystem);
		
		// test put and delete
		if (idDomain != null) {
			urlBuilder.append("/").append(idDomain);
			testPut(urlBuilder.toString(), dato);
			testDelete(urlBuilder.toString(), dato);
		}
		
		// delete ecosystem
		ecosystemUrlBuilder.append("/").append(idEcosystem);
		deleteEcosystem(ecosystemUrlBuilder.toString());
	}
	
	private Integer postEchosystem(String url, JSONObject dato){
		RequestSpecification requestSpecification = given().body(dato.get("adminapi.ecosystem.message")).contentType(ContentType.JSON);
		Response response = requestSpecification.when().post(url);
		Integer idEcosystem =  response.then().extract().path(dato.getString("adminapi.id-ecosystem-generated"));
		return idEcosystem;
	}
	
	private void deleteEcosystem(String url){
		given().when().contentType(ContentType.JSON).delete(url);
	}	
	
	
	
	
	private Integer testPost(String url, JSONObject dato, Integer idEcosystem){
		
		String jsonString = (String)dato.get("adminapi.message");
		if(!dato.getString("test-name").contains("ecosystem")){
			jsonString = "{\"idEcosystem\":"+idEcosystem+"," + jsonString.substring(1);	
		}
		
		RequestSpecification requestSpecification = given().body(jsonString).contentType(ContentType.JSON);
		
		Response response = requestSpecification.when().post(url);
		ValidatableResponse validatableResponse  = response.then().statusCode(dato.getInt("expected.httpStatus.response"));
		Integer idGenerated =  validatableResponse.extract().path(dato.getString("adminapi.id-generated"));
		// check dell'eventuale messaggio di errore:
		if(!dato.optString("expected.errorName").isEmpty()){
			validatableResponse.assertThat().body("errorName", Matchers.containsString(dato.getString("expected.errorName")));
		}
		return idGenerated;

	}

	private void testDelete(String url, JSONObject dato){
		given().when().contentType(ContentType.JSON).delete(url).then().statusCode(dato.getInt("expected.httpStatus.delete-response"));
	}
	
	private void testPut(String url, JSONObject dato){
		RequestSpecification updateRequestSpecification = given().body(dato.get("adminapi.message.update")).contentType(ContentType.JSON);
		Response updateResponse = updateRequestSpecification.when().put(url);
		ValidatableResponse updateValidatableResponse  = updateResponse.then().statusCode(dato.getInt("expected.httpStatus.update-response"));
		// check dell'eventuale messaggio di errore:
		if(!dato.optString("expected.update-errorName").isEmpty()){
//			updateValidatableResponse.assertThat().body("errorName", Matchers.contains(dato.get("expected.update-errorName")));
			updateValidatableResponse.assertThat().body("errorName", Matchers.containsString(dato.getString("expected.update-errorName")));
		}
		
	}
	
	
	
	
}