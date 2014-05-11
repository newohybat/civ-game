package cz.muni.fi.civ.newohybat.game;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.drools.core.base.RuleNameEqualsAgendaFilter;
import org.drools.core.impl.StatefulKnowledgeSessionImpl;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.DebugAgendaEventListener;
import org.kie.api.event.rule.DebugRuleRuntimeEventListener;
import org.kie.api.runtime.process.ProcessInstance;
import org.mockito.ArgumentCaptor;

import cz.muni.fi.civ.newohybat.persistence.facade.dto.CityDTO;

@RunWith(Arquillian.class)
public class CityMoodRulesJUnitTest extends BaseJUnitTest {
	@Test
    public void testCityNeitherWeLoveDayNorDisorder(){
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	
    	CityDTO city = getCity(5L,"marefy");
		city.setPeopleHappy(5);
		city.setPeopleContent(6);
		city.setPeopleUnhappy(1);
		city.setPeopleEntertainers(0);
		city.setPeopleScientists(0);
		city.setPeopleTaxmen(0);
		city.setWeLoveDay(false);
		city.setDisorder(false);
		city.setSize(12);
		// insert facts
		game.insert(city);
		
		// activate rule flow group and filter triggered rule
		((StatefulKnowledgeSessionImpl)ksession).session.getAgenda().activateRuleFlowGroup("cityMood");
		ksession.fireAllRules();
				
		
		ArgumentCaptor<AfterMatchFiredEvent> aafe = ArgumentCaptor.forClass( AfterMatchFiredEvent.class );
		verify( ael ,atLeastOnce()).afterMatchFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
		Assert.assertFalse("Get City Disorder Not Fired", firedRules.contains("Get City Disorder"));
		Assert.assertFalse("Get City We Love Ruler Day Not Fired", firedRules.contains("Get City We Love Ruler Day"));
		
		Assert.assertFalse("City Is Not In Disorder.",city.getDisorder());
		Assert.assertFalse("City Is Not Celebrating We Love Day.", city.getWeLoveDay());
    }
	@Test
    public void testCityWeLoveDay(){
    	
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	
    	CityDTO city = getCity(5L,"marefy");
		city.setPeopleHappy(5);
		city.setPeopleContent(3);
		city.setPeopleUnhappy(0);
		city.setPeopleEntertainers(0);
		city.setPeopleScientists(0);
		city.setPeopleTaxmen(0);
		city.setWeLoveDay(false);
		city.setDisorder(false);
		city.setSize(8);
		// insert facts
		game.insert(city);
		
		// activate rule flow group and filter triggered rule
		((StatefulKnowledgeSessionImpl)ksession).session.getAgenda().activateRuleFlowGroup("cityMood");
		ksession.fireAllRules();
				
		
		ArgumentCaptor<AfterMatchFiredEvent> aafe = ArgumentCaptor.forClass( AfterMatchFiredEvent.class );
		verify( ael ,atLeastOnce()).afterMatchFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
		Assert.assertFalse("Get City Disorder Not Fired", firedRules.contains("Get City Disorder"));
		Assert.assertTrue("Get City We Love Ruler Day Fired", firedRules.contains("Get City We Love Ruler Day"));
		Assert.assertFalse("City Is Not In Disorder.",city.getDisorder());
		Assert.assertTrue("City Is Celebrating We Love Day.", city.getWeLoveDay());
    }
	@Test
    public void testCityDisorder(){
    	
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	
    	CityDTO city = getCity(5L,"marefy");
		city.setPeopleHappy(5);
		city.setPeopleContent(3);
		city.setPeopleUnhappy(6);
		city.setPeopleEntertainers(0);
		city.setPeopleScientists(0);
		city.setPeopleTaxmen(0);
		city.setWeLoveDay(false);
		city.setDisorder(false);
		city.setSize(14);
		
		// insert facts
		game.insert(city);
		// activate rule flow group and filter triggered rule
		((StatefulKnowledgeSessionImpl)ksession).session.getAgenda().activateRuleFlowGroup("cityMood");
		ksession.fireAllRules();
		// get fired rules
		ArgumentCaptor<AfterMatchFiredEvent> aafe = ArgumentCaptor.forClass( AfterMatchFiredEvent.class );
		verify( ael ,atLeastOnce()).afterMatchFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
		Assert.assertTrue("Get City Disorder Fired", firedRules.contains("Get City Disorder"));
		Assert.assertFalse("Get City We Love Ruler Day Not Fired", firedRules.contains("Get City We Love Ruler Day"));
		
		
		Assert.assertTrue("City Is In Disorder.",city.getDisorder());
		Assert.assertFalse("City Is  Not Celebrating We Love Day.", city.getWeLoveDay());
    }

	private static CityDTO getCity(Long id, String name){
		CityDTO city = new CityDTO();
		city.setId(id);
		city.setName(name);
		city.setResourcesConsumption(0);
		city.setResourcesProduction(0);
		city.setUnitsSupport(0);
		city.setFoodConsumption(0);
		city.setSize(0);
		city.setTradeProduction(0);
		return city;
	}

}