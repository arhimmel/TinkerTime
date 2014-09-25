package aohara.tinkertime.views;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import aohara.common.Util;
import aohara.common.selectorPanel.ListListener;
import aohara.tinkertime.Config;
import aohara.tinkertime.TinkerTime;
import aohara.tinkertime.controllers.ModManager;
import aohara.tinkertime.controllers.ModManager.CannotDisableModException;
import aohara.tinkertime.controllers.ModManager.ModAlreadyDisabledException;
import aohara.tinkertime.controllers.ModManager.ModAlreadyEnabledException;
import aohara.tinkertime.controllers.ModManager.ModNotDownloadedException;
import aohara.tinkertime.controllers.ModManager.ModUpdateFailedException;
import aohara.tinkertime.controllers.crawlers.Constants;
import aohara.tinkertime.controllers.crawlers.CrawlerFactory.UnsupportedHostException;
import aohara.tinkertime.controllers.fileUpdater.ModuleManagerUpdateController;
import aohara.tinkertime.controllers.fileUpdater.TinkerTimeUpdateController;
import aohara.tinkertime.models.Mod;

/**
 * JMenuBar for Tinker Time.
 * 
 * Also includes all of the Actions that are performed when a Menu Item is activated.
 * 
 * Includes the following menus:
 * - File: Select KSP Installation Dir and exit the application
 * - Mod: Actions on the selected mod
 * - Update: Actions for updating mods or other files
 * - Help: Help, Contact Information, and About
 * 
 * @author Andrew O'Hara
 */
@SuppressWarnings("serial")
public class TinkerMenuBar extends JMenuBar implements ListListener<Mod>{
	
	private final ModManager mm;
	private Mod selectedMod;
	private final JPopupMenu popupMenu;
	
	public TinkerMenuBar(ModManager mm){
		this.mm = mm;
		
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(new JMenuItem(new UpdatePathsAction()));
		fileMenu.add(new JMenuItem(new ExitAction()));
		add(fileMenu);
		
		JMenu modMenu = new JMenu("Mod");
		modMenu.add(new JMenuItem(new AddModAction()));
		modMenu.add(new JMenuItem(new EnableDisableModAction()));
		modMenu.add(new JMenuItem(new DeleteModAction()));
		add(modMenu);
		
		JMenu updateMenu = new JMenu("Update");
		updateMenu.add(new JMenuItem(new UpdateModAction()));
		updateMenu.add(new JMenuItem(new UpdateAllAction()));
		updateMenu.add(new JMenuItem(new CheckforUpdatesAction()));
		updateMenu.add(new JMenuItem(new UpdateModuleManagerAction()));
		updateMenu.add(new JMenuItem(new UpdateTinkerTimeAction()));
		add(updateMenu);
		
		JMenu helpMenu = new JMenu("Help");
		helpMenu.add(new JMenuItem(new AboutAction()));
		helpMenu.add(new JMenuItem(new HelpAction()));
		helpMenu.add(new JMenuItem(new ContactAction()));
		add(helpMenu);
		
		popupMenu = new JPopupMenu();
		popupMenu.add(new EnableDisableModAction());
		popupMenu.add(new DeleteModAction());
	}
	
	private void errorMessage(Exception ex){
		ex.printStackTrace();
		errorMessage(ex.toString());
	}
	
