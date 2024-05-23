/** 
 * Copyright (c) 1998, 2021, Oracle and/or its affiliates. All rights reserved.
 * 
 */


package com.kpi.tuke.scql;

import javacard.framework.*;
import javacardx.apdu.ExtendedLength;

/**
 * SCQL Applet class implementing the SCQL handling mechanism by definition of ISO7816-7
 * 
 * @author Roman Danylych
 */
public class ScqlApplet extends Applet implements ExtendedLength {

    private ScqlDatabase db;

    /**
     * Installs this applet.
     * 
     * @param bArray
     *            the array containing installation parameters
     * @param bOffset
     *            the starting offset in bArray
     * @param bLength
     *            the length in bytes of the parameter data in bArray
     */
    public static void install(byte[] bArray, short bOffset, byte bLength) {
    	new ScqlApplet();
    }

    /**
     * Only this class's install method should create the applet object.
     */
    protected ScqlApplet() {
        register();

        if (this.db == null) {
            this.db = new ScqlDatabase();
        }
    }

    /**
     * Processes an incoming APDU.
     * @param apdu the incoming APDU.
     */
    @Override
    public void process(APDU apdu) {

        byte[] buffer = apdu.getBuffer();

        if (apdu.isISOInterindustryCLA()) {
            if (buffer[ISO7816.OFFSET_INS] == (byte) (0xA4)) {
                return;
            }
        } else {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }

        if (db == null) {
            ISOException.throwIt(ISO7816.SW_FILE_INVALID);
        }

        switch (buffer[ISO7816.OFFSET_INS]) {
            case (byte) 0x10:
                performScql(apdu);
                return;
            case (byte) 0x12:
                performTransaction(apdu);
                return;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    /**
     * Performs APDU Transaction operation, as defined in ISO7816-7.
     * @param apdu incoming APDU command.
     */
    private void performTransaction(APDU apdu) {
        byte[] buffer = apdu.getBuffer();

        switch (buffer[ISO7816.OFFSET_P2]) {
            case (byte) 0x80:
                JCSystem.beginTransaction();
                return;
            case (byte) 0x81:
                JCSystem.commitTransaction();
                return;
            case (byte) 0x82:
                JCSystem.abortTransaction(); 
                return;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    /**
     * Performs APDU SCQL operation, as defined in ISO7816-7/
     * @param apdu incoming APDU command.
     */
    private void performScql(APDU apdu) {

        byte[] buffer = apdu.getBuffer();
        
        short memoryConsumption = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_PERSISTENT);

        switch (buffer[ISO7816.OFFSET_P2]) {
            case (byte) 0x80:
                db.createTable(apdu);
                return;
            case (byte) 0x81:
                db.createView(apdu);
                return;
            case (byte) 0x8c:
                db.insertInto(apdu);
                return;
            case (byte) 0x87:
            	db.declareCursor(apdu);
            	return;
            case (byte) 0x88:
                db.open();
                return;
            case (byte) 0x89:
                db.next();
                return;
            case (byte) 0x8a:
                db.fetch(apdu);
                return;
            case (byte) 0x8b:
                db.fetchNext(apdu);
                return;
            case (byte) 0x83:
                db.dropTable(apdu);
                return;
            case (byte) 0x84:
                db.dropView(apdu);
                return;
            case (byte) 0x8e:
                db.delete();
                return;
            default:
                ISOException.throwIt(ISO7816.SW_FUNC_NOT_SUPPORTED);
        }
    }
}
