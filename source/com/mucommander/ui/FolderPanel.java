
package com.mucommander.ui;

import com.mucommander.file.*;
import com.mucommander.ui.table.FileTable;
import com.mucommander.ui.table.FileTableCellRenderer;
import com.mucommander.ui.comp.FocusRequester;
import com.mucommander.conf.*;
import com.mucommander.text.Translator;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.border.Border;

import java.awt.*;
import java.awt.event.*;

import java.io.*;

import java.util.Vector;


public class FolderPanel extends JPanel implements ActionListener, PopupMenuListener, KeyListener, ConfigurationListener {
	private MainFrame mainFrame;
    
    private AbstractFile currentFolder;

    // Registered LocationListeners
    private Vector locationListeners = new Vector();
	
	/*  We're NOT using JComboBox anymore because of its strange behavior: 
		it calls actionPerformed() each time an item is highlighted with the arrow (UP/DOWN) keys,
		so there is no way to tell if it's the final selection (ENTER) or not.
	*/
	private JButton rootButton;
	private JPopupMenu rootPopup;
	private Vector rootMenuItems;
	private JTextField locationField;
	
	private FileTable fileTable;
	private JScrollPane scrollPane;
	
	private static AbstractFile rootFolders[];
	
    private static Color backgroundColor;

	private int lastPopupIndex;

	private Vector history;
	private int historyIndex;
    
	private String lastFolderOnExit;

	
	static {
		rootFolders = RootFolders.getRootFolders();

		// Set background color
		backgroundColor = FileTableCellRenderer.getColor("prefs.colors.background", "000084");
	}

	
	public FolderPanel(MainFrame mainFrame, AbstractFile initialFolder) {
		super(new BorderLayout());

        this.mainFrame = mainFrame;
		JPanel locationPanel = new JPanel(new BorderLayout()) {
			public Insets getInsets() {
				return new Insets(0, 0, 0, 0);
			}
		
			public Border getBorder() {
				return null;
			}
		};
		
		rootButton = new JButton(rootFolders[0].toString());
		// For Mac OS X whose minimum width for buttons is enormous
		rootButton.setMinimumSize(new Dimension(40, (int)rootButton.getPreferredSize().getWidth()));
		rootButton.setMargin(new Insets(0,5,0,5));
		
		rootButton.addActionListener(this);
		rootPopup = new JPopupMenu();
		rootPopup.addPopupMenuListener(this);
		rootMenuItems = new Vector();
		JMenuItem menuItem;
		for(int i=0; i<rootFolders.length; i++) {
			menuItem = new JMenuItem(rootFolders[i].toString());
			menuItem.addActionListener(this);
			rootMenuItems.add(menuItem);
			rootPopup.add(menuItem);
		}

		locationPanel.add(rootButton, BorderLayout.WEST);

		locationField = new JTextField();
		locationField.addActionListener(this);
		locationField.addKeyListener(this);
		locationPanel.add(locationField, BorderLayout.CENTER);

		add(locationPanel, BorderLayout.NORTH);
		fileTable = new FileTable(mainFrame, this);

		// Initializes history vector
		history = new Vector();
    	historyIndex = -1;
		
		try {
			// Sets initial folder to current directory
			_setCurrentFolder(initialFolder, true);
		}
		catch(Exception e) {
			// If that failed, tries to read any other drive
				for(int i=0; i<rootFolders.length; i++) {
					try  {
						_setCurrentFolder(rootFolders[i], true);
						break;
					}
					catch(IOException e2) {
						if (i==rootFolders.length-1) {
							// Now we're screwed
							throw new RuntimeException("Unable to read any drive");
						}
					}					
				}
		}

		locationField.setText(currentFolder.getAbsolutePath(true));
				
		scrollPane = new JScrollPane(fileTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) {
			public Insets getInsets() {
				return new Insets(0, 0, 0, 0);
			}
		
			public Border getBorder() {
				return null;
			}
		};
		scrollPane.getViewport().setBackground(backgroundColor);
		add(scrollPane, BorderLayout.CENTER);
	
		// Listens to some configuration variables
		ConfigurationManager.addConfigurationListener(this);
	}

	
	public Border getBorder() {
		return null;
	}
	

