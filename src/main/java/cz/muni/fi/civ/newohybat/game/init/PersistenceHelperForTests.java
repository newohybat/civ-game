package cz.muni.fi.civ.newohybat.game.init;

import java.util.Iterator;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.internal.runtime.StatefulKnowledgeSession;

import cz.muni.fi.civ.newohybat.persistence.facade.dto.CityDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.UnitDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.iface.CivBackend;

@Alternative
public class PersistenceHelperForTests implements PersistenceHelper{
	@Inject
	@ApplicationScoped
	private SessionLoader loader;
	
	public void persistDirtyObjects(){
		KieSession ksession = loader.getKieSession();
		QueryResults results = ksession.getQueryResults("getDirtyObjects", new Object[]{});
		for (Iterator<QueryResultsRow> i = results.iterator(); i.hasNext();) { 
            QueryResultsRow row = (QueryResultsRow)i.next(); 
            Object dirtyObject = (Object)row.get("$dirtyObject");
            Object subject = (Object)row.get("$subject");
             
            ksession.delete(ksession.getFactHandle(dirtyObject));
            
            //backend.update(subject);
		}
	}

	public CityDTO persistNewCity(CityDTO city) {
		city.setId((long)Math.ceil(Math.random()*1000000));
		return city;
	}

	public UnitDTO persistNewUnit(UnitDTO unit) {
		unit.setId((long)Math.ceil(Math.random()*1000000));
		return unit;
	}
	
}