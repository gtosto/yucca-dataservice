package org.csi.yucca.adminapi.controller.v1;

import org.apache.log4j.Logger;
import org.csi.yucca.adminapi.controller.YuccaController;
import org.csi.yucca.adminapi.exception.BadRequestException;
import org.csi.yucca.adminapi.exception.NotFoundException;
import org.csi.yucca.adminapi.request.DataTypeRequest;
import org.csi.yucca.adminapi.request.DomainRequest;
import org.csi.yucca.adminapi.request.EcosystemRequest;
import org.csi.yucca.adminapi.request.LicenseRequest;
import org.csi.yucca.adminapi.request.MeasureUnitRequest;
import org.csi.yucca.adminapi.request.OrganizationRequest;
import org.csi.yucca.adminapi.request.PhenomenonRequest;
import org.csi.yucca.adminapi.request.SubdomainRequest;
import org.csi.yucca.adminapi.request.TagRequest;
import org.csi.yucca.adminapi.service.ClassificationService;
import org.csi.yucca.adminapi.service.ComponentService;
import org.csi.yucca.adminapi.util.ApiCallable;
import org.csi.yucca.adminapi.util.ServiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("1/backoffice")
public class BackOfficeController extends YuccaController{
	
	private static final Logger logger = Logger.getLogger(BackOfficeController.class);

	@Autowired
	private ClassificationService classificationService;

