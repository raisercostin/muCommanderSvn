/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.viewer;

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.text.Translator;
import com.mucommander.ui.helper.MenuToolkit;
import com.mucommander.ui.helper.MnemonicHelper;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;

/**
 * An abstract class to be subclassed by file viewer implementations.
 *
 * <p><b>Warning:</b> the file viewer/editor API may soon receive a major overhaul.</p>
 *
 * @author Maxence Bernard, Arik Hadas
 */
public abstract class FileViewer implements FilePresenter, ActionListener {
	
    /** ViewerFrame instance that contains this viewer (may be null). */
    private ViewerFrame frame;

    /** File currently being viewed. */
    private AbstractFile file;
	
    /** Close menu item */
    private JMenuItem closeItem;
    
    /**
     * Creates a new FileViewer.
     */
    public FileViewer() {}
	

    /**
     * Returns the frame which contains this viewer.
     * <p>
     * This method may return <code>null</code>if the viewer is not inside a ViewerFrame.
     * </p>
     * @return the frame which contains this viewer.
     * @see    #setFrame(ViewerFrame)
     */
    protected ViewerFrame getFrame() {
        return frame;
    }

    /**
     * Sets the ViewerFrame (separate window) that contains this FileViewer.
     * @param frame frame that contains this <code>FileViewer</code>.
     * @see         #getFrame()
     */
    final void setFrame(ViewerFrame frame) {
        this.frame = frame;
    }


    /**
     * Returns a description of the file currently being viewed which will be used as a window title.
     * This method returns the file's name but it can be overridden to provide more information.
     * @return this dialog's title.
     */
    protected String getTitle() {
        return file.getAbsolutePath();
    }
	

    /**
     * Returns the file that is being viewed.
     *
     * @return the file that is being viewed.
     */
    protected AbstractFile getCurrentFile() {
        return file;
    }

    /**
     * Sets the file that is to be viewed.
     * This method will automatically be called after a file viewer is created and should not be called directly.
     * 
     * @param file file that is to be viewed.
     */
    protected final void setCurrentFile(AbstractFile file) {
        this.file = file;
        // Update frame's title
        getFrame().setTitle(getTitle());
    }
    
    /**
     * Returns the menu bar that controls the viewer's frame. The menu bar should be retrieved using this method and
     * not by calling {@link JFrame#getJMenuBar()}, which may return <code>null</code>.
     *
     * @return the menu bar that controls the viewer's frame.
     */
    protected JMenuBar getMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        MnemonicHelper menuMnemonicHelper = new MnemonicHelper();
        MnemonicHelper menuItemMnemonicHelper = new MnemonicHelper();

        // File menu
        JMenu fileMenu = MenuToolkit.addMenu(Translator.get("file_viewer.file_menu"), menuMnemonicHelper, null);
        closeItem = MenuToolkit.addMenuItem(fileMenu, Translator.get("file_viewer.close"), menuItemMnemonicHelper, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), this);
        fileMenu.add(closeItem);
        
        menuBar.add(fileMenu);

        return menuBar;
    }
    
    //////////////////////////////////
    // FilePresenter implementation //
    //////////////////////////////////
    
    public void open(AbstractFile file) throws IOException {
    	show(file);
    	setCurrentFile(file);
    }
    
    ///////////////////////////////////
    // ActionListener implementation //
    ///////////////////////////////////

    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==closeItem)
            frame.dispose();
    }


    //////////////////////
    // Abstract methods //
    //////////////////////
	
    /**
     * This method is invoked when the specified file is about to be opened.
     * This method should retrieve the file and do the necessary so that this component can be displayed.
     *
     * @param  file        the file that is about to be viewed.
     * @throws IOException if an I/O error occurs.
     */
    public abstract void show(AbstractFile file) throws IOException;
}
