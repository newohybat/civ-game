package cz.muni.fi.civ.newohybat.game;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.drools.core.event.DebugProcessEventListener;
import org.jboss.arquillian.junit.Arquillian;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.DebugAgendaEventListener;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.mockito.ArgumentCaptor;

import cz.muni.fi.civ.newohybat.drools.events.CityEvent;
import cz.muni.fi.civ.newohybat.drools.events.TurnEvent;
import cz.muni.fi.civ.newohybat.drools.events.UnitEvent;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.CityDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.PlayerDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.TileDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.UnitDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.UnitTypeDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.iface.CivBackend;

@RunWith(Arquillian.class)
public class BuildCityRulesJUnitTest extends BaseJUnitTest {
    /*
     * Test Of Building of a new city: action performed by a unit with this ability. Action lasts only till first
     * TurnEvent occurs.
     */
	@Inject
	private CivBackend cb;
    @Test
    public void testWaitForNewTurnToComplete(){
    	// add mock eventlistener to collect fired rules
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	// prepare test data
    	PlayerDTO player = getPlayer(1L, "honza");
    	player.setGovernment("despotism");
    	
    	TileDTO tile = getTile(1L, 1L, 1L, "plains", new HashSet<String>());
    	
    	UnitDTO unit = getUnit("phalanx", tile.getId());
    	unit.setOwner(player.getId());
    	
    	// the unit is now eligible to build city (actions are derived from UnitType, this is simplified)
    	Set<String> actions = new HashSet<String>();
    	actions.add("buildCity");
    	unit.setActions(actions);
    	
    	
    	UnitTypeDTO unitType = getUnitType(1L,"phalanx",150);
    	unitType.getActions().add("buildCity");
		// insert test data as facts
		game.insert(tile);
		game.insert(unitType);
		game.insert(player);
		
		game.insert(unit);
		
		// start the action after
    	game.unitBeginAction(unit.getId(), "buildCity");
		
		
		// get fired rules from afterMatchFired events
		ArgumentCaptor<AfterMatchFiredEvent> aafe = ArgumentCaptor.forClass( AfterMatchFiredEvent.class );
		verify( ael ,atLeastOnce()).afterMatchFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
		// rule which starts the buildCity process, see its definition in buildCityRules.drl
		Assert.assertTrue("Rule Unit Start Build City fired",firedRules.contains("Unit Start Build City"));
		
		
		// new turn occured, after that, new city should be on the same tile as unit
		ksession.getEntryPoint("GameControlStream").insert(new TurnEvent());
		ksession.fireAllRules();
		
		
		// query to get all cities of player. Originally player had no city, now should have one
		QueryResults results = ksession.getQueryResults("getAllCities", new Object[]{});
		Assert.assertTrue(results.size()==1);
		QueryResultsRow row = results.iterator().next(); 
        CityDTO city = (CityDTO)row.get("$city"); 
		
        Assert.assertNotNull("Created city is not null", city);
		
		Assert.assertNotNull("Created city has id set", city.getId());
        
		Assert.assertTrue("No Pending Processes Are There",ksession.getProcessInstances().size()==0);
	}
    
    /*
     * Tests canceling the creation of the city, before TurnEvent occurs. No change should be done.
     */
    @Test
    public void testCancel(){
    	ksession.addEventListener(new DebugAgendaEventListener());
    	// add mock eventlistener to collect fired rules
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	PlayerDTO player = getPlayer(1L, "honza");
    	player.setGovernment("despotism");
    	
    	TileDTO tile = getTile(1L, 1L, 1L, "plains", new HashSet<String>());
    	
    	UnitDTO unit = getUnit("phalanx", tile.getId());
    	unit.setOwner(player.getId());
    	
    	// add ability to create city
    	Set<String> actions = new HashSet<String>();
    	actions.add("buildCity");
    	unit.setActions(actions);
    	
    	
    	UnitTypeDTO unitType = getUnitType(1L,"phalanx",150);
    	unitType.getActions().add("buildCity");
    	
		// insert test data as facts
		game.insert(tile);
		game.insert(unitType);
		game.insert(player);
		
		game.insert(unit);
		
		game.unitBeginAction(unit.getId(), "buildCity");
		
		// get fired rules from afterMatchFired events
		ArgumentCaptor<AfterMatchFiredEvent> aafe = ArgumentCaptor.forClass( AfterMatchFiredEvent.class );
		verify( ael ,atLeastOnce()).afterMatchFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
		Assert.assertTrue("Rule Unit Start Build City fired",firedRules.contains("Unit Start Build City"));
		
		// cancel the process
		ksession.getEntryPoint("ActionCanceledStream").insert(new CityEvent(unit.getId()));
		ksession.fireAllRules();
		
	
		// next turn, which is after cancel - city shouldn't be created
		ksession.getEntryPoint("GameControlStream").insert(new TurnEvent());
		ksession.fireAllRules();
		
		// check that player still has no city
		QueryResults results = ksession.getQueryResults("getAllCities", new Object[]{});
		Assert.assertTrue(results.size()==0);
	}

    private static PlayerDTO getPlayer(Long id, String name){
		PlayerDTO player = new PlayerDTO();
		player.setId(id);
		player.setName(name);
		player.setLuxuriesRatio(0);
		player.setTaxesRatio(0);
		player.setResearchRatio(0);
		player.setResearch(0);
		return player;
	}
    private static UnitTypeDTO getUnitType(Long id, String ident, Integer cost){
    	UnitTypeDTO unitType = new UnitTypeDTO();
    	unitType.setIdent(ident);
    	unitType.setCost(cost);
    	unitType.setActions(new HashSet<String>());
    	return unitType;
    }
    private static UnitDTO getUnit(String type, Long pos){
    	UnitDTO unit = new UnitDTO();
    	unit.setId(3L);
    	unit.setType(type);
    	unit.setAttackStrength(0);
    	unit.setDefenseStrength(0);
    	unit.setTile(pos);
    	return unit;
    }
    private static TileDTO getTile(Long id,Long posX, Long posY, String terrain, Set<String> imps){
    	TileDTO tile = new TileDTO();
    	tile.setId(id);
    	tile.setPosX(posX);
    	tile.setPosY(posY);
    	tile.setImprovements(imps);
    	tile.setDefenseBonus(0);
    	tile.setTerrain(terrain);
    	tile.setFoodProduction(0);
    	tile.setResourcesProduction(0);
    	tile.setTradeProduction(0);
    	return tile;
    }
}