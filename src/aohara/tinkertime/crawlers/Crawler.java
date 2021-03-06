package aohara.tinkertime.crawlers;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

import aohara.tinkertime.crawlers.pageLoaders.PageLoader;

/**
 * Abstract Base Class for Creating Web Crawlers to gather file information.
 * This Crawler is meant to be controlled by a Workflow since these operations
 * are blocking, and may be long-running. 
 * 
 * @author Andrew O'Hara
 *
 * @param <T> Type of Page that is to be returned by getPage
 */
public abstract class Crawler<T> {
	
	private final PageLoader<T> pageLoader;
	private final URL url;
	
	public Crawler(URL url, PageLoader<T> pageLoader){
		this.url = url;
		this.pageLoader = pageLoader;
	}
	
	public T getPage(URL url) throws IOException {
		return pageLoader.getPage(this, url);
	}
	
	public abstract String generateId();
	public abstract URL getDownloadLink() throws IOException;
	public abstract String getNewestFileName() throws IOException;
	protected abstract Date getUpdatedOn() throws IOException;
	
	public boolean isUpdateAvailable(Date lastUpdated, String lastFileName) {
		try {
			if (lastUpdated != null && getUpdatedOn() != null){
				return getUpdatedOn().compareTo(lastUpdated) > 0;
			} else if (lastFileName != null){
				return !getNewestFileName().equals(lastFileName);
			}
			return true;
		} catch (IOException e){
			e.printStackTrace();
			return false;
		}
	}
	
	public URL getPageUrl(){
		return url;
	}
	
	public URL getApiUrl(){
		return url;
	}
}