	private void errorMessage(String message){
		JOptionPane.showMessageDialog(
			getParent(), message, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	// -- Listeners --------------------------------------------------

		@Override
		public void elementClicked(Mod element, int numTimes) {
			// Do Nothing
		}

		@Override
		public void elementSelected(Mod element) {
			selectedMod = element;
		}
		
		@Override
		public void elementRightClicked(MouseEvent evt, Mod element) throws Exception {
			popupMenu.show((Component) evt.getSource(), evt.getX(), evt.getY());
		}
		
	// -- Actions ---------------------------------------------------
	
	private class AddModAction extends AbstractAction {
		
		private AddModAction(){
			super("Add Mod");
		}

		@Override
		public void actionPerformed(ActionEvent evt) {
			// Get URL from user
			String urlString = JOptionPane.showInputDialog(
				getParent(),
				"Please enter the URL of the mod you would like to"
				+ " add.\ne.g. http://www.curse.com/ksp-mods/kerbal/220221-mechjeb\n\n"
				+ "Supported Hosts are " + Constants.ACCEPTED_MOD_HOSTS,
				"Enter Mod Page URL",
				JOptionPane.QUESTION_MESSAGE
			);
			
			// Try to add Mod
			try {
				mm.downloadMod(new URL(urlString));
			} catch (UnsupportedHostException | ModUpdateFailedException | MalformedURLException ex) {
				errorMessage(ex);
			}
		}
	}
	
	private class DeleteModAction extends AbstractAction {
		
		private DeleteModAction(){
			super("Delete");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (selectedMod != null){
				try {
					if (JOptionPane.showConfirmDialog(
						getParent(),
						"Are you sure you want to delete "
						+ selectedMod.getName() + "?",
						"Delete?",
						JOptionPane.YES_NO_OPTION
					) == JOptionPane.YES_OPTION){
						mm.deleteMod(selectedMod);
					}
				} catch (CannotDisableModException | IOException e1) {
					errorMessage(selectedMod.getName() + " could not be disabled.");
				}
			}
		}
	}
	
	private class UpdateModAction extends AbstractAction {
		
		private UpdateModAction(){
			this("Update Mod");
		}
		
		protected UpdateModAction(String string){
			super(string);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (selectedMod != null){
				try {
					mm.updateMod(selectedMod);
				} catch (ModUpdateFailedException e1) {
					errorMessage(e1);
				}
			}
		}
	}
	
	private class UpdateAllAction extends UpdateModAction {
		
		private UpdateAllAction() {
			super("Update All");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				mm.updateMods();
			} catch (ModUpdateFailedException e1) {
				errorMessage("One or more mods failed to update");
			}
		}
	}
	
	private class CheckforUpdatesAction extends AbstractAction {
		
		private CheckforUpdatesAction(){
			super("Check for Updates");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				mm.checkForModUpdates();
			} catch (Exception e1) {
				e1.printStackTrace();
				errorMessage("Error checking for updates.");
			}
		}
	}
	
	private class EnableDisableModAction extends AbstractAction {
		
		private EnableDisableModAction(){
			super("Enable/Disable");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (selectedMod != null && selectedMod.isEnabled()){
				try {
					mm.disableMod(selectedMod);
				} catch (ModAlreadyDisabledException | IOException e1) {
					errorMessage(e1);
				}
			} else if (selectedMod != null){
				try {
					mm.enableMod(selectedMod);
				} catch (ModAlreadyEnabledException | ModNotDownloadedException | IOException e1) {
					errorMessage(e1);
				}
			}
		}
	}
	
	private class UpdatePathsAction extends AbstractAction {
		
		private UpdatePathsAction(){
			super("Select KSP Installation");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Config.updateConfig(false);
		}
	}
	
	private class ExitAction extends AbstractAction {
		
		private ExitAction(){
			super("Exit");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			System.exit(0);
		}
	}
	
	private class HelpAction extends AbstractAction {
		
		private HelpAction(){
			super("Help");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				Util.goToHyperlink(new URL("https://github.com/oharaandrew314/TinkerTime/blob/master/README.md"));
			} catch (IOException e1) {
				errorMessage("Error opening help");
			}
		}
	}
	
	private class AboutAction extends AbstractAction {
		
		private AboutAction(){
			super("About");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String aboutText = String.format(
				"<html>%s v%s - by %s\n",
				TinkerTime.NAME,
				TinkerTime.VERSION,
				TinkerTime.AUTHOR
			);
			
			String licenseText = (
				"This work is licensed under the Creative Commons \n"
				+ "Attribution-ShareAlike 4.0 International License. \n"
			);
			
			try {
				Object[] message = {
					aboutText,
					"\n",
					licenseText,
					new UrlPanel("View a copy of this license", new URL("http://creativecommons.org/licenses/by-sa/4.0/")).getComponent()
				};
				JOptionPane.showMessageDialog(
						getParent(),
						message,
						"About " + TinkerTime.NAME,
						JOptionPane.INFORMATION_MESSAGE
					);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	private class ContactAction extends AbstractAction {
		
		private ContactAction(){
			super("Contact Me");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				Util.goToHyperlink(new URL("http://tinkertime.uservoice.com"));
			} catch (IOException e1) {
				errorMessage(e1.getMessage());
			}
		}
	}
	
	private class UpdateModuleManagerAction extends AbstractAction {
		
		private UpdateModuleManagerAction(){
			super("Update Module Manager");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				new ModuleManagerUpdateController(mm, new Config()).showDialog();
			} catch (UnsupportedHostException e1) {
				errorMessage(e1);
			}
		}
	}
	
	private class UpdateTinkerTimeAction extends AbstractAction {
		
		private UpdateTinkerTimeAction(){
			super("Update Tinker Time");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				new TinkerTimeUpdateController(mm).showDialog();
			} catch (UnsupportedHostException e1) {
				errorMessage(e1);
			}
		}
	}
}
