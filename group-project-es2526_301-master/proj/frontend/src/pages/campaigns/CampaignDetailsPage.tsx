import { useParams, useNavigate } from "react-router-dom";
import { useState, useEffect } from "react";
import {
  ChevronDown,
  Calendar,
  Plus,
  Tag,
  Eye,
  WorkflowIcon,
} from "lucide-react";
import { getCampaignById, updateCampaign } from "../../api/apiCampaigns";
import { getWorkflowByCampaignId } from "../../api/apiWorkflows";
import type { Campaign } from "../../types/campaign";
import type { Workflow } from "../../types/workflow";
import BackButton from "../../components/buttons/BackButton";
import { useUser } from "../../context/UserContext";
import MaterialCardRenderer from "../../components/cards/MaterialCardRenderer";
import {
  getMaterialsByCampaign,
  type Material,
  detachMaterial
} from "../../api/apiMaterials";
import AddMaterialModal from "../../components/modals/AddMaterialModal";
import ConfirmationModal  from "../../components/modals/ConfirmationModal";
import { useFlags } from "flagsmith/react";



import { SpanStatusCode } from "@opentelemetry/api";
import useTracer from "../../hooks/useTracer";

const CampaignDetailsPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [campaign, setCampaign] = useState<Campaign | null>(null);
  const [workflow, setWorkflow] = useState<Workflow | null>(null);
  const [showStatusMenu, setShowStatusMenu] = useState(false);
  const [updating, setUpdating] = useState(false);
  const { hasRole } = useUser();
  const [showAddModal, setShowAddModal] = useState(false);
  const [materials, setMaterials] = useState<Material[]>([]);
  const [materialToRemove, setMaterialToRemove] = useState<Material | null>(null);
  const [showWorkflowWarning, setShowWorkflowWarning] = useState(false);
  const [pendingStatus, setPendingStatus] = useState<Campaign["status"] | null>(null);
  const tracer = useTracer();

  const flags = useFlags(["enable_campaign_materials"]);
  const enableCampaignMaterials = flags?.enable_campaign_materials?.enabled ?? true;


  useEffect(() => {
    tracer.startActiveSpan("Page.CampaignDetails.Load", (span) => {
      span.setAttribute("page.name", "CampaignDetailsPage");
      span.setAttribute("campaign.id", id ?? "unknown");
      span.setAttribute("user.role.MARKETING_MANAGER", hasRole("MARKETING_MANAGER"));

      span.addEvent("page_render_start");

      return () => {
        span.addEvent("page_render_complete");
        span.end();
      };
    });
  }, [id]);

  useEffect(() => {
    tracer.startActiveSpan("API.GetCampaignById", async (span) => {
      span.setAttribute("campaign.id", id ?? "unknown");

      try {
        const data = await getCampaignById(Number(id));
        setCampaign(data);

        span.setAttribute("api.success", true);
        span.addEvent("campaign_fetch_success");
      } catch (err) {
        span.setAttribute("api.success", false);
        if (err instanceof Error) {
          span.recordException(err);
        } else {
          span.recordException(new Error(String(err)));
        }
        span.setStatus({ code: SpanStatusCode.ERROR });

        console.error("Error fetching campaign:", err);
      } finally {
        span.end();
      }
    });
  }, [id]);

  useEffect(() => {
    tracer.startActiveSpan("API.GetWorkflowByCampaignId", async (span) => {
      span.setAttribute("campaign.id", id ?? "unknown");

      try {
        const data = await getWorkflowByCampaignId(Number(id));
        setWorkflow(data);

        span.setAttribute("api.success", true);
      } catch (err: any) {
        if (err.response?.status === 404) {
          setWorkflow(null);
          span.setAttribute("api.success", false);
          span.setAttribute("api.status", 404);
        } else {
          console.error("Error fetching workflow:", err);
          span.setAttribute("api.success", false);
          span.recordException(err as Error);
          span.setStatus({ code: SpanStatusCode.ERROR });
        }
      } finally {
        span.end();
      }
    });
  }, [id]);

  useEffect(() => {
    const fetchCampaignMaterials = async () => {
      if (!id) return;
      try {
        const data = await getMaterialsByCampaign(Number(id));

        const normalized = data.map((m: any) => ({
          ...m,
          type: (m.dtype || m.type || "").toUpperCase()
        }));

        setMaterials(normalized);
      } catch (err) {
        console.error("Error fetching campaign materials:", err);
      }
    };

    fetchCampaignMaterials();
  }, [id]);

  useEffect(() => {
    const fetchCampaignMaterials = async () => {
      if (!id) return;
      try {
        const data = await getMaterialsByCampaign(Number(id));

        // normalizar types
        const normalized = data.map((m: any) => ({
          ...m,
          type: (m.dtype || m.type || "").toUpperCase()
        }));

        setMaterials(normalized);
      } catch (err) {
        console.error("Error fetching campaign materials:", err);
      }
    };

    fetchCampaignMaterials();
  }, [id]);


  const handleStatusChange = async (newStatus: Campaign["status"]) => {
    tracer.startActiveSpan("User.Action.ChangeCampaignStatus", async (span) => {
      if (!campaign) {
        span.end();
        return;
      }

      span.setAttribute("campaign.id", campaign.id);
      span.setAttribute("status.old", campaign.status);
      span.setAttribute("status.new", newStatus);

      // Check if switching to ACTIVE from IN_PROGRESS or FINISHED without proper workflow
      if (newStatus === "ACTIVE" && 
          (campaign.status === "IN_PROGRESS" || campaign.status === "FINISHED") &&
          (!workflow || workflow.isReadyToUse === false)) {
        setPendingStatus(newStatus);
        setShowWorkflowWarning(true);
        setShowStatusMenu(false);
        span.addEvent("workflow_warning_shown");
        span.end();
        return;
      }

      setUpdating(true);
      try {
        const updated = { ...campaign, status: newStatus };
        await updateCampaign(campaign.id, updated);
        setCampaign(updated);

        span.setAttribute("api.success", true);
        span.addEvent("campaign_status_updated");
      } catch (err) {
        span.setAttribute("api.success", false);
        if (err instanceof Error) {
          span.recordException(err);
        } else {
          span.recordException(new Error(String(err)));
        }
        console.error("Error updating campaign:", err);
        alert("Failed to update campaign status. Please try again.");
      } finally {
        setUpdating(false);
        setShowStatusMenu(false);
        span.end();
      }
    });
  };

  const confirmStatusChange = async () => {
    if (!campaign || !pendingStatus) return;

    setShowWorkflowWarning(false);
    setUpdating(true);

    try {
      const updated = { ...campaign, status: pendingStatus };
      await updateCampaign(campaign.id, updated);
      setCampaign(updated);
    } catch (err) {
      console.error("Error updating campaign:", err);
      alert("Failed to update campaign status. Please try again.");
    } finally {
      setUpdating(false);
      setPendingStatus(null);
    }
  };

  const handleToggleStatusMenu = () => {
    tracer.startActiveSpan("UI.ToggleStatusMenu", (span) => {
      span.setAttribute("menu.opening", !showStatusMenu);
      span.end();
    });

    setShowStatusMenu(!showStatusMenu);
  };

  const handleNavigateWorkflow = () => {
    tracer.startActiveSpan("User.Action.NavigateToWorkflow", (span) => {
      span.setAttribute("campaign.id", id ?? "unknown");
      span.addEvent("navigate_to_workflow");

      navigate(`/app/workflows/${id}`);
      span.end();
    });
  };

  const getStatusStyle = (status: Campaign["status"]) => {
    switch (status) {
      case "ACTIVE":
        return "bg-green-100 text-green-700";
      case "IN_PROGRESS":
        return "bg-yellow-100 text-yellow-700";
      case "FINISHED":
        return "bg-gray-200 text-gray-800";
      default:
        return "bg-gray-100 text-gray-700";
    }
  };

  const handleRemoveMaterial = (material: Material) => {
    setMaterialToRemove(material); 
  };
  
  const confirmMaterialRemoval = async () => {
    if (!id || !materialToRemove) return;
    
    setMaterialToRemove(null); 
    
    try {
        await detachMaterial(Number(id), materialToRemove.id); 

        setMaterials(materials.filter(m => m.id !== materialToRemove.id)); 
        
    } catch (err) {
        console.error("Error removing material from campaign:", err);
        alert("Failed to remove material from campaign. Please try again.");
    }
  };

  return (
    <div className="min-h-screen bg-[#F9FAFB] flex flex-col px-8 py-10">
      <BackButton to="/app/campaigns" label="Back to Campaigns" />

      <div className="mt-6 bg-white border border-[#E5E7EB] rounded-xl shadow-sm p-8 flex flex-col md:flex-row justify-between gap-8 items-start md:items-center transition">
        <div className="flex-1">
          <h1 className="text-xl font-semibold text-[#111827]">
            {campaign?.name || "Unnamed Campaign"}
          </h1>

          <p className="text-[#6B7280] text-sm mt-2 leading-relaxed max-w-2xl">
            {campaign?.description || "No description available."}
          </p>

          <p className="flex items-center gap-2 text-sm text-[#374151] font-medium mt-4">
            <Calendar className="w-4 h-4 opacity-80" />
            <span>Due:</span>{" "}
            {campaign?.dueDate
              ? new Date(campaign.dueDate).toLocaleDateString("en-GB", {
                  day: "2-digit",
                  month: "short",
                  year: "numeric",
                })
              : "N/A"}
          </p>

          {campaign?.segment && campaign.segment.length > 0 && (
            <div className="flex items-center gap-2 mt-3 flex-wrap">
              <Tag className="w-4 h-4" />
              <span className="text-sm font-medium text-[#111827]">
                Segments:
              </span>
              {campaign.segment.map((seg) => (
                <span
                  key={seg.id}
                  className="text-xs bg-[#E0E7FF] text-[#1E3A8A] font-medium px-2 py-1 rounded-md"
                >
                  {seg.name}
                </span>
              ))}
            </div>
          )}
        </div>

        <div className="flex flex-col items-end gap-3">
          <div className="flex flex-col items-end">
            <span className="text-sm text-[#6B7280] font-medium">
              Campaign Status
            </span>
            <span
              className={`flex items-center gap-2 mt-1 text-sm font-medium px-3 py-1 rounded-full ${getStatusStyle(
                campaign?.status || "ACTIVE"
              )}`}
            >
              <span
                className={`w-2 h-2 rounded-full ${
                  campaign?.status === "ACTIVE"
                    ? "bg-green-500"
                    : campaign?.status === "IN_PROGRESS"
                    ? "bg-yellow-500"
                    : "bg-gray-500"
                }`}
              ></span>
              {campaign?.status || "UNDEFINED"}
            </span>
            <p className="text-xs text-[#6B7280] mt-2">
              Created:{" "}
              {campaign?.createdAt
                ? new Date(campaign.createdAt).toLocaleDateString("en-GB", {
                    day: "2-digit",
                    month: "short",
                    year: "numeric",
                  })
                : "N/A"}
            </p>
          </div>

          <div className="relative">
            <button
              onClick={handleToggleStatusMenu}
              disabled={updating}
              className="bg-[#2563EB] hover:bg-[#1D4ED8] text-white text-sm font-medium px-5 py-2.5 rounded-lg flex items-center gap-2 transition"
            >
              {updating ? "Updating..." : "Change Status"}
              <ChevronDown
                className={`w-4 h-4 transition-transform ${
                  showStatusMenu ? "rotate-180" : ""
                }`}
              />
            </button>

            {showStatusMenu && (
              <div className="absolute right-0 mt-2 w-40 bg-white border border-[#E5E7EB] rounded-lg shadow-lg z-10">
                {["ACTIVE", "IN_PROGRESS", "FINISHED"].map((option) => (
                  <button
                    key={option}
                    onClick={() =>
                      handleStatusChange(option as Campaign["status"])
                    }
                    className={`w-full text-left px-4 py-2 text-sm hover:bg-[#F3F4F6] ${
                      campaign?.status === option
                        ? "font-semibold text-[#2563EB]"
                        : "text-[#374151]"
                    }`}
                  >
                    {option.replace("_", " ")}
                  </button>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      

      {/* ==== MATERIALS SECTION ==== */}
      {enableCampaignMaterials && (
        <div className="mt-10">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-lg font-semibold">Campaign Materials</h2>

            {hasRole("MARKETING_MANAGER") && (
              <button
                onClick={() => setShowAddModal(true)}
                className="bg-[#2563EB] hover:bg-[#1D4ED8] text-white text-sm font-medium px-4 py-2 rounded-lg flex items-center gap-2"
              >
                <Plus className="w-4 h-4" />
                Add Material
              </button>
            )}
          </div>

          {materials.length > 0 ? (
            <div className="grid gap-6 grid-cols-[repeat(auto-fill,minmax(320px,1fr))] justify-center">
              {materials.map((mat) => (
                <MaterialCardRenderer
                  key={mat.id}
                  material={mat}
                  onView={() => {
                    if (mat.type === "EMAIL") {
                      navigate(`/app/email-templates/view/${mat.id}`);
                    } else if (mat.type === "FORM") {
                      navigate(`/app/forms/view/${mat.id}`);
                    } else if (mat.type === "LANDING_PAGE") {
                      navigate(`/app/landing-pages/view/${mat.id}`);
                    } else if (mat.type === "POST") {
                      navigate(`/app/social-posts/view/${mat.id}`);
                    }
                  }}

                  onRemove={handleRemoveMaterial}

                  onDelete={() => console.log("TODO delete material")}
                />
              ))}
            </div>
          ) : (
            <p className="text-gray-500 text-sm">No materials linked to this campaign.</p>
          )}
        </div>
      )}


      <div className="mt-10 bg-[#EEF2FF] border border-[#DBEAFE] rounded-xl shadow-sm p-10 flex flex-col items-center text-center">
        <WorkflowIcon className="w-10 h-10 text-[#2563EB] mb-4" />
        <h2 className="text-lg font-semibold text-[#111827] mb-2">
          Campaign Workflow
        </h2>
        <p className="text-sm text-[#6B7280] mb-6 max-w-xl mx-auto">
          Visualize and manage your campaign automation flow with our
          drag-and-drop workflow builder.
        </p>

        {!workflow ? (
          <>
            {hasRole("MARKETING_MANAGER") && (
              <button
                disabled={!campaign?.id}
                onClick={() =>
                  navigate(`/app/workflows/create?campaignId=${campaign!.id}`)
                }
                className="bg-[#2563EB] hover:bg-[#1D4ED8] text-white text-sm font-medium px-6 py-3 rounded-lg transition flex items-center justify-center gap-2"
              >
                <Plus className="w-4 h-4" />
                Create New Workflow
              </button>
            )}
            {(campaign?.status === "ACTIVE" || campaign?.status === "IN_PROGRESS") && (
              <div className="mt-4 bg-yellow-50 border border-yellow-300 text-yellow-800 rounded-lg px-4 py-3 max-w-lg">
                <p className="text-sm font-medium">
                  ⚠️ Workflow not created yet. Campaign cannot capture leads until a workflow is created and ready to use.
                </p>
              </div>
            )}
          </>
        ) : (
          <>
            <button
              onClick={handleNavigateWorkflow}
              className="bg-[#2563EB] hover:bg-[#1D4ED8] text-white text-sm font-medium px-6 py-3 rounded-lg transition flex items-center justify-center gap-2"
            >
              <Eye className="w-4 h-4" />
              View Workflow
            </button>
            {(campaign?.status === "ACTIVE" || campaign?.status === "IN_PROGRESS") && workflow.isReadyToUse === false && (
              <div className="mt-4 bg-yellow-50 border border-yellow-300 text-yellow-800 rounded-lg px-4 py-3 max-w-lg">
                <p className="text-sm font-medium">
                  ⚠️ Workflow is not ready to use. Campaign cannot capture leads until the workflow is complete with all nodes connected and configured.
                </p>
              </div>
            )}
          </>
        )}
      </div>

      {showAddModal && (
        <AddMaterialModal
          campaignId={Number(id)}
          onClose={() => setShowAddModal(false)}
          onAdded={() => window.location.reload()}
          onView={(materialId: number, materialType: string) => {
            setShowAddModal(false);
            if (materialType === "EMAIL") {
              navigate(`/app/email-templates/view/${materialId}`);
            }
            else if (materialType === "FORM") {
              navigate(`/app/forms/view/${materialId}`);
            }
            else if (materialType === "LANDING_PAGE") {
              navigate(`/app/landing-pages/view/${materialId}`);
            }
            else if (materialType === "POST") {
              navigate(`/app/social-posts/view/${materialId}`);
            }
          }}
        />
      )}

      {materialToRemove && (
        <ConfirmationModal
          title={`Remove ${materialToRemove.type.replace("_", " ")}`}
          message={`Are you sure you want to remove "${materialToRemove.name}" from this campaign? This action only removes the link and DOES NOT delete the material permanently.`}
          confirmText="Remove from Campaign"
          onConfirm={confirmMaterialRemoval}
          onCancel={() => setMaterialToRemove(null)} // Fecha o modal
        />
      )}

      {showWorkflowWarning && (
        <ConfirmationModal
          title="Workflow Not Ready"
          message={
            !workflow 
              ? `This campaign doesn't have a workflow yet. Setting the campaign to ${pendingStatus} without a workflow means it won't be able to capture or process leads. Do you want to proceed anyway?`
              : `The workflow for this campaign is not complete. All nodes must be connected and configured properly. Setting the campaign to ${pendingStatus} without a ready workflow means it won't function correctly. Do you want to proceed anyway?`
          }
          confirmText={`Set to ${pendingStatus} Anyway`}
          onConfirm={confirmStatusChange}
          onCancel={() => {
            setShowWorkflowWarning(false);
            setPendingStatus(null);
          }}
        />
      )}
    </div>
  );
};

export default CampaignDetailsPage;
