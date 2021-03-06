package aohara.tinkertime.views;

import java.awt.Component;
import java.util.LinkedList;

import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import thirdParty.CompoundIcon;
import aohara.tinkertime.Config;
import aohara.tinkertime.models.Mod;

/**
 * Custom ListCellRenderer for a Mod to be displayed on a JList.
 * 
 * Displays the Mod name as well as all status icons to the left of it.
 *  
 * @author Andrew O'Hara
 */
public class ModListCellRenderer implements ListCellRenderer<Mod> {
	
	private final ImageIcon checkIcon, xIcon, errorIcon, updateIcon;
	private final DefaultListCellRenderer def = new DefaultListCellRenderer();
	
	public ModListCellRenderer(){
		checkIcon = loadImage("check.png");
		xIcon = loadImage("x.png");
		errorIcon = loadImage("exclamation.png");
		updateIcon = loadImage("upArrow.gif");
	}
	
	private ImageIcon loadImage(String name){
		return new ImageIcon(getClass().getClassLoader().getResource("icon/" + name));
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends Mod> list,
			Mod mod, int index, boolean isSelected, boolean cellHasFocus) {

		// Compile list of icons
		LinkedList<ImageIcon> icons = new LinkedList<>();
		if (mod.isDownloaded(new Config())){
			icons.add(mod.isEnabled() ? checkIcon : xIcon);
		} else {
			icons.add(errorIcon);
		}
		
		if (mod.isUpdateAvailable()){
			icons.add(updateIcon);
		}
		
		// Create cell label
		String text = mod.getName();
		if (mod.getSupportedVersion() != null){
			text = String.format("[%s] %s", mod.getSupportedVersion(), text);
		}
		JLabel label = (JLabel) def.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
		label.setIcon(new CompoundIcon(icons.toArray(new Icon[0])));
		return label;
	}


}
