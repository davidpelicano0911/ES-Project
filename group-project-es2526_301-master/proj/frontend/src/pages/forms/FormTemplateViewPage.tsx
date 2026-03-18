import { useParams, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { getTemplate } from "../../api/apiFormBuilder";
import type { FormTemplate } from "../../api/apiFormBuilder";
import { ArrowLeft } from "lucide-react";
import { Model } from "survey-core";
import { Survey } from "survey-react-ui";
// Substituí o 'defaultV2.min.css' (que deu erro) por 'survey-core.css',
// que é o estilo base usado no FormBuilderPage, garantindo a visualização correta.
import "survey-core/survey-core.css"; 

export default function FormTemplateViewPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [template, setTemplate] = useState<FormTemplate | null>(null);
  const [loading, setLoading] = useState(true);
  const [parsedForm, setParsedForm] = useState<any>(null);

  useEffect(() => {
    if (!id) return;

    const load = async () => {
      try {
        const data = await getTemplate(id);
        setTemplate(data);

        if (data.formJson) {
          // O JSON do formulário pode estar como string ou objeto.
          let parsedData: any;
          try {
             parsedData =
                typeof data.formJson === "string"
                  ? JSON.parse(data.formJson)
                  : data.formJson;
          } catch (err) {
            console.error("Failed to parse formJson:", err);
            parsedData = {}; // Fallback
          }
         
          // O JSON real do SurveyJS está aninhado em 'settings.surveyJson'
          // no payload que o FormBuilderPage.tsx salva.
          const surveyJson = parsedData?.settings?.surveyJson ?? parsedData;

          setParsedForm(surveyJson);
        }
      } catch (error) {
        console.error("Failed to load form template:", error);
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [id]);

  if (loading) {
    return <p className="p-6 text-gray-500">Loading form template...</p>;
  }

  if (!template) {
    return <p className="p-6 text-red-500">Form template not found.</p>;
  }

  // --- Lógica de Renderização ---

  // Verifica se o JSON do formulário (que o SurveyJS precisa) está disponível
  const formHasContent = parsedForm && (parsedForm.pages || parsedForm.elements);

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
        {template.description || "No description available"}
      </p>

      <h2 className="text-lg font-medium mt-4 mb-2">Form Preview</h2>

      {formHasContent ? (
          // Usamos uma IIFE (função auto-invocável) para permitir
          // comandos de declaração (const, atribuição) dentro do JSX.
          (() => {
            const model = new Model(parsedForm);
            
            // Corrige o erro de TypeScript (propriedade 'readOnly' não existe) 
            // causado por tipos desatualizados, mas garante que a funcionalidade 
            // de somente leitura é aplicada.
            (model as any).readOnly = true; 

            return <Survey model={model} />;
          })()
      ) : (
        <p className="text-gray-500">
            {parsedForm 
                ? "The form configuration is empty or invalid. Please check the Form Builder."
                : "This form has no defined configuration (JSON)."}
        </p>
      )}
    </div>
  );
}