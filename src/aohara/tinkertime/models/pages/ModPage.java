package aohara.tinkertime.models.pages;

import java.net.URL;

import org.jsoup.nodes.Element;

public abstract class ModPage extends HtmlPage implements FilePage {

	public ModPage(URL pageUrl, Element element) {
		super(pageUrl, element);
	}
	
	public abstract String getName();
	public abstract String getCreator();
	public abstract URL getImageUrl();
	
	@Override
	public String toString(){
		return getName();
	}

}
