package net.ra23.tpm.context
import iaik.tc.tss.api.constants.tsp.TcTssConstants;
import iaik.tc.tss.api.tspi._;


object TPMContext {
	val factory = new TcTssContextFactory();
	val context = factory.newContextObject();
	
}