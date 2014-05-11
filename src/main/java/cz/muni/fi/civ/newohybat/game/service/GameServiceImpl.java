package cz.muni.fi.civ.newohybat.game.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.internal.runtime.StatefulKnowledgeSession;

import cz.muni.fi.civ.newohybat.drools.events.AdvanceEvent;
import cz.muni.fi.civ.newohybat.drools.events.CityImprovementEvent;
import cz.muni.fi.civ.newohybat.drools.events.MoveEvent;
import cz.muni.fi.civ.newohybat.drools.events.TileImprovementEvent;
import cz.muni.fi.civ.newohybat.drools.events.TurnEvent;
import cz.muni.fi.civ.newohybat.drools.events.UnitEvent;
import cz.muni.fi.civ.newohybat.game.init.PersistenceHelper;
import cz.muni.fi.civ.newohybat.game.init.PersistenceHelperImpl;
import cz.muni.fi.civ.newohybat.game.init.SessionLoader;
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
import cz.muni.fi.civ.newohybat.persistence.facade.iface.CivBackend;

@RequestScoped
@TransactionManagement(TransactionManagementType.CONTAINER)
public class GameServiceImpl implements GameService{
//	@Inject @ApplicationScoped
//	RuntimeManagerService rms;
//	
	@Inject
	@ApplicationScoped
	private SessionLoader loader;
	@Inject
	private PersistenceHelper persistenceHelper;
	
	private KieSession getSession(){
//		KieSession ksession = rms.getKieSession();
		return loader.getKieSession();
	}
	
	public void insert(Object o){
        KieSession ksession = getSession();
		FactHandle handle =ksession.getFactHandle(o);
		if(handle==null){
			ksession.insert(o);
		}else{
			ksession.update(handle, o);
		}
		ksession.fireAllRules();
		persistenceHelper.persistDirtyObjects();
	}

