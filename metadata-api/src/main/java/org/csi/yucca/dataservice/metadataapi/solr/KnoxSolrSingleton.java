package org.csi.yucca.dataservice.metadataapi.solr;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpClientUtil;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.params.ModifiableSolrParams;

import org.csi.yucca.dataservice.metadataapi.util.Config;


public class KnoxSolrSingleton {
	private SolrClient server;
	
	private KnoxSolrSingleton() {
		
		HttpClientBuilder clientBuilder = HttpClientBuilder.create();
		if (Config.getInstance().getSolrUsername()!=null)
		{
			CredentialsProvider provider = new BasicCredentialsProvider();
			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(
					Config.getInstance().getSolrUsername(), Config.getInstance().getSolrPassword());
			clientBuilder.setDefaultCredentialsProvider(provider);
		}
		clientBuilder.setMaxConnTotal(128);
				
		server = new HttpSolrClient(Config.getInstance().getSearchEngineBaseUrl(),
				clientBuilder.build());
		
		
    }
	
		  /**
		   * SingletonHolder is loaded on the first execution of Singleton.getInstance()() 
		   * or the first access to SingletonHolder.INSTANCE, not before.
		   */
	  static class SingletonHolder { 
	    static final KnoxSolrSingleton INSTANCE = new KnoxSolrSingleton();
	  }

	  public static SolrClient getServer() {
		    return SingletonHolder.INSTANCE.server;
		  }
}