import { useEffect, useState } from "react";
import { MoreHorizontal, PlayCircle, CheckCircle2, Loader2, AlertCircle } from "lucide-react";
import { getWorkflowByCampaignId, getWorkflowInstances } from "../../api/apiWorkflows";

interface WorkflowStats {
  id: number;
  name: string;
  active: number;
  completed: number;
  total: number;
}

interface WorkflowStatusWidgetProps {
  campaignId?: number;
}

const WorkflowStatusWidget = ({ campaignId }: WorkflowStatusWidgetProps) => {
  const [workflowsStats, setWorkflowsStats] = useState<WorkflowStats[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(false);

  useEffect(() => {
    const fetchData = async () => {
      if (!campaignId) return;
      
      setLoading(true);
      setError(false);
      
      try {
        console.log(`Fetching workflow for campaign ${campaignId}...`);
        
        let workflowDefinition = null;
        try {
            workflowDefinition = await getWorkflowByCampaignId(campaignId);
        } catch (e) {
            console.warn("No workflow found specifically linked to this campaign.");
        }

        if (!workflowDefinition || !workflowDefinition.id) {
            console.log("No workflow returned from API.");
            setWorkflowsStats([]);
            setLoading(false);
            return;
        }

        console.log("Workflow found:", workflowDefinition);

        const workflowList = [workflowDefinition];

        const statsPromises = workflowList.map(async (wf: any) => {
          try {
            console.log(`Fetching instances for workflow ${wf.id}...`);
            const instances = await getWorkflowInstances(wf.id);
            console.log(`Instances for workflow ${wf.id}:`, instances);
            
            const completedCount = instances.filter(i => i.finished === true).length;
            const activeCount = instances.filter(i => i.finished === false).length;
            
            return {
              id: wf.id,
              name: wf.name || `Workflow #${wf.id}`,
              active: activeCount,
              completed: completedCount,
              total: activeCount + completedCount
            };
          } catch (err) {
            console.error(`Error fetching instances for workflow ${wf.id}`, err);
            return null;
          }
        });

        const results = await Promise.all(statsPromises);
        setWorkflowsStats(results.filter((item): item is WorkflowStats => item !== null));

      } catch (err) {
        console.error("Critical error in WorkflowStatusWidget:", err);
        setError(true);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [campaignId]);

  if (loading) {
    return (
      <div className="h-full w-full flex flex-col items-center justify-center text-gray-400">
        <Loader2 className="animate-spin mb-2" size={24} />
        <span className="text-xs">Loading workflow data...</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="h-full w-full flex flex-col items-center justify-center text-red-400">
        <AlertCircle className="mb-2" size={24} />
        <span className="text-xs">Unable to load workflow stats</span>
      </div>
    );
  }

  return (
    <div className="h-full w-full flex flex-col">
      <div className="flex justify-between items-center mb-4 px-1">
        <div className="flex items-center gap-2">
           <h3 className="font-semibold text-gray-800">Workflow Status</h3>
        </div>
        <MoreHorizontal size={16} className="text-gray-400 cursor-pointer" />
      </div>
      
      <div className="flex-1 overflow-y-auto pr-2 custom-scrollbar space-y-4">
        {workflowsStats.length === 0 ? (
           <div className="flex flex-col items-center justify-center h-40 text-gray-400 text-sm text-center px-4">
             <p>No workflow linked.</p>
             <p className="text-xs mt-1 text-gray-300">
                This campaign does not have a workflow associated via the backend relationship.
             </p>
           </div>
        ) : (
          workflowsStats.map((wf) => {
            const percentage = wf.total > 0 ? (wf.completed / wf.total) * 100 : 0;

            return (
              <div key={wf.id} className="group border border-gray-100 rounded-lg p-3 hover:border-blue-100 hover:bg-blue-50/20 transition-all">
                <div className="flex justify-between items-start mb-2">
                  <div className="flex items-center gap-2">
                    {percentage === 100 && wf.total > 0 ? (
                      <CheckCircle2 size={16} className="text-green-500" />
                    ) : (
                      <PlayCircle size={16} className="text-blue-500" />
                    )}
                    <span className="font-medium text-sm text-gray-700 truncate max-w-[150px]" title={wf.name}>
                      {wf.name}
                    </span>
                  </div>
                  <span className="text-[10px] px-2 py-0.5 rounded-full font-medium bg-gray-100 text-gray-600">
                    Total Leads: {wf.total}
                  </span>
                </div>
                
                <div className="flex justify-between text-xs text-gray-500 mb-1.5">
                  <span className="flex items-center gap-1">
                    <div className="w-2 h-2 rounded-full bg-blue-500"></div>
                    <strong>{wf.active}</strong> active
                  </span>
                  <span className="flex items-center gap-1">
                    <div className="w-2 h-2 rounded-full bg-green-500"></div>
                    <strong>{wf.completed}</strong> done ({percentage.toFixed(0)}%)
                  </span>
                </div>
                
                <div className="w-full h-2 flex rounded-full overflow-hidden bg-gray-100">
                  <div 
                    className="bg-green-500 h-full transition-all duration-500" 
                    style={{ width: `${percentage}%` }}
                  ></div>
                  <div 
                    className="bg-blue-400 h-full transition-all duration-500" 
                    style={{ width: `${100 - percentage}%` }}
                  ></div>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
};

export default WorkflowStatusWidget;