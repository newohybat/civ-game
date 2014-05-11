package cz.muni.fi.civ.newohybat.game;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.api.runtime.rule.FactHandle;
import org.mockito.ArgumentCaptor;

import cz.muni.fi.civ.newohybat.drools.events.CityImprovementEvent;
import cz.muni.fi.civ.newohybat.drools.events.TurnEvent;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.AdvanceDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.CityDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.CityImprovementDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.PlayerDTO;

@RunWith(Arquillian.class)
public class BuildCityImprovementRulesJUnitTest extends BaseJUnitTest {
	/*
	 * Tests build of CityImprovement.
	 * One TurnEvent is needed to complete, because city has sufficient production
	 */
	@Test
    public void testWaitForNewTurnToComplete(){
    	// Add mock eventlistener
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	// prepare test data
    	CityDTO city = getCity(1L, "marefy");
    	Set<String> improvements = new HashSet<String>();
    	improvements.add("granary");
    	improvements.add("bank");
    	city.setImprovements(improvements);
    	
    	// 300 resources to spent per turn
    	city.setResourcesSurplus(300);
    	Set<String> enabledImprovements = new HashSet<String>();
    	enabledImprovements.add("courthouse");
    	city.setEnabledImprovements(enabledImprovements);
    	// triggers the buildCityImprovement process
    	
    	// cost is less than available resources
    	CityImprovementDTO courtHouse = getImprovement("courthouse",250);
		// insert test data as facts
		game.insert(courtHouse);
		game.insert(city);
		
		game.cityBeginCityImprovement(city.getId(), courtHouse.getIdent());
		// get fired rules
		ArgumentCaptor<AfterMatchFiredEvent> aafe = ArgumentCaptor.forClass( AfterMatchFiredEvent.class );
		verify( ael ,atLeastOnce()).afterMatchFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
		Assert.assertTrue("Rule Build City Improvement Fired",firedRules.contains("Build City Improvement"));

		// New Turn occured
		ksession.getEntryPoint("GameControlStream").insert(new TurnEvent());
		ksession.fireAllRules();
		
		
		city = game.getCity(city.getId());
		
		Assert.assertTrue("City Contains Courthouse Improvement.",city.getImprovements().contains("courthouse"));
	}
	/*
	 * Tests the behaviour when two consecutive improvements are built
	 */
	@Test
    public void testTwoImprovementsConsecutive(){
    	// Add mock eventlistener
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	// prepare test data
    	CityDTO city = getCity(1L, "marefy");
    	Set<String> improvements = new HashSet<String>();
    	improvements.add("granary");
    	city.setImprovements(improvements);
    	
    	// 300 resources to spent per turn
    	city.setResourcesSurplus(300);
    	Set<String> enabledImprovements = new HashSet<String>();
    	enabledImprovements.add("courthouse");
    	enabledImprovements.add("bank");
    	city.setEnabledImprovements(enabledImprovements);
    	// triggers the buildCityImprovement process
    	city.setCurrentImprovement("courthouse");
    	
    	// cost is less than available resources
    	CityImprovementDTO bank = getImprovement("bank",100);
    	CityImprovementDTO courtHouse = getImprovement("courthouse",100);
		// insert test data as facts
		game.insert(courtHouse);
		game.insert(bank);
		game.insert(city);
		
		// get fired rules
		ArgumentCaptor<AfterMatchFiredEvent> aafe = ArgumentCaptor.forClass( AfterMatchFiredEvent.class );
		verify( ael ,atLeastOnce()).afterMatchFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
		Assert.assertTrue("Rule Build City Improvement Fired",firedRules.contains("Build City Improvement"));

		// New Turn occured
		ksession.getEntryPoint("GameControlStream").insert(new TurnEvent());
		ksession.fireAllRules();

		city = game.getCity(city.getId());
		
		Assert.assertTrue("City Contains Courthouse Improvement.",city.getImprovements().contains("courthouse"));

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		game.cityBeginCityImprovement(city.getId(), bank.getIdent());
		
		// New Turn occured
		ksession.getEntryPoint("GameControlStream").insert(new TurnEvent());
		ksession.fireAllRules();

		city = game.getCity(city.getId());
		
		Assert.assertTrue("City Contains Bank Improvement.",city.getImprovements().contains("bank"));
		Assert.assertTrue("City CurrentImprovement Is Null", city.getCurrentImprovement()==null);
	}
    
