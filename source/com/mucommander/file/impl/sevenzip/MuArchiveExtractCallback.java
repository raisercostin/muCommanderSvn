package com.mucommander.file.impl.sevenzip;

import java.io.File;

import com.mucommander.Debug;
import com.mucommander.file.impl.sevenzip.provider.SevenZip.ArchiveExtractCallback;
import com.mucommander.file.impl.sevenzip.provider.SevenZip.HRESULT;
import com.mucommander.file.impl.sevenzip.provider.SevenZip.Archive.IInArchive;
import com.mucommander.file.impl.sevenzip.provider.SevenZip.Archive.SevenZipEntry;

public class MuArchiveExtractCallback extends ArchiveExtractCallback {
	private java.io.OutputStream out;
	private String filename;
	
	public MuArchiveExtractCallback(java.io.OutputStream out, String filename) {
		this.out = out;
		this.filename = filename;
	}
	
	public void PrintString(String str) {
		if (Debug.ON) { Debug.trace(str); }
    }
	
	public int GetStream(int index,
            java.io.OutputStream [] outStream,
            int askExtractMode) throws java.io.IOException {
        
        outStream[0] = null;
        
        SevenZipEntry item = _archiveHandler.getEntry(index);
        _filePath = item.getName();
        
        File file = new File(_filePath);
        
        switch (askExtractMode) {
            case IInArchive.NExtract_NAskMode_kTest:
                return HRESULT.S_OK;
                
            case IInArchive.NExtract_NAskMode_kExtract:
                
                    boolean isDirectory = item.isDirectory();
                    
                    if (isDirectory) {
                        if (file.isDirectory()) {
                            return HRESULT.S_OK;
                        }
                        if (file.mkdirs())
                            return HRESULT.S_OK;
                        else
                            return HRESULT.S_FALSE;
                    }
                    
//                    System.out.println("filename = " + filename);
//                    System.out.println("_filePath = " + _filePath);
                    if (!filename.equals(_filePath))
                    	return HRESULT.S_FALSE;
                    
                    File dirs = file.getParentFile();
                    if (dirs != null) {
                        if (!dirs.isDirectory())
                            if (!dirs.mkdirs())
                                return HRESULT.S_FALSE;
                    }
                    
                    long pos = item.getPosition();
                    if (pos == -1) {
                        file.delete();
                    }
                    
                    outStream[0] = this.out;
                
                return HRESULT.S_OK;
                
        }
        
        // other case : skip ...
        
        return HRESULT.S_OK;
    }
}
