package it.csi.smartdata.dataapi.servlet;

import it.csi.smartdata.dataapi.constants.SDPDataApiConfig;
import it.csi.smartdata.dataapi.constants.SDPDataApiConstants;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;


public class SDPOdataFilter implements Filter{

	static Logger log = Logger.getLogger(SDPOdataFilter.class.getPackage().getName());


	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws ServletException, IOException {
		log.info("[SDPOdataFilter::doFilter] BEGIN");
		try { 
			HttpServletRequest request = (HttpServletRequest) req;
			String requestURI = request.getRequestURI();
			log.debug("[SDPOdataFilter::doFilter] requestURI="+requestURI);

			String webFilterPattern=SDPDataApiConstants.SDP_WEB_FILTER_PATTERN;
			String webServletUrl=SDPDataApiConstants.SDP_WEB_SERVLET_URL;

			try { 
				webFilterPattern=SDPDataApiConfig.getInstance().getWebFilterPattern();
			} catch (Exception e) {

			}
			try { 
				webServletUrl=SDPDataApiConfig.getInstance().getWebServletUrl();
			} catch (Exception e) {

			}
			log.debug("[SDPOdataFilter::doFilter] webFilterPattern="+webFilterPattern);
			log.debug("[SDPOdataFilter::doFilter] webServletUrl="+webServletUrl);

			if (requestURI.indexOf(webFilterPattern)!=-1) {
				log.info("[SDPOdataFilter::doFilter] FILTERING");


				String prima=requestURI.substring(0,requestURI.indexOf(webFilterPattern));
				String dopo=requestURI.substring(requestURI.indexOf("/",requestURI.indexOf(webFilterPattern)+webFilterPattern.length()+1)+1);


				String codiceApi=requestURI.substring(prima.length()+webFilterPattern.length());


				if (dopo.length()>0) codiceApi=codiceApi.substring(0, codiceApi.indexOf(dopo)-1);
				else codiceApi=codiceApi.substring(0, codiceApi.length()-1);
				String newURI=webServletUrl+dopo+"?codiceApi="+codiceApi;


				if (request.getQueryString()!=null && request.getQueryString().length()>0) newURI=newURI+"&"+request.getQueryString();

				log.info("[SDPOdataFilter::doFilter] codiceApi="+codiceApi);
				log.info("[SDPOdataFilter::doFilter] newURI="+newURI);
				req.getRequestDispatcher(newURI).forward(req, res);
			} else {
				log.info("[SDPOdataFilter::doFilter] NO FILTER");
				
				chain.doFilter(req, res);
			}
		} catch (Exception e) {
			log.error("[SDPOdataFilter::doFilter] " +e);
			
			if (e instanceof ServletException) throw (ServletException)e;
			if (e instanceof IOException) throw (IOException)e;
			throw new ServletException(e.getMessage());
			
		} finally {
			log.info("[SDPOdataFilter::doFilter] END");

		}
	}

	@Override
	public void destroy() {
		//
	}
	@Override
	public void init(FilterConfig config) throws ServletException {
		//
	}

}