import { useParams, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { getEmailTemplateById } from "../../api/apiEmailTemplates";
import type { EmailTemplate } from "../../types/emailTemplate";
import { ArrowLeft } from "lucide-react";

export default function EmailTemplateViewPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [template, setTemplate] = useState<EmailTemplate | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;

    const load = async () => {
      try {
        const data = await getEmailTemplateById(Number(id));
        setTemplate(data);
      } catch (error) {
        console.error("Failed to load email template", error);
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [id]);

  if (loading) {
    return <p className="p-6 text-gray-500">Loading template...</p>;
  }

  if (!template) {
    return <p className="p-6 text-red-500">Template not found.</p>;
  }

  return (
    <div className="p-10 bg-white max-w-5xl mx-auto rounded-xl shadow-sm">
      <button
        onClick={() => navigate(-1)}
        className="flex items-center gap-2 text-sm text-gray-600 hover:text-gray-900 mb-6"
      >
        <ArrowLeft className="w-4 h-4" />
        Back
      </button>

      <h1 className="text-2xl font-semibold text-gray-900 mb-2">
        {template.name}
      </h1>

      <p className="text-sm text-gray-500 mb-6">
        Subject: <span className="font-medium">{template.subject}</span>
      </p>

      <div
        className="border border-gray-200 rounded-lg p-6 bg-[#f8f9fb]"
        dangerouslySetInnerHTML={{ __html: template.body }}
      />
    </div>
  );
}