    public FileTable getFileTable() {
        return fileTable;
    }

	public MainFrame getMainFrame() {
		return mainFrame;
	}

	
	public void addLocationListener(LocationListener listener) {
		locationListeners.add(listener);
	}

	public void removeLocationListener(LocationListener listener) {
		locationListeners.remove(listener);
	}

	
	public void showRootBox() {
		rootPopup.show(rootButton, 0, rootButton.getHeight());		
		rootPopup.requestFocus();
//		FocusRequester.requestFocus(rootPopup);
	}

	public AbstractFile getCurrentFolder() {
		return currentFolder;
	}


	private void _setCurrentFolder(AbstractFile folder, boolean addToHistory) throws IOException {
		mainFrame.setCursor(new Cursor(Cursor.WAIT_CURSOR));

        if(com.mucommander.Debug.ON)
            System.out.println("FolderPanel._setCurrentFolder: "+folder+" ");

		try {
			fileTable.setCurrentFolder(folder);
			this.currentFolder = folder;

			// Updates root button label if necessary
			String currentPath = currentFolder.getAbsolutePath();
			for(int i=0; i<rootFolders.length; i++) {
				if (currentPath.toLowerCase().startsWith(rootFolders[i].getAbsolutePath().toLowerCase())) {
					rootButton.setText(rootFolders[i].toString());
					lastPopupIndex = i;
					break;
				}
			}

			locationField.setText(currentFolder.getAbsolutePath(true));
			locationField.repaint();

			if (addToHistory) {
				historyIndex++;

				// Deletes 'forward' history items if any
				int size = history.size();
				for(int i=historyIndex; i<size; i++) {
					history.removeElementAt(historyIndex);
				}
				// Inserts previous folder in history
				history.add(folder);
			}

			// Saves last folder recallable on startup
			if(!(folder instanceof RemoteFile))
				this.lastFolderOnExit = folder.getAbsolutePath();
			
			// Notifies listeners that location has changed
			fireLocationChanged();
		}
		catch(IOException e) {
			mainFrame.setCursor(Cursor.getDefaultCursor());
			throw e;
		}
		mainFrame.setCursor(Cursor.getDefaultCursor());
	}


