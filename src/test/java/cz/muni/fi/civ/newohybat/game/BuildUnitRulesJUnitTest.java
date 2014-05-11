package cz.muni.fi.civ.newohybat.game;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

import cz.muni.fi.civ.newohybat.drools.events.TurnEvent;
import cz.muni.fi.civ.newohybat.drools.events.UnitEvent;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.CityDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.UnitDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.UnitTypeDTO;

@RunWith(Arquillian.class)
public class BuildUnitRulesJUnitTest extends BaseJUnitTest {
 
	/**
	 * Tests whether can finish unit creation when has enough resources after one turn.
	 * Process is started by setting the currentUnit property of a CityDTO object.
	 */
	@Test
    public void testWaitForNewTurnToComplete(){
    	// add mock event listener to collect fired rules
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	CityDTO city = getCity(1L, "marefy");
    	Set<String> improvements = new HashSet<String>();
    	city.setImprovements(improvements);
    	
    	// this type of unit will be created and triggers the process
    	city.setResourcesSurplus(300);
    	
    	UnitTypeDTO unitType = getUnitType(1L,"phalanx",150);
    	
		// insert test data as facts
		game.insert(unitType);
		game.insert(city);
		
		game.cityBeginUnit(city.getId(), unitType.getIdent());
		
		// get active processes
		List<ProcessInstance> processes = (List<ProcessInstance>)ksession.getProcessInstances();
		
		
		// get fired rules
		ArgumentCaptor<AfterMatchFiredEvent> aafe = ArgumentCaptor.forClass( AfterMatchFiredEvent.class );
		verify( ael ,atLeastOnce()).afterMatchFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
		// test whether process started
		Assert.assertTrue("City Start Build Unit Rule Fired",firedRules.contains("City Start Build Unit"));
		// and process is placed in working memory
		Assert.assertTrue("One Process Should Be Active",processes.size()==1);
		// get the process
		Long pId = processes.get(0).getId();
		
		// process is active, it waits for a TurnEvent
		assertProcessInstanceActive(pId, ksession);
		
		// signal TurnEvent
		ksession.getEntryPoint("GameControlStream").insert(new TurnEvent());
		ksession.fireAllRules();
		
		// Process waited only for one turn
		assertProcessInstanceCompleted(pId, ksession);
		
		Set<Long> units = city.getHomeUnits();
		Assert.assertTrue("City Has New Unit.",units.size()==1);
		
		// get new unit
		QueryResults results = ksession.getQueryResults("getUnit", new Object[]{units.iterator().next()});
		Assert.assertTrue("Unit Is Within ksession",results.size()==1);
		QueryResultsRow row = results.iterator().next(); 
        UnitDTO unit = (UnitDTO)row.get("$unit"); 
		Assert.assertNotNull("Unit Is Not Null",unit);
		
		Assert.assertNotNull("Unit Has Assigned Id",unit.getId());
        
		Assert.assertTrue("No Pending Processes Are There",ksession.getProcessInstances().size()==0);
		
		Assert.assertTrue("City Current Unit Null", city.getCurrentUnit()==null);
	}
	
	@Test
    public void testTwoConsecutiveUnits(){
    	// add mock event listener to collect fired rules
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	CityDTO city = getCity(1L, "marefy");
    	Set<String> improvements = new HashSet<String>();
    	city.setImprovements(improvements);
    	
    	// this type of unit will be created and triggers the process
    	city.setResourcesSurplus(300);
    	
    	UnitTypeDTO unitType = getUnitType(1L,"phalanx",150);
    	
		// insert test data as facts
		game.insert(unitType);
		game.insert(city);
		
		
		game.cityBeginUnit(city.getId(), unitType.getIdent());
		
		// get fired rules
		ArgumentCaptor<AfterMatchFiredEvent> aafe = ArgumentCaptor.forClass( AfterMatchFiredEvent.class );
		verify( ael ,atLeastOnce()).afterMatchFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
		// test whether process started
		Assert.assertTrue("City Start Build Unit Rule Fired",firedRules.contains("City Start Build Unit"));
		
		// signal TurnEvent
		ksession.getEntryPoint("GameControlStream").insert(new TurnEvent());
		ksession.fireAllRules();
		
		Set<Long> units = city.getHomeUnits();
		Assert.assertTrue("City Has New Unit.",units.size()==1);
		
		// get new unit
		UnitDTO result = game.getUnit(units.iterator().next());
		Assert.assertNotNull("Unit Is Within ksession",result);
		
		Assert.assertNotNull("Unit Has Assigned Id",result.getId());
        
		Assert.assertTrue("City Current Unit Null", city.getCurrentUnit()==null);
		
		
		city.setResourcesSurplus(300);
		ksession.update(ksession.getFactHandle(city),city);
		
		game.cityBeginUnit(city.getId(), unitType.getIdent());
		
		// get fired rules
		ArgumentCaptor<AfterMatchFiredEvent> aafe2 = ArgumentCaptor.forClass( AfterMatchFiredEvent.class );
		verify( ael ,atLeastOnce()).afterMatchFired( aafe2.capture() );
		List<String> firedRules2 = getFiredRules(aafe2.getAllValues());
		
		// test whether process started
		Assert.assertTrue("City Start Build Unit Rule Fired",firedRules2.contains("City Start Build Unit"));
		
		// signal TurnEvent
		ksession.getEntryPoint("GameControlStream").insert(new TurnEvent());
		ksession.fireAllRules();
		
		Set<Long> units2 = city.getHomeUnits();
		Assert.assertTrue("City Has Two New Units.",units2.size()==2);
		
		// get new unit
		Collection<UnitDTO> results= game.getUnits(units2);
		Assert.assertTrue("Units Are Within ksession",results.size()==2);
		
		Assert.assertTrue("City Current Unit Null", city.getCurrentUnit()==null);
	}
    
