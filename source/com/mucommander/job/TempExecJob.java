/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.job;

import com.mucommander.PlatformManager;
import com.mucommander.file.AbstractFile;
import com.mucommander.file.util.FileSet;
import com.mucommander.ui.dialog.file.FileCollisionDialog;
import com.mucommander.ui.dialog.file.ProgressDialog;
import com.mucommander.ui.main.MainFrame;

import java.io.File;

/**
 * This job copies a file to a temporary local file, makes the temporary file read-only and executes it
 * with native file associations.
 *
 * @author Maxence Bernard
 */
public class TempExecJob extends CopyJob {

    private AbstractFile tempFile;

    public TempExecJob(ProgressDialog progressDialog, MainFrame mainFrame, AbstractFile fileToExecute, AbstractFile tempFile) {
        super(progressDialog, mainFrame, new FileSet(fileToExecute.getParent(), fileToExecute), tempFile.getParent(), tempFile.getName(), COPY_MODE, FileCollisionDialog.OVERWRITE_ACTION);
        this.tempFile = tempFile;
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    protected void jobCompleted() {
        super.jobCompleted();

        // Make the temporary file read only
        new File(tempFile.getAbsolutePath()).setReadOnly();

        // Try to open the file.
        PlatformManager.open(tempFile);
    }
}