	/**
	 * Notifies all listeners that have registered interest for notification on this event type.
	 */
	public void fireLocationChanged() {
		for(int i=0; i<locationListeners.size(); i++)
			((LocationListener)locationListeners.elementAt(i)).locationChanged(this);
	}

	
	/**
	 * Displays a popup message notifying the user that the request folder couldn't be opened.
	 */
	private void showFolderAccessError(IOException e) {
		String exceptionMsg = e.getMessage();
		String errorMsg = Translator.get("table.folder_access_error")+(exceptionMsg==null?".":": "+exceptionMsg);
		if(!errorMsg.endsWith("."))
			errorMsg += ".";
		JOptionPane.showMessageDialog(mainFrame, errorMsg, Translator.get("table.folder_access_error_title"), JOptionPane.ERROR_MESSAGE);
	}

	
	/**
	 * Returns <code>true</code> if the folder was correctly set, <code>false</code> if
	 * an Exception has been thrown and a error message has been displayed to the end user.
	 */
	public boolean setCurrentFolder(AbstractFile folder, boolean addToHistory) {
		boolean success = false;

        if(com.mucommander.Debug.ON)
            System.out.println("FolderPanel.setCurrentFolder: "+folder+" ");
        
		if (folder==null || !folder.exists()) {
			JOptionPane.showMessageDialog(mainFrame, Translator.get("table.folder_does_not_exist"), Translator.get("table.folder_access_error_title"), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		boolean hadFocus = fileTable.hasFocus();

		try {
			_setCurrentFolder(folder, addToHistory);
			success = true;
		}
        catch(IOException e) {
        	showFolderAccessError(e);
		}
    
        if(hadFocus || mainFrame.getLastActiveTable()==fileTable)
			fileTable.requestFocus();

		return success;
	}
	
	
	public boolean goBack() {

		if (historyIndex==0)
			return false;
		
		boolean success = false;
		try {
			_setCurrentFolder((AbstractFile)history.elementAt(historyIndex-1), false);
			historyIndex--;

			success = true;
		}
		catch(IOException e) {
//		    JOptionPane.showMessageDialog(mainFrame, "Unable to access folder contents.", "Access error", JOptionPane.ERROR_MESSAGE);
			showFolderAccessError(e);
		}
	
		// Notifies listeners that location has changed
		fireLocationChanged();

		fileTable.requestFocus();

		return success;
	}
	
	public boolean goForward() {
		if (historyIndex==history.size()-1)
			return false;
		
		boolean success = false;
		try {
			_setCurrentFolder((AbstractFile)history.elementAt(historyIndex+1), false);
			historyIndex++;

			success = true;
		}
		catch(IOException e) {
//		    JOptionPane.showMessageDialog(mainFrame, "Unable to access folder contents.", "Access error", JOptionPane.ERROR_MESSAGE);
			showFolderAccessError(e);
		}

		// Notifies listeners that location has changed
		fireLocationChanged();

		fileTable.requestFocus();

		return success;
	}

	/**
	 * Returns <code>true</code> if there is at least one folder 'back' in the history.
	 */
	public boolean hasBackFolder() {
		return historyIndex!=0;
	}

	/**
	 * Returns <code>true</code> if there is at least one folder 'forward' in the history.
	 */
	public boolean hasForwardFolder() {
		return historyIndex!=history.size()-1;
	}
	
	
	/**
	 * Returns the last folder the user went to before quitting the application.
	 * This folder will be loaded on next mucommander startup, so the returned folder
	 * should NOT be a folder on a remote filesystem (likely not to be reachable).
	 */
	public String getLastSavableFolder() {
		return this.lastFolderOnExit;
	}
	
	
	/**
	 * Refreshes this panel's components, file table, root button and location field.
	 */
	public void refresh() {
		rootButton.repaint();
		locationField.repaint();
	
		try {
			fileTable.refresh();
		}
		catch(IOException e) {
//			JOptionPane.showMessageDialog(mainFrame, "Unable to access folder contents.", "Access error", JOptionPane.ERROR_MESSAGE);
			showFolderAccessError(e);
		}
	}


	/**
	 * This method must be called when this FolderPanel isn't used anymore, otherwise
	 * resources associated to this FolderPanel won't be released.
	 */
	public void dispose() {
		ConfigurationManager.removeConfigurationListener(this);
		fileTable.dispose();
	}
	
	
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		if (source == locationField) {
			// If folder could not be set, restore current folder's path
			if(!setCurrentFolder(AbstractFile.getAbstractFile(locationField.getText()), true))
				locationField.setText(currentFolder.getAbsolutePath(true));
		}
		else if (source == rootButton)	 {
			showRootBox();
		}
		// root menu items
		else {		
			if (rootMenuItems.indexOf(source)!=-1) {
				int index = rootMenuItems.indexOf(source);

				// Tries to change current folder
				if (setCurrentFolder(rootFolders[index], true)) {
					// if success, hide popup
					rootPopup.setVisible(false);
					
					// and request focus on this file table
					fileTable.requestFocus();
				}
			}
		}
	}

	/***********************
	 * KeyListener methods *
	 ***********************/

	public void keyPressed(KeyEvent e) {
		if (e.getSource()==locationField) {
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				// Restore current location string
				locationField.setText(currentFolder.getAbsolutePath(true));
				fileTable.requestFocus();
			}
		}
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
	}


	/*****************************
	 * PopupMenuListener methods *
	 *****************************/
	 
	 public void popupMenuCanceled(PopupMenuEvent e) {
	 }

	 public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		fileTable.requestFocus();
	 }

	 public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
	 }




    /**
     * Listens to certain configuration variables.
     */
    public boolean configurationChanged(ConfigurationEvent event) {
    	String var = event.getVariable();
    
		if (var.equals("prefs.colors.background"))  {
			scrollPane.getViewport().setBackground(backgroundColor=FileTableCellRenderer.getColor(event.getValue()));
			repaint();    		
		}
		
    	return true;
    }
}