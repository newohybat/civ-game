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
import org.kie.api.event.rule.DebugAgendaEventListener;
import org.kie.api.event.rule.DebugRuleRuntimeEventListener;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.rule.FactHandle;
import org.mockito.ArgumentCaptor;

import cz.muni.fi.civ.newohybat.drools.events.TileImprovementEvent;
import cz.muni.fi.civ.newohybat.drools.events.TurnEvent;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.PlayerDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.TileDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.TileImprovementDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.UnitDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.UnitTypeDTO;

@RunWith(Arquillian.class)
public class BuildTileImprovementRulesJUnitTest extends BaseJUnitTest {
    /*
     * Test available action update after each move of unit. Some actions, like here buildIrrigation, are dependent
     * upon its surrounding. Tile can be irrigated when it is close to source of water.
     * In this case it is not possible.
     */
    @Test
    public void testCantIrrigateWhenCloseTilesAreNot(){
    	ksession.addEventListener(new DebugAgendaEventListener());
    	// Add mock eventlistener
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	// prepare test data
    	PlayerDTO player = getPlayer(11L, "honza");
    	UnitDTO unit = getUnit("phalanx",5L);
		unit.setOwner(player.getId());
		unit.getActions().add("buildIrrigation");
		// map definition - tiles without source of water
		TileDTO tile = getTile(5L, 5L,5L,"forest", new HashSet<String>());
		tile.setDefenseBonus(50); 
		tile.setPosX(45L);
		tile.setPosY(56L);
		TileDTO tile2 = getTile(6L, 3L,3L, "forest", new HashSet<String>());
		tile2.setDefenseBonus(50); 
		tile2.setPosX(45L);
		tile2.setPosY(57L);
		
		// insert test data as facts
		game.insert(player);
		game.insert(getTileImp("irrigation",1));
		game.insert(getUnitType("phalanx"));
		game.insert(tile);
		game.insert(tile2);
		
		game.insert(unit);
		
		unit = game.getUnit(unit.getId());
		Assert.assertFalse(unit.getActions().contains("buildIrrigation"));
		// this triggers the "Build Irrigation" rule when it is possible to process
		System.out.println("vcil");
		game.unitBeginAction(unit.getId(),"buildIrrigation");
		
		// get fired rules
		ArgumentCaptor<AfterMatchFiredEvent> aafe = ArgumentCaptor.forClass( AfterMatchFiredEvent.class );
		verify( ael ,atLeastOnce()).afterMatchFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		
		// rule "Build Irrigation" didn't fire
		Assert.assertFalse("Rule Build Irrigation Shouldn't Have Fired",firedRules.contains("Build Irrigation"));
		
	}
    /*
     * Tests that can build irrigation, when close tile is irrigated
     */
    @Test
    public void testCanIrrigateWhenOneCloseTileIs(){
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	
    	PlayerDTO player = getPlayer(11L, "honza");
    	TileDTO tile = getTile(3L, 3L,3L,"forest", new HashSet<String>());
		tile.setDefenseBonus(50);
		
		UnitDTO unit = getUnit("phalanx",tile.getId());
		unit.setOwner(player.getId());
		unit.getActions().add("buildIrrigation");
		// already irrigated tile
		TileDTO tile2 = getTile(4L,3L,4L, "plains", new HashSet<String>());
		tile2.getImprovements().add("irrigation");
		tile2.setDefenseBonus(0);
		// cost of improvement irrigation is two turns, so action will last 2 turns
		TileImprovementDTO irrigation = getTileImp("irrigation",2);
		
		// insert facts
		game.insert(tile);
		game.insert(tile2);
		game.insert(irrigation);
		game.insert(getUnitType("phalanx"));
		
		game.insert(player);
		
		game.insert(unit);
		

		// this triggers the "Build Irrigation" rule when it is possible to process
		game.unitBeginAction(unit.getId(), "buildIrrigation");
		
		
		// new turn
		ksession.getEntryPoint("GameControlStream").insert(new TurnEvent());
		ksession.fireAllRules();
		
		// new turn
		ksession.getEntryPoint("GameControlStream").insert(new TurnEvent());
		ksession.fireAllRules();
		
		// get fired rules
		ArgumentCaptor<AfterMatchFiredEvent> aafe = ArgumentCaptor.forClass( AfterMatchFiredEvent.class );
		verify( ael ,atLeastOnce()).afterMatchFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		// rule "Build Irrigation" did fire
		Assert.assertTrue("Build Irrigation Rule fired.",firedRules.contains("Build Irrigation"));
		Assert.assertTrue("Process Build Irrigation completed.",tile.getImprovements().contains("irrigation"));
		Assert.assertNull("Current action should change to null", unit.getCurrentAction());
    }
    /*
     * Tests that can build irrigation, when close tile is ocean
     */
    @Test
    public void testCanIrrigateWhenOneCloseTileIsOcean(){
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	
    	PlayerDTO player = getPlayer(11L, "honza");
    	TileDTO tile = getTile(3L, 3L,3L,"forest", new HashSet<String>());
		tile.setDefenseBonus(50);
		
		UnitDTO unit = getUnit("phalanx",tile.getId());
		unit.setOwner(player.getId());
		unit.getActions().add("buildIrrigation");
		// tile with ocean
		TileDTO tile2 = getTile(4L,3L,4L, "ocean", new HashSet<String>());
		tile2.setDefenseBonus(0);
		// cost of improvement irrigation is two turns, so action will last 2 turns
		TileImprovementDTO irrigation = getTileImp("irrigation",2);
		
		// insert facts
		game.insert(tile);
		game.insert(tile2);
		game.insert(irrigation);
		game.insert(getUnitType("phalanx"));
		
		game.insert(player);
		
		game.insert(unit);
		
		game.unitBeginAction(unit.getId(), "buildIrrigation");
		
		// new turn
		ksession.getEntryPoint("GameControlStream").insert(new TurnEvent());
		ksession.fireAllRules();
		
		// new turn
		ksession.getEntryPoint("GameControlStream").insert(new TurnEvent());
		ksession.fireAllRules();
		
		// get fired rules
		ArgumentCaptor<AfterMatchFiredEvent> aafe = ArgumentCaptor.forClass( AfterMatchFiredEvent.class );
		verify( ael ,atLeastOnce()).afterMatchFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		// rule "Build Irrigation" did fire
		Assert.assertTrue("Build Irrigation Rule fired.",firedRules.contains("Build Irrigation"));
		Assert.assertTrue("Process Build Irrigation completed.",tile.getImprovements().contains("irrigation"));
		Assert.assertNull("Current action should change to null", unit.getCurrentAction());
    }
    /*
     * Tests that can build irrigation, when close tile is river
     */
    @Test
    public void testCanIrrigateWhenOneCloseTileIsRiver(){
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	
    	PlayerDTO player = getPlayer(11L, "honza");
    	TileDTO tile = getTile(3L, 3L,3L,"forest", new HashSet<String>());
		tile.setDefenseBonus(50);
		
		UnitDTO unit = getUnit("phalanx",tile.getId());
		unit.setOwner(player.getId());
		unit.getActions().add("buildIrrigation");
		// tile with river
		TileDTO tile2 = getTile(4L,3L,4L, "rivers", new HashSet<String>());
		tile2.setDefenseBonus(0);
		// cost of improvement irrigation is two turns, so action will last 2 turns
		TileImprovementDTO irrigation = getTileImp("irrigation",2);
		
		// insert facts
		game.insert(tile);
		game.insert(tile2);
		game.insert(irrigation);
		game.insert(getUnitType("phalanx"));
		
		game.insert(player);
		
		game.insert(unit);
		
		// this starts the process of build irrigation
		game.unitBeginAction(unit.getId(), "buildIrrigation");
		
		// simulate the two turns to complete action
		// new turn
		ksession.getEntryPoint("GameControlStream").insert(new TurnEvent());
		ksession.fireAllRules();
		
		// new turn
		ksession.getEntryPoint("GameControlStream").insert(new TurnEvent());
		ksession.fireAllRules();
		
		// get fired rules
		ArgumentCaptor<AfterMatchFiredEvent> aafe = ArgumentCaptor.forClass( AfterMatchFiredEvent.class );
		verify( ael ,atLeastOnce()).afterMatchFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		// rule "Build Irrigation" did fire
		Assert.assertTrue("Build Irrigation Rule fired.",firedRules.contains("Build Irrigation"));
		Assert.assertTrue("Process Build Irrigation completed.",tile.getImprovements().contains("irrigation"));
		Assert.assertNull("Current action should change to null", unit.getCurrentAction());
    }
    @Test
    public void testCancelIrrigation(){
    	AgendaEventListener ael = mock( AgendaEventListener.class );
    	ksession.addEventListener( ael );
    	
    	// prepare test data
    	
    	PlayerDTO player = getPlayer(11L, "honza");
    	TileDTO tile = getTile(3L, 3L,3L,"forest", new HashSet<String>());
		tile.setDefenseBonus(50);
		
		UnitDTO unit = getUnit("phalanx",tile.getId());
		unit.setOwner(player.getId());
		unit.getActions().add("buildIrrigation");
		unit.setCurrentAction("buildIrrigation");
		// tile with river
		TileDTO tile2 = getTile(4L,3L,4L, "rivers", new HashSet<String>());
		tile2.setDefenseBonus(0);
		// cost of improvement irrigation is two turns, so action will last 2 turns
		TileImprovementDTO irrigation = getTileImp("irrigation",2);
		
		// insert facts
		game.insert(tile);
		game.insert(tile2);
		game.insert(irrigation);
		game.insert(getUnitType("phalanx"));
		
		game.insert(player);
		
		game.insert(unit);
		
		// this starts the process of build irrigation
		game.unitBeginAction(unit.getId(), "buildIrrigation");
		
		
		// simulate the two turns to complete action
		// new turn
		ksession.getEntryPoint("GameControlStream").insert(new TurnEvent());
		ksession.fireAllRules();
		
		// cancel the action of unit
		ksession.getEntryPoint("ActionCanceledStream").insert(new TileImprovementEvent(unit.getId()));
		ksession.fireAllRules();
		
		// new turn
		ksession.getEntryPoint("GameControlStream").insert(new TurnEvent());
		ksession.fireAllRules();
		
		
		// get fired rules
		ArgumentCaptor<AfterMatchFiredEvent> aafe = ArgumentCaptor.forClass( AfterMatchFiredEvent.class );
		verify( ael ,atLeastOnce()).afterMatchFired( aafe.capture() );
		List<String> firedRules = getFiredRules(aafe.getAllValues());
		// rule "Build Irrigation" did fire
		Assert.assertTrue("Build Irrigation Rule fired.",firedRules.contains("Build Irrigation"));
		Assert.assertFalse("Process Build Irrigation canceled.",tile.getImprovements().contains("irrigation"));
		Assert.assertNull("Current action should change to null", unit.getCurrentAction());
    }
    private static UnitDTO getUnit(String type, Long pos){
    	UnitDTO unit = new UnitDTO();
    	unit.setId(3L);
    	unit.setType(type);
    	unit.setAttackStrength(0);
    	unit.setDefenseStrength(0);
    	unit.setTile(pos);
    	unit.setActions(new HashSet<String>());
    	return unit;
    }
    private static TileDTO getTile(Long id,Long posX, Long posY, String terrain, Set<String> imps){
    	TileDTO tile = new TileDTO();
    	tile.setId(id);
    	tile.setPosX(posX);
    	tile.setPosY(posY);
    	tile.setImprovements(imps);
    	tile.setTerrain(terrain);
    	return tile;
    }
    private static PlayerDTO getPlayer(Long id, String name){
    	PlayerDTO player = new PlayerDTO();
    	player.setId(id);
    	player.setName(name);
    	return player;
    }
    private static UnitTypeDTO getUnitType(String ident){
    	UnitTypeDTO type = new UnitTypeDTO();
		type.setIdent(ident);
		Set<String> actions = new HashSet<String>();
		actions.add("buildIrrigation");
		type.setActions(actions);
		return type;
    }
    private static TileImprovementDTO getTileImp(String ident, Integer cost){
    	TileImprovementDTO imp = new TileImprovementDTO();
    	imp.setIdent(ident);
    	imp.setCost(cost);
    	return imp;
    }
}