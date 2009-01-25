/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2009 Maxence Bernard
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


package com.mucommander.file;

/**
 * This class represents a generic archive entry. It provides getters and setters for common archive entry attributes
 * and allows to encapsulate the entry object of a 3rd party library.
 *
 * <p><b>Important</b>: the path of archive entries must use the '/' character as a path delimiter, and be relative
 * to the archive's root, i.e. must not start with a leading '/'.</p>
 *
 * @author Maxence Bernard 
 */
public class ArchiveEntry extends SimpleFileAttributes {

    /** Encapsulated entry object */
    protected Object entryObject;


    /**
     * Creates a new ArchiveEntry with all attributes set to their default value.
     */
    public ArchiveEntry() {
    }

    /**
     * Creates a new ArchiveEntry using the supplied path and directory attributes.
     *
     * @param path the entry's path
     * @param directory true if the entry is a directory
     */
    public ArchiveEntry(String path, boolean directory) {
        setPath(path);
        setDirectory(directory);
    }

    /**
     * Creates a new ArchiveEntry using the values of the supplied attributes.
     *
     * @param path the entry's path
     * @param directory true if the entry is a directory
     * @param date the entry's date
     * @param size the entry's size
     */
    public ArchiveEntry(String path, boolean directory, long date, long size) {
        setPath(path);
        setDate(date);
        setSize(size);
        setDirectory(directory);
    }


    /**
     * Returns the depth of this entry based on the number of path delimiters ('/') its path contains.
     * Top-level entries have a depth of 0 (minimum depth).
     *
     * @return the depth of this entry
     */
    public int getDepth() {
        return getDepth(getPath());
    }

    /**
     * Returns the depth of the specified entry path, based on the number of path delimiters ('/') it contains.
     * Top-level entries have a depth of 0 (minimum depth).
     *
     * @param entryPath the path for which to calculate the depth
     * @return the depth of the given entry path
     */
    public static int getDepth(String entryPath) {
        int depth = 0;
        int pos=0;

        while ((pos=entryPath.indexOf('/', pos+1))!=-1)
            depth++;

        // Directories in archives end with a '/'
        if(entryPath.charAt(entryPath.length()-1)=='/')
            depth--;

        return depth;
    }

    /**
     * Extracts this entry's filename from its path and returns it.
     *
     * @return this entry's filename
     */
    public String getName() {
        String path = getPath();
        int len = path.length();
        // Remove trailing '/' if any
        if(path.charAt(len-1)=='/')
            path = path.substring(0, --len);

        int lastSlash = path.lastIndexOf('/');
        return lastSlash==-1?
          path:
          path.substring(lastSlash+1, len);
    }

    /**
     * Returns an archive format-dependent object providing extra information about this entry, typically an object from
     * a 3rd party library ; <code>null</code> if this entry has none.
     *
     * @return an object providing extra information about this entry, null if this entry has none
     */
    public Object getEntryObject() {
        return entryObject;
    }

    /**
     * Sets an archive format-dependent object providing extra information about this entry, typically an object from
     * a 3rd party library ; <code>null</code> for none.
     *
     * @param entryObject an object providing extra information about this entry, null for none
     */
    public void setEntryObject(Object entryObject) {
        this.entryObject = entryObject;
    }


    ////////////////////////
    // Overridden methods //
    ////////////////////////

    /**
     * Returns the file permissions of this entry. This method is overridden to return default permissions
     * ({@link FilePermissions#DEFAULT_DIRECTORY_PERMISSIONS} for directories, {@link FilePermissions#DEFAULT_FILE_PERMISSIONS}
     * for regular files), when none have been set.
     *
     * @return the file permissions of this entry
     */
    public FilePermissions getPermissions() {
        FilePermissions permissions = super.getPermissions();
        if(permissions==null)
            return isDirectory()?FilePermissions.DEFAULT_DIRECTORY_PERMISSIONS:FilePermissions.DEFAULT_FILE_PERMISSIONS;

        return permissions;
    }
}
