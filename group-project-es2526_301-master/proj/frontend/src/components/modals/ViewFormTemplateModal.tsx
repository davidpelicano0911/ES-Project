import { useEffect, useState } from "react";
import { X } from "lucide-react";
import { Model } from "survey-core";
import { Survey } from "survey-react-ui";
import SuccessMessage from "../messages/SuccessMessage";
import FailMessage from "../messages/FailMessage";
import LoadingState from "../states/LoadingState";
import type { FormTemplate } from "../../types/formTemplate";

interface ViewFormTemplateModalProps {
  isOpen: boolean;
  form?: FormTemplate;  
  onClose: () => void;
}

const ViewFormTemplateModal = ({
  isOpen,
  form,
  onClose,
}: ViewFormTemplateModalProps) => {
  const [survey, setSurvey] = useState<Model | null>(null);
  const [toastMessage, setToastMessage] = useState<{
    type: "success" | "fail";
    text: string;
  } | null>(null);

  useEffect(() => {
    if (!isOpen || !form) return;

    if (form.schema) {
      setSurvey(new Model(form.schema));
      return;
    }

    console.log(`Fetching form with ID ${form.id} (mock)`);

    let fetchedJson: any = null;
    if (form.id === "1") {
      fetchedJson = {
        title: "Event Registration",
        elements: [
          { type: "text", name: "full_name", title: "Full Name" },
          { type: "text", name: "email", title: "Email Address" },
          { type: "text", name: "phone", title: "Phone Number" },
        ],
      };
    } else if (form.id === "2") {
      fetchedJson = {
        title: "Customer Feedback",
        elements: [
          { type: "rating", name: "satisfaction", title: "Satisfaction Level" },
          { type: "comment", name: "comments", title: "Additional Comments" },
        ],
      };
    }

    if (fetchedJson) setSurvey(new Model(fetchedJson));
    else {
      setToastMessage({ type: "fail", text: "Form not found." });
      setTimeout(() => setToastMessage(null), 3000);
    }
  }, [form, isOpen]);

  if (!isOpen) return null;

  return (
    <>
      {toastMessage && (
        <>
          {toastMessage.type === "success" ? (
            <SuccessMessage
              entity={toastMessage.text}
              onClose={() => setToastMessage(null)}
            />
          ) : (
            <FailMessage
              entity={toastMessage.text}
              onClose={() => setToastMessage(null)}
            />
          )}
        </>
      )}

      <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
        <div className="bg-white w-[900px] max-h-[90vh] rounded-2xl shadow-xl p-8 overflow-y-auto animate-fadeIn relative">
          <div className="flex justify-between items-center mb-5">
            <div>
              <h2 className="text-xl font-semibold text-[#111827]">
                {form?.name || "Form Preview"}
              </h2>
              <p className="text-sm text-[#6B7280]">
                Preview of your form template
              </p>
            </div>
            <button
              onClick={onClose}
              className="text-[#6B7280] hover:text-[#111827] p-2 rounded-md hover:bg-gray-100 transition-all cursor-pointer"
              title="Close"
            >
              <X className="h-5 w-5" />
            </button>
          </div>

          <div className="border border-[#E5E7EB] rounded-lg overflow-hidden bg-white shadow-inner p-6">
            {survey ? (
              <Survey model={survey} />
            ) : (
              <LoadingState message="Loading form preview..." />
            )}
          </div>

          <div className="flex justify-end mt-8 gap-4">
            <a
              href={`/forms/${form?.id}/view`}
              className="bg-blue-600 text-white px-5 py-2.5 rounded-lg text-sm font-medium shadow-sm hover:bg-blue-700 transition-all cursor-pointer"
            >
              Submit Form
            </a>
            <button
              onClick={onClose}
              className="border border-[#D1D5DB] text-[#111827] hover:bg-[#F9FAFB] px-5 py-2.5 rounded-lg text-sm font-medium shadow-sm flex items-center gap-2 cursor-pointer"
            >
              <X className="h-4 w-4" />
              Close
            </button>
          </div>
        </div>
      </div>
    </>
  );
};

export default ViewFormTemplateModal;
