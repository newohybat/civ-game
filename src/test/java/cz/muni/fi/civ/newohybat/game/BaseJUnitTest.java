package cz.muni.fi.civ.newohybat.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import bitronix.tm.resource.jdbc.PoolingDataSource;
import cz.muni.fi.civ.newohybat.drools.listeners.AgendaListener;
import cz.muni.fi.civ.newohybat.drools.listeners.ProcessListener;
import cz.muni.fi.civ.newohybat.game.init.SessionLoader;
import cz.muni.fi.civ.newohybat.game.service.GameService;
import cz.muni.fi.civ.newohybat.jbpm.itemhandler.CityWorkItemHandler;
import cz.muni.fi.civ.newohybat.jbpm.itemhandler.UnitWorkItemHandler;


public abstract class BaseJUnitTest {
	protected static Logger logger = Logger.getAnonymousLogger();

	@Inject
    @ApplicationScoped
	protected SessionLoader loader;
	
	@Inject
	protected GameService game;
	
	protected KieSession ksession;

	@Deployment()
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(JavaArchive.class, "jbpm-cdi-sample.jar")
        		.addPackage("org.jbpm.services.task")
                .addPackage("org.jbpm.services.task.wih") // work items org.jbpm.services.task.wih
                .addPackage("org.jbpm.services.task.annotations")
                .addPackage("org.jbpm.services.task.api")
                .addPackage("org.jbpm.services.task.impl")
                .addPackage("org.jbpm.services.task.events")
                .addPackage("org.jbpm.services.task.exception")
                .addPackage("org.jbpm.services.task.identity")
                .addPackage("org.jbpm.services.task.factories")
                .addPackage("org.jbpm.services.task.internals")
                .addPackage("org.jbpm.services.task.internals.lifecycle")
                .addPackage("org.jbpm.services.task.lifecycle.listeners")
                .addPackage("org.jbpm.services.task.query")
                .addPackage("org.jbpm.services.task.util")
                .addPackage("org.jbpm.services.task.commands") // This should not be required here
                .addPackage("org.jbpm.services.task.deadlines") // deadlines
                .addPackage("org.jbpm.services.task.deadlines.notifications.impl")
                .addPackage("org.jbpm.services.task.subtask")
                .addPackage("org.jbpm.services.task.rule")
                .addPackage("org.jbpm.services.task.rule.impl")

                .addPackage("org.kie.api.runtime")
                .addPackage("org.kie.api.runtime.manager")
                .addPackage("org.kie.api.runtime.process")
                .addPackage("org.kie.internal.runtime.manager")
                .addPackage("org.kie.internal.runtime.manager.context")
                .addPackage("org.kie.internal.runtime.manager.cdi.qualifier")
                
                .addPackage("org.jbpm.runtime.manager.impl")
                .addPackage("org.jbpm.runtime.manager.impl.cdi")                               
                .addPackage("org.jbpm.runtime.manager.impl.factory")
                .addPackage("org.jbpm.runtime.manager.impl.jpa")
                .addPackage("org.jbpm.runtime.manager.impl.manager")
                .addPackage("org.jbpm.runtime.manager.impl.task")
                .addPackage("org.jbpm.runtime.manager.impl.tx")
                
                .addPackage("org.jbpm.shared.services.api")
                .addPackage("org.jbpm.shared.services.impl")
                .addPackage("org.jbpm.shared.services.impl.tx")
                
                .addPackage("org.jbpm.kie.services.api")
                .addPackage("org.jbpm.kie.services.impl")
                .addPackage("org.jbpm.kie.services.cdi.producer")
                .addPackage("org.jbpm.kie.services.api.bpmn2")
                .addPackage("org.jbpm.kie.services.impl.bpmn2")
                .addPackage("org.jbpm.kie.services.impl.event.listeners")
                .addPackage("org.jbpm.kie.services.impl.audit")
                
                .addPackage("org.kie.api.runtime.rule")
                
                .addPackage("org.jbpm.services.task.commands")
                .addPackage("org.drools.core.command.impl")
                
                .addPackage("org.jbpm.kie.services.impl.example")
                .addPackage("org.kie.commons.java.nio.fs.jgit")
                
                .addPackage("cz.muni.fi.civ.newohybat.persistence.facade.iface")
                .addPackage("cz.muni.fi.civ.newohybat.persistence.facade.impl")
                .addPackage("cz.muni.fi.civ.newohybat.persistence.services.iface")
                .addPackage("cz.muni.fi.civ.newohybat.persistence.services.impl")
                .addPackage("cz.muni.fi.civ.newohybat.persistence.dao.iface")
                .addPackage("cz.muni.fi.civ.newohybat.persistence.dao.impl")
                .addPackage("cz.muni.fi.civ.newohybat.persistence.facade.dto")
                .addPackage("cz.muni.fi.civ.newohybat.persistence.facade.helpers")
                .addPackage("cz.muni.fi.civ.newohybat.persistence.entities")
                .addPackage("cz.muni.fi.civ.newohybat.game.init")
                .addPackage("cz.muni.fi.civ.newohybat.game.service")
                .addPackage("cz.muni.fi.civ.newohybat.game.listeners")
                .addPackage("cz.muni.fi.civ.newohybat.drools.events")
                .addPackage("cz.muni.fi.civ.newohybat.drools.listeners")
                .addPackage("cz.muni.fi.civ.newohybat.game.itemhandler")
                .addPackage("org.mockito")
                .addAsResource("jndi.properties", "jndi.properties")
                .addAsManifestResource("META-INF/beans.xml", "beans.xml")
                ;

    }
    
    private static PoolingDataSource pds;

    @BeforeClass
    public static void setup() {
    	pds = new PoolingDataSource();
        pds.setUniqueName("jdbc/jbpm-ds");
        pds.setClassName("bitronix.tm.resource.jdbc.lrc.LrcXADataSource");
        pds.setMaxPoolSize(5);
        pds.setAllowLocalTransactions(true);
        pds.getDriverProperties().put("user", "sa");
        pds.getDriverProperties().put("password", "");
        pds.getDriverProperties().put("url", "jdbc:h2:mem:jbpm-db;MVCC=true");
        pds.getDriverProperties().put("driverClassName", "org.h2.Driver");
        pds.init();        
    }
    
    @AfterClass
    public static void cleanup() {
        if (pds != null) {
            pds.close();
        }
    }
    
    @Before
	public void before(){
    	loader.initSession();
    	ksession = loader.getKieSession();
	}
	@After
	public void after(){
//		ksession.halt();
//		ksession.dispose();
	}

    protected List<String> getFiredRules(List<AfterMatchFiredEvent> events){
    	List<String> runRules = new ArrayList<String>();
		for(AfterMatchFiredEvent e:events){
			runRules.add(e.getMatch().getRule().getName());
		}
		return runRules;
    }
    
    public void assertProcessInstanceCompleted(long processInstanceId, KieSession ksession) {
    		Assert.assertNull(ksession.getProcessInstance(processInstanceId));
    	}
    	
    	public void assertProcessInstanceAborted(long processInstanceId, KieSession ksession) {
    		Assert.assertNull(ksession.getProcessInstance(processInstanceId));
    	}
    	
    	public void assertProcessInstanceActive(long processInstanceId, KieSession ksession) {
    			Assert.assertNotNull(ksession.getProcessInstance(processInstanceId));
    	}
	
}