    @Test
    public void testCancelImprovement(){
    	// Add mock eventlistener
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	// prepare test data
    	CityDTO city = getCity(1L, "marefy");
    	Set<String> improvements = new HashSet<String>();
    	improvements.add("granary");
    	improvements.add("bank");
    	city.setImprovements(improvements);
    	city.setCurrentImprovement("courthouse");
    	Set<String> enabledImprovements = new HashSet<String>();
    	enabledImprovements.add("courthouse");
    	city.setEnabledImprovements(enabledImprovements);
    	
    	// enough to finish with first TurnEvent
    	city.setResourcesSurplus(300);
    	CityImprovementDTO courtHouse = getImprovement("courthouse",250);
    	
		// insert test data as facts
		game.insert(courtHouse);
		
		game.insert(city);
		
		// get fired rules
		ArgumentCaptor<AfterMatchFiredEvent> aafe = ArgumentCaptor.forClass( AfterMatchFiredEvent.class );
		verify( ael ,atLeastOnce()).afterMatchFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
		Assert.assertTrue("Rule Build City Improvement Fired",firedRules.contains("Build City Improvement"));
		
		// cancel the process
		ksession.getEntryPoint("ActionCanceledStream").insert(new CityImprovementEvent(city.getId()));
		ksession.fireAllRules();
		
		// New Turn occured late
		ksession.getEntryPoint("GameControlStream").insert(new TurnEvent());
		ksession.fireAllRules();
		
		city = game.getCity(city.getId());
		
		Assert.assertFalse("City Not Contains Courthouse Improvement.",city.getImprovements().contains("courthouse"));
		
		Assert.assertTrue("No Pending Processes Are There",ksession.getProcessInstances().size()==0);
	}
    @Test
    public void testCompleteAfterThreeTurns(){
    	// Add mock eventlistener
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	// prepare test data
    	CityDTO city = getCity(1L, "marefy");
    	Set<String> improvements = new HashSet<String>();
    	city.setImprovements(improvements);
    	// not sufficient to build it at once
    	city.setResourcesSurplus(100);
    	Set<String> enabledImprovements = new HashSet<String>();
    	enabledImprovements.add("courthouse");
    	city.setEnabledImprovements(enabledImprovements);
    	
    	CityImprovementDTO courtHouse = getImprovement("courthouse",251);
    	
		// insert test data as facts
    	game.insert(courtHouse);
		
		game.insert(city);
		
		game.cityBeginCityImprovement(city.getId(), courtHouse.getIdent());
		// get fired rules
		ArgumentCaptor<AfterMatchFiredEvent> aafe = ArgumentCaptor.forClass( AfterMatchFiredEvent.class );
		verify( ael ,atLeastOnce()).afterMatchFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
		
		Assert.assertTrue("Rule Build City Improvement Fired",firedRules.contains("Build City Improvement"));

		// New Turn occured
		ksession.getEntryPoint("GameControlStream").insert(new TurnEvent());
		ksession.fireAllRules();
		
		// not enough
		city = game.getCity(city.getId());
		city.setResourcesSurplus(100);
		ksession.update(ksession.getFactHandle(city), city);
		
		// New Turn occured
		ksession.getEntryPoint("GameControlStream").insert(new TurnEvent());
		ksession.fireAllRules();
		
		// not enough
		city = game.getCity(city.getId());
		city.setResourcesSurplus(100);
		ksession.update(ksession.getFactHandle(city), city);
		//game.insert(city);
		
		// New Turn occured
		ksession.getEntryPoint("GameControlStream").insert(new TurnEvent());
		ksession.fireAllRules();
		
		city = game.getCity(city.getId());
		
		Assert.assertTrue("No Pending Processes Are There",ksession.getProcessInstances().size()==0);
		
		Assert.assertTrue("City Contains Courthouse Improvement.",city.getImprovements().contains("courthouse"));
		
	}
    /*
     * Wonder can be built only once throughout the game. When it is built, all other attempts have to be stopped,
     * the possibility to built it should disappear (The linkage is through player's advances).
     */
    @Test
    public void testCancelWhenWonderBuilt(){
    	// Add mock eventlistener
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	// prepare test data
    	
    	CityDTO city = getCity(1L, "marefy");
    	Set<String> improvements = new HashSet<String>();
    	improvements.add("granary");
    	improvements.add("bank");
    	city.setImprovements(improvements);
    	city.setResourcesSurplus(300);
    	Set<String> enabledImprovements = new HashSet<String>();
    	enabledImprovements.add("colossus");
    	city.setEnabledImprovements(enabledImprovements);
    	// wants to build colossus
    	city.setCurrentImprovement("colossus");
    	
    	// already has colossus
    	CityDTO cityWithColossus = getCity(2L, "Rhodos");
    	Set<String> improvements2 = new HashSet<String>();
    	improvements2.add("colossus");
    	improvements2.add("bank");
    	cityWithColossus.setImprovements(improvements2);
    	
    	AdvanceDTO advance = new AdvanceDTO();
    	advance.setIdent("advance");
    	Set<String> enabledCityImprovements = new HashSet<String>();
    	enabledCityImprovements.add("colossus");
    	advance.setEnabledCityImprovements(enabledCityImprovements);
    	
    	PlayerDTO owner = getPlayer(1L, "honzik");
    	city.setOwner(owner.getId());
    	cityWithColossus.setOwner(owner.getId());
    	Set<String>advances = new HashSet<String>();
    	advances.add(advance.getIdent());
    	owner.setAdvances(advances);
    	
    	// finally define colossus as wonder
    	CityImprovementDTO wonder = getImprovement("colossus",250);
    	wonder.setWonder(true);
    	
		// insert test data as facts but not the cityWithColossus
    	game.insert(advance);
    	game.insert(wonder);
		
    	game.insert(owner);
    	game.insert(city);
		
		// get fired rules
		ArgumentCaptor<AfterMatchFiredEvent> aafe = ArgumentCaptor.forClass( AfterMatchFiredEvent.class );
		verify( ael ,atLeastOnce()).afterMatchFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
		Assert.assertTrue("Rule Build City Improvement Fired",firedRules.contains("Build City Improvement"));
		
		// insert now cityWithColossus, it should stop the process for city called city
		game.insert(cityWithColossus);
		
		// process is gone
		city=game.getCity(city.getId());
		
		Assert.assertFalse("City Not Contains Colossus.",city.getImprovements().contains("colossus"));
		
		Assert.assertFalse("Enabled Improvements Not Contains Colossus.",city.getEnabledImprovements().contains("colossus"));
		
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
    	return city;
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
    private static CityImprovementDTO getImprovement(String ident, Integer constructionCost){
    	CityImprovementDTO imp = new CityImprovementDTO();
    	imp.setConstructionCost(constructionCost);
    	imp.setIdent(ident);
    	return imp;
    }
    
}