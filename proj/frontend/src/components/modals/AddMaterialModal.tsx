import { useEffect, useState } from "react";
import { getAllMaterials, attachMaterial, getMaterialsByCampaign } from "../../api/apiMaterials";
import type { Material } from "../../api/apiMaterials";
import MaterialCardSelect from "../cards/MaterialCardSelect";

interface AddMaterialModalProps {
  campaignId: number;
  onClose: () => void;
  onAdded: () => void;
  // Adiciona a prop 'onView'
  onView: (materialId: number, materialType: string) => void;
}

export default function AddMaterialModal({
  campaignId,
  onClose,
  onAdded,
  onView, // Recebe a nova prop
}: AddMaterialModalProps) {
  const [materials, setMaterials] = useState<Material[]>([]);
useEffect(() => {
  async function load() {
    // Fetch both in parallel
    const [all, attached] = await Promise.all([
      getAllMaterials(),
      getMaterialsByCampaign(campaignId)
    ]);

    console.log("Fetched materials:", all);
    console.log("Attached materials:", attached);

    // Build a set of attached material IDs (recommended)
    const attachedIds = new Set(attached.map((x: any) => x.id));

    // Filter: only materials NOT attached yet
    const unassigned = all.filter((mat: any) => !attachedIds.has(mat.id));

    setMaterials(unassigned);
  }

  load();
}, [campaignId]);
  

  const handleAdd = async (materialId: number) => {
    await attachMaterial(campaignId, materialId);
    onAdded();
    onClose();
  };

  return (
    <div className="fixed inset-0 bg-transparent flex justify-center items-center p-4">
      <div className="bg-white p-6 rounded-xl shadow-2xl w-full max-w-xl">
        <h2 className="text-xl font-semibold mb-4 text-gray-800 border-b pb-3">
          Add Material
        </h2>

        {materials.length === 0 && (
          <p className="text-sm text-gray-500 mb-2 p-4 bg-gray-50 rounded-lg">
            No materials available to link.
          </p>
        )}

        <div className="
          grid gap-4 
          grid-cols-[repeat(auto-fill,minmax(260px,1fr))]
          justify-center
          max-h-[430px]
          overflow-y-auto
          pr-2
        ">
            {materials.map((mat) => (
                <MaterialCardSelect
                    key={mat.id}
                    material={mat}
                    onAdd={() => handleAdd(mat.id)}
                    onView={() => onView(mat.id, mat.type)} 
                />
            ))}
        </div>

        <div className="pt-4 mt-4 border-t flex justify-end">
            <button
                className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-100 transition shadow-sm"
                onClick={onClose}
            >
                Cancel
            </button>
        </div>
      </div>
    </div>
  );
}