package cz.muni.fi.civ.newohybat.game.itemhandler;

import java.util.HashMap;
import java.util.Map;










import javax.inject.Inject;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.runtime.StatefulKnowledgeSession;

import cz.muni.fi.civ.newohybat.game.init.PersistenceHelper;
import cz.muni.fi.civ.newohybat.game.init.SessionLoader;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.CityDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.UnitDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.UnitTypeDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.iface.CivBackend;

public class PersistableUnitWorkItemHandler implements WorkItemHandler {

	@Inject
	private SessionLoader loader;
	
	@Inject
	private PersistenceHelper helper;
	
	
	public void abortWorkItem(WorkItem arg0, WorkItemManager arg1) {
	
	}

	public void executeWorkItem(WorkItem item, WorkItemManager manager) {
		KieSession ksession = loader.getKieSession();
		UnitTypeDTO unitType = (UnitTypeDTO)item.getParameter("unitType");
		CityDTO city = (CityDTO)item.getParameter("city");
		UnitDTO unit = new UnitDTO();
		unit.setType(unitType.getIdent());
		unit.setCurrentAction(null);
		unit.setDistanceHome(0);
		unit.setOwner(city.getOwner());
		unit.setTile(city.getCityCentre());
		
		unit = helper.persistNewUnit(unit);
		ksession.insert(unit);
		
		city.getHomeUnits().add(unit.getId());
		city.setCurrentUnit(null);
		Map<String,Object> results = new HashMap<String, Object>();
		results.put("unit", unit);
		manager.completeWorkItem(item.getId(), results);
	}

}