    /*
     * Tests cancel signal sent to active process.
     */
    @Test
    public void testCancelUnit(){
    	// Add mock eventlistener to collect fired rules
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	CityDTO city = getCity(1L, "marefy");
    	Set<String> improvements = new HashSet<String>();
    	city.setImprovements(improvements);
    	city.setResourcesSurplus(300);
    	
    	UnitTypeDTO unitType = getUnitType(1L,"phalanx",150);
		// insert test data as facts
		game.insert(unitType);
		
		game.insert(city);
		

    	// this unit will be created, trigger of the process
    	game.cityBeginUnit(city.getId(), "phalanx");
    	
		
		// afterMatchFired events contain information about fired rules
		ArgumentCaptor<AfterMatchFiredEvent> aafe = ArgumentCaptor.forClass( AfterMatchFiredEvent.class );
		verify( ael ,atLeastOnce()).afterMatchFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
		Assert.assertTrue("Rule City Start Build Unit Fired",firedRules.contains("City Start Build Unit"));
		
		// cancel the waiting process
		ksession.getEntryPoint("ActionCanceledStream").insert(new UnitEvent(city.getId()));
		ksession.fireAllRules();
		
		Assert.assertFalse("City Has Not Any Unit.",city.getHomeUnits().size()>0);
		
	}
    /*
     * Same as testWaitForNewTurnToComplete with one difference: it has to wait for three turns 
     * to gain sufficient resources.
     */
    @Test
    public void testCompleteAfterThreeTurns(){
    	// Add mock eventlistener
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	// prepare test data
    	CityDTO city = getCity(1L, "marefy");
    	Set<String> improvements = new HashSet<String>();
    	city.setImprovements(improvements);
    	city.setResourcesSurplus(60);
    	UnitTypeDTO unitType = getUnitType(1L,"phalanx",150);
    	
		// insert test data as facts
    	game.insert(unitType);
		game.insert(city);
		
    	// this unit will be created, trigger of the process
    	game.cityBeginUnit(city.getId(), "phalanx");
	
		
		ArgumentCaptor<AfterMatchFiredEvent> aafe = ArgumentCaptor.forClass( AfterMatchFiredEvent.class );
		verify( ael ,atLeastOnce()).afterMatchFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
		Assert.assertTrue(firedRules.contains("City Start Build Unit"));
		
		//fst turn
		ksession.getEntryPoint("GameControlStream").insert(new TurnEvent());
		ksession.fireAllRules();
		
		// simulate production update on new turn
		city.setResourcesSurplus(60);
		ksession.update(ksession.getFactHandle(city), city);
		// snd turn
		ksession.getEntryPoint("GameControlStream").insert(new TurnEvent());
		ksession.fireAllRules();
		
		
		// simulate production update on new turn
		city.setResourcesSurplus(60);
		ksession.update(ksession.getFactHandle(city), city);
		
		// third turn
		ksession.getEntryPoint("GameControlStream").insert(new TurnEvent());
		ksession.fireAllRules();
		
		
		Set<Long> units = city.getHomeUnits();
		Assert.assertTrue("City Has New Unit.",units.size()==1);
		
		// get new unit
		UnitDTO result = game.getUnit(units.iterator().next());
		Assert.assertNotNull("Unit Is Within ksession",result);
		
		Assert.assertNotNull("Unit Has Assigned Id",result.getId());
        
		Assert.assertTrue("City Current Unit Null", city.getCurrentUnit()==null);
		
		Assert.assertTrue("City Contains Courthouse Improvement.",city.getHomeUnits().size()==1);
		
		Assert.assertTrue("Created Unit Is Of Given Type.",result.getType()==unitType.getIdent());
		
	}

	private static CityDTO getCity(Long id, String name){
		CityDTO city = new CityDTO();
    	city.setId(id);
    	city.setName(name);
    	city.setResourcesConsumption(0);
    	city.setResourcesProduction(0);
    	city.setUnitsSupport(0);
    	city.setFoodConsumption(0);
    	city.setFoodProduction(0);
    	city.setFoodStock(0);
    	city.setSize(0);
    	city.setTradeProduction(0);
    	city.setPeopleEntertainers(0);
		city.setPeopleScientists(0);
		city.setPeopleTaxmen(0);
		city.setWeLoveDay(false);
		city.setDisorder(false);
		city.setSize(1);
		city.setPeopleHappy(0);
		city.setPeopleContent(0);
		city.setPeopleUnhappy(0);
		city.setImprovements(new HashSet<String>());
		city.setHomeUnits(new HashSet<Long>());
    	return city;
	}
    private static UnitTypeDTO getUnitType(Long id, String ident, Integer cost){
    	UnitTypeDTO unitType = new UnitTypeDTO();
    	unitType.setIdent(ident);
    	unitType.setCost(cost);
    	unitType.setActions(new HashSet<String>());
    	return unitType;
    }
}