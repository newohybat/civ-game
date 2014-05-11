package cz.muni.fi.civ.newohybat.bpmn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.runtime.manager.context.EmptyContext;

import bitronix.tm.resource.jdbc.PoolingDataSource;
import cz.muni.fi.civ.newohybat.game.init.SessionLoader;
import cz.muni.fi.civ.newohybat.game.service.GameService;
import cz.muni.fi.civ.newohybat.game.service.GameServiceImpl;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.AdvanceDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.CityDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.CityImprovementDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.GovernmentDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.PlayerDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.SpecialDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.TerrainDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.TileDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.TileImprovementDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.UnitDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.UnitTypeDTO;

/**
 * This test case illustrates how to use the CDI with jBPM.
 * <ul>
 *  <li>ProcessEngineService - this is the primary entry point for application business logic</li>
 * </ul>
 * 
 * Test case has regular JUnit life cycle phases
 * <ul>
 *  <li>BeforeClass - configures data source to processes/tasks can be persisted</li>
 *  <li>Before - cleans up singleton session id as it is singleton so it must persist session id that was used - not relevant in tests</li>
 *  <li>AfterClass - shuts down data source</li>
 * </ul>
 * Additionally since this is Arquillian based test there is a Deployment section that is responsible for 
 * setting up the CDI container.
 * <br/>
 * Test itself is very simple as it aims at presenting:
 * <ul>
 *  <li>hot to list processes available</li>
 *  <li>how to get hold of RuntimeManager and RuntimeEngine</li>
 *  <li>how to start process</li>
 * </ul>
 */
@RunWith(Arquillian.class)
public class ProcessEngineServiceTest {


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
    public void prepare() {
        cleanupSingletonSessionId();
    }
    

    @Inject
    private GameServiceImpl gameService;
    @Inject
    @ApplicationScoped
    private SessionLoader loader;
//    @Inject
//    private ProcessEngineService gameService;
    
    
    @Test
    public void testStartTurnProcess() {
       
        assertNotNull(gameService);
        gameService.init();
        gameService.startGame();
        gameService.stopGame();
    }
    @Test
    public void testAdvanceMethods() {
       
        assertNotNull(gameService);
        gameService.init();
        AdvanceDTO o = new AdvanceDTO();
        o.setIdent("base");
        o.setName("Basic advance");
        gameService.insert(o);
        
        AdvanceDTO fromSession = null;
        QueryResults results = loader.getKieSession().getQueryResults("getAdvance", new Object[]{o.getIdent()});
        for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            fromSession = (AdvanceDTO)row.get("$advance"); 
        } 
        Assert.assertTrue("Advance Placed As Fact Into KieSession",fromSession!=null);
     
        
        // get advance back
        AdvanceDTO returned = gameService.getAdvance("base");
        
        Assert.assertTrue("Advances Returned From KieSession and GameService Are The Same",returned.equals(fromSession));
        
        Collection<AdvanceDTO> advances = gameService.getAdvances();
        Assert.assertTrue("There Is Only One Advance In Session", advances.size()==1);
        Assert.assertTrue("Advance Is In getAll Method Results",advances.contains(returned));
        
