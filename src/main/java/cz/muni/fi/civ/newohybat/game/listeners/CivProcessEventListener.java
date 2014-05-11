package cz.muni.fi.civ.newohybat.game.listeners;

import java.util.Map;

import javax.inject.Inject;

import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.internal.runtime.StatefulKnowledgeSession;

import cz.muni.fi.civ.newohybat.game.init.PersistenceHelper;


public class CivProcessEventListener implements ProcessEventListener{
	@Inject
	private PersistenceHelper helper;
	
	public void beforeVariableChanged(
			org.kie.api.event.process.ProcessVariableChangedEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public void beforeProcessStarted(ProcessStartedEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public void beforeProcessCompleted(
			org.kie.api.event.process.ProcessCompletedEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public void beforeNodeTriggered(ProcessNodeTriggeredEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public void beforeNodeLeft(ProcessNodeLeftEvent arg0) {
		// TODO Auto-generated method stub
	}
	
	public void afterVariableChanged(
			org.kie.api.event.process.ProcessVariableChangedEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public void afterProcessStarted(ProcessStartedEvent event) {
//		((StatefulKnowledgeSession)event.getKieRuntime()).insert((WorkflowProcessInstance)event.getProcessInstance());
//        event.getKieRuntime().insert(((ProcessInstance)event.getProcessInstance()));
    }
	
	public void afterProcessCompleted(ProcessCompletedEvent event){
    	if(event.getProcessInstance().getState()==WorkflowProcessInstance.STATE_COMPLETED){
        	WorkflowProcessInstance wpi = (WorkflowProcessInstance)event.getProcessInstance();
        	VariableScopeInstance variableScope = (VariableScopeInstance) wpi.getContextInstance(VariableScope.VARIABLE_SCOPE);
        	Map<String,Object> variables = variableScope.getVariables();
        	for(Object o:variables.values()){
        		if(o!=null){
        		org.kie.api.runtime.rule.FactHandle h = event.getKieRuntime().getFactHandle(o);
        		if(h!=null){
        			event.getKieRuntime().update(h, o);
        		}
        		}
        	}
    	}
    	FactHandle fH = event.getKieRuntime().getFactHandle(event.getProcessInstance());
    	if(fH!=null){
    		event.getKieRuntime().delete(fH);
    	}
    	((StatefulKnowledgeSession)event.getKieRuntime()).fireAllRules();
    	helper.persistDirtyObjects();
    }
	
	public void afterNodeTriggered(ProcessNodeTriggeredEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public void afterNodeLeft(ProcessNodeLeftEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
