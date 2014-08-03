package aohara.tinkertime.controllers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import aohara.common.Listenable;
import aohara.common.workflows.ConflictResolver;
import aohara.common.workflows.ProgressPanel;
import aohara.common.workflows.Workflow;
import aohara.tinkertime.Config;
import aohara.tinkertime.models.Mod;
import aohara.tinkertime.workflows.CheckForUpdateWorkflow;
import aohara.tinkertime.workflows.DeleteModWorkflow;
import aohara.tinkertime.workflows.DisableModWorkflow;
import aohara.tinkertime.workflows.EnableModWorkflow;
import aohara.tinkertime.workflows.UpdateModWorkflow;

public class ModManager extends Listenable<ModUpdateListener> {
	
	public static final int NUM_CONCURRENT_DOWNLOADS = 4;
	
	private final Executor executor = Executors.newFixedThreadPool(NUM_CONCURRENT_DOWNLOADS);
	private final Config config;
	private final ModStateManager sm;
	private final ProgressPanel progressPanel;
	private final ConflictResolver cr;
	
	public ModManager(
			ModStateManager sm, Config config, ProgressPanel progressPanel,
			ConflictResolver cr){
		this.sm = sm;
		this.config = config;
		this.progressPanel = progressPanel;
		this.cr = cr;
		
		this.addListener(sm);
	}
	
	// -- Listeners -----------------------
	
	public void notifyModUpdated(Mod mod, boolean deleted){
		for (ModUpdateListener l : getListeners()){
			l.modUpdated(mod, deleted);
		}
	}
	
	// -- Accessors ------------------------
	
	public static boolean isDownloaded(Mod mod, Config config){
		return config.getModZipPath(mod).toFile().exists();
	}
	
	public boolean isDownloaded(Mod mod){
		return isDownloaded(mod, config);
	}
	
	// -- Modifiers ---------------------------------
	
	private void submitWorkflow(Workflow workflow){
		workflow.addListener(progressPanel);
		executor.execute(workflow);
	}
	
	public void addNewMod(String url) throws CannotAddModException {
		try {
			submitWorkflow(new UpdateModWorkflow(new URL(url), config, sm));
		} catch (MalformedURLException e) {
			throw new CannotAddModException();
		}
	}
	
	public void updateMod(Mod mod) throws ModUpdateFailedException {
		// TODO Merge with addNewMod
		try {
			addNewMod(mod.getPageUrl().toString());
		} catch (CannotAddModException e) {
			throw new ModUpdateFailedException();
		}
	}
	
	public void updateMods() throws ModUpdateFailedException{
		boolean error = false;
		for (Mod mod : sm.getMods()){
			try {
				updateMod(mod);
			} catch (ModUpdateFailedException e) {
				error = true;
			}
		}
		
		if (error){
			throw new ModUpdateFailedException();
		}
	}
	
	public void enableMod(Mod mod)
		throws ModAlreadyEnabledException, ModNotDownloadedException,
		CannotEnableModException, CannotDisableModException
	{
		if (mod.isEnabled()){
			throw new ModAlreadyEnabledException();
		} else if (!isDownloaded(mod)){
			throw new ModNotDownloadedException();
		}
		
		submitWorkflow(new EnableModWorkflow(mod, config, sm, cr));
	}
	
	public void disableMod(Mod mod)
			throws ModAlreadyDisabledException, CannotDisableModException {
		if (!mod.isEnabled()){
			throw new ModAlreadyDisabledException();
		}
		
		submitWorkflow(new DisableModWorkflow(mod, config, sm));
	}
	
	public void deleteMod(Mod mod) throws CannotDisableModException {
		submitWorkflow(new DeleteModWorkflow(mod, config, sm));
	}
	
	public void checkForUpdates() throws ModUpdateFailedException {	
		for (Mod mod : sm.getMods()){
			submitWorkflow(new CheckForUpdateWorkflow(mod, sm));
		}
	}
	
	// -- Exceptions ------------------------------------------------------
	
	@SuppressWarnings("serial")
	public static class CannotAddModException extends Exception {}
	@SuppressWarnings("serial")
	public static class ModAlreadyEnabledException extends Exception {}
	@SuppressWarnings("serial")
	public static class ModAlreadyDisabledException extends Exception {}
	@SuppressWarnings("serial")
	public static class ModNotDownloadedException extends Exception {}
	@SuppressWarnings("serial")
	public static class CannotDisableModException extends Exception {}
	@SuppressWarnings("serial")
	public static class CannotEnableModException extends Exception {}
	@SuppressWarnings("serial")
	public static class ModUpdateFailedException extends Exception {}
	@SuppressWarnings("serial")
	public static class ModAlreadyUpToDateException extends Exception {}
}
