package aohara.tinkertime.models.context;

import java.net.URL;
import java.nio.file.Path;

import aohara.tinkertime.controllers.ModManager.CannotAddModException;
import aohara.tinkertime.models.pages.CurseModPage;

public class NewModPageContext extends PageDownloadContext {
	
	public NewModPageContext(URL pageUrl, Path tempPath) {
		super(pageUrl, tempPath);
	}
	
	@Override
	public String toString() {
		return getSource().toString();
	}
	
	public CurseModPage getPage() throws CannotAddModException{
		return CurseModPage.createFromFile(getDest(), getSource());
	}
	
	public boolean isUpdateAvailable(){
		return true;
	}
	
	public boolean updateAfter(){
		return true;
	}

}
