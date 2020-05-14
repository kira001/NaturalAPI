/**
 * @file RepoBALInterface
 * @version 1.0.0
 * @type java
 * @data 2020-05-13
 * @author Alessio Barbiero
 * @email hexatech016@gmail.com
 * @license MIT
 */

package com.hexaTech.repo;

import com.hexaTech.entities.Document;

/**
 * RepoBAL class interface.
 */
public interface RepoBALInterface extends RepoInterface{
    /**
     * Returns BAL object.
     * @return BAL - BAL object.
     */
    Document getBAL();
}//RepoBALInterface