        Set<String> identsToGet = new HashSet<String>();
        identsToGet.add(o.getIdent());
        Collection<AdvanceDTO> selectedAdvances = gameService.getAdvances(identsToGet);
        Assert.assertTrue("There Is Only One Advance Returned", selectedAdvances.size()==1);
        Assert.assertTrue("Advance Is In getAll Method Results",selectedAdvances.contains(returned));
    }
    @Test
    public void testCityImprovementMethods() {
       
        assertNotNull(gameService);
        gameService.init();
        CityImprovementDTO o = new CityImprovementDTO();
        o.setIdent("ident");
        gameService.insert(o);
        
        CityImprovementDTO fromSession = null;
        QueryResults results = loader.getKieSession().getQueryResults("getCityImprovement", new Object[]{o.getIdent()});
        for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            fromSession = (CityImprovementDTO)row.get("$cityImprovement"); 
        } 
        Assert.assertTrue("CityImprovement Placed As Fact Into KieSession",fromSession!=null);
     
        
        // get advance back
        CityImprovementDTO returned = gameService.getCityImprovement(o.getIdent());
        
        Assert.assertTrue("CityImprovements Returned From KieSession and GameService Are The Same",returned.equals(fromSession));
        
        Collection<CityImprovementDTO> all = gameService.getCityImprovements();
        Assert.assertTrue("There Is Only One CityImprovement In Session", all.size()==1);
        Assert.assertTrue("CityImprovement Is In getAll Method Results",all.contains(returned));
        
        Set<String> identsToGet = new HashSet<String>();
        identsToGet.add(o.getIdent());
        Collection<CityImprovementDTO> selected = gameService.getCityImprovements(identsToGet);
        Assert.assertTrue("There Is Only One CityImprovement Returned", selected.size()==1);
        Assert.assertTrue("CityImprovement Is In getAll Method Results",selected.contains(returned));
    }
    @Test
    public void testGovernmentMethods() {
       
        assertNotNull(gameService);
        gameService.init();
        GovernmentDTO o = new GovernmentDTO();
        o.setIdent("ident");
        gameService.insert(o);
        
        GovernmentDTO fromSession = null;
        QueryResults results = loader.getKieSession().getQueryResults("getGovernment", new Object[]{o.getIdent()});
        for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            fromSession = (GovernmentDTO)row.get("$government"); 
        } 
        Assert.assertTrue("Government Placed As Fact Into KieSession",fromSession!=null);
     
        
        // get advance back
        GovernmentDTO returned = gameService.getGovernment(o.getIdent());
        
        Assert.assertTrue("Governments Returned From KieSession and GameService Are The Same",returned.equals(fromSession));
        
        Collection<GovernmentDTO> all = gameService.getGovernments();
        Assert.assertTrue("There Is Only One Government In Session", all.size()==1);
        Assert.assertTrue("Government Is In getAll Method Results",all.contains(returned));
        
        Set<String> identsToGet = new HashSet<String>();
        identsToGet.add(o.getIdent());
        Collection<GovernmentDTO> selected = gameService.getGovernments(identsToGet);
        Assert.assertTrue("There Is Only One Government Returned", selected.size()==1);
        Assert.assertTrue("Government Is In getAll Method Results",selected.contains(returned));
    }
    @Test
    public void testSpecialMethods() {
       
        assertNotNull(gameService);
        gameService.init();
        SpecialDTO o = new SpecialDTO();
        o.setIdent("ident");
        gameService.insert(o);
        
        SpecialDTO fromSession = null;
        QueryResults results = loader.getKieSession().getQueryResults("getSpecial", new Object[]{o.getIdent()});
        for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            fromSession = (SpecialDTO)row.get("$special"); 
        } 
        Assert.assertTrue("Special Placed As Fact Into KieSession",fromSession!=null);
     
        
        // get advance back
        SpecialDTO returned = gameService.getSpecial(o.getIdent());
        
        Assert.assertTrue("Specials Returned From KieSession and GameService Are The Same",returned.equals(fromSession));
        
        Collection<SpecialDTO> all = gameService.getSpecials();
        Assert.assertTrue("There Is Only One Special In Session", all.size()==1);
        Assert.assertTrue("Special Is In getAll Method Results",all.contains(returned));
        
        Set<String> identsToGet = new HashSet<String>();
        identsToGet.add(o.getIdent());
        Collection<SpecialDTO> selected = gameService.getSpecials(identsToGet);
        Assert.assertTrue("There Is Only One Special Returned", selected.size()==1);
        Assert.assertTrue("Special Is In getAll Method Results",selected.contains(returned));
    }
    @Test
    public void testTerrainMethods() {
       
        assertNotNull(gameService);
        gameService.init();
        TerrainDTO o = new TerrainDTO();
        o.setIdent("ident");
        gameService.insert(o);
        
        TerrainDTO fromSession = null;
        QueryResults results = loader.getKieSession().getQueryResults("getTerrain", new Object[]{o.getIdent()});
        for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            fromSession = (TerrainDTO)row.get("$terrain"); 
        } 
        Assert.assertTrue("Terrain Placed As Fact Into KieSession",fromSession!=null);
     
        
        // get advance back
        TerrainDTO returned = gameService.getTerrain(o.getIdent());
        
        Assert.assertTrue("Terrains Returned From KieSession and GameService Are The Same",returned.equals(fromSession));
        
        Collection<TerrainDTO> all = gameService.getTerrains();
        Assert.assertTrue("There Is Only One Terrain In Session", all.size()==1);
        Assert.assertTrue("Terrain Is In getAll Method Results",all.contains(returned));
        
        Set<String> identsToGet = new HashSet<String>();
        identsToGet.add(o.getIdent());
        Collection<TerrainDTO> selected = gameService.getTerrains(identsToGet);
        Assert.assertTrue("There Is Only One Terrain Returned", selected.size()==1);
        Assert.assertTrue("Terrain Is In getAll Method Results",selected.contains(returned));
    }
    @Test
    public void testTileImprovementMethods() {
       
        assertNotNull(gameService);
        gameService.init();
        TileImprovementDTO o = new TileImprovementDTO();
        o.setIdent("ident");
        gameService.insert(o);
        
        TileImprovementDTO fromSession = null;
        QueryResults results = loader.getKieSession().getQueryResults("getTileImprovement", new Object[]{o.getIdent()});
        for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            fromSession = (TileImprovementDTO)row.get("$tileImprovement"); 
        } 
        Assert.assertTrue("TileImprovement Placed As Fact Into KieSession",fromSession!=null);
     
        
        // get advance back
        TileImprovementDTO returned = gameService.getTileImprovement(o.getIdent());
        
        Assert.assertTrue("TileImprovements Returned From KieSession and GameService Are The Same",returned.equals(fromSession));
        
        Collection<TileImprovementDTO> all = gameService.getTileImprovements();
        Assert.assertTrue("There Is Only One TileImprovement In Session", all.size()==1);
        Assert.assertTrue("TileImprovement Is In getAll Method Results",all.contains(returned));
        
        Set<String> identsToGet = new HashSet<String>();
        identsToGet.add(o.getIdent());
        Collection<TileImprovementDTO> selected = gameService.getTileImprovements(identsToGet);
        Assert.assertTrue("There Is Only One TileImprovement Returned", selected.size()==1);
        Assert.assertTrue("TileImprovement Is In getAll Method Results",selected.contains(returned));
    }
    @Test
    public void testUnitTypeMethods() {
       
        assertNotNull(gameService);
        gameService.init();
        UnitTypeDTO o = new UnitTypeDTO();
        o.setIdent("ident");
        gameService.insert(o);
        
        UnitTypeDTO fromSession = null;
        QueryResults results = loader.getKieSession().getQueryResults("getUnitType", new Object[]{o.getIdent()});
        for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            fromSession = (UnitTypeDTO)row.get("$unitType"); 
        } 
        Assert.assertTrue("UnitType Placed As Fact Into KieSession",fromSession!=null);
     
        
        // get advance back
        UnitTypeDTO returned = gameService.getUnitType(o.getIdent());
        
        Assert.assertTrue("UnitTypes Returned From KieSession and GameService Are The Same",returned.equals(fromSession));
        
        Collection<UnitTypeDTO> all = gameService.getUnitTypes();
        Assert.assertTrue("There Is Only One UnitType In Session", all.size()==1);
        Assert.assertTrue("UnitType Is In getAll Method Results",all.contains(returned));
        
        Set<String> identsToGet = new HashSet<String>();
        identsToGet.add(o.getIdent());
        Collection<UnitTypeDTO> selected = gameService.getUnitTypes(identsToGet);
        Assert.assertTrue("There Is Only One UnitType Returned", selected.size()==1);
        Assert.assertTrue("UnitType Is In getAll Method Results",selected.contains(returned));
    }
    @Test
    public void testTileMethods() {
       
        assertNotNull(gameService);
        gameService.init();
        TileDTO o = new TileDTO();
        o.setId(1L);
        o.setPosX(1L);
    	o.setPosY(1L);
    	o.setImprovements(new HashSet<String>());
    	o.setTerrain("plains");
    	o.setDefenseBonus(0);
    	TerrainDTO tdto = new TerrainDTO();
    	tdto.setIdent("plains");
//    	loader.getKieSession().insert(o);
//    	loader.getKieSession().fireAllRules();
//    	gameService.insert(tdto);
        gameService.insert(o);
        
        TileDTO fromSession = null;
        QueryResults results = loader.getKieSession().getQueryResults("getTile", new Object[]{o.getId()});
        for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            fromSession = (TileDTO)row.get("$tile"); 
        } 
        Assert.assertTrue("Tile Placed As Fact Into KieSession",fromSession!=null);
     
        
        // get advance back
        TileDTO returned = gameService.getTile(o.getId());
        
        Assert.assertTrue("Tiles Returned From KieSession and GameService Are The Same",returned.equals(fromSession));
        
        Collection<TileDTO> all = gameService.getTiles();
        Assert.assertTrue("There Is Only One Tile In Session", all.size()==1);
        Assert.assertTrue("Tile Is In getAll Method Results",all.contains(returned));
        
        Set<Long> identsToGet = new HashSet<Long>();
        identsToGet.add(o.getId());
        Collection<TileDTO> selected = gameService.getTiles(identsToGet);
        Assert.assertTrue("There Is Only One Tile Returned", selected.size()==1);
        Assert.assertTrue("Tile Is In getAll Method Results",selected.contains(returned));
    }
    @Test
    public void testUnitMethods() {
       
        assertNotNull(gameService);
        gameService.init();
        UnitDTO o = new UnitDTO();
        o.setId(1L);
        o.setType("phalanx");
        gameService.insert(o);
        
        UnitDTO fromSession = null;
        QueryResults results = loader.getKieSession().getQueryResults("getUnit", new Object[]{o.getId()});
        for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            fromSession = (UnitDTO)row.get("$unit"); 
        } 
        Assert.assertTrue("Unit Placed As Fact Into KieSession",fromSession!=null);
     
        
        // get advance back
        UnitDTO returned = gameService.getUnit(o.getId());
        
        Assert.assertTrue("Unit Returned From KieSession and GameService Are The Same",returned.equals(fromSession));
        
        Collection<UnitDTO> all = gameService.getUnits();
        Assert.assertTrue("There Is Only One Unit In Session", all.size()==1);
        Assert.assertTrue("Unit Is In getAll Method Results",all.contains(returned));
        
        Set<Long> identsToGet = new HashSet<Long>();
        identsToGet.add(o.getId());
        Collection<UnitDTO> selected = gameService.getUnits(identsToGet);
        Assert.assertTrue("There Is Only One Unit Returned", selected.size()==1);
        Assert.assertTrue("Unit Is In getAll Method Results",selected.contains(returned));
    }
    @Test
    public void testCityMethods() {
        assertNotNull(gameService);
        gameService.init();
        TileDTO centre = new TileDTO();
        centre.setId(1L);
        centre.setPosX(1L);
        centre.setPosY(1L);
        centre.setFoodProduction(0);
        centre.setResourcesProduction(0);
        centre.setTradeProduction(0);
        gameService.insert(centre);
        PlayerDTO player = new PlayerDTO();
        player.setId(1L);
        player.setGovernment("democracy");
        gameService.insert(player);
        CityDTO o = new CityDTO();
        o.setId(1L);
        o.setOwner(1L);
        o.setCityCentre(1L);
        o.setName("hej");
        o.getManagedTiles().add(1L);
        gameService.insert(o);
        
        CityDTO fromSession = null;
        QueryResults results = loader.getKieSession().getQueryResults("getCity", new Object[]{o.getId()});
        for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            fromSession = (CityDTO)row.get("$city"); 
        } 
        Assert.assertTrue("City Placed As Fact Into KieSession",fromSession!=null);
     
        
        // get advance back
        CityDTO returned = gameService.getCity(o.getId());
        
        Assert.assertTrue("Cities Returned From KieSession and GameService Are The Same",returned.equals(fromSession));
        
        Collection<CityDTO> all = gameService.getCities();
        Assert.assertTrue("There Is Only One City In Session", all.size()==1);
        Assert.assertTrue("City Is In getAll Method Results",all.contains(returned));
        
        Set<Long> idsToGet = new HashSet<Long>();
        idsToGet.add(o.getId());
        Collection<CityDTO> selected = gameService.getCities(idsToGet);
        Assert.assertTrue("There Is Only One City Returned", selected.size()==1);
        Assert.assertTrue("City Is In getAll Method Results",selected.contains(returned));
    }
   
    
    
    /*
     * helper methods
     */    
    protected void cleanupSingletonSessionId() {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        if (tempDir.exists()) {            
            String[] jbpmSerFiles = tempDir.list(new FilenameFilter() {                
                public boolean accept(File dir, String name) {                    
                    return name.endsWith("-jbpmSessionId.ser");
                }
            });
            for (String file : jbpmSerFiles) {
                
                new File(tempDir, file).delete();
            }
        }
    }
}