	@Autowired
	private ComponentService componentService;

	
	/**
	 * 
	 * LOAD MEASURE UNIT
	 * 
	 * @param idMeasureUnit
	 * @return
	 */
	@GetMapping("/measure_units/{idMeasureUnit}")
	public ResponseEntity<Object> loadMeasureUnit(@PathVariable final Integer idMeasureUnit) {
		logger.info("loadMeasureUnit");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return componentService.selectMeasureUnit(idMeasureUnit);
			}
		}, logger);		
	}
	
	/**
	 * 
	 * LOAD PHENOMENON
	 * 
	 * @param idPhenomenon
	 * @return
	 */
	@GetMapping("/phenomenons/{idPhenomenon}")
	public ResponseEntity<Object> loadPhenomenon(@PathVariable final Integer idPhenomenon) {
		logger.info("loadPhenomenon");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return componentService.selectPhenomenon(idPhenomenon);
			}
		}, logger);		
	}
	
	
	/**
	 * 
	 * DELETE PHENOMENON
	 * 
	 * @param idPhenomenon
	 * @return
	 */
	@DeleteMapping("/phenomenons/{idPhenomenon}")
	public ResponseEntity<Object> deletePhenomenon(@PathVariable final Integer idPhenomenon){
		logger.info("deletePhenomenon");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return componentService.deletePhenomenon(idPhenomenon);
			}
		}, logger);		
	}
	
	/**
	 * 
	 * UPDATE PHENOMENON
	 * 
	 * @param phenomenonRequest
	 * @param idPhenomenon
	 * @return
	 */
	@PutMapping("/phenomenons/{idPhenomenon}")
	public ResponseEntity<Object> updatePhenomenon(@RequestBody final PhenomenonRequest phenomenonRequest, @PathVariable final Integer idPhenomenon){
		logger.info("updatePhenomenon");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return componentService.updatePhenomenon(phenomenonRequest, idPhenomenon);
			}
		}, logger);		
	}	
	
	
	/**
	 * 
	 * CREATE SEQUENCE int_yucca.phenomenon_id_phenomenon_seq;
	 * ALTER TABLE int_yucca.yucca_d_phenomenon ALTER COLUMN id_phenomenon SET DEFAULT nextval('int_yucca.phenomenon_id_phenomenon_seq');
	 * ALTER TABLE int_yucca.yucca_d_phenomenon ALTER COLUMN id_phenomenon SET NOT NULL;
	 * ALTER SEQUENCE int_yucca.phenomenon_id_phenomenon_seq OWNED BY int_yucca.yucca_d_phenomenon.id_phenomenon;    -- 8.2 or later
	 * 
	 * ALTER SEQUENCE int_yucca.phenomenon_id_phenomenon_seq RESTART WITH 49;
	 * 
	 * 
	 * INSERT PHENOMENON
	 * 
	 * @param phenomenonRequest
	 * @return
	 */
	@PostMapping("/phenomenons")
	public ResponseEntity<Object> createPhenomenon(@RequestBody final PhenomenonRequest phenomenonRequest){
		logger.info("createPhenomenon");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return componentService.insertPhenomenon(phenomenonRequest);
			}
		}, logger);		
	}

	
	/**
	 * 
	 * DELETE MEASURE UNIT
	 * 
	 * @param idMeasureUnit
	 * @return
	 */
	@DeleteMapping("/measure_units/{idMeasureUnit}")
	public ResponseEntity<Object> deleteMeasureUnit(@PathVariable final Integer idMeasureUnit){
		logger.info("deleteMeasureUnit");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return componentService.deleteMeasureUnit(idMeasureUnit);
			}
		}, logger);		
	}

	
	/**
	 * 
	 * UPDATE MEASURE UNIT
	 * 
	 * @param measureUnitRequest
	 * @param idMeasureUnit
	 * @return
	 */
	@PutMapping("/measure_units/{idMeasureUnit}")
	public ResponseEntity<Object> updateMeasureUnit(@RequestBody final MeasureUnitRequest measureUnitRequest, @PathVariable final Integer idMeasureUnit){
		logger.info("updateMeasureUnit");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return componentService.updateMeasureUnit(measureUnitRequest, idMeasureUnit);
			}
		}, logger);		
	}	
	
	
	/**
	 * 
	 * CREATE SEQUENCE int_yucca.measure_unit_id_measure_unit_seq;
	 * ALTER TABLE int_yucca.yucca_d_measure_unit ALTER COLUMN id_measure_unit SET DEFAULT nextval('int_yucca.measure_unit_id_measure_unit_seq');
	 * ALTER TABLE int_yucca.yucca_d_measure_unit ALTER COLUMN id_measure_unit SET NOT NULL;
	 * ALTER SEQUENCE int_yucca.measure_unit_id_measure_unit_seq OWNED BY int_yucca.yucca_d_measure_unit.id_measure_unit;    -- 8.2 or later
	 * 
	 * ALTER SEQUENCE int_yucca.measure_unit_id_measure_unit_seq RESTART WITH 53;
	 * 
	 * 
	 * @param measureUnitRequest
	 * @return
	 */
	@PostMapping("/measure_units")
	public ResponseEntity<Object> createMeasureUnit(@RequestBody final MeasureUnitRequest measureUnitRequest){
		logger.info("createMeasureUnit");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return componentService.insertMeasureUnit(measureUnitRequest);
			}
		}, logger);		
	}

	
	/**
	 * 
	 * DELETE FDATA TYPE
	 * 
	 * @param idDataType
	 * @return
	 */
	@DeleteMapping("/data_types/{idDataType}")
	public ResponseEntity<Object> deleteDataType(@PathVariable final Integer idDataType){
		logger.info("deleteDataType");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return componentService.deleteDataType(idDataType);
			}
		}, logger);		
	}

	
	/**
	 * UPDATE DATA TYPE
	 * 
	 * @param dataTypeRequest
	 * @param idDataType
	 * @return
	 */
	@PutMapping("/data_types/{idDataType}")
	public ResponseEntity<Object> updateDataType(@RequestBody final DataTypeRequest dataTypeRequest, @PathVariable final Integer idDataType){
		logger.info("updateDataType");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return componentService.updateDataType(dataTypeRequest, idDataType);
			}
		}, logger);		
	}	
	
	/**
	 *  
	 * INSERT DATA TYPE
	 * 
	 * CREATE SEQUENCE int_yucca.data_type_id_data_type_seq;
	 * ALTER TABLE int_yucca.yucca_d_data_type ALTER COLUMN id_data_type SET DEFAULT nextval('int_yucca.data_type_id_data_type_seq');
	 * ALTER TABLE int_yucca.yucca_d_data_type ALTER COLUMN id_data_type SET NOT NULL;
	 * ALTER SEQUENCE int_yucca.data_type_id_data_type_seq OWNED BY int_yucca.yucca_d_data_type.id_data_type;    -- 8.2 or later
	 * 
	 * ALTER SEQUENCE int_yucca.data_type_id_data_type_seq RESTART WITH 15;
	 * 
	 * @param dataTypeRequest
	 * @return
	 */
	@PostMapping("/data_types")
	public ResponseEntity<Object> createDataType(@RequestBody final DataTypeRequest dataTypeRequest){
		logger.info("createDataType");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return componentService.insertDataType(dataTypeRequest);
			}
		}, logger);		
	}

	
	/**
	 * LOAD SUBDOMAIN
	 * 
	 * @param idSubdomain
	 * @return
	 */
	@GetMapping("/subdomains/{idSubdomain}")
	public ResponseEntity<Object> loadSubdomain(@PathVariable final Integer idSubdomain) {
		logger.info("loadSubdomain");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return classificationService.selectSubdomain(idSubdomain);
			}
		}, logger);		
	}

	
	/**
	 * SELECT ORGANIZATION
	 * 
	 * @param idOrganization
	 * @return
	 */
	@GetMapping("/organizations/{idOrganization}")
	public ResponseEntity<Object> loadOrganization(@PathVariable final Integer idOrganization) {
		logger.info("loadOrganization");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return classificationService.selectOrganization(idOrganization);
			}
		}, logger);		
	}
	
	/**
	 * 
	 * LOAD LICENSE
	 * 
	 * @param idLicense
	 * @return
	 */
	@GetMapping("/licenses/{idLicense}")
	public ResponseEntity<Object> loadLicense(@PathVariable final Integer idLicense) {
		logger.info("loadLicense");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return classificationService.selectLicense(idLicense);
			}
		}, logger);		
	}
	
	
	/**
	 * LOAD ECOSYSTEM
	 * 
	 * @param idEcosystem
	 * @return
	 */
	@GetMapping("/ecosystems/{idEcosystem}")
	public ResponseEntity<Object> loadEcosystem(@PathVariable final Integer idEcosystem) {
		logger.info("loadEcosystem");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return classificationService.selectEcosystem(idEcosystem);
			}
		}, logger);		
	}
	
	/**
	 * LOAD TAG
	 * 
	 * @param idTag
	 * @return
	 */
	@GetMapping("/tags/{idTag}")
	public ResponseEntity<Object> loadTag(@PathVariable final Integer idTag) {
		logger.info("loadTag");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return classificationService.selectTag(idTag);
			}
		}, logger);		
	}
	
	/**
	 * 
	 * LOAD DOMAIN
	 * 
	 * @param idDomain
	 * @return
	 */
	@GetMapping("/domains/{idDomain}")
	public ResponseEntity<Object> loadDomain(@PathVariable final Integer idDomain) {
		logger.info("loadDomain");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return classificationService.selectDomain(idDomain);
			}
		}, logger);		
	}
	
	
	/**
	 * 
	 * DELETE SUBDOMAIN
	 * 
	 * @param idSubdomain
	 * @return
	 */
	@DeleteMapping("/subdomains/{idSubdomain}")
	public ResponseEntity<Object> deleteSubdomain(@PathVariable final Integer idSubdomain){
		logger.info("deleteSubdomain");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return classificationService.deleteSubdomain(idSubdomain);
			}
		}, logger);		
	}
	
	/**
	 * 
	 * UPDATE SUBDOMAIN
	 * 
	 * @param subdomainRequest
	 * @param idSubdomain
	 * @return
	 */
	@PutMapping("/subdomains/{idSubdomain}")
	public ResponseEntity<Object> updateSubdomain(@RequestBody final SubdomainRequest subdomainRequest, @PathVariable final Integer idSubdomain ){
		logger.info("updateSubdomain");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return classificationService.updateSubdomain(subdomainRequest, idSubdomain);
			}
		}, logger);		
	}	

	
	/**
	 * CREATE SUBDOMAIN
	 * 
	 * CREATE SEQUENCE int_yucca.subdomain_id_subdomain_seq;
     * ALTER TABLE int_yucca.yucca_d_subdomain ALTER COLUMN id_subdomain SET DEFAULT nextval('int_yucca.subdomain_id_subdomain_seq');
     * ALTER TABLE int_yucca.yucca_d_subdomain ALTER COLUMN id_subdomain SET NOT NULL;
     * ALTER SEQUENCE int_yucca.subdomain_id_subdomain_seq OWNED BY int_yucca.yucca_d_subdomain.id_subdomain;    -- 8.2 or later
     * 
     * ALTER SEQUENCE int_yucca.subdomain_id_subdomain_seq RESTART WITH 200;
     * 
     * AGGIUNGERE CHIAVE UNIVOCA PER subdomaincode NELLA TABELLA SUBDOMAIN.
	 * 
	 * @param subdomainRequest
	 * @return
	 */
	@PostMapping("/subdomains")
	public ResponseEntity<Object> createSubdomain(@RequestBody final SubdomainRequest subdomainRequest ){
		logger.info("createSubdomain");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return classificationService.insertSubdomain(subdomainRequest);
			}
		}, logger);		
	}
	
	@DeleteMapping("/tags/{idTag}")
	public ResponseEntity<Object> deleteTag(@PathVariable final Integer idTag){
		logger.info("deleteTag");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return classificationService.deleteTag(idTag);
			}
		}, logger);		
	}
	
	@PutMapping("/tags/{idTag}")
	public ResponseEntity<Object> updateTag(@RequestBody final TagRequest tagRequest, @PathVariable final Integer idTag){
		logger.info("updateTag");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return classificationService.updateTag(tagRequest, idTag);
			}
		}, logger);		
	}	
	
	
	@PostMapping("/tags")
	public ResponseEntity<Object> createTag(@RequestBody final TagRequest tagRequest ){
		logger.info("createTag");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return classificationService.insertTag(tagRequest);
			}
		}, logger);		
	}

	
	@DeleteMapping("/licenses/{idLicense}")
	public ResponseEntity<Object> deleteLicense(@PathVariable final Integer idLicense){
		logger.info("deleteLicense");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return classificationService.deleteLicense(idLicense);
			}
		}, logger);		
	}

	
	@PutMapping("/licenses/{idLicense}")
	public ResponseEntity<Object> updateLicense(@RequestBody final LicenseRequest licenseRequest, @PathVariable final Integer idLicense ){
		logger.info("updateLicense");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return classificationService.updateLicense(licenseRequest, idLicense);
			}
		}, logger);		
	}	
	
	/**
	 * CREATE SEQUENCE int_yucca.license_id_license_seq;
       ALTER TABLE int_yucca.yucca_d_license ALTER COLUMN id_license SET DEFAULT nextval('int_yucca.license_id_license_seq');
       ALTER TABLE int_yucca.yucca_d_license ALTER COLUMN id_license SET NOT NULL;
       ALTER SEQUENCE int_yucca.license_id_license_seq OWNED BY int_yucca.yucca_d_license.id_license;    -- 8.2 or later
       
       ALTER SEQUENCE int_yucca.license_id_license_seq RESTART WITH 200;
	 * @param licenseRequest
	 * @return
	 */
	@PostMapping("/licenses")
	public ResponseEntity<Object> createLicense(@RequestBody final LicenseRequest licenseRequest ){
		logger.info("createLicense");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return classificationService.insertLicense(licenseRequest);
			}
		}, logger);		
	}

	
	@PutMapping("/organizations/{idOrganization}")
	public ResponseEntity<Object> updateOrganization(@RequestBody final OrganizationRequest organizationRequest, @PathVariable final Integer idOrganization ){
		logger.info("updateOrganization");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return classificationService.updateOrganization(organizationRequest, idOrganization);
			}
		}, logger);		
	}	
	
	@DeleteMapping("/organizations/{idOrganization}")
	public ResponseEntity<Object> deleteOrganization(@PathVariable final Integer idOrganization){
		logger.info("deleteOrganization");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return classificationService.deleteOrganization(idOrganization);
			}
		}, logger);		
	}
	
	@PostMapping("/organizations")
	public ResponseEntity<Object> createOrganization(@RequestBody final OrganizationRequest organizationRequest ){
		logger.info("createOrganization");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return classificationService.insertOrganization(organizationRequest);
			}
		}, logger);		
	}
	
	@DeleteMapping("/ecosystems/{idEcosystem}")
	public ResponseEntity<Object> deleteEcosystem(@PathVariable final Integer idEcosystem){
		logger.info("deleteEcosystem");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return classificationService.deleteEcosystem(idEcosystem);
			}
		}, logger);		
	}
	
	@PostMapping("/ecosystems")
	public ResponseEntity<Object> createEcosystem(@RequestBody final EcosystemRequest ecosystemRequest ){
		logger.info("createEcosystem");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return classificationService.insertEcosystem(ecosystemRequest);
			}
		}, logger);		
	}
	
	
	@PutMapping("/ecosystems/{idEcosystem}")
	public ResponseEntity<Object> updateEcosystem(@RequestBody final EcosystemRequest ecosystemRequest, @PathVariable final Integer idEcosystem ){
		logger.info("updateEcosystem");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return classificationService.updateEcosystem(ecosystemRequest, idEcosystem);
			}
		}, logger);		
	}	
	
	
	@DeleteMapping("/domains/{idDomain}")
	public ResponseEntity<Object> deleteDomain(@PathVariable final Integer idDomain){
		logger.info("deleteDomain");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return classificationService.deleteDomain(idDomain);
			}
		}, logger);		
	}
	
	/**
	 * CREATE SEQUENCE foo_a_seq;
       ALTER TABLE foo ALTER COLUMN a SET DEFAULT nextval('foo_a_seq');
       ALTER TABLE foo ALTER COLUMN a SET NOT NULL;
       ALTER SEQUENCE foo_a_seq OWNED BY foo.a;    -- 8.2 or later
       
       ALTER SEQUENCE int_yucca.yucca_d_domain_seq RESTART WITH 22;
       
	 * @param domainRequest
	 * @return
	 */
	@PostMapping("/domains")
	public ResponseEntity<Object> createDomain(@RequestBody final DomainRequest domainRequest ){
		logger.info("createDomain");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return classificationService.insertDomain(domainRequest);
			}
		}, logger);		
	}

	@PutMapping("/domains/{idDomain}")
	public ResponseEntity<Object> updateDomain(@RequestBody final DomainRequest domainRequest, @PathVariable final Integer idDomain ){
		logger.info("updateDomain");
		
		return run(new ApiCallable() {
			public ServiceResponse call() throws BadRequestException, NotFoundException, Exception {
				return classificationService.updateDomain(domainRequest, idDomain);
			}
		}, logger);		
	}	
	
}




