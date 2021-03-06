/**
 * @file RepoDocumentInterface
 * @version 1.0.0
 * @type java
 * @data 2020-05-13
 * @author Eduard Serban
 * @email hexatech016@gmail.com
 * @license MIT
 */

package com.hexaTech.domain.port.out.repository;

import com.hexaTech.domain.entity.Document;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * RepoDocument class interface.
 */
public interface RepoDocumentInterface {

    /**
     * Loads a new document from file system.
     * @param directory string - directory used to save the file.
     * @throws IOException if an error occurs during file loading process.
     * @return boolean - false if something goes wrong, true if not.
     */
    boolean importDoc(String directory,String document) throws IOException;

    /**
     * Saves the doucment's path into a backup file.
     * @param title string - title of the file to be saved.
     * @param directory string - directory of the file to be saved
     * @throws IOException if occurs an error while creating the file or writing into it.
     */
    void saveDoc(String title, String directory);

    /**
     * Verifies if the documents exists.
     * @param path string - document's path.
     * @return boolean - true if document exists, false if not.
     */
    boolean existsDoc(String path);

    /**
     * Delete the specified document.
     * @param path string - path to the document to be deleted.
     * @return bool - true if the file exists, false if not.
     */
    boolean deleteDoc(String path);

    /**
     * Gets document's content.
     * @param path string - document path.
     * @return string - document content.
     * @throws IOException if the document doesn't exist.
     */
    String getContentFromPath(String path) throws IOException;

    /**
     * Loads content from a backup file and restore it.
     * @param directory string - directory used to search the file in.
     * @throws IOException if the backup file doesn't exist.
     */
    void loadBackup(String directory) throws IOException;

    List<Document> getDocuments();

    String toString();

    String getBackup(String path) throws FileNotFoundException;

    boolean removeDoc(int position);

    void makeEmpty();

    boolean isEmpty();

}//RepoDocumentInterface
