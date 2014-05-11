package cz.muni.fi.civ.newohybat.game.init;

import javax.transaction.TransactionManager;

public class CustomJbossJTAPlatform extends org.hibernate.service.jta.platform.internal.JBossAppServerJtaPlatform{
	@Override
   protected TransactionManager locateTransactionManager() {
      return (TransactionManager) jndiService().locate( "java:jboss/TransactionManager" );
   }
}
