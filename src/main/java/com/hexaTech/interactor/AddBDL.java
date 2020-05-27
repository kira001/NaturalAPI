/**
 * @file AddBDL
 * @version 1.0.0
 * @type java
 * @data 2020-04-25
 * @author Matteo Brosolo
 * @email hexatech016@gmail.com
 * @license MIT
 */

package com.hexaTech.interactor;

import com.hexaTech.entities.BDL;
import com.hexaTech.portInterface.AddBDLInputPort;
import com.hexaTech.portInterface.AddBDLOutputPort;
import com.hexaTech.repointerface.RepoBDLInterface;

import java.io.IOException;

/**
 * Class used to manage a BDL insertion.
 */
public class AddBDL implements AddBDLInputPort {


    AddBDLOutputPort addBDLOutputPort;
    RepoBDLInterface repoBDLInterface;
    /**
     * AddBDL class constructor.
     * @param addBDLOutputPort AddBDLOutputPort - used to send output notifications.
     */
    public AddBDL(AddBDLOutputPort addBDLOutputPort, RepoBDLInterface repoBDLInterface) {
        this.addBDLOutputPort = addBDLOutputPort;
        this.repoBDLInterface = repoBDLInterface;
    }

    /**
     * Loads a new BDL.
     * @throws IOException if an error occurs during loading process.
     */
    @Override
    public void addBDL(String directory) throws IOException {
        String path=repoBDLInterface.importPathOfBDL();
        if(path.equals("")) {
            addBDLOutputPort.showAddedBDL("Select an existing .json file. Please retry.");
        }else{
            repoBDLInterface.setBDL(repoBDLInterface.loadBDLFromJsonFile(path));
            addBDLOutputPort.showAddedBDL("BDL added");
        }//if_else
    }//addBDL

}//AddBDL
