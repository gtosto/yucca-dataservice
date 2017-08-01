package org.csi.yucca.adminapi.controller.v1;

import org.apache.log4j.Logger;
import org.csi.yucca.adminapi.controller.YuccaController;
import org.csi.yucca.adminapi.exception.BadRequestException;
import org.csi.yucca.adminapi.exception.NotFoundException;
import org.csi.yucca.adminapi.request.DomainRequest;
import org.csi.yucca.adminapi.request.EcosystemRequest;
import org.csi.yucca.adminapi.request.LicenseRequest;
import org.csi.yucca.adminapi.request.OrganizationRequest;
import org.csi.yucca.adminapi.request.SubdomainRequest;
import org.csi.yucca.adminapi.request.TagRequest;
import org.csi.yucca.adminapi.service.ClassificationService;
import org.csi.yucca.adminapi.util.ApiCallable;
import org.csi.yucca.adminapi.util.ServiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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




