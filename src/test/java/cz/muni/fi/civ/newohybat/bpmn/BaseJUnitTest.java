package cz.muni.fi.civ.newohybat.bpmn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.jbpm.workflow.instance.WorkflowProcessInstance;
import org.junit.Assert;
import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.AgendaGroupPoppedEvent;
import org.kie.api.event.rule.AgendaGroupPushedEvent;
import org.kie.api.event.rule.BeforeMatchFiredEvent;
import org.kie.api.event.rule.MatchCancelledEvent;
import org.kie.api.event.rule.MatchCreatedEvent;
import org.kie.api.event.rule.RuleFlowGroupDeactivatedEvent;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;


public abstract class BaseJUnitTest {
	protected static Logger logger = Logger.getAnonymousLogger();
	protected KieSession ksession;
	public static RuntimeManager manager;
	public static RuntimeEngine runtime;
	
    public void closeTestEnv(){
//    	localTaskService.dispose();
//    	taskSession.dispose();
    	ksession.dispose();
//    	emf.close();
    }
    
    protected void setSessions(){

		ksession = runtime.getKieSession();
    	appendListeners();
    }
    protected void appendListeners(){
    	ksession.addEventListener(
                new AgendaEventListener()
                {
					public void afterMatchFired(AfterMatchFiredEvent arg0) {
						// TODO Auto-generated method stub
						
					}
					public void afterRuleFlowGroupActivated(
							org.kie.api.event.rule.RuleFlowGroupActivatedEvent arg0) {
						((RuntimeEngine)(arg0.getKieRuntime())).getKieSession().fireAllRules();
						
					}
					public void afterRuleFlowGroupDeactivated(
							RuleFlowGroupDeactivatedEvent arg0) {
						// TODO Auto-generated method stub
						
					}
					public void agendaGroupPopped(AgendaGroupPoppedEvent arg0) {
						// TODO Auto-generated method stub
						
					}
					public void agendaGroupPushed(AgendaGroupPushedEvent arg0) {
						// TODO Auto-generated method stub
						
					}
					public void beforeMatchFired(BeforeMatchFiredEvent arg0) {
						// TODO Auto-generated method stub
						
					}
					public void beforeRuleFlowGroupActivated(
							org.kie.api.event.rule.RuleFlowGroupActivatedEvent arg0) {
						// TODO Auto-generated method stub
						
					}
					public void beforeRuleFlowGroupDeactivated(
							RuleFlowGroupDeactivatedEvent arg0) {
						// TODO Auto-generated method stub
						
					}
					public void matchCancelled(MatchCancelledEvent arg0) {
						// TODO Auto-generated method stub
						
					}
					public void matchCreated(MatchCreatedEvent arg0) {
						// TODO Auto-generated method stub
						
					}
                    
                }
                );
        ksession.addEventListener(new ProcessEventListener() {
		
			
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
//            	System.out.println(event);
                ((RuntimeEngine)event.getKieRuntime()).getKieSession().insert(((WorkflowProcessInstance)event.getProcessInstance()));
                ((RuntimeEngine)event.getKieRuntime()).getKieSession().fireAllRules();
            }
			
			public void afterProcessCompleted(ProcessCompletedEvent event){
            	if(event.getProcessInstance().getState()==WorkflowProcessInstance.STATE_COMPLETED){
	            	WorkflowProcessInstance wpi = (WorkflowProcessInstance)event.getProcessInstance();
	            	VariableScopeInstance variableScope = (VariableScopeInstance) wpi.getContextInstance(VariableScope.VARIABLE_SCOPE);
	            	Map<String,Object> variables = variableScope.getVariables();
	            	for(Object o:variables.values()){
	            		org.kie.api.runtime.rule.FactHandle h = ((RuntimeEngine)event.getKieRuntime()).getKieSession().getFactHandle(o);
	            		if(h!=null){
	            			((RuntimeEngine)event.getKieRuntime()).getKieSession().update(h, o);
	            		}
	            	}
            	}
            	((RuntimeEngine)event.getKieRuntime()).getKieSession().delete(((RuntimeEngine)event.getKieRuntime()).getKieSession().getFactHandle(event.getProcessInstance()));
	            	
//            	((StatefulKnowledgeSession) event.getKnowledgeRuntime()).fireAllRules();
            	
            }
			
			public void afterNodeTriggered(ProcessNodeTriggeredEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			public void afterNodeLeft(ProcessNodeLeftEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		}); 
    
    }

    protected List<String> getFiredRules(List<AfterMatchFiredEvent> events){
    	List<String> runRules = new ArrayList<String>();
		for(AfterMatchFiredEvent e:events){
			runRules.add(e.getMatch().getRule().getName());
		}
		return runRules;
    }
    
    public void assertProcessInstanceCompleted(long processInstanceId, KieSession ksession) {
    		Assert.assertNull(ksession.getProcessInstance(processInstanceId));
    	}
    	
    	public void assertProcessInstanceAborted(long processInstanceId, KieSession ksession) {
    		Assert.assertNull(ksession.getProcessInstance(processInstanceId));
    	}
    	
    	public void assertProcessInstanceActive(long processInstanceId, KieSession ksession) {
    			Assert.assertNotNull(ksession.getProcessInstance(processInstanceId));
    	}
	
	protected static class CustomProcessEventListener implements ProcessEventListener {

		public void afterNodeLeft(ProcessNodeLeftEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		public void afterNodeTriggered(ProcessNodeTriggeredEvent arg0) {
		}

		public void afterProcessCompleted(ProcessCompletedEvent event) {
			// TODO Auto-generated method stub
//			logger.log(Level.INFO, "Process has completed.");
//			logger.log(Level.INFO,"hej");
		}

		public void afterProcessStarted(ProcessStartedEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		public void afterVariableChanged(ProcessVariableChangedEvent arg0) {
			// TODO Auto-generated method stub
			logger.log(Level.INFO,arg0.toString());
		}

		public void beforeNodeLeft(ProcessNodeLeftEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		public void beforeNodeTriggered(ProcessNodeTriggeredEvent arg0) {
			logger.log(Level.INFO,arg0.toString());
		}

		public void beforeProcessCompleted(ProcessCompletedEvent arg0) {
//			// TODO Auto-generated method stub
//			logger.log(Level.INFO, "Process is going to be completed.");
		}

		public void beforeProcessStarted(ProcessStartedEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		public void beforeVariableChanged(ProcessVariableChangedEvent arg0) {
			// TODO Auto-generated method stub
			//logger.log(Level.INFO,arg0.toString());
		}
		
	}
	
}