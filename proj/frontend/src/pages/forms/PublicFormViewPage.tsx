import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import api from "../../api/apiConfig";
import { Model } from "survey-core";
import { Survey } from "survey-react-ui";
import "survey-core/survey-core.css";
import LoadingState from "../../components/states/LoadingState";
import FailMessage from "../../components/messages/FailMessage";
import type { FormTemplate } from "../../types/formTemplate";
import {
  extractTrackingMetadata,
  storeTrackingMetadata,
  formatMetadataForLogging,
  type TrackingMetadata,
} from "../../utils/trackingMetadata";

const PublicFormViewPage = () => {
  const { id } = useParams<{ id: string }>();
  const [form, setForm] = useState<FormTemplate | null>(null);
  const [survey, setSurvey] = useState<Model | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [metadata, setMetadata] = useState<TrackingMetadata>({});
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [submitSuccess, setSubmitSuccess] = useState(false);

  const handleFormSubmit = async (formData: any) => {
    setSubmitError(null);

    try {
      // Merge tracking metadata with form responses
      const responsesWithTracking = {
        ...formData,
        // Add tracking metadata to responses
        _tracking: {
          email: metadata.email || null,
          campaign: metadata.campaign || null,
          source: metadata.source || null,
          submittedAt: new Date().toISOString(),
          ...metadata,
        },
      };

      // Prepare submission payload with tracking metadata
      const submissionPayload = {
        formId: parseInt(id || "0"),
        responses: responsesWithTracking,
        email: metadata.email || null,
        campaign: metadata.campaign || null,
        source: metadata.source || null,
        metadata: metadata,
      };

      console.log("Submitting form with payload:", submissionPayload);

      // Call public form submission endpoint
      const response = await api.post(
        "/form-submissions/public",
        submissionPayload
      );

      console.log("Form submission successful:", response.data);
      setSubmitSuccess(true);

      // Show success message for 3 seconds, then reset
      setTimeout(() => {
        setSubmitSuccess(false);
      }, 3000);
    } catch (err) {
      console.error("Form submission error:", err);
      let errorMessage = "Failed to submit form";
      
      if (err instanceof Error) {
        errorMessage = err.message;
      } else if (typeof err === 'object' && err !== null && 'response' in err) {
        const axiosError = err as any;
        errorMessage = `Server Error: ${axiosError.response?.status} - ${axiosError.response?.data?.message || axiosError.response?.statusText || 'Unknown error'}`;
      }
      
      setSubmitError(errorMessage);
    }
  };

  useEffect(() => {
    const initializePage = async () => {
      try {
        // Extract and store tracking metadata from URL
        const extractedMetadata = extractTrackingMetadata();
        storeTrackingMetadata(extractedMetadata);
        setMetadata(extractedMetadata);

        // Log tracking metadata for debugging
        console.log(
          "Public Form View - Tracking metadata:",
          formatMetadataForLogging(extractedMetadata)
        );

        if (!id) {
          setError("Form ID is required");
          setLoading(false);
          return;
        }

        // Fetch form template from public endpoint (no authentication required)
        const response = await api.get(`/form-template/public/${id}`);
        const fetchedForm = response.data;
        setForm(fetchedForm as FormTemplate);

        // Parse formJson and create survey model
        if (fetchedForm && fetchedForm.formJson) {
          try {
            let formJson: any;
            if (typeof fetchedForm.formJson === "string") {
              formJson = JSON.parse(fetchedForm.formJson);
            } else {
              formJson = fetchedForm.formJson;
            }

            // Handle wrapped form (settings.surveyJson)
            const surveyJson = formJson?.settings?.surveyJson ?? formJson;
            setSurvey(new Model(surveyJson));
          } catch (err) {
            console.error("Failed to parse form JSON:", err);
            setError("Failed to parse form schema");
          }
        }
      } catch (err) {
        console.error("Error loading form:", err);
        setError("Failed to load form. Please try again.");
      } finally {
        setLoading(false);
      }
    };

    initializePage();
  }, [id]);

  // Handle form completion when survey is ready
  useEffect(() => {
    if (!survey) return;

    const handleComplete = () => {
      const responses = (survey as any).data || {};
      handleFormSubmit(responses);
    };

    // SurveyJS fires 'complete' event when user completes the survey
    (survey as any).onComplete.add(handleComplete);

    return () => {
      // Cleanup event listener
      (survey as any).onComplete.remove(handleComplete);
    };
  }, [survey]);

  if (loading) {
    return <LoadingState message="Loading form..." />;
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
        <FailMessage
          entity={error}
          onClose={() => setError(null)}
        />
      </div>
    );
  }

  if (!form) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <h1 className="text-2xl font-bold text-gray-900 mb-2">Form Not Found</h1>
          <p className="text-gray-600">
            The form you're looking for doesn't exist or has been removed.
          </p>
        </div>
      </div>
    );
  }

  return (
    <div className="w-full min-h-screen bg-white">
      {/* Form Content - Full Width */}
      {survey ? (
        <div className="w-full">
          <Survey model={survey} />
        </div>
      ) : (
        <div className="w-full h-screen flex items-center justify-center">
          <p className="text-center text-gray-500">This form has no content</p>
        </div>
      )}

      {/* Submission Error Message */}
      {submitError && (
        <div className="fixed bottom-4 left-4 right-4 md:right-auto md:w-96 bg-red-50 border border-red-200 rounded-lg shadow-lg p-4 z-40">
          <div className="flex items-start gap-3">
            <div className="text-red-600 font-semibold mt-0.5">✕</div>
            <div>
              <h3 className="text-sm font-semibold text-red-900">
                Submission Failed
              </h3>
              <p className="text-sm text-red-700 mt-1">{submitError}</p>
            </div>
            <button
              onClick={() => setSubmitError(null)}
              className="ml-auto text-red-600 hover:text-red-900 font-semibold"
            >
              ✕
            </button>
          </div>
        </div>
      )}

      {/* Submission Success Message */}
      {submitSuccess && (
        <div className="fixed bottom-4 left-4 right-4 md:right-auto md:w-96 bg-green-50 border border-green-200 rounded-lg shadow-lg p-4 z-40">
          <div className="flex items-start gap-3">
            <div className="text-green-600 font-semibold mt-0.5">✓</div>
            <div>
              <h3 className="text-sm font-semibold text-green-900">
                Form Submitted Successfully
              </h3>
              <p className="text-sm text-green-700 mt-1">
                Thank you for your submission. We'll be in touch soon.
              </p>
            </div>
          </div>
        </div>
      )}

    </div>
  );
};

export default PublicFormViewPage;
