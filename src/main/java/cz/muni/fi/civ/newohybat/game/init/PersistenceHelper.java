package cz.muni.fi.civ.newohybat.game.init;

import cz.muni.fi.civ.newohybat.persistence.facade.dto.CityDTO;
import cz.muni.fi.civ.newohybat.persistence.facade.dto.UnitDTO;

public interface PersistenceHelper {
	void persistDirtyObjects();
	public CityDTO persistNewCity(CityDTO city);
	public UnitDTO persistNewUnit(UnitDTO unit);
}