	public List<TileDTO> get(Long x){
		KieSession ksession = getSession();
		List<TileDTO> tiles= new ArrayList<TileDTO>();
		QueryResults results = ksession.getQueryResults("getTile", new Object[]{x,x});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            TileDTO tile = (TileDTO)row.get("$tile"); 
            tiles.add(tile);
         } 
		return tiles;
	}
	public void insertAll(Collection<? extends Object> objects) {
		KieSession ksession = getSession();
		for(Object o:objects){
			ksession.insert(o);
		}
		ksession.fireAllRules();
		persistenceHelper.persistDirtyObjects();
	}
	public AdvanceDTO getAdvance(String ident) {
		KieSession ksession = getSession();
		AdvanceDTO advance= new AdvanceDTO();
		QueryResults results = ksession.getQueryResults("getAdvance", new Object[]{ident});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            advance = (AdvanceDTO)row.get("$advance"); 
        } 
		return advance;
	}
	public Collection<AdvanceDTO> getAdvances(Collection<String> idents) {
        KieSession ksession = getSession();
		
		Collection<AdvanceDTO> advances= new ArrayList<AdvanceDTO>();
		QueryResults results = ksession.getQueryResults("getAdvances", new Object[]{idents});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            AdvanceDTO advance = (AdvanceDTO)row.get("$advance");
            advances.add(advance);
        } 
		return advances;
	}
	public Collection<AdvanceDTO> getAdvances() {
        KieSession ksession = getSession();
		Collection<AdvanceDTO> advances= new ArrayList<AdvanceDTO>();
		QueryResults results = ksession.getQueryResults("getAllAdvances", new Object[]{});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            AdvanceDTO advance = (AdvanceDTO)row.get("$advance");
            advances.add(advance);
        } 
		return advances;
	}
	public CityDTO getCity(Long id) {
        KieSession ksession = getSession();
		CityDTO city= new CityDTO();
		QueryResults results = ksession.getQueryResults("getCity", new Object[]{id});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            city = (CityDTO)row.get("$city"); 
        } 
		return city;
	}
	public Collection<CityDTO> getCities(Collection<Long> ids) {
        KieSession ksession = getSession();
		Collection<CityDTO> cities= new ArrayList<CityDTO>();
		QueryResults results = ksession.getQueryResults("getCities", new Object[]{ids});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            CityDTO city = (CityDTO)row.get("$city");
            cities.add(city);
        } 
		return cities;
	}
	public Collection<CityDTO> getCities() {
        KieSession ksession = getSession();
		Collection<CityDTO> cities= new ArrayList<CityDTO>();
		QueryResults results = ksession.getQueryResults("getAllCities", new Object[]{});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            CityDTO city = (CityDTO)row.get("$city");
            cities.add(city);
        } 
		return cities;
	}
	public CityImprovementDTO getCityImprovement(String ident) {
        KieSession ksession = getSession();
		CityImprovementDTO cityImprovement= new CityImprovementDTO();
		QueryResults results = ksession.getQueryResults("getCityImprovement", new Object[]{ident});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            cityImprovement = (CityImprovementDTO)row.get("$cityImprovement"); 
        } 
		return cityImprovement;
	}
	public Collection<CityImprovementDTO> getCityImprovements(
			Collection<String> idents) {
        KieSession ksession = getSession();
		Collection<CityImprovementDTO> cityImprovements= new ArrayList<CityImprovementDTO>();
		QueryResults results = ksession.getQueryResults("getCityImprovements", new Object[]{idents});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            CityImprovementDTO cityImprovement = (CityImprovementDTO)row.get("$cityImprovement"); 
            cityImprovements.add(cityImprovement);
        } 
		return cityImprovements;
	}
	public Collection<CityImprovementDTO> getCityImprovements() {
        KieSession ksession = getSession();
		Collection<CityImprovementDTO> cityImprovements= new ArrayList<CityImprovementDTO>();
		QueryResults results = ksession.getQueryResults("getAllCityImprovements", new Object[]{});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            CityImprovementDTO cityImprovement = (CityImprovementDTO)row.get("$cityImprovement"); 
            cityImprovements.add(cityImprovement);
        } 
		return cityImprovements;
	}
	public GovernmentDTO getGovernment(String ident) {
        KieSession ksession = getSession();
		GovernmentDTO government= new GovernmentDTO();
		QueryResults results = ksession.getQueryResults("getGovernment", new Object[]{ident});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            government = (GovernmentDTO)row.get("$government"); 
        } 
		return government;
	}
	public Collection<GovernmentDTO> getGovernments(Collection<String> idents) {
        KieSession ksession = getSession();
		Collection<GovernmentDTO> governments= new ArrayList<GovernmentDTO>();
		QueryResults results = ksession.getQueryResults("getGovernments", new Object[]{idents});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            GovernmentDTO government = (GovernmentDTO)row.get("$government");
            governments.add(government);
        } 
		return governments;
	}
	public Collection<GovernmentDTO> getGovernments() {
        KieSession ksession = getSession();
		Collection<GovernmentDTO> governments= new ArrayList<GovernmentDTO>();
		QueryResults results = ksession.getQueryResults("getAllGovernments", new Object[]{});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            GovernmentDTO government = (GovernmentDTO)row.get("$government");
            governments.add(government);
        } 
		return governments;
	}
	public PlayerDTO getPlayer(Long id) {
        KieSession ksession = getSession();
		PlayerDTO player= new PlayerDTO();
		QueryResults results = ksession.getQueryResults("getPlayer", new Object[]{id});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            player = (PlayerDTO)row.get("$player"); 
        } 
		return player;
	}
	public Collection<PlayerDTO> getPlayers(Collection<Long> ids) {
        KieSession ksession = getSession();
		Collection<PlayerDTO> players= new ArrayList<PlayerDTO>();
		QueryResults results = ksession.getQueryResults("getPlayers", new Object[]{ids});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            PlayerDTO player = (PlayerDTO)row.get("$player");
            players.add(player);
        } 
		return players;
	}
	public Collection<PlayerDTO> getPlayers() {
        KieSession ksession = getSession();
		Collection<PlayerDTO> players= new ArrayList<PlayerDTO>();
		QueryResults results = ksession.getQueryResults("getAllPlayers", new Object[]{});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            PlayerDTO player = (PlayerDTO)row.get("$player");
            players.add(player);
        } 
		return players;
	}
	public SpecialDTO getSpecial(String ident) {
        KieSession ksession = getSession();
		SpecialDTO special= new SpecialDTO();
		QueryResults results = ksession.getQueryResults("getSpecial", new Object[]{ident});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            special = (SpecialDTO)row.get("$special"); 
        } 
		return special;
	}
	public Collection<SpecialDTO> getSpecials(Collection<String> idents) {
        KieSession ksession = getSession();
		Collection<SpecialDTO> specials= new ArrayList<SpecialDTO>();
		QueryResults results = ksession.getQueryResults("getSpecials", new Object[]{idents});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            SpecialDTO special = (SpecialDTO)row.get("$special");
            specials.add(special);
        } 
		return specials;
	}
	public Collection<SpecialDTO> getSpecials() {
        KieSession ksession = getSession();
		Collection<SpecialDTO> specials= new ArrayList<SpecialDTO>();
		QueryResults results = ksession.getQueryResults("getAllSpecials", new Object[]{});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            SpecialDTO special = (SpecialDTO)row.get("$special");
            specials.add(special);
        } 
		return specials;
	}
	public TerrainDTO getTerrain(String ident) {
        KieSession ksession = getSession();
		TerrainDTO terrain= new TerrainDTO();
		QueryResults results = ksession.getQueryResults("getTerrain", new Object[]{ident});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            terrain = (TerrainDTO)row.get("$terrain"); 
        } 
		return terrain;
	}
	public Collection<TerrainDTO> getTerrains(Collection<String> idents) {
        KieSession ksession = getSession();
		Collection<TerrainDTO> terrains= new ArrayList<TerrainDTO>();
		QueryResults results = ksession.getQueryResults("getTerrains", new Object[]{idents});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            TerrainDTO terrain = (TerrainDTO)row.get("$terrain");
            terrains.add(terrain);
        } 
		return terrains;
	}
	public Collection<TerrainDTO> getTerrains() {
        KieSession ksession = getSession();
		Collection<TerrainDTO> terrains= new ArrayList<TerrainDTO>();
		QueryResults results = ksession.getQueryResults("getAllTerrains", new Object[]{});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            TerrainDTO terrain = (TerrainDTO)row.get("$terrain");
            terrains.add(terrain);
        } 
		return terrains;
	}
	public TileDTO getTile(Long id) {
        KieSession ksession = getSession();
		TileDTO tile= new TileDTO();
		QueryResults results = ksession.getQueryResults("getTile", new Object[]{id});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            tile = (TileDTO)row.get("$tile"); 
        } 
		return tile;
	}
	public Collection<TileDTO> getTiles(Collection<Long> ids) {
        KieSession ksession = getSession();
		QueryResults results = ksession.getQueryResults("getTiles", new Object[]{ids});
		Collection<TileDTO> tiles=  new ArrayList<TileDTO>();
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            TileDTO tile = (TileDTO)row.get("$tile");
            tiles.add(tile);
        } 
		return tiles;
	}
	public Collection<TileDTO> getTiles() {
        KieSession ksession = getSession();
		QueryResults results = ksession.getQueryResults("getAllTiles", new Object[]{});
		Collection<TileDTO> tiles=  new ArrayList<TileDTO>();
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            TileDTO tile = (TileDTO)row.get("$tile");
            tiles.add(tile);
        } 
		return tiles;
	}
	public TileDTO getTileByCoordinate(Coordinate coord) {
        KieSession ksession = getSession();
		TileDTO tile= new TileDTO();
		QueryResults results = ksession.getQueryResults("getTileByPosition", new Object[]{coord.getX(),coord.getY()});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            tile = (TileDTO)row.get("$tile"); 
        } 
		return tile;
	}
	public Collection<TileDTO> getTilesByCoordinates(Collection<Coordinate> coords) {
		Collection<TileDTO> tiles = new ArrayList<TileDTO>();
		for(Coordinate c:coords){
			tiles.add(this.getTileByCoordinate(c));
		}
		return tiles;
	}
	public TileImprovementDTO getTileImprovement(String ident) {
        KieSession ksession = getSession();
		TileImprovementDTO tileImp= new TileImprovementDTO();
		QueryResults results = ksession.getQueryResults("getTileImprovement", new Object[]{ident});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            tileImp = (TileImprovementDTO)row.get("$tileImprovement"); 
        } 
		return tileImp;
	}
	public Collection<TileImprovementDTO> getTileImprovements(Collection<String> ident) {
        KieSession ksession = getSession();
		Collection<TileImprovementDTO> tileImps= new ArrayList<TileImprovementDTO>();
		QueryResults results = ksession.getQueryResults("getTileImprovements", new Object[]{ident});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            TileImprovementDTO tileImp = (TileImprovementDTO)row.get("$tileImprovement"); 
            tileImps.add(tileImp);
        } 
		return tileImps;
	}
	public Collection<TileImprovementDTO> getTileImprovements() {
        KieSession ksession = getSession();
		Collection<TileImprovementDTO> tileImps= new ArrayList<TileImprovementDTO>();
		QueryResults results = ksession.getQueryResults("getAllTileImprovements", new Object[]{});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            TileImprovementDTO tileImp = (TileImprovementDTO)row.get("$tileImprovement"); 
            tileImps.add(tileImp);
        } 
		return tileImps;
	}
	public UnitDTO getUnit(Long id) {
        KieSession ksession = getSession();
		UnitDTO unit= new UnitDTO();
		QueryResults results = ksession.getQueryResults("getUnit", new Object[]{id});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            unit = (UnitDTO)row.get("$unit"); 
        } 
		return unit;
	}
	public Collection<UnitDTO> getUnits(Collection<Long> ids) {
        KieSession ksession = getSession();
		Collection<UnitDTO> units= new ArrayList<UnitDTO>();
		QueryResults results = ksession.getQueryResults("getUnits", new Object[]{ids});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            UnitDTO unit = (UnitDTO)row.get("$unit");
            units.add(unit);
        } 
		return units;
	}
	public Collection<UnitDTO> getUnits() {
        KieSession ksession = getSession();
		Collection<UnitDTO> units= new ArrayList<UnitDTO>();
		QueryResults results = ksession.getQueryResults("getAllUnits", new Object[]{});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            UnitDTO unit = (UnitDTO)row.get("$unit");
            units.add(unit);
        } 
		return units;
	}
	public UnitTypeDTO getUnitType(String ident) {
        KieSession ksession = getSession();
		UnitTypeDTO unitType= new UnitTypeDTO();
		QueryResults results = ksession.getQueryResults("getUnitType", new Object[]{ident});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            unitType = (UnitTypeDTO)row.get("$unitType"); 
        } 
		return unitType;
	}
	public Collection<UnitTypeDTO> getUnitTypes(Collection<String> idents) {
        KieSession ksession = getSession();
		Collection<UnitTypeDTO> unitTypes= new ArrayList<UnitTypeDTO>();
		QueryResults results = ksession.getQueryResults("getUnitTypes", new Object[]{idents});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            UnitTypeDTO unitType = (UnitTypeDTO)row.get("$unitType");
            unitTypes.add(unitType);
        } 
		return unitTypes;
	}
	public Collection<UnitTypeDTO> getUnitTypes() {
        KieSession ksession = getSession();
		Collection<UnitTypeDTO> unitTypes= new ArrayList<UnitTypeDTO>();
		QueryResults results = ksession.getQueryResults("getAllUnitTypes", new Object[]{});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            UnitTypeDTO unitType = (UnitTypeDTO)row.get("$unitType");
            unitTypes.add(unitType);
        } 
		return unitTypes;
	}
	public void startGame() {
		KieSession ksession = getSession();
		// initial turn to activate rules that should be fired during first run of turn process
		ksession.getEntryPoint("GameControlStream").insert(new TurnEvent());
        
		HashMap<String,Object>params = new HashMap<String, Object>();
		Properties properties = new Properties();
		String turnLength = "";
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			properties.load(classLoader.getResourceAsStream("game.properties"));
		  	turnLength=properties.getProperty("turn.length");
		  	params.put("timer-delay", turnLength);
		  	ProcessInstance turnProcess = ksession.createProcessInstance("cz.muni.fi.civ.newohybat.bpmn.turn", params);
			ksession.startProcessInstance(turnProcess.getId());
			ksession.insert(turnProcess);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	public void stopGame() {
        KieSession ksession = getSession();
		QueryResults results = ksession.getQueryResults("getTurnProcess", new Object[]{});
		Iterator<QueryResultsRow> i = results.iterator();
		ProcessInstance pi = null;
		if(i.hasNext()){
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            pi = (ProcessInstance)row.get("$process");
            ksession.delete(ksession.getFactHandle(pi));
            ksession.abortProcessInstance(pi.getId());
        }
		
	}

	public void cityBeginCityImprovement(Long cityId, String impIdent) {
		KieSession ksession = getSession();
		CityDTO city = getCity(cityId);
		city.setCurrentImprovement(impIdent);
		ksession.update(ksession.getFactHandle(city), city);
		ksession.fireAllRules();
		persistenceHelper.persistDirtyObjects();
	}

	public void cityBeginUnit(Long cityId, String unitTypeIdent) {
		KieSession ksession = getSession();
		CityDTO city = getCity(cityId);
		city.setCurrentUnit(unitTypeIdent);
		ksession.update(ksession.getFactHandle(city), city);
		ksession.fireAllRules();
		persistenceHelper.persistDirtyObjects();
	}

	public void playerBeginAdvance(Long playerId, String advanceIdent) {
		KieSession ksession = getSession();
		PlayerDTO player = getPlayer(playerId);
		player.setCurrentAdvance(advanceIdent);
		ksession.update(ksession.getFactHandle(player), player);
		ksession.fireAllRules();
		persistenceHelper.persistDirtyObjects();
	}

	public void playerChangeGovernment(Long playerId, String governmentIdent) {
		KieSession ksession = getSession();
		PlayerDTO player = getPlayer(playerId);
		player.setGovernment(governmentIdent);
		ksession.update(ksession.getFactHandle(player), player);
		ksession.fireAllRules();
		persistenceHelper.persistDirtyObjects();
		
	}

	public void unitBeginAction(Long unitId, String actionIdent) {
		KieSession ksession = getSession();
		UnitDTO unit = getUnit(unitId);
		unit.setCurrentAction(actionIdent);
		ksession.update(ksession.getFactHandle(unit), unit);
		ksession.fireAllRules();
		persistenceHelper.persistDirtyObjects();
	}
	

	public void unitBeginMove(Long id, String actionIdent,Long targetTile) {
		KieSession ksession = getSession();
		UnitDTO unit = getUnit(id);
		unit.setTargetTile(targetTile);
		unit.setCurrentAction(actionIdent);
		ksession.update(ksession.getFactHandle(unit), unit);
		ksession.fireAllRules();
		persistenceHelper.persistDirtyObjects();
	}

	public void load(Integer sessionId) {
//		ksession.destroy();
		loader.loadSession(sessionId);
	}

	public void init() {
		loader.initSession();
	}

	public void cityStopCityImprovement(Long cityId) {
		KieSession ksession = getSession();
		ksession.getEntryPoint("ActionCanceledStream").insert(new CityImprovementEvent(cityId));
		ksession.fireAllRules();
		persistenceHelper.persistDirtyObjects();
	}

	public void cityStopUnit(Long cityId) {
		KieSession ksession = getSession();
		ksession.getEntryPoint("ActionCanceledStream").insert(new UnitEvent(cityId));
		ksession.fireAllRules();
		persistenceHelper.persistDirtyObjects();
	}

	public void playerStopAdvance(Long playerId) {
		KieSession ksession = getSession();
		ksession.getEntryPoint("ActionCanceledStream").insert(new AdvanceEvent(playerId));
		ksession.fireAllRules();
		persistenceHelper.persistDirtyObjects();
	}

	public void unitStopAction(Long id) {
		KieSession ksession = getSession();
		ksession.getEntryPoint("ActionCanceledStream").insert(new TileImprovementEvent(id));
		ksession.fireAllRules();
		persistenceHelper.persistDirtyObjects();
	}

	public void unitStopMove(Long id) {
		KieSession ksession = getSession();
		ksession.getEntryPoint("ActionCanceledStream").insert(new MoveEvent(id));
		ksession.fireAllRules();
		persistenceHelper.persistDirtyObjects();
	}

	public void removeDeadUnit(Long id) {
		KieSession ksession = getSession();
		UnitDTO dead = null;
		QueryResults results = ksession.getQueryResults("Get Unit If Dead", new Object[]{id});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            dead = (UnitDTO)row.get("$unit"); 
        }
		if(dead!=null){
			ksession.delete(ksession.getFactHandle(dead));
			ksession.fireAllRules();
			persistenceHelper.persistDirtyObjects();
		}
	}
	
	

	
}
