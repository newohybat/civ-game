package cz.muni.fi.civ.newohybat.game.init;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.transaction.TransactionManager;

import org.drools.core.event.DebugProcessEventListener;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.cdi.KBase;
import org.kie.api.event.rule.DebugAgendaEventListener;
import org.kie.api.event.rule.DebugRuleRuntimeEventListener;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.internal.runtime.StatefulKnowledgeSession;

import cz.muni.fi.civ.newohybat.drools.listeners.AgendaListener;
import cz.muni.fi.civ.newohybat.drools.listeners.ProcessListener;
import cz.muni.fi.civ.newohybat.game.itemhandler.PersistableCityWorkItemHandler;
import cz.muni.fi.civ.newohybat.game.itemhandler.PersistableUnitWorkItemHandler;
import cz.muni.fi.civ.newohybat.game.listeners.CivAgendaEventListener;
import cz.muni.fi.civ.newohybat.game.listeners.CivProcessEventListener;
import cz.muni.fi.civ.newohybat.persistence.facade.iface.CivBackend;

@ApplicationScoped
public class SessionLoader {
	@Inject
	@ApplicationScoped
	private KieBase kbase;
	
	@Inject
	CivBackend cb;
	
	private KieSession ksession;
	
	@Inject
	private EntityManagerFactory emf;
	
	@Inject
	CivAgendaEventListener civAgendaListener;
	
	@Inject
	CivProcessEventListener civProcessListener;
	
	@Inject
	PersistableCityWorkItemHandler cityWorkItemHandler;
	
	@Inject
	PersistableUnitWorkItemHandler unitWorkItemHandler;
	
	public KieSession getKieSession(){
		return ksession;
	}
//	This method served for loading session when persistence employed
//	public KieSession loadSession(Integer sessionId){
//    	InitialContext ic;
//		TransactionManager man = null;
//		try {
//			ic = new InitialContext();
//			man = ((TransactionManager)ic.lookup("java:jboss/TransactionManager"));
//		} catch (NamingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}  
//		KieServices kieServices = KieServices.Factory.get();
//
//		Environment env = kieServices.newEnvironment();
//
//		env.set( EnvironmentName.ENTITY_MANAGER_FACTORY,emf );
//
//		env.set( EnvironmentName.TRANSACTION_MANAGER,man );
//
//
//		// KieSessionConfiguration may be null, and a default will be used
//
//		ksession = kieServices.getStoreServices().loadKieSession(sessionId, kbase, null, env );
//		
//		System.out.println("KSESSION_ID:"+sessionId);
//		
//		Collection<ProcessInstance> processes = ksession.getProcessInstances();
//		for(ProcessInstance pi:processes){
//			ksession.insert(pi);
//		}
//		
//		appendListenersAndHandlers();
//		return ksession;
//    }
	/*
	 * To ensure compatibility with persistence enabled version.
	 */
	public KieSession loadSession(Integer sessionId){
		return initSession();
	}
	public KieSession initSession(){
		
//		// This code serves to enable persistence
//    	InitialContext ic;
//		TransactionManager man = null;
//		try {
//			ic = new InitialContext();
//			man = ((TransactionManager)ic.lookup("java:jboss/TransactionManager"));
//		} catch (NamingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}  
//		KieServices kieServices = KieServices.Factory.get();

//		Environment env = kieServices.newEnvironment();
//
//		env.set( EnvironmentName.ENTITY_MANAGER_FACTORY,emf );
//
//		env.set( EnvironmentName.TRANSACTION_MANAGER, man );


		// KieSessionConfiguration may be null, and a default will be used
//		ksession = kieServices.getStoreServices().newKieSession(kbase, null, env );
		
		ksession = kbase.newKieSession();

		
		System.out.println("KSESSION_ID:"+ksession.getId());
		
		appendListenersAndHandlers();
		
		setGlobals();
		return ksession;
    }
	
	public void appendListenersAndHandlers(){		
		ksession.getWorkItemManager().registerWorkItemHandler("Unit", unitWorkItemHandler);
		ksession.getWorkItemManager().registerWorkItemHandler("City", cityWorkItemHandler);
		ksession.addEventListener(civAgendaListener);
		ksession.addEventListener(civProcessListener);
		ksession.addEventListener(new DebugAgendaEventListener());
		ksession.addEventListener(new DebugProcessEventListener());
		ksession.addEventListener(new DebugRuleRuntimeEventListener());
	}
	
	private void setGlobals(){
		Properties properties = new Properties();
		String foodStockLimit = "";
		String movementTimeUnit = "";
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			properties.load(classLoader.getResourceAsStream("game.properties"));
		  	foodStockLimit=properties.getProperty("foodstock.limit");
		  	movementTimeUnit = properties.getProperty("movementtime.unit");
		  	Integer value = Integer.parseInt(foodStockLimit);
		  	ksession.setGlobal("foodStockLimit", value);
		  	value = Integer.parseInt(movementTimeUnit);
		  	ksession.setGlobal("movementTimeUnit", value);
		}catch(IOException e){
			// noop
		}
	}
}
