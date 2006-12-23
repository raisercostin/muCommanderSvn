package com.mucommander.res;

import com.mucommander.xml.parser.*;
import java.io.InputStream;
import java.util.*;

/**
 * @author Nicolas Rinaudo
 */
public class ResourceListReader implements ContentHandler, XmlConstants {
    // - Parser states -------------------------------------------------------
    // -----------------------------------------------------------------------
    /** Parsing hasn't started. */
    private static final int STATE_UNKNOWN = 0;
    /** Parsing the root element. */
    private static final int STATE_ROOT    = 1;



    // - Instance variables --------------------------------------------------
    // -----------------------------------------------------------------------
    /** Resources contained in the stream being parsed. */
    private Vector content;
    /** Current XML parser state. */
    private int    state;



    // - Initialisation ------------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Creates a new instance of resource list reader.
     */
    public ResourceListReader() {}



    // - XML interaction -----------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Reads the content of the specified stream and returns the result
     * @param  in        where to read the data from.
     * @return           the different resource file paths described by the list.
     * @throws Exception thrown if an error occurs.
     */
    public Vector read(InputStream in) throws Exception {
        // Initialises parsing.
        state   = STATE_UNKNOWN;
        content = new Vector();

        new Parser().parse(in, this, "UTF-8");

        return content;
    }

    /**
     * Notifies the reader that a new XML element is starting.
     */
    public void startElement(String uri, String name, Hashtable attributes, Hashtable attURIs) throws Exception {
        String path;

        // Root element.
        if(name.equals(ROOT_ELEMENT)) {
            if(state != STATE_UNKNOWN)
                throw new Exception("Illegal start of element: " + name);
            state = STATE_ROOT;
        }

        // File element.
        else if(name.equals(FILE_ELEMENT)) {
            if(state != STATE_ROOT)
                throw new Exception("Illegal start of element: " + name);
            if((path = (String)attributes.get(PATH_ATTRIBUTE)) != null)
                content.add(path);
        }

        // Unknown element.
        else
            throw new Exception("Unknown XML element: " + name);
    }
    
    /**
     * Notifies the reader that an XML element declaration is finished,
     */
    public void endElement(String uri, String name) throws Exception {
        // Root element.
        if(name.equals(ROOT_ELEMENT)) {
            if(state != STATE_ROOT)
                throw new Exception("Illegal end of element: " + name);
            state = STATE_UNKNOWN;
        }

        // Unknown element.
        else if(!name.equals(FILE_ELEMENT))
            throw new Exception("Unknown XML element: " + name);
    }



    // - Unused XML methods --------------------------------------------------
    // -----------------------------------------------------------------------
    /**
     * Not used.
     */
    public void startDocument() {}

    /**
     * Not used.
     */
    public void endDocument() {}

    /**
     * Not used.
     */
    public void characters(String s) {}
}
