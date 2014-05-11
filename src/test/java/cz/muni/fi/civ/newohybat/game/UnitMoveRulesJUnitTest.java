package cz.muni.fi.civ.newohybat.game;

import java.util.HashSet;
import java.util.Set;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.rule.FactHandle;

import cz.muni.fi.civ.newohybat.persistence.facade.dto.PlayerDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.TileDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.TileImprovementDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.UnitDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.UnitTypeDTO;

@RunWith(Arquillian.class)
public class UnitMoveRulesJUnitTest extends BaseJUnitTest {
	/*
	 * Tests dummy implementation of move rules. Move is processed in rectangles, first from current x position to target x position,
	 * after that from current y position, to target y position. All is based on the list of tiles, called path. This path
	 * decides how the unit would move. So to change move strategy, just prepare the path in different manner. Move is than done step
	 * by step, length of each step depends on how much is movementCost of the terrain of current tile and how many movementPoints has
	 * the unit. Speed is counted as movementCost/movementPoints all multiplied by a constant defining the movement time unit.
	 */
	@Test
    public void testPhalanxMoveInPositiveDirection(){
		UnitDTO unit = getUnit("phalanx",3L);
		UnitTypeDTO unitType = getUnitType("phalanx"); 
		ksession.insert(unitType);
		
		ksession.setGlobal("movementTimeUnit", 1);
		
		TileDTO tile1 = getTile(3L, 3L,3L,"plains", new HashSet<String>());
		TileDTO tile2 = getTile(4L, 3L,4L,"hills", new HashSet<String>());
		TileDTO tile3 = getTile(5L, 4L,3L,"plains", new HashSet<String>());
		TileDTO tile4 = getTile(6L, 4L,4L,"forest", new HashSet<String>());
		ksession.insert(tile1);
		ksession.insert(tile2);
		ksession.insert(tile3);
		ksession.insert(tile4);
		
		ksession.fireAllRules();
		
		unit.setTile(tile1.getId());
		unit.setTargetTile(tile4.getId());
		FactHandle unitHandle = ksession.insert(unit);
		ksession.fireAllRules();
		
		Set<String> actions = new HashSet<String>();
		actions.add("move");
		unit.setActions(actions);
		unit.setCurrentAction("move");
		ksession.update(unitHandle, unit);
		ksession.fireAllRules();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Assert.assertTrue("Unit Moved", unit.getTile()==tile4.getId());
		Assert.assertNull("Unit target tile is null",unit.getTargetTile());
	}
	/*
	 * Same as previous, but in different direction. In negative direction of axes.
	 */
	@Test
    public void testPhalanxMoveInNegativeDirection(){
		UnitDTO unit = getUnit("phalanx",3L);
		UnitTypeDTO unitType = getUnitType("phalanx"); 
		ksession.insert(unitType);
		
		ksession.setGlobal("movementTimeUnit", 1);
		
		TileDTO tile1 = getTile(3L, 3L,3L,"plains", new HashSet<String>());
		TileDTO tile2 = getTile(4L, 3L,4L,"hills", new HashSet<String>());
		TileDTO tile3 = getTile(5L, 4L,3L,"plains", new HashSet<String>());
		TileDTO tile4 = getTile(6L, 4L,4L,"forest", new HashSet<String>());
		ksession.insert(tile1);
		ksession.insert(tile2);
		ksession.insert(tile3);
		ksession.insert(tile4);
		
		ksession.fireAllRules();
		
		unit.setTile(tile4.getId());
		unit.setTargetTile(tile1.getId());
		FactHandle unitHandle = ksession.insert(unit);
		ksession.fireAllRules();
		
		Set<String> actions = new HashSet<String>();
		actions.add("move");
		unit.setActions(actions);
		unit.setCurrentAction("move");
		ksession.update(unitHandle, unit);
		ksession.fireAllRules();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Assert.assertTrue("Unit Moved", unit.getTile()==tile1.getId());
		Assert.assertNull("Unit target tile is null",unit.getTargetTile());
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
    	tile.setTerrain(terrain);
    	tile.setDefenseBonus(0);
    	tile.setMovementCost(1);
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