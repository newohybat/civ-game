package cz.muni.fi.civ.newohybat.game.itemhandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;







import java.util.Set;

import javax.inject.Inject;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

import cz.muni.fi.civ.newohybat.game.init.PersistenceHelper;
import cz.muni.fi.civ.newohybat.game.init.SessionLoader;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.CityDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.UnitDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.iface.CivBackend;

public class PersistableCityWorkItemHandler implements WorkItemHandler {

	@Inject
	private SessionLoader loader;
	
	@Inject
	private PersistenceHelper helper;
	
	
	
	public void abortWorkItem(WorkItem arg0, WorkItemManager arg1) {
	
	}

	public void executeWorkItem(WorkItem item, WorkItemManager manager) {		
		KieSession ksession = loader.getKieSession();
		UnitDTO unit = (UnitDTO)item.getParameter("unit");
		CityDTO city = new CityDTO();
		city.setCityCentre(unit.getTile());
		city.setOwner(unit.getOwner());
		Set<Long> managedTiles = new HashSet<Long>();
		managedTiles.add(city.getCityCentre());
		city.setManagedTiles(managedTiles);
		city.setSize(1);
		city.setPeopleContent(1);
		city = helper.persistNewCity(city);
		ksession.insert(city);
		// unitService.remove(unit);
		unit.setCurrentAction(null);
		Map<String,Object> results = new HashMap<String, Object>();
		results.put("city", city);
		manager.completeWorkItem(item.getId(), results);
	}

}
