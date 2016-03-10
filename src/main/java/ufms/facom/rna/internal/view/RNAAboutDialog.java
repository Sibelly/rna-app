package ufms.facom.rna.internal.view;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLEditorKit;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.util.swing.OpenBrowser;

import ufms.facom.rna.internal.util.RNAResources;
import ufms.facom.rna.internal.util.RNAUtil;

public class RNAAboutDialog extends JDialog{

	private static final long serialVersionUID = 6701868758424597496L;

	private final OpenBrowser openBrowser;
	private final String version;
	private final String buildDate;

	/** Main panel for dialog box */
	private JEditorPane mainContainer;
	private JPanel buttonPanel;
	
	public RNAAboutDialog(	final CySwingApplication swingApplication,
							final OpenBrowser openBrowser,
							final RNAUtil rnaUtil) {
		super(swingApplication.getJFrame(), "About RNA", false);
		this.openBrowser = openBrowser;
		version = rnaUtil.getProperty("project.version");
		buildDate = rnaUtil.getProperty("buildDate");
		
		setResizable(false);
		getContentPane().add(getMainContainer(), BorderLayout.CENTER);
		getContentPane().add(getButtonPanel(), BorderLayout.SOUTH);
		pack();
	}

	private JEditorPane getMainContainer() {
		if (mainContainer == null) {
			mainContainer = new JEditorPane();
			mainContainer.setMargin(new Insets(10, 10, 10, 10));
			mainContainer.setEditable(false);
			mainContainer.setEditorKit(new HTMLEditorKit());
			mainContainer.addHyperlinkListener(new HyperlinkAction());

			URL logoURL = RNAResources.getUrl(RNAResources.ImageName.LOGO_SIMPLE);
			String logoCode = "";

			if (logoURL != null) {
				logoCode = "<center><img src='" + logoURL + "'></center>";
			}

			String text = "<html><body>" +
						  logoCode +
						  "<P align=center><b>RNA - Regulatory Network Analyzer</b><BR>" + 
						  "<b>v" + version + " (" + buildDate + ")</b><BR>" +
						  "A Cytoscape App<BR><BR>" +

						  "Version " + version + " by <a href='http://facom.sites.ufms.br/'>FACOM</a>, University of Mato Grosso do Sul<BR>" + "<BR><BR>" + "</P></body></html>";

			mainContainer.setText(text);
			
			mainContainer.addKeyListener(new KeyListener() {
				
				@Override
				public void keyTyped(KeyEvent e) {
				}
				
				@Override
				public void keyReleased(KeyEvent e) {
					switch (e.getKeyCode()) {
						case KeyEvent.VK_ENTER:
						case KeyEvent.VK_ESCAPE:
							dispose();
							break;
					}
				}
				
				@Override
				public void keyPressed(KeyEvent e) {
				}
			});
		}

		return mainContainer;
	}

	private JPanel getButtonPanel() {
		if (buttonPanel == null) {
			buttonPanel = new JPanel();
			
			JButton button = new JButton("Close");
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
			button.setAlignmentX(CENTER_ALIGNMENT);
			
			buttonPanel.add(button);
		}
		
		return buttonPanel;
	}
	
	private class HyperlinkAction implements HyperlinkListener {
		@Override
		public void hyperlinkUpdate(HyperlinkEvent event) {
			if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				openBrowser.openURL(event.getURL().toString());
			}
		}
	}
	
}
