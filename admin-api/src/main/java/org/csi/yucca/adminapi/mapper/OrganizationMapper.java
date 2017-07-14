package org.csi.yucca.adminapi.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.csi.yucca.adminapi.model.Organization;
import org.csi.yucca.adminapi.util.Constants;

/**
 * 
 * @author gianfranco.stolfa
 *
 */
public interface OrganizationMapper {
	
	String ORGANIZATION_TABLE = Constants.SCHEMA_DB + ".yucca_organization";
	String R_ECOSYSTEM_ORGANIZATION_TABLE = Constants.SCHEMA_DB + ".yucca_r_ecosystem_organization";
	
	public static final String SELECT = 
			
			
			" SELECT ORG.id_organization, organizationcode, description " +
			" FROM " + ORGANIZATION_TABLE + " ORG, " + R_ECOSYSTEM_ORGANIZATION_TABLE + " R_ORG_ECO " +
			" WHERE R_ORG_ECO.id_ecosystem = #{ecosystemCode} AND " +
			" ORG.id_organization = R_ORG_ECO.id_organization " +

			"<if test=\"sortList != null\">" +
				" ORDER BY " +
			
				" <foreach item=\"propName\" separator=\",\" index=\"index\" collection=\"sortList\">" +
				
				"<if test=\"propName == 'idOrganization-'\">" +
			        " id_organization desc" +
		        "</if>" +
		        "<if test=\"propName == 'idOrganization'\">" +
		            " id_organization" +
	            "</if>" +			
				
				"<if test=\"propName == 'organizationcode-'\">" +
		           " organizationcode desc" +
	            "</if>" +
	            "<if test=\"propName == 'organizationcode'\">" +
	               " organizationcode" +
	            "</if>" +			
				
				"<if test=\"propName == 'description-'\">" +
		           " description desc" +
	            "</if>" +
	            "<if test=\"propName == 'description'\">" +
	               " description" +
	            "</if>" +			
	            "</foreach>" +
            "</if>";
	

	/*************************************************************************
	 * 
	 * 					select ORGANIZATIONS
	 * 
	 * ***********************************************************************/
	@Results({
        @Result(property = "idOrganization", column = "id_organization"),
        @Result(property = "organizationcode", column = "organizationcode"),
        @Result(property = "description", column = "description")
      })
	@Select({"<script>",
				SELECT,
             "</script>"}) 
	List<Organization> selectOrganization(@Param("ecosystemCode") int ecosystemCode, @Param("sortList") List<String> sortList);
	